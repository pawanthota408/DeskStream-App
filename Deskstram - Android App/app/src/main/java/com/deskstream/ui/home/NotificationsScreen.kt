package com.deskstream.ui.home

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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deskstream.ui.theme.DeskStreamTheme

data class NotificationItem(
    val title: String,
    val message: String,
    val timeAgo: String,
    val iconVector: ImageVector,
    val iconBgColor: Color,
    val iconTint: Color,
    val isUnread: Boolean = false
)

@Composable
fun NotificationsScreen(
    viewModel: HomeViewModel? = null,
    onBackClick: () -> Unit = {}
) {
    val uiState = viewModel?.uiState?.collectAsState()?.value
    val notificationsList = remember(uiState?.notifications) {
        val live = uiState?.notifications
        if (!live.isNullOrEmpty()) {
            live.map { notif ->
                NotificationItem(
                    title = notif.title,
                    message = notif.message,
                    timeAgo = notif.createdAt.ifBlank { "Recently" },
                    iconVector = Icons.Outlined.Notifications,
                    iconBgColor = if (notif.isRead == 1) Color(0xFFF1F5F9) else Color(0xFFDCFCE7),
                    iconTint = if (notif.isRead == 1) Color(0xFF64748B) else Color(0xFF16A34A),
                    isUnread = notif.isRead == 0
                )
            }
        } else {
            listOf(
                NotificationItem(
                    title = "New Connection Established",
                    message = "Office PC connected successfully to your DeskStream session.",
                    timeAgo = "10 mins ago",
                    iconVector = Icons.Outlined.Computer,
                    iconBgColor = Color(0xFFDCFCE7),
                    iconTint = Color(0xFF16A34A),
                    isUnread = true
                ),
                NotificationItem(
                    title = "File Transfer Completed",
                    message = "Project_Report.pdf (2.4 MB) was received from Home Laptop.",
                    timeAgo = "1 hour ago",
                    iconVector = Icons.Outlined.Folder,
                    iconBgColor = Color(0xFFE0F2FE),
                    iconTint = Color(0xFF0284C7),
                    isUnread = true
                ),
                NotificationItem(
                    title = "Security Alert",
                    message = "New login detected from Windows 11 device in California.",
                    timeAgo = "3 hours ago",
                    iconVector = Icons.Outlined.Security,
                    iconBgColor = Color(0xFFFFF0F0),
                    iconTint = Color(0xFFE50914),
                    isUnread = false
                ),
                NotificationItem(
                    title = "App Updated",
                    message = "DeskStream v1.0.1 successfully installed with bug fixes.",
                    timeAgo = "1 day ago",
                    iconVector = Icons.Outlined.CheckCircle,
                    iconBgColor = Color(0xFFF3E8FF),
                    iconTint = Color(0xFF9333EA),
                    isUnread = false
                )
            )
        }
    }

    Scaffold(
        containerColor = Color(0xFFFAFAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            // Top Bar: Back Button + Screen Title & Clear All
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onBackClick() },
                        modifier = Modifier
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

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            fontSize = 20.sp
                        )
                    )
                }

                if (notificationsList.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFFFF0F0))
                            .border(1.dp, Color(0xFFFEE2E2), RoundedCornerShape(20.dp))
                            .clickable { viewModel?.markAllNotificationsRead() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Clear All",
                            tint = Color(0xFFE50914),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Clear All",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE50914)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (notificationsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "No Notifications",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Notifications",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You're all caught up! Check back later.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    notificationsList.forEach { notification ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (notification.isUnread) Color(0xFFFEF2F2) else Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (notification.isUnread) Color(0xFFFEE2E2) else Color(0xFFF1F5F9),
                                    RoundedCornerShape(16.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(notification.iconBgColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = notification.iconVector,
                                        contentDescription = notification.title,
                                        tint = notification.iconTint,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = notification.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = notification.timeAgo,
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = notification.message,
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
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
fun NotificationsScreenPreview() {
    DeskStreamTheme {
        NotificationsScreen()
    }
}
