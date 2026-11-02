package com.example.localworkconnect

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WorkerDao {

    @Insert
    suspend fun insertWorker(worker: Worker)

    @Query("SELECT * FROM workers")
    suspend fun getAllWorkers(): List<Worker>

    @Query("SELECT * FROM workers WHERE skill LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%'")
    suspend fun searchWorkers(query: String): List<Worker>
}