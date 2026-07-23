package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scam_calls")
data class ScamCall(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val riskLevel: String, // "low", "medium", "high"
    val carrier: String?,
    val location: String?,
    val wasBlocked: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
