package com.supdevinci.cooktail.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.supdevinci.cooktail.R

// Police personnalisée chargée depuis le dossier res/font
val CocktailFontFamily = FontFamily(
    Font(R.font.typococktail, FontWeight.Normal),
    Font(R.font.typococktail, FontWeight.Bold)
)

val CookTailTypography = Typography(
    // Grands titres - Utilisation de la police typococktail
    displayLarge = TextStyle(
        fontFamily = CocktailFontFamily,
        fontSize = 34.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    ),
    displayMedium = TextStyle(
        fontFamily = CocktailFontFamily,
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp
    ),
    displaySmall = TextStyle(
        fontFamily = CocktailFontFamily,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
    ),

    // Titres de sections
    headlineLarge = TextStyle(
        fontFamily = CocktailFontFamily,
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = CocktailFontFamily,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.4.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = CocktailFontFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.3.sp
    ),

    // Corps de texte (On garde une police système pour la lisibilité)
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.4.sp
    ),

    // Labels
    labelLarge = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.25.sp
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Light,
        letterSpacing = 1.5.sp
    )
)