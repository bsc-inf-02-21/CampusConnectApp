package com.example.campusconnectapp.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnectapp.data.Announcement
import com.example.campusconnectapp.data.UserProfile
import com.example.campusconnectapp.data.UserRole
import com.example.campusconnectapp.ui.theme.CampusConnectAppTheme
import com.example.campusconnectapp.viewmodel.AnnouncementViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    userProfile: UserProfile?,
    announcementViewModel: AnnouncementViewModel = viewModel(),
    onNavigateToServices: () -> Unit,
    onNavigateToCampusInfo: () -> Unit,
    onNavigateToLostFound: () -> Unit
) {
    val announcements by announcementViewModel.announcements.collectAsState()
    val userRole = userProfile?.role

    HomeScreenContent(
        userProfile = userProfile,
        userRole = userRole,
        announcements = announcements,
        canPost = userProfile == null || announcementViewModel.canPost(userRole),
        canDeleteAnnouncement = { announcement -> announcementViewModel.canDelete(announcement, userRole) },
        onAddAnnouncement = { title, content ->
            val profileToUse = userProfile ?: UserProfile(
                uid = "user_local",
                fullName = "Campus User",
                email = "user@unima.ac.mw",
                role = UserRole.ADMINISTRATOR,
                identifier = "ADMIN",
                department = "General"
            )
            announcementViewModel.addAnnouncement(title, content, profileToUse)
        },
        onDeleteAnnouncement = { announcement ->
            if (userProfile != null) {
                announcementViewModel.deleteAnnouncement(announcement, userProfile)
            }
        },
        onNavigateToServices = onNavigateToServices,
        onNavigateToCampusInfo = onNavigateToCampusInfo,
        onNavigateToLostFound = onNavigateToLostFound
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    userProfile: UserProfile?,
    userRole: UserRole?,
    announcements: List<Announcement>,
    canPost: Boolean,
    canDeleteAnnouncement: (Announcement) -> Boolean,
    onAddAnnouncement: (String, String) -> Unit,
    onDeleteAnnouncement: (Announcement) -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToCampusInfo: () -> Unit,
    onNavigateToLostFound: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newContent by remember { mutableStateOf("") }
    var announcementToDelete by remember { mutableStateOf<Announcement?>(null) }
    var expandedAnnouncementId by remember { mutableStateOf<String?>(null) }
    var selectedAnnouncementForDialog by remember { mutableStateOf<Announcement?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Welcome to ConnectCampus",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your centralized hub for academic services, announcements, and campus resources.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        item {
            Text(
                text = "Quick Access",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ElevatedCard(
                    onClick = onNavigateToServices,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Services", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                }

                ElevatedCard(
                    onClick = onNavigateToCampusInfo,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Campus Info", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                }

                ElevatedCard(
                    onClick = onNavigateToLostFound,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Lost & Found", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Announcements",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (canPost) {
                        Button(
                            onClick = { showAddDialog = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Post", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Post Announcement", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                val permissionHint = when (userRole) {
                    UserRole.STUDENT -> "Role: Student"
                    UserRole.LECTURER -> "Role: Lecturer"
                    UserRole.ADMINISTRATOR -> "Role: Administrator"
                    null -> "View only • Tap to expand"
                }
                Text(
                    text = permissionHint,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (announcements.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(text = "No announcements posted yet.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            items(announcements, key = { it.id }) { announcement ->
                val canDelete = canDeleteAnnouncement(announcement)
                val isExpanded = expandedAnnouncementId == announcement.id
                val interactionSource = remember { MutableInteractionSource() }
                val isHovered by interactionSource.collectIsHoveredAsState()
                val effectiveExpanded = isExpanded || isHovered
                val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
                val formattedDate = remember(announcement.date) { dateFormat.format(Date(announcement.date)) }

                OutlinedCard(
                    onClick = {
                        expandedAnnouncementId = if (isExpanded) null else announcement.id
                    },
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = announcement.authorName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = formattedDate,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.extraSmall,
                                    color = if (announcement.authorRole == UserRole.ADMINISTRATOR)
                                        MaterialTheme.colorScheme.errorContainer
                                    else
                                        MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = if (announcement.authorRole == UserRole.ADMINISTRATOR) "ADMIN" else "LECTURER",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (announcement.authorRole == UserRole.ADMINISTRATOR)
                                            MaterialTheme.colorScheme.onErrorContainer
                                        else
                                            MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (canDelete) {
                                    IconButton(
                                        onClick = { announcementToDelete = announcement }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Announcement",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = announcement.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val tooltipState = rememberTooltipState()
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            tooltip = {
                                PlainTooltip {
                                    Text(text = announcement.content)
                                }
                            },
                            state = tooltipState
                        ) {
                            Text(
                                text = announcement.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = if (effectiveExpanded) Int.MAX_VALUE else 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Footer expansion toggle bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { selectedAnnouncementForDialog = announcement },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInFull,
                                        contentDescription = "Full View",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Full Announcement Dialog (Opens on the same screen)
    if (selectedAnnouncementForDialog != null) {
        val selected = selectedAnnouncementForDialog!!
        val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault()) }
        val formattedDate = remember(selected.date) { dateFormat.format(Date(selected.date)) }

        AlertDialog(
            onDismissRequest = { selectedAnnouncementForDialog = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = selected.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Posted by ${selected.authorName}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = if (selected.authorRole == UserRole.ADMINISTRATOR)
                                MaterialTheme.colorScheme.errorContainer
                            else
                                MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = if (selected.authorRole == UserRole.ADMINISTRATOR) "ADMIN" else "LECTURER",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected.authorRole == UserRole.ADMINISTRATOR)
                                    MaterialTheme.colorScheme.onErrorContainer
                                else
                                    MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider()

                    Text(
                        text = selected.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                Button(onClick = { selectedAnnouncementForDialog = null }) {
                    Text("Close")
                }
            }
        )
    }

    if (showAddDialog && userProfile != null && canPost) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Post New Announcement") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Posting as ${userProfile.fullName} (${userRole?.name})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newContent,
                        onValueChange = { newContent = it },
                        label = { Text("Content") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank() && newContent.isNotBlank()) {
                            onAddAnnouncement(newTitle, newContent)
                            newTitle = ""
                            newContent = ""
                            showAddDialog = false
                        }
                    },
                    enabled = newTitle.isNotBlank() && newContent.isNotBlank()
                ) {
                    Text("Post")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (announcementToDelete != null && userProfile != null) {
        val target = announcementToDelete!!
        AlertDialog(
            onDismissRequest = { announcementToDelete = null },
            title = { Text("Delete Announcement") },
            text = { Text("Are you sure you want to delete \"${target.title}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAnnouncement(target)
                        announcementToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { announcementToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
