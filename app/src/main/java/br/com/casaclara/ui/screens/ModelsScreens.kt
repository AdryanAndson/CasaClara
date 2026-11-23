package br.com.casaclara.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.casaclara.data.AppContainer
import br.com.casaclara.domain.Dates
import br.com.casaclara.domain.Routes
import br.com.casaclara.domain.content.Library
import br.com.casaclara.domain.content.MessageTemplates
import br.com.casaclara.ui.components.AlertBox
import br.com.casaclara.ui.components.BrandCard
import br.com.casaclara.ui.components.EmojiBadge
import br.com.casaclara.ui.components.EmptyState
import br.com.casaclara.ui.components.Ill
import br.com.casaclara.ui.components.LocalFeedback
import br.com.casaclara.ui.components.Mood
import br.com.casaclara.ui.components.Page
import br.com.casaclara.ui.components.PrimaryButton
import br.com.casaclara.ui.components.ScreenHeader
import br.com.casaclara.ui.components.SecondaryButton
import br.com.casaclara.ui.components.SectionTitle
import br.com.casaclara.ui.components.Tag
import br.com.casaclara.ui.components.Tone
import br.com.casaclara.ui.shareText
import kotlinx.coroutines.launch

private const val PROFILE_STORE = "modelos-perfil"

@Composable
private fun rememberModelStore(c: AppContainer, id: String): Pair<SnapshotStateMap<String, String>, (String, String) -> Unit>? {
    val values = remember(id) { mutableStateMapOf<String, String>() }
    var loaded by remember(id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(id) { values.putAll(c.prefs.current().calc[id].orEmpty()); loaded = true }
    if (!loaded) return null
    return values to { k, v -> values[k] = v; scope.launch { c.prefs.setCalc(id, values.toMap()) } }
}

private fun norm(s: String) = java.text.Normalizer.normalize(s.lowercase(), java.text.Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModelsScreen(c: AppContainer, nav: NavController) {
    var q by rememberSaveable { mutableStateOf("") }
    var cat by rememberSaveable { mutableStateOf("") }
    val (prof, setProf) = rememberModelStore(c, PROFILE_STORE) ?: return
    var showProfile by rememberSaveable { mutableStateOf(false) }
    val list = MessageTemplates.ALL.filter { (cat.isBlank() || it.cat == cat) && (q.isBlank() || norm(it.title + " " + it.description).contains(norm(q))) }
    Page(nav, "Modelos prontos") {
        ScreenHeader("Modelos prontos", "${MessageTemplates.ALL.size} mensagens e cartas para preencher e mandar: proprietário, loja, banco, trabalho, casa.", ill = Ill.CONTA, tone = Tone.ROXO)
        BrandCard(tone = Tone.AMARELO, padding = 12.dp, onClick = { showProfile = !showProfile }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🪪 Meus dados para os modelos", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Text(if (showProfile) "▲" else "▼")
            }
            Text(if (prof["nome"].isNullOrBlank()) "Preencha uma vez e todos os modelos já vêm com seu nome e endereço." else "Usando: ${prof["nome"]}${prof["cidade"]?.let { " · $it" } ?: ""}", style = MaterialTheme.typography.bodySmall)
        }
        if (showProfile) {
            MessageTemplates.PROFILE_FIELDS.forEach { f ->
                OutlinedTextField(prof[f] ?: "", { setProf(f, it) }, label = { Text(MessageTemplates.LABELS[f] ?: f) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
            Text("Fica salvo só neste aparelho.", style = MaterialTheme.typography.bodySmall)
        }
        OutlinedTextField(q, { q = it }, leadingIcon = { Icon(Icons.Filled.Search, null) }, placeholder = { Text("Buscar (ex.: conserto, cancelar, dívida)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FilterChip(cat.isBlank(), { cat = "" }, { Text("Todos") })
            MessageTemplates.CATS.forEach { k -> FilterChip(cat == k.id, { cat = if (cat == k.id) "" else k.id }, { Text("${k.emoji} ${k.title}") }) }
        }
        if (list.isEmpty()) EmptyState("Nenhum modelo encontrado", "Tente outra palavra ou outra categoria.", Mood.PENSANDO)
        MessageTemplates.CATS.forEach { k ->
            val items = list.filter { it.cat == k.id }
            if (items.isNotEmpty()) {
                SectionTitle("${k.emoji} ${k.title}")
                items.forEach { t ->
                    BrandCard(onClick = { nav.navigate(Routes.model(t.id)) }, padding = 12.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            EmojiBadge(t.emoji, 42.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(t.title, style = MaterialTheme.typography.titleSmall)
                                Text(t.description, style = MaterialTheme.typography.bodySmall)
                            }
                            Tag("${t.channel.emoji} ${t.channel.label}", Tone.NEUTRO)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModelScreen(c: AppContainer, nav: NavController, id: String) {
    val t = MessageTemplates.BY_ID[id]
    if (t == null) { Page(nav, "Modelo") { EmptyState("Modelo não encontrado", "Volte e escolha outro.", Mood.CONFUSA) }; return }
    val ctx = LocalContext.current
    val clip = LocalClipboardManager.current
    val fb = LocalFeedback.current
    val (prof, setProf) = rememberModelStore(c, PROFILE_STORE) ?: return
    val (v, set) = rememberModelStore(c, "modelo-$id") ?: return
    val values = buildMap {
        put("data", Dates.br(Dates.today().toEpochDay()))
        putAll(prof.filterValues { it.isNotBlank() })
        putAll(v.filterValues { it.isNotBlank() })
    }
    val filled = t.fill(values)
    var editing by remember { mutableStateOf<String?>(null) }
    val finalText = editing ?: filled
    val missing = t.fields.count { values[it].isNullOrBlank() }
    Page(nav, "Modelo pronto") {
        BrandCard(tone = Tone.ROXO) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(t.emoji, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(t.title, style = MaterialTheme.typography.titleLarge)
                    Text(t.description, style = MaterialTheme.typography.bodySmall)
                }
            }
            Tag("${t.channel.emoji} ${t.channel.label}", Tone.NEUTRO)
        }
        if (t.fields.isNotEmpty()) {
            SectionTitle("1. Preencha")
            t.fields.forEach { f ->
                val isProfile = f in MessageTemplates.PROFILE_FIELDS
                val cur = if (isProfile) prof[f] ?: "" else v[f] ?: (if (f == "data") Dates.br(Dates.today().toEpochDay()) else "")
                OutlinedTextField(cur, { if (isProfile) setProf(f, it) else set(f, it); editing = null },
                    label = { Text((MessageTemplates.LABELS[f] ?: f) + if (isProfile) " (meus dados)" else "") },
                    singleLine = f != "problema" && f != "regras" && f != "motivo", modifier = Modifier.fillMaxWidth())
            }
            if (missing > 0) Text("Faltam $missing campo(s). O que não for preenchido aparece entre [colchetes].", style = MaterialTheme.typography.bodySmall)
        }
        SectionTitle(if (t.fields.isNotEmpty()) "2. Confira o texto" else "Texto")
        if (editing == null) {
            BrandCard(padding = 14.dp) { Text(finalText, style = MaterialTheme.typography.bodyLarge) }
            TextButton(onClick = { editing = filled }) { Text("✏️ Ajustar o texto à mão") }
        } else {
            OutlinedTextField(editing!!, { editing = it }, modifier = Modifier.fillMaxWidth(), minLines = 8)
            TextButton(onClick = { editing = null }) { Text("↩ Voltar ao modelo preenchido") }
        }
        SectionTitle(if (t.fields.isNotEmpty()) "3. Envie" else "Envie")
        PrimaryButton("💬 Mandar pelo WhatsApp", Modifier.fillMaxWidth()) {
            val i = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, finalText).setPackage("com.whatsapp")
            runCatching { ctx.startActivity(i) }.onFailure { shareText(ctx, t.title, finalText) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryButton("📋 Copiar", Modifier.weight(1f)) { clip.setText(AnnotatedString(finalText)); fb.toast("Texto copiado!", Mood.FELIZ) }
            SecondaryButton("📤 Outro app", Modifier.weight(1f)) { shareText(ctx, t.title, finalText) }
        }
        t.tip?.let { AlertBox(it, "DICA") }
        t.guide?.let { gid -> Library.BY_ID[gid]?.let { g ->
            SecondaryButton("📚 Ler o guia: ${g.title}", Modifier.fillMaxWidth()) { nav.navigate(Routes.guide(g.id)) }
        } }
        Text("Modelo de referência. Revise antes de enviar e adapte à sua situação. Para casos jurídicos complexos, procure o Procon, a Defensoria Pública ou um advogado.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Normal)
    }
}
