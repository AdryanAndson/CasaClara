package br.com.casaclara.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.casaclara.ui.theme.BigNumber
import br.com.casaclara.ui.theme.Brand
import br.com.casaclara.ui.theme.LocalSoft

// ---------------------------------------------------------------------------
// Feedback global: avisos curtos e comemorações (com confete)
// ---------------------------------------------------------------------------
interface Feedback {
    fun toast(message: String, mood: Mood = Mood.FELIZ)
    fun celebrate(message: String)
}
val LocalFeedback = staticCompositionLocalOf<Feedback> { object : Feedback { override fun toast(message: String, mood: Mood) {}; override fun celebrate(message: String) {} } }

/** Confirmação antes de ações destrutivas. */
class ConfirmState {
    var request by mutableStateOf<ConfirmRequest?>(null)
    fun ask(title: String, message: String, confirmText: String = "Sim, excluir", danger: Boolean = true, onConfirm: () -> Unit) {
        request = ConfirmRequest(title, message, confirmText, danger, onConfirm)
    }
}
data class ConfirmRequest(val title: String, val message: String, val confirmText: String, val danger: Boolean, val onConfirm: () -> Unit)
val LocalConfirm = staticCompositionLocalOf { ConfirmState() }

@Composable
fun ConfirmHost(state: ConfirmState) {
    val r = state.request ?: return
    AlertDialog(
        onDismissRequest = { state.request = null },
        icon = { Mascot(Mood.CONFUSA, 64.dp) },
        title = { Text(r.title) },
        text = { Text(r.message) },
        confirmButton = {
            Button(
                onClick = { state.request = null; r.onConfirm() },
                colors = if (r.danger) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError) else ButtonDefaults.buttonColors(),
            ) { Text(r.confirmText) }
        },
        dismissButton = { TextButton(onClick = { state.request = null }) { Text("Cancelar") } },
    )
}

// ---------------------------------------------------------------------------
// Blocos visuais
// ---------------------------------------------------------------------------
enum class Tone { VERDE, AMARELO, AZUL, CORAL, ROXO, NEUTRO }

@Composable
fun toneColor(t: Tone): Color {
    val s = LocalSoft.current
    return when (t) {
        Tone.VERDE -> s.verde; Tone.AMARELO -> s.amarelo; Tone.AZUL -> s.azul; Tone.CORAL -> s.coral; Tone.ROXO -> s.roxo
        Tone.NEUTRO -> MaterialTheme.colorScheme.surface
    }
}

/** Sombra caricata: um "carimbo" escuro deslocado atrás do elemento. */
fun Modifier.cartoonShadow(color: Color, radius: Dp = 20.dp, offset: Dp = 4.dp): Modifier = this.drawBehind {
    val o = offset.toPx()
    drawRoundRect(color, topLeft = androidx.compose.ui.geometry.Offset(o, o), size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius.toPx()))
}

/** Cartão no estilo caricato: contorno grosso, cantos bem arredondados e sombra deslocada. */
@Composable
fun BrandCard(modifier: Modifier = Modifier, tone: Tone = Tone.NEUTRO, onClick: (() -> Unit)? = null, padding: Dp = 16.dp, content: @Composable ColumnScope.() -> Unit) {
    val colors = CardDefaults.cardColors(containerColor = toneColor(tone), contentColor = MaterialTheme.colorScheme.onSurface)
    val ink = LocalSoft.current.contorno
    val border = BorderStroke(2.5.dp, ink)
    val shape = RoundedCornerShape(22.dp)
    val m = modifier.padding(end = 4.dp, bottom = 4.dp).cartoonShadow(ink, 22.dp).fillMaxWidth()
    if (onClick != null) Card(onClick = onClick, modifier = m, shape = shape, colors = colors, border = border) { Column(Modifier.padding(padding), content = content) }
    else Card(modifier = m, shape = shape, colors = colors, border = border) { Column(Modifier.padding(padding), content = content) }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, action: (@Composable RowScope.() -> Unit)? = null) {
    Row(modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f).semantics { heading() })
        action?.invoke(this)
    }
}

/** Cabeçalho caricato: faixa colorida com título grande e a Clarinha ou uma ilustração. */
@Composable
fun ScreenHeader(title: String, subtitle: String? = null, emoji: String? = null, mood: Mood? = null, ill: Ill? = null, tone: Tone = Tone.AMARELO) {
    BrandCard(Modifier.padding(top = 8.dp), tone = tone, padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
                if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.width(8.dp))
            when {
                ill != null -> Illustration(ill, 92.dp)
                mood != null -> Mascot(mood, 84.dp)
                emoji != null -> EmojiBadge(emoji, 64.dp, Tone.NEUTRO)
            }
        }
    }
}

@Composable
fun EmojiBadge(emoji: String, size: Dp = 44.dp, tone: Tone = Tone.AMARELO) {
    Box(
        Modifier.size(size).clip(CircleShape).background(toneColor(tone)).border(2.dp, LocalSoft.current.contorno, CircleShape),
        contentAlignment = Alignment.Center,
    ) { Text(emoji, style = MaterialTheme.typography.titleLarge) }
}

/** Balão de fala da Clarinha. */
@Composable
fun MascotBubble(text: String, mood: Mood = Mood.FELIZ, size: Dp = 76.dp, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Mascot(mood, size)
        Spacer(Modifier.width(8.dp))
        Card(
            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(2.dp, LocalSoft.current.contorno),
            modifier = Modifier.weight(1f),
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                if (action != null) { Spacer(Modifier.height(8.dp)); action() }
            }
        }
    }
}

@Composable
fun Tag(text: String, tone: Tone = Tone.NEUTRO) {
    Box(
        Modifier.clip(RoundedCornerShape(50)).background(toneColor(tone)).border(1.5.dp, LocalSoft.current.contorno, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) { Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1) }
}

@Composable
fun DemoTag(show: Boolean) { if (show) Tag("demo", Tone.AMARELO) }

@Composable
fun Stat(label: String, value: String, modifier: Modifier = Modifier, color: Color? = null) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = BigNumber, color = color ?: MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Barra de progresso própria (bordas marcadas), com descrição acessível. */
@Composable
fun ProgressLine(value: Float, label: String, color: Color = Brand.Verde, modifier: Modifier = Modifier) {
    val v by animateFloatAsState(value.coerceIn(0f, 1f), label = "progresso")
    Box(
        modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.surface)
            .border(2.dp, LocalSoft.current.contorno, RoundedCornerShape(50))
            .semantics { contentDescription = "$label: ${(value.coerceIn(0f, 1f) * 100).toInt()}%" },
    ) {
        Box(Modifier.fillMaxWidth(v).height(16.dp).background(color))
    }
}

@Composable
fun EmptyState(title: String, text: String, mood: Mood = Mood.PENSANDO, actionText: String? = null, onAction: (() -> Unit)? = null, secondary: (@Composable () -> Unit)? = null) {
    Column(Modifier.fillMaxWidth().padding(vertical = 20.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Mascot(mood, 110.dp)
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.widthIn(max = 420.dp))
        if (actionText != null && onAction != null) AccentButton(actionText, Modifier.widthIn(min = 200.dp)) { onAction() }
        secondary?.invoke()
    }
}

@Composable
fun AlertBox(text: String, kind: String = "INFO") {
    val (tone, icon) = when (kind) { "PERIGO" -> Tone.CORAL to "⚠️"; "DICA" -> Tone.AMARELO to "💡"; "OK" -> Tone.VERDE to "✅"; else -> Tone.AZUL to "ℹ️" }
    BrandCard(tone = tone, padding = 12.dp) {
        Row(verticalAlignment = Alignment.Top) {
            Text(icon, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(10.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Linha de item com marcação opcional, ações de editar/excluir e espaço para ações extras. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemRow(
    title: String,
    subtitle: String? = null,
    amount: String? = null,
    tags: (@Composable () -> Unit)? = null,
    checked: Boolean? = null,
    onCheck: ((Boolean) -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    extra: (@Composable RowScope.() -> Unit)? = null,
    itemLabel: String = "item",
) {
    val done = checked == true
    val ink = LocalSoft.current.contorno
    Card(
        modifier = Modifier.padding(end = 3.dp, bottom = 3.dp).cartoonShadow(ink, 16.dp, 3.dp).fillMaxWidth().let { if (onClick != null) it.clickable(role = Role.Button, onClick = onClick) else it },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (done) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface),
        border = BorderStroke(2.dp, ink),
    ) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (checked != null) Checkbox(checked = checked, onCheckedChange = onCheck, modifier = Modifier.semantics { contentDescription = (if (done) "Desmarcar " else "Marcar ") + title })
            leading?.invoke()
            Column(Modifier.weight(1f).padding(start = if (checked == null && leading == null) 8.dp else 4.dp)) {
                Text(
                    title, style = MaterialTheme.typography.titleSmall,
                    color = if (done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (done) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                )
                if (!subtitle.isNullOrBlank()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (tags != null) FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) { tags() }
            }
            if (amount != null) Text(amount, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp))
            extra?.invoke(this)
            if (onEdit != null) IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Editar $itemLabel") }
            if (onDelete != null) IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Excluir $itemLabel") }
        }
    }
}

@Composable
fun Tabs(titles: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    ScrollableTabRow(selectedTabIndex = selected.coerceIn(0, titles.lastIndex), edgePadding = 12.dp, containerColor = MaterialTheme.colorScheme.background) {
        titles.forEachIndexed { i, t -> Tab(selected = i == selected, onClick = { onSelect(i) }, text = { Text(t, fontWeight = FontWeight.Bold) }) }
    }
}

/** Gráfico de barras horizontais — fácil de ler no celular. */
@Composable
fun Bars(data: List<Pair<String, Long>>, format: (Long) -> String, colors: List<Color> = listOf(Brand.Verde, Brand.Azul, Brand.Amarelo, Brand.Coral, Color(0xFF9B7BE0), Color(0xFF20B5C4))) {
    val max = (data.maxOfOrNull { it.second } ?: 1L).coerceAtLeast(1L)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        data.forEachIndexed { i, (label, v) ->
            Column(Modifier.semantics(mergeDescendants = true) { contentDescription = "$label: ${format(v)}" }) {
                Row { Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f)); Text(format(v), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold) }
                ProgressLine(v.toFloat() / max, label, colors[i % colors.size])
            }
        }
    }
}

/** Botão caricato: contorno, sombra e "afunda" ao ser pressionado. */
@Composable
fun CartoonButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, container: Color, content: Color, height: Dp = 52.dp, onClick: () -> Unit) {
    val ink = LocalSoft.current.contorno
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val press = if (pressed && enabled) 3.dp else 0.dp
    Box(modifier.height(height + 4.dp)) {
        Box(Modifier.matchParentSize().padding(top = 4.dp, start = 4.dp).clip(RoundedCornerShape(16.dp)).background(if (enabled) ink else ink.copy(alpha = 0.3f)))
        Button(
            onClick = onClick, enabled = enabled, interactionSource = source,
            modifier = Modifier.fillMaxWidth().height(height).offset(x = press, y = press).border(2.5.dp, if (enabled) ink else ink.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
        ) { Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 2) }
    }
}

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) =
    CartoonButton(text, modifier, enabled, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary, onClick = onClick)

@Composable
fun SecondaryButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) =
    CartoonButton(text, modifier, enabled, MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, height = 48.dp, onClick = onClick)

@Composable
fun AccentButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) =
    CartoonButton(text, modifier, enabled, Brand.Amarelo, Brand.Tinta, onClick = onClick)

@Composable
fun ProvideFeedback(feedback: Feedback, confirm: ConfirmState, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalFeedback provides feedback, LocalConfirm provides confirm, content = content)
}
