package kr.ilf.soodal.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kr.ilf.soodal.R


val tiny5 = FontFamily(
    Font(R.font.tiny5_regular)
)
val notoSansKr = FontFamily(
    Font(R.font.noto_sans_kr_thin, FontWeight.Thin, FontStyle.Normal),
    Font(R.font.noto_sans_kr_extra_light, FontWeight.ExtraLight, FontStyle.Normal),
    Font(R.font.noto_sans_kr_light, FontWeight.Light, FontStyle.Normal),
    Font(R.font.noto_sans_kr_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.noto_sans_kr_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.noto_sans_kr_semi_bold, FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.noto_sans_kr_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.noto_sans_kr_extra_bold, FontWeight.ExtraBold, FontStyle.Normal),
    Font(R.font.noto_sans_kr_black, FontWeight.Black, FontStyle.Normal)
)

// Set of Material typography styles to start with
val fontFamily = notoSansKr
val DefaultTypography = Typography()

internal val Typography = Typography(
    displayLarge = DefaultTypography.displayLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium),
    displayMedium = DefaultTypography.displayMedium.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium),
    displaySmall = DefaultTypography.displaySmall.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium),
    headlineLarge = DefaultTypography.headlineLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium),
    headlineMedium = DefaultTypography.headlineMedium.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium),
    headlineSmall = DefaultTypography.headlineSmall.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium),
    titleLarge = DefaultTypography.titleLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium),
    titleMedium = DefaultTypography.titleMedium.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium),
    titleSmall = DefaultTypography.titleSmall.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium),
    bodyLarge = DefaultTypography.bodyLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium), // default
    bodyMedium = DefaultTypography.bodyMedium.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium),
    bodySmall = DefaultTypography.bodySmall.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium),
    labelLarge = DefaultTypography.labelLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium),
    labelMedium = DefaultTypography.labelMedium.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium),
    labelSmall = DefaultTypography.labelSmall.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium),
)

val Typography.splashTitle: TextStyle
    get() = TextStyle(
        fontFamily = notoSansKr,
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp,
        lineHeight = 30.sp,
        letterSpacing = 7.sp,
    )
//val Typography = Typography(
//    bodyLarge = TextStyle(
//        fontFamily = notoSansKr,
//        fontWeight = FontWeight.Bold,
//        fontSize = 16.sp,
//        lineHeight = 24.sp,
//        letterSpacing = 0.5.sp
//    ),
//
//    headlineLarge = TextStyle(
//        fontFamily = notoSansKr,
//        fontWeight = FontWeight.Normal,
//        fontSize = 30.sp,
//        lineHeight = 30.sp,
//        letterSpacing = 1.sp,
//    ),
//
//    titleLarge = TextStyle(
//        fontFamily = notoSansKr,
//        fontWeight = FontWeight.Normal,
//        fontSize = 30.sp,
//        lineHeight = 30.sp,
//        letterSpacing = 7.sp,
//    ),
//
//    titleMedium = TextStyle(
//        fontFamily = notoSansKr,
//        fontWeight = FontWeight.Normal,
//        fontSize = 24.sp,
//        lineHeight = 24.sp,
//        letterSpacing = 0.5.sp,
//    ),
//
//    titleSmall = TextStyle(
//        fontFamily = notoSansKr,
//        fontWeight = FontWeight.Normal,
//        fontSize = 12.sp,
//        lineHeight = 12.sp,
//        letterSpacing = 0.5.sp,
//    ),
//
//    labelSmall = TextStyle(
//        fontFamily = notoSansKr,
//        fontWeight = FontWeight.Medium,
//        fontSize = 10.sp,
//        lineHeight = 8.sp,
//        letterSpacing = 0.5.sp,
//    )
// )

data class SoodalTypography(
    private val fontFamily: FontFamily = notoSansKr,
    val displayLarge: TextStyle = DefaultTypography.displayLarge.copy(fontFamily = fontFamily),
    val displayMedium: TextStyle = DefaultTypography.displayMedium.copy(fontFamily = fontFamily),
    val displaySmall: TextStyle = DefaultTypography.displaySmall.copy(fontFamily = fontFamily),
    val headlineLarge: TextStyle = DefaultTypography.headlineLarge.copy(fontFamily = fontFamily),
    val headlineMedium: TextStyle = DefaultTypography.headlineMedium.copy(fontFamily = fontFamily),
    val headlineSmall: TextStyle = DefaultTypography.headlineSmall.copy(fontFamily = fontFamily),
    val titleLarge: TextStyle = DefaultTypography.titleLarge.copy(fontFamily = fontFamily),
    val titleMedium: TextStyle = DefaultTypography.titleMedium.copy(fontFamily = fontFamily),
    val titleSmall: TextStyle = DefaultTypography.titleSmall.copy(fontFamily = fontFamily),
    val bodyLarge: TextStyle = DefaultTypography.bodyLarge.copy(fontFamily = fontFamily), // default
    val bodyMedium: TextStyle = DefaultTypography.bodyMedium.copy(fontFamily = fontFamily),
    val bodySmall: TextStyle = DefaultTypography.bodySmall.copy(fontFamily = fontFamily),
    val labelLarge: TextStyle = DefaultTypography.labelLarge.copy(fontFamily = fontFamily),
    val labelMedium: TextStyle = DefaultTypography.labelMedium.copy(fontFamily = fontFamily),
    val labelSmall: TextStyle = DefaultTypography.labelSmall.copy(fontFamily = fontFamily),

    val splashTitle: TextStyle = TextStyle(
        fontFamily = notoSansKr,
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp,
        lineHeight = 30.sp,
        letterSpacing = 7.sp,
    ),
)

val LocalSoodalTypography = staticCompositionLocalOf { Typography }