package com.example.campusconnectapp.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.campusconnectapp.data.CampusInfo
import com.example.campusconnectapp.data.InfoType
import com.example.campusconnectapp.ui.theme.CampusConnectAppTheme
import com.example.campusconnectapp.utils.WebUtils

fun launchPhoneDialer(context: Context, phone: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.trim()}"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to open phone dialer: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

fun launchEmailApp(context: Context, email: String) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${email.trim()}"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to open email app: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

fun launchWebUrl(context: Context, url: String) {
    WebUtils.openUrlInstantly(context, url)
}

@Composable
fun CampusInfoScreen(onNavigateToMap: () -> Unit) {
    val context = LocalContext.current

    val infoList = listOf(
        CampusInfo(
            id = "1",
            title = "Campus Security Emergency",
            detail = "24/7 Hotline for immediate campus security & emergency assistance.",
            type = InfoType.EMERGENCY,
            phone = "+265991894726",
            email = "security@unima.ac.mw"
        ),
        CampusInfo(
            id = "2",
            title = "UNIMA Medical Center & Clinic",
            detail = "Building 4, Room 102. Open Mon-Fri 8:00 AM - 6:00 PM for medical services.",
            type = InfoType.EMERGENCY,
            phone = "0988956325",
            email = "clinic@unima.ac.mw"
        ),
        CampusInfo(
            id = "3",
            title = "University Registrar & Administration",
            detail = "Main Admin Block. Handles admissions, registration, and academic transcripts.",
            type = InfoType.EMERGENCY,
            phone = "+2659824000",
            email = "registrar@unima.ac.mw"
        ),
        CampusInfo(
            id = "4",
            title = "UNIMA Official Website",
            detail = "Main university portal for academic calendars, news, and official announcements.",
            type = InfoType.MAP,
            url = "https://www.unima.ac.mw"
        ),
        CampusInfo(
            id = "5",
            title = "Central Library & Study Halls",
            detail = "Central Library Building, open 24/7 during exam periods. Contact for research assistance.",
            type = InfoType.MAP,
            phone = "+265890524111",
            email = "library@unima.ac.mw"
        ),
        CampusInfo(
            id = "6",
            title = "ICT & E-Learning Support Desk",
            detail = "Computer Centre, Room 12. Technical support for student portals & Moodle.",
            type = InfoType.ANNOUNCEMENT,
            phone = "+265881524444",
            email = "ict-support@unima.ac.mw"
        ),
        CampusInfo(
            id = "7",
            title = "Dean of Students Office",
            detail = "Student welfare, accommodation, and campus life support.",
            type = InfoType.ANNOUNCEMENT,
            phone = "+265983804455",
            email = "deanofstudents@unima.ac.mw"
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Campus Information & Contacts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Tap any contact detail to call, send email, or launch maps",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        item {
            Button(
                onClick = onNavigateToMap,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Map, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Interactive UNIMA Campus Map")
            }
        }

        items(infoList) { info ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        when {
                            info.phone != null -> launchPhoneDialer(context, info.phone)
                            info.email != null -> launchEmailApp(context, info.email)
                            info.url != null -> launchWebUrl(context, info.url)
                            else -> onNavigateToMap()
                        }
                    }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon = when (info.type) {
                            InfoType.EMERGENCY -> Icons.Default.Call
                            InfoType.MAP -> Icons.Default.Map
                            InfoType.ANNOUNCEMENT -> Icons.Default.Info
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = info.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = info.detail,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        info.phone?.let { phoneNum ->
                            OutlinedButton(
                                onClick = { launchPhoneDialer(context, phoneNum) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        info.email?.let { emailAddr ->
                            OutlinedButton(
                                onClick = { launchEmailApp(context, emailAddr) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Email, contentDescription = "Email", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Email", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        info.url?.let { webUrl ->
                            OutlinedButton(
                                onClick = { launchWebUrl(context, webUrl) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Language, contentDescription = "Website", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Website", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CampusInfoScreenPreview() {
    CampusConnectAppTheme {
        CampusInfoScreen(onNavigateToMap = {})
    }
}
