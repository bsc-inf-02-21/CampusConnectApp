package com.example.campusconnectapp

import com.example.campusconnectapp.data.UserProfile
import com.example.campusconnectapp.data.UserRole
import com.example.campusconnectapp.viewmodel.ValidationUtils
import org.junit.Assert.*
import org.junit.Test

class UserRegistrationLoginFlowTest {

    @Test
    fun testUserRegistrationAndLoginFlow() {
        val testEmail = "bsc-inf-04-21@unima.ac.mw"
        val testRegNumber = "bsc-inf-04-21"
        val testPassword = "SecurePassword1!"

        // 1. Verify Registration Number format
        assertTrue(ValidationUtils.isValidRegistrationNumber(testRegNumber))

        // 2. Verify Email matches registration number and @unima.ac.mw domain
        val emailError = ValidationUtils.validateSignUpEmail(testEmail, testRegNumber)
        assertNull("Email validation should pass without errors", emailError)

        // 3. Verify Strong Password
        assertTrue(ValidationUtils.isStrongPassword(testPassword))

        // 4. Simulate User Profile creation upon successful registration & login
        val profile = UserProfile(
            uid = "user_test_123",
            fullName = "Test Student",
            email = testEmail,
            role = UserRole.STUDENT,
            identifier = testRegNumber,
            department = "Computer Science & IT"
        )

        assertEquals(testEmail, profile.email)
        assertEquals(UserRole.STUDENT, profile.role)
        assertEquals("bsc-inf-04-21", profile.identifier)
    }
}
