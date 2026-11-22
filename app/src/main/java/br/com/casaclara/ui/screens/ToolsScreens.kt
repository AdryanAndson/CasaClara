package br.com.casaclara.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.casaclara.data.AppContainer
import br.com.casaclara.data.DocEntity
import br.com.casaclara.data.ReadingEntity
import br.com.casaclara.domain.Dates
import br.com.casaclara.domain.EmergencyNumbers
import br.com.casaclara.domain.Money
import br.com.casaclara.domain.Quiz
import br.com.casaclara.domain.Readings
import br.com.casaclara.domain.Routes
import br.com.casaclara.domain.ScamCheck
import br.com.casaclara.domain.content.Library
import br.com.casaclara.ui.asState
import br.com.casaclara.ui.components.AlertBox
import br.com.casaclara.ui.components.Bars
import br.com.casaclara.ui.components.BrandCard
import br.com.casaclara.ui.components.CrudList
import br.com.casaclara.ui.components.EmojiBadge
import br.com.casaclara.ui.components.EmptySpec
import br.com.casaclara.ui.components.FieldSpec
import br.com.casaclara.ui.components.FieldType
import br.com.casaclara.ui.components.Ill
import br.com.casaclara.ui.components.Init
import br.com.casaclara.ui.components.LocalFeedback
import br.com.casaclara.ui.components.MascotBubble
import br.com.casaclara.ui.components.MoneyInput
import br.com.casaclara.ui.components.Mood
import br.com.casaclara.ui.components.Page
import br.com.casaclara.ui.components.PrimaryButton
import br.com.casaclara.ui.components.ProgressLine
import br.com.casaclara.ui.components.RowSpec
import br.com.casaclara.ui.components.ScreenHeader
import br.com.casaclara.ui.components.SecondaryButton
import br.com.casaclara.ui.components.SectionTitle
import br.com.casaclara.ui.components.Stat
import br.com.casaclara.ui.components.Tabs
import br.com.casaclara.ui.components.Tag
import br.com.casaclara.ui.components.Tone
import br.com.casaclara.ui.shareText
import br.com.casaclara.ui.theme.Brand
import kotlinx.coroutines.launch

/** Pequeno armazenamento persistente por ferramenta (reaproveita o mapa de calculadoras das preferências). */
@Composable
private fun rememberStore(c: AppContainer, id: String): Pair<SnapshotStateMap<String, String>, (String, String) -> Unit>? {
    val values = remember(id) { mutableStateMapOf<String, String>() }
    var loaded by remember(id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(id) { values.putAll(c.prefs.current().calc[id].orEmpty()); loaded = true }
    if (!loaded) return null
    return values to { k, v -> values[k] = v; scope.launch { c.prefs.setCalc(id, values.toMap()) } }
}

@Composable
private fun GuideLinks(nav: NavController, ids: List<String>) {
    SectionTitle("📚 Para saber mais")
    ids.mapNotNull { Library.BY_ID[it] }.forEach { g ->
        BrandCard(onClick = { nav.navigate(Routes.guide(g.id)) }, padding = 12.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(g.emoji, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.width(10.dp))
                Text(g.title, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                Text("›", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

private fun dial(ctx: android.content.Context, number: String) {
    runCatching { ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number.filter { it.isDigit() || it == '+' }))) }
}

// ------------------------------------------------------------------ Emergência
private val CARD_FIELDS = listOf(
    "nome1" to "Contato 1 — nome", "tel1" to "Contato 1 — telefone",
    "nome2" to "Contato 2 — nome", "tel2" to "Contato 2 — telefone",
    "sangue" to "Tipo sanguíneo (se souber)", "alergias" to "Alergias", "remedios" to "Remédios de uso contínuo",
    "plano" to "Plano de saúde / nº do cartão SUS", "obs" to "Outras informações importantes",
)

@Composable
fun EmergencyScreen(c: AppContainer, nav: NavController) {
    val ctx = LocalContext.current
    val (v, set) = rememberStore(c, "emergencia") ?: return
    Page(nav, "Emergência") {
        ScreenHeader("Emergência", "Números úteis e seu cartão de emergência, funcionando sem internet.", ill = Ill.ESCUDO, tone = Tone.CORAL)
        AlertBox("Risco à vida? Ligue agora. Tocar num número abre o discador do celular — a ligação para emergência é gratuita.", "PERIGO")
        EmergencyNumbers.ALL.forEach { n ->
            BrandCard(onClick = { dial(ctx, n.number) }, padding = 12.dp, tone = if (n.number in listOf("192", "193", "190")) Tone.CORAL else Tone.NEUTRO) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EmojiBadge(n.emoji, 44.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("${n.number} · ${n.name}", style = MaterialTheme.typography.titleMedium)
                        Text(n.what, style = MaterialTheme.typography.bodySmall)
                    }
                    Text("📞", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
        SectionTitle("🆘 Meu cartão de emergência")
        Text("Fica salvo só neste aparelho. Mostre para quem estiver ajudando você, ou compartilhe com alguém de confiança.", style = MaterialTheme.typography.bodySmall)
        listOf(v["tel1"] to v["nome1"], v["tel2"] to v["nome2"]).filter { !it.first.isNullOrBlank() }.forEach { (tel, nome) ->
            SecondaryButton("📞 Ligar para ${nome?.ifBlank { null } ?: tel}", Modifier.fillMaxWidth()) { dial(ctx, tel!!) }
        }
        CARD_FIELDS.forEach { (k, label) ->
            OutlinedTextField(value = v[k] ?: "", onValueChange = { set(k, it) }, label = { Text(label) }, modifier = Modifier.fillMaxWidth(),
                singleLine = k != "obs" && k != "remedios" && k != "alergias")
        }
        PrimaryButton("📤 Compartilhar cartão", Modifier.fillMaxWidth()) {
            val txt = buildString {
                append("🆘 Cartão de emergência\n")
                CARD_FIELDS.forEach { (k, label) -> v[k]?.takeIf { it.isNotBlank() }?.let { append("$label: $it\n") } }
                append("\nEmergências: SAMU 192 · Bombeiros 193 · Polícia 190")
            }
            shareText(ctx, "Cartão de emergência", txt)
        }
        AlertBox("Dica: configure também as \"informações de emergência\" do próprio celular. Elas aparecem na tela de bloqueio sem precisar da senha.", "DICA")
        GuideLinks(nav, listOf("cartao-emergencia-guia", "quando-upa", "cano-estourado", "vazamento-gas", "cheiro-queimado", "chuva-forte"))
    }
}

// ------------------------------------------------------------------ Documentos com validade
private val DOC_KINDS = listOf("DOCUMENTO" to "🪪 Documento pessoal", "CONTRATO" to "📝 Contrato", "SEGURO" to "🛡️ Seguro", "VEICULO" to "🚗 Veículo", "SAUDE" to "🩺 Saúde", "OUTRO" to "📁 Outro")
private fun docEmoji(k: String) = DOC_KINDS.firstOrNull { it.first == k }?.second?.substringBefore(' ') ?: "📁"

@Composable
fun DocsScreen(c: AppContainer, nav: NavController) {
    val docs by c.db.docs().all().asState(emptyList())
    val warr by c.db.warranties().all().asState(emptyList())
    val today = Dates.todayEpoch()
    Page(nav, "Documentos e validades") {
        ScreenHeader("Documentos e validades", "CNH, identidade, contrato de aluguel, seguro… O app avisa antes de vencer.", ill = Ill.CONTA, tone = Tone.AZUL)
        AlertBox("Não guarde números de documentos nem fotos aqui — só o nome, a validade e onde está guardado. Assim, mesmo que alguém pegue seu celular, seus dados ficam protegidos.", "INFO")
        val vencendo = docs.filter { it.expires != null && it.expires - today <= it.remindDays }
        if (vencendo.isNotEmpty()) BrandCard(tone = Tone.CORAL) {
            Text("⏰ Atenção", style = MaterialTheme.typography.titleMedium)
            vencendo.forEach { d -> Text("${docEmoji(d.kind)} ${d.name}: " + status(d.expires!!, today), fontWeight = FontWeight.Bold) }
        }
        CrudList(
            items = docs, itemName = "documento", masculine = true, title = "Meus documentos",
            fields = listOf(
                FieldSpec("name", "Nome", required = true, placeholder = "Ex.: CNH, contrato de aluguel, seguro do celular"),
                FieldSpec("kind", "Tipo", FieldType.SELECT, true, DOC_KINDS),
                FieldSpec("expires", "Validade", FieldType.DATE, help = "Deixe em branco se não vence."),
                FieldSpec("remind", "Avisar quantos dias antes", FieldType.INT, min = 1.0, max = 365.0),
                FieldSpec("place", "Onde está guardado", placeholder = "Ex.: pasta azul, gaveta do quarto, app gov.br"),
                FieldSpec("note", "Observação", FieldType.LONGTEXT),
            ),
            toForm = { d -> if (d == null) mapOf("kind" to "DOCUMENTO", "remind" to "30") else mapOf("name" to d.name, "kind" to d.kind, "expires" to Init.date(d.expires), "remind" to d.remindDays.toString(), "place" to (d.place ?: ""), "note" to (d.note ?: "")) },
            save = { f, old -> c.saveDoc(DocEntity(id = old?.id ?: 0, name = f.str("name"), kind = f.str("kind"), expires = f.date("expires"), remindDays = (f.int("remind") ?: 30).coerceIn(1, 365), place = f.text("place"), note = f.text("note"), reminderId = old?.reminderId, isDemo = old?.isDemo ?: false)) },
            delete = { c.deleteDoc(it) },
            row = { d ->
                RowSpec("${docEmoji(d.kind)} ${d.name}", listOfNotNull(d.expires?.let { "Validade: ${Dates.br(it)}" } ?: "Sem validade", d.place?.let { "Guardado em: $it" }).joinToString(" · "),
                    tags = {
                        val e = d.expires
                        when {
                            e == null -> {}
                            e < today -> Tag("vencido", Tone.CORAL)
                            e - today <= d.remindDays -> Tag(status(e, today), Tone.AMARELO)
                            else -> Tag("em dia", Tone.VERDE)
                        }
                        if (d.reminderId != null) Tag("🔔 aviso", Tone.AZUL)
                    })
            },
            empty = EmptySpec("Nenhum documento cadastrado", "Comece pela identidade, CNH e contrato de aluguel. O app cria um lembrete antes de cada vencimento."),
            isDemo = { it.isDemo },
        )
        SectionTitle("🧾 Garantias de produtos")
        val ends = warr.mapNotNull { w -> if (w.purchaseDate != null && w.months != null) w to Dates.of(w.purchaseDate).plusMonths(w.months.toLong()).toEpochDay() else null }.sortedBy { it.second }
        if (ends.isEmpty()) Text("Nenhuma garantia com data cadastrada.", style = MaterialTheme.typography.bodySmall)
        ends.take(5).forEach { (w, end) -> Text("• ${w.product}: " + if (end < today) "garantia encerrada" else "até ${Dates.br(end)}", style = MaterialTheme.typography.bodyMedium) }
        SecondaryButton("Ver e cadastrar garantias", Modifier.fillMaxWidth()) { nav.navigate(Routes.house(Routes.HouseTab.GARANTIAS)) }
        GuideLinks(nav, listOf("documentos-essenciais", "organizar-papeis", "perdi-documentos", "garantia-legal"))
    }
}

private fun status(exp: Long, today: Long): String {
    val d = exp - today
    return when { d < 0 -> "venceu há ${-d} dia(s)"; d == 0L -> "vence hoje"; d == 1L -> "vence amanhã"; else -> "vence em $d dias" }
}

// ------------------------------------------------------------------ Consumo de luz, água e gás
private data class Meter(val kind: String, val title: String, val unit: String, val emoji: String, val tariffHelp: String)
private val METERS = listOf(
    Meter("LUZ", "Luz", "kWh", "💡", "Divida o valor da última conta pelo consumo em kWh dela (já inclui impostos e bandeira)."),
    Meter("AGUA", "Água", "m³", "🚰", "Divida o valor da conta (água + esgoto) pelos m³ consumidos."),
    Meter("GAS", "Gás encanado", "m³", "🔥", "Divida o valor da conta pelos m³ consumidos."),
)

@Composable
fun ConsumptionScreen(c: AppContainer, nav: NavController) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val all by c.db.readings().all().asState(emptyList())
    val (t, setT) = rememberStore(c, "tarifas") ?: return
    val m = METERS[tab]
    val list = all.filter { it.kind == m.kind }
    val st = Readings.stats(list.map { Readings.Point(it.date, it.value) })
    val tariff = Money.parse(t[m.kind]) ?: 0
    fun fmt(x: Double) = String.format(java.util.Locale.ROOT, "%.1f", x).replace('.', ',')
    Page(nav, "Consumo da casa") {
        ScreenHeader("Consumo de luz, água e gás", "Anote a leitura do medidor e descubra quanto está gastando antes da conta chegar.", ill = Ill.LUPA, tone = Tone.VERDE)
        Tabs(METERS.map { it.emoji + " " + it.title }, tab) { tab = it }
        BrandCard(tone = Tone.VERDE) {
            when {
                st.error != null -> AlertBox(st.error!!, "PERIGO")
                st.monthEstimate == null -> Text("Anote pelo menos duas leituras (por exemplo, com uma semana de diferença) para ver a estimativa.", style = MaterialTheme.typography.bodyMedium)
                else -> {
                    Stat("Estimativa para 30 dias", "${fmt(st.monthEstimate!!)} ${m.unit}")
                    if (tariff > 0) Stat("Custo estimado do mês", Money.format(Readings.cost(st.monthEstimate!!, tariff)))
                    Text("Média recente: ${fmt(st.perDay!!)} ${m.unit} por dia.", style = MaterialTheme.typography.bodySmall)
                    st.trendPct?.let { p ->
                        val up = p > 10; val down = p < -10
                        Text(when { up -> "📈 O consumo subiu ${p.toInt()}% em relação ao período anterior."; down -> "📉 O consumo caiu ${(-p).toInt()}%. Boa!"; else -> "➡️ Consumo estável em relação ao período anterior." }, fontWeight = FontWeight.Bold,
                            color = if (up) MaterialTheme.colorScheme.error else Brand.VerdeEscuro)
                    }
                    if (st.periods.size >= 2) Bars(st.periods.takeLast(6).map { "até ${Dates.br(it.to).take(5)}" to (it.perDay * 30 * 10).toLong() }, { "${fmt(it / 10.0)} ${m.unit}/mês" })
                }
            }
        }
        MoneyInput("Preço por ${m.unit} (opcional)", t[m.kind] ?: "", { setT(m.kind, it) }, help = m.tariffHelp)
        CrudList(
            items = list, itemName = "leitura", title = "Leituras de ${m.title.lowercase()}",
            fields = listOf(
                FieldSpec("date", "Data da leitura", FieldType.DATE, true),
                FieldSpec("value", "Número do medidor (${m.unit})", FieldType.DECIMAL, true, help = "Anote todos os números inteiros do visor, da esquerda para a direita."),
                FieldSpec("note", "Observação"),
            ),
            toForm = { r -> if (r == null) mapOf("date" to Init.today()) else mapOf("date" to Init.date(r.date), "value" to Init.num(r.value), "note" to (r.note ?: "")) },
            save = { f, old -> c.db.readings().upsert(ReadingEntity(id = old?.id ?: 0, kind = m.kind, date = f.date("date")!!, value = f.double("value") ?: 0.0, note = f.text("note"), isDemo = old?.isDemo ?: false)) },
            delete = { c.db.readings().delete(it) },
            row = { r -> RowSpec("${Dates.br(r.date)}: ${Init.num(r.value)} ${m.unit}", r.note) },
            empty = EmptySpec("Nenhuma leitura ainda", "Vá até o medidor, anote o número e cadastre aqui. Repita em alguns dias."),
            isDemo = { it.isDemo },
        )
        GuideLinks(nav, listOf("conta-luz-entender", "conta-agua-entender", "economizar-basico"))
    }
}

// ------------------------------------------------------------------ Detector de golpe
@Composable
fun ScamScreen(c: AppContainer, nav: NavController) {
    val fb = LocalFeedback.current
    val (v, set) = rememberStore(c, "golpe") ?: return
    val yes = remember { mutableStateMapOf<String, Boolean>() }
    val chosen = yes.filterValues { it }.keys
    val level = ScamCheck.level(chosen)
    Page(nav, "Detector de golpe") {
        ScreenHeader("É golpe?", "Recebeu uma mensagem, ligação ou oferta estranha? Responda e veja o risco.", ill = Ill.ESCUDO, tone = Tone.ROXO)
        ScamCheck.QUESTIONS.forEach { q ->
            BrandCard(padding = 12.dp, tone = if (yes[q.id] == true) Tone.AMARELO else Tone.NEUTRO, onClick = { yes[q.id] = !(yes[q.id] ?: false) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(q.text, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = yes[q.id] == true, onCheckedChange = { yes[q.id] = it })
                }
                if (yes[q.id] == true) Text(q.why, style = MaterialTheme.typography.bodySmall)
            }
        }
        BrandCard(tone = when (level) { ScamCheck.Level.ALTO -> Tone.CORAL; ScamCheck.Level.ATENCAO -> Tone.AMARELO; else -> Tone.VERDE }) {
            Text(when (level) { ScamCheck.Level.ALTO -> "🚨 "; ScamCheck.Level.ATENCAO -> "⚠️ "; else -> "✅ " } + level.label, style = MaterialTheme.typography.titleLarge)
            ProgressLine((ScamCheck.score(chosen) / 8f).coerceAtMost(1f), "Nível de risco", when (level) { ScamCheck.Level.ALTO -> Brand.Coral; ScamCheck.Level.ATENCAO -> Brand.Amarelo; else -> Brand.Verde })
            Text(when (level) {
                ScamCheck.Level.ALTO -> "Suas respostas têm sinais fortes de golpe. Faça assim:"
                ScamCheck.Level.ATENCAO -> "Há sinais que merecem cuidado. Antes de qualquer coisa:"
                else -> "Poucos sinais de golpe nas suas respostas. Mesmo assim, na dúvida, confirme pelo canal oficial."
            })
            if (level != ScamCheck.Level.BAIXO) ScamCheck.WHAT_TO_DO.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
        }
        PrimaryButton("Concluir verificação", Modifier.fillMaxWidth()) {
            set("count", ((v["count"]?.toIntOrNull() ?: 0) + 1).toString())
            fb.toast(if (level == ScamCheck.Level.BAIXO) "Verificação feita. Desconfiar é um superpoder!" else "Na dúvida, não faça nada agora. Confirme primeiro.", Mood.DICA)
            yes.clear()
        }
        GuideLinks(nav, listOf("golpe-pix", "golpe-falso-parente", "golpe-falsa-central", "links-falsos", "pix-errado"))
    }
}

// ------------------------------------------------------------------ Teste rápido
@Composable
fun QuizScreen(c: AppContainer, nav: NavController) {
    val fb = LocalFeedback.current
    val (v, set) = rememberStore(c, "quiz") ?: return
    var seed by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    val qs = remember(seed) { Quiz.draw(10, seed) }
    var index by rememberSaveable(seed) { mutableIntStateOf(0) }
    val answers = remember(seed) { mutableStateMapOf<String, Int>() }
    val best = v["best"]?.toIntOrNull() ?: 0
    Page(nav, "Teste rápido") {
        ScreenHeader("Teste rápido", "10 perguntas sobre dinheiro, casa, direitos e segurança. Cada uma leva a um guia.", ill = Ill.LIVRO, tone = Tone.AMARELO)
        if (index < qs.size) {
            val q = qs[index]
            val a = answers[q.id]
            ProgressLine(index / qs.size.toFloat(), "Pergunta ${index + 1} de ${qs.size}")
            Text("Pergunta ${index + 1} de ${qs.size}", style = MaterialTheme.typography.labelLarge)
            BrandCard(tone = Tone.AZUL) { Text(q.text, style = MaterialTheme.typography.titleMedium) }
            q.options.forEachIndexed { i, o ->
                val tone = when { a == null -> Tone.NEUTRO; i == q.correct -> Tone.VERDE; i == a -> Tone.CORAL; else -> Tone.NEUTRO }
                BrandCard(padding = 12.dp, tone = tone, onClick = if (a == null) { { answers[q.id] = i } } else null) {
                    Text((if (a != null && i == q.correct) "✅ " else if (a == i) "❌ " else "") + o, style = MaterialTheme.typography.bodyLarge)
                }
            }
            if (a != null) {
                MascotBubble((if (a == q.correct) "Isso! " else "Quase! ") + q.explain, if (a == q.correct) Mood.COMEMORANDO else Mood.DICA)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { nav.navigate(Routes.guide(q.guide)) }) { Text("Ler o guia") }
                    Spacer(Modifier.weight(1f))
                    PrimaryButton(if (index == qs.size - 1) "Ver resultado" else "Próxima") {
                        index++
                        if (index == qs.size) {
                            val s = Quiz.score(qs, answers)
                            if (s > best) set("best", s.toString())
                            if (s >= 8) fb.celebrate("$s de ${qs.size}! Você manda bem.")
                        }
                    }
                }
            }
        } else {
            val s = Quiz.score(qs, answers)
            BrandCard(tone = if (s >= 8) Tone.VERDE else Tone.AMARELO) {
                Stat("Você acertou", "$s de ${qs.size}")
                Text(when { s >= 9 -> "Uau! Pronto(a) para a vida independente."; s >= 7 -> "Muito bem! Vale revisar os guias das que errou."; s >= 4 -> "Bom começo. Os guias abaixo vão ajudar."; else -> "Tudo bem, é para isso que o teste serve. Bora aprender juntos!" })
                Text("Seu recorde: ${maxOf(best, s)} de ${qs.size}", style = MaterialTheme.typography.bodySmall)
            }
            val wrong = qs.filter { answers[it.id] != it.correct }
            if (wrong.isNotEmpty()) GuideLinks(nav, wrong.map { it.guide }.distinct())
            PrimaryButton("🔁 Fazer de novo (novas perguntas)", Modifier.fillMaxWidth()) { seed = System.currentTimeMillis() }
        }
        if (index < qs.size && best > 0) Text("Seu recorde: $best de 10", style = MaterialTheme.typography.bodySmall)
    }
}
