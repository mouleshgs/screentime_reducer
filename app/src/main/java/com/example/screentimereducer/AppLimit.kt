package com.example.screentimereducer

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_limits")
data class AppLimit(
    @PrimaryKey val packageName: String, // e.g., "com.instagram.android"
    val timeLimitMinutes: Int,
    val isEnabled: Boolean = true
)