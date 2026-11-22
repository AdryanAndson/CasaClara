package br.com.casaclara.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.casaclara.ui.theme.LocalDark

/** Ilustrações caricatas (vetores próprios, desenhados em Canvas). Área de 100 x 82. */
enum class Ill { CAIXAS, CONTA, CARRINHO, VASSOURA, SOFA, COFRINHO, CALENDARIO, FERRAMENTA, GRUPO, LIVRO, ESCUDO, PANELA, CHAVE, LUPA, MAPA, CARTAO, CRONOMETRO }

private class Part(val d: String, val fill: Long?, val stroke: Float = 3.5f, val noStroke: Boolean = false)

private const val INK = 0xFF1C2B39
private const val AM = 0xFFFFD23F
private const val VD = 0xFF1F9D6B
private const val AZ = 0xFF2F6FDF
private const val CR = 0xFFFF6B5B
private const val BR = 0xFFFFFFFF
private const val AZC = 0xFFDFE9FF
private const val VDC = 0xFFD9F5E8
private const val CZ = 0xFFC9D3DC

private fun c(cx: Float, cy: Float, r: Float) = "M${cx - r},$cy a$r,$r 0 1,0 ${2 * r},0 a$r,$r 0 1,0 ${-2 * r},0 Z"
private fun r(x: Float, y: Float, w: Float, h: Float) = "M$x,$y h$w v$h h${-w} Z"
private fun eyes(x: Float, y: Float, gap: Float = 12f) = listOf(Part(c(x, y, 2.6f), INK, noStroke = true), Part(c(x + gap, y, 2.6f), INK, noStroke = true))
private fun smile(x: Float, y: Float, w: Float = 10f) = Part("M$x,$y q${w / 2},${w / 2.2f} $w,0", null, 3f)

private val ART: Map<Ill, List<Part>> = mapOf(
    Ill.CAIXAS to listOf(
        Part(r(14f, 40f, 44f, 34f), 0xFFE8B57A), Part("M14,52 h44 M36,40 v12", null),
        Part(r(50f, 22f, 38f, 30f), 0xFFF3C98F), Part("M50,33 h38 M69,22 v11", null),
        Part(r(54f, 52f, 34f, 22f), 0xFFE8B57A),
    ) + eyes(28f, 62f) + smile(28f, 67f, 12f),
    Ill.CONTA to listOf(
        Part("M26,12 h40 l12,12 v52 l-6,-4 -6,4 -6,-4 -6,4 -6,-4 -6,4 -6,-4 -6,4 z", BR),
        Part("M66,12 v12 h12", AZC), Part("M34,32 h24 M34,42 h34 M34,52 h18", null),
        Part(c(62f, 58f, 9f), VD), Part("M62,52 v12 M58.5,55 q3.5,-2 7,0 q0,3 -3.5,3 q-3.5,0 -3.5,3 q3.5,2 7,0", null, 2f),
    ),
    Ill.CARRINHO to listOf(
        Part("M10,18 h12 l8,38 h44 l8,-28 H26", AZC),
        Part(r(34f, 16f, 14f, 16f), CR), Part(c(58f, 22f, 8f), VD), Part(r(64f, 14f, 10f, 18f), AM),
        Part(c(36f, 68f, 6f), BR), Part(c(68f, 68f, 6f), BR),
    ) + eyes(46f, 42f) + smile(47f, 47f),
    Ill.VASSOURA to listOf(
        Part("M62,8 L44,50", null, 5f), Part("M32,48 h26 l8,26 H22 z", AM),
        Part("M30,60 l-4,14 M40,60 l-2,14 M50,60 l2,14 M58,60 l4,14", null, 2.5f),
        Part(c(78f, 22f, 4f), AZC, 2.5f), Part(c(86f, 36f, 6f), AZC, 2.5f), Part("M76,52 l4,-4 m0,4 l-4,-4", null, 2.5f),
    ),
    Ill.SOFA to listOf(
        Part(r(18f, 26f, 64f, 30f), AZ), Part(r(8f, 40f, 18f, 26f), AZ), Part(r(74f, 40f, 18f, 26f), AZ),
        Part(r(22f, 48f, 56f, 18f), 0xFF5B8FF0), Part("M18,66 v8 M82,66 v8", null),
        Part(c(42f, 38f, 2.6f), BR, noStroke = true), Part(c(54f, 38f, 2.6f), BR, noStroke = true),
    ),
    Ill.COFRINHO to listOf(
        Part("M20,44 q0,-22 30,-22 q22,0 30,14 l8,-2 -2,10 q4,6 2,14 l-8,2 -4,10 h-10 v-6 h-24 v6 h-10 l-4,-10 q-8,-4 -8,-16 z", 0xFFFF9EB0),
        Part(r(40f, 18f, 16f, 4f), INK, noStroke = true), Part(c(70f, 40f, 2.6f), INK, noStroke = true),
        Part(c(50f, 10f, 6f), AM, 2.5f), Part("M30,58 q4,4 8,0", null, 2.5f),
    ),
    Ill.CALENDARIO to listOf(
        Part(r(16f, 16f, 68f, 58f), BR), Part(r(16f, 16f, 68f, 16f), CR), Part("M32,10 v12 M68,10 v12", null, 5f),
        Part(r(26f, 40f, 10f, 9f), VD, noStroke = true), Part(r(45f, 40f, 10f, 9f), AM, noStroke = true), Part(r(64f, 40f, 10f, 9f), VD, noStroke = true),
        Part(r(26f, 56f, 10f, 9f), AZ, noStroke = true), Part(r(45f, 56f, 10f, 9f), VD, noStroke = true),
    ),
    Ill.FERRAMENTA to listOf(
        Part("M24,66 L58,32", null, 8f),
        Part("M58,32 a14,14 0 1,0 14,-16 l-8,8 -6,-2 -2,-6 8,-8", CZ),
        Part("M50,52 l7,-7 22,22 -7,7 z", CR), Part(r(22f, 18f, 18f, 8f), AM),
    ),
    Ill.GRUPO to listOf(
        Part("M14,42 L50,14 L86,42 v32 H14 z", VDC),
        Part(c(34f, 50f, 8f), CR), Part("M22,74 q12,-16 24,0", CR), Part(c(66f, 50f, 8f), AZ), Part("M54,74 q12,-16 24,0", AZ),
        Part("M44,34 l6,-5 6,5 -6,6 z", AM, 2.5f),
    ),
    Ill.LIVRO to listOf(
        Part("M50,22 Q34,12 12,16 v50 q22,-4 38,6 z", BR), Part("M50,22 Q66,12 88,16 v50 q-22,-4 -38,6 z", VDC),
        Part("M20,30 h20 M20,40 h22 M60,30 h20 M60,40 h18", null, 2.5f),
    ) + eyes(62f, 52f, 10f),
    Ill.ESCUDO to listOf(Part("M50,8 L80,18 v22 q0,24 -30,36 q-30,-12 -30,-36 V18 z", VDC), Part("M36,42 l10,10 18,-20", null, 5f)),
    Ill.PANELA to listOf(
        Part("M18,34 h64 v24 q0,14 -14,14 H32 q-14,0 -14,-14 z", CR), Part("M8,38 h10 M82,38 h10", null, 5f),
        Part(r(16f, 26f, 68f, 8f), CZ), Part("M40,18 q-4,-6 0,-12 M52,18 q-4,-6 0,-12 M64,18 q-4,-6 0,-12", null, 2.5f),
        Part(c(42f, 50f, 2.6f), BR, noStroke = true), Part(c(58f, 50f, 2.6f), BR, noStroke = true),
    ),
    Ill.CHAVE to listOf(Part(c(32f, 40f, 18f), AM), Part(c(32f, 40f, 6f), 0xFFFFFAF0), Part("M50,40 h38 v10 M76,40 v8", null, 5f), Part("M20,14 l4,6 M44,12 l-3,7 M32,8 v8", null)),
    Ill.LUPA to listOf(
        Part("M12,46 L36,26 L60,46 v26 H12 z", AM), Part(r(30f, 54f, 12f, 18f), VD, 2.5f),
        Part(c(66f, 34f, 16f), 0xCCDFE9FF), Part("M78,46 l12,12", null, 7f),
    ),
    Ill.MAPA to listOf(
        Part("M12,20 l24,-8 28,10 24,-8 v52 l-24,8 -28,-10 -24,8 z", VDC), Part("M36,12 v52 M64,22 v52", null, 2.5f),
        Part("M22,48 q14,-20 30,-6 t30,-10", null, 3f), Part(c(80f, 32f, 5f), CR, 2.5f),
    ),
    Ill.CARTAO to listOf(
        Part(r(12f, 18f, 76f, 48f), AZ), Part(r(12f, 28f, 76f, 9f), INK, noStroke = true), Part(r(20f, 46f, 14f, 10f), AM, 2.5f),
        Part("M44,52 h30", null, 3f),
    ),
    Ill.CRONOMETRO to listOf(
        Part(c(50f, 46f, 28f), BR), Part(r(44f, 8f, 12f, 8f), CR), Part("M50,46 L50,28 M50,46 L64,52", null, 4f), Part("M74,20 l6,-6", null, 4f),
    ),
)

@Composable
fun Illustration(which: Ill, size: Dp = 96.dp, modifier: Modifier = Modifier) {
    val parts = remember(which) {
        ART.getValue(which).map { p -> Triple(PathParser().parsePathString(p.d).toPath(), p, Unit) }
    }
    val ink = if (LocalDark.current) Color(0xFF0E151B) else Color(INK)
    Canvas(modifier.size(size, size * 0.82f)) {
        val s = this.size.width / 100f
        scale(s, s, pivot = Offset.Zero) {
            parts.forEach { (path, p, _) ->
                p.fill?.let { drawPath(path, Color(it)) }
                if (!p.noStroke) drawPath(path, ink, style = Stroke(width = p.stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }
}

