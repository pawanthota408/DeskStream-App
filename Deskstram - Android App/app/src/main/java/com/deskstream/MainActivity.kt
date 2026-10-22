package com.deskstream

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.deskstream.data.local.SessionManager
import com.deskstream.ui.home.DevicesScreen
import com.deskstream.ui.home.FilesScreen
import com.deskstream.ui.home.HomeScreen
import com.deskstream.ui.home.HomeViewModel
import com.deskstream.ui.home.NotificationsScreen
import com.deskstream.ui.home.ProfileScreen
import com.deskstream.ui.home.SettingsScreen
import com.deskstream.ui.home.SharedScreen
import com.deskstream.ui.login.LoginScreen
import com.deskstream.ui.login.LoginViewModel
import com.deskstream.ui.splash.SplashScreen
import com.deskstream.ui.theme.DeskStreamTheme

import com.deskstream.data.remote.ApiClient
import com.deskstream.ui.home.OutboundConnectionState
import com.deskstream.ui.remote.RemoteSessionScreen

enum class AppDestination {
    SPLASH,
    LOGIN,
    HOME,
    DEVICES,
    FILES,
    SHARED,
    SETTINGS,
    PROFILE,
    NOTIFICATIONS,
    REMOTE_SESSION
}

class MainActivity : ComponentActivity() {

    // ViewModels survive configuration changes (e.g. rotation)
    private val loginViewModel: LoginViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager.getInstance(this)
        com.deskstream.data.remote.ApiClient.onSessionCookieUpdated = { cookie ->
            sessionManager.saveCookieHeader(cookie)
        }
        com.deskstream.data.remote.ApiClient.restoreSession(sessionManager.cookieHeader)

        setContent {
            DeskStreamTheme {
                var currentDestination by rememberSaveable { mutableStateOf(AppDestination.SPLASH) }

                // Observe login UI state
                val loginUiState by loginViewModel.uiState.collectAsState()

                // Navigate to HOME when login succeeds
                LaunchedEffect(loginUiState.isLoginSuccess) {
                    if (loginUiState.isLoginSuccess) {
                        currentDestination = AppDestination.HOME
                    }
                }

                // Observe home logout state
                val homeUiState by homeViewModel.uiState.collectAsState()
                LaunchedEffect(homeUiState.isLoggedOut) {
                    if (homeUiState.isLoggedOut) {
                        currentDestination = AppDestination.LOGIN
                    }
                }

                // Navigate to live remote session when outbound connection is approved/connected
                LaunchedEffect(homeUiState.outboundState) {
                    if (homeUiState.outboundState is OutboundConnectionState.Connected) {
                        currentDestination = AppDestination.REMOTE_SESSION
                    }
                }

                Crossfade(
                    targetState = currentDestination,
                    animationSpec = tween(durationMillis = 600),
                    label = "ScreenTransition"
                ) { destination ->
                    when (destination) {
                        AppDestination.SPLASH -> {
                            SplashScreen(
                                onSplashFinished = {
                                    // Skip login only if authenticated AND persistent credentials exist for auto-reauth
                                    currentDestination = if (sessionManager.isLoggedIn && !sessionManager.savedPassword.isNullOrBlank()) {
                                        AppDestination.HOME
                                    } else {
                                        // Clear stale session so user gets fresh authentication and saves credentials
                                        sessionManager.clearSession()
                                        ApiClient.clearSession()
                                        AppDestination.LOGIN
                                    }
                                }
                            )
                        }

                        AppDestination.LOGIN -> {
                            LoginScreen(
                                onLoginSuccess = { email, password ->
                                    // Trigger real API login via ViewModel
                                    loginViewModel.login(email, password)
                                },
                                loginUiState = loginUiState,
                                onClearError = { loginViewModel.clearError() },
                                onCheckHealth = { loginViewModel.checkSystemHealth() }
                            )
                        }

                        AppDestination.HOME -> {
                            HomeScreen(
                                viewModel = homeViewModel,
                                onProfileClick = {
                                    currentDestination = AppDestination.PROFILE
                                },
                                onNotificationClick = {
                                    currentDestination = AppDestination.NOTIFICATIONS
                                },
                                onDevicesTabClick = {
                                    currentDestination = AppDestination.DEVICES
                                },
                                onFilesTabClick = {
                                    currentDestination = AppDestination.FILES
                                },
                                onSharedClick = {
                                    currentDestination = AppDestination.SHARED
                                },
                                onProfileTabClick = {
                                    currentDestination = AppDestination.PROFILE
                                },
                                onSettingsTabClick = {
                                    currentDestination = AppDestination.SETTINGS
                                },
                                onLogoutClick = {
                                    homeViewModel.logout()
                                }
                            )
                        }

                        AppDestination.DEVICES -> {
                            DevicesScreen(
                                viewModel = homeViewModel,
                                onBackClick = {
                                    currentDestination = AppDestination.HOME
                                },
                                onHomeTabClick = {
                                    currentDestination = AppDestination.HOME
                                },
                                onFilesTabClick = {
                                    currentDestination = AppDestination.FILES
                                },
                                onProfileTabClick = {
                                    currentDestination = AppDestination.PROFILE
                                }
                            )
                        }

                        AppDestination.FILES -> {
                            FilesScreen(
                                onBackClick = {
                                    currentDestination = AppDestination.HOME
                                },
                                onNotificationClick = {
                                    currentDestination = AppDestination.NOTIFICATIONS
                                },
                                onHomeTabClick = {
                                    currentDestination = AppDestination.HOME
                                },
                                onDevicesTabClick = {
                                    currentDestination = AppDestination.DEVICES
                                },
                                onProfileTabClick = {
                                    currentDestination = AppDestination.PROFILE
                                },
                                onProfileClick = {
                                    currentDestination = AppDestination.PROFILE
                                }
                            )
                        }

                        AppDestination.SHARED -> {
                            SharedScreen(
                                onBackClick = {
                                    currentDestination = AppDestination.HOME
                                },
                                onHomeTabClick = {
                                    currentDestination = AppDestination.HOME
                                },
                                onDevicesTabClick = {
                                    currentDestination = AppDestination.DEVICES
                                },
                                onFilesTabClick = {
                                    currentDestination = AppDestination.FILES
                                },
                                onProfileTabClick = {
                                    currentDestination = AppDestination.PROFILE
                                }
                            )
                        }

                        AppDestination.SETTINGS -> {
                            SettingsScreen(
                                onBackClick = {
                                    currentDestination = AppDestination.HOME
                                },
                                onHomeTabClick = {
                                    currentDestination = AppDestination.HOME
                                },
                                onDevicesTabClick = {
                                    currentDestination = AppDestination.DEVICES
                                },
                                onFilesTabClick = {
                                    currentDestination = AppDestination.FILES
                                },
                                onProfileTabClick = {
                                    currentDestination = AppDestination.PROFILE
                                },
                                onLogoutClick = {
                                    homeViewModel.logout()
                                }
                            )
                        }

                        AppDestination.PROFILE -> {
                            ProfileScreen(
                                userName = homeViewModel.userName,
                                userEmail = homeViewModel.userEmail,
                                onBackClick = {
                                    currentDestination = AppDestination.HOME
                                },
                                onHomeTabClick = {
                                    currentDestination = AppDestination.HOME
                                },
                                onDevicesTabClick = {
                                    currentDestination = AppDestination.DEVICES
                                },
                                onFilesTabClick = {
                                    currentDestination = AppDestination.FILES
                                },
                                onLogoutClick = {
                                    homeViewModel.logout()
                                }
                            )
                        }

                        AppDestination.NOTIFICATIONS -> {
                            NotificationsScreen(
                                viewModel = homeViewModel,
                                onBackClick = {
                                    currentDestination = AppDestination.HOME
                                }
                            )
                        }

                        AppDestination.REMOTE_SESSION -> {
                            val connectedState = homeUiState.outboundState as? OutboundConnectionState.Connected
                            RemoteSessionScreen(
                                remoteId = connectedState?.targetId ?: "",
                                sessionUrl = connectedState?.sessionUrl ?: "",
                                onDisconnect = {
                                    homeViewModel.dismissConnectionState()
                                    currentDestination = AppDestination.HOME
                                },
                                onReLogin = {
                                    homeViewModel.dismissConnectionState()
                                    sessionManager.clearSession()
                                    ApiClient.clearSession()
                                    currentDestination = AppDestination.LOGIN
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
