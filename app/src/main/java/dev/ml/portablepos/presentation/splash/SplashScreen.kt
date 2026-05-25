package dev.ml.portablepos.presentation.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import dev.ml.portablepos.presentation.navigation.Screen
import dev.ml.portablepos.ui.theme.PrimaryBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    navController: NavHostController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var startAnimation by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition()
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val arcProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2800)
        navController.navigate(Screen.Dashboard.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBlue)
            .clickable {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scanY = size.height * scanLineY
            drawLine(
                color = Color.White.copy(alpha = 0.12f),
                start = Offset(0f, scanY),
                end = Offset(size.width, scanY),
                strokeWidth = 2f
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer {
                        scaleX = if (startAnimation) pulseScale else 0.6f
                        scaleY = if (startAnimation) pulseScale else 0.6f
                        alpha = if (startAnimation) 1f else 0f
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2
                    val cy = size.height / 2
                    val outerR = size.minDimension / 2
                    val innerR = outerR * 0.78f

                    drawCircle(
                        color = Color.White.copy(alpha = 0.10f),
                        radius = outerR
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.06f),
                        radius = innerR
                    )

                    drawArc(
                        color = Color.White.copy(alpha = 0.25f),
                        startAngle = arcProgress,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = Offset(cx - innerR, cy - innerR),
                        size = Size(innerR * 2, innerR * 2),
                        style = Stroke(width = 2.5f)
                    )

                    val barWidth = size.width / 20
                    val barHeight = size.height * 0.4f
                    val startX = cx - barWidth * 3.5f
                    val gap = barWidth * 1.5f

                    for (i in 0 until 8) {
                        val barX = startX + i * gap
                        val h = barHeight * (0.45f + (i % 4) * 0.12f)
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(barX, cy - h / 2),
                            size = Size(barWidth * 0.5f, h),
                            cornerRadius = CornerRadius(barWidth * 0.25f)
                        )
                    }

                    val dotRadius = barWidth * 0.18f
                    val dotY = cy + barHeight * 0.55f
                    val dotStartX = cx - dotRadius * 6
                    val dotGap = dotRadius * 4
                    for (i in 0 until 4) {
                        val dotAlpha = when {
                            arcProgress in (i * 90f)..(i * 90f + 90f) -> 1f
                            else -> 0.3f
                        }
                        drawCircle(
                            color = Color.White.copy(alpha = dotAlpha),
                            radius = dotRadius,
                            center = Offset(dotStartX + i * dotGap, dotY)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "PortablePOS",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = if (startAnimation) 1f else 0f
                    translationY = if (startAnimation) 0f else 30f
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Mobile Point of Sale",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = 4.sp
                ),
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = if (startAnimation) 0.8f else 0f
                    translationY = if (startAnimation) 0f else 20f
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.graphicsLayer {
                    alpha = if (startAnimation) 1f else 0f
                }
            ) {
                for (i in 0 until 3) {
                    val dotDelay = i * 200
                    var dotVisible by remember { mutableStateOf(false) }

                    LaunchedEffect(startAnimation) {
                        if (startAnimation) {
                            delay(dotDelay.toLong())
                            dotVisible = true
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .alpha(if (dotVisible) 1f else 0.3f)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.6f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "Tap anywhere to continue",
                style = MaterialTheme.typography.bodySmall.copy(
                    letterSpacing = 1.sp
                ),
                color = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.graphicsLayer {
                    alpha = if (startAnimation) 0.6f else 0f
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
