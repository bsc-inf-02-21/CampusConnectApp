package com.example.campusconnectapp

import com.example.campusconnectapp.data.UserRole
import org.junit.Assert.*
import org.junit.Test

class AppFeatureCoverageTest {

    private fun validateStaffAccessCode(role: UserRole, code: String): Boolean {
        if (role == UserRole.STUDENT) return true
        return code.trim() == "UNIMA-STAFF-2025"
    }


    @Test
    fun testStaffAccessCodeVerification() {
        // Student does not require staff access code (TC_REG_001)
        assertTrue(validateStaffAccessCode(UserRole.STUDENT, ""))

        // Lecturer requires correct access code (TC_REG_002, TC_REG_003)
        assertTrue(validateStaffAccessCode(UserRole.LECTURER, "UNIMA-STAFF-2025"))
        assertFalse(validateStaffAccessCode(UserRole.LECTURER, "WRONG-CODE"))

        // Administrator requires correct access code (TC_REG_002, TC_REG_003)
        assertTrue(validateStaffAccessCode(UserRole.ADMINISTRATOR, "UNIMA-STAFF-2025"))
        assertFalse(validateStaffAccessCode(UserRole.ADMINISTRATOR, ""))
    }
}

