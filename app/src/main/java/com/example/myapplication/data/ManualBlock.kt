package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "manual_blocks")
data class ManualBlock(
    @PrimaryKey
    val phoneNumber: String,
    val timestamp: Long = System.currentTimeMillis()
)
