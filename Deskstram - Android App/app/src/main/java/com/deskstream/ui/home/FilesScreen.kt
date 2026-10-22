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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deskstream.R
import com.deskstream.ui.theme.DeskStreamTheme

data class FileFolderItem(
    val name: String,
    val detail: String,
    val badgeLabel: String,
    val badgeBgColor: Color,
    val badgeTextColor: Color,
    val isFolder: Boolean = false
)

@Composable
fun FilesScreen(
    onBackClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onHomeTabClick: () -> Unit = {},
    onDevicesTabClick: () -> Unit = {},
    onProfileTabClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("Files") }
    var hasUnreadNotifications by remember { mutableStateOf(true) }

    val filesAndFolders = remember {
        listOf(
            FileFolderItem("Documents", "12 items • 2 hours ago", "📁", Color(0xFFE0F2FE), Color(0xFF0284C7), true),
            FileFolderItem("Images", "8 items • 1 day ago", "📁", Color(0xFFFEF3C7), Color(0xFFD97706), true),
            FileFolderItem("Videos", "3 items • 3 days ago", "📁", Color(0xFFDCFCE7), Color(0xFF16A34A), true),
            FileFolderItem("Others", "5 items • 5 days ago", "📁", Color(0xFFFEE2E2), Color(0xFFDC2626), true),
            FileFolderItem("Project_Report.pdf", "2.4 MB • Today, 10:30 AM", "PDF", Color(0xFFFEE2E2), Color(0xFFDC2626)),
            FileFolderItem("Screenshot.png", "1.2 MB • Yesterday, 06:20 PM", "PNG", Color(0xFFDCFCE7), Color(0xFF16A34A)),
            FileFolderItem("Meeting_Notes.docx", "856 KB • 12 Oct 2024, 04:12 PM", "DOCX", Color(0xFFE0F2FE), Color(0xFF0284C7))
        )
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            // 1. Top Bar (Back Arrow + Logo Header & Bell + Avatar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onBackClick() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Notification Bell Badge with Red Dot Indicator
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                            .clickable { onNotificationClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(24.dp)
                            )
                            if (hasUnreadNotifications) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 2.dp, y = (-1).dp)
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF0000))
                                        .border(1.5.dp, Color.White, CircleShape)
                                )
                            }
                        }
                    }

                    // User Avatar Circle (Opens Profile)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE50914))
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "US",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Title Header ("Files" & "Your files, anywhere.")
            Text(
                text = "Files",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Your files, anywhere.",
                fontSize = 14.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Search Field with Upload Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search files...",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    },
                    leadingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 8.dp, end = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(18.dp)
                                    .background(Color(0xFFCBD5E1))
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFFCBD5E1),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                )

                Button(
                    onClick = { },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914)),
                    modifier = Modifier.height(48.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⊕ ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Upload",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Files & Folders List Cards
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                filesAndFolders.forEach { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(item.badgeBgColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (item.isFolder) {
                                        Icon(
                                            imageVector = Icons.Outlined.Folder,
                                            contentDescription = "Folder",
                                            tint = item.badgeTextColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Text(
                                            text = item.badgeLabel,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = item.badgeTextColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = item.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = item.detail,
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "Options",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FilesScreenPreview() {
    DeskStreamTheme {
        FilesScreen()
    }
}
