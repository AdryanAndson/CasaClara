package br.com.casaclara.ui.components

import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.casaclara.ui.theme.Brand
import br.com.casaclara.ui.theme.LocalDark

/** Clarinha, a casinha mascote. Desenhada em Canvas, com humores e animação leve. */
enum class Mood(val label: String) {
    FELIZ("sorrindo"), COMEMORANDO("comemorando"), PENSANDO("pensando"), CONFUSA("confusa"),
    DICA("dando uma dica"), ACENANDO("acenando"), TRANQUILA("tranquila"),
}

@Composable
fun rememberReduceMotion(): Boolean {
    val ctx = LocalContext.current
    val system = remember {
        runCatching { Settings.Global.getFloat(ctx.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }.getOrDefault(false)
    }
    return system || br.com.casaclara.ui.UiFlags.reduceMotion.value
}

@Composable
fun Mascot(mood: Mood = Mood.FELIZ, size: Dp = 96.dp, modifier: Modifier = Modifier, describe: Boolean = true) {
    val reduce = rememberReduceMotion()
    val tr = rememberInfiniteTransition(label = "mascote")
    val bob by tr.animateFloat(
        initialValue = 0f, targetValue = if (reduce) 0f else 1f,
        animationSpec = infiniteRepeatable(tween(if (mood == Mood.COMEMORANDO) 380 else 1600, easing = LinearEasing), RepeatMode.Reverse), label = "bob",
    )
    val wave by tr.animateFloat(
        initialValue = 0f, targetValue = if (reduce) 0f else 1f,
        animationSpec = infiniteRepeatable(tween(650), RepeatMode.Reverse), label = "wave",
    )
    val ink = if (LocalDark.current) Color(0xFF0E151B) else Brand.Tinta
    val m = if (describe) modifier.semantics { contentDescription = "Clarinha, a casinha mascote, ${mood.label}" } else modifier
    Canvas(m.size(size)) {
        val s = this.size.minDimension / 124f
        scale(s, s, pivot = Offset.Zero) {
            val lift = if (mood == Mood.COMEMORANDO) -10f * bob else -4f * bob
            // sombra
            drawOval(ink.copy(alpha = 0.15f), topLeft = Offset(30f, 114f), size = Size(60f, 8f))
            translate(top = lift) { drawHouse(mood, ink, wave) }
            drawExtras(mood, ink)
        }
    }
}

private fun DrawScope.stroke(w: Float = 4f) = Stroke(width = w, cap = StrokeCap.Round, join = StrokeJoin.Round)

private fun DrawScope.arm(ink: Color, from: Offset, ctrl: Offset, to: Offset) {
    val p = Path().apply { moveTo(from.x, from.y); quadraticBezierTo(ctrl.x, ctrl.y, to.x, to.y) }
    drawPath(p, ink, style = stroke(5f))
    drawCircle(Color.White, 5.5f, to)
    drawCircle(ink, 5.5f, to, style = stroke(3.5f))
}

private fun DrawScope.drawHouse(mood: Mood, ink: Color, wave: Float) {
    // pés
    drawOval(ink, topLeft = Offset(38f, 105f), size = Size(16f, 10f))
    drawOval(ink, topLeft = Offset(66f, 105f), size = Size(16f, 10f))
    // braços
    val leftDown = { arm(ink, Offset(28f, 82f), Offset(18f, 88f), Offset(14f, 98f)) }
    val rightDown = { arm(ink, Offset(92f, 82f), Offset(102f, 88f), Offset(106f, 98f)) }
    when (mood) {
        Mood.COMEMORANDO -> { arm(ink, Offset(28f, 76f), Offset(16f, 66f), Offset(11f, 49f)); arm(ink, Offset(92f, 76f), Offset(104f, 66f), Offset(109f, 49f)) }
        Mood.ACENANDO -> { leftDown(); rotate(-22f * wave, pivot = Offset(92f, 78f)) { arm(ink, Offset(92f, 78f), Offset(104f, 70f), Offset(109f, 53f)) } }
        Mood.PENSANDO -> { leftDown(); arm(ink, Offset(92f, 84f), Offset(100f, 96f), Offset(78f, 90f)) }
        Mood.CONFUSA -> { arm(ink, Offset(28f, 80f), Offset(14f, 76f), Offset(10f, 63f)); arm(ink, Offset(92f, 80f), Offset(106f, 76f), Offset(110f, 63f)) }
        Mood.DICA -> { leftDown(); arm(ink, Offset(92f, 78f), Offset(104f, 68f), Offset(106f, 49f)) }
        else -> { leftDown(); rightDown() }
    }
    // chaminé
    drawRect(Brand.Coral, Offset(76f, 20f), Size(11f, 22f)); drawRect(ink, Offset(76f, 20f), Size(11f, 22f), style = stroke())
    // paredes
    drawRoundRect(Brand.Amarelo, Offset(26f, 50f), Size(68f, 58f), CornerRadius(10f))
    drawRoundRect(ink, Offset(26f, 50f), Size(68f, 58f), CornerRadius(10f), style = stroke())
    // telhado
    val roof = Path().apply { moveTo(16f, 58f); lineTo(60f, 18f); lineTo(104f, 58f); quadraticBezierTo(106f, 62f, 101f, 62f); lineTo(19f, 62f); quadraticBezierTo(14f, 62f, 16f, 58f); close() }
    drawPath(roof, Brand.Coral); drawPath(roof, ink, style = stroke())
    drawPath(Path().apply { moveTo(36f, 50f); lineTo(60f, 29f); lineTo(84f, 50f) }, Color.White.copy(alpha = 0.55f), style = stroke(3f))
    // porta
    drawRoundRect(Brand.Verde, Offset(52f, 90f), Size(16f, 18f), CornerRadius(6f))
    drawRoundRect(ink, Offset(52f, 90f), Size(16f, 18f), CornerRadius(6f), style = stroke(3.5f))
    drawCircle(ink, 1.6f, Offset(64f, 99f))
    // bochechas
    drawOval(Brand.Bochecha.copy(alpha = 0.8f), topLeft = Offset(33f, 77f), size = Size(10f, 6.4f))
    drawOval(Brand.Bochecha.copy(alpha = 0.8f), topLeft = Offset(77f, 77f), size = Size(10f, 6.4f))
    // rosto
    when (mood) {
        Mood.COMEMORANDO -> {
            drawPath(Path().apply { moveTo(42f, 73f); quadraticBezierTo(47f, 66f, 52f, 73f) }, ink, style = stroke(3.5f))
            drawPath(Path().apply { moveTo(68f, 73f); quadraticBezierTo(73f, 66f, 78f, 73f) }, ink, style = stroke(3.5f))
            val mouth = Path().apply { moveTo(48f, 80f); quadraticBezierTo(60f, 94f, 72f, 80f); close() }
            drawPath(mouth, ink)
        }
        Mood.TRANQUILA -> {
            drawPath(Path().apply { moveTo(42f, 72f); quadraticBezierTo(47f, 76f, 52f, 72f) }, ink, style = stroke(3.5f))
            drawPath(Path().apply { moveTo(68f, 72f); quadraticBezierTo(73f, 76f, 78f, 72f) }, ink, style = stroke(3.5f))
            drawPath(Path().apply { moveTo(52f, 81f); quadraticBezierTo(60f, 87f, 68f, 81f) }, ink, style = stroke(3.5f))
        }
        Mood.CONFUSA -> {
            drawLine(ink, Offset(41f, 64f), Offset(50f, 66f), 3f, StrokeCap.Round)
            drawLine(ink, Offset(70f, 61f), Offset(79f, 58f), 3f, StrokeCap.Round)
            drawCircle(ink, 4.5f, Offset(47f, 72f)); drawCircle(ink, 5.5f, Offset(73f, 71f))
            drawPath(Path().apply { moveTo(50f, 85f); quadraticBezierTo(53.5f, 81f, 57f, 85f); quadraticBezierTo(60.5f, 89f, 64f, 85f); quadraticBezierTo(67f, 82f, 70f, 85f) }, ink, style = stroke(3.2f))
        }
        Mood.PENSANDO -> {
            drawCircle(ink, 4.5f, Offset(49f, 70f)); drawCircle(ink, 4.5f, Offset(73f, 70f))
            drawCircle(Color.White, 1.4f, Offset(50.5f, 68.5f)); drawCircle(Color.White, 1.4f, Offset(74.5f, 68.5f))
            drawPath(Path().apply { moveTo(53f, 84f); quadraticBezierTo(60f, 81f, 67f, 84f) }, ink, style = stroke(3.5f))
        }
        else -> {
            drawCircle(ink, 4.8f, Offset(47f, 72f)); drawCircle(ink, 4.8f, Offset(73f, 72f))
            drawCircle(Color.White, 1.5f, Offset(48.6f, 70.4f)); drawCircle(Color.White, 1.5f, Offset(74.6f, 70.4f))
            drawPath(Path().apply { moveTo(50f, 80f); quadraticBezierTo(60f, 89f, 70f, 80f) }, ink, style = stroke(3.8f))
        }
    }
}

private fun DrawScope.drawExtras(mood: Mood, ink: Color) {
    when (mood) {
        Mood.PENSANDO -> {
            drawCircle(Color.White, 3f, Offset(104f, 30f)); drawCircle(ink, 3f, Offset(104f, 30f), style = stroke(2.5f))
            drawCircle(Color.White, 4.5f, Offset(111f, 19f)); drawCircle(ink, 4.5f, Offset(111f, 19f), style = stroke(2.5f))
        }
        Mood.CONFUSA -> {
            val q = Path().apply { moveTo(100f, 18f); quadraticBezierTo(100f, 10f, 107f, 10f); quadraticBezierTo(114f, 10f, 114f, 17f); quadraticBezierTo(114f, 22f, 107f, 24f); lineTo(107f, 28f) }
            drawPath(q, Brand.Azul, style = stroke(3.5f)); drawCircle(Brand.Azul, 2.2f, Offset(107f, 34f))
        }
        Mood.DICA -> {
            drawCircle(Color(0xFFFFF4C7), 9f, Offset(106f, 32f)); drawCircle(ink, 9f, Offset(106f, 32f), style = stroke(3f))
            drawRect(ink, Offset(102.5f, 40f), Size(7f, 5f))
            drawLine(ink, Offset(94f, 24f), Offset(90f, 21f), 2.5f, StrokeCap.Round)
            drawLine(ink, Offset(106f, 18f), Offset(106f, 13f), 2.5f, StrokeCap.Round)
            drawLine(ink, Offset(118f, 24f), Offset(122f, 21f), 2.5f, StrokeCap.Round)
        }
        Mood.COMEMORANDO -> {
            drawRect(Brand.Azul, Offset(6f, 20f), Size(7f, 9f)); drawRect(Brand.Verde, Offset(104f, 16f), Size(7f, 9f))
            drawCircle(Brand.Amarelo, 3.5f, Offset(24f, 10f)); drawCircle(Brand.Coral, 3f, Offset(96f, 6f))
        }
        else -> {}
    }
}
