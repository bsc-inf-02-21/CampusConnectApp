package com.example.campusconnectapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnectapp.ui.screens.*
import com.example.campusconnectapp.ui.theme.CampusConnectAppTheme
import com.example.campusconnectapp.utils.WebUtils
import com.example.campusconnectapp.viewmodel.ProfileViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()
        setContent {
            CampusConnectAppTheme {
                ConnectCampusApp()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Services : Screen("services", "Services", Icons.Default.School)
    object CampusInfo : Screen("campus_info", "Info", Icons.Default.Info)
    object CampusMap : Screen("campus_map", "Map", Icons.Default.Map)
    object LostFound : Screen("lost_found", "Lost & Found", Icons.Default.Search)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

@Composable
fun ConnectCampusApp() {
    val profileViewModel: ProfileViewModel = viewModel()
    val currentProfile by profileViewModel.userProfile.collectAsState()

    if (currentProfile == null) {
        LoginScreen(viewModel = profileViewModel)
    } else {
        MainAppContent(profileViewModel = profileViewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(profileViewModel: ProfileViewModel) {
    val currentProfile by profileViewModel.userProfile.collectAsState()
    val navHistory = remember { mutableStateListOf<Screen>(Screen.Home) }
    val currentScreen = navHistory.lastOrNull() ?: Screen.Home
    val bottomNavItems = listOf(Screen.Home, Screen.Services, Screen.CampusInfo, Screen.LostFound, Screen.Profile)
    val context = LocalContext.current

    // Handle system back button
    BackHandler(enabled = navHistory.size > 1) {
        if (navHistory.size > 1) {
            navHistory.removeAt(navHistory.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "ConnectCampus - ${currentScreen.title}") },
                navigationIcon = {
                    if (navHistory.size > 1) {
                        IconButton(onClick = {
                            if (navHistory.size > 1) {
                                navHistory.removeAt(navHistory.size - 1)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentScreen == screen || (currentScreen == Screen.CampusMap && screen == Screen.CampusInfo),
                        onClick = {
                            if (currentScreen != screen) {
                                if (screen == Screen.Home) {
                                    navHistory.clear()
                                    navHistory.add(Screen.Home)
                                } else {
                                    if (navHistory.contains(screen)) {
                                        while (navHistory.lastOrNull() != screen) {
                                            navHistory.removeAt(navHistory.size - 1)
                                        }
                                    } else {
                                        navHistory.add(screen)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (currentScreen) {
                is Screen.Home -> HomeScreen(
                    userProfile = currentProfile,
                    onNavigateToServices = { 
                        if (!navHistory.contains(Screen.Services)) navHistory.add(Screen.Services)
                        else {
                            navHistory.remove(Screen.Services)
                            navHistory.add(Screen.Services)
                        }
                    },
                    onNavigateToCampusInfo = { 
                        if (!navHistory.contains(Screen.CampusInfo)) navHistory.add(Screen.CampusInfo)
                        else {
                            navHistory.remove(Screen.CampusInfo)
                            navHistory.add(Screen.CampusInfo)
                        }
                    },
                    onNavigateToLostFound = { 
                        if (!navHistory.contains(Screen.LostFound)) navHistory.add(Screen.LostFound)
                        else {
                            navHistory.remove(Screen.LostFound)
                            navHistory.add(Screen.LostFound)
                        }
                    }
                )
                is Screen.Services -> ServicesScreen(
                    userRole = currentProfile?.role,
                    onOpenUrl = { url ->
                        WebUtils.openUrlInstantly(context, url)
                    }
                )
                is Screen.CampusInfo -> CampusInfoScreen(
                    onNavigateToMap = {
                        if (!navHistory.contains(Screen.CampusMap)) navHistory.add(Screen.CampusMap)
                    }
                )
                is Screen.CampusMap -> CampusMapScreen()
                is Screen.LostFound -> LostFoundScreen()
                is Screen.Profile -> ProfileScreen(viewModel = profileViewModel)
            }
        }
    }
}
