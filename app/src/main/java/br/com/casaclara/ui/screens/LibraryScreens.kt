package br.com.casaclara.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.casaclara.data.AppContainer
import br.com.casaclara.data.GuideStateEntity
import br.com.casaclara.domain.Routes
import br.com.casaclara.domain.content.AlertKind
import br.com.casaclara.domain.content.Guide
import br.com.casaclara.domain.content.Library
import br.com.casaclara.ui.asState
import br.com.casaclara.ui.components.AlertBox
import br.com.casaclara.ui.components.BrandCard
import br.com.casaclara.ui.components.EmojiBadge
import br.com.casaclara.ui.components.EmptyState
import br.com.casaclara.ui.components.ItemRow
import br.com.casaclara.ui.components.LocalFeedback
import br.com.casaclara.ui.components.Mood
import br.com.casaclara.ui.components.Page
import br.com.casaclara.ui.components.ScreenHeader
import br.com.casaclara.ui.components.SectionTitle
import br.com.casaclara.ui.components.Tag
import br.com.casaclara.ui.components.Tone
import br.com.casaclara.ui.theme.LocalDark
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraryScreen(c: AppContainer, nav: NavController) {
    val states by c.db.guides().all().asState(emptyList())
    var query by rememberSaveable { mutableStateOf("") }
    var mode by rememberSaveable { mutableStateOf("CATEGORIAS") }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    var difficulty by rememberSaveable { mutableStateOf<String?>(null) }
    val byId = states.associateBy { it.guideId }
    Page(nav, "Dicas e guias") {
        ScreenHeader("Biblioteca", "${Library.GUIDES.size} guias práticos para a vida independente. Leitura de 2 a 5 minutos.", mood = Mood.DICA)
        OutlinedTextField(
            value = query, onValueChange = { query = it }, label = { Text("Buscar (ex.: caução, mofo, gás)") }, singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }, modifier = Modifier.fillMaxWidth(),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("CATEGORIAS" to "Categorias", "FAVORITOS" to "⭐ Favoritos", "HISTORICO" to "🕘 Histórico").forEach { (k, l) -> FilterChip(mode == k && query.isBlank(), { mode = k; query = ""; category = null }, { Text(l) }) }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(null to "Todos os níveis", "FACIL" to "Fácil", "MEDIO" to "Médio", "AVANCADO" to "Avançado").forEach { (k, l) -> FilterChip(difficulty == k, { difficulty = k }, { Text(l) }) }
        }
        val diff = difficulty
        val selectedCat = category
        fun filterDiff(l: List<Guide>) = if (diff == null) l else l.filter { it.difficulty.name == diff }
        when {
            query.isNotBlank() -> {
                val r = filterDiff(Library.search(query))
                SectionTitle("${r.size} resultado(s)")
                if (r.isEmpty()) EmptyState("Não achei nada com isso", "Tente outra palavra, como \"aluguel\", \"limpeza\" ou \"dívida\".", Mood.CONFUSA)
                r.forEach { GuideRow(it, byId[it.guideIdKey()], nav) }
            }
            mode == "FAVORITOS" -> {
                val favs = filterDiff(Library.GUIDES.filter { byId[it.id]?.favorite == true })
                if (favs.isEmpty()) EmptyState("Nenhum favorito ainda", "Toque no coração dentro de um guia para salvá-lo aqui.", Mood.PENSANDO)
                favs.forEach { GuideRow(it, byId[it.id], nav) }
            }
            mode == "HISTORICO" -> {
                val hist = states.filter { it.lastRead != null }.sortedByDescending { it.lastRead }.mapNotNull { Library.BY_ID[it.guideId] }
                if (hist.isEmpty()) EmptyState("Nada lido ainda", "Os guias que você abrir aparecem aqui.", Mood.PENSANDO)
                filterDiff(hist).forEach { GuideRow(it, byId[it.id], nav) }
            }
            selectedCat != null -> {
                val cat = Library.category(selectedCat)
                SectionTitle("${cat.emoji} ${cat.name}") { androidx.compose.material3.TextButton(onClick = { category = null }) { Text("Todas") } }
                Library.byCategory(cat.id).let(::filterDiff).groupBy { it.sub }.forEach { (sub, list) ->
                    Text(sub, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 6.dp).semantics { heading() })
                    list.forEach { GuideRow(it, byId[it.id], nav) }
                }
            }
            else -> {
                Library.CATEGORIES.forEach { cat ->
                    val n = Library.byCategory(cat.id).size
                    val read = Library.byCategory(cat.id).count { (byId[it.id]?.readCount ?: 0) > 0 }
                    BrandCard(onClick = { category = cat.id }, padding = 12.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            EmojiBadge(cat.emoji, 48.dp, Tone.NEUTRO)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(cat.name, style = MaterialTheme.typography.titleMedium)
                                Text(cat.description, style = MaterialTheme.typography.bodySmall)
                                Text("$n guias · $read lidos", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            Text("›", style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
            }
        }
    }
}

private fun Guide.guideIdKey() = id

@Composable
private fun GuideRow(g: Guide, st: GuideStateEntity?, nav: NavController) {
    ItemRow(
        title = g.title, subtitle = g.problem.take(110) + if (g.problem.length > 110) "…" else "",
        leading = { EmojiBadge(g.emoji, 40.dp, Tone.NEUTRO) },
        tags = {
            Tag("${g.minutes} min"); Tag(g.difficulty.label, Tone.AZUL)
            if (st?.favorite == true) Tag("⭐", Tone.AMARELO)
            if ((st?.readCount ?: 0) > 0) Tag("lido", Tone.VERDE)
        },
        onClick = { nav.navigate(Routes.guide(g.id)) },
    )
}

@Composable
fun GuideScreen(c: AppContainer, nav: NavController, id: String) {
    val g = Library.BY_ID[id]
    val states by c.db.guides().all().asState(emptyList())
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val fb = LocalFeedback.current
    if (g == null) {
        Page(nav, "Guia") { EmptyState("Esse guia não existe", "Ele pode ter mudado de lugar. Volte para a biblioteca.", Mood.CONFUSA, "Ir para a biblioteca", { nav.navigate(Routes.LIBRARY) }) }
        return
    }
    LaunchedEffect(id) {
        val cur = c.db.guides().get(id) ?: GuideStateEntity(id)
        c.db.guides().upsert(cur.copy(lastRead = System.currentTimeMillis(), readCount = cur.readCount + 1))
    }
    val st = states.firstOrNull { it.guideId == id } ?: GuideStateEntity(id)
    val checked = st.checkedSet()
    val cat = Library.category(g.category)
    val bg = if (LocalDark.current) MaterialTheme.colorScheme.surfaceVariant else Color(cat.color)
    Page(nav, cat.name, actions = {
        IconButton(onClick = {
            scope.launch { c.db.guides().upsert(st.copy(favorite = !st.favorite)) }
            fb.toast(if (st.favorite) "Removido dos favoritos." else "Salvo nos favoritos! ⭐")
        }) { Icon(if (st.favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, contentDescription = if (st.favorite) "Remover dos favoritos" else "Salvar nos favoritos") }
        IconButton(onClick = {
            val text = buildString {
                appendLine(g.title); appendLine(); appendLine(g.problem); appendLine()
                g.steps.forEachIndexed { i, s -> appendLine("${i + 1}. $s") }
                appendLine(); append("Dica do app Casa Clara — orientação geral e educativa.")
            }
            ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), "Compartilhar dica"))
        }) { Icon(Icons.Filled.Share, contentDescription = "Compartilhar") }
    }) {
        androidx.compose.material3.Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = bg), shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text(g.emoji, style = MaterialTheme.typography.displaySmall)
                Text(g.title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) { Tag("${g.minutes} min de leitura"); Tag(g.difficulty.label, Tone.AZUL); Tag(g.sub, Tone.AMARELO) }
            }
        }
        SectionTitle("O problema")
        Text(g.problem, style = MaterialTheme.typography.bodyLarge)
        SectionTitle("Passo a passo")
        g.steps.forEachIndexed { i, s ->
            Row(verticalAlignment = Alignment.Top) {
                EmojiBadge("${i + 1}", 32.dp, Tone.VERDE)
                Spacer(Modifier.width(10.dp))
                Text(s, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).padding(top = 4.dp))
            }
        }
        g.example?.let {
            SectionTitle("Exemplo")
            BrandCard(tone = Tone.AZUL, padding = 12.dp) { Text(it) }
        }
        if (g.mistakes.isNotEmpty()) {
            SectionTitle("Erros comuns")
            g.mistakes.forEach { Text("✗ $it", style = MaterialTheme.typography.bodyLarge) }
        }
        g.alert?.let { AlertBox(it.text, when (it.kind) { AlertKind.PERIGO -> "PERIGO"; AlertKind.DICA -> "DICA"; AlertKind.INFO -> "INFO" }) }
        if (g.legal && g.alert?.kind != AlertKind.INFO) AlertBox("Este conteúdo envolve regras oficiais que podem mudar. Confira em fontes oficiais atualizadas e, se precisar, procure orientação profissional.", "INFO")
        if (g.checklist.isNotEmpty()) {
            SectionTitle("Checklist prático (${checked.count { it < g.checklist.size }}/${g.checklist.size})")
            g.checklist.forEachIndexed { i, item ->
                ItemRow(item, checked = i in checked, onCheck = { v ->
                    val n = if (v) checked + i else checked - i
                    scope.launch { c.db.guides().upsert(st.copy(checked = n.joinToString(","))) }
                    if (v && n.count { it < g.checklist.size } == g.checklist.size) fb.celebrate("Checklist completo! Mandou bem.")
                })
            }
        }
        val rel = Library.related(g)
        if (rel.isNotEmpty()) {
            SectionTitle("Conteúdo relacionado")
            rel.forEach { r -> GuideRow(r, states.firstOrNull { it.guideId == r.id }, nav) }
        }
        Text("Orientação geral e educativa. Não substitui orientação médica, psicológica, jurídica, financeira ou técnica profissional.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Normal)
    }
}
