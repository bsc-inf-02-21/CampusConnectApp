package com.example.campusconnectapp

import org.junit.Assert.*
import org.junit.Test

class ProfileUpdateTest {

    private fun validateProfileUpdate(name: String, dept: String, email: String): Boolean {
        return name.isNotBlank() && dept.isNotBlank() && email.trim().lowercase().endsWith("@unima.ac.mw")
    }

    @Test
    fun testProfileUpdateValidation() {
        // Valid profile update
        assertTrue(validateProfileUpdate("Alex Johnson", "Computer Science", "alex@unima.ac.mw"))

        // Invalid update (blank name)
        assertFalse(validateProfileUpdate("", "Computer Science", "alex@unima.ac.mw"))

        // Invalid update (invalid email domain)
        assertFalse(validateProfileUpdate("Alex Johnson", "Computer Science", "alex@gmail.com"))
    }
}
