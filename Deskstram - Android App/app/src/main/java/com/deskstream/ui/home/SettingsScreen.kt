package com.deskstream.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deskstream.R
import com.deskstream.ui.theme.DeskStreamTheme

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onHomeTabClick: () -> Unit = {},
    onDevicesTabClick: () -> Unit = {},
    onFilesTabClick: () -> Unit = {},
    onProfileTabClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf("Settings") }

    Scaffold(
        bottomBar = {
            Surface(
                color = Color.White,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavItem("Home", Icons.Outlined.Home, selectedTab == "Home") {
                        selectedTab = "Home"
                        onHomeTabClick()
                    }
                    NavItem("Devices", Icons.Outlined.Devices, selectedTab == "Devices") {
                        selectedTab = "Devices"
                        onDevicesTabClick()
                    }
                    NavItem("Files", Icons.Outlined.Folder, selectedTab == "Files") {
                        selectedTab = "Files"
                        onFilesTabClick()
                    }
                    NavItem("Profile", Icons.Outlined.Person, selectedTab == "Profile") {
                        selectedTab = "Profile"
                        onProfileTabClick()
                    }
                }
            }
        },
        containerColor = Color(0xFFFAFAFC)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top Waves Header Background
            Image(
                painter = painterResource(id = R.drawable.settings_bg),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Top Header Bar with Back Button + Centered Logo Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    IconButton(
                        onClick = { onBackClick() },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Centered Brand Header (Logo + DeskStream Title + Tagline)
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.desk_stream_d),
                            contentDescription = "DeskStream Logo",
                            modifier = Modifier.size(36.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Desk",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A),
                                        fontSize = 18.sp,
                                        letterSpacing = (-0.5).sp,
                                        lineHeight = 18.sp
                                    )
                                )
                                Text(
                                    text = "Stream",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE50914),
                                        fontSize = 18.sp,
                                        letterSpacing = (-0.5).sp,
                                        lineHeight = 18.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "Secure. Fast. Remote Access.",
                                fontSize = 9.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium,
                                lineHeight = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Title Section
                Text(
                    text = "Settings",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Manage your app preferences",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Section 1 Card: Theme, Language, Notifications
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                ) {
                    Column {
                        SettingsRowWithValue(
                            iconEmoji = "☀️",
                            badgeBgColor = Color(0xFFFFF0F0),
                            label = "Theme",
                            value = "Light"
                        )
                        HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsRowWithValue(
                            iconEmoji = "🌐",
                            badgeBgColor = Color(0xFFE0F2FE),
                            label = "Language",
                            value = "English"
                        )
                        HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsSwitchRow(
                            iconEmoji = "🔔",
                            badgeBgColor = Color(0xFFFFF0F0),
                            label = "Notifications",
                            isChecked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2 Card: Change Password, Two-Factor Authentication
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                ) {
                    Column {
                        SettingsRowWithValue(
                            iconEmoji = "🔒",
                            badgeBgColor = Color(0xFFE0F2FE),
                            label = "Change Password",
                            value = ""
                        )
                        HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsRowWithValue(
                            iconEmoji = "🛡️",
                            badgeBgColor = Color(0xFFFFF0F0),
                            label = "Two-Factor Authentication",
                            value = "Off"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 3 Card: About, Privacy Policy, Terms of Service
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                ) {
                    Column {
                        SettingsRowWithValue(
                            iconEmoji = "ℹ️",
                            badgeBgColor = Color(0xFFE0F2FE),
                            label = "About",
                            value = "v1.0.0"
                        )
                        HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsRowWithValue(
                            iconEmoji = "📄",
                            badgeBgColor = Color(0xFFFFF0F0),
                            label = "Privacy Policy",
                            value = ""
                        )
                        HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsRowWithValue(
                            iconEmoji = "📄",
                            badgeBgColor = Color(0xFFE0F2FE),
                            label = "Terms of Service",
                            value = ""
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Logout Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFF0F0))
                        .border(1.dp, Color(0xFFFEE2E2), RoundedCornerShape(16.dp))
                        .clickable { onLogoutClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color(0xFFE50914),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Logout",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE50914)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SettingsRowWithValue(
    iconEmoji: String,
    badgeBgColor: Color,
    label: String,
    value: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(badgeBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(text = iconEmoji, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(text = "›", fontSize = 18.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
fun SettingsSwitchRow(
    iconEmoji: String,
    badgeBgColor: Color,
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(badgeBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(text = iconEmoji, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A)
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFE50914),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFCBD5E1)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    DeskStreamTheme {
        SettingsScreen()
    }
}
