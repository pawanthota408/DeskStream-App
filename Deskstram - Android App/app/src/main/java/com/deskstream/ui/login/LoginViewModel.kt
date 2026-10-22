package com.deskstream.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deskstream.data.local.SessionManager
import com.deskstream.data.remote.ApiClient
import com.deskstream.data.repository.ApiResult
import com.deskstream.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SystemHealthState(
    val isChecking: Boolean = false,
    val serverOnline: Boolean? = null,
    val serverLatencyMs: Long = 0L,
    val dbConnected: Boolean? = null,
    val serverUrl: String = ApiClient.PRODUCTION_BASE_URL,
    val lastCheckedTime: String = "",
    val statusMessage: String = "Checking system connectivity..."
)

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccess: Boolean = false,
    val userName: String = "",
    val userEmail: String = "",
    val healthState: SystemHealthState = SystemHealthState(),
    val debugDetails: String? = null
)

/**
 * ViewModel for the LoginScreen.
 * Calls AuthRepository.login() and updates [uiState] accordingly.
 * Actively monitors and diagnostics Server & Database connectivity for real-time debugging.
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val authRepository = AuthRepository()
    private val sessionManager = SessionManager.getInstance(application)

    init {
        checkSystemHealth()
    }

    /**
     * Probes the live backend server and MySQL database connection,
     * recording latency and status codes for in-app diagnostic display.
     */
    fun checkSystemHealth() {
        _uiState.value = _uiState.value.copy(
            healthState = _uiState.value.healthState.copy(isChecking = true)
        )

        viewModelScope.launch {
            val health = authRepository.checkHealth()
            val timeStr = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
            _uiState.value = _uiState.value.copy(
                healthState = SystemHealthState(
                    isChecking = false,
                    serverOnline = health.isServerReachable,
                    serverLatencyMs = health.latencyMs,
                    dbConnected = health.isDatabaseConnected,
                    serverUrl = health.endpointUrl,
                    lastCheckedTime = timeStr,
                    statusMessage = health.message
                )
            )
        }
    }

    fun login(email: String, password: String) {
        if (_uiState.value.isLoading) return

        val cleanEmail = email.trim()
        val cleanPassword = password

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
            debugDetails = null
        )

        viewModelScope.launch {
            when (val result = authRepository.login(cleanEmail, cleanPassword)) {
                is ApiResult.Success -> {
                    val user = result.data.user
                    val cookie = ApiClient.sessionCookieHeader
                    if (user != null) {
                        sessionManager.saveLoginSession(user, cookieHeader = cookie)
                    }
                    sessionManager.saveCredentials(cleanEmail, cleanPassword)
                    ApiClient.syncCookiesToWebView()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccess = true,
                        userName = user?.username ?: "",
                        userEmail = user?.email ?: cleanEmail,
                        errorMessage = null,
                        debugDetails = null
                    )
                }
                is ApiResult.Error -> {
                    val currentHealth = _uiState.value.healthState
                    val debugMsg = buildString {
                        append("• HTTP Status: ${if (result.code > 0) result.code else "Network Error"}\n")
                        append("• Endpoint: ${ApiClient.PRODUCTION_BASE_URL}auth/login.php\n")
                        append("• Input Tested: '$cleanEmail'\n")
                        append("• Server Status: ${if (currentHealth.serverOnline == true) "Online (${currentHealth.serverLatencyMs}ms)" else "Unreachable"}\n")
                        append("• Database Status: ${if (currentHealth.dbConnected == true) "Connected" else "Error / Offline"}")
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        debugDetails = debugMsg
                    )
                }
                is ApiResult.Loading -> { }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null, debugDetails = null)
    }
}
