package com.deskstream.data.model

import com.google.gson.annotations.SerializedName

// ─── Auth ────────────────────────────────────────────────────────────────────

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class UserDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("username") val username: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("unique_user_id") val uniqueUserId: String = ""
)

data class SystemDto(
    @SerializedName("system_id") val systemId: String = "",
    @SerializedName("device_name") val deviceName: String = "",
    @SerializedName("status") val status: String = "offline"
)

data class LoginResponse(
    @SerializedName("status") val status: String = "",
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("user") val user: UserDto? = null,
    @SerializedName("system") val system: SystemDto? = null
)

data class RegisterResponse(
    @SerializedName("status") val status: String = "",
    @SerializedName("message") val message: String = "",
    @SerializedName("user_id") val userId: Int = 0,
    @SerializedName("unique_user_id") val uniqueUserId: String = ""
)

// ─── Devices ─────────────────────────────────────────────────────────────────

data class DeviceDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("user_id") val userId: Int = 0,
    @SerializedName("system_id") val systemId: String = "",
    @SerializedName("device_uid") val deviceUid: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("os_type") val osType: String = "",
    @SerializedName("ip_address") val ipAddress: String = "",
    @SerializedName("is_online") val isOnline: Int = 0,
    @SerializedName("last_seen_at") val lastSeenAt: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
) {
    val deviceName: String get() = name.ifBlank { systemId }
    val status: String get() = if (isOnline == 1) "online" else "offline"
    val os: String? get() = osType.ifBlank { null }
    val uniqueDeviceId: String? get() = deviceUid.ifBlank { systemId.ifBlank { null } }
    val deviceType: String? get() = osType.ifBlank { null }
}

data class DeviceListResponse(
    @SerializedName("status") val status: String = "",
    @SerializedName("count") val count: Int = 0,
    @SerializedName("devices") val devices: List<DeviceDto> = emptyList()
)

// ─── Sessions ─────────────────────────────────────────────────────────────────

data class SessionDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("session_key") val sessionKey: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("started_at") val startedAt: String? = null,
    @SerializedName("ended_at") val endedAt: String? = null,
    @SerializedName("last_activity_at") val lastActivityAt: String? = null,
    @SerializedName("bandwidth_used_mb") val bandwidthUsedMb: Double = 0.0,
    @SerializedName("host_device_id") val hostDeviceId: Int = 0,
    @SerializedName("requester_user_id") val requesterUserId: Int = 0,
    @SerializedName("connection_mode") val connectionMode: String = "desktop"
)

data class ActiveSessionsResponse(
    @SerializedName("status") val status: String = "",
    @SerializedName("sessions") val sessions: List<SessionDto> = emptyList()
)

data class SessionHistoryResponse(
    @SerializedName("status") val status: String = "",
    @SerializedName("sessions") val sessions: List<SessionDto> = emptyList(),
    @SerializedName("total") val total: Int = 0
)

// ─── Notifications ────────────────────────────────────────────────────────────

data class NotificationDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("user_id") val userId: Int = 0,
    @SerializedName("type") val type: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("message") val message: String = "",
    @SerializedName("is_read") val isRead: Int = 0,
    @SerializedName("created_at") val createdAt: String = ""
)

data class NotificationsResponse(
    @SerializedName("status") val status: String = "",
    @SerializedName("notifications") val notifications: List<NotificationDto> = emptyList(),
    @SerializedName("unread_count") val unreadCount: Int = 0
)

// ─── Generic ─────────────────────────────────────────────────────────────────

data class GenericResponse(
    @SerializedName("status") val status: String = "",
    @SerializedName("message") val message: String = ""
)

data class UserProfileResponse(
    @SerializedName("status") val status: String = "",
    @SerializedName("user") val user: UserDto? = null,
    @SerializedName("subscription") val subscription: SubscriptionDto? = null
)

data class SubscriptionDto(
    @SerializedName("plan_name") val planName: String = "",
    @SerializedName("plan_slug") val planSlug: String = "",
    @SerializedName("expires_at") val expiresAt: String? = null,
    @SerializedName("status") val status: String = ""
)

// ─── Connection Requests ──────────────────────────────────────────────────────

data class ConnectionRequestPayload(
    @SerializedName("target_system_id") val targetSystemId: String,
    @SerializedName("requester_system_id") val requesterSystemId: String,
    @SerializedName("requester_name") val requesterName: String = "Android Mobile"
)

data class ConnectionRequestData(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("request_token") val requestToken: String = "",
    @SerializedName("target_system_id") val targetSystemId: String = "",
    @SerializedName("target_name") val targetName: String? = null,
    @SerializedName("requester_system_id") val requesterSystemId: String = "",
    @SerializedName("requester_name") val requesterName: String = "",
    @SerializedName("status") val status: String = "pending",
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("expires_at") val expiresAt: String? = null
)

data class ConnectionRequestResponse(
    @SerializedName("status") val status: String = "",
    @SerializedName("message") val message: String? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("request") val request: ConnectionRequestData? = null
)

data class ConnectionStatusResponse(
    @SerializedName("status") val status: String = "",
    @SerializedName("request_status") val requestStatus: String = "",
    @SerializedName("message") val message: String? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("request") val request: ConnectionRequestData? = null
)

// ─── Mobile Device Registration & Inbound Support ─────────────────────────────

data class DeviceRegisterPayload(
    @SerializedName("system_id") val systemId: String,
    @SerializedName("name") val name: String,
    @SerializedName("os_info") val osInfo: String = "android",
    @SerializedName("user_id") val userId: Int? = null
)

data class HeartbeatPayload(
    @SerializedName("system_id") val systemId: String
)

data class IncomingConnectionRequest(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("request_token") val requestToken: String = "",
    @SerializedName("requester_system_id") val requesterSystemId: String = "",
    @SerializedName("requester_name") val requesterName: String = "Remote User",
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("expires_at") val expiresAt: String? = null
)

data class IncomingConnectionResponse(
    @SerializedName("status") val status: String = "",
    @SerializedName("has_request") val hasRequest: Boolean = false,
    @SerializedName("request") val request: IncomingConnectionRequest? = null,
    @SerializedName("message") val message: String? = null
)

data class ConnectionAcceptPayload(
    @SerializedName("request_token") val requestToken: String,
    @SerializedName("target_system_id") val targetSystemId: String
)

data class ConnectionRejectPayload(
    @SerializedName("request_token") val requestToken: String,
    @SerializedName("target_system_id") val targetSystemId: String
)


