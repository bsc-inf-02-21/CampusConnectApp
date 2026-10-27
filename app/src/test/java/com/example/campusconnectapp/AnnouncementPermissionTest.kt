package com.example.campusconnectapp

import com.example.campusconnectapp.data.Announcement
import com.example.campusconnectapp.data.UserRole
import org.junit.Assert.*
import org.junit.Test

class AnnouncementPermissionTest {

    private fun canPost(role: UserRole?): Boolean {
        if (role == null) return false
        return role == UserRole.LECTURER || role == UserRole.ADMINISTRATOR
    }

    private fun canDelete(announcement: Announcement, role: UserRole?): Boolean {
        if (role == null) return false
        return when (role) {
            UserRole.STUDENT -> false
            UserRole.LECTURER -> announcement.authorRole != UserRole.ADMINISTRATOR
            UserRole.ADMINISTRATOR -> true
        }
    }

    @Test
    fun testCanPostPermissions() {
        // Students cannot post announcements
        assertFalse(canPost(UserRole.STUDENT))

        // Lecturers can post announcements
        assertTrue(canPost(UserRole.LECTURER))

        // Administrators can post announcements
        assertTrue(canPost(UserRole.ADMINISTRATOR))

        // Null role cannot post
        assertFalse(canPost(null))
    }

    @Test
    fun testCanDeletePermissions() {
        val lecturerAnnouncement = Announcement(
            id = "1",
            title = "Title",
            content = "Content",
            authorName = "Dr. Smith",
            authorRole = UserRole.LECTURER,
            authorId = "lec_1"
        )

        val adminAnnouncement = Announcement(
            id = "2",
            title = "Title",
            content = "Content",
            authorName = "Admin Staff",
            authorRole = UserRole.ADMINISTRATOR,
            authorId = "adm_1"
        )

        // 1. Student cannot delete any announcement
        assertFalse(canDelete(lecturerAnnouncement, UserRole.STUDENT))
        assertFalse(canDelete(adminAnnouncement, UserRole.STUDENT))

        // 2. Lecturer can delete lecturer announcements, but NOT admin announcements
        assertTrue(canDelete(lecturerAnnouncement, UserRole.LECTURER))
        assertFalse(canDelete(adminAnnouncement, UserRole.LECTURER))

        // 3. Administrator can delete ANY announcement
        assertTrue(canDelete(lecturerAnnouncement, UserRole.ADMINISTRATOR))
        assertTrue(canDelete(adminAnnouncement, UserRole.ADMINISTRATOR))
    }
}
