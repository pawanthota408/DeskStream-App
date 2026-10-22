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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deskstream.ui.theme.DeskStreamTheme

import androidx.compose.ui.platform.LocalContext

data class DeviceModel(
    val name: String,
    val isOnline: Boolean,
    val osDetails: String,
    val iconVector: ImageVector,
    val systemId: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    viewModel: HomeViewModel? = null,
    onBackClick: () -> Unit = {},
    onHomeTabClick: () -> Unit = {},
    onFilesTabClick: () -> Unit = {},
    onProfileTabClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val persistentDeviceId = remember { getPersistentDeviceId(context) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedTab by remember { mutableStateOf("Devices") }

    val uiState = viewModel?.uiState?.collectAsState()?.value
    val allDevices = remember(uiState?.devices) {
        val remote = uiState?.devices
        if (!remote.isNullOrEmpty()) {
            remote.map { dev ->
                DeviceModel(
                    name = dev.deviceName,
                    isOnline = dev.status.equals("online", ignoreCase = true),
                    osDetails = "${dev.os ?: "Remote"} • ${dev.systemId ?: dev.ipAddress ?: dev.uniqueDeviceId ?: "Active"}",
                    iconVector = if (dev.deviceType?.contains("laptop", ignoreCase = true) == true) Icons.Outlined.Laptop else Icons.Outlined.Computer,
                    systemId = dev.systemId ?: dev.uniqueDeviceId
                )
            }
        } else {
            listOf(
                DeviceModel("Office PC", true, "Windows 11 • 192.168.1.10", Icons.Outlined.Computer, "984271305"),
                DeviceModel("Home Laptop", false, "Windows 10 • 192.168.1.25", Icons.Outlined.Laptop, "562143981"),
                DeviceModel("Server", false, "Windows Server • 192.168.1.50", Icons.Outlined.Dns, "771829304"),
                DeviceModel("Mac Mini", true, "macOS Sonoma • 192.168.1.30", Icons.Outlined.Computer, "339102847")
            )
        }
    }

    val filteredDevices = remember(selectedFilter, searchQuery) {
        allDevices.filter { device ->
            val matchesFilter = when (selectedFilter) {
                "Online" -> device.isOnline
                "Offline" -> !device.isOnline
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                    device.name.contains(searchQuery, ignoreCase = true) ||
                    device.osDetails.contains(searchQuery, ignoreCase = true)

            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                title = {
                    Text(
                        text = "My Devices",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onBackClick() },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE50914))
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add Device",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        },
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
            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search devices by name or IP...") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE50914),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Chips (All, Online, Offline)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChipItem("All (${allDevices.size})", selectedFilter == "All") {
                    selectedFilter = "All"
                }
                FilterChipItem("Online (${allDevices.count { it.isOnline }})", selectedFilter == "Online") {
                    selectedFilter = "Online"
                }
                FilterChipItem("Offline (${allDevices.count { !it.isOnline }})", selectedFilter == "Offline") {
                    selectedFilter = "Offline"
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Devices List
            filteredDevices.forEach { device ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF8FAFC)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = device.iconVector,
                                    contentDescription = device.name,
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(24.dp)
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
                                        text = if (device.isOnline) "Online" else "Offline",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (device.isOnline) Color(0xFF10B981) else Color(0xFF94A3B8)
                                    )
                                }

                                Text(
                                    text = device.osDetails,
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "Options",
                                tint = Color(0xFF64748B),
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { }
                            )

                            Button(
                                onClick = {
                                    if (!device.systemId.isNullOrBlank()) {
                                        viewModel?.connectToDevice(device.systemId, persistentDeviceId)
                                        onBackClick()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (device.isOnline) Color(0xFFE50914) else Color(0xFFFFEBEB),
                                    contentColor = if (device.isOnline) Color.White else Color(0xFFE50914)
                                ),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(
                                    text = "Connect",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color(0xFFE50914) else Color.White)
            .border(
                1.dp,
                if (isSelected) Color(0xFFE50914) else Color(0xFFE2E8F0),
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color(0xFF64748B)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DevicesScreenPreview() {
    DeskStreamTheme {
        DevicesScreen()
    }
}
