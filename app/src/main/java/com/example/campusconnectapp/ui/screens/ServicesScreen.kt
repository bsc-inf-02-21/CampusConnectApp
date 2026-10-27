package com.example.campusconnectapp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.campusconnectapp.data.UserRole
import com.example.campusconnectapp.ui.theme.CampusConnectAppTheme

data class UnimaService(
    val title: String,
    val url: String,
    val description: String,
    val icon: ImageVector,
    val category: String,
    val targetRoles: List<UserRole> = emptyList() // Empty list means applicable to ALL roles
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    userRole: UserRole? = null,
    onOpenUrl: (String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val effectiveRole = userRole ?: UserRole.STUDENT

    val categories = listOf("All", "Academics", "Financial & Loans", "Staff & Admin", "Library & Research", "ICT & Systems")

    val allServices = listOf(
        // --- STUDENT SPECIFIC SERVICES ---
        UnimaService(
            title = "UNIMA Student Portal",
            url = "https://students.unima.ac.mw/login.php",
            description = "Access course registration, exam results, fee statements, and official transcripts.",
            icon = Icons.Default.Person,
            category = "Academics",
            targetRoles = listOf(UserRole.STUDENT)
        ),
        UnimaService(
            title = "Google Classroom (Student)",
            url = "https://classroom.google.com/",
            description = "Submit coursework assignments, track deadlines, and receive class notices.",
            icon = Icons.Default.Class,
            category = "Academics",
            targetRoles = listOf(UserRole.STUDENT)
        ),

        UnimaService(
            title = "HESLGB Loans & Allowances",
            url = "https://sfmis.heslgb.mw/login",
            description = "Check Higher Education Students Grants & Loans status, tuition payments, and upkeep allowances.",
            icon = Icons.Default.AccountBalance,
            category = "Financial & Loans",
            targetRoles = listOf(UserRole.STUDENT)
        ),
        UnimaService(
            title = "UNIMA Moodle E-Learning",
            url = "https://moodle.unima.ac.mw/login/index.php",
            description = "Download lecture slides, attempt continuous assessment quizzes, and join class forums.",
            icon = Icons.Default.School,
            category = "Academics",
            targetRoles = listOf(UserRole.STUDENT, UserRole.LECTURER)
        ),
        // --- LECTURER SPECIFIC SERVICES ---
        UnimaService(
            title = "Google Classroom (Lecturer)",
            url = "https://classroom.google.com/",
            description = "Manage classes, create coursework, grade student assignments, and post announcements.",
            icon = Icons.Default.Class,
            category = "Academics",
            targetRoles = listOf(UserRole.LECTURER, UserRole.ADMINISTRATOR)
        ),
        UnimaService(
            title = "UNIMA Lecturer & Faculty Portal",
            url = "https://students.unima.ac.mw/",
            description = "Access class lists, enter student assessment grades, and view teaching schedules.",
            icon = Icons.Default.School,
            category = "Academics",
            targetRoles = listOf(UserRole.LECTURER)
        ),
        UnimaService(
            title = "UNIMA Moodle Course Management",
            url = "https://moodle.unima.ac.mw/login/index.php",
            description = "Upload lecture slides, manage online module content, and track student submissions.",
            icon = Icons.Default.School,
            category = "Academics",
            targetRoles = listOf(UserRole.LECTURER)
        ),
        UnimaService(
            title = "UNIMA Staff Email & Webmail",
            url = "https://mail.google.com/a/unima.ac.mw",
            description = "Official UNIMA staff email for faculty communication and institutional correspondence.",
            icon = Icons.Default.Email,
            category = "Staff & Admin",
            targetRoles = listOf(UserRole.LECTURER, UserRole.ADMINISTRATOR)
        ),

        // --- ADMINISTRATOR SPECIFIC SERVICES ---
        UnimaService(
            title = "UNIMA Staff & Admin System",
            url = "https://www.unima.ac.mw/",
            description = "Administrative records management, staff portal, and university registry systems.",
            icon = Icons.Default.AdminPanelSettings,
            category = "Staff & Admin",
            targetRoles = listOf(UserRole.ADMINISTRATOR)
        ),
        UnimaService(
            title = "UNIMA ICT Infrastructure & Systems",
            url = "https://www.unima.ac.mw/",
            description = "Manage campus networks, portal accounts, server infrastructure, and security settings.",
            icon = Icons.Default.Computer,
            category = "ICT & Systems",
            targetRoles = listOf(UserRole.ADMINISTRATOR)
        ),

        // --- GENERAL SERVICES FOR ALL ROLES ---
        UnimaService(
            title = "ODEL Distance Learning",
            url = "https://www.unima.ac.mw/odel",
            description = "Open, Distance and E-Learning platform for distance learning modules and support.",
            icon = Icons.Default.Language,
            category = "Academics",
            targetRoles = emptyList()
        ),
        UnimaService(
            title = "UNIMA Library & Repository",
            url = "https://www.unima.ac.mw/library/",
            description = "Access past examination papers, online catalog, digital thesis repository, and e-journals.",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            category = "Library & Research",
            targetRoles = emptyList()
        ),
        UnimaService(
            title = "ICT & Wi-Fi Helpdesk",
            url = "https://www.unima.ac.mw/",
            description = "Support for @unima.ac.mw emails, EDUROAM campus Wi-Fi, and portal password resets.",
            icon = Icons.Default.Computer,
            category = "ICT & Systems",
            targetRoles = emptyList()
        ),
        UnimaService(
            title = "UNIMA Official Domain",
            url = "https://www.unima.ac.mw/",
            description = "Main university portal for academic calendars, press releases, and official campus notices.",
            icon = Icons.Default.Language,
            category = "Academics",
            targetRoles = emptyList()
        )
    )

    val filteredShortcuts = allServices.filter { service ->
        val matchesRole = service.targetRoles.isEmpty() || service.targetRoles.contains(effectiveRole)
        val matchesCategory = (selectedCategory == "All" || service.category == selectedCategory)
        val matchesQuery = service.title.contains(searchQuery, ignoreCase = true) ||
                             service.description.contains(searchQuery, ignoreCase = true)
        matchesRole && matchesCategory && matchesQuery
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column {
            Text(
                text = "UNIMA Services & Portals",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            val roleBadgeText = when (effectiveRole) {
                UserRole.STUDENT -> "Student View • Academic & Student Services"
                UserRole.LECTURER -> "Lecturer View • Course Management & Staff Services"
                UserRole.ADMINISTRATOR -> "Administrator View • Staff & Administrative Services"
            }

            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                color = when (effectiveRole) {
                    UserRole.ADMINISTRATOR -> MaterialTheme.colorScheme.errorContainer
                    UserRole.LECTURER -> MaterialTheme.colorScheme.secondaryContainer
                    UserRole.STUDENT -> MaterialTheme.colorScheme.primaryContainer
                },
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = roleBadgeText,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = when (effectiveRole) {
                        UserRole.ADMINISTRATOR -> MaterialTheme.colorScheme.onErrorContainer
                        UserRole.LECTURER -> MaterialTheme.colorScheme.onSecondaryContainer
                        UserRole.STUDENT -> MaterialTheme.colorScheme.onPrimaryContainer
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search services...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category, style = MaterialTheme.typography.labelMedium) }
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredShortcuts) { shortcut ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(shortcut.url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                onOpenUrl(shortcut.url)
                            }
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = shortcut.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = shortcut.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = shortcut.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = shortcut.category,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Launch,
                            contentDescription = "Open Link",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Admin Services View")
@Composable
fun ServicesScreenAdminPreview() {
    CampusConnectAppTheme {
        ServicesScreen(userRole = UserRole.ADMINISTRATOR, onOpenUrl = {})
    }
}

@Preview(showBackground = true, name = "Lecturer Services View")
@Composable
fun ServicesScreenLecturerPreview() {
    CampusConnectAppTheme {
        ServicesScreen(userRole = UserRole.LECTURER, onOpenUrl = {})
    }
}

@Preview(showBackground = true, name = "Student Services View")
@Composable
fun ServicesScreenStudentPreview() {
    CampusConnectAppTheme {
        ServicesScreen(userRole = UserRole.STUDENT, onOpenUrl = {})
    }
}
