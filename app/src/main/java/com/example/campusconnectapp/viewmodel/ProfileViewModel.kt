package com.example.campusconnectapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnectapp.data.AppDatabase
import com.example.campusconnectapp.data.UserEntity
import com.example.campusconnectapp.data.UserProfile
import com.example.campusconnectapp.data.UserRole
import com.example.campusconnectapp.utils.AppPreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

object ValidationUtils {
    /**
     * Registration Number / Username must follow valid organisation format (alphanumeric, slashes, hyphens)
     * e.g., CS-2023-4091, BSC-INF-01-20, EMP-LECT-882
     */
    fun isValidRegistrationNumber(regNum: String): Boolean {
        val trimmed = regNum.trim()
        if (trimmed.length < 3) return false
        val regNumRegex = Regex("^[A-Za-z0-9/\\-_]+$")
        return regNumRegex.matches(trimmed)
    }

    /**
     * Strong password validation:
     * - Minimum 8 characters
     * - Contains uppercase letter
     * - Contains lowercase letter
     * - Contains digit
     * - Contains special character
     */
    fun isStrongPassword(password: String): Boolean {
        if (password.length < 8) return false
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        return hasUpper && hasLower && hasDigit && hasSpecial
    }

    /**
     * Validates signup email:
     * 1. Must be valid email format
     * 2. Must end with @unima.ac.mw
     * 3. Must start with username / registration number prefix
     */
    fun validateSignUpEmail(email: String, username: String): String? {
        val trimmedEmail = email.trim().lowercase()
        val trimmedUsername = username.trim().lowercase()

        val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (!emailPattern.matches(trimmedEmail)) {
            return "Please enter a valid email address."
        }

        if (!trimmedEmail.endsWith("@unima.ac.mw")) {
            return "Email must belong to @unima.ac.mw domain (e.g., username@unima.ac.mw)."
        }

        val emailPrefix = trimmedEmail.substringBefore("@unima.ac.mw")
        val sanitizedUsername = trimmedUsername.replace(" ", "")

        if (emailPrefix != sanitizedUsername && !emailPrefix.startsWith(sanitizedUsername)) {
            return "Email must start with your username/registration number (e.g., ${sanitizedUsername}@unima.ac.mw)."
        }

        return null
    }

    /**
     * Validates a Malawi phone number:
     * - Must contain digits only
     * - Must start with 098, 099, or 088
     * - Must be exactly 10 digits long
     */
    fun isValidPhoneNumber(phone: String): Boolean {
        val trimmed = phone.trim()
        if (trimmed.length != 10) return false
        if (!trimmed.all { it.isDigit() }) return false
        val prefix = trimmed.take(3)
        return prefix == "098" || prefix == "099" || prefix == "088"
    }

    /**
     * Validates contact details (Email or Phone Number):
     * If phone: must start with 098, 099, or 088 and be exactly 10 digits.
     * If email: must be a valid email format.
     */
    fun validateContact(contact: String): String? {
        val trimmed = contact.trim()
        if (trimmed.isBlank()) {
            return "Contact detail is required."
        }

        // If contact starts with a digit or contains no '@', validate as phone number
        if (trimmed.first().isDigit() || !trimmed.contains("@")) {
            if (!trimmed.all { it.isDigit() }) {
                return "Phone number must contain digits only."
            }
            if (trimmed.length != 10) {
                return "Phone number must be exactly 10 digits (e.g., 0981234567, 0991234567, or 0881234567)."
            }
            val prefix = trimmed.substring(0, 3)
            if (prefix != "098" && prefix != "099" && prefix != "088") {
                return "Phone number must start with 098, 099, or 088 (e.g., 0981234567)."
            }
            return null // Valid phone number
        }

        // Validate as Email address
        val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (!emailPattern.matches(trimmed)) {
            return "Please enter a valid email address or phone number (e.g., 098XXXXXXX, 099XXXXXXX, 088XXXXXXX)."
        }

        return null // Valid email
    }
}

private fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getDatabase(application).userDao()
    private val prefsManager = AppPreferencesManager.getInstance(application)

    val notificationsEnabled: StateFlow<Boolean> = prefsManager.notificationsEnabled
    val realtimeSyncEnabled: StateFlow<Boolean> = prefsManager.realtimeSyncEnabled

    private val auth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        Log.e("ProfileViewModel", "Firebase Auth init error", e)
        null
    }
    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        Log.e("ProfileViewModel", "Firestore init error", e)
        null
    }

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun setNotificationsEnabled(enabled: Boolean) {
        prefsManager.setNotificationsEnabled(enabled)
    }

    fun setRealtimeSyncEnabled(enabled: Boolean) {
        prefsManager.setRealtimeSyncEnabled(enabled)
    }

    init {
        // Auto-restore cached user from local Room DB on app launch, and fetch latest from Firestore
        viewModelScope.launch {
            try {
                val cachedUser = userDao.getFirstUser()
                if (cachedUser != null) {
                    _userProfile.value = cachedUser.toUserProfile()
                    reauthenticateFirebase(cachedUser.email, cachedUser.password)

                    // Fetch latest profile updates from Firestore in background
                    if (firestore != null) {
                        firestore.collection("users").document(cachedUser.uid).get()
                            .addOnSuccessListener { doc ->
                                if (doc != null && doc.exists()) {
                                    val updatedProfile = UserProfile(
                                        uid = cachedUser.uid,
                                        fullName = doc.getString("fullName") ?: cachedUser.fullName,
                                        email = doc.getString("email") ?: cachedUser.email,
                                        role = try { UserRole.valueOf(doc.getString("role") ?: cachedUser.role.name) } catch (e: Exception) { cachedUser.role },
                                        identifier = doc.getString("identifier") ?: cachedUser.identifier,
                                        department = doc.getString("department") ?: cachedUser.department,
                                        avatarUrl = if (!doc.getString("avatarUrl").isNullOrBlank()) doc.getString("avatarUrl") else cachedUser.avatarUrl
                                    )
                                    _userProfile.value = updatedProfile
                                    viewModelScope.launch {
                                        val entity = UserEntity.fromUserProfile(updatedProfile, cachedUser.password)
                                        userDao.insertUser(entity)
                                    }
                                }
                            }
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error restoring cached user profile", e)
            }
        }
    }

    private fun reauthenticateFirebase(email: String, pass: String) {
        if (auth == null) return
        if (auth.currentUser == null || auth.currentUser?.isAnonymous == true || auth.currentUser?.email != email) {
            try {
                auth.signInWithEmailAndPassword(email, pass)
                    .addOnFailureListener {
                        if (auth.currentUser == null) {
                            try { auth.signInAnonymously() } catch (e: Exception) {}
                        }
                    }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Reauth error", e)
            }
        }
    }

    private fun saveProfileToFirestore(profile: UserProfile) {
        if (firestore == null) return
        try {
            val userData = hashMapOf(
                "uid" to profile.uid,
                "fullName" to profile.fullName,
                "email" to profile.email,
                "role" to profile.role.name,
                "identifier" to profile.identifier,
                "department" to profile.department,
                "avatarUrl" to (profile.avatarUrl ?: "")
            )
            firestore.collection("users").document(profile.uid).set(userData)
                .addOnSuccessListener {
                    Log.d("ProfileViewModel", "User profile synced to Cloud Firestore successfully: ${profile.uid}")
                }
                .addOnFailureListener { e ->
                    Log.w("ProfileViewModel", "Failed to sync user profile to Firestore: ${e.localizedMessage}")
                }
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Firestore save error", e)
        }
    }

    private fun fetchFirestoreProfileAndSync(uid: String, password: String, fallbackProfile: UserProfile) {
        if (firestore == null) {
            saveProfileToFirestore(fallbackProfile)
            return
        }
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val cloudProfile = UserProfile(
                        uid = uid,
                        fullName = doc.getString("fullName") ?: fallbackProfile.fullName,
                        email = doc.getString("email") ?: fallbackProfile.email,
                        role = try { UserRole.valueOf(doc.getString("role") ?: fallbackProfile.role.name) } catch (e: Exception) { fallbackProfile.role },
                        identifier = doc.getString("identifier") ?: fallbackProfile.identifier,
                        department = doc.getString("department") ?: fallbackProfile.department,
                        avatarUrl = if (!doc.getString("avatarUrl").isNullOrBlank()) doc.getString("avatarUrl") else fallbackProfile.avatarUrl
                    )
                    _userProfile.value = cloudProfile
                    viewModelScope.launch {
                        val entity = UserEntity.fromUserProfile(cloudProfile, password)
                        userDao.insertUser(entity)
                    }
                } else {
                    saveProfileToFirestore(fallbackProfile)
                }
            }
            .addOnFailureListener {
                saveProfileToFirestore(fallbackProfile)
            }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Clears all users from the local Room database to ensure clean user state.
     */
    fun clearAllDatabaseUsers() {
        viewModelScope.launch {
            try {
                userDao.clearAllUsers()
                Log.d("ProfileViewModel", "Cleared all users from local Room database")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error clearing Room database users", e)
            }
        }
    }

    /**
     * Login Flow:
     * 1. Check local Room DB first for instant, offline-reliable login.
     * 2. If local user is found, log in instantly and fetch latest from Firestore in background.
     * 3. If not found locally, attempt Firebase authentication. If unregistered, reject login.
     */
    fun login(email: String, password: String) {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isBlank() || !trimmedEmail.endsWith("@unima.ac.mw")) {
            _errorMessage.value = "Login requires a valid @unima.ac.mw email address"
            return
        }
        if (password.isBlank()) {
            _errorMessage.value = "Password cannot be empty"
            return
        }

        viewModelScope.launch {
            try {
                // 1. Check local Room DB first for instant responsiveness
                val localUser = userDao.login(trimmedEmail, password)
                if (localUser != null) {
                    val profile = localUser.toUserProfile()
                    _userProfile.value = profile
                    _errorMessage.value = null

                    // Background Firebase fetch & sync (non-blocking)
                    launch {
                        if (auth != null) {
                            if (auth.currentUser == null || auth.currentUser?.isAnonymous == true || auth.currentUser?.email != trimmedEmail) {
                                auth.signInWithEmailAndPassword(trimmedEmail, password)
                                    .addOnSuccessListener {
                                        fetchFirestoreProfileAndSync(localUser.uid, password, profile)
                                    }
                                    .addOnFailureListener {
                                        fetchFirestoreProfileAndSync(localUser.uid, password, profile)
                                    }
                            } else {
                                fetchFirestoreProfileAndSync(localUser.uid, password, profile)
                            }
                        }
                    }
                    return@launch
                }

                // 2. Attempt online authentication with Firebase if not in local DB
                if (auth != null) {
                    auth.signInWithEmailAndPassword(trimmedEmail, password)
                        .addOnSuccessListener { authResult ->
                            val uid = authResult.user?.uid ?: ""
                            if (firestore != null) {
                                firestore.collection("users").document(uid).get()
                                    .addOnSuccessListener { doc ->
                                        if (doc != null && doc.exists()) {
                                            val fetchedProfile = UserProfile(
                                                uid = uid,
                                                fullName = doc.getString("fullName") ?: "Campus User",
                                                email = doc.getString("email") ?: trimmedEmail,
                                                role = try { UserRole.valueOf(doc.getString("role") ?: UserRole.STUDENT.name) } catch (e: Exception) { UserRole.STUDENT },
                                                identifier = doc.getString("identifier") ?: trimmedEmail.substringBefore("@"),
                                                department = doc.getString("department") ?: "General",
                                                avatarUrl = doc.getString("avatarUrl")
                                            )

                                            saveProfileToFirestore(fetchedProfile)

                                            viewModelScope.launch {
                                                val entity = UserEntity.fromUserProfile(fetchedProfile, password)
                                                userDao.insertUser(entity)
                                                _userProfile.value = fetchedProfile
                                                _errorMessage.value = null
                                            }
                                        } else {
                                            _errorMessage.value = "Account does not exist. Please register first."
                                        }
                                    }
                                    .addOnFailureListener {
                                        _errorMessage.value = "Account does not exist. Please register first."
                                    }
                            } else {
                                _errorMessage.value = "Account does not exist. Please register first."
                            }
                        }
                        .addOnFailureListener {
                            _errorMessage.value = "Invalid email or password. Please check your credentials or register first."
                        }
                } else {
                    _errorMessage.value = "Account does not exist in local database. Please register first."
                }

            } catch (e: Exception) {
                _errorMessage.value = "Invalid email or password. Please check your credentials or register first."
            }
        }
    }

    /**
     * Registration Flow:
     * 1. Validate fields & Staff Access Code for staff roles.
     * 2. Instant local registration in Room DB (offline-reliable, never blocks).
     * 3. Background Firebase sync (non-blocking).
     */
    fun register(profile: UserProfile, password: String, staffCode: String = "") {
        if (profile.role != UserRole.STUDENT) {
            if (staffCode.trim() != "UNIMA-STAFF-2025") {
                _errorMessage.value = "Invalid Staff Access Code for Lecturer/Admin registration (Required: UNIMA-STAFF-2025)"
                return
            }
        }

        if (profile.fullName.isBlank()) {
            _errorMessage.value = "Full Name cannot be empty"
            return
        }

        if (profile.department.isBlank()) {
            _errorMessage.value = "Department / Faculty cannot be empty"
            return
        }

        val username = profile.identifier.trim()
        if (!ValidationUtils.isValidRegistrationNumber(username)) {
            _errorMessage.value = "Invalid Registration Number. Must be a valid organisation ID (e.g., CS-2023-4091 or EMP-LECT-882)"
            return
        }

        val emailError = ValidationUtils.validateSignUpEmail(profile.email, username)
        if (emailError != null) {
            _errorMessage.value = emailError
            return
        }

        if (!ValidationUtils.isStrongPassword(password)) {
            _errorMessage.value = "Password is too weak. Must be at least 8 characters long and contain uppercase, lowercase, numbers, and special characters."
            return
        }

        val formattedEmail = profile.email.trim().lowercase()

        val finalProfile = profile.copy(
            fullName = profile.fullName.trim(),
            department = profile.department.trim(),
            email = formattedEmail,
            identifier = username
        )

        // Instant local registration first (guarantees instant responsiveness without waiting for network timeouts)
        viewModelScope.launch {
            try {
                val entity = UserEntity.fromUserProfile(finalProfile, password)
                userDao.insertUser(entity)
                _userProfile.value = finalProfile
                _errorMessage.value = null

                // Background Firebase sync (non-blocking)
                if (auth != null) {
                    launch {
                        auth.createUserWithEmailAndPassword(formattedEmail, password)
                            .addOnSuccessListener { authResult ->
                                val firebaseUid = authResult.user?.uid ?: finalProfile.uid
                                val registeredProfile = finalProfile.copy(uid = firebaseUid)
                                saveProfileToFirestore(registeredProfile)
                            }
                            .addOnFailureListener {
                                if (auth.currentUser == null) {
                                    auth.signInAnonymously().addOnCompleteListener {
                                        saveProfileToFirestore(finalProfile)
                                    }
                                } else {
                                    saveProfileToFirestore(finalProfile)
                                }
                            }
                    }
                } else {
                    saveProfileToFirestore(finalProfile)
                }
            } catch (dbEx: Exception) {
                _errorMessage.value = "Registration failed: ${dbEx.localizedMessage}"
            }
        }
    }

    fun logout() {
        auth?.signOut()
        _userProfile.value = null
        _errorMessage.value = null
        clearAllDatabaseUsers()
    }

    fun requestPasswordReset(email: String, onResult: (Boolean, String?) -> Unit) {
        val trimmed = email.trim().lowercase()
        if (trimmed.isBlank() || !trimmed.endsWith("@unima.ac.mw")) {
            onResult(false, "Please enter a valid @unima.ac.mw email address.")
            return
        }

        viewModelScope.launch {
            try {
                // 1. Check if email exists in local Room DB first
                val existingUser = userDao.getUserByEmail(trimmed)
                if (existingUser != null) {
                    generateAndSendOtp(trimmed, onResult)
                } else if (firestore != null) {
                    // 2. Check Firestore if not in local Room DB
                    firestore.collection("users").whereEqualTo("email", trimmed).get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                generateAndSendOtp(trimmed, onResult)
                            } else {
                                onResult(false, "Email address not found in database. Please register first.")
                            }
                        }
                        .addOnFailureListener {
                            onResult(false, "Email address not found in database. Please register first.")
                        }
                } else {
                    onResult(false, "Email address not found in database. Please register first.")
                }
            } catch (e: Exception) {
                onResult(false, "Error checking email: ${e.localizedMessage}")
            }
        }
    }

    private fun generateAndSendOtp(email: String, onResult: (Boolean, String?) -> Unit) {
        val otpCode = (100000..999999).random().toString()
        val expiresAt = System.currentTimeMillis() + (10 * 60 * 1000) // 10 minutes

        val resetData = hashMapOf(
            "email" to email,
            "code" to otpCode,
            "expiresAt" to expiresAt
        )

        if (firestore != null) {
            firestore.collection("password_resets").document(email).set(resetData)
                .addOnSuccessListener {
                    onResult(true, "Verification code generated! (Code: $otpCode)")
                }
                .addOnFailureListener { e ->
                    onResult(false, "Failed to generate code: ${e.localizedMessage}")
                }
        } else {
            onResult(true, "Verification code generated! (Code: $otpCode)")
        }
    }

    fun verifyOtpAndResetPassword(email: String, code: String, newPass: String, onResult: (Boolean, String?) -> Unit) {
        val trimmedEmail = email.trim().lowercase()
        val trimmedCode = code.trim()

        if (!ValidationUtils.isStrongPassword(newPass)) {
            onResult(false, "Password is too weak. Must be 8+ chars with uppercase, lowercase, number & symbol.")
            return
        }

        viewModelScope.launch {
            try {
                if (firestore != null) {
                    firestore.collection("password_resets").document(trimmedEmail).get()
                        .addOnSuccessListener { doc ->
                            if (doc != null && doc.exists()) {
                                val storedCode = doc.getString("code") ?: ""
                                val expiresAt = doc.getLong("expiresAt") ?: 0L

                                if (storedCode == trimmedCode && System.currentTimeMillis() <= expiresAt) {
                                    viewModelScope.launch {
                                        // 1. Update Room DB
                                        val userEntity = userDao.getUserByEmail(trimmedEmail)
                                        if (userEntity != null) {
                                            val updatedEntity = userEntity.copy(password = newPass)
                                            userDao.updateUser(updatedEntity)
                                        }

                                        // 2. Update Firebase Auth if current user matches or active
                                        if (auth?.currentUser != null) {
                                            try {
                                                auth.currentUser?.updatePassword(newPass)
                                            } catch (e: Exception) {
                                                // Reauth if needed
                                            }
                                        }

                                        // Delete reset token
                                        firestore.collection("password_resets").document(trimmedEmail).delete()

                                        onResult(true, "Password reset successfully in both Room DB and Firebase!")
                                    }
                                } else {
                                    onResult(false, "Invalid or expired verification code.")
                                }
                            } else {
                                onResult(false, "No active reset request found for this email.")
                            }
                        }
                        .addOnFailureListener { e ->
                            onResult(false, "Verification failed: ${e.localizedMessage}")
                        }
                } else {
                    onResult(false, "Database connection not available.")
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.localizedMessage}")
            }
        }
    }

    fun updateProfile(newName: String, newDepartment: String, newEmail: String, newAvatarUrl: String? = null) {
        val formattedEmail = newEmail.trim().lowercase()
        if (!formattedEmail.endsWith("@unima.ac.mw")) {
            _errorMessage.value = "Email must belong to @unima.ac.mw domain"
            return
        }

        viewModelScope.launch {
            val current = _userProfile.value
            if (current != null) {
                val updated = current.copy(
                    fullName = newName.trim(),
                    department = newDepartment.trim(),
                    email = formattedEmail,
                    avatarUrl = newAvatarUrl ?: current.avatarUrl
                )

                saveProfileToFirestore(updated)

                // Update local Room database
                val existingEntity = userDao.getUserByEmail(current.email)
                if (existingEntity != null) {
                    val updatedEntity = existingEntity.copy(
                        fullName = newName.trim(),
                        department = newDepartment.trim(),
                        email = formattedEmail,
                        avatarUrl = newAvatarUrl ?: current.avatarUrl
                    )
                    userDao.updateUser(updatedEntity)
                }
                _userProfile.value = updated
            }
        }
    }

    fun sendFeedback(message: String, onResult: (Boolean, String?) -> Unit) {
        if (message.isBlank()) {
            _errorMessage.value = "Feedback message cannot be empty"
            return
        }
        val current = _userProfile.value ?: UserProfile(
            uid = "anonymous",
            fullName = "Anonymous User",
            email = "anonymous@unima.ac.mw",
            role = UserRole.STUDENT,
            identifier = "N/A",
            department = "N/A"
        )

        val feedbackData = hashMapOf(
            "userId" to current.uid,
            "userName" to current.fullName,
            "userEmail" to current.email,
            "userRole" to current.role.name,
            "message" to message,
            "date" to System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance().collection("feedback")
            .add(feedbackData)
            .addOnSuccessListener {
                Log.d("ProfileViewModel", "Feedback submitted successfully")
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("ProfileViewModel", "Failed to submit feedback", e)
                onResult(false, e.localizedMessage)
            }
    }
}
