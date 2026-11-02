package com.example.localworkconnect

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "job_applications")
data class JobApplication(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val jobId: Int,
    val jobTitle: String,
    val workerName: String,
    val workerMobile: String,
    val workerSkill: String,
    val status: String = "APPLIED",
    val appliedAt: Long = System.currentTimeMillis()
)
