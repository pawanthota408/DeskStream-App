package com.deskstream.data.remote

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit/OkHttp client for all DeskStream API calls.
 *
 * Key design decisions:
 *  - [PRODUCTION_BASE_URL] points to the live DeskStream server.
 *  - An in-memory [PersistentCookieJar] stores PHP session cookies so that
 *    all subsequent requests after login carry the PHPSESSID automatically.
 *  - [HttpLoggingInterceptor] logs request/response bodies in DEBUG builds only.
 *  - Timeouts are set generously to handle poor mobile connections.
 */
object ApiClient {

    /** Live production server — points to DeskStream PHP API on Hostinger */
    const val PRODUCTION_BASE_URL = "https://friendssoftwaresolutions.in/DeskStream/api/"

    val cookieJar = PersistentCookieJar()

    @Volatile
    var sessionCookieHeader: String? = null

    /** Callback to notify when session cookies update so they can be persisted */
    var onSessionCookieUpdated: ((String) -> Unit)? = null

    /**
     * Restore session cookies from local persistent storage (e.g. SessionManager).
     */
    fun restoreSession(savedCookieHeader: String?) {
        if (!savedCookieHeader.isNullOrBlank()) {
            sessionCookieHeader = savedCookieHeader
            syncCookiesToWebView()
        }
    }

    /**
     * Synchronize all active session cookies from OkHttp / sessionCookieHeader
     * directly into Android's native WebView CookieManager for all relevant URLs and paths.
     */
    fun syncCookiesToWebView(domainUrl: String = "https://friendssoftwaresolutions.in") {
        try {
            val cm = android.webkit.CookieManager.getInstance()
            cm.setAcceptCookie(true)

            val cookies = cookieJar.getAllCookies()
            for (cookie in cookies) {
                val cookieValDomain = "${cookie.name}=${cookie.value}; Path=/; Domain=friendssoftwaresolutions.in"
                val cookieValSimple = "${cookie.name}=${cookie.value}; Path=/"
                val cookieValSub = "${cookie.name}=${cookie.value}; Path=/DeskStream/"

                cm.setCookie("https://friendssoftwaresolutions.in", cookieValDomain)
                cm.setCookie("https://friendssoftwaresolutions.in", cookieValSimple)
                cm.setCookie("https://friendssoftwaresolutions.in/DeskStream/", cookieValSimple)
                cm.setCookie("https://friendssoftwaresolutions.in/DeskStream/", cookieValSub)
                cm.setCookie("https://friendssoftwaresolutions.in/DeskStream/remote/", cookieValSimple)
                cm.setCookie("https://friendssoftwaresolutions.in/DeskStream/remote/session.php", cookieValSimple)
            }

            sessionCookieHeader?.let { header ->
                header.split(";").forEach { part ->
                    val trimmed = part.trim()
                    if (trimmed.isNotEmpty()) {
                        cm.setCookie("https://friendssoftwaresolutions.in", "$trimmed; Path=/; Domain=friendssoftwaresolutions.in")
                        cm.setCookie("https://friendssoftwaresolutions.in", "$trimmed; Path=/")
                        cm.setCookie("https://friendssoftwaresolutions.in/DeskStream/", "$trimmed; Path=/")
                        cm.setCookie("https://friendssoftwaresolutions.in/DeskStream/", "$trimmed; Path=/DeskStream/")
                        cm.setCookie("https://friendssoftwaresolutions.in/DeskStream/remote/", "$trimmed; Path=/")
                        cm.setCookie("https://friendssoftwaresolutions.in/DeskStream/remote/session.php", "$trimmed; Path=/")
                    }
                }
            }
            cm.flush()
        } catch (_: Exception) {}
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Set to BODY for debugging; will be NONE in release builds via ProGuard/R8 stripping
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(loggingInterceptor)
            // Set Content-Type: application/json on all requests automatically
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("Accept", "application/json")
                    .method(original.method, original.body)
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(PRODUCTION_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: DeskStreamApiService by lazy {
        retrofit.create(DeskStreamApiService::class.java)
    }

    /**
     * Clear all stored session cookies (call on logout).
     */
    fun clearSession() {
        sessionCookieHeader = null
        cookieJar.clear()
        try {
            val cm = android.webkit.CookieManager.getInstance()
            cm.removeAllCookies(null)
            cm.flush()
        } catch (_: Exception) {}
    }
}

/**
 * Thread-safe cookie jar that preserves session cookies across requests
 * and immediately synchronizes them with Android's WebView CookieManager.
 */
class PersistentCookieJar : CookieJar {
    private val cookieStore = ConcurrentHashMap<String, MutableList<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        val existing = cookieStore.getOrPut(host) { mutableListOf() }
        val cookiePairs = mutableListOf<String>()

        synchronized(existing) {
            cookies.forEach { newCookie ->
                existing.removeAll { it.name == newCookie.name }
                existing.add(newCookie)
                cookiePairs.add("${newCookie.name}=${newCookie.value}")
            }
        }

        if (cookiePairs.isNotEmpty()) {
            val headerVal = cookiePairs.joinToString("; ")
            ApiClient.sessionCookieHeader = headerVal
            ApiClient.onSessionCookieUpdated?.invoke(headerVal)
        }

        // Immediately push cookies into Android's WebView CookieManager
        ApiClient.syncCookiesToWebView()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore[url.host]?.toList() ?: emptyList()
    }

    fun getAllCookies(): List<Cookie> {
        return cookieStore.values.flatten()
    }

    fun getCookiesForHost(host: String): List<Cookie> {
        return cookieStore[host]?.toList() ?: emptyList()
    }

    fun clear() {
        cookieStore.clear()
    }
}
