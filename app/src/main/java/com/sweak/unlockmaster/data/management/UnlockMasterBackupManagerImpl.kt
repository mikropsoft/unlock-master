package com.sweak.unlockmaster.data.management

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sweak.unlockmaster.data.local.database.UnlockMasterDatabase
import com.sweak.unlockmaster.data.local.database.entities.CounterPausedEventEntity
import com.sweak.unlockmaster.data.local.database.entities.CounterUnpausedEventEntity
import com.sweak.unlockmaster.data.local.database.entities.LockEventEntity
import com.sweak.unlockmaster.data.local.database.entities.ScreenOnEventEntity
import com.sweak.unlockmaster.data.local.database.entities.UnlockEventEntity
import com.sweak.unlockmaster.data.local.database.entities.UnlockLimitEntity
import com.sweak.unlockmaster.domain.management.UnlockMasterBackupManager
import com.sweak.unlockmaster.domain.model.UiThemeMode
import com.sweak.unlockmaster.domain.repository.TimeRepository
import com.sweak.unlockmaster.domain.repository.UserSessionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.lang.IllegalArgumentException
import java.nio.charset.Charset
import javax.inject.Inject

class UnlockMasterBackupManagerImpl @Inject constructor(
    private val unlockMasterDatabase: UnlockMasterDatabase,
    private val userSessionRepository: UserSessionRepository,
    private val timeRepository: TimeRepository
) : UnlockMasterBackupManager {

    private val backupFileCharset: Charset = Charsets.UTF_8

    override suspend fun createDataBackupFile(): ByteArray {
        val currentTimeInMillis = timeRepository.getCurrentTimeInMillis()

        val unlockEventsEntities = unlockMasterDatabase.unlockEventsDao().getAllUnlockEvents()
        var lockEventsEntities = unlockMasterDatabase.lockEventsDao().getAllLockEvents()

        // We have to add a matching LockEvent if the latest UnlockEvent does not have a matching
        // LockEvent in order to prevent an undefined screen time session that isn't closed:
        if (shouldAddMatchingLockEvent(unlockEventsEntities, lockEventsEntities)) {
            lockEventsEntities = lockEventsEntities.toMutableList().apply {
                add(LockEventEntity(currentTimeInMillis))
            }
        }

        val counterPausedEventsEntities =
            unlockMasterDatabase.counterPausedEventsDao().getAllCounterPausedEvents()
        var counterUnpausedEventsEntities =
            unlockMasterDatabase.counterUnpausedEventsDao().getAllCounterUnpausedEvents()

        // We have to add a matching CounterUnpausedEvent if the latest CounterPausedEvent does not
        // have a matching CounterUnpausedEvent in order to prevent an undefined counter pause
        // session that isn't closed:
        if (shouldAddMatchingCounterUnpausedEvent(
                counterPausedEventsEntities,
                counterUnpausedEventsEntities
            )
        ) {
            counterUnpausedEventsEntities =
                counterUnpausedEventsEntities.toMutableList().apply {
                    // If we're adding a CounterUnpausedEvent, a LockEvent must have been added
                    // previously so we make the CounterUnpausedEvent time slightly before LockEvent
                    // to preserve data integrity:
                    add(CounterUnpausedEventEntity(currentTimeInMillis - 1))
                }
        }

        val unlockLimitsEntities = unlockMasterDatabase.unlockLimitsDao().getAllUnlockLimits()
        val screenOnEventsEntities = unlockMasterDatabase.screenOnEventsDao().getAllScreenOnEvents()

        val userPreferences = UnlockMasterBackupData.UserPreferences(
            mobilizingNotificationsFrequencyPercentage =
            userSessionRepository.getMobilizingNotificationsFrequencyPercentage(),
            dailyWrapUpNotificationsTimeInMinutesPastMidnight =
            userSessionRepository.getDailyWrapUpNotificationsTimeInMinutesAfterMidnight(),
            uiThemeMode = userSessionRepository.getUiThemeModeFlow().first().name
        )

        val unlockMasterBackupData = UnlockMasterBackupData(
            backupCreationTimeInMillis = currentTimeInMillis,
            unlockEvents = unlockEventsEntities,
            lockEvents = lockEventsEntities,
            unlockLimits = unlockLimitsEntities,
            screenOnEvents = screenOnEventsEntities,
            counterPausedEvents = counterPausedEventsEntities,
            counterUnpausedEvents = counterUnpausedEventsEntities,
            userPreferences = userPreferences
        )

        val backupFileJsonString = GsonBuilder()
            .setPrettyPrinting()
            .create()
            .toJson(unlockMasterBackupData)

        return backupFileJsonString.toByteArray(backupFileCharset)
    }

    override suspend fun restoreDataFromBackupFile(backupFileBytes: ByteArray) {
        val backupFileJsonString = backupFileBytes.toString(backupFileCharset)

        val unlockMasterBackupData = Gson().fromJson(
            backupFileJsonString,
            UnlockMasterBackupData::class.java
        )

        unlockMasterDatabase.apply {
            runInTransaction {
                runBlocking {
                    applyLocalDataCorrections(unlockMasterBackupData.backupCreationTimeInMillis)

                    unlockEventsDao().insertAll(unlockMasterBackupData.unlockEvents)
                    lockEventsDao().insertAll(unlockMasterBackupData.lockEvents)
                    unlockLimitsDao().insertAll(unlockMasterBackupData.unlockLimits)
                    screenOnEventsDao().insertAll(unlockMasterBackupData.screenOnEvents)
                    counterPausedEventsDao().insertAll(unlockMasterBackupData.counterPausedEvents)
                    counterUnpausedEventsDao().insertAll(unlockMasterBackupData.counterUnpausedEvents)
                }
            }
        }

        userSessionRepository.apply {
            setMobilizingNotificationsFrequencyPercentage(
                unlockMasterBackupData.userPreferences.mobilizingNotificationsFrequencyPercentage
            )
            setDailyWrapUpNotificationsTimeInMinutesAfterMidnight(
                unlockMasterBackupData.userPreferences
                    .dailyWrapUpNotificationsTimeInMinutesPastMidnight
            )

            // If the backup uiThemeMode can't be recognized, the local value will be left:
            try {
                setUiThemeMode(
                    UiThemeMode.valueOf(unlockMasterBackupData.userPreferences.uiThemeMode)
                )
            } catch (ignored: IllegalArgumentException) { }
        }
    }

    private fun shouldAddMatchingLockEvent(
        unlockEventsEntities: List<UnlockEventEntity>,
        lockEventsEntities: List<LockEventEntity>
    ): Boolean {
        val latestUnlockEventEntity = unlockEventsEntities.maxByOrNull { it.timeInMillis }
        val latestLockEventEntity = lockEventsEntities.maxByOrNull { it.timeInMillis }

        return (latestLockEventEntity == null && latestUnlockEventEntity != null) ||
                (latestLockEventEntity != null && latestUnlockEventEntity != null &&
                        latestLockEventEntity.timeInMillis < latestUnlockEventEntity.timeInMillis)
    }

    private fun shouldAddMatchingCounterUnpausedEvent(
        counterPausedEventsEntities: List<CounterPausedEventEntity>,
        counterUnpausedEventsEntities: List<CounterUnpausedEventEntity>
    ): Boolean {
        val latestCounterPausedEventEntity =
            counterPausedEventsEntities.maxByOrNull { it.timeInMillis }
        val latestCounterUnpausedEventEntity =
            counterUnpausedEventsEntities.maxByOrNull { it.timeInMillis }

        return (latestCounterUnpausedEventEntity == null &&
                latestCounterPausedEventEntity != null) ||
                (latestCounterUnpausedEventEntity != null &&
                        latestCounterPausedEventEntity != null &&
                        latestCounterUnpausedEventEntity.timeInMillis < latestCounterPausedEventEntity.timeInMillis)
    }

    private suspend fun applyLocalDataCorrections(backupCreationTimeInMillis: Long) =
        // We have to account for the case when the backup and local data is overlapping. We always
        // remove local data generated before the backup creation time and "fill the gaps" left
        // after the deletion to preserve data integrity.
        unlockMasterDatabase.apply {
            val (unlockEventsBeforeBackup, unlockEventsAfterBackup) =
                unlockEventsDao().getAllUnlockEvents().partition {
                    it.timeInMillis <= backupCreationTimeInMillis
                }
            val (lockEventsBeforeBackup, lockEventsAfterBackup) =
                lockEventsDao().getAllLockEvents().partition {
                    it.timeInMillis <= backupCreationTimeInMillis
                }
            val (counterPausedEventsBeforeBackup, counterPausedEventsAfterBackup) =
                counterPausedEventsDao().getAllCounterPausedEvents().partition {
                    it.timeInMillis <= backupCreationTimeInMillis
                }
            val (counterUnpausedEventsBeforeBackup, counterUnpausedEventsAfterBackup) =
                counterUnpausedEventsDao().getAllCounterUnpausedEvents().partition {
                    it.timeInMillis <= backupCreationTimeInMillis
                }
            val screenOnEventsBeforeBackup = screenOnEventsDao().getAllScreenOnEvents().filter {
                it.timeInMillis <= backupCreationTimeInMillis
            }

            // Always removing all events from before backup time:
            unlockEventsDao().deleteAll(unlockEventsBeforeBackup)
            lockEventsDao().deleteAll(lockEventsBeforeBackup)
            counterPausedEventsDao().deleteAll(counterPausedEventsBeforeBackup)
            counterUnpausedEventsDao().deleteAll(counterUnpausedEventsBeforeBackup)
            screenOnEventsDao().deleteAll(screenOnEventsBeforeBackup)

            val firstLockEventAfterBackup =
                lockEventsAfterBackup.minByOrNull { it.timeInMillis }
            val firstUnlockEventAfterBackup =
                unlockEventsAfterBackup.minByOrNull { it.timeInMillis }
            val firstCounterPausedEventAfterBackup =
                counterPausedEventsAfterBackup.minByOrNull { it.timeInMillis }
            val firstCounterUnpausedEventAfterBackup =
                counterUnpausedEventsAfterBackup.minByOrNull { it.timeInMillis }

            val firstDifferentEventsAfterBackupWithTimeInMillis = mutableMapOf<Any, Long>().apply {
                firstLockEventAfterBackup?.let { put(it, it.timeInMillis) }
                firstUnlockEventAfterBackup?.let { put(it, it.timeInMillis) }
                firstCounterPausedEventAfterBackup?.let { put(it, it.timeInMillis) }
                firstCounterUnpausedEventAfterBackup?.let { put(it, it.timeInMillis) }
            }

            val firstEventAfterBackup = firstDifferentEventsAfterBackupWithTimeInMillis
                .minByOrNull { it.value }

            // If the first event after backup time is one of lock or counter unpause, then we have
            // to insert a matching unlock (and screen on) or counter pause event respectively.
            // In any other case the data is already integral and we don't need to take any action:
            when (firstEventAfterBackup?.key) {
                is LockEventEntity -> {
                    unlockEventsDao().insert(UnlockEventEntity(backupCreationTimeInMillis + 1))
                    screenOnEventsDao().insert(ScreenOnEventEntity(backupCreationTimeInMillis + 1))
                }

                is CounterUnpausedEventEntity -> {
                    counterPausedEventsDao().insert(
                        CounterPausedEventEntity(backupCreationTimeInMillis + 1)
                    )
                }
            }

            // With unlock limits we take the straightforward approach of just removing all local
            // unlock limits so that backup unlock limits take precedence:
            unlockLimitsDao().deleteAll(unlockLimitsDao().getAllUnlockLimits())
        }

    data class UnlockMasterBackupData(
        val backupCreationTimeInMillis: Long,
        val unlockEvents: List<UnlockEventEntity>,
        val lockEvents: List<LockEventEntity>,
        val unlockLimits: List<UnlockLimitEntity>,
        val screenOnEvents: List<ScreenOnEventEntity>,
        val counterPausedEvents: List<CounterPausedEventEntity>,
        val counterUnpausedEvents: List<CounterUnpausedEventEntity>,
        val userPreferences: UserPreferences
    ) {
        data class UserPreferences(
            val mobilizingNotificationsFrequencyPercentage: Int,
            val dailyWrapUpNotificationsTimeInMinutesPastMidnight: Int,
            val uiThemeMode: String
        )
    }
}