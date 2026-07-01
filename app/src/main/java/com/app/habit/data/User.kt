package com.app.habit.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    val name: String,
    val profileImage: String? = null,
    val passwordHash: String
) : Serializable