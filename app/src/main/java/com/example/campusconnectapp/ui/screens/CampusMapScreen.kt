package com.example.campusconnectapp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.campusconnectapp.ui.theme.CampusConnectAppTheme

data class Building(
    val name: String, 
    val info: String, 
    val location: String // Geo coordinates for UNIMA
)

@Composable
fun CampusMapScreen() {
    val context = LocalContext.current
    
    // Key UNIMA (Chancellor College) Locations
    val buildings = listOf(
        Building("Great Hall", "Main auditorium and graduation venue.", "geo:-15.3880,35.3340?q=Great+Hall+UNIMA"),
        Building("Library", "Main university library, open 24/7 during exams.", "geo:-15.3875,35.3325?q=Library+UNIMA"),
        Building("Administration Block", "Registry, Finance, and Management offices.", "geo:-15.3862,35.3318?q=Admin+Block+UNIMA"),
        Building("Chanco Clinic", "Student health and emergency services.", "geo:-15.3850,35.3300?q=Clinic+UNIMA"),
        Building("Science Labs", "Faculty of Science and computer labs.", "geo:-15.3868,35.3330?q=Science+Labs+UNIMA"),
        Building("Cafeteria", "Student dining hall and social area.", "geo:-15.3890,35.3350?q=Cafeteria+UNIMA")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Visual header for UNIMA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.LocationOn, 
                    contentDescription = null, 
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "University of Malawi (UNIMA)", 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("Zomba, Malawi", style = MaterialTheme.typography.bodySmall)
            }
        }

        Text(
            text = "Locate Buildings & Sites",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(buildings) { building ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = building.name, 
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = building.info, 
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(building.location))
                            context.startActivity(intent)
                        }) {
                            Icon(
                                Icons.Default.Navigation, 
                                contentDescription = "Locate",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CampusMapScreenPreview() {
    CampusConnectAppTheme {
        CampusMapScreen()
    }
}
