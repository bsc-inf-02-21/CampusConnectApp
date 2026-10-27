package com.example.campusconnectapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val uid: String,
    val fullName: String,
    val email: String,
    val password: String,
    val role: UserRole,
    val identifier: String,
    val department: String,
    val avatarUrl: String? = null
) {
    fun toUserProfile(): UserProfile {
        return UserProfile(
            uid = uid,
            fullName = fullName,
            email = email,
            role = role,
            identifier = identifier,
            department = department,
            avatarUrl = avatarUrl
        )
    }

    companion object {
        fun fromUserProfile(profile: UserProfile, password: String): UserEntity {
            return UserEntity(
                uid = profile.uid,
                fullName = profile.fullName,
                email = profile.email,
                password = password,
                role = profile.role,
                identifier = profile.identifier,
                department = profile.department,
                avatarUrl = profile.avatarUrl
            )
        }
    }
}
