package dev.ml.portablepos.presentation.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import dev.ml.portablepos.presentation.components.ErrorMessage
import dev.ml.portablepos.presentation.components.LoadingIndicator
import dev.ml.portablepos.presentation.navigation.Screen
import dev.ml.portablepos.ui.theme.PrimaryBlue
import dev.ml.portablepos.ui.theme.SecondaryTeal
import dev.ml.portablepos.ui.theme.SuccessGreen
import dev.ml.portablepos.ui.theme.WarningOrange
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val color: Color
)

private val quickActions = listOf(
    QuickAction("New Sale", Icons.Default.PointOfSale, Screen.POS.route, PrimaryBlue),
    QuickAction("Scan", Icons.Default.QrCodeScanner, Screen.Scanner.createRoute("SALE"), SecondaryTeal),
    QuickAction("Add Product", Icons.Default.Add, Screen.AddProduct.createRoute(), SuccessGreen),
    QuickAction("Inventory", Icons.Default.Inventory, Screen.Inventory.route, WarningOrange),
    QuickAction("History", Icons.Default.History, Screen.SalesHistory.route, Color(0xFF7B1FA2)),
    QuickAction("Reports", Icons.Default.BarChart, Screen.Reports.route, Color(0xFFE91E63))
)

private val gradientStart = Color(0xFF0D47A1)
private val gradientEnd = Color(0xFF1976D2)

@Composable
fun DashboardScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showContent by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(!uiState.isLoading) {
        if (!uiState.isLoading) {
            showContent = true
        }
    }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) isRefreshing = false
    }

    Scaffold(
        containerColor = Color(0xFFF0F2F5)
    ) { paddingValues ->
        when {
            uiState.isLoading && !isRefreshing -> LoadingIndicator()
            uiState.error != null && !isRefreshing -> ErrorMessage(
                message = uiState.error!!,
                onRetry = { viewModel.loadDashboard() }
            )
            else -> {
                val data = uiState.data
                val topInset = paddingValues.calculateTopPadding()
                val bottomInset = paddingValues.calculateBottomPadding()

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        isRefreshing = true
                        viewModel.loadDashboard()
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(gradientStart, gradientEnd)
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = topInset + 8.dp)
                                .padding(horizontal = 20.dp)
                                .padding(bottom = 24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = Color.White
                                    )
                                }
                            }

                            Text(
                                text = getGreeting(),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "PortablePOS",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
                            Text(
                                text = dateFormat.format(Date()),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard(
                                    value = "₱${String.format("%.2f", data.todaySales)}",
                                    label = "Today's Sales",
                                    icon = Icons.Default.PointOfSale,
                                    gradientColors = listOf(Color(0xFF1565C0), Color(0xFF1E88E5)),
                                    modifier = Modifier.weight(1f),
                                    show = showContent,
                                    delay = 0
                                )
                                StatCard(
                                    value = data.transactionCount.toString(),
                                    label = "Transactions",
                                    icon = Icons.Default.History,
                                    gradientColors = listOf(Color(0xFF00897B), Color(0xFF26A69A)),
                                    modifier = Modifier.weight(1f),
                                    show = showContent,
                                    delay = 100
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard(
                                    value = data.lowStockCount.toString(),
                                    label = "Low Stock Items",
                                    icon = Icons.Default.Warning,
                                    gradientColors = listOf(Color(0xFFEF6C00), Color(0xFFFF9800)),
                                    modifier = Modifier.weight(1f),
                                    show = showContent,
                                    delay = 200
                                )
                                StatCard(
                                    value = data.totalProducts.toString(),
                                    label = "Total Products",
                                    icon = Icons.Default.Inventory,
                                    gradientColors = listOf(Color(0xFF4CAF50), Color(0xFF66BB6A)),
                                    modifier = Modifier.weight(1f),
                                    show = showContent,
                                    delay = 300
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        quickActions.take(3).forEachIndexed { index, action ->
                            ActionCard(
                                title = action.title,
                                icon = action.icon,
                                color = action.color,
                                onClick = { navController.navigate(action.route) },
                                modifier = Modifier.weight(1f),
                                show = showContent,
                                delay = 400 + index * 100
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        quickActions.drop(3).forEachIndexed { index, action ->
                            ActionCard(
                                title = action.title,
                                icon = action.icon,
                                color = action.color,
                                onClick = { navController.navigate(action.route) },
                                modifier = Modifier.weight(1f),
                                show = showContent,
                                delay = 700 + index * 100
                            )
                        }
                    }

                    if (data.lowStockCount > 0) {
                        Spacer(modifier = Modifier.height(24.dp))
                        LowStockAlertCard(
                            count = data.lowStockCount,
                            onClick = { navController.navigate(Screen.Inventory.route) },
                            show = showContent
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp + bottomInset))
                }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    show: Boolean,
    delay: Int
) {
    val animAlpha by animateFloatAsState(
        targetValue = if (show) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = delay)
    )
    val animTranslation by animateFloatAsState(
        targetValue = if (show) 0f else 30f,
        animationSpec = tween(durationMillis = 600, delayMillis = delay)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(animAlpha)
            .offset(y = animTranslation.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(gradientColors.first().copy(alpha = 0.12f))
                    .padding(8.dp),
                tint = gradientColors.first()
            )
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = gradientColors.first()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    show: Boolean,
    delay: Int
) {
    val animAlpha by animateFloatAsState(
        targetValue = if (show) 1f else 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = delay)
    )
    val animTranslation by animateFloatAsState(
        targetValue = if (show) 0f else 30f,
        animationSpec = tween(durationMillis = 500, delayMillis = delay)
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .alpha(animAlpha)
            .offset(y = animTranslation.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    tint = color
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF444444),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LowStockAlertCard(
    count: Int,
    onClick: () -> Unit,
    show: Boolean
) {
    AnimatedVisibility(
        visible = show,
        enter = fadeIn() + slideInVertically { it / 2 }
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFF3E0)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(WarningOrange.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = WarningOrange,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Low Stock Alert",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFFE65100)
                    )
                    Text(
                        text = "$count product${if (count > 1) "s" else ""} need${if (count == 1) "s" else ""} to be restocked",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFBF360C)
                    )
                }
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = "View Inventory",
                    tint = WarningOrange,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..20 -> "Good evening"
        else -> "Good evening"
    }
}
