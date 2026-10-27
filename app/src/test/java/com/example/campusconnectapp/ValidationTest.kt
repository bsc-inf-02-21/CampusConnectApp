package com.example.campusconnectapp

import com.example.campusconnectapp.viewmodel.ValidationUtils
import org.junit.Assert.*
import org.junit.Test

class ValidationTest {

    @Test
    fun testPasswordStrength() {
        // Strong password meeting all rules (8+ chars, upper, lower, digit, special char)
        assertTrue(ValidationUtils.isStrongPassword("SecurePass1!"))

        // Weak passwords failing rules
        assertFalse(ValidationUtils.isStrongPassword("short1!")) // too short
        assertFalse(ValidationUtils.isStrongPassword("nouppercase1!")) // no upper
        assertFalse(ValidationUtils.isStrongPassword("NOLOWERCASE1!")) // no lower
        assertFalse(ValidationUtils.isStrongPassword("NoDigitSpecial")) // no digit/special
    }

    @Test
    fun testEmailDomainValidation() {
        // Valid UNIMA domain email matching username
        assertNull(ValidationUtils.validateSignUpEmail("cs-2023-4091@unima.ac.mw", "cs-2023-4091"))

        // Invalid domain (not @unima.ac.mw)
        assertNotNull(ValidationUtils.validateSignUpEmail("student@gmail.com", "student"))

        // Invalid format
        assertNotNull(ValidationUtils.validateSignUpEmail("notanemail", "student"))
    }
}
