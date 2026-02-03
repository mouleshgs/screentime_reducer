package com.example.screentimereducer

import androidx.room.*

@Dao
interface AppLimitDao {
    @Query("SELECT * FROM app_limits")
    fun getAllLimits(): List<AppLimit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLimit(limit: AppLimit)

    @Query("SELECT * FROM app_limits WHERE packageName = :pName LIMIT 1")
    suspend fun getLimitForApp(pName: String): AppLimit?
}