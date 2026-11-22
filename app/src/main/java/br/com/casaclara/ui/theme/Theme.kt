package br.com.casaclara.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casaclara.data.ThemeMode

object Brand {
    val Verde = Color(0xFF1F9D6B)
    val VerdeEscuro = Color(0xFF137A52)
    val Amarelo = Color(0xFFFFD23F)
    val Azul = Color(0xFF2F6FDF)
    val Coral = Color(0xFFFF6B5B)
    val CoralEscuro = Color(0xFFC63D32)
    val Tinta = Color(0xFF1C2B39)
    val Papel = Color(0xFFFFFAF0)
    val Bochecha = Color(0xFFFF8F84)
}

/** Cores de apoio que mudam no tema escuro (fundos suaves de cartões e etiquetas). */
data class SoftColors(val verde: Color, val amarelo: Color, val azul: Color, val coral: Color, val roxo: Color, val contorno: Color)
val LocalSoft = staticCompositionLocalOf { SoftColors(Color(0xFFD9F5E8), Color(0xFFFFF4C7), Color(0xFFDFE9FF), Color(0xFFFFE2DE), Color(0xFFEFE6FF), Brand.Tinta) }
val LocalDark = staticCompositionLocalOf { false }

private val Light = lightColorScheme(
    primary = Brand.VerdeEscuro, onPrimary = Color.White, primaryContainer = Color(0xFFD9F5E8), onPrimaryContainer = Brand.Tinta,
    secondary = Brand.Azul, onSecondary = Color.White, secondaryContainer = Color(0xFFDFE9FF), onSecondaryContainer = Brand.Tinta,
    tertiary = Brand.CoralEscuro, onTertiary = Color.White, tertiaryContainer = Color(0xFFFFE2DE), onTertiaryContainer = Brand.Tinta,
    background = Brand.Papel, onBackground = Brand.Tinta, surface = Color.White, onSurface = Brand.Tinta,
    surfaceVariant = Color(0xFFF4EEDF), onSurfaceVariant = Color(0xFF4A5A68), outline = Brand.Tinta, outlineVariant = Color(0xFFE2D9C6),
    error = Color(0xFFB3261E), onError = Color.White, surfaceContainer = Color(0xFFFFF7E8), surfaceContainerHigh = Color(0xFFFFF3DF),
)
private val Dark = darkColorScheme(
    primary = Color(0xFF6FD8A8), onPrimary = Color(0xFF00391F), primaryContainer = Color(0xFF174A36), onPrimaryContainer = Color(0xFFD9F5E8),
    secondary = Color(0xFF9DBBFF), onSecondary = Color(0xFF0B2C66), secondaryContainer = Color(0xFF1F3561), onSecondaryContainer = Color(0xFFDFE9FF),
    tertiary = Color(0xFFFFB4A9), onTertiary = Color(0xFF5F150E), tertiaryContainer = Color(0xFF5A2A25), onTertiaryContainer = Color(0xFFFFE2DE),
    background = Color(0xFF141B22), onBackground = Color(0xFFEDE7DA), surface = Color(0xFF1C252E), onSurface = Color(0xFFEDE7DA),
    surfaceVariant = Color(0xFF2A3540), onSurfaceVariant = Color(0xFFC4CCD4), outline = Color(0xFF8C99A6), outlineVariant = Color(0xFF3A4652),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005), surfaceContainer = Color(0xFF202A34), surfaceContainerHigh = Color(0xFF26313C),
)

private val AppTypography = Typography().let { t ->
    t.copy(
        headlineLarge = t.headlineLarge.copy(fontWeight = FontWeight.Black, fontSize = 30.sp),
        headlineMedium = t.headlineMedium.copy(fontWeight = FontWeight.Black, fontSize = 26.sp),
        headlineSmall = t.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 22.sp),
        titleLarge = t.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
        titleMedium = t.titleMedium.copy(fontWeight = FontWeight.Bold),
        titleSmall = t.titleSmall.copy(fontWeight = FontWeight.Bold),
        bodyLarge = t.bodyLarge.copy(fontSize = 17.sp, lineHeight = 25.sp),
        bodyMedium = t.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
        labelLarge = t.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
    )
}
val BigNumber = TextStyle(fontWeight = FontWeight.Black, fontSize = 26.sp, letterSpacing = (-0.5).sp)

@Composable
fun CasaClaraTheme(mode: ThemeMode, textScale: Float, content: @Composable () -> Unit) {
    val dark = when (mode) { ThemeMode.SISTEMA -> isSystemInDarkTheme(); ThemeMode.CLARO -> false; ThemeMode.ESCURO -> true }
    val soft = if (dark) SoftColors(Color(0xFF173D2F), Color(0xFF41391A), Color(0xFF1E2F52), Color(0xFF4A2622), Color(0xFF33284A), Color(0xFF8C99A6))
    else SoftColors(Color(0xFFD9F5E8), Color(0xFFFFF4C7), Color(0xFFDFE9FF), Color(0xFFFFE2DE), Color(0xFFEFE6FF), Brand.Tinta)
    val d = LocalDensity.current
    CompositionLocalProvider(
        LocalSoft provides soft,
        LocalDark provides dark,
        LocalDensity provides Density(d.density, d.fontScale * textScale.coerceIn(0.85f, 1.6f)),
    ) {
        MaterialTheme(
            colorScheme = if (dark) Dark else Light,
            typography = AppTypography,
            shapes = Shapes(small = RoundedCornerShape(10.dp), medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(22.dp)),
            content = content,
        )
    }
}
