package br.com.casaclara.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.casaclara.data.AppContainer
import br.com.casaclara.data.CardEntity
import br.com.casaclara.data.GoalEntity
import br.com.casaclara.data.RecurringEntity
import br.com.casaclara.data.TxEntity
import br.com.casaclara.domain.Cards
import br.com.casaclara.domain.Dates
import br.com.casaclara.domain.Money
import br.com.casaclara.domain.StatementImport
import br.com.casaclara.ui.EXPENSE_CATEGORIES
import br.com.casaclara.ui.INCOME_CATEGORIES
import br.com.casaclara.ui.asState
import br.com.casaclara.ui.components.AlertBox
import br.com.casaclara.ui.components.BrandCard
import br.com.casaclara.ui.components.CrudList
import br.com.casaclara.ui.components.EmptySpec
import br.com.casaclara.ui.components.EmptyState
import br.com.casaclara.ui.components.FieldSpec
import br.com.casaclara.ui.components.FieldType
import br.com.casaclara.ui.components.Ill
import br.com.casaclara.ui.components.Init
import br.com.casaclara.ui.components.ItemRow
import br.com.casaclara.ui.components.LocalFeedback
import br.com.casaclara.ui.components.MoneyInput
import br.com.casaclara.ui.components.Mood
import br.com.casaclara.ui.components.Page
import br.com.casaclara.ui.components.PrimaryButton
import br.com.casaclara.ui.components.ProgressLine
import br.com.casaclara.ui.components.RowSpec
import br.com.casaclara.ui.components.ScreenHeader
import br.com.casaclara.ui.components.SecondaryButton
import br.com.casaclara.ui.components.SectionTitle
import br.com.casaclara.ui.components.SelectField
import br.com.casaclara.ui.components.Stat
import br.com.casaclara.ui.components.TabBody
import br.com.casaclara.ui.components.Tag
import br.com.casaclara.ui.components.Tone
import br.com.casaclara.ui.components.opts
import br.com.casaclara.ui.theme.Brand
import br.com.casaclara.ui.theme.LocalSoft
import kotlinx.coroutines.launch

private val CARD_COLORS = listOf("AZUL" to "Azul", "ROXO" to "Roxo", "VERDE" to "Verde", "CORAL" to "Coral", "AMARELO" to "Amarelo")
private fun cardTone(c: String) = when (c) { "ROXO" -> Tone.ROXO; "VERDE" -> Tone.VERDE; "CORAL" -> Tone.CORAL; "AMARELO" -> Tone.AMARELO; else -> Tone.AZUL }

// ------------------------------------------------------------------ Cartões de crédito
@Composable
fun CartoesTab(c: AppContainer) {
    val cards by c.db.cards().all().asState(emptyList())
    val tx by c.db.tx().all().asState(emptyList())
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    val today = Dates.today()
    TabBody {
        AlertBox("Compras no cartão entram no mês em que você compra (como gasto). Aqui você acompanha as faturas e o limite. Para lançar uma compra no cartão, escolha o cartão em Lançamentos.", "INFO")
        cards.forEach { card ->
            val purchases = tx.filter { it.cardId == card.id && it.kind == "SAIDA" }.map { Cards.Purchase(it.amount, Dates.of(it.date), it.installments) }
            val inv = Cards.invoices(purchases, card.closingDay, card.dueDay)
            val paid = card.paidSet()
            val openKey = Cards.invoiceKey(today, card.closingDay, card.dueDay)
            val used = Cards.usedLimit(inv, paid)
            BrandCard(tone = cardTone(card.color)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("💳 ${card.name}", style = MaterialTheme.typography.titleLarge)
                        Text("Fecha dia ${card.closingDay} · vence dia ${card.dueDay}", style = MaterialTheme.typography.bodySmall)
                    }
                    br.com.casaclara.ui.components.Illustration(Ill.CARTAO, 64.dp)
                }
                Stat("Fatura aberta (vence ${Dates.monthLabel(openKey).lowercase()})", Money.format(inv[openKey] ?: 0))
                card.limitAmount?.let { lim ->
                    Text("Limite usado: ${Money.format(used)} de ${Money.format(lim)} · disponível ${Money.format(lim - used)}", fontWeight = FontWeight.Bold)
                    ProgressLine(used.toFloat() / lim.coerceAtLeast(1), "Limite usado do ${card.name}", if (used > lim * 0.8) Brand.Coral else Brand.Verde)
                }
                inv.filterKeys { it >= Dates.shiftMonth(openKey, -2) }.forEach { (k, v) ->
                    val isPaid = k in paid
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${Dates.monthLabel(k)}: ${Money.format(v)}", modifier = Modifier.weight(1f), fontWeight = if (k == openKey) FontWeight.Black else FontWeight.Normal)
                        if (isPaid) Tag("paga", Tone.VERDE)
                        TextButton(onClick = {
                            val n = if (isPaid) paid - k else paid + k
                            scope.launch { c.db.cards().upsert(card.copy(paidInvoices = n.joinToString(","))) }
                            if (!isPaid) fb.celebrate("Fatura paga! Limite liberado.")
                        }) { Text(if (isPaid) "Desfazer" else "Paguei") }
                    }
                }
                if (inv.isEmpty()) Text("Nenhuma compra lançada neste cartão ainda.", style = MaterialTheme.typography.bodySmall)
            }
        }
        CrudList(
            items = cards, itemName = "cartão", masculine = true, title = "Meus cartões",
            fields = listOf(
                FieldSpec("name", "Nome do cartão", required = true, placeholder = "Ex.: Nubank, Inter"),
                FieldSpec("limit", "Limite", FieldType.MONEY),
                FieldSpec("closing", "Dia do fechamento", FieldType.DAY, true, help = "Compras depois desse dia vão para a fatura seguinte."),
                FieldSpec("due", "Dia do vencimento", FieldType.DAY, true),
                FieldSpec("color", "Cor", FieldType.SELECT, options = CARD_COLORS),
            ),
            toForm = { k -> if (k == null) mapOf("color" to "AZUL") else mapOf("name" to k.name, "limit" to Init.money(k.limitAmount), "closing" to k.closingDay.toString(), "due" to k.dueDay.toString(), "color" to k.color) },
            save = { d, old -> c.db.cards().upsert(CardEntity(id = old?.id ?: 0, name = d.str("name"), limitAmount = d.money("limit"), closingDay = d.int("closing")!!, dueDay = d.int("due")!!, color = d.text("color") ?: "AZUL", paidInvoices = old?.paidInvoices ?: "", isDemo = old?.isDemo ?: false)) },
            delete = { c.db.cards().delete(it) },
            row = { k -> RowSpec("💳 ${k.name}", "Fecha dia ${k.closingDay} · vence dia ${k.dueDay}", k.limitAmount?.let { "limite " + Money.format(it) }) },
            empty = EmptySpec("Nenhum cartão cadastrado", "Cadastre seu cartão com o dia de fechamento e de vencimento para acompanhar a fatura e o limite."),
            isDemo = { it.isDemo },
            canDelete = { k -> if (tx.any { it.cardId == k.id }) "Esse cartão tem compras lançadas. Edite ou exclua as compras antes." else null },
        )
    }
}

// ------------------------------------------------------------------ Recorrentes
@Composable
fun RecorrentesTab(c: AppContainer) {
    val items by c.db.recurring().all().asState(emptyList())
    val cards by c.db.cards().all().asState(emptyList())
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    val cardOpts = listOf("" to "Não é no cartão") + cards.map { it.id.toString() to "💳 ${it.name}" }
    TabBody {
        AlertBox("Salário, aluguel, assinaturas… cadastre uma vez e o app lança sozinho todo mês, no dia certo. Você pode editar ou apagar o lançamento gerado como qualquer outro.", "DICA")
        CrudList(
            items = items, itemName = "recorrente", masculine = true, title = "Lançamentos recorrentes",
            fields = listOf(
                FieldSpec("kind", "Tipo", FieldType.SELECT, true, listOf("SAIDA" to "Gasto", "ENTRADA" to "Entrada")),
                FieldSpec("description", "Descrição", required = true, placeholder = "Ex.: Aluguel, Salário, Streaming"),
                FieldSpec("amount", "Valor", FieldType.MONEY, true, validate = { d -> if ((d.money("amount") ?: 0) <= 0) "O valor precisa ser maior que zero." else null }),
                FieldSpec("category", "Categoria", FieldType.SELECT, true, opts((EXPENSE_CATEGORIES + INCOME_CATEGORIES).distinct())),
                FieldSpec("day", "Dia do mês", FieldType.DAY, true),
                FieldSpec("card", "Cartão (para gastos)", FieldType.SELECT, options = cardOpts),
                FieldSpec("start", "Começa em", FieldType.DATE, true, help = "Meses anteriores a esta data não são lançados."),
                FieldSpec("active", "Ativo", FieldType.CHECK),
            ),
            toForm = { r -> if (r == null) mapOf("kind" to "SAIDA", "category" to "Assinaturas", "start" to Init.today(), "active" to "1") else mapOf("kind" to r.kind, "description" to r.description, "amount" to Init.money(r.amount), "category" to r.category, "day" to r.day.toString(), "card" to (r.cardId?.toString() ?: ""), "start" to Init.date(Dates.yearMonth(r.startMonth).atDay(1).toEpochDay()), "active" to Init.bool(r.active)) },
            save = { d, old ->
                val kind = d.str("kind")
                c.db.recurring().upsert(RecurringEntity(id = old?.id ?: 0, kind = kind, description = d.str("description"), amount = d.money("amount")!!, category = d.str("category"),
                    day = d.int("day")!!, startMonth = Dates.monthKey(d.date("start")!!), lastGenerated = old?.lastGenerated, active = d.bool("active"),
                    cardId = if (kind == "SAIDA") d.text("card")?.toLongOrNull() else null, incomeType = if (kind == "ENTRADA") "FIXA" else null, isDemo = old?.isDemo ?: false))
                val n = c.runRecurring()
                if (n > 0) fb.toast("$n lançamento(s) criado(s) automaticamente.", Mood.COMEMORANDO)
            },
            delete = { c.db.recurring().delete(it) },
            row = { r ->
                RowSpec(
                    (if (r.kind == "ENTRADA") "💵 " else "🔁 ") + r.description,
                    "Todo dia ${r.day} · ${r.category}" + (r.lastGenerated?.let { " · último: ${Dates.monthLabel(it).lowercase()}" } ?: " · ainda não lançado"),
                    (if (r.kind == "ENTRADA") "+ " else "− ") + Money.format(r.amount),
                    tags = { if (!r.active) Tag("pausado"); if (r.cardId != null) Tag("cartão", Tone.AZUL) },
                    extra = { TextButton(onClick = { scope.launch { c.db.recurring().upsert(r.copy(active = !r.active)) } }) { Text(if (r.active) "⏸" else "▶") } },
                )
            },
            empty = EmptySpec("Nada recorrente ainda", "Cadastre o que se repete todo mês — e pare de anotar a mesma coisa sempre."),
            isDemo = { it.isDemo },
        )
    }
}

// ------------------------------------------------------------------ Cofrinho desenhado
@Composable
fun Jar(fill: Float, size: Dp = 64.dp, label: String) {
    val soft = LocalSoft.current
    val f = fill.coerceIn(0f, 1f)
    Canvas(Modifier.size(size).semantics { contentDescription = "$label: ${(f * 100).toInt()}% cheio" }) {
        val w = this.size.width; val h = this.size.height
        val body = Path().apply {
            moveTo(w * 0.2f, h * 0.18f); lineTo(w * 0.8f, h * 0.18f); lineTo(w * 0.8f, h * 0.26f)
            quadraticBezierTo(w * 0.95f, h * 0.35f, w * 0.92f, h * 0.6f); quadraticBezierTo(w * 0.9f, h * 0.95f, w * 0.5f, h * 0.95f)
            quadraticBezierTo(w * 0.1f, h * 0.95f, w * 0.08f, h * 0.6f); quadraticBezierTo(w * 0.05f, h * 0.35f, w * 0.2f, h * 0.26f); close()
        }
        drawPath(body, soft.azul)
        clipPath(body) {
            val top = h * 0.95f - (h * 0.77f) * f
            drawRect(Brand.Amarelo, Offset(0f, top), Size(w, h))
            for (i in 0..3) drawCircle(soft.contorno.copy(alpha = 0.25f), w * 0.05f, Offset(w * (0.3f + i * 0.13f), top + h * 0.08f))
        }
        drawPath(body, soft.contorno, style = Stroke(w * 0.05f))
        drawRoundRect(Brand.Coral, Offset(w * 0.15f, h * 0.06f), Size(w * 0.7f, h * 0.12f), CornerRadius(w * 0.04f))
        drawRoundRect(soft.contorno, Offset(w * 0.15f, h * 0.06f), Size(w * 0.7f, h * 0.12f), CornerRadius(w * 0.04f), style = Stroke(w * 0.04f))
    }
}

/** Diálogo de guardar/retirar/transferir dinheiro de um cofrinho. */
@Composable
fun GoalMoveDialog(c: AppContainer, goal: GoalEntity, goals: List<GoalEntity>, mode: String, onClose: () -> Unit) {
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    var value by remember { mutableStateOf(Money.toInput(goal.monthly).takeIf { mode == "GUARDAR" } ?: "") }
    var target by remember { mutableStateOf(goals.firstOrNull { it.id != goal.id }?.id?.toString() ?: "") }
    val v = Money.parse(value) ?: 0
    val invalid = v <= 0 || (mode != "GUARDAR" && v > goal.saved)
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(when (mode) { "GUARDAR" -> "Guardar em \"${goal.name}\""; "RETIRAR" -> "Retirar de \"${goal.name}\""; else -> "Transferir de \"${goal.name}\"" }) },
        text = {
            Column {
                MoneyInput("Valor", value, { value = it }, help = if (mode != "GUARDAR") "Disponível: ${Money.format(goal.saved)}" else null)
                if (mode == "TRANSFERIR") SelectField("Para qual cofrinho", target, goals.filter { it.id != goal.id }.map { it.id.toString() to it.name }) { target = it }
                Text("Isso só atualiza o acompanhamento — o app não movimenta dinheiro de verdade.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(enabled = !invalid && (mode != "TRANSFERIR" || target.isNotBlank()), onClick = {
                scope.launch {
                    when (mode) {
                        "GUARDAR" -> c.depositGoal(goal, v)
                        "RETIRAR" -> c.depositGoal(goal, -v, "Retirada")
                        else -> goals.firstOrNull { it.id.toString() == target }?.let { c.transferGoal(goal, it, v) }
                    }
                }
                when (mode) {
                    "GUARDAR" -> fb.celebrate(if (goal.saved + v >= goal.target) "Cofrinho cheio! Meta batida! 🎉" else "+${Money.format(v)} no cofrinho. Boa!")
                    "RETIRAR" -> fb.toast("Retirada anotada. Tudo bem usar quando precisa.", Mood.TRANQUILA)
                    else -> fb.toast("Transferência anotada.", Mood.FELIZ)
                }
                onClose()
            }) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Cancelar") } },
    )
}

@Composable
fun GoalHistory(c: AppContainer, goal: GoalEntity) {
    val all by c.db.goalDeposits().all().asState(emptyList())
    val list = all.filter { it.goalId == goal.id }
    if (list.isEmpty()) Text("Ainda sem movimentações registradas.", style = MaterialTheme.typography.bodySmall)
    list.take(10).forEach { m ->
        Row {
            Text("${Dates.br(m.date)}${m.note?.let { " · $it" } ?: ""}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Text((if (m.amount >= 0) "+ " else "− ") + Money.format(kotlin.math.abs(m.amount)), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (m.amount >= 0) Brand.VerdeEscuro else MaterialTheme.colorScheme.error)
        }
    }
}

// ------------------------------------------------------------------ Importar extrato
private data class ImportRow(val row: StatementImport.Row, val category: String, val selected: Boolean, val duplicate: Boolean)

@Composable
fun ImportScreen(c: AppContainer, nav: NavController) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    val existing by c.db.tx().all().asState(emptyList())
    val rows = remember { mutableStateListOf<ImportRow>() }
    var error by remember { mutableStateOf<String?>(null) }
    var skipped by remember { mutableStateOf(0) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val bytes = runCatching { ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull()
        if (bytes == null) { error = "Não consegui abrir o arquivo."; return@rememberLauncherForActivityResult }
        var text = String(bytes, Charsets.UTF_8)
        if (text.contains('�')) text = String(bytes, Charsets.ISO_8859_1) // muitos bancos exportam em Latin-1
        val r = StatementImport.parse(text)
        error = r.error; skipped = r.skipped
        rows.clear()
        val keys = existing.map { Triple(it.date, it.amount, it.description.lowercase()) }.toSet()
        r.rows.forEach { row ->
            val amt = kotlin.math.abs(row.amount)
            val dup = Triple(row.date.toEpochDay(), amt, row.description.lowercase()) in keys
            rows += ImportRow(row, StatementImport.guessCategory(row.description, row.isIncome), !dup, dup)
        }
    }
    Page(nav, "Importar extrato") {
        ScreenHeader("Importar extrato do banco", "Traga vários lançamentos de uma vez a partir de um arquivo CSV.", ill = Ill.CONTA, tone = Tone.VERDE)
        AlertBox("No app ou site do banco, procure \"exportar extrato\" em CSV (planilha). O arquivo é lido só aqui no aparelho — nada vai para a internet. Antes de salvar, você confere e escolhe o que entra.", "INFO")
        PrimaryButton("📂 Escolher arquivo CSV", Modifier.fillMaxWidth()) { picker.launch(arrayOf("text/*", "application/csv", "application/vnd.ms-excel", "*/*")) }
        error?.let { AlertBox("$it Confira se o arquivo é CSV com colunas de data, descrição e valor.", "PERIGO") }
        if (rows.isNotEmpty()) {
            val sel = rows.count { it.selected }
            SectionTitle("${rows.size} lançamento(s) encontrados")
            if (skipped > 0) Text("$skipped linha(s) ignoradas (cabeçalho, saldo ou sem valor).", style = MaterialTheme.typography.bodySmall)
            if (rows.any { it.duplicate }) Text("Os marcados como \"já existe\" vieram desmarcados para não duplicar.", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { for (i in rows.indices) rows[i] = rows[i].copy(selected = true) }) { Text("Marcar todos") }
                TextButton(onClick = { for (i in rows.indices) rows[i] = rows[i].copy(selected = false) }) { Text("Desmarcar todos") }
            }
            rows.forEachIndexed { i, ir ->
                ItemRow(
                    title = ir.row.description,
                    subtitle = "${Dates.br(ir.row.date.toEpochDay())} · ${ir.category}",
                    amount = (if (ir.row.isIncome) "+ " else "− ") + Money.format(kotlin.math.abs(ir.row.amount)),
                    checked = ir.selected, onCheck = { v -> rows[i] = ir.copy(selected = v) },
                    tags = if (ir.duplicate) { { Tag("já existe", Tone.AMARELO) } } else null,
                    extra = {
                        var open by remember { mutableStateOf(false) }
                        TextButton(onClick = { open = true }) { Text("🏷️") }
                        if (open) AlertDialog(
                            onDismissRequest = { open = false }, title = { Text("Categoria") },
                            text = { SelectField("Categoria", ir.category, (if (ir.row.isIncome) INCOME_CATEGORIES else EXPENSE_CATEGORIES).map { it to it }) { rows[i] = ir.copy(category = it); open = false } },
                            confirmButton = { TextButton(onClick = { open = false }) { Text("OK") } },
                        )
                    },
                )
            }
            PrimaryButton("Importar $sel lançamento(s)", Modifier.fillMaxWidth(), enabled = sel > 0) {
                val chosen = rows.filter { it.selected }
                scope.launch {
                    chosen.forEach { ir ->
                        c.db.tx().upsert(TxEntity(kind = if (ir.row.isIncome) "ENTRADA" else "SAIDA", description = ir.row.description, amount = kotlin.math.abs(ir.row.amount),
                            date = ir.row.date.toEpochDay(), category = ir.category, incomeType = if (ir.row.isIncome) "VARIAVEL" else null, payment = "Extrato"))
                    }
                }
                fb.celebrate("${chosen.size} lançamento(s) importados!")
                rows.clear()
                nav.popBackStack()
            }
        } else if (error == null) {
            EmptyState("Nenhum arquivo escolhido", "Formatos aceitos: separados por ponto e vírgula, vírgula ou tabulação; datas como 31/12/2026 ou 2026-12-31; valores como -1.234,56 ou 1234.56 D.", Mood.PENSANDO)
        }
    }
}
