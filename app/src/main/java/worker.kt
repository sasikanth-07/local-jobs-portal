package com.example.localworkconnect

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workers")
data class Worker(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    val mobile: String,

    val skill: String,

    val experience: String,

    val location: String,

    val wage: String = ""
)