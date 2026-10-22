package com.deskstream.ui.home

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deskstream.R
import com.deskstream.ui.theme.DeskStreamTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState

data class RecentDeviceItem(
    val name: String,
    val statusText: String,
    val isOnline: Boolean,
    val iconVector: ImageVector
)

data class RecentFileItem(
    val fileName: String,
    val fileDetails: String,
    val badgeLabel: String,
    val badgeBgColor: Color,
    val badgeTextColor: Color
)

data class HeroBannerPage(
    val category: String,
    val titlePrefix: String,
    val titleRed: String,
    val subtitle: String,
    val buttonText: String,
    val graphicEmoji: String
)

fun getPersistentDeviceId(context: Context): String {
    return try {
        val prefs = context.getSharedPreferences("deskstream_prefs", Context.MODE_PRIVATE)
        var deviceId = prefs?.getString("unique_device_id", null)
        if (deviceId == null) {
            val part1 = (100..999).random()
            val part2 = (100..999).random()
            val part3 = (100..999).random()
            deviceId = "$part1 $part2 $part3"
            prefs?.edit()?.putString("unique_device_id", deviceId)?.apply()
        }
        deviceId ?: "984 271 305"
    } catch (e: Exception) {
        "984 271 305"
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    hasUnreadNotifications: Boolean = true,
    onProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onDevicesTabClick: () -> Unit = {},
    onFilesTabClick: () -> Unit = {},
    onSharedClick: () -> Unit = {},
    onProfileTabClick: () -> Unit = {},
    onSettingsTabClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val persistentDeviceId = remember { getPersistentDeviceId(context) }
    // Observe live data from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val hasUnreadFromApi = uiState.unreadNotificationCount > 0

    var selectedTab by remember { mutableStateOf("Home") }
    var heroPageIndex by remember { mutableIntStateOf(0) }
    var isCopiedToastShown by remember { mutableStateOf(false) }

    // Remote Device Code Form State
    var remoteDeviceCode by remember { mutableStateOf("") }
    var connectionErrorMsg by remember { mutableStateOf<String?>(null) }
    var connectionSuccessMsg by remember { mutableStateOf<String?>(null) }

    val deviceModelName = remember {
        val manufacturer = android.os.Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = android.os.Build.MODEL
        "$manufacturer $model Mobile"
    }

    // Register mobile device and keep heartbeats & incoming listener active
    LaunchedEffect(persistentDeviceId) {
        viewModel.startDeviceHeartbeatAndListener(persistentDeviceId, deviceModelName)
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val heroPages = remember {
        listOf(
            HeroBannerPage(
                category = "REMOTE DESKTOP ACCESS",
                titlePrefix = "Work from ",
                titleRed = "Anywhere",
                subtitle = "Access your devices securely from any location, anytime.",
                buttonText = "Connect Now  ➔",
                graphicEmoji = "💻"
            ),
            HeroBannerPage(
                category = "FAST FILE TRANSFER",
                titlePrefix = "Transfer Files ",
                titleRed = "Instantly",
                subtitle = "Lightning-fast encrypted file sharing across all endpoints.",
                buttonText = "Explore Files  ➔",
                graphicEmoji = "📁⚡💻"
            ),
            HeroBannerPage(
                category = "UNATTENDED ACCESS",
                titlePrefix = "Seamless ",
                titleRed = "Remote Control",
                subtitle = "Connect to remote machines without manual confirmation.",
                buttonText = "Set Up Access  ➔",
                graphicEmoji = "🛡️🔒🖥️"
            )
        )
    }

    val recentDevices = remember(uiState.devices) {
        if (uiState.devices.isNotEmpty()) {
            uiState.devices.map { d ->
                RecentDeviceItem(
                    name = d.deviceName,
                    statusText = if (d.status.equals("online", ignoreCase = true)) "Online" else "Offline",
                    isOnline = d.status.equals("online", ignoreCase = true),
                    iconVector = if (d.deviceType?.contains("laptop", ignoreCase = true) == true) Icons.Outlined.Laptop else Icons.Outlined.Computer
                )
            }
        } else {
            listOf(
                RecentDeviceItem("Office PC", "Online", true, Icons.Outlined.Computer),
                RecentDeviceItem("Home Laptop", "Last seen 2 hours ago", false, Icons.Outlined.Laptop),
                RecentDeviceItem("Server", "Last seen 1 day ago", false, Icons.Outlined.Dns)
            )
        }
    }

    val recentFiles = remember {
        listOf(
            RecentFileItem("Project_Report.pdf", "2.4 MB • Today, 10:30 AM", "PDF", Color(0xFFFEE2E2), Color(0xFFDC2626)),
            RecentFileItem("Dashboard_Screenshot.png", "1.1 MB • Yesterday, 06:20 PM", "PNG", Color(0xFFDCFCE7), Color(0xFF16A34A)),
            RecentFileItem("Meeting_Notes.docx", "856 KB • 12 Oct 2024, 04:12 PM", "DOCX", Color(0xFFE0F2FE), Color(0xFF0284C7))
        )
    }

    // REAL OUTBOUND CONNECTION PROGRESS / AUTH DIALOG
    val outboundState = uiState.outboundState
    if (outboundState !is OutboundConnectionState.Idle) {
        AlertDialog(
            onDismissRequest = {
                if (outboundState is OutboundConnectionState.Failed) {
                    viewModel.dismissConnectionState()
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            when (outboundState) {
                                is OutboundConnectionState.Failed -> Color(0xFFFEE2E2)
                                is OutboundConnectionState.Connected -> Color(0xFFDCFCE7)
                                else -> Color(0xFFFFF0F0)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (outboundState) {
                            is OutboundConnectionState.Connected -> Icons.Outlined.CheckCircle
                            is OutboundConnectionState.Failed -> Icons.Outlined.Security
                            else -> Icons.Outlined.Computer
                        },
                        contentDescription = "Session Status",
                        tint = when (outboundState) {
                            is OutboundConnectionState.Connected -> Color(0xFF16A34A)
                            is OutboundConnectionState.Failed -> Color(0xFFDC2626)
                            else -> Color(0xFFE50914)
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = when (outboundState) {
                        is OutboundConnectionState.Requesting -> "Connecting to Remote PC"
                        is OutboundConnectionState.WaitingApproval -> "Waiting for Remote Approval"
                        is OutboundConnectionState.Connected -> "Connection Established!"
                        is OutboundConnectionState.Failed -> "Unable to Connect"
                        else -> "Remote Connection"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val targetId = when (outboundState) {
                        is OutboundConnectionState.Requesting -> outboundState.targetId
                        is OutboundConnectionState.WaitingApproval -> outboundState.targetId
                        is OutboundConnectionState.Connected -> outboundState.targetId
                        is OutboundConnectionState.Failed -> outboundState.targetId
                        else -> remoteDeviceCode
                    }

                    Text(
                        text = "Remote Device ID: $targetId",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE50914)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    when (outboundState) {
                        is OutboundConnectionState.Requesting -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = Color(0xFFE50914),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Locating device on DeskStream network and sending connection handshake...",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center
                            )
                        }
                        is OutboundConnectionState.WaitingApproval -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = Color(0xFFD97706),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "A session request prompt has appeared on the remote PC screen.\nPlease ask the computer user to click 'Accept'.",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Waiting... (${60 - outboundState.secondsElapsed}s)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        is OutboundConnectionState.Connected -> {
                            Text(
                                text = "⚡ Connection established! Launching live remote viewer...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF16A34A),
                                textAlign = TextAlign.Center
                            )
                        }
                        is OutboundConnectionState.Failed -> {
                            Text(
                                text = outboundState.message,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFFDC2626),
                                textAlign = TextAlign.Center
                            )
                        }
                        else -> {}
                    }
                }
            },
            confirmButton = {
                when (outboundState) {
                    is OutboundConnectionState.Failed -> {
                        Button(
                            onClick = { viewModel.dismissConnectionState() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914))
                        ) {
                            Text(
                                text = "Dismiss",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                    is OutboundConnectionState.WaitingApproval, is OutboundConnectionState.Requesting -> {
                        Button(
                            onClick = { viewModel.cancelOutgoingConnection() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B))
                        ) {
                            Text(
                                text = "Cancel Request",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                    else -> {}
                }
            }
        )
    }

    // REAL INBOUND CONNECTION PERMISSION AUTHORIZATION DIALOG
    val incomingReq = uiState.incomingRequest
    if (incomingReq != null) {
        AlertDialog(
            onDismissRequest = {
                viewModel.rejectIncomingConnection(persistentDeviceId)
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF2F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = "Incoming Request",
                        tint = Color(0xFFE50914),
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Incoming Connection Request",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "A remote user wants to connect to your phone:",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Requester: ${incomingReq.requesterName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Remote ID: ${incomingReq.requesterSystemId}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE50914)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Requested Permissions:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(text = "• View Screen & Remote Desktop", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text(text = "• Remote Control & Touch Input", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text(text = "• File Transfer Access", fontSize = 11.sp, color = Color(0xFF64748B))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.acceptIncomingConnection(persistentDeviceId)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Text(text = "Accept & Share", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.rejectIncomingConnection(persistentDeviceId)
                    }
                ) {
                    Text(text = "Reject", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                modifier = Modifier.width(280.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Drawer Header with Logo & Brand Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.desk_stream_d),
                            contentDescription = "Logo",
                            modifier = Modifier.size(36.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Desk",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF0F172A),
                                    lineHeight = 18.sp
                                )
                                Text(
                                    text = "Stream",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFFE50914),
                                    lineHeight = 18.sp
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

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(16.dp))

                    // User Profile Card in Side Menu
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFAFAFC))
                            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(14.dp))
                            .clickable {
                                scope.launch { drawerState.close() }
                                onProfileClick()
                            }
                            .padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF60A5FA)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (uiState.currentUser?.username?.take(2) ?: "US").uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = uiState.currentUser?.username ?: "User Name",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = uiState.currentUser?.email ?: "user@deskstream.com",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "ID: $persistentDeviceId",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE50914)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Side Navigation Items with Outlined Icons
                    DrawerMenuItem(Icons.Outlined.Home, "Home") {
                        scope.launch { drawerState.close() }
                    }
                    DrawerMenuItem(Icons.Outlined.Devices, "Devices") {
                        scope.launch { drawerState.close() }
                        onDevicesTabClick()
                    }
                    DrawerMenuItem(Icons.Outlined.Folder, "Files") {
                        scope.launch { drawerState.close() }
                        onFilesTabClick()
                    }
                    DrawerMenuItem(Icons.Outlined.Group, "Shared") {
                        scope.launch { drawerState.close() }
                        onSharedClick()
                    }
                    DrawerMenuItem(Icons.Outlined.Person, "Profile") {
                        scope.launch { drawerState.close() }
                        onProfileClick()
                    }
                    DrawerMenuItem(Icons.Outlined.Settings, "Settings") {
                        scope.launch { drawerState.close() }
                        onSettingsTabClick()
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(12.dp))

                    DrawerMenuItem(
                        iconVector = Icons.AutoMirrored.Outlined.ExitToApp,
                        label = "Logout",
                        textColor = Color(0xFFE50914),
                        iconTint = Color(0xFFE50914)
                    ) {
                        scope.launch { drawerState.close() }
                        onLogoutClick()
                    }
                }
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                // Bottom Navigation Bar with 4 Tabs: Home, Devices, Files, Profile
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                // 1. Top Navigation Bar (Hamburger Menu + Logo with Tagline + Bell & Avatar)
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
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Menu,
                                contentDescription = "Open Drawer",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(26.dp)
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
                                if (hasUnreadNotifications || hasUnreadFromApi) {
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
                                text = (uiState.currentUser?.username?.take(2) ?: "US").uppercase(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. "Your Device ID" Card Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFFF0F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Key,
                                    contentDescription = "Device ID Icon",
                                    tint = Color(0xFFE50914),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "THIS PHONE'S VIEWER ID",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF64748B),
                                        letterSpacing = 0.8.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Online",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = persistentDeviceId,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F172A),
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Copy ID Button - Compact Pill with Icon Beside Text
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isCopiedToastShown) Color(0xFFDCFCE7) else Color(0xFFF1F5F9))
                                .border(
                                    1.dp,
                                    if (isCopiedToastShown) Color(0xFFBBF7D0) else Color(0xFFE2E8F0),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(persistentDeviceId.replace(" ", "")))
                                    isCopiedToastShown = true
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy ID",
                                tint = if (isCopiedToastShown) Color(0xFF16A34A) else Color(0xFF0F172A),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isCopiedToastShown) "Copied!" else "Copy",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCopiedToastShown) Color(0xFF16A34A) else Color(0xFF0F172A),
                                maxLines = 1
                            )
                        }
                    }
                }

                if (uiState.incomingSessionMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.dismissIncomingMsg() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🔔 ${uiState.incomingSessionMsg}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF16A34A),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "✕",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Enter Code Form Card for Device Connection (Outbound Session)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFFF0F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Computer,
                                    contentDescription = "Remote Connect",
                                    tint = Color(0xFFE50914),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "REMOTE CONNECTION",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B),
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "Connect to Remote PC / Laptop",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = remoteDeviceCode,
                                onValueChange = {
                                    remoteDeviceCode = it
                                    if (connectionErrorMsg != null) connectionErrorMsg = null
                                    if (connectionSuccessMsg != null) connectionSuccessMsg = null
                                },
                                placeholder = {
                                    Text(
                                        text = "Enter PC / Laptop ID (9 digits)",
                                        fontSize = 13.sp,
                                        color = Color(0xFF94A3B8),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF0F172A)
                                ),
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Key,
                                        contentDescription = "Code Icon",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC),
                                    focusedBorderColor = Color(0xFFE50914),
                                    unfocusedBorderColor = Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            )

                            Button(
                                onClick = {
                                    val codeTrimmed = remoteDeviceCode.trim().replace(" ", "").replace("-", "")
                                    if (codeTrimmed.isBlank()) {
                                        connectionErrorMsg = "Please enter a device code."
                                        connectionSuccessMsg = null
                                    } else if (codeTrimmed.length < 6) {
                                        connectionErrorMsg = "Invalid code. Minimum 6 digits required."
                                        connectionSuccessMsg = null
                                    } else {
                                        connectionErrorMsg = null
                                        connectionSuccessMsg = null
                                        viewModel.connectToDevice(codeTrimmed, persistentDeviceId)
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE50914),
                                    contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Connect",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "➔",
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Feedback message
                        if (connectionErrorMsg != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "⚠️ $connectionErrorMsg",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFDC2626)
                            )
                        } else if (outboundState is OutboundConnectionState.Failed) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "⚠️ ${outboundState.message}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFDC2626)
                            )
                        } else if (connectionSuccessMsg != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "✅ $connectionSuccessMsg",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF16A34A)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Smaller Compact "Work from Anywhere" Hero Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(18.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                heroPageIndex = (heroPageIndex + 1) % heroPages.size
                            }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.work_from_anywhere_part_bg),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )

                        AnimatedContent(
                            targetState = heroPageIndex,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "HeroCarouselPageCompact"
                        ) { pageIdx ->
                            val page = heroPages[pageIdx]
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1.2f),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = page.category,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF64748B),
                                        letterSpacing = 0.8.sp
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(
                                                SpanStyle(
                                                    color = Color(0xFF0F172A),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                )
                                            ) {
                                                append(page.titlePrefix)
                                            }
                                            withStyle(
                                                SpanStyle(
                                                    color = Color(0xFFE50914),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                )
                                            ) {
                                                append(page.titleRed)
                                            }
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = page.subtitle,
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .weight(0.8f)
                                        .height(80.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (page.graphicEmoji == "💻") {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_laptop_hero),
                                            contentDescription = "Laptop",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Text(
                                            text = page.graphicEmoji,
                                            fontSize = 32.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 5. Quick Action Grid (4 Action Cards: Devices, Files, Shared, Settings)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionCard(
                        title = "Devices",
                        subtitle = "Manage devices",
                        iconVector = Icons.Outlined.Devices,
                        iconTint = Color(0xFFE50914),
                        bgColor = Color(0xFFFFF0F0),
                        modifier = Modifier.weight(1f)
                    ) {
                        onDevicesTabClick()
                    }
                    QuickActionCard(
                        title = "Files",
                        subtitle = "View & share",
                        iconVector = Icons.Outlined.Folder,
                        iconTint = Color(0xFF0284C7),
                        bgColor = Color(0xFFE0F2FE),
                        modifier = Modifier.weight(1f)
                    ) {
                        onFilesTabClick()
                    }
                    QuickActionCard(
                        title = "Shared",
                        subtitle = "Team access",
                        iconVector = Icons.Outlined.Group,
                        iconTint = Color(0xFF16A34A),
                        bgColor = Color(0xFFDCFCE7),
                        modifier = Modifier.weight(1f)
                    ) {
                        onSharedClick()
                    }
                    QuickActionCard(
                        title = "Settings",
                        subtitle = "Preferences",
                        iconVector = Icons.Outlined.Settings,
                        iconTint = Color(0xFF9333EA),
                        bgColor = Color(0xFFF3E8FF),
                        modifier = Modifier.weight(1f)
                    ) {
                        onSettingsTabClick()
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 6. "Recent Devices" Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Devices",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "View All  ➔",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE50914),
                        modifier = Modifier.clickable { onDevicesTabClick() }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        recentDevices.forEachIndexed { index, device ->
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
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFF8FAFC)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = device.iconVector,
                                            contentDescription = device.name,
                                            tint = Color(0xFF475569),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = device.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (device.isOnline) Color(0xFF10B981) else Color(0xFF94A3B8)
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = device.statusText,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (device.isOnline) Color(0xFF10B981) else Color(0xFF64748B)
                                            )
                                        }
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

                            if (index < recentDevices.size - 1) {
                                HorizontalDivider(
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.padding(horizontal = 14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 7. "Recent Files" Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Files",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "View All  ➔",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE50914),
                        modifier = Modifier.clickable { onFilesTabClick() }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        recentFiles.forEachIndexed { index, file ->
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
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(file.badgeBgColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = file.badgeLabel,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = file.badgeTextColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = file.fileName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = file.fileDetails,
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

                            if (index < recentFiles.size - 1) {
                                HorizontalDivider(
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.padding(horizontal = 14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 8. "Storage Overview" Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFE0F2FE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Storage,
                                        contentDescription = "Storage",
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "Storage Overview",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "12.5 GB used of 50 GB",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Text(
                                text = "25%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = { 0.25f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF0284C7),
                            trackColor = Color(0xFFE2E8F0)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun DrawerMenuItem(
    iconVector: ImageVector,
    label: String,
    textColor: Color = Color(0xFF0F172A),
    iconTint: Color = Color(0xFF64748B),
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    iconVector: ImageVector,
    iconTint: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun NavItem(
    label: String,
    iconVector: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color(0xFFFFF0F0) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = label,
                tint = if (isSelected) Color(0xFFE50914) else Color(0xFF64748B),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFFE50914) else Color(0xFF64748B)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    DeskStreamTheme {
        HomeScreen()
    }
}
