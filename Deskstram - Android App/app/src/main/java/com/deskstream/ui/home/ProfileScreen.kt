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
fun ProfileScreen(
    userName: String = "User Name",
    userEmail: String = "user@deskstream.com",
    onBackClick: () -> Unit = {},
    onHomeTabClick: () -> Unit = {},
    onDevicesTabClick: () -> Unit = {},
    onFilesTabClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf("Profile") }

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
                painter = painterResource(id = R.drawable.profile_sc_),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
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
                            .border(1.dp, Color(0xFFFEE2E2), CircleShape)
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

                val initials = (userName.trim().take(2).ifEmpty { "US" }).uppercase()

                // Profile Avatar & Main Info
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF0000)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = userName.ifBlank { "User Name" },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = userEmail.ifBlank { "user@deskstream.com" },
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Online",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF10B981)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // "Edit Profile" Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFFFF0F0))
                        .border(1.dp, Color(0xFFFEE2E2), RoundedCornerShape(20.dp))
                        .clickable { }
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "✏️",
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Edit Profile",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE50914)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Card Section 1: Account Details, Change Password, Notifications
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                ) {
                    Column {
                        ProfileSettingRow(
                            iconEmoji = "👤",
                            label = "Account Details"
                        )
                        HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))
                        ProfileSettingRow(
                            iconEmoji = "🔒",
                            label = "Change Password"
                        )
                        HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 12.dp))
                        ProfileSwitchRow(
                            iconEmoji = "🔔",
                            label = "Notifications",
                            isChecked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card Section 2: Theme, Language
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                ) {
                    Column {
                        ProfileSettingValueRow(
                            iconEmoji = "🖥️",
                            label = "Theme",
                            value = "Light"
                        )
                        HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))
                        ProfileSettingValueRow(
                            iconEmoji = "🌐",
                            label = "Language",
                            value = "English"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card Section 3: About, Privacy Policy, Terms of Service
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                ) {
                    Column {
                        ProfileSettingRow(
                            iconEmoji = "ℹ️",
                            label = "About"
                        )
                        HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))
                        ProfileSettingRow(
                            iconEmoji = "🛡️",
                            label = "Privacy Policy"
                        )
                        HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))
                        ProfileSettingRow(
                            iconEmoji = "📄",
                            label = "Terms of Service"
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
fun ProfileSettingRow(
    iconEmoji: String,
    label: String,
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
            Text(text = iconEmoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A)
            )
        }
        Text(text = "›", fontSize = 18.sp, color = Color(0xFF94A3B8))
    }
}

@Composable
fun ProfileSettingValueRow(
    iconEmoji: String,
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
            Text(text = iconEmoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 14.sp,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "›", fontSize = 18.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
fun ProfileSwitchRow(
    iconEmoji: String,
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = iconEmoji, fontSize = 16.sp)
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
fun ProfileScreenPreview() {
    DeskStreamTheme {
        ProfileScreen()
    }
}
