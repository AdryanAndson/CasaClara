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
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import br.com.casaclara.R
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
    val Papel = Color(0xFFFFF6E3)
    val Roxo = Color(0xFF8B6CEF)
    val Rosa = Color(0xFFFF8FB1)
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
    surfaceVariant = Color(0xFFF4EEDF), onSurfaceVariant = Color(0xFF3D4B58), outline = Brand.Tinta, outlineVariant = Color(0xFFE2D9C6),
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

/** Baloo 2 (títulos, arredondada e expressiva) + Nunito (textos, muito legível). Licença OFL. */
val Baloo = FontFamily(Font(R.font.baloo2_bold, FontWeight.Bold), Font(R.font.baloo2_extrabold, FontWeight.ExtraBold), Font(R.font.baloo2_extrabold, FontWeight.Black))
val Nunito = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal), Font(R.font.nunito_semibold, FontWeight.SemiBold), Font(R.font.nunito_semibold, FontWeight.Medium),
    Font(R.font.nunito_bold, FontWeight.Bold), Font(R.font.nunito_extrabold, FontWeight.ExtraBold), Font(R.font.nunito_extrabold, FontWeight.Black),
)

private fun appTypography(classic: Boolean) = Typography().let { t ->
    val Baloo = if (classic) FontFamily.Default else Baloo
    val Nunito = if (classic) FontFamily.Default else Nunito
    fun TextStyle.body() = copy(fontFamily = Nunito, letterSpacing = 0.1.sp)
    fun TextStyle.head(w: FontWeight) = copy(fontFamily = Baloo, fontWeight = w, letterSpacing = 0.sp)
    t.copy(
        displayLarge = t.displayLarge.head(FontWeight.ExtraBold), displayMedium = t.displayMedium.head(FontWeight.ExtraBold), displaySmall = t.displaySmall.head(FontWeight.ExtraBold),
        headlineLarge = t.headlineLarge.head(FontWeight.ExtraBold).copy(fontSize = 32.sp, lineHeight = 36.sp),
        headlineMedium = t.headlineMedium.head(FontWeight.ExtraBold).copy(fontSize = 28.sp, lineHeight = 32.sp),
        headlineSmall = t.headlineSmall.head(FontWeight.ExtraBold).copy(fontSize = 23.sp, lineHeight = 28.sp),
        titleLarge = t.titleLarge.head(FontWeight.ExtraBold).copy(fontSize = 21.sp, lineHeight = 26.sp),
        titleMedium = t.titleMedium.copy(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp),
        titleSmall = t.titleSmall.copy(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp),
        bodyLarge = t.bodyLarge.body().copy(fontSize = 17.sp, lineHeight = 25.sp, letterSpacing = 0.15.sp),
        bodyMedium = t.bodyMedium.body().copy(fontSize = 15.sp, lineHeight = 22.sp),
        bodySmall = t.bodySmall.body().copy(fontSize = 14.sp, lineHeight = 20.sp),
        labelLarge = t.labelLarge.copy(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp),
        labelSmall = t.labelSmall.copy(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp),
        labelMedium = t.labelMedium.copy(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 0.15.sp),
    )
}
/** Números grandes: dígitos com a mesma largura (tnum) para valores alinhados e sem "pular" ao mudar. */
val BigNumber = TextStyle(fontFamily = Baloo, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 32.sp, fontFeatureSettings = "tnum")

/** Cores de destaque escolhidas nas configurações: (claro, contêiner claro, escuro, contêiner escuro). */
val ACCENTS = linkedMapOf(
    "VERDE" to listOf(0xFF137A52, 0xFFD9F5E8, 0xFF6FD8A8, 0xFF174A36),
    "AZUL" to listOf(0xFF1F5FCF, 0xFFDFE9FF, 0xFF9DBBFF, 0xFF1F3561),
    "ROXO" to listOf(0xFF6247D1, 0xFFEFE6FF, 0xFFC6B5FF, 0xFF3A2D66),
    "CORAL" to listOf(0xFFC63D32, 0xFFFFE2DE, 0xFFFFB4A9, 0xFF5A2A25),
    "ROSA" to listOf(0xFFB0336C, 0xFFFFE0EC, 0xFFFFB0CF, 0xFF5C2440),
    "LARANJA" to listOf(0xFFB35300, 0xFFFFE6CC, 0xFFFFB77A, 0xFF5A3210),
)
val ACCENT_LABELS = mapOf("VERDE" to "Verde", "AZUL" to "Azul", "ROXO" to "Roxo", "CORAL" to "Coral", "ROSA" to "Rosa", "LARANJA" to "Laranja")

@Composable
fun CasaClaraTheme(mode: ThemeMode, textScale: Float, accent: String = "VERDE", classicFont: Boolean = false, content: @Composable () -> Unit) {
    val dark = when (mode) { ThemeMode.SISTEMA -> isSystemInDarkTheme(); ThemeMode.CLARO -> false; ThemeMode.ESCURO -> true }
    val soft = if (dark) SoftColors(Color(0xFF173D2F), Color(0xFF41391A), Color(0xFF1E2F52), Color(0xFF4A2622), Color(0xFF33284A), Color(0xFF8C99A6))
    else SoftColors(Color(0xFFC4F1DC), Color(0xFFFFEDA6), Color(0xFFCFE0FF), Color(0xFFFFD3CC), Color(0xFFE6DBFF), Brand.Tinta)
    val d = LocalDensity.current
    CompositionLocalProvider(
        LocalSoft provides soft,
        LocalDark provides dark,
        LocalDensity provides Density(d.density, d.fontScale * textScale.coerceIn(0.85f, 1.6f)),
    ) {
        MaterialTheme(
            colorScheme = run {
                val a = ACCENTS[accent] ?: ACCENTS.getValue("VERDE")
                if (dark) Dark.copy(primary = Color(a[2]), primaryContainer = Color(a[3]))
                else Light.copy(primary = Color(a[0]), primaryContainer = Color(a[1]))
            },
            typography = remember(classicFont) { appTypography(classicFont) },
            shapes = Shapes(small = RoundedCornerShape(10.dp), medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(22.dp)),
            content = content,
        )
    }
}
