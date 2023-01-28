package com.sweak.unlockmaster.data.local.database.dao

import androidx.room.*
import com.sweak.unlockmaster.data.local.database.entities.UnlockLimit

@Dao
interface UnlockLimitsDao {

    @Insert
    suspend fun insert(unlockLimit: UnlockLimit)

    @Update
    suspend fun update(unlockLimit: UnlockLimit)

    @Delete
    suspend fun delete(unlockLimit: UnlockLimit)

    @Query(
        "SELECT * FROM unlock_limit WHERE limitApplianceDayTimeInMillis = " +
                "(SELECT MAX(limitApplianceDayTimeInMillis) FROM unlock_limit " +
                "WHERE limitApplianceDayTimeInMillis < :currentTimeInMillis)"
    )
    suspend fun getCurrentUnlockLimit(currentTimeInMillis: Long): UnlockLimit?

    @Query(
        "SELECT * FROM unlock_limit " +
                "WHERE limitApplianceDayTimeInMillis = :limitApplianceDayTimeInMillis"
    )
    suspend fun getUnlockLimitWithApplianceDay(limitApplianceDayTimeInMillis: Long): UnlockLimit?
}