package com.deskstream.data.local

import android.content.Context
import android.content.SharedPreferences
import com.deskstream.data.model.UserDto
import com.google.gson.Gson

/**
 * SessionManager persists the logged-in user's data across app restarts
 * using Android SharedPreferences.
 *
 * Stored data:
 *  - User object (id, username, email, unique_user_id)
 *  - Session token / PHPSESSID (if the server ever exposes it as a token)
 *  - Login state flag
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "deskstream_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_JSON = "user_json"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_UNIQUE_USER_ID = "unique_user_id"
        private const val KEY_COOKIE_HEADER = "cookie_header"
        private const val KEY_SAVED_EMAIL = "saved_email"
        private const val KEY_SAVED_PASSWORD = "saved_password"

        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // ─── Login / Logout ───────────────────────────────────────────────────────

    fun saveLoginSession(user: UserDto, token: String? = null, cookieHeader: String? = null) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_JSON, gson.toJson(user))
            putString(KEY_UNIQUE_USER_ID, user.uniqueUserId)
            if (token != null) putString(KEY_AUTH_TOKEN, token)
            if (cookieHeader != null) putString(KEY_COOKIE_HEADER, cookieHeader)
            apply()
        }
    }

    fun saveCredentials(email: String, pass: String) {
        prefs.edit().apply {
            putString(KEY_SAVED_EMAIL, email)
            putString(KEY_SAVED_PASSWORD, pass)
            apply()
        }
    }

    fun saveCookieHeader(cookie: String) {
        prefs.edit().putString(KEY_COOKIE_HEADER, cookie).apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    // ─── Getters ─────────────────────────────────────────────────────────────

    val isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    val currentUser: UserDto?
        get() {
            val json = prefs.getString(KEY_USER_JSON, null) ?: return null
            return try {
                gson.fromJson(json, UserDto::class.java)
            } catch (e: Exception) {
                null
            }
        }

    val authToken: String?
        get() = prefs.getString(KEY_AUTH_TOKEN, null)

    val cookieHeader: String?
        get() = prefs.getString(KEY_COOKIE_HEADER, null)

    val uniqueUserId: String
        get() = prefs.getString(KEY_UNIQUE_USER_ID, "") ?: ""

    val userName: String
        get() = currentUser?.username ?: ""

    val userEmail: String
        get() = currentUser?.email ?: ""

    val savedEmail: String?
        get() = prefs.getString(KEY_SAVED_EMAIL, null)

    val savedPassword: String?
        get() = prefs.getString(KEY_SAVED_PASSWORD, null)
}
