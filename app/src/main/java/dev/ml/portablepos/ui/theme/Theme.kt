package dev.ml.portablepos.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = OnPrimary,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = SecondaryTeal,
    onSecondary = OnSecondary,
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = Color(0xFF00201E),
    tertiary = TertiaryOrange,
    onTertiary = OnTertiary,
    tertiaryContainer = Color(0xFFFFDCC2),
    onTertiaryContainer = Color(0xFF2C1400),
    error = ErrorRed,
    onError = OnError,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = BackgroundLight,
    onBackground = OnBackground,
    surface = SurfaceWhite,
    onSurface = OnSurface,
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = OnSurfaceVariant,
    outline = Color(0xFF79747E)
)

data class AppColors(
    val success: Color = SuccessGreen,
    val warning: Color = WarningOrange,
    val outOfStock: Color = OutOfStockRed
)

val LocalAppColors = staticCompositionLocalOf { AppColors() }
