package com.deskstream.data.repository

import com.deskstream.data.model.*
import com.deskstream.data.remote.ApiClient
import com.deskstream.data.remote.DeskStreamApiService

/**
 * Sealed class representing the result of an API operation.
 * Wraps both success data and error messages in a type-safe container.
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int = -1) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

/**
 * AuthRepository handles all authentication-related API calls.
 *
 * Design: all functions are suspend functions (for use with Kotlin Coroutines
 * from a ViewModel) and return [ApiResult] — never throw exceptions to callers.
 */
class AuthRepository(
    private val api: DeskStreamApiService = ApiClient.apiService
) {

    /** Login with email and password. */
    suspend fun login(email: String, password: String): ApiResult<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(email = email, password = password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && (body.success || body.status == "success")) {
                    ApiResult.Success(body)
                } else {
                    ApiResult.Error(body?.message ?: "Login failed. Please try again.")
                }
            } else {
                val errMsg = parseErrorBody(response.errorBody()?.string(), response.code())
                ApiResult.Error(errMsg, response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(networkErrorMessage(e))
        }
    }

    /** Register a new account. */
    suspend fun register(username: String, email: String, password: String): ApiResult<RegisterResponse> {
        return try {
            val response = api.register(RegisterRequest(username = username, email = email, password = password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == "success") {
                    ApiResult.Success(body)
                } else {
                    ApiResult.Error(body?.message ?: "Registration failed.")
                }
            } else {
                ApiResult.Error(parseErrorBody(response.errorBody()?.string()), response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(networkErrorMessage(e))
        }
    }

    /** Logout — clears server session and local cookies. */
    suspend fun logout(): ApiResult<GenericResponse> {
        return try {
            val response = api.logout()
            ApiClient.clearSession()
            if (response.isSuccessful) {
                ApiResult.Success(response.body() ?: GenericResponse("success", "Logged out"))
            } else {
                // Still clear local session even if server call fails
                ApiResult.Success(GenericResponse("success", "Logged out locally"))
            }
        } catch (e: Exception) {
            // Ensure local session is cleared even on network error
            ApiClient.clearSession()
            ApiResult.Success(GenericResponse("success", "Logged out"))
        }
    }

    /** Fetch current user profile and subscription details. */
    suspend fun getProfile(): ApiResult<UserProfileResponse> {
        return try {
            val response = api.getProfile()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Could not load profile.")
            } else {
                ApiResult.Error(parseErrorBody(response.errorBody()?.string()), response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(networkErrorMessage(e))
        }
    }

    data class HealthStatus(
        val isServerReachable: Boolean,
        val isDatabaseConnected: Boolean,
        val latencyMs: Long,
        val message: String,
        val endpointUrl: String = ApiClient.PRODUCTION_BASE_URL
    )

    /**
     * Check real-time server and database connectivity.
     */
    suspend fun checkHealth(): HealthStatus {
        val startTime = System.currentTimeMillis()
        var serverReachable = false
        var dbConnected = false
        var latency = 0L
        var msg = ""

        try {
            val serverResp = api.pingServer()
            latency = (System.currentTimeMillis() - startTime).coerceAtLeast(1L)
            serverReachable = serverResp.isSuccessful || serverResp.code() in 200..499

            val dbResp = api.probeDatabaseHealth(mapOf("system_id" to "000000000"))
            // HTTP 200 or 404 confirms PHP connected to MySQL and executed the query.
            // HTTP 500 confirms DB connection failure in config/database.php.
            dbConnected = dbResp.code() != 500
            msg = if (serverReachable && dbConnected) {
                "Server and Database are Online (${latency}ms)"
            } else if (serverReachable) {
                "Server is Online, but Database returned error (${dbResp.code()})"
            } else {
                "Server is Unreachable"
            }
        } catch (e: Exception) {
            latency = (System.currentTimeMillis() - startTime).coerceAtLeast(1L)
            msg = "Connection check failed: ${e.localizedMessage ?: "Network error"}"
        }

        return HealthStatus(
            isServerReachable = serverReachable,
            isDatabaseConnected = dbConnected,
            latencyMs = latency,
            message = msg
        )
    }
}

/**
 * DevicesRepository handles device listing and heartbeat operations.
 */
class DevicesRepository(
    private val api: DeskStreamApiService = ApiClient.apiService
) {

    suspend fun listDevices(): ApiResult<DeviceListResponse> {
        return try {
            val response = api.listDevices()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("No device data received.")
            } else {
                ApiResult.Error(parseErrorBody(response.errorBody()?.string()), response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(networkErrorMessage(e))
        }
    }
}

/**
 * SessionsRepository handles remote streaming session operations.
 */
class SessionsRepository(
    private val api: DeskStreamApiService = ApiClient.apiService
) {

    suspend fun getActiveSessions(): ApiResult<ActiveSessionsResponse> {
        return try {
            val response = api.getActiveSessions()
            if (response.isSuccessful) {
                ApiResult.Success(response.body() ?: ActiveSessionsResponse("success", emptyList()))
            } else {
                ApiResult.Error(parseErrorBody(response.errorBody()?.string()), response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(networkErrorMessage(e))
        }
    }

    suspend fun getSessionHistory(page: Int = 1, limit: Int = 20): ApiResult<SessionHistoryResponse> {
        return try {
            val response = api.getSessionHistory(page, limit)
            if (response.isSuccessful) {
                ApiResult.Success(response.body() ?: SessionHistoryResponse("success", emptyList(), 0))
            } else {
                ApiResult.Error(parseErrorBody(response.errorBody()?.string()), response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(networkErrorMessage(e))
        }
    }

    suspend fun endSession(sessionKey: String): ApiResult<GenericResponse> {
        return try {
            val response = api.endSession(sessionKey)
            if (response.isSuccessful) {
                ApiResult.Success(response.body() ?: GenericResponse("success", "Session ended"))
            } else {
                ApiResult.Error(parseErrorBody(response.errorBody()?.string()), response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(networkErrorMessage(e))
        }
    }
}

/**
 * NotificationsRepository handles notification list and read operations.
 */
class NotificationsRepository(
    private val api: DeskStreamApiService = ApiClient.apiService
) {

    suspend fun getNotifications(): ApiResult<NotificationsResponse> {
        return try {
            val response = api.getNotifications()
            if (response.isSuccessful) {
                ApiResult.Success(response.body() ?: NotificationsResponse("success", emptyList(), 0))
            } else {
                ApiResult.Error(parseErrorBody(response.errorBody()?.string()), response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(networkErrorMessage(e))
        }
    }

    suspend fun markAllRead(): ApiResult<GenericResponse> {
        return try {
            val response = api.markAllNotificationsRead()
            if (response.isSuccessful) {
                ApiResult.Success(response.body() ?: GenericResponse("success", "Marked all as read"))
            } else {
                ApiResult.Error(parseErrorBody(response.errorBody()?.string()), response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(networkErrorMessage(e))
        }
    }

    suspend fun markRead(notificationId: Int): ApiResult<GenericResponse> {
        return try {
            val response = api.markNotificationRead(notificationId)
            if (response.isSuccessful) {
                ApiResult.Success(response.body() ?: GenericResponse("success", "Marked as read"))
            } else {
                ApiResult.Error(parseErrorBody(response.errorBody()?.string()), response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(networkErrorMessage(e))
        }
    }
}

/**
 * ConnectionsRepository manages initiating, polling, and cancelling remote desktop sessions.
 */
class ConnectionsRepository(
    private val api: DeskStreamApiService = ApiClient.apiService
) {
    suspend fun requestConnection(
        targetSystemId: String,
        requesterSystemId: String,
        requesterName: String = "Android Mobile"
    ): ApiResult<ConnectionRequestResponse> {
        return try {
            val response = api.requestConnection(
                ConnectionRequestPayload(
                    targetSystemId = targetSystemId,
                    requesterSystemId = requesterSystemId,
                    requesterName = requesterName
                )
            )
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                val errorMsg = parseErrorBody(response.errorBody()?.string(), response.code())
                ApiResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(networkErrorMessage(e))
        }
    }

    suspend fun checkStatus(token: String): ApiResult<ConnectionStatusResponse> {
        return try {
            val response = api.checkConnectionStatus(token)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                val errorMsg = parseErrorBody(response.errorBody()?.string(), response.code())
                ApiResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(networkErrorMessage(e))
        }
    }

    suspend fun cancelConnection(token: String): ApiResult<GenericResponse> {
        return try {
            val response = api.cancelConnection(mapOf("token" to token))
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(parseErrorBody(response.errorBody()?.string(), response.code()), response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(networkErrorMessage(e))
        }
    }

    suspend fun registerDevice(
        systemId: String,
        deviceName: String,
        osInfo: String = "android",
        userId: Int? = null
    ): ApiResult<GenericResponse> {
        return try {
            val response = api.registerDevice(
                DeviceRegisterPayload(
                    systemId = systemId,
                    name = deviceName,
                    osInfo = osInfo,
                    userId = userId
                )
            )
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(parseErrorBody(response.errorBody()?.string(), response.code()), response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(networkErrorMessage(e))
        }
    }

    suspend fun sendHeartbeat(systemId: String): ApiResult<GenericResponse> {
        return try {
            val response = api.sendDeviceHeartbeat(HeartbeatPayload(systemId))
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(parseErrorBody(response.errorBody()?.string(), response.code()), response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(networkErrorMessage(e))
        }
    }

    suspend fun checkIncomingConnection(systemId: String): ApiResult<IncomingConnectionResponse> {
        return try {
            val response = api.checkIncomingConnection(systemId)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(parseErrorBody(response.errorBody()?.string(), response.code()), response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(networkErrorMessage(e))
        }
    }

    suspend fun acceptConnection(token: String, systemId: String): ApiResult<GenericResponse> {
        return try {
            val response = api.acceptConnection(ConnectionAcceptPayload(token, systemId))
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(parseErrorBody(response.errorBody()?.string(), response.code()), response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(networkErrorMessage(e))
        }
    }

    suspend fun rejectConnection(token: String, systemId: String): ApiResult<GenericResponse> {
        return try {
            val response = api.rejectConnection(ConnectionRejectPayload(token, systemId))
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(parseErrorBody(response.errorBody()?.string(), response.code()), response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(networkErrorMessage(e))
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

/** Parse a Gson-encoded JSON error body for the "message" or "error" field, with HTTP status fallbacks. */
private fun parseErrorBody(body: String?, httpCode: Int = -1): String {
    if (!body.isNullOrBlank()) {
        try {
            val element = com.google.gson.JsonParser.parseString(body)
            if (element.isJsonObject) {
                val obj = element.asJsonObject
                val message = obj.get("message")?.asString
                    ?: obj.get("error")?.asString
                    ?: obj.get("status")?.asString
                if (!message.isNullOrBlank()) {
                    return message
                }
            }
        } catch (_: Exception) {
            // Body was not valid JSON (e.g. HTML 502/500 gateway error page)
        }
    }
    return when (httpCode) {
        400 -> "Invalid request. Please check your details."
        401 -> "Invalid email or password."
        403 -> "Access denied."
        404 -> "Service endpoint not found on server."
        500 -> "Internal server error. Please try again later."
        502, 503, 504 -> "Server is temporarily unavailable. Please try again later."
        else -> "An unexpected error occurred. Please try again."
    }
}

/** Convert common network exceptions into user-friendly messages. */
private fun networkErrorMessage(e: Exception): String {
    return when {
        e is java.net.UnknownHostException -> "No internet connection. Please check your network."
        e is java.net.SocketTimeoutException -> "Request timed out. Please try again."
        e is javax.net.ssl.SSLException -> "Secure connection failed. Please try again."
        e.message?.contains("failed to connect", ignoreCase = true) == true ->
            "Could not connect to the server. Please try again later."
        else -> e.localizedMessage ?: "An unexpected network error occurred."
    }
}
