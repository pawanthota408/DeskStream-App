package com.deskstream.ui.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deskstream.R
import com.deskstream.ui.theme.DeskStreamRed
import com.deskstream.ui.theme.DeskStreamTheme

enum class LoginStep {
    EMAIL,
    PASSWORD
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (String, String) -> Unit = { _, _ -> },
    onForgotPasswordClick: () -> Unit = {},
    loginUiState: LoginUiState = LoginUiState(),
    onClearError: () -> Unit = {},
    onCheckHealth: () -> Unit = {}
) {
    var currentStep by remember { mutableStateOf(LoginStep.EMAIL) }
    var emailId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    // Use isLoading from ViewModel state so the UI reflects the real API call progress
    val isLoading = loginUiState.isLoading

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAFAFC)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Soft Pink-Blue Ambient Wave Background Canvas
            Image(
                painter = painterResource(id = R.drawable.login_page_bg_final),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // 2. Main Content Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    // Brand Header: Centered Logo + "DeskStream"
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.desk_stream_d),
                            contentDescription = "DeskStream Logo",
                            modifier = Modifier.size(84.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Desk",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    fontSize = 32.sp,
                                    letterSpacing = (-0.5).sp
                                )
                            )
                            Text(
                                text = "Stream",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE50914),
                                    fontSize = 32.sp,
                                    letterSpacing = (-0.5).sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // LIVE SYSTEM CONNECTIVITY & DIAGNOSTICS CARD
                    val health = loginUiState.healthState
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "📡",
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "SYSTEM CONNECTIVITY",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF475569),
                                        letterSpacing = 0.8.sp
                                    )
                                }

                                // Refresh / Test Button
                                Row(
                                    modifier = Modifier
                                        .clickable(enabled = !health.isChecking) { onCheckHealth() }
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (health.isChecking) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(11.dp),
                                            strokeWidth = 2.dp,
                                            color = Color(0xFFE50914)
                                        )
                                    } else {
                                        Text(
                                            text = "🔄",
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text(
                                        text = if (health.isChecking) "Testing..." else "Test Connection",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFE50914)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Badges Row: Server & Database
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Server Status Badge
                                val serverBgColor = when (health.serverOnline) {
                                    true -> Color(0xFFDCFCE7)
                                    false -> Color(0xFFFEE2E2)
                                    null -> Color(0xFFFEF3C7)
                                }
                                val serverTextColor = when (health.serverOnline) {
                                    true -> Color(0xFF15803D)
                                    false -> Color(0xFFB91C1C)
                                    null -> Color(0xFFB45309)
                                }
                                val serverText = when (health.serverOnline) {
                                    true -> if (health.serverLatencyMs > 0) "Server: Online (${health.serverLatencyMs}ms)" else "Server: Online"
                                    false -> "Server: Offline"
                                    null -> if (health.isChecking) "Server: Checking..." else "Server: Unknown"
                                }

                                Surface(
                                    color = serverBgColor,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = when (health.serverOnline) {
                                                true -> "● "
                                                false -> "● "
                                                null -> "⏳ "
                                            },
                                            color = serverTextColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = serverText,
                                            color = serverTextColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1
                                        )
                                    }
                                }

                                // Database Status Badge
                                val dbBgColor = when (health.dbConnected) {
                                    true -> Color(0xFFDCFCE7)
                                    false -> Color(0xFFFEE2E2)
                                    null -> Color(0xFFFEF3C7)
                                }
                                val dbTextColor = when (health.dbConnected) {
                                    true -> Color(0xFF15803D)
                                    false -> Color(0xFFB91C1C)
                                    null -> Color(0xFFB45309)
                                }
                                val dbText = when (health.dbConnected) {
                                    true -> "DB: Connected"
                                    false -> "DB: Disconnected"
                                    null -> if (health.isChecking) "DB: Checking..." else "DB: Unknown"
                                }

                                Surface(
                                    color = dbBgColor,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = when (health.dbConnected) {
                                                true -> "● "
                                                false -> "● "
                                                null -> "⏳ "
                                            },
                                            color = dbTextColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = dbText,
                                            color = dbTextColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Animated Login Steps
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            if (targetState == LoginStep.PASSWORD) {
                                slideInHorizontally { width -> width } + fadeIn() with
                                        slideOutHorizontally { width -> -width } + fadeOut()
                            } else {
                                slideInHorizontally { width -> -width } + fadeIn() with
                                        slideOutHorizontally { width -> width } + fadeOut()
                            }
                        },
                        label = "LoginStepTransition"
                    ) { step ->
                        when (step) {
                            LoginStep.EMAIL -> {
                                // STEP 1: EMAIL / USERNAME ADDRESS FLOATING WHITE CARD
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(24.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Welcome Back",
                                            style = MaterialTheme.typography.headlineLarge.copy(
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "Enter your email or username to continue",
                                            fontSize = 14.sp,
                                            color = Color(0xFF64748B),
                                            fontWeight = FontWeight.Medium
                                        )

                                        Spacer(modifier = Modifier.height(28.dp))

                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.Start
                                        ) {
                                            Text(
                                                text = "Email or Username",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF64748B)
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            OutlinedTextField(
                                                value = emailId,
                                                onValueChange = { emailId = it },
                                                placeholder = {
                                                    Text(
                                                        text = "Enter your email or username",
                                                        color = Color(0xFF94A3B8),
                                                        fontSize = 14.sp
                                                    )
                                                },
                                                singleLine = true,
                                                leadingIcon = {
                                                    Image(
                                                        painter = painterResource(id = R.drawable.ic_email),
                                                        contentDescription = "User Icon",
                                                        modifier = Modifier
                                                            .padding(start = 12.dp, end = 4.dp)
                                                            .size(20.dp)
                                                    )
                                                },
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Text,
                                                    autoCorrect = false
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(54.dp),
                                                shape = RoundedCornerShape(14.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Color(0xFFE2E8F0),
                                                    unfocusedBorderColor = Color(0xFFF1F5F9),
                                                    focusedContainerColor = Color(0xFFF8FAFC),
                                                    unfocusedContainerColor = Color(0xFFF8FAFC),
                                                    focusedTextColor = Color(0xFF0F172A),
                                                    unfocusedTextColor = Color(0xFF0F172A)
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))

                                        Button(
                                            onClick = {
                                                if (emailId.isNotBlank()) {
                                                    emailId = emailId.trim()
                                                    currentStep = LoginStep.PASSWORD
                                                }
                                            },
                                            enabled = emailId.isNotBlank(),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(52.dp),
                                            shape = RoundedCornerShape(26.dp),
                                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFE50914),
                                                contentColor = Color.White,
                                                disabledContainerColor = Color(0xFFE50914),
                                                disabledContentColor = Color.White
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Spacer(modifier = Modifier.width(20.dp))
                                                Text(
                                                    text = "Continue",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "➔",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            LoginStep.PASSWORD -> {
                                // STEP 2: PASSWORD ENTRY FLOATING WHITE CARD
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(24.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Enter Password",
                                                style = MaterialTheme.typography.headlineLarge.copy(
                                                    fontSize = 26.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF0F172A)
                                                )
                                            )
                                            Text(
                                                text = "← Change",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = DeskStreamRed,
                                                modifier = Modifier.clickable {
                                                    currentStep = LoginStep.EMAIL
                                                }
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "Logging in as ${emailId.trim()}",
                                            fontSize = 14.sp,
                                            color = Color(0xFF64748B),
                                            fontWeight = FontWeight.Medium
                                        )

                                        Spacer(modifier = Modifier.height(28.dp))

                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.Start
                                        ) {
                                            Text(
                                                text = "Password",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF64748B)
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            OutlinedTextField(
                                                value = password,
                                                onValueChange = { password = it },
                                                placeholder = {
                                                    Text(
                                                        text = "Enter your password",
                                                        color = Color(0xFF94A3B8),
                                                        fontSize = 14.sp
                                                    )
                                                },
                                                singleLine = true,
                                                leadingIcon = {
                                                    Text(
                                                        text = "🔒",
                                                        fontSize = 18.sp,
                                                        modifier = Modifier.padding(start = 10.dp, end = 4.dp)
                                                    )
                                                },
                                                trailingIcon = {
                                                    Text(
                                                        text = "👁",
                                                        fontSize = 18.sp,
                                                        modifier = Modifier
                                                            .alpha(if (isPasswordVisible) 1.0f else 0.45f)
                                                            .clickable {
                                                                isPasswordVisible = !isPasswordVisible
                                                            }
                                                    )
                                                },
                                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(54.dp),
                                                shape = RoundedCornerShape(14.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Color(0xFFE2E8F0),
                                                    unfocusedBorderColor = Color(0xFFF1F5F9),
                                                    focusedContainerColor = Color(0xFFF8FAFC),
                                                    unfocusedContainerColor = Color(0xFFF8FAFC),
                                                    focusedTextColor = Color(0xFF0F172A),
                                                    unfocusedTextColor = Color(0xFF0F172A)
                                                )
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Box(
                                                modifier = Modifier.fillMaxWidth(),
                                                contentAlignment = Alignment.CenterEnd
                                            ) {
                                                Text(
                                                    text = "Forgot Password?",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DeskStreamRed,
                                                    modifier = Modifier.clickable { onForgotPasswordClick() }
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))

                                        Button(
                                            onClick = {
                                                if (password.isNotBlank()) {
                                                    onLoginSuccess(emailId.trim(), password)
                                                }
                                            },
                                            enabled = !isLoading && password.isNotBlank(),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(52.dp),
                                            shape = RoundedCornerShape(26.dp),
                                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFE50914),
                                                contentColor = Color.White,
                                                disabledContainerColor = Color(0xFFE50914),
                                                disabledContentColor = Color.White
                                            )
                                        ) {
                                            if (isLoading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(22.dp),
                                                    color = Color.White,
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 12.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Spacer(modifier = Modifier.width(20.dp))
                                                    Text(
                                                        text = "Sign In",
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                    Text(
                                                        text = "➔",
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Show API error message & in-app diagnostic details from ViewModel
                val errorMsg = loginUiState.errorMessage
                if (!errorMsg.isNullOrBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFEE2E2)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = errorMsg,
                                    color = Color(0xFFB91C1C),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "✕",
                                    color = Color(0xFFB91C1C),
                                    fontSize = 16.sp,
                                    modifier = Modifier.clickable { onClearError() }
                                )
                            }

                            // Structured debug diagnostic trace for real-time connection debugging
                            val debugDetails = loginUiState.debugDetails
                            if (!debugDetails.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    color = Color(0xFFFEF2F2),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "DEBUG TRACE & CONNECTION INFO",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF991B1B),
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = debugDetails,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFF7F1D1D),
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Footer Branding ("POWERED BY Friends Software Solutions")
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth(0.6f)
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFE50914)
                        )
                        Text(
                            text = "  POWERED BY  ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF718096),
                                letterSpacing = 2.sp
                            )
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFE50914)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Friends Software Solutions",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    DeskStreamTheme {
        LoginScreen()
    }
}
