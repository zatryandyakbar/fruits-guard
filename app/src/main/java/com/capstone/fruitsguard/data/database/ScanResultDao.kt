package com.capstone.fruitsguard.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ScanResultDao {
    @Insert
    suspend fun insertScanResult(scanResult: ScanResultEntity)

    @Query("SELECT * FROM result_data ORDER BY timestamp DESC")
    suspend fun getAllScanResults(): List<ScanResultEntity>

    @Query("DELETE FROM result_data WHERE id = :id")
    suspend fun deleteScanResultById(id: Int)
}