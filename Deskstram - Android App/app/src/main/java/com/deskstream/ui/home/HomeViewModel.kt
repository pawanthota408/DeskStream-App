package com.deskstream.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deskstream.data.local.SessionManager
import com.deskstream.data.model.DeviceDto
import com.deskstream.data.model.IncomingConnectionRequest
import com.deskstream.data.model.NotificationDto
import com.deskstream.data.model.SessionDto
import com.deskstream.data.model.UserDto
import com.deskstream.data.repository.ApiResult
import com.deskstream.data.repository.AuthRepository
import com.deskstream.data.repository.DevicesRepository
import com.deskstream.data.repository.NotificationsRepository
import com.deskstream.data.repository.SessionsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.deskstream.data.repository.ConnectionsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class OutboundConnectionState {
    object Idle : OutboundConnectionState()
    data class Requesting(val targetId: String) : OutboundConnectionState()
    data class WaitingApproval(val targetId: String, val token: String, val secondsElapsed: Int = 0) : OutboundConnectionState()
    data class Connected(val targetId: String, val token: String, val sessionUrl: String) : OutboundConnectionState()
    data class Failed(val targetId: String, val message: String) : OutboundConnectionState()
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val currentUser: UserDto? = null,
    val devices: List<DeviceDto> = emptyList(),
    val devicesError: String? = null,
    val activeSessions: List<SessionDto> = emptyList(),
    val sessionHistory: List<SessionDto> = emptyList(),
    val notifications: List<NotificationDto> = emptyList(),
    val unreadNotificationCount: Int = 0,
    val isLoggedOut: Boolean = false,
    val errorMessage: String? = null,
    val outboundState: OutboundConnectionState = OutboundConnectionState.Idle,
    val incomingRequest: IncomingConnectionRequest? = null,
    val incomingSessionMsg: String? = null,
    val isDeviceOnline: Boolean = false
)

/**
 * Shared ViewModel for HomeScreen and related screens (Devices, Sessions, Notifications).
 * Loads data lazily from the respective repositories and exposes it via StateFlow.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val authRepository = AuthRepository()
    private val devicesRepository = DevicesRepository()
    private val sessionsRepository = SessionsRepository()
    private val notificationsRepository = NotificationsRepository()
    private val connectionsRepository = ConnectionsRepository()
    private val sessionManager = SessionManager.getInstance(application)

    private var connectionPollJob: Job? = null

    init {
        // Load cached user immediately
        _uiState.value = _uiState.value.copy(
            currentUser = sessionManager.currentUser
        )
        // Refresh server session in background if credentials are saved
        val email = sessionManager.savedEmail ?: sessionManager.userEmail
        val pass = sessionManager.savedPassword
        if (!email.isNullOrBlank() && !pass.isNullOrBlank()) {
            viewModelScope.launch {
                authRepository.login(email, pass)
                com.deskstream.data.remote.ApiClient.syncCookiesToWebView("https://friendssoftwaresolutions.in")
            }
        }
        // Fetch live data on init
        loadAllData()
    }

    // ─── Data Loading ─────────────────────────────────────────────────────────

    fun loadAllData() {
        loadDevices()
        loadActiveSessions()
        loadNotifications()
    }

    fun loadDevices() {
        viewModelScope.launch {
            when (val result = devicesRepository.listDevices()) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        devices = result.data.devices,
                        devicesError = null
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        devicesError = result.message
                    )
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun loadActiveSessions() {
        viewModelScope.launch {
            when (val result = sessionsRepository.getActiveSessions()) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        activeSessions = result.data.sessions
                    )
                }
                is ApiResult.Error -> { /* silently fail for background polling */ }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun loadSessionHistory() {
        viewModelScope.launch {
            when (val result = sessionsRepository.getSessionHistory()) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        sessionHistory = result.data.sessions
                    )
                }
                is ApiResult.Error -> { /* silently fail */ }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            when (val result = notificationsRepository.getNotifications()) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        notifications = result.data.notifications,
                        unreadNotificationCount = result.data.unreadCount
                    )
                }
                is ApiResult.Error -> { /* silently fail */ }
                is ApiResult.Loading -> {}
            }
        }
    }

    // ─── Actions ──────────────────────────────────────────────────────────────

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            notificationsRepository.markAllRead()
            _uiState.value = _uiState.value.copy(
                unreadNotificationCount = 0,
                notifications = _uiState.value.notifications.map { it.copy(isRead = 1) }
            )
        }
    }

    fun endSession(sessionKey: String) {
        viewModelScope.launch {
            sessionsRepository.endSession(sessionKey)
            // Refresh active sessions list
            loadActiveSessions()
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            sessionManager.clearSession()
            _uiState.value = _uiState.value.copy(isLoggedOut = true)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // ─── Remote Connection Workflow ──────────────────────────────────────────

    /**
     * Initiates a real connection request to a remote device by system ID.
     * Handles 404 (device not found), 409 (offline), 403 (limit reached),
     * and polls connections/status.php until approved, rejected, or expired.
     */
    fun connectToDevice(targetId: String, mySystemId: String) {
        val cleanTarget = targetId.replace("[^0-9]".toRegex(), "")
        val cleanMyId = mySystemId.replace("[^0-9]".toRegex(), "").ifBlank {
            sessionManager.uniqueUserId.ifBlank { "000000000" }
        }

        if (cleanTarget.isBlank()) {
            _uiState.value = _uiState.value.copy(
                outboundState = OutboundConnectionState.Failed(cleanTarget, "Please enter a valid remote device code.")
            )
            return
        }

        if (cleanTarget.length < 6) {
            _uiState.value = _uiState.value.copy(
                outboundState = OutboundConnectionState.Failed(cleanTarget, "Invalid device ID. DeskStream device IDs are 9 digits.")
            )
            return
        }

        if (cleanTarget == cleanMyId) {
            _uiState.value = _uiState.value.copy(
                outboundState = OutboundConnectionState.Failed(cleanTarget, "Cannot connect to your own device ID.")
            )
            return
        }

        connectionPollJob?.cancel()
        _uiState.value = _uiState.value.copy(
            outboundState = OutboundConnectionState.Requesting(cleanTarget)
        )

        connectionPollJob = viewModelScope.launch {
            // Re-authenticate if credentials exist to guarantee fresh session
            val email = sessionManager.savedEmail ?: sessionManager.userEmail
            val pass = sessionManager.savedPassword
            if (!email.isNullOrBlank() && !pass.isNullOrBlank()) {
                authRepository.login(email, pass)
                com.deskstream.data.remote.ApiClient.syncCookiesToWebView("https://friendssoftwaresolutions.in")
            }

            val myName = sessionManager.userName.ifBlank { "Android Mobile" }
            when (val result = connectionsRepository.requestConnection(cleanTarget, cleanMyId, myName)) {
                is ApiResult.Success -> {
                    val reqData = result.data.request
                    val token = reqData?.requestToken ?: ""
                    val initialStatus = reqData?.status ?: "pending"
                    val targetName = reqData?.targetName ?: ""

                    val isMobile = targetName.contains("Mobile", ignoreCase = true) ||
                                   targetName.contains("Android", ignoreCase = true) ||
                                   targetName.contains("Vivo", ignoreCase = true)

                    if (isMobile) {
                        _uiState.value = _uiState.value.copy(
                            outboundState = OutboundConnectionState.Failed(
                                cleanTarget,
                                "Target device '$targetName' ($cleanTarget) is a mobile phone. DeskStream Android app is designed to view and control PC / Laptop screens. To view your computer screen, please enter your PC / Laptop's DeskStream ID (e.g. 375 768 952)."
                            )
                        )
                        return@launch
                    }

                    if (token.isBlank()) {
                        _uiState.value = _uiState.value.copy(
                            outboundState = OutboundConnectionState.Failed(cleanTarget, "Server did not return a session token.")
                        )
                        return@launch
                    }

                    if (initialStatus == "accepted") {
                        // Immediate accept (e.g. auto-approve or local loopback)
                        val sessionUrl = "https://friendssoftwaresolutions.in/DeskStream/remote/session.php?id=$cleanTarget&requester_system_id=$cleanMyId&token=$token&mode=control"
                        _uiState.value = _uiState.value.copy(
                            outboundState = OutboundConnectionState.Connected(cleanTarget, token, sessionUrl)
                        )
                    } else {
                        // Start polling status
                        _uiState.value = _uiState.value.copy(
                            outboundState = OutboundConnectionState.WaitingApproval(cleanTarget, token, 0)
                        )
                        pollConnectionStatus(cleanTarget, cleanMyId, token)
                    }
                }
                is ApiResult.Error -> {
                    val msg = when (result.code) {
                        400 -> result.message
                        404 -> "Device ID '$cleanTarget' not found on DeskStream network. Please check the ID or verify DeskStream Desktop Agent is installed and running on that computer."
                        409 -> "Remote device '$cleanTarget' is currently offline. Please ensure the computer is powered on and connected to the internet."
                        403 -> "Account limit reached. Free Plan allows 1 active session. Please close existing sessions."
                        else -> result.message
                    }
                    _uiState.value = _uiState.value.copy(
                        outboundState = OutboundConnectionState.Failed(cleanTarget, msg)
                    )
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    private suspend fun pollConnectionStatus(targetId: String, myId: String, token: String) {
        var elapsed = 0
        val maxWaitSeconds = 60
        while (elapsed < maxWaitSeconds) {
            delay(1500)
            elapsed += 2
            when (val statusRes = connectionsRepository.checkStatus(token)) {
                is ApiResult.Success -> {
                    val reqStatus = statusRes.data.requestStatus.lowercase()
                    when (reqStatus) {
                        "accepted" -> {
                            val sessionUrl = "https://friendssoftwaresolutions.in/DeskStream/remote/session.php?id=$targetId&requester_system_id=$myId&token=$token&mode=control"
                            _uiState.value = _uiState.value.copy(
                                outboundState = OutboundConnectionState.Connected(targetId, token, sessionUrl)
                            )
                            return
                        }
                        "rejected" -> {
                            _uiState.value = _uiState.value.copy(
                                outboundState = OutboundConnectionState.Failed(targetId, "Connection request was rejected by the remote computer.")
                            )
                            return
                        }
                        "expired", "cancelled" -> {
                            _uiState.value = _uiState.value.copy(
                                outboundState = OutboundConnectionState.Failed(targetId, "Connection request expired (no response from remote computer).")
                            )
                            return
                        }
                        else -> {
                            _uiState.value = _uiState.value.copy(
                                outboundState = OutboundConnectionState.WaitingApproval(targetId, token, elapsed)
                            )
                        }
                    }
                }
                is ApiResult.Error -> {
                    // Transient error, continue polling
                }
                is ApiResult.Loading -> {}
            }
        }
        _uiState.value = _uiState.value.copy(
            outboundState = OutboundConnectionState.Failed(targetId, "Connection request timed out. Please try again.")
        )
    }

    fun cancelOutgoingConnection() {
        val current = _uiState.value.outboundState
        if (current is OutboundConnectionState.WaitingApproval) {
            viewModelScope.launch {
                connectionsRepository.cancelConnection(current.token)
            }
        }
        connectionPollJob?.cancel()
        _uiState.value = _uiState.value.copy(outboundState = OutboundConnectionState.Idle)
    }

    fun dismissConnectionState() {
        connectionPollJob?.cancel()
        _uiState.value = _uiState.value.copy(outboundState = OutboundConnectionState.Idle)
    }

    // ─── Mobile Device Registration & Inbound Request Handling ───────────────

    private var hostListenerJob: Job? = null

    /**
     * Registers this mobile phone on the DeskStream network as an active device
     * and maintains periodic heartbeats and incoming connection polling.
     */
    fun startDeviceHeartbeatAndListener(systemId: String, deviceName: String) {
        val cleanId = systemId.replace(" ", "").replace("-", "")
        if (cleanId.isBlank()) return
        if (hostListenerJob?.isActive == true) return

        hostListenerJob = viewModelScope.launch {
            // 1. Register device with backend
            val userId = sessionManager.uniqueUserId.toIntOrNull()
            when (connectionsRepository.registerDevice(cleanId, deviceName, "android", userId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(isDeviceOnline = true)
                }
                else -> {}
            }

            // 2. Continuous loop: Heartbeat + Incoming Request Check
            while (true) {
                // Heartbeat to keep device "Online" in DB
                connectionsRepository.sendHeartbeat(cleanId)

                // Check for incoming connection requests
                when (val incRes = connectionsRepository.checkIncomingConnection(cleanId)) {
                    is ApiResult.Success -> {
                        if (incRes.data.hasRequest && incRes.data.request != null) {
                            _uiState.value = _uiState.value.copy(incomingRequest = incRes.data.request)
                        } else if (!incRes.data.hasRequest && _uiState.value.incomingRequest != null) {
                            _uiState.value = _uiState.value.copy(incomingRequest = null)
                        }
                    }
                    else -> {}
                }

                delay(6000)
            }
        }
    }

    fun acceptIncomingConnection(mySystemId: String) {
        val req = _uiState.value.incomingRequest ?: return
        val cleanId = mySystemId.replace(" ", "").replace("-", "")
        viewModelScope.launch {
            when (val res = connectionsRepository.acceptConnection(req.requestToken, cleanId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        incomingRequest = null,
                        incomingSessionMsg = "Session connected with ${req.requesterName}"
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        incomingRequest = null,
                        incomingSessionMsg = "Failed to accept connection: ${res.message}"
                    )
                }
                else -> {}
            }
        }
    }

    fun rejectIncomingConnection(mySystemId: String) {
        val req = _uiState.value.incomingRequest ?: return
        val cleanId = mySystemId.replace(" ", "").replace("-", "")
        viewModelScope.launch {
            connectionsRepository.rejectConnection(req.requestToken, cleanId)
            _uiState.value = _uiState.value.copy(
                incomingRequest = null,
                incomingSessionMsg = "Rejected incoming connection from ${req.requesterName}"
            )
        }
    }

    fun dismissIncomingMsg() {
        _uiState.value = _uiState.value.copy(incomingSessionMsg = null)
    }

    // ─── Convenience Getters ─────────────────────────────────────────────────

    val userName: String get() = sessionManager.userName.ifBlank { "User" }
    val userEmail: String get() = sessionManager.userEmail
    val uniqueUserId: String get() = sessionManager.uniqueUserId
}
