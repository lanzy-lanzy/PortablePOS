package dev.ml.portablepos.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.SpaceDashboard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ml.portablepos.ui.theme.PrimaryBlue

private val ActiveBlue = PrimaryBlue
private val InactiveGray = Color(0xFF9CA3AF)
private val BarWhite = Color.White

val portableBottomNavItems = listOf(
    BottomNavItem("Dashboard", Icons.Filled.SpaceDashboard, Icons.Outlined.SpaceDashboard, Screen.Dashboard.route),
    BottomNavItem("Products", Icons.Filled.Inventory2, Icons.Outlined.Inventory2, Screen.ProductList.route),
    BottomNavItem("Sale", Icons.Filled.PointOfSale, Icons.Outlined.PointOfSale, Screen.POS.route),
    BottomNavItem("History", Icons.Filled.History, Icons.Outlined.History, Screen.SalesHistory.route),
    BottomNavItem("Reports", Icons.Filled.BarChart, Icons.Outlined.BarChart, Screen.Reports.route)
)

val portableShowBottomNavRoutes = setOf(
    Screen.Dashboard.route,
    Screen.ProductList.route,
    Screen.POS.route,
    Screen.SalesHistory.route,
    Screen.Reports.route
)

@Composable
fun PortableBottomBar(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val centerIndex = 2
    val isCenterSelected = selectedIndex == centerIndex
    val leftItems = items.take(centerIndex)
    val rightItems = items.drop(centerIndex + 1)
    val centerItem = items.getOrNull(centerIndex)

    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val safeBottomPadding = if (bottomPadding > 0.dp) bottomPadding else 12.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = safeBottomPadding
                )
                .height(64.dp),
            color = BarWhite,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, Color(0xFFF3F4F6))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leftItems.forEachIndexed { index, item ->
                    BottomNavTab(
                        item = item,
                        isSelected = selectedIndex == index,
                        onClick = { onItemSelected(index) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.width(68.dp))

                rightItems.forEachIndexed { index, item ->
                    val actualIndex = centerIndex + 1 + index
                    BottomNavTab(
                        item = item,
                        isSelected = selectedIndex == actualIndex,
                        onClick = { onItemSelected(actualIndex) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (centerItem != null) {
            val centerScale by animateFloatAsState(
                targetValue = if (isCenterSelected) 1.12f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "centerScale"
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 4.dp)
                    .size(60.dp)
                    .graphicsLayer(scaleX = centerScale, scaleY = centerScale)
                    .shadow(8.dp, CircleShape, clip = false)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = if (isCenterSelected) {
                                listOf(Color(0xFF1E88E5), ActiveBlue)
                            } else {
                                listOf(Color(0xFF42A5F5), Color(0xFF1E88E5))
                            }
                        )
                    )
                    .border(3.5.dp, BarWhite, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onItemSelected(centerIndex) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = centerItem.selectedIcon,
                    contentDescription = centerItem.label,
                    modifier = Modifier.size(28.dp),
                    tint = BarWhite
                )
            }
        }
    }
}

@Composable
private fun BottomNavTab(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) ActiveBlue else InactiveGray,
        animationSpec = tween(200),
        label = "iconTint"
    )

    val labelColor by animateColorAsState(
        targetValue = if (isSelected) ActiveBlue else InactiveGray,
        animationSpec = tween(200),
        label = "labelColor"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconScale"
    )

    val indicatorScale by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "indicatorScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .height(56.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Icon(
            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.label,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer(scaleX = iconScale, scaleY = iconScale),
            tint = iconTint
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                lineHeight = 12.sp,
                letterSpacing = 0.2.sp,
                textAlign = TextAlign.Center
            ),
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(3.dp))

        Box(
            modifier = Modifier
                .size(4.dp)
                .graphicsLayer(scaleX = indicatorScale, scaleY = indicatorScale)
                .background(ActiveBlue, CircleShape)
        )
    }
}

@Composable
fun SamplePortableBottomBarUsage() {
    var selectedIndex by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            PortableBottomBar(
                items = bottomNavItems,
                selectedIndex = selectedIndex,
                onItemSelected = { selectedIndex = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Selected: ${bottomNavItems[selectedIndex].label}",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
