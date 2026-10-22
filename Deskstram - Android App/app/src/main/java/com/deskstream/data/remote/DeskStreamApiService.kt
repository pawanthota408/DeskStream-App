package com.deskstream.data.remote

import com.deskstream.data.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * DeskStream API Service interface.
 * All endpoints map to the existing PHP backend under /backend/api/ and /api/.
 *
 * Base URL is configured in ApiClient depending on build flavor (prod vs dev).
 * The backend uses PHP sessions for authentication; the OkHttp cookie jar
 * in ApiClient handles session cookies automatically after login.
 */
interface DeskStreamApiService {

    // ─── Auth ─────────────────────────────────────────────────────────────────

    /**
     * POST /DeskStream/api/auth/login.php
     * Accepts JSON body { email, password }.
     * Returns session cookie + user info.
     */
    @POST("auth/login.php")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    /**
     * POST /DeskStream/api/auth/register.php
     * Accepts JSON body { username, email, password }.
     */
    @POST("auth/register.php")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    /**
     * POST /DeskStream/api/auth/logout.php
     */
    @POST("auth/logout.php")
    suspend fun logout(): Response<GenericResponse>

    // ─── User Profile ─────────────────────────────────────────────────────────

    /**
     * GET /DeskStream/api/auth/profile.php
     */
    @GET("auth/profile.php")
    suspend fun getProfile(): Response<UserProfileResponse>

    // ─── Devices ──────────────────────────────────────────────────────────────

    /**
     * GET /DeskStream/api/devices/list.php
     * Returns list of registered devices for authenticated user.
     */
    @GET("devices/list.php")
    suspend fun listDevices(): Response<DeviceListResponse>

    /**
     * POST /DeskStream/api/devices/heartbeat.php
     * Called periodically by the desktop agent; Android app can poll device status.
     */
    @POST("devices/heartbeat.php")
    @FormUrlEncoded
    suspend fun sendDeviceHeartbeat(@Field("device_uid") deviceUid: String): Response<GenericResponse>

    // ─── Sessions ─────────────────────────────────────────────────────────────

    /**
     * GET /DeskStream/api/sessions/active.php
     * Returns all active streaming sessions for the authenticated user.
     */
    @GET("sessions/active.php")
    suspend fun getActiveSessions(): Response<ActiveSessionsResponse>

    /**
     * GET /DeskStream/api/sessions/history.php
     * Returns paginated session history.
     */
    @GET("sessions/history.php")
    suspend fun getSessionHistory(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<SessionHistoryResponse>

    /**
     * POST /DeskStream/api/sessions/end.php
     * Terminates an active session by session_key.
     */
    @POST("sessions/end.php")
    @FormUrlEncoded
    suspend fun endSession(@Field("session_key") sessionKey: String): Response<GenericResponse>

    // ─── Notifications ────────────────────────────────────────────────────────

    /**
     * GET /DeskStream/api/notifications/list.php
     */
    @GET("notifications/list.php")
    suspend fun getNotifications(): Response<NotificationsResponse>

    /**
     * POST /DeskStream/api/notifications/mark-all-read.php
     */
    @POST("notifications/mark-all-read.php")
    suspend fun markAllNotificationsRead(): Response<GenericResponse>

    /**
     * POST /DeskStream/api/notifications/read.php
     */
    @POST("notifications/read.php")
    @FormUrlEncoded
    suspend fun markNotificationRead(@Field("notification_id") notificationId: Int): Response<GenericResponse>

    // ─── Diagnostics / Health Checks ──────────────────────────────────────────

    /**
     * GET /DeskStream/api/updates/check.php
     * Lightweight health check to test server connectivity and latency.
     */
    @GET("updates/check.php")
    suspend fun pingServer(): Response<ResponseBody>

    /**
     * POST /DeskStream/api/devices/heartbeat.php
     * Probe database connectivity via the heartbeat endpoint.
     */
    @POST("devices/heartbeat.php")
    suspend fun probeDatabaseHealth(@Body probe: Map<String, String>): Response<ResponseBody>

    // ─── Connection Requests ──────────────────────────────────────────────────

    /**
     * POST /DeskStream/api/connections/request.php
     * Request connection to remote device by system ID.
     */
    @POST("connections/request.php")
    suspend fun requestConnection(
        @Body payload: ConnectionRequestPayload
    ): Response<ConnectionRequestResponse>

    /**
     * GET /DeskStream/api/connections/status.php
     * Poll status of connection request.
     */
    @GET("connections/status.php")
    suspend fun checkConnectionStatus(
        @Query("token") token: String
    ): Response<ConnectionStatusResponse>

    /**
     * POST /DeskStream/api/connections/cancel.php
     * Cancel pending connection request.
     */
    @POST("connections/cancel.php")
    suspend fun cancelConnection(
        @Body body: Map<String, String>
    ): Response<GenericResponse>

    // ─── Mobile Device Registration & Inbound Support ─────────────────────────

    /**
     * POST /DeskStream/api/devices/register.php
     * Register this mobile device on the DeskStream network.
     */
    @POST("devices/register.php")
    suspend fun registerDevice(
        @Body payload: DeviceRegisterPayload
    ): Response<GenericResponse>

    /**
     * POST /DeskStream/api/devices/heartbeat.php
     * Send heartbeat for this mobile device.
     */
    @POST("devices/heartbeat.php")
    suspend fun sendDeviceHeartbeat(
        @Body payload: HeartbeatPayload
    ): Response<GenericResponse>

    /**
     * GET /DeskStream/api/connections/incoming.php
     * Poll incoming connection requests targeting this device.
     */
    @GET("connections/incoming.php")
    suspend fun checkIncomingConnection(
        @Query("system_id") systemId: String
    ): Response<IncomingConnectionResponse>

    /**
     * POST /DeskStream/api/connections/accept.php
     * Accept incoming connection request.
     */
    @POST("connections/accept.php")
    suspend fun acceptConnection(
        @Body payload: ConnectionAcceptPayload
    ): Response<GenericResponse>

    /**
     * POST /DeskStream/api/connections/reject.php
     * Reject incoming connection request.
     */
    @POST("connections/reject.php")
    suspend fun rejectConnection(
        @Body payload: ConnectionRejectPayload
    ): Response<GenericResponse>
}

