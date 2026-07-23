package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scam_messages")
data class ScamMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String,
    val body: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)
