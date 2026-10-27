package com.example.campusconnectapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.campusconnectapp.data.UserProfile
import com.example.campusconnectapp.data.UserRole
import com.example.campusconnectapp.ui.theme.CampusConnectAppTheme
import com.example.campusconnectapp.viewmodel.ProfileViewModel
import com.example.campusconnectapp.viewmodel.ValidationUtils
import java.util.UUID

@Composable
fun LoginScreen(viewModel: ProfileViewModel) {
    val errorMessage by viewModel.errorMessage.collectAsState()
    LoginContent(
        errorMessage = errorMessage,
        onLogin = { email, pass -> viewModel.login(email, pass) },
        onRegister = { profile, pass, code -> viewModel.register(profile, pass, code) },
        onRequestCode = { email, cb -> viewModel.requestPasswordReset(email, cb) },
        onVerifyAndReset = { email, code, pass, cb -> viewModel.verifyOtpAndResetPassword(email, code, pass, cb) },
        onClearError = { viewModel.clearError() }
    )
}

@Composable
fun LoginContent(
    errorMessage: String? = null,
    onLogin: (String, String) -> Unit = { _, _ -> },
    onRegister: (UserProfile, String, String) -> Unit = { _, _, _ -> },
    onRequestCode: (String, (Boolean, String?) -> Unit) -> Unit = { _, _ -> },
    onVerifyAndReset: (String, String, String, (Boolean, String?) -> Unit) -> Unit = { _, _, _, _ -> },
    onClearError: () -> Unit = {}
) {
    var isSignUp by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            onDismiss = { showForgotPasswordDialog = false },
            onRequestCode = onRequestCode,
            onVerifyAndReset = onVerifyAndReset
        )
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var identifier by remember { mutableStateOf("") }
    var staffCode by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var passwordVisible by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(90.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isSignUp) "Create Account" else "ConnectCampus Login",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isSignUp) "Register with your role and organisation details" else "Sign in to access campus services",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            TabRow(selectedTabIndex = if (isSignUp) 1 else 0, modifier = Modifier.fillMaxWidth()) {
                Tab(
                    selected = !isSignUp,
                    onClick = {
                        isSignUp = false
                        onClearError()
                    },
                    text = { Text("Sign In") }
                )
                Tab(
                    selected = isSignUp,
                    onClick = {
                        isSignUp = true
                        onClearError()
                    },
                    text = { Text("Register") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isSignUp) {
                Text(
                    text = "Select Role:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedRole == UserRole.STUDENT,
                        onClick = {
                            selectedRole = UserRole.STUDENT
                            identifier = "name"
                            if (email.isEmpty() || email.endsWith("@unima.ac.mw")) {
                                email = "name"
                            }
                        },
                        label = { Text("Student") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedRole == UserRole.LECTURER,
                        onClick = {
                            selectedRole = UserRole.LECTURER
                            identifier = "name"
                            if (email.isEmpty() || email.endsWith("@unima.ac.mw")) {
                                email = "name@unima.ac.mw"
                            }
                        },
                        label = { Text("Lecturer") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedRole == UserRole.ADMINISTRATOR,
                        onClick = {
                            selectedRole = UserRole.ADMINISTRATOR
                            identifier = "name"
                            if (email.isEmpty() || email.endsWith("@unima.ac.mw")) {
                                email = "name@unima.ac.mw"
                            }
                        },
                        label = { Text("Admin") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department / Faculty") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                val isIdentifierInvalid = isSignUp && identifier.isNotBlank() && !ValidationUtils.isValidRegistrationNumber(identifier)
                OutlinedTextField(
                    value = identifier,
                    onValueChange = { newId ->
                        identifier = newId
                        // Auto-suggest email if email is default or matches pattern
                        val cleanId = newId.trim().lowercase().replace(" ", "")
                        if (cleanId.isNotEmpty()) {
                            email = "$cleanId@unima.ac.mw"
                        }
                    },
                    label = { Text(if (selectedRole == UserRole.STUDENT) "Username / Student Reg Number" else "Username / Staff Reg Number") },
                    isError = isIdentifierInvalid,
                    supportingText = {
                        if (isIdentifierInvalid) {
                            Text("Invalid format", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (selectedRole != UserRole.STUDENT) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = staffCode,
                        onValueChange = { staffCode = it },
                        label = { Text("Staff Access Code") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            val isEmailInvalid = isSignUp && email.isNotBlank() && !email.trim().lowercase().endsWith("@unima.ac.mw")
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                isError = isEmailInvalid,
                supportingText = {
                    if (isEmailInvalid) {
                        Text("Invalid email format", color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            val isPasswordInvalid = isSignUp && password.isNotBlank() && !ValidationUtils.isStrongPassword(password)
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Enter your Password") },
                isError = isPasswordInvalid,
                supportingText = {
                    if (isPasswordInvalid) {
                        Text("Invalid password format", color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                }
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isSignUp) {
                        val profile = UserProfile(
                            uid = "user_" + UUID.randomUUID().toString().take(6),
                            fullName = fullName,
                            email = email,
                            role = selectedRole,
                            identifier = identifier,
                            department = department
                        )
                        onRegister(profile, password, staffCode)
                    } else {
                        onLogin(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = if (isSignUp) "Register & Sign In" else "Sign In",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (!isSignUp) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { showForgotPasswordDialog = true },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
                ) {
                    Text("Forgot Password? Reset via Email", color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onRequestCode: (String, (Boolean, String?) -> Unit) -> Unit,
    onVerifyAndReset: (String, String, String, (Boolean, String?) -> Unit) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) } // 1: Enter email, 2: Enter code & new password
    var submitting by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (step == 1) "Verify Account & Get Code" else "Reset Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (step == 1) {
                    Text("Enter your registered @unima.ac.mw email address. We will verify if your account exists in the database.")
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else {
                    Text("Account verified! Enter the verification code and your new password.")
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Verification Code") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                }

                if (statusMessage != null) {
                    val isSuccess = statusMessage!!.contains("successfully", ignoreCase = true) ||
                                    statusMessage!!.contains("generated", ignoreCase = true)
                    Surface(
                        color = if (isSuccess) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (isSuccess) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = statusMessage!!,
                                color = if (isSuccess) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !submitting && (if (step == 1) email.isNotBlank() else code.isNotBlank() && newPassword.isNotBlank()),
                onClick = {
                    submitting = true
                    if (step == 1) {
                        onRequestCode(email) { success, msg ->
                            submitting = false
                            statusMessage = msg
                            if (success) {
                                step = 2
                            }
                        }
                    } else {
                        onVerifyAndReset(email, code, newPassword) { success, msg ->
                            submitting = false
                            statusMessage = msg
                            if (success) {
                                Toast.makeText(context, msg ?: "Password reset successful!", Toast.LENGTH_LONG).show()
                                onDismiss()
                            }
                        }
                    }
                }
            ) {
                Text(if (submitting) "Processing..." else if (step == 1) "Verify & Get Code" else "Update Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    CampusConnectAppTheme {
        LoginContent()
    }
}
