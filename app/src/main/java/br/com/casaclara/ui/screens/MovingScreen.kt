package br.com.casaclara.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.casaclara.data.AppContainer
import br.com.casaclara.data.MilestoneEntity
import br.com.casaclara.data.PropertyEntity
import br.com.casaclara.data.ReminderEntity
import br.com.casaclara.data.UserPrefs
import br.com.casaclara.domain.Calculators
import br.com.casaclara.domain.Dates
import br.com.casaclara.domain.Money
import br.com.casaclara.domain.Routes
import br.com.casaclara.domain.content.Templates
import br.com.casaclara.notifications.ReminderScheduler
import br.com.casaclara.ui.asState
import br.com.casaclara.ui.components.Ill
import br.com.casaclara.ui.components.AlertBox
import br.com.casaclara.ui.components.Bars
import br.com.casaclara.ui.components.BrandCard
import br.com.casaclara.ui.components.CrudList
import br.com.casaclara.ui.components.EmptySpec
import br.com.casaclara.ui.components.FieldSpec
import br.com.casaclara.ui.components.FieldType
import br.com.casaclara.ui.components.Init
import br.com.casaclara.ui.components.LocalConfirm
import br.com.casaclara.ui.components.LocalFeedback
import br.com.casaclara.ui.components.MascotBubble
import br.com.casaclara.ui.components.Mood
import br.com.casaclara.ui.components.PrimaryButton
import br.com.casaclara.ui.components.ProgressLine
import br.com.casaclara.ui.components.RowSpec
import br.com.casaclara.ui.components.ScreenHeader
import br.com.casaclara.ui.components.SecondaryButton
import br.com.casaclara.ui.components.SectionTitle
import br.com.casaclara.ui.components.Stat
import br.com.casaclara.ui.components.TabBody
import br.com.casaclara.ui.components.Tabs
import br.com.casaclara.ui.components.Tag
import br.com.casaclara.ui.components.Tone
import br.com.casaclara.ui.components.opts
import kotlinx.coroutines.launch

@Composable
fun MovingScreen(c: AppContainer, nav: NavController, initialTab: Int) {
    var tab by rememberSaveable { mutableIntStateOf(initialTab) }
    Column {
        Row { TextButton(onClick = { nav.popBackStack() }) { Text("‹ Voltar") } }
        Box(Modifier.padding(horizontal = 16.dp)) { ScreenHeader("Mudança e aluguel", "Do primeiro anúncio à primeira noite na casa nova.", ill = Ill.CAIXAS, tone = Tone.CORAL) }
        Tabs(listOf("Checklist", "Cronograma", "Documentos", "Custos", "Visita", "Imóveis", "Perguntas"), tab) { tab = it }
        when (tab) {
            0 -> ChecklistTab(c)
            1 -> CronogramaTab(c)
            2 -> DocumentosTab(c)
            3 -> CustosTab(c, nav)
            4 -> VisitaTab(c)
            5 -> ImoveisTab(c)
            else -> PerguntasTab(c, nav)
        }
    }
}

@Composable
private fun ChecklistTab(c: AppContainer) {
    val moving by c.db.checks().byList("MOVING").asState(emptyList())
    val prep by c.db.checks().byList("PREP").asState(emptyList())
    val fb = LocalFeedback.current
    TabBody {
        if (moving.isNotEmpty()) BrandCard(tone = Tone.VERDE) {
            Text("${moving.count { it.done }} de ${moving.size} itens feitos", fontWeight = FontWeight.Bold)
            ProgressLine(moving.count { it.done }.toFloat() / moving.size, "Checklist de mudança")
        }
        CheckListEditor(c, "MOVING", "item", true, moving, title = "Checklist de mudança", groupLabel = "Quando", groupOptions = Templates.MOVING.map { it.group }.distinct(),
            empty = EmptySpec("Checklist vazio", "Use a lista sugerida com 16 passos, do planejamento à primeira semana."),
            onDone = { fb.toast("Check! Mudança andando.", Mood.COMEMORANDO) })
        CheckListEditor(c, "PREP", "tarefa", false, prep, title = "Plano de preparação", description = "Treinos de vida adulta antes de sair de casa.",
            groupLabel = "Fase", groupOptions = Templates.PREP.map { it.group }.distinct(),
            empty = EmptySpec("Nenhuma tarefa de preparação", "Use o plano sugerido com 12 tarefas em 4 fases."),
            templateLabel = "✨ Plano sugerido", onDone = { fb.celebrate("Boa! Mais um passo de gente grande.") })
    }
}

@Composable
private fun CronogramaTab(c: AppContainer) {
    val items by c.db.milestones().all().asState(emptyList())
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    val today = Dates.todayEpoch()
    TabBody {
        CrudList(
            items = items, itemName = "data", title = "Cronograma da mudança", description = "Visitas, assinatura, ligação de serviços, dia da mudança. Aparece no calendário da casa.",
            fields = listOf(
                FieldSpec("title", "O quê", required = true, placeholder = "Ex.: Assinar o contrato"),
                FieldSpec("date", "Quando", FieldType.DATE, true),
                FieldSpec("note", "Anotação", FieldType.LONGTEXT),
                FieldSpec("remind", "Criar lembrete na véspera", FieldType.CHECK),
            ),
            toForm = { m -> if (m == null) mapOf("date" to Init.today(), "remind" to "1") else mapOf("title" to m.title, "date" to Init.date(m.date), "note" to (m.note ?: "")) },
            save = { d, old ->
                val m = MilestoneEntity(id = old?.id ?: 0, title = d.str("title"), date = d.date("date")!!, done = old?.done ?: false, note = d.text("note"), isDemo = old?.isDemo ?: false)
                c.db.milestones().upsert(m)
                if (d.bool("remind")) {
                    val r = ReminderEntity(title = "Amanhã: ${m.title}", kind = "MUDANCA", date = (m.date - 1).coerceAtLeast(today), minuteOfDay = 19 * 60)
                    val id = c.db.reminders().upsert(r); ReminderScheduler.schedule(c.context, r.copy(id = id))
                }
            },
            delete = { c.db.milestones().delete(it) },
            row = { m ->
                RowSpec(m.title, "${Dates.br(m.date)} (${Dates.relative(m.date - today)})${m.note?.let { " · $it" } ?: ""}",
                    checked = m.done, onCheck = { v -> scope.launch { c.db.milestones().upsert(m.copy(done = v)) }; if (v) fb.toast("Feito!", Mood.COMEMORANDO) },
                    tags = when { !m.done && m.date < today -> { { Tag("passou", Tone.CORAL) } }; m.date == today -> { { Tag("hoje", Tone.AMARELO) } }; else -> null })
            },
            empty = EmptySpec("Nenhuma data ainda", "Adicione os marcos: visitas, assinatura do contrato, dia da mudança…"),
            isDemo = { it.isDemo },
        )
    }
}

@Composable
private fun DocumentosTab(c: AppContainer) {
    val items by c.db.checks().byList("DOCS").asState(emptyList())
    TabBody {
        AlertBox("Guarde os documentos numa pasta que vai com você, não no caminhão. Cópias digitais devem ficar protegidas por senha.", "DICA")
        CheckListEditor(c, "DOCS", "documento", true, items, title = "Lista de documentos", groupLabel = "Tipo", groupOptions = Templates.DOCUMENTS.map { it.group }.distinct(),
            noteLabel = "Onde está guardado", empty = EmptySpec("Lista vazia", "Use a lista sugerida: documentos pessoais, renda, contrato, vistoria e garantias."))
        Text("Imobiliárias podem pedir documentos diferentes. Confirme a lista exata com quem vai alugar.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun CustosTab(c: AppContainer, nav: NavController) {
    val p by c.prefs.flow.asState(UserPrefs())
    val v = p.calc["inicial"].orEmpty()
    val r = Calculators.initialCost(
        Calculators.InitialCost(
            aluguel = Money.parse(v["aluguel"]), mesesCaucao = v["caucao"]?.toIntOrNull() ?: 0, primeiroAluguel = v["primeiro"] == "1",
            garantia = Money.parse(v["garantia"]), frete = Money.parse(v["frete"]), moveis = Money.parse(v["moveis"]), eletros = Money.parse(v["eletros"]),
            utensilios = Money.parse(v["utensilios"]), ligacoes = Money.parse(v["ligacoes"]), reparos = Money.parse(v["reparos"]), outros = Money.parse(v["outros"]),
        ),
    )
    TabBody {
        BrandCard(tone = Tone.AMARELO) {
            Stat("Estimativa da mudança", Money.format(r.total))
            if (r.total > 0) Bars(r.itens, Money::format) else Text("Você ainda não fez a estimativa.")
        }
        PrimaryButton(if (r.total > 0) "Editar estimativa" else "Fazer estimativa", Modifier.padding(top = 4.dp)) { nav.navigate(Routes.calc("inicial")) }
        SecondaryButton("🚚 Estimar custo de transporte no dia a dia") { nav.navigate(Routes.calc("transporte")) }
        SectionTitle("Custos que surpreendem")
        listOf("Caução: depósito de até 3 aluguéis, devolvido no fim se estiver tudo certo.", "Seguro contra incêndio, geralmente exigido no contrato.", "Taxas de ligação de luz, água e instalação de internet.", "Frete e montagem de móveis.", "Primeira compra grande de mercado e limpeza.")
            .forEach { Text("• $it") }
        MascotBubble("Guarde um pouco a mais do que o calculado. Mudança sempre tem uma surpresinha.", Mood.DICA, 64.dp)
    }
}

@Composable
private fun VisitaTab(c: AppContainer) {
    val items by c.db.checks().byList("VISIT").asState(emptyList())
    val scope = rememberCoroutineScope()
    val confirm = LocalConfirm.current
    TabBody {
        if (items.isNotEmpty()) BrandCard(tone = Tone.VERDE) {
            Text("${items.count { it.done }} de ${items.size} itens conferidos", fontWeight = FontWeight.Bold)
            ProgressLine(items.count { it.done }.toFloat() / items.size, "Visita")
        }
        CheckListEditor(c, "VISIT", "item", true, items, title = "Checklist para visitar um imóvel", description = "Leve no celular. Depois da visita, cadastre o imóvel no comparador e comece uma nova visita.",
            empty = EmptySpec("Checklist vazio", "Use a lista sugerida com 14 coisas para conferir antes de fechar negócio."),
            extraActions = { if (items.any { it.done }) TextButton(onClick = { confirm.ask("Nova visita?", "Os itens continuam na lista, só desmarcados.", "Nova visita", danger = false) { scope.launch { c.db.checks().reset("VISIT", null) } } }) { Text("↺ Nova visita") } })
    }
}

@Composable
private fun ImoveisTab(c: AppContainer) {
    val items by c.db.properties().all().asState(emptyList())
    val scores = Calculators.compareProperties(items.map { Calculators.PropertyInput(it.id, it.rent, it.condo, it.iptu, it.area, it.transport) }).associateBy { it.id }
    val minTotal = scores.values.minOfOrNull { it.comTransporte }
    val bestRating = items.mapNotNull { it.rating }.maxOrNull()
    TabBody {
        CrudList(
            items = items.sortedBy { scores[it.id]?.comTransporte ?: 0 }, itemName = "imóvel", masculine = true,
            title = "Comparador de imóveis", description = "Valores informados por você. O total inclui aluguel, condomínio, IPTU e, se informado, o transporte.",
            fields = listOf(
                FieldSpec("name", "Apelido do imóvel", required = true, placeholder = "Ex.: Kitnet da Rua das Flores"),
                FieldSpec("address", "Bairro ou endereço"),
                FieldSpec("rent", "Aluguel", FieldType.MONEY, true),
                FieldSpec("condo", "Condomínio", FieldType.MONEY),
                FieldSpec("iptu", "IPTU por mês", FieldType.MONEY),
                FieldSpec("transport", "Transporte por mês a partir dali", FieldType.MONEY),
                FieldSpec("area", "Área (m²)", FieldType.DECIMAL, min = 1.0, max = 2000.0),
                FieldSpec("bedrooms", "Quartos", FieldType.INT, min = 0.0, max = 20.0),
                FieldSpec("distance", "Distância do trabalho/estudo", placeholder = "Ex.: 20 min de ônibus"),
                FieldSpec("guarantee", "Garantia exigida", FieldType.SELECT, options = opts("", "Caução", "Fiador", "Seguro-fiança", "Título de capitalização", "Outra")),
                FieldSpec("rating", "Sua nota (1 a 5)", FieldType.INT, min = 1.0, max = 5.0),
                FieldSpec("pets", "Aceita pet", FieldType.CHECK),
                FieldSpec("furnished", "Mobiliado", FieldType.CHECK),
                FieldSpec("pros", "Pontos positivos", FieldType.LONGTEXT),
                FieldSpec("cons", "Pontos negativos", FieldType.LONGTEXT),
                FieldSpec("link", "Link do anúncio", FieldType.URL),
            ),
            toForm = { p ->
                if (p == null) emptyMap() else mapOf(
                    "name" to p.name, "address" to (p.address ?: ""), "rent" to Init.money(p.rent), "condo" to Init.money(p.condo), "iptu" to Init.money(p.iptu),
                    "transport" to Init.money(p.transport), "area" to Init.num(p.area), "bedrooms" to (p.bedrooms?.toString() ?: ""), "distance" to (p.distance ?: ""),
                    "guarantee" to (p.guarantee ?: ""), "rating" to (p.rating?.toString() ?: ""), "pets" to Init.bool(p.pets), "furnished" to Init.bool(p.furnished),
                    "pros" to (p.pros ?: ""), "cons" to (p.cons ?: ""), "link" to (p.link ?: ""),
                )
            },
            save = { d, old ->
                c.db.properties().upsert(PropertyEntity(id = old?.id ?: 0, name = d.str("name"), address = d.text("address"), rent = d.money("rent")!!, condo = d.money("condo"), iptu = d.money("iptu"),
                    transport = d.money("transport"), area = d.double("area"), bedrooms = d.int("bedrooms"), distance = d.text("distance"), guarantee = d.text("guarantee"), rating = d.int("rating"),
                    pets = d.bool("pets"), furnished = d.bool("furnished"), pros = d.text("pros"), cons = d.text("cons"), link = d.text("link"), isDemo = old?.isDemo ?: false))
            },
            delete = { c.db.properties().delete(it) },
            row = { p ->
                val s = scores[p.id]
                RowSpec(
                    title = p.name,
                    subtitle = listOfNotNull(p.address, p.area?.let { "${Init.num(it)} m²" }, p.bedrooms?.let { "$it quarto(s)" }, p.distance, s?.porM2?.let { "${Money.format(it)}/m²" }).joinToString(" · ") +
                        (p.pros?.let { "\n👍 $it" } ?: "") + (p.cons?.let { "\n👎 $it" } ?: ""),
                    amount = s?.let { Money.format(it.totalMensal) + "/mês" },
                    tags = {
                        if (items.size > 1 && s?.comTransporte == minTotal) Tag("mais em conta", Tone.VERDE)
                        if (items.size > 1 && p.rating != null && p.rating == bestRating) Tag("sua melhor nota", Tone.AMARELO)
                        p.rating?.let { Tag("★".repeat(it)) }
                        if (p.pets) Tag("aceita pet", Tone.AZUL)
                        if (p.furnished) Tag("mobiliado", Tone.ROXO)
                        if ((p.transport ?: 0) > 0) Tag("+ transporte ${Money.format(p.transport)}")
                    },
                )
            },
            empty = EmptySpec("Nenhum imóvel ainda", "Depois de cada visita, cadastre aqui para comparar com calma — e não decidir pela foto bonita."),
            isDemo = { it.isDemo },
        )
        if (items.size >= 2) {
            SectionTitle("Lado a lado")
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                Column(Modifier.width(130.dp)) {
                    listOf("", "Total/mês", "Com transporte", "Aluguel", "Condomínio", "IPTU", "Área", "R$/m²", "Garantia", "Nota").forEach { Text(it, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp)) }
                }
                items.forEach { p ->
                    val s = scores[p.id]
                    Column(Modifier.width(150.dp)) {
                        listOf(
                            p.name, Money.format(s?.totalMensal), Money.format(s?.comTransporte), Money.format(p.rent), p.condo?.let { Money.format(it) } ?: "—",
                            p.iptu?.let { Money.format(it) } ?: "—", p.area?.let { "${Init.num(it)} m²" } ?: "—", s?.porM2?.let { Money.format(it) } ?: "—", p.guarantee ?: "—", p.rating?.let { "$it/5" } ?: "—",
                        ).forEachIndexed { i, t -> Text(t, maxLines = 2, fontWeight = if (i == 0 || (i == 2 && s?.comTransporte == minTotal)) FontWeight.Black else FontWeight.Normal, modifier = Modifier.padding(vertical = 4.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PerguntasTab(c: AppContainer, nav: NavController) {
    val items by c.db.checks().byList("QUESTIONS").asState(emptyList())
    TabBody {
        CheckListEditor(c, "QUESTIONS", "pergunta", false, items, title = "Perguntas para o proprietário ou imobiliária", description = "Marque as que já fez e anote a resposta.",
            groupLabel = "Tema", groupOptions = Templates.QUESTIONS.map { it.group }.distinct(), noteLabel = "Resposta que recebi",
            empty = EmptySpec("Nenhuma pergunta ainda", "Use a lista sugerida com 12 perguntas importantes."))
        SectionTitle("Antes de assinar")
        SecondaryButton("📝 Contrato e garantias") { nav.navigate(Routes.guide("contrato-garantias")) }
        SecondaryButton("✍️ O que conferir antes de assinar") { nav.navigate(Routes.guide("antes-assinar")) }
        SecondaryButton("🚨 Como evitar golpes de aluguel") { nav.navigate(Routes.guide("golpes-aluguel")) }
        AlertBox("Regra de ouro: nada de pagamento antes de visitar o imóvel e assinar o contrato.", "PERIGO")
    }
}
