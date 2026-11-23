package com.example.campusconnectapp.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.campusconnectapp.viewmodel.ValidationUtils
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.campusconnectapp.data.ItemType
import com.example.campusconnectapp.data.LostItem
import com.example.campusconnectapp.ui.theme.CampusConnectAppTheme
import com.example.campusconnectapp.utils.ImageUtils
import com.example.campusconnectapp.viewmodel.LostFoundViewModel
import com.example.campusconnectapp.viewmodel.ProfileViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

fun createTempImageUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", context.cacheDir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

@Composable
fun LostFoundScreen(
    viewModel: LostFoundViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val itemsList by viewModel.itemsList.collectAsState()
    val currentProfile by profileViewModel.userProfile.collectAsState()
    val currentUserId = currentProfile?.uid ?: ""
    val currentUserEmail = currentProfile?.email ?: ""

    LostFoundContent(
        itemsList = itemsList,
        currentUserId = currentUserId,
        currentUserEmail = currentUserEmail,
        onAddItem = { title, category, description, location, contact, type, imageUrl ->
            viewModel.addItem(title, category, description, location, contact, type, imageUrl, currentUserId)
        },
        onHideItem = { item ->
            viewModel.hideItem(item, currentUserId.ifBlank { "preview_user" })
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostFoundContent(
    itemsList: List<LostItem>,
    currentUserId: String? = null,
    currentUserEmail: String? = null,
    onAddItem: (String, String, String, String, String, ItemType, String?) -> Unit = { _, _, _, _, _, _, _ -> },
    onHideItem: (LostItem) -> Unit = {}
) {
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Personal Items") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var itemType by remember { mutableStateOf(ItemType.LOST) }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri.toString()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempPhotoUri != null) {
            selectedImageUri = tempPhotoUri.toString()
        }
    }

    val filteredItems = itemsList.filter { item ->
        !item.isHidden && (
            item.title.contains(searchQuery, ignoreCase = true) ||
            item.category.contains(searchQuery, ignoreCase = true) ||
            item.description.contains(searchQuery, ignoreCase = true) ||
            item.location.contains(searchQuery, ignoreCase = true)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Post Item")
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Campus Lost & Found",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search lost & found items...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredItems) { item ->
                        LostItemCard(
                            item = item,
                            currentUserId = currentUserId,
                            currentUserEmail = currentUserEmail,
                            onHide = { onHideItem(item) }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false),
                title = { Text("Report Lost or Found Item") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Item Title") }, singleLine = true)
                        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, singleLine = true)
                        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                        OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, singleLine = true)
                        OutlinedTextField(
                            value = contact,
                            onValueChange = { input ->
                                if (input.isEmpty()) {
                                    contact = ""
                                } else if (input.first().isDigit()) {
                                    // Restrict to digits only and cap at maximum 10 digits
                                    val filteredDigits = input.filter { it.isDigit() }
                                    if (filteredDigits.length <= 10) {
                                        contact = filteredDigits
                                    }
                                } else {
                                    // Email mode
                                    contact = input
                                }
                            },
                            label = { Text("Contact Email / Phone") },
                            singleLine = true,
                            supportingText = {
                                if (contact.isNotEmpty() && contact.first().isDigit()) {
                                    Text("${contact.length}/10 digits • Must start with 098, 099, or 088")
                                }
                            }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = itemType == ItemType.LOST,
                                onClick = { itemType = ItemType.LOST },
                                label = { Text("Lost") }
                            )
                            FilterChip(
                                selected = itemType == ItemType.FOUND,
                                onClick = { itemType = ItemType.FOUND },
                                label = { Text("Found") }
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Item Photo Options:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val photoUri = createTempImageUri(context)
                                        tempPhotoUri = photoUri
                                        cameraLauncher.launch(photoUri)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Unable to launch camera: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Camera", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(
                                onClick = {
                                    galleryLauncher.launch("image/*")
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gallery", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        if (selectedImageUri != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = ImageUtils.getLoadableModel(selectedImageUri),
                                        contentDescription = "Selected Photo Preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { selectedImageUri = null },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove Photo",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (title.isBlank() || location.isBlank()) {
                                Toast.makeText(context, "Please enter Item Title and Location", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (contact.isNotBlank()) {
                                val contactError = ValidationUtils.validateContact(contact)
                                if (contactError != null) {
                                    Toast.makeText(context, contactError, Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                            }

                            val compressedImageStr = if (!selectedImageUri.isNullOrBlank()) {
                                try {
                                    val uri = Uri.parse(selectedImageUri)
                                    ImageUtils.uriToBase64(context, uri)
                                } catch (e: Exception) {
                                    null
                                }
                            } else {
                                null
                            }

                            onAddItem(
                                title,
                                category,
                                description,
                                location,
                                contact.ifBlank { "student1@unima.ac.mw" },
                                itemType,
                                compressedImageStr
                            )
                            Toast.makeText(context, "Item posted successfully!", Toast.LENGTH_SHORT).show()
                            title = ""
                            description = ""
                            location = ""
                            contact = ""
                            selectedImageUri = null
                            showDialog = false
                        },
                        enabled = title.isNotBlank() && location.isNotBlank()
                    ) {
                        Text("Post")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        selectedImageUri = null
                        showDialog = false
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun LostItemCard(
    item: LostItem,
    currentUserId: String?,
    currentUserEmail: String?,
    onHide: () -> Unit
) {
    val context = LocalContext.current
    var showFullImageDialog by remember { mutableStateOf(false) }
    var showHideConfirmationDialog by remember { mutableStateOf(false) }

    if (showHideConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showHideConfirmationDialog = false },
            title = { Text("Hide Post") },
            text = { Text("Do you want to hide this post?") },
            confirmButton = {
                Button(
                    onClick = {
                        showHideConfirmationDialog = false
                        onHide()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHideConfirmationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showFullImageDialog && !item.imageUrl.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = { showFullImageDialog = false },
            confirmButton = {
                Button(onClick = { showFullImageDialog = false }) {
                    Text("Close")
                }
            },
            title = { Text(item.title) },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageUtils.getLoadableModel(item.imageUrl),
                        contentDescription = "Full Size Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        )
    }

    val onContactClick = {
        try {
            if (item.contact.contains("@")) {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${item.contact.trim()}"))
                context.startActivity(intent)
            } else {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.contact.trim()}"))
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to contact: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    // Strict permission check: only the exact user who posted the item can see the resolution button
    val canHide = !currentUserId.isNullOrBlank() && !item.postedByUid.isNullOrBlank() && currentUserId == item.postedByUid

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (item.type == ItemType.LOST) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (item.type == ItemType.LOST) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (item.type == ItemType.LOST) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.type.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "Category: ${item.category}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = item.description, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Location: ${item.location}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )

            if (!item.imageUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clickable { showFullImageDialog = true },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = ImageUtils.getLoadableModel(item.imageUrl),
                            contentDescription = "Item Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "Tap image to view full screen",
                                modifier = Modifier.padding(4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AssistChip(
                onClick = onContactClick,
                label = {
                    Text(
                        text = "Contact: ${item.contact}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (item.contact.contains("@")) Icons.Default.Email else Icons.Default.Call,
                        contentDescription = "Contact",
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )

            if (canHide) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { showHideConfirmationDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mark as Resolved",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = formatDate(item.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

fun formatDate(time: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(Date(time))
}


