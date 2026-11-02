package com.example.localworkconnect

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface JobDao {

    @Insert
    suspend fun insertJob(job: Job)

    @Query("SELECT * FROM jobs ORDER BY id DESC")
    suspend fun getAllJobs(): List<Job>

    @Query("SELECT * FROM jobs WHERE categorySkill LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' ORDER BY id DESC")
    suspend fun searchJobs(query: String): List<Job>

    @Insert
    suspend fun insertApplication(application: JobApplication)

    @Query("SELECT * FROM job_applications WHERE jobId = :jobId ORDER BY id DESC")
    suspend fun getApplicationsForJob(jobId: Int): List<JobApplication>

    @Query("SELECT * FROM job_applications WHERE workerMobile = :mobile ORDER BY id DESC")
    suspend fun getApplicationsForWorker(mobile: String): List<JobApplication>
}
