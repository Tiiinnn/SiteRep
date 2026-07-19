package com.sitereports.app.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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

@Composable
fun SiteReportsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WhiteColors,
        shapes = CleanShapes,
        content = content,
    )
}
