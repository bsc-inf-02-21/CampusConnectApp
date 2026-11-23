package com.example.campusconnectapp.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.campusconnectapp.data.UserProfile
import com.example.campusconnectapp.data.UserRole
import com.example.campusconnectapp.ui.theme.CampusConnectAppTheme
import com.example.campusconnectapp.utils.ImageUtils
import com.example.campusconnectapp.viewmodel.ProfileViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val realtimeSyncEnabled by viewModel.realtimeSyncEnabled.collectAsState()

    profile?.let { currentProfile ->
        ProfileScreen(
            profile = currentProfile,
            notificationsEnabled = notificationsEnabled,
            realtimeSyncEnabled = realtimeSyncEnabled,
            onToggleNotifications = { viewModel.setNotificationsEnabled(it) },
            onToggleRealtimeSync = { viewModel.setRealtimeSyncEnabled(it) },
            onUpdateProfile = { name, dept, email, avatar -> viewModel.updateProfile(name, dept, email, avatar) },
            onSendFeedback = { msg, cb -> viewModel.sendFeedback(msg, cb) },
            onLogout = { viewModel.logout() }
        )
    }
}

@Composable
fun ProfileScreen(
    profile: UserProfile,
    notificationsEnabled: Boolean = true,
    realtimeSyncEnabled: Boolean = true,
    onToggleNotifications: (Boolean) -> Unit = {},
    onToggleRealtimeSync: (Boolean) -> Unit = {},
    onUpdateProfile: (String, String, String, String?) -> Unit,
    onSendFeedback: (String, (Boolean, String?) -> Unit) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }
    var nameInput by remember(profile) { mutableStateOf(profile.fullName) }
    var deptInput by remember(profile) { mutableStateOf(profile.department) }
    var emailInput by remember(profile) { mutableStateOf(profile.email) }
    var selectedAvatarUri by remember(profile) { mutableStateOf<String?>(profile.avatarUrl) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showAdminFeedbackDialog by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedAvatarUri = uri.toString()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempPhotoUri != null) {
            selectedAvatarUri = tempPhotoUri.toString()
        }
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Update Profile Picture") },
            text = { Text("Choose source for your new profile picture:") },
            confirmButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    try {
                        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                        val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", context.cacheDir)
                        val photoUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        tempPhotoUri = photoUri
                        cameraLauncher.launch(photoUri)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Camera error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Take Photo")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    galleryLauncher.launch("image/*")
                }) {
                    Text("Choose from Gallery")
                }
            }
        )
    }

    if (showFeedbackDialog) {
        FeedbackDialog(
            onDismiss = { showFeedbackDialog = false },
            onSubmit = onSendFeedback
        )
    }

    if (showAdminFeedbackDialog) {
        AdminFeedbackDialog(
            onDismiss = { showAdminFeedbackDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .clickable(enabled = isEditing) {
                    showImageSourceDialog = true
                }
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (!selectedAvatarUri.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageUtils.getLoadableModel(selectedAvatarUri),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (isEditing) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Photo",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Text(
            text = profile.fullName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Role: ${profile.role.name} • ID: ${profile.identifier}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "User Profile Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = {
                        isEditing = !isEditing
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                    }
                }

                if (isEditing) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = deptInput,
                        onValueChange = { deptInput = it },
                        label = { Text("Department / Faculty") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                nameInput = profile.fullName
                                deptInput = profile.department
                                emailInput = profile.email
                                selectedAvatarUri = profile.avatarUrl
                                isEditing = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val compressedAvatar = if (!selectedAvatarUri.isNullOrBlank() && selectedAvatarUri != profile.avatarUrl) {
                                    try {
                                        val uri = Uri.parse(selectedAvatarUri)
                                        ImageUtils.uriToBase64(context, uri) ?: selectedAvatarUri
                                    } catch (e: Exception) {
                                        selectedAvatarUri
                                    }
                                } else {
                                    profile.avatarUrl
                                }
                                onUpdateProfile(nameInput, deptInput, emailInput, compressedAvatar)
                                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                                isEditing = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save Changes")
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${profile.email}")
                            }
                            try { context.startActivity(intent) } catch (e: Exception) {}
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Email: ${profile.email}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(text = "Department: ${profile.department}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Status: Active ${profile.role.name}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "App Background & Data Controls",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Announcement Notifications",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Receive system notifications when new announcements arrive",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = onToggleNotifications
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Real-Time Background Data Sync",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Automatically sync announcements & lost items live from Firestore",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Switch(
                        checked = realtimeSyncEnabled,
                        onCheckedChange = onToggleRealtimeSync
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (profile.role == UserRole.ADMINISTRATOR) {
            Button(
                onClick = { showAdminFeedbackDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.Feedback, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "View Feedback from Users")
            }
        } else {
            Button(
                onClick = { showFeedbackDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Feedback, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Send Feedback to Admin")
            }
        }

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text(text = "Sign Out")
        }
    }
}

@Composable
fun AdminFeedbackDialog(
    onDismiss: () -> Unit
) {
    var feedbackList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("feedback")
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                feedbackList = result.documents.map { doc ->
                    doc.data ?: emptyMap()
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("User Feedback Inbox") },
        text = {
            Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (feedbackList.isEmpty()) {
                    Text("No feedback received yet from users.", modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(feedbackList) { fb ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = fb["userName"]?.toString() ?: "Unknown User",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            shape = MaterialTheme.shapes.extraSmall,
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                text = fb["userRole"]?.toString() ?: "USER",
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Email: ${fb["userEmail"]?.toString() ?: "N/A"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = fb["message"]?.toString() ?: "",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun FeedbackDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, (Boolean, String?) -> Unit) -> Unit
) {
    var message by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Send Feedback to Admin") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Report a bug, request a feature, or send feedback directly to the system administrator.")
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Your Feedback") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                if (resultMessage != null) {
                    Text(resultMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !submitting && message.isNotBlank(),
                onClick = {
                    submitting = true
                    onSubmit(message) { success, err ->
                        submitting = false
                        if (success) {
                            Toast.makeText(context, "Feedback sent successfully to Admin!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } else {
                            resultMessage = err ?: "Failed to send feedback"
                        }
                    }
                }
            ) {
                Text(if (submitting) "Sending..." else "Submit")
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
fun ProfileScreenPreview() {
    CampusConnectAppTheme {
        ProfileScreen(
            profile = UserProfile(
                uid = "user_001",
                fullName = "Alex Johnson",
                email = "alex.johnson@student.unima.ac.mw",
                role = UserRole.STUDENT,
                identifier = "CS-2023-4091",
                department = "Computer Science & IT"
            ),
            onUpdateProfile = { _, _, _, _ -> },
            onSendFeedback = { _, _ -> },
            onLogout = {}
        )
    }
}
