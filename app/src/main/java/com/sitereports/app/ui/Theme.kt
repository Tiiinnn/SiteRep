package com.sitereports.app.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sitereports.app.R

private val WhiteColors = lightColorScheme(
    primary = Color(0xFF111111),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF2F2F2),
    onPrimaryContainer = Color(0xFF111111),
    secondary = Color(0xFF606060),
    onSecondary = Color.White,
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Color(0xFFF5F5F5),
    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerLowest = Color.White,
    surfaceContainerHigh = Color(0xFFF7F7F7),
    surfaceContainerHighest = Color(0xFFF2F2F2),
    onSurface = Color(0xFF111111),
    onSurfaceVariant = Color(0xFF5F5F5F),
    outline = Color(0xFF2A2A2A),
    outlineVariant = Color(0xFFE3E3E3),
    error = Color(0xFFB3261E),
)

private val CleanShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(2.dp),
    medium = RoundedCornerShape(3.dp),
    large = RoundedCornerShape(4.dp),
    extraLarge = RoundedCornerShape(6.dp),
)

private val BebasNeue = FontFamily(Font(R.font.bebas_neue_regular, FontWeight.Normal))
private val InterTight = FontFamily(Font(R.font.inter_tight, FontWeight.Normal))
private val DefaultTypography = Typography()

private fun TextStyle.withBebasNeue(sizeIncreaseSp: Float) = copy(
    fontFamily = BebasNeue,
    fontSize = (fontSize.value + sizeIncreaseSp).sp,
    lineHeight = (lineHeight.value + sizeIncreaseSp).sp,
)

private val SiteRepTypography = Typography(
    displayLarge = DefaultTypography.displayLarge.withBebasNeue(8f),
    displayMedium = DefaultTypography.displayMedium.withBebasNeue(8f),
    displaySmall = DefaultTypography.displaySmall.withBebasNeue(8f),
    headlineLarge = DefaultTypography.headlineLarge.withBebasNeue(8f),
    headlineMedium = DefaultTypography.headlineMedium.withBebasNeue(8f),
    headlineSmall = DefaultTypography.headlineSmall.withBebasNeue(8f),
    titleLarge = DefaultTypography.titleLarge.withBebasNeue(8f),
    titleMedium = DefaultTypography.titleMedium.withBebasNeue(8f),
    titleSmall = DefaultTypography.titleSmall.withBebasNeue(8f),
    labelLarge = DefaultTypography.labelLarge.withBebasNeue(4f),
    labelMedium = DefaultTypography.labelMedium.withBebasNeue(4f),
    labelSmall = DefaultTypography.labelSmall.withBebasNeue(4f),
    bodyLarge = DefaultTypography.bodyLarge.copy(fontFamily = InterTight),
    bodyMedium = DefaultTypography.bodyMedium.copy(fontFamily = InterTight),
    bodySmall = DefaultTypography.bodySmall.copy(fontFamily = InterTight),
)

@Composable
fun SiteReportsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WhiteColors,
        shapes = CleanShapes,
        typography = SiteRepTypography,
        content = content,
    )
}
