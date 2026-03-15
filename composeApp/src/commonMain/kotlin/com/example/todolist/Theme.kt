package com.example.todolist

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


// Custom Typography set
val AppTypography = Typography(
    // Used for your "Ktor Task Manager" title
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        letterSpacing = 1.sp
    ),
    // Used for the task titles
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp
    )
)


// Custom Colors
val SpaceDarkBackground = Color(0xFF0B0D17) // Deep space black/blue
val NebulaPurple = Color(0xFF9D4EDD)        // Vibrant purple for buttons/accents
val StarWhite = Color(0xFFF8F9FA)           // Crisp white for text

val SkyLightBackground = Color(0xFFF4F6F8)  // Soft daylight gray/white
val HorizonBlue = Color(0xFF023E8A)         // Deep blue for light mode accents
val EclipseBlack = Color(0xFF1A1A1A)        // Dark text for light mode

// Dark Theme
private val DarkThemeColors = darkColorScheme(
    primary = NebulaPurple,
    background = SpaceDarkBackground,
    surface = SpaceDarkBackground, // The color of Cards and Dialogs
    onPrimary = StarWhite,         // Text color on top of primary buttons
    onBackground = StarWhite,      // General text color
    onSurface = StarWhite
)

// Light Theme
private val LightThemeColors = lightColorScheme(
    primary = HorizonBlue,
    background = SkyLightBackground,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = EclipseBlack,
    onSurface = EclipseBlack
)

@Composable
fun TaskAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkThemeColors else LightThemeColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}