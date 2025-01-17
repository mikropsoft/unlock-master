package com.sweak.unlockmaster.data.repository

import com.sweak.unlockmaster.data.local.database.dao.LockEventsDao
import com.sweak.unlockmaster.data.local.database.entities.LockEventEntity
import com.sweak.unlockmaster.domain.model.UnlockMasterEvent.LockEvent
import com.sweak.unlockmaster.domain.repository.LockEventsRepository

class LockEventsRepositoryImpl(
    private val lockEventsDao: LockEventsDao
) : LockEventsRepository {

    override suspend fun addLockEvent(lockEvent: LockEvent) {
        lockEventsDao.insert(
            LockEventEntity(timeInMillis = lockEvent.timeInMillis)
        )
    }

    override suspend fun getLockEventsSinceTime(sinceTimeInMillis: Long): List<LockEvent> =
        lockEventsDao.getAllLockEvents()
            .filter {
                it.timeInMillis >= sinceTimeInMillis
            }
            .map {
                LockEvent(lockTimeInMillis = it.timeInMillis)
            }
}