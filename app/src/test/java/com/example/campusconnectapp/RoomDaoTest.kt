package com.example.campusconnectapp

import com.example.campusconnectapp.data.ItemType
import com.example.campusconnectapp.data.LostItemEntity
import com.example.campusconnectapp.data.UserEntity
import com.example.campusconnectapp.data.UserRole
import org.junit.Assert.*
import org.junit.Test

class RoomDaoTest {

    @Test
    fun testUserEntityMapping() {
        val entity = UserEntity(
            uid = "user_1",
            fullName = "John Doe",
            email = "john@unima.ac.mw",
            password = "SecurePassword1!",
            role = UserRole.STUDENT,
            identifier = "CS-2023-1000",
            department = "Computer Science",
            avatarUrl = null
        )

        val profile = entity.toUserProfile()
        assertEquals("user_1", profile.uid)
        assertEquals("John Doe", profile.fullName)
        assertEquals(UserRole.STUDENT, profile.role)
    }

    @Test
    fun testLostItemEntityMapping() {
        val entity = LostItemEntity(
            id = "item_1",
            title = "Student ID",
            category = "IDs",
            description = "Lost ID card",
            location = "Library",
            date = 1234567890L,
            type = ItemType.LOST,
            contact = "student@unima.ac.mw",
            imageUrl = null,
            isHidden = false,
            postedByUid = "user_1"
        )

        val item = entity.toLostItem()
        assertEquals("item_1", item.id)
        assertEquals("Student ID", item.title)
        assertEquals(ItemType.LOST, item.type)
        assertFalse(item.isHidden)
    }
}
