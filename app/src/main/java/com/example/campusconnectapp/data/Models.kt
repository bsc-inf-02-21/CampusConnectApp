package com.example.campusconnectapp.data

import androidx.compose.ui.graphics.vector.ImageVector

enum class UserRole {
    STUDENT, LECTURER, ADMINISTRATOR
}

data class UserProfile(
    val uid: String,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val identifier: String, // Student ID or Staff ID
    val department: String,
    val avatarUrl: String? = null
)

enum class ItemType {
    LOST, FOUND
}

data class Shortcut(
    val title: String,
    val url: String,
    val description: String,
    val icon: ImageVector
)

data class LostItem(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val location: String,
    val date: Long,
    val type: ItemType,
    val contact: String,
    val imageUrl: String? = null,
    val isHidden: Boolean = false,
    val postedByUid: String = ""
)

data class CampusInfo(
    val id: String,
    val title: String,
    val detail: String,
    val type: InfoType,
    val phone: String? = null,
    val email: String? = null,
    val url: String? = null
)

enum class InfoType {
    EMERGENCY, MAP, ANNOUNCEMENT
}

data class Announcement(
    val id: String,
    val title: String,
    val content: String,
    val date: Long = System.currentTimeMillis(),
    val authorName: String,
    val authorRole: UserRole,
    val authorId: String = ""
)
