package com.example.localworkconnect

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jobs")
data class Job(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val categorySkill: String,
    val wage: String,
    val location: String,
    val employerName: String,
    val employerMobile: String,
    val status: String = "OPEN",
    val timestamp: Long = System.currentTimeMillis()
)
