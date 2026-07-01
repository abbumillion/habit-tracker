package com.app.habit.data


import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.Date

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String = "",
    val frequency: String = "Daily", // Daily, Weekly, Monthly
    val category: String = "General", // Health, Work, Personal, etc.
    val createdAt: Date = Date(),
    var streak: Int = 0,
    var lastCompletedDate: Date? = null,
    var isActive: Boolean = true
) : Serializable