package com.equipo.sanmarkfood.restaurante.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.equipo.sanmarkfood.restaurante.R

@OptIn(ExperimentalTextApi::class)
private fun variable(resId: Int, weight: Int) = Font(
    resId = resId,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight))
)

val Bricolage = FontFamily(
    variable(R.font.bricolage_grotesque, 600),
    variable(R.font.bricolage_grotesque, 800),
)

val Figtree = FontFamily(
    variable(R.font.figtree, 400),
    variable(R.font.figtree, 500),
    variable(R.font.figtree, 600),
    variable(R.font.figtree, 700),
)

val Caveat = FontFamily(
    variable(R.font.caveat, 700),
)

val Typography = Typography(
    displayMedium = TextStyle(fontFamily = Bricolage, fontWeight = FontWeight.ExtraBold, fontSize = 40.sp, lineHeight = 40.sp, letterSpacing = (-1).sp),
    displaySmall = TextStyle(fontFamily = Bricolage, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp),
    headlineSmall = TextStyle(fontFamily = Bricolage, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = Bricolage, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, lineHeight = 27.sp, letterSpacing = (-0.5).sp),
    titleMedium = TextStyle(fontFamily = Bricolage, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp),
    titleSmall = TextStyle(fontFamily = Figtree, fontWeight = FontWeight.Bold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = Figtree, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = Figtree, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Figtree, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = Figtree, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
    labelMedium = TextStyle(fontFamily = Figtree, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.sp),
    labelSmall = TextStyle(fontFamily = Figtree, fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
)

val HandwrittenStyle = TextStyle(fontFamily = Caveat, fontWeight = FontWeight.Bold, fontSize = 26.sp)
