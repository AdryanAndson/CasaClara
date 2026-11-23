package br.com.casaclara.ui.screens

import androidx.compose.runtime.remember

import androidx.compose.runtime.mutableStateOf

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.casaclara.data.AppContainer
import br.com.casaclara.data.CheckEntity
import br.com.casaclara.data.HouseTaskEntity
import br.com.casaclara.data.ReminderEntity
import br.com.casaclara.data.ShopEntity
import br.com.casaclara.data.WarrantyEntity
import br.com.casaclara.domain.Calculators
import br.com.casaclara.domain.Dates
import br.com.casaclara.domain.Money
import br.com.casaclara.notifications.ReminderScheduler
import br.com.casaclara.ui.FREQ_DAYS
import br.com.casaclara.ui.FREQ_LABEL
import br.com.casaclara.ui.ROOMS
import br.com.casaclara.ui.asState
import br.com.casaclara.ui.billStatus
import br.com.casaclara.ui.components.Ill
import br.com.casaclara.ui.components.AlertBox
import br.com.casaclara.ui.components.BrandCard
import br.com.casaclara.ui.components.CrudList
import br.com.casaclara.ui.components.EmptySpec
import br.com.casaclara.ui.components.FieldSpec
import br.com.casaclara.ui.components.FieldType
import br.com.casaclara.ui.components.Init
import br.com.casaclara.ui.components.LocalConfirm
import br.com.casaclara.ui.components.LocalFeedback
import br.com.casaclara.ui.components.Mood
import br.com.casaclara.ui.components.ProgressLine
import br.com.casaclara.ui.components.RowSpec
import br.com.casaclara.ui.components.ScreenHeader
import br.com.casaclara.ui.components.SecondaryButton
import br.com.casaclara.ui.components.SectionTitle
import br.com.casaclara.ui.components.SelectField
import br.com.casaclara.ui.components.TabBody
import br.com.casaclara.ui.components.Tabs
import br.com.casaclara.ui.components.Tag
import br.com.casaclara.ui.components.Tone
import br.com.casaclara.ui.components.opts
import br.com.casaclara.ui.taskStatus
import br.com.casaclara.ui.theme.LocalSoft
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun HouseScreen(c: AppContainer, nav: NavController, initialTab: Int) {
    var tab by rememberSaveable { mutableIntStateOf(initialTab) }
    Column {
        Box(Modifier.padding(horizontal = 16.dp)) { ScreenHeader("Casa", "Pouco todo dia vence a faxina de seis horas.", ill = Ill.VASSOURA, tone = Tone.AZUL) }
        Tabs(listOf("Tarefas", "Limpeza por cômodo", "Calendário", "Compras da casa", "Inventário", "Garantias e notas"), tab) { tab = it }
        when (tab) {
            0 -> TarefasTab(c, nav)
            1 -> LimpezaTab(c)
            2 -> CalendarioTab(c)
            3 -> TabBody { ShoppingList(c, "CASA", listOf("Limpeza", "Higiene", "Utensílios", "Manutenção", "Decoração", "Outros"), "Compras para a casa", "Produtos de limpeza, utensílios, lâmpadas e afins.") }
            4 -> InventarioTab(c)
            else -> GarantiasTab(c)
        }
    }
}

@Composable
private fun TarefasTab(c: AppContainer, nav: NavController) {
    val tasks by c.db.houseTasks().all().asState(emptyList())
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    val pend = tasks.count { taskStatus(it).days <= 0 }
    TabBody {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            br.com.casaclara.ui.components.SecondaryButton("🗓️ Semana", Modifier.weight(1f)) { nav.navigate(br.com.casaclara.domain.Routes.WEEKPLAN) }
            br.com.casaclara.ui.components.SecondaryButton("⏱️ Faxina", Modifier.weight(1f)) { nav.navigate(br.com.casaclara.domain.Routes.TIMER) }
        }
        if (tasks.isNotEmpty()) BrandCard(tone = Tone.VERDE) {
            Text(if (pend > 0) "$pend para hoje ou atrasada(s)" else "Tudo em dia! ✨", fontWeight = FontWeight.Bold)
            ProgressLine((tasks.size - pend).toFloat() / tasks.size, "Tarefas em dia")
            Text("Atrasou? Tudo bem. Aqui não tem sequência punitiva: é só fazer quando der.", style = MaterialTheme.typography.bodySmall)
        }
        CrudList(
            items = tasks.sortedBy { taskStatus(it).days },
            itemName = "tarefa",
            title = "Tarefas diárias, semanais e mensais",
            fields = listOf(
                FieldSpec("title", "Tarefa", required = true),
                FieldSpec("frequency", "Frequência", FieldType.SELECT, true, FREQ_LABEL.toList()),
                FieldSpec("room", "Cômodo", FieldType.SELECT, options = opts(listOf("") + ROOMS)),
                FieldSpec("lastDone", "Última vez que fiz", FieldType.DATE),
            ),
            toForm = { t -> if (t == null) mapOf("frequency" to "SEMANAL") else mapOf("title" to t.title, "frequency" to t.frequency, "room" to (t.room ?: ""), "lastDone" to Init.date(t.lastDone)) },
            save = { d, old -> c.db.houseTasks().upsert(HouseTaskEntity(id = old?.id ?: 0, title = d.str("title"), frequency = d.str("frequency"), room = d.text("room"), lastDone = d.date("lastDone"), doneCount = old?.doneCount ?: 0, isDemo = old?.isDemo ?: false)) },
            delete = { c.db.houseTasks().delete(it) },
            groupBy = { FREQ_LABEL[it.frequency] ?: "Outras" }, groupOrder = FREQ_LABEL.values.toList(),
            row = { t ->
                val s = taskStatus(t)
                RowSpec(
                    title = t.title,
                    subtitle = (t.room?.let { "$it · " } ?: "") + if (s.never) "ainda não registrada" else "última: ${Dates.br(t.lastDone)} · próxima ${Dates.relative(s.days)}",
                    tags = { if (s.never || s.days <= 0) Tag(if (s.late) "atrasada" else "para hoje", if (s.late) Tone.CORAL else Tone.AMARELO) else Tag("em dia", Tone.VERDE) },
                    extra = {
                        val hoje = t.lastDone == Dates.todayEpoch()
                        TextButton(enabled = !hoje, onClick = { scope.launch { c.markTaskDone(t) }; fb.toast("Feito! Sua casa agradece.", Mood.COMEMORANDO) }) { Text(if (hoje) "✓ Hoje" else "Feito") }
                        TextButton(onClick = {
                            scope.launch {
                                val rep = when (t.frequency) { "DIARIA" -> "DIARIO"; "SEMANAL" -> "SEMANAL"; else -> "MENSAL" }
                                val r = ReminderEntity(title = t.title, kind = if (t.frequency == "SEMESTRAL") "MANUTENCAO" else "TAREFA", date = s.next.toEpochDay().coerceAtLeast(Dates.todayEpoch()), repeat = rep)
                                val id = c.db.reminders().upsert(r); ReminderScheduler.schedule(c.context, r.copy(id = id))
                            }
                            fb.toast("Lembrete criado. Ajuste em Lembretes.", Mood.DICA)
                        }) { Text("🔔") }
                    },
                )
            },
            empty = EmptySpec("Nenhuma tarefa ainda", "Use a rotina sugerida (diárias, semanais e mensais) ou crie a sua."),
            headerActions = { TextButton(onClick = { scope.launch { c.loadHouseTasks() }; fb.toast("Rotina sugerida adicionada. Edite à vontade!", Mood.COMEMORANDO) }) { Text("✨ Rotina sugerida") } },
            isDemo = { it.isDemo },
        )
    }
}

@Composable
private fun LimpezaTab(c: AppContainer) {
    val items by c.db.checks().byList("CLEANING").asState(emptyList())
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    val confirm = LocalConfirm.current
    TabBody {
        val rooms = items.groupBy { it.grp }
        rooms.forEach { (room, list) ->
            val d = list.count { it.done }
            BrandCard(padding = 12.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(room, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Text("$d/${list.size}")
                    if (d > 0) TextButton(onClick = { confirm.ask("Recomeçar $room?", "Desmarca os itens deste cômodo para a próxima limpeza.", "Recomeçar", danger = false) { scope.launch { c.db.checks().reset("CLEANING", room) } } }) { Text("↺") }
                }
                ProgressLine(d.toFloat() / list.size, "Limpeza de $room")
            }
        }
        CheckListEditor(
            c, "CLEANING", "item", masculine = true, items = items, title = "Limpeza por cômodo", groupLabel = "Cômodo", groupOptions = ROOMS,
            empty = EmptySpec("Nenhuma lista ainda", "Use a lista sugerida para cozinha, banheiro, quarto, sala e área de serviço."),
            onDone = { e -> if (items.none { it.grp == e.grp && it.id != e.id && !it.done }) fb.celebrate("${e.grp} brilhando! ✨") },
        )
        AlertBox("Nunca misture produtos de limpeza. Use um por vez, com janela aberta.", "PERIGO")
    }
}

/** Editor genérico das listas marcáveis (preparação, mudança, documentos, visita, perguntas, limpeza). */
@Composable
fun CheckListEditor(
    c: AppContainer, list: String, itemName: String, masculine: Boolean = false, items: List<CheckEntity>, title: String? = null, description: String? = null,
    groupLabel: String? = null, groupOptions: List<String> = emptyList(), empty: EmptySpec, noteLabel: String = "Anotação",
    templateLabel: String = "✨ Lista sugerida", onDone: (CheckEntity) -> Unit = {}, extraActions: (@Composable () -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    val fields = buildList {
        add(FieldSpec("title", "Texto", required = true))
        if (groupLabel != null) add(FieldSpec("grp", groupLabel, if (groupOptions.isEmpty()) FieldType.TEXT else FieldType.SELECT, options = opts(groupOptions + "Outro")))
        add(FieldSpec("note", noteLabel, FieldType.LONGTEXT))
    }
    CrudList(
        items = items, itemName = itemName, masculine = masculine, title = title, description = description, fields = fields,
        toForm = { e -> if (e == null) mapOf("grp" to (groupOptions.firstOrNull() ?: "")) else mapOf("title" to e.title, "grp" to e.grp, "note" to (e.note ?: "")) },
        save = { d, old -> c.db.checks().upsert(CheckEntity(id = old?.id ?: 0, list = list, grp = d.text("grp") ?: "", title = d.str("title"), note = d.text("note"), done = old?.done ?: false, isDemo = old?.isDemo ?: false)) },
        delete = { c.db.checks().delete(it) },
        groupBy = if (groupLabel != null) { e -> e.grp.ifBlank { "Outro" } } else null,
        groupOrder = groupOptions,
        row = { e -> RowSpec(e.title, e.note, checked = e.done, onCheck = { v -> scope.launch { c.db.checks().upsert(e.copy(done = v)) }; if (v) onDone(e) }) },
        empty = empty,
        isDemo = { it.isDemo },
        headerActions = {
            extraActions?.invoke()
            TextButton(onClick = { scope.launch { c.loadChecklist(list) }; fb.toast("Lista sugerida adicionada. Edite à vontade!", Mood.COMEMORANDO) }) { Text(templateLabel) }
        },
    )
}

private data class CalEvent(val kind: String, val title: String)

@Composable
private fun CalendarioTab(c: AppContainer) {
    val bills by c.db.bills().all().asState(emptyList())
    val tasks by c.db.houseTasks().all().asState(emptyList())
    val milestones by c.db.milestones().all().asState(emptyList())
    val reminders by c.db.reminders().all().asState(emptyList())
    var month by rememberSaveable { mutableIntStateOf(Dates.currentMonthKey()) }
    var sel by rememberSaveable { mutableLongStateOf(Dates.todayEpoch()) }
    val ym = Dates.yearMonth(month)
    val start = ym.atDay(1); val end = ym.atEndOfMonth()
    val events = HashMap<Long, MutableList<CalEvent>>()
    fun add(d: LocalDate, e: CalEvent) { if (!d.isBefore(start) && !d.isAfter(end)) events.getOrPut(d.toEpochDay()) { mutableListOf() }.add(e) }
    bills.forEach { add(Dates.dueInMonth(it.dueDay, month), CalEvent("Conta", it.name + (it.amount?.let { v -> " · " + Money.format(v) } ?: ""))) }
    val today = Dates.today()
    tasks.forEach { t ->
        val step = FREQ_DAYS[t.frequency] ?: 7L
        var d = taskStatus(t).next.let { if (it.isBefore(today)) today else it }
        var guard = 0
        while (d.isBefore(start) && guard++ < 500) d = d.plusDays(step)
        guard = 0
        while (!d.isAfter(end) && guard++ < 40) { add(d, CalEvent("Tarefa", t.title)); d = d.plusDays(step) }
    }
    milestones.forEach { add(Dates.of(it.date), CalEvent("Mudança", it.title)) }
    reminders.filter { it.enabled }.forEach { add(Dates.of(it.date), CalEvent("Lembrete", it.title)) }
    val soft = LocalSoft.current
    TabBody {
        MonthSwitcher(month) { month = it }
        Row { listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom").forEach { Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium) } }
        val offset = start.dayOfWeek.value - 1
        val cells = List(offset) { null } + (1..end.dayOfMonth).map { it }
        cells.chunked(7).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                (0 until 7).forEach { i ->
                    val day = week.getOrNull(i)
                    if (day == null) Box(Modifier.weight(1f).aspectRatio(0.8f))
                    else {
                        val date = start.withDayOfMonth(day)
                        val evs = events[date.toEpochDay()].orEmpty()
                        val isSel = sel == date.toEpochDay()
                        Card(
                            onClick = { sel = date.toEpochDay() },
                            modifier = Modifier.weight(1f).aspectRatio(0.8f).semantics { contentDescription = "$day: " + if (evs.isEmpty()) "sem eventos" else evs.joinToString { it.title } },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(if (isSel) 3.dp else 1.dp, if (isSel) MaterialTheme.colorScheme.secondary else soft.contorno),
                            colors = CardDefaults.cardColors(containerColor = if (date == today) soft.amarelo else MaterialTheme.colorScheme.surface),
                        ) {
                            Column(Modifier.padding(3.dp)) {
                                Text("$day", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium)
                                evs.take(3).forEach { e ->
                                    Box(Modifier.fillMaxWidth().padding(top = 2.dp)) {
                                        Text("●", color = when (e.kind) { "Conta" -> br.com.casaclara.ui.theme.Brand.Coral; "Tarefa" -> br.com.casaclara.ui.theme.Brand.Verde; "Mudança" -> br.com.casaclara.ui.theme.Brand.Azul; else -> br.com.casaclara.ui.theme.Brand.Amarelo }, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Clip)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Tag("● Conta", Tone.CORAL); Tag("● Tarefa", Tone.VERDE); Tag("● Mudança", Tone.AZUL); Tag("● Lembrete", Tone.AMARELO) }
        SectionTitle(Dates.br(sel))
        val evs = events[sel].orEmpty()
        if (evs.isEmpty()) Text("Nada marcado neste dia.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        evs.forEach { e -> br.com.casaclara.ui.components.ItemRow(title = e.title, tags = { Tag(e.kind, when (e.kind) { "Conta" -> Tone.CORAL; "Tarefa" -> Tone.VERDE; "Mudança" -> Tone.AZUL; else -> Tone.AMARELO }) }) }
        if (bills.isEmpty() && tasks.isEmpty() && milestones.isEmpty()) Text("O calendário junta suas contas, tarefas, datas da mudança e lembretes.", style = MaterialTheme.typography.bodySmall)
    }
}

/** Lista de compras reutilizada no mercado e nas compras da casa. */
@Composable
fun ShoppingList(c: AppContainer, list: String, categories: List<String>, title: String, description: String, favoritesOnly: Boolean = false) {
    val items by c.db.shop().byList(list).asState(emptyList(), list)
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    val confirm = LocalConfirm.current
    val shown = if (favoritesOnly) items.filter { it.favorite } else items
    val withPrice = shown.filter { (it.price ?: 0) > 0 }
    val total = withPrice.sumOf { (it.qty * (it.price ?: 0)).toLong() }
    val inCart = withPrice.filter { it.bought }.sumOf { (it.qty * (it.price ?: 0)).toLong() }
    val semPreco = shown.size - withPrice.size
    var pickList by remember { mutableStateOf(false) }
    val ready = br.com.casaclara.domain.content.ListTemplates.ALL.filter { it.target == list }
    if (!favoritesOnly && ready.isNotEmpty()) {
        br.com.casaclara.ui.components.SecondaryButton("📋 Usar uma lista pronta", Modifier.fillMaxWidth()) { pickList = true }
        if (pickList) androidx.compose.material3.AlertDialog(
            onDismissRequest = { pickList = false },
            title = { Text("Listas prontas") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Os itens entram na sua lista sem preço (você informa). Itens que já estão lá não se repetem.", style = MaterialTheme.typography.bodySmall)
                    ready.forEach { lt ->
                        BrandCard(padding = 10.dp, onClick = {
                            val have = items.map { it.name.trim().lowercase() }.toSet()
                            val add = lt.items.filter { it.name.trim().lowercase() !in have }
                            scope.launch { add.forEach { i -> c.db.shop().upsert(ShopEntity(list = list, name = i.name, category = if (i.category in categories) i.category else categories.last(), qty = i.qty, unit = i.unit)) } }
                            pickList = false
                            fb.toast(if (add.isEmpty()) "Esses itens já estão na sua lista." else "${add.size} item(ns) adicionados!", Mood.FELIZ)
                        }) {
                            Text("${lt.emoji} ${lt.title}", fontWeight = FontWeight.Bold)
                            Text("${lt.items.size} itens · ${lt.description}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { pickList = false }) { Text("Fechar") } },
        )
    }
    if (shown.isNotEmpty()) BrandCard(tone = Tone.AMARELO) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            br.com.casaclara.ui.components.Stat("Estimativa", Money.format(total), Modifier.weight(1f))
            br.com.casaclara.ui.components.Stat("No carrinho", Money.format(inCart), Modifier.weight(1f))
        }
        Text("${shown.count { it.bought }}/${shown.size} itens marcados", fontWeight = FontWeight.Bold)
        if (semPreco > 0) Text("$semPreco item(ns) sem preço não entram na estimativa. Os preços são os que você informar.", style = MaterialTheme.typography.bodySmall)
    }
    CrudList(
        items = shown, itemName = "item", masculine = true, title = title, description = description,
        fields = listOf(
            FieldSpec("name", "Item", required = true),
            FieldSpec("category", "Categoria", FieldType.SELECT, true, opts(categories)),
            FieldSpec("qty", "Quantidade", FieldType.DECIMAL, min = 0.01, max = 9999.0),
            FieldSpec("unit", "Unidade", FieldType.SELECT, options = opts("un", "kg", "g", "L", "ml", "pct", "cx", "dz")),
            FieldSpec("price", "Preço unitário (você informa)", FieldType.MONEY),
            FieldSpec("favorite", "Favorito (sempre compro)", FieldType.CHECK),
        ),
        toForm = { s -> if (s == null) mapOf("category" to categories.first(), "qty" to "1", "unit" to "un", "favorite" to Init.bool(favoritesOnly)) else mapOf("name" to s.name, "category" to s.category, "qty" to Init.num(s.qty), "unit" to s.unit, "price" to Init.money(s.price), "favorite" to Init.bool(s.favorite)) },
        save = { d, old -> c.db.shop().upsert(ShopEntity(id = old?.id ?: 0, list = list, name = d.str("name"), category = d.str("category"), qty = d.double("qty") ?: 1.0, unit = d.text("unit") ?: "un", price = d.money("price"), favorite = d.bool("favorite"), bought = old?.bought ?: false, isDemo = old?.isDemo ?: false)) },
        delete = { c.db.shop().delete(it) },
        groupBy = { it.category }, groupOrder = categories,
        row = { s ->
            RowSpec(
                title = s.name,
                subtitle = "${Init.num(s.qty)} ${s.unit}" + (s.price?.let { " × ${Money.format(it)}" } ?: " · sem preço"),
                amount = s.price?.let { Money.format((s.qty * it).toLong()) },
                checked = s.bought, onCheck = { v -> scope.launch { c.db.shop().upsert(s.copy(bought = v)) } },
                tags = if (s.favorite) { { Tag("⭐ favorito", Tone.AMARELO) } } else null,
                extra = { TextButton(onClick = { scope.launch { c.db.shop().upsert(s.copy(favorite = !s.favorite)) } }) { Text(if (s.favorite) "★" else "☆") } },
            )
        },
        empty = EmptySpec(if (favoritesOnly) "Nenhum favorito ainda" else "Lista vazia", if (favoritesOnly) "Marque com ☆ os itens que você sempre compra. Eles voltam para a lista quando você limpa os comprados." else "Adicione o que precisa comprar. Se quiser, informe o preço para estimar o gasto."),
        isDemo = { it.isDemo },
        headerActions = {
            if (items.any { it.bought }) TextButton(onClick = {
                confirm.ask("Limpar itens comprados?", "Itens comprados saem da lista. Os favoritos ficam, desmarcados, para a próxima compra.", "Limpar", danger = false) {
                    scope.launch { c.db.shop().clearBought(list); c.db.shop().resetFavorites(list) }; fb.toast("Lista pronta para a próxima compra!")
                }
            }) { Text("🧹 Limpar comprados") }
        },
    )
}

@Composable
private fun InventarioTab(c: AppContainer) {
    val items by c.db.shop().byList("INVENTARIO").asState(emptyList())
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    val estados = linkedMapOf("OK" to "Tem bastante", "ACABANDO" to "Acabando", "FALTA" to "Acabou")
    val faltando = items.filter { it.state != "OK" }
    TabBody {
        if (faltando.isNotEmpty()) AlertBox("${faltando.size} item(ns) acabando ou em falta. Use 🛒 para mandar para a lista de compras.", "DICA")
        CrudList(
            items = items, itemName = "item", masculine = true, title = "Inventário de essenciais", description = "Saiba o que tem em casa antes de acabar o papel higiênico na pior hora.",
            fields = listOf(
                FieldSpec("name", "Item", required = true),
                FieldSpec("category", "Onde fica", FieldType.SELECT, options = opts(ROOMS)),
                FieldSpec("qty", "Quantidade", FieldType.DECIMAL, min = 0.0, max = 9999.0),
                FieldSpec("state", "Situação", FieldType.SELECT, true, estados.toList()),
            ),
            toForm = { s -> if (s == null) mapOf("category" to "Casa toda", "state" to "OK") else mapOf("name" to s.name, "category" to s.category, "qty" to Init.num(s.qty), "state" to s.state) },
            save = { d, old -> c.db.shop().upsert(ShopEntity(id = old?.id ?: 0, list = "INVENTARIO", name = d.str("name"), category = d.text("category") ?: "Casa toda", qty = d.double("qty") ?: 1.0, state = d.str("state"), isDemo = old?.isDemo ?: false)) },
            delete = { c.db.shop().delete(it) },
            groupBy = { it.category }, groupOrder = ROOMS,
            row = { s ->
                RowSpec(
                    title = s.name, subtitle = "Quantidade: ${Init.num(s.qty)}",
                    tags = { Tag(estados[s.state] ?: s.state, when (s.state) { "OK" -> Tone.VERDE; "ACABANDO" -> Tone.AMARELO; else -> Tone.CORAL }) },
                    extra = {
                        val next = when (s.state) { "OK" -> "ACABANDO"; "ACABANDO" -> "FALTA"; else -> "OK" }
                        TextButton(onClick = { scope.launch { c.db.shop().upsert(s.copy(state = next)) } }) { Text("↻") }
                        if (s.state != "OK") TextButton(onClick = { scope.launch { c.db.shop().upsert(ShopEntity(list = "CASA", name = s.name, category = "Outros")) }; fb.toast("Na lista de compras da casa!") }) { Text("🛒") }
                    },
                )
            },
            empty = EmptySpec("Inventário vazio", "Comece pelos essenciais: papel higiênico, detergente, sabão, saco de lixo, lanterna…"),
            isDemo = { it.isDemo },
            headerActions = { TextButton(onClick = { scope.launch { c.loadInventory() }; fb.toast("Essenciais adicionados!", Mood.COMEMORANDO) }) { Text("✨ Essenciais") } },
        )
    }
}

@Composable
fun GarantiasTab(c: AppContainer) {
    val items by c.db.warranties().all().asState(emptyList())
    val furniture by c.db.furniture().all().asState(emptyList())
    TabBody {
        CrudList(
            items = items, itemName = "garantia",
            title = "Garantias e notas fiscais", description = "Anote a data da compra e o prazo: o app avisa quando estiver perto de vencer.",
            fields = listOf(
                FieldSpec("product", "Produto ou serviço", required = true),
                FieldSpec("store", "Loja ou prestador"),
                FieldSpec("purchaseDate", "Data da compra", FieldType.DATE),
                FieldSpec("months", "Garantia (meses)", FieldType.INT, min = 0.0, max = 120.0, help = "Garantia legal mínima de produto durável: 90 dias. Confira a do fabricante."),
                FieldSpec("value", "Valor", FieldType.MONEY),
                FieldSpec("invoice", "Nº da nota ou onde guardei"),
                FieldSpec("note", "Anotações", FieldType.LONGTEXT),
            ),
            toForm = { w -> if (w == null) mapOf("purchaseDate" to Init.today()) else mapOf("product" to w.product, "store" to (w.store ?: ""), "purchaseDate" to Init.date(w.purchaseDate), "months" to (w.months?.toString() ?: ""), "value" to Init.money(w.value), "invoice" to (w.invoice ?: ""), "note" to (w.note ?: "")) },
            save = { d, old -> c.db.warranties().upsert(WarrantyEntity(id = old?.id ?: 0, product = d.str("product"), store = d.text("store"), purchaseDate = d.date("purchaseDate"), months = d.int("months"), value = d.money("value"), invoice = d.text("invoice"), note = d.text("note"), isDemo = old?.isDemo ?: false)) },
            delete = { c.db.warranties().delete(it) },
            row = { w -> RowSpec(w.product, listOfNotNull(w.store, w.purchaseDate?.let { "comprado em ${Dates.br(it)}" }, w.invoice?.let { "NF: $it" }).joinToString(" · "), w.value?.let { Money.format(it) }, tags = warrantyTag(w.purchaseDate, w.months)) },
            empty = EmptySpec("Nenhuma garantia anotada", "A nota fiscal some justo quando o produto quebra. Anote aqui."),
            isDemo = { it.isDemo },
        )
        val fromFurniture = furniture.filter { it.bought && (it.warrantyMonths != null || it.invoice != null) }
        if (fromFurniture.isNotEmpty()) {
            SectionTitle("Da sua lista de móveis")
            fromFurniture.forEach { f -> br.com.casaclara.ui.components.ItemRow(f.name, listOfNotNull(f.store, f.invoice?.let { "NF: $it" }).joinToString(" · "), tags = warrantyTag(f.purchaseDate, f.warrantyMonths)) }
        }
    }
}

fun warrantyTag(purchase: Long?, months: Int?): (@Composable () -> Unit)? {
    if (purchase == null || months == null) return null
    val end = Dates.of(purchase).plusMonths(months.toLong())
    val d = Dates.daysBetween(Dates.today(), end)
    return {
        when {
            d < 0 -> Tag("garantia vencida")
            d <= 30 -> Tag("vence em $d dias", Tone.AMARELO)
            else -> Tag("até ${Dates.br(end.toEpochDay())}", Tone.VERDE)
        }
    }
}
