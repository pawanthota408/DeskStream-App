package com.deskstream.ui.remote

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.deskstream.data.local.SessionManager
import com.deskstream.data.remote.ApiClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RemoteSessionScreen(
    remoteId: String,
    sessionUrl: String,
    onDisconnect: () -> Unit = {},
    onReLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var showToolbar by remember { mutableStateOf(false) }

    // Intercept back button to confirm exit / disconnect
    BackHandler {
        onDisconnect()
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.let { wv ->
                wv.stopLoading()
                wv.loadUrl("about:blank")
                wv.destroy()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // 1. High-Performance WebRTC/WebSocket Remote Desktop Canvas
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewRef = this
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    setBackgroundColor(android.graphics.Color.parseColor("#0F172A"))

                    // Enable Hardware Acceleration & Cookie Persistence
                    setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                    isFocusable = true
                    isFocusableInTouchMode = true

                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, true)

                    // 1. Force push all active OkHttp & saved session cookies into CookieManager
                    ApiClient.syncCookiesToWebView("https://friendssoftwaresolutions.in")

                    // 2. Resolve active cookie string
                    val sessionMgr = SessionManager.getInstance(ctx)
                    var cookieHeader = cookieManager.getCookie("https://friendssoftwaresolutions.in")
                    if (cookieHeader.isNullOrBlank()) {
                        cookieHeader = ApiClient.sessionCookieHeader ?: sessionMgr.cookieHeader
                        if (!cookieHeader.isNullOrBlank()) {
                            ApiClient.restoreSession(cookieHeader)
                        }
                    }

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        builtInZoomControls = true
                        displayZoomControls = false
                        supportZoom()
                        allowFileAccess = true
                        allowContentAccess = true
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        cacheMode = WebSettings.LOAD_DEFAULT
                        mediaPlaybackRequiresUserGesture = false
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onPermissionRequest(request: PermissionRequest?) {
                            request?.grant(request.resources)
                        }
                    }

                    var redirectAttempt = 0
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                            loadError = null
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                            showToolbar = false
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val destUrl = request?.url?.toString() ?: return false
                            // Intercept navigation to dashboard to exit session gracefully to Android native UI
                            if (destUrl.contains("dashboard.php")) {
                                view?.post { onDisconnect() }
                                return true
                            }
                            // Intercept unexpected redirection to web login or index.php
                            if (destUrl.contains("index.php") || destUrl.contains("login.php")) {
                                redirectAttempt++
                                if (redirectAttempt <= 1) {
                                    val email = sessionMgr.savedEmail ?: sessionMgr.userEmail
                                    val pass = sessionMgr.savedPassword
                                    if (!email.isNullOrBlank() && !pass.isNullOrBlank()) {
                                        // Auto-login in background thread to establish PHP session
                                        Thread {
                                            try {
                                                kotlinx.coroutines.runBlocking {
                                                    com.deskstream.data.repository.AuthRepository().login(email, pass)
                                                }
                                                ApiClient.syncCookiesToWebView("https://friendssoftwaresolutions.in")
                                                val freshCookie = cookieManager.getCookie("https://friendssoftwaresolutions.in")
                                                    ?: ApiClient.sessionCookieHeader
                                                    ?: ""
                                                val retryHeaders = mutableMapOf<String, String>()
                                                if (freshCookie.isNotBlank()) {
                                                    retryHeaders["Cookie"] = freshCookie
                                                }
                                                view?.post {
                                                    view.loadUrl(sessionUrl, retryHeaders)
                                                }
                                            } catch (_: Exception) {
                                                view?.post {
                                                    isLoading = false
                                                    loadError = "Your remote login session has expired. Please tap 'Close' and log in again."
                                                }
                                            }
                                        }.start()
                                        return true
                                    }
                                }
                                isLoading = false
                                loadError = "Your remote session has expired or is unauthenticated. Please tap 'Close' and log in again."
                                return true
                            }
                            return false
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            if (request?.isForMainFrame == true) {
                                isLoading = false
                                loadError = error?.description?.toString() ?: "Failed to connect to remote display"
                            }
                        }
                    }

                    // 3. Load remote session stream carrying explicit Cookie header
                    val initialHeaders = mutableMapOf<String, String>()
                    if (!cookieHeader.isNullOrBlank()) {
                        initialHeaders["Cookie"] = cookieHeader
                    }
                    loadUrl(sessionUrl, initialHeaders)
                }
            }
        )

        // 2. Loading Indicator Overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A).copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(44.dp),
                        color = Color(0xFFE50914),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Connecting to Remote Screen...",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Device ID: $remoteId",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                }
            }
        }

        // 3. Connection Error Overlay
        if (loadError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A).copy(alpha = 0.95f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "⚠️ Connection Issue",
                        color = Color(0xFFEF4444),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = loadError ?: "Could not load remote desktop stream.",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val isAuthError = loadError?.contains("expired", ignoreCase = true) == true ||
                                loadError?.contains("unauthenticated", ignoreCase = true) == true

                        if (isAuthError) {
                            Surface(
                                color = Color(0xFFE50914),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable { onReLogin() }
                            ) {
                                Text(
                                    text = "Sign In Again",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                                )
                            }
                        } else {
                            Surface(
                                color = Color(0xFFE50914),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable {
                                    ApiClient.syncCookiesToWebView("https://friendssoftwaresolutions.in")
                                    val c = CookieManager.getInstance().getCookie("https://friendssoftwaresolutions.in") ?: ApiClient.sessionCookieHeader
                                    val h = mutableMapOf<String, String>()
                                    if (!c.isNullOrBlank()) h["Cookie"] = c
                                    webViewRef?.loadUrl(sessionUrl, h)
                                }
                            ) {
                                Text(
                                    text = "Retry",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                                )
                            }
                        }

                        Surface(
                            color = Color(0xFF334155),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.clickable { onDisconnect() }
                        ) {
                            Text(
                                text = "Close",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. Floating Top Control Bar (Collapsible / Toggleable)
        AnimatedVisibility(
            visible = showToolbar,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                color = Color(0xFF0F172A).copy(alpha = 0.92f),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { onDisconnect() },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF334155))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Disconnect",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Remote Session Live",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "ID: $remoteId",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Soft Keyboard Toggle
                        IconButton(
                            onClick = {
                                webViewRef?.let { wv ->
                                    wv.requestFocus()
                                    wv.evaluateJavascript("if (typeof window.focusMobileKeyboard === 'function') window.focusMobileKeyboard();", null)
                                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                    imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                                }
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF334155))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Keyboard,
                                contentDescription = "Keyboard",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Refresh Stream
                        IconButton(
                            onClick = {
                                ApiClient.syncCookiesToWebView("https://friendssoftwaresolutions.in")
                                val c = CookieManager.getInstance().getCookie("https://friendssoftwaresolutions.in") ?: ApiClient.sessionCookieHeader
                                val h = mutableMapOf<String, String>()
                                if (!c.isNullOrBlank()) h["Cookie"] = c
                                webViewRef?.loadUrl(sessionUrl, h)
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF334155))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "Refresh",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Disconnect Button
                        Surface(
                            color = Color(0xFFE50914),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.clickable { onDisconnect() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Disconnect",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
