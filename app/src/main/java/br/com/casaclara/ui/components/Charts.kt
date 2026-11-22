package br.com.casaclara.ui.components

import androidx.compose.foundation.layout.widthIn

import androidx.compose.ui.unit.sp

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.casaclara.ui.theme.Brand
import br.com.casaclara.ui.theme.LocalSoft

val CHART_COLORS = listOf(Brand.Verde, Brand.Azul, Brand.Amarelo, Brand.Coral, Brand.Roxo, Color(0xFF20B5C4), Brand.Rosa, Color(0xFF8A9BAA))

/** Gráfico de rosca caricato (contorno grosso), com legenda e descrição acessível. */
@Composable
fun Donut(data: List<Pair<String, Long>>, format: (Long) -> String, center: String, centerLabel: String, size: Dp = 150.dp) {
    val top = data.take(6).let { l -> if (data.size > 6) l + ("Outros" to data.drop(6).sumOf { it.second }) else l }
    val total = top.sumOf { it.second }.coerceAtLeast(1)
    val reduce = rememberReduceMotion()
    val anim = remember(top) { Animatable(if (reduce) 1f else 0f) }
    LaunchedEffect(top) { if (!reduce) anim.animateTo(1f, tween(700)) }
    val ink = LocalSoft.current.contorno
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.semantics(mergeDescendants = true) {
        contentDescription = "$centerLabel: $center. " + top.joinToString("; ") { "${it.first} ${format(it.second)}" }
    }) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(size)) {
                val w = this.size.minDimension * 0.22f
                val inset = w / 2 + 3f
                val arcSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)
                var start = -90f
                top.forEachIndexed { i, (_, v) ->
                    val sweep = 360f * v / total * anim.value
                    drawArc(CHART_COLORS[i % CHART_COLORS.size], start, sweep, false, Offset(inset, inset), arcSize, style = Stroke(w))
                    start += sweep
                }
                // contornos caricatos
                drawCircle(ink, this.size.minDimension / 2 - 2f, style = Stroke(4f))
                drawCircle(ink, this.size.minDimension / 2 - w - 4f, style = Stroke(4f))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val fs = when { center.length > 11 -> 12.sp; center.length > 9 -> 13.sp; center.length > 7 -> 15.sp; else -> 18.sp }
                Text(center, style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"), fontSize = fs, fontWeight = FontWeight.Black, maxLines = 1, softWrap = false,
                    modifier = Modifier.widthIn(max = size * 0.52f))
                Text(centerLabel, style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            top.forEachIndexed { i, (label, v) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(14.dp).background(CHART_COLORS[i % CHART_COLORS.size], CircleShape).border(2.dp, ink, CircleShape))
                    Spacer(Modifier.width(6.dp))
                    Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text("${(v * 100 / total)}%", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
