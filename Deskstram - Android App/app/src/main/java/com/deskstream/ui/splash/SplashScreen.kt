package com.deskstream.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deskstream.R
import com.deskstream.ui.theme.DeskStreamTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit = {}
) {
    val isPreview = LocalInspectionMode.current

    // Initial states fully visible so there is zero blank screen delay
    val logoOffsetX = remember { Animatable(0f) }
    val logoScale = remember { Animatable(1f) }
    val logoRotation = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(1f) }

    // Animation States for Title, Tagline, Loader & Footer
    val textAlpha = remember { Animatable(1f) }
    val textOffsetY = remember { Animatable(0f) }
    val footerAlpha = remember { Animatable(1f) }

    // Continuous subtle breathing pulse for center logo
    val infiniteTransition = rememberInfiniteTransition(label = "logoPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LaunchedEffect(Unit) {
        if (!isPreview) {
            // Hold splash screen for 3.5 seconds before navigating to login
            delay(3500)
            onSplashFinished()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Splash Screen Background
            Image(
                painter = painterResource(id = R.drawable.splash_screen_fix),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // 2. Center Content (Logo + Typography + Loader Badge)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                // Logo 'D'
                Box(
                    modifier = Modifier
                        .offset(x = logoOffsetX.value.dp)
                        .scale(logoScale.value * if (isPreview) 1f else pulseScale)
                        .rotate(logoRotation.value)
                        .alpha(logoAlpha.value),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.desk_stream_d),
                        contentDescription = "DeskStream Logo D",
                        modifier = Modifier.size(185.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Brand Name: "Desk" + "Stream"
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .offset(y = textOffsetY.value.dp)
                        .alpha(textAlpha.value)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Desk",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 42.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A),
                                letterSpacing = (-0.5).sp
                            )
                        )
                        Text(
                            text = "Stream",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 42.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFE50914),
                                letterSpacing = (-0.5).sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Seamless Remote Access & Control",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569)
                        )
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Status Loader Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFFFFF0F0))
                            .border(1.dp, Color(0xFFFEE2E2), RoundedCornerShape(24.dp))
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color(0xFFE50914),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Establishing Secure Connection",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }

            // 3. Bottom Company Credits
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 60.dp)
                    .alpha(footerAlpha.value)
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
                            color = Color(0xFF475569),
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

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    DeskStreamTheme {
        SplashScreen()
    }
}
