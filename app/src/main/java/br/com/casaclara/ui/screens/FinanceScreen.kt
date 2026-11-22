package br.com.casaclara.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.casaclara.data.AppContainer
import br.com.casaclara.data.BillEntity
import br.com.casaclara.data.BudgetEntity
import br.com.casaclara.data.DebtEntity
import br.com.casaclara.data.GoalEntity
import br.com.casaclara.data.ReminderEntity
import br.com.casaclara.data.TxEntity
import br.com.casaclara.data.UserPrefs
import br.com.casaclara.domain.Calculators
import br.com.casaclara.domain.Dates
import br.com.casaclara.domain.Finance
import br.com.casaclara.domain.Money
import br.com.casaclara.domain.Routes
import br.com.casaclara.notifications.ReminderScheduler
import br.com.casaclara.ui.EXPENSE_CATEGORIES
import br.com.casaclara.ui.INCOME_CATEGORIES
import br.com.casaclara.ui.asState
import br.com.casaclara.ui.billStatus
import br.com.casaclara.ui.components.Ill
import br.com.casaclara.ui.components.AlertBox
import br.com.casaclara.ui.components.Bars
import br.com.casaclara.ui.components.BrandCard
import br.com.casaclara.ui.components.CrudList
import br.com.casaclara.ui.components.EmptySpec
import br.com.casaclara.ui.components.FieldSpec
import br.com.casaclara.ui.components.FieldType
import br.com.casaclara.ui.components.Init
import br.com.casaclara.ui.components.LocalFeedback
import br.com.casaclara.ui.components.MascotBubble
import br.com.casaclara.ui.components.MoneyInput
import br.com.casaclara.ui.components.Mood
import br.com.casaclara.ui.components.NumberInput
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
import br.com.casaclara.ui.theme.Brand
import br.com.casaclara.ui.theme.LocalSoft
import kotlinx.coroutines.launch

private val TABS = listOf("Resumo", "Lançamentos", "Contas", "Cartões", "Recorrentes", "Dívidas", "Orçamento", "Cofrinhos", "Relatórios")
private val INCOME_TYPES = listOf("FIXA" to "Fixa (salário, bolsa…)", "VARIAVEL" to "Variável (comissão, freela…)", "INFORMAL" to "Informal (bico, vendas…)")

@Composable
fun FinanceScreen(c: AppContainer, nav: NavController, initialTab: Int) {
    var tab by rememberSaveable { mutableIntStateOf(initialTab) }
    Column {
        androidx.compose.foundation.layout.Box(Modifier.padding(horizontal = 16.dp)) {
            ScreenHeader("Finanças", "Quanto entrou, quanto saiu e quanto sobrou.", ill = Ill.COFRINHO, tone = Tone.VERDE)
        }
        Tabs(TABS, tab) { tab = it }
        when (tab) {
            0 -> ResumoTab(c) { tab = it }
            1 -> LancamentosTab(c, nav)
            2 -> ContasTab(c)
            3 -> CartoesTab(c)
            4 -> RecorrentesTab(c)
            5 -> DividasTab(c)
            6 -> OrcamentoTab(c)
            7 -> MetasTab(c, nav)
            else -> RelatoriosTab(c)
        }
    }
}

private fun List<TxEntity>.movs(kind: String) = filter { it.kind == kind }.map { Finance.Movement(it.amount, it.date, it.category) }

@Composable
private fun ResumoTab(c: AppContainer, goTab: (Int) -> Unit) {
    val tx by c.db.tx().all().asState(emptyList())
    var month by rememberSaveable { mutableIntStateOf(Dates.currentMonthKey()) }
    val fb = LocalFeedback.current
    val scope = rememberCoroutineScope()
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) scope.launch { runCatching { c.exportFinanceCsv(uri) }.onSuccess { fb.toast("Planilha exportada! 📄") }.onFailure { fb.toast("Não consegui exportar.", Mood.CONFUSA) } }
    }
    val e = tx.movs("ENTRADA"); val s = tx.movs("SAIDA")
    val sum = Finance.summary(e, s, month)
    val cmp = Finance.compareMonths(s, month)
    val ym = Dates.yearMonth(month)
    val cats = Finance.byCategory(s, ym.atDay(1).toEpochDay(), ym.atEndOfMonth().toEpochDay())
    TabBody {
        MonthSwitcher(month) { month = it }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BrandCard(Modifier.weight(1f), Tone.VERDE, padding = 12.dp) { Stat("Entrou", Money.format(sum.entrou), color = Brand.VerdeEscuro) }
            BrandCard(Modifier.weight(1f), Tone.CORAL, padding = 12.dp) { Stat("Saiu", Money.format(sum.saiu)) }
        }
        BrandCard(tone = Tone.AMARELO, padding = 12.dp) { Stat("Sobrou", Money.format(sum.sobrou), color = if (sum.sobrou < 0) MaterialTheme.colorScheme.error else Brand.VerdeEscuro) }
        when {
            sum.entrou + sum.saiu == 0L -> MascotBubble("Nada registrado em ${Dates.monthLabel(month).lowercase()}. Comece anotando sua renda e seus gastos.", Mood.PENSANDO) {
                Button(onClick = { goTab(1) }) { Text("Registrar agora") }
            }
            sum.sobrou >= 0 -> MascotBubble("Sobrou ${Money.format(sum.sobrou)}! Que tal mandar uma parte para uma meta?", Mood.COMEMORANDO)
            else -> MascotBubble("Saiu um pouco mais do que entrou. Olhe as maiores categorias abaixo — às vezes um ajuste pequeno resolve.", Mood.DICA)
        }
        if (cmp.gastoAnterior > 0) {
            val v = cmp.variacao ?: 0.0
            AlertBox(
                if (cmp.diferenca > 0) "Você gastou ${Money.format(cmp.diferenca)} a mais que no mês anterior (${Money.percent(v)})."
                else "Você gastou ${Money.format(-cmp.diferenca)} a menos que no mês anterior. 👏",
                if (cmp.diferenca > 0) "DICA" else "OK",
            )
        }
        SectionTitle("Para onde foi o dinheiro")
        if (cats.isEmpty()) Text("Sem gastos neste mês.", color = MaterialTheme.colorScheme.onSurfaceVariant) else Bars(cats, Money::format)
        SecondaryButton("⬇ Exportar planilha (CSV)", Modifier.fillMaxWidth(), enabled = tx.isNotEmpty()) { export.launch("casa-clara-financas-${Dates.today()}.csv") }
        Text("O arquivo é salvo onde você escolher. Nada é enviado para a internet.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun MonthSwitcher(month: Int, onChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        IconButton(onClick = { onChange(Dates.shiftMonth(month, -1)) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mês anterior") }
        Text(Dates.monthLabel(month), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
        IconButton(onClick = { onChange(Dates.shiftMonth(month, 1)) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Próximo mês") }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LancamentosTab(c: AppContainer, nav: NavController) {
    val tx by c.db.tx().all().asState(emptyList())
    val cards by c.db.cards().all().asState(emptyList())
    val cardOpts = listOf("" to "Não foi no cartão") + cards.map { it.id.toString() to "💳 ${it.name}" }
    var kind by rememberSaveable { mutableStateOf("TODOS") }
    var period by rememberSaveable { mutableStateOf("MES") }
    var cat by rememberSaveable { mutableStateOf("") }
    val today = Dates.today()
    val from = when (period) { "MES" -> today.withDayOfMonth(1); "ANTERIOR" -> today.minusMonths(1).withDayOfMonth(1); "TRES" -> today.minusMonths(2).withDayOfMonth(1); else -> null }?.toEpochDay()
    val to = if (period == "ANTERIOR") today.withDayOfMonth(1).minusDays(1).toEpochDay() else null
    val list = tx.filter {
        (kind == "TODOS" || it.kind == kind) && (from == null || it.date >= from) && (to == null || it.date <= to) && (cat.isEmpty() || it.category == cat)
    }
    TabBody {
        SecondaryButton("📥 Importar extrato do banco (CSV)", Modifier.fillMaxWidth()) { nav.navigate(Routes.IMPORT) }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("TODOS" to "Tudo", "ENTRADA" to "Entradas", "SAIDA" to "Gastos").forEach { (k, l) -> FilterChip(kind == k, { kind = k }, { Text(l) }) }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("MES" to "Este mês", "ANTERIOR" to "Mês passado", "TRES" to "3 meses", "TUDO" to "Tudo").forEach { (k, l) -> FilterChip(period == k, { period = k }, { Text(l) }) }
        }
        br.com.casaclara.ui.components.SelectField("Categoria", cat, listOf("" to "Todas") + (EXPENSE_CATEGORIES + INCOME_CATEGORIES).distinct().map { it to it }) { cat = it }
        val totalIn = list.filter { it.kind == "ENTRADA" }.sumOf { it.amount }
        val totalOut = list.filter { it.kind == "SAIDA" }.sumOf { it.amount }
        Text("No filtro: entrou ${Money.format(totalIn)} · saiu ${Money.format(totalOut)}", fontWeight = FontWeight.Bold)
        CrudList(
            items = list,
            itemName = "lançamento", masculine = true,
            fields = listOf(
                FieldSpec("kind", "Tipo", FieldType.SELECT, true, listOf("SAIDA" to "Gasto", "ENTRADA" to "Entrada")),
                FieldSpec("description", "Descrição", required = true, placeholder = "Ex.: Mercado, Salário"),
                FieldSpec("amount", "Valor", FieldType.MONEY, true, validate = { d -> if ((d.money("amount") ?: 0) <= 0) "O valor precisa ser maior que zero." else null }),
                FieldSpec("date", "Data", FieldType.DATE, true),
                FieldSpec("category", "Categoria", FieldType.SELECT, true, opts((EXPENSE_CATEGORIES + INCOME_CATEGORIES).distinct())),
                FieldSpec("incomeType", "Tipo de renda (para entradas)", FieldType.SELECT, options = listOf("" to "—") + INCOME_TYPES),
                FieldSpec("payment", "Forma de pagamento", FieldType.SELECT, options = opts("", "Pix", "Débito", "Crédito", "Dinheiro", "Boleto")),
                FieldSpec("card", "Cartão de crédito (para gastos)", FieldType.SELECT, options = cardOpts, help = if (cards.isEmpty()) "Cadastre cartões na aba Cartões." else "A compra entra na fatura certa automaticamente."),
                FieldSpec("installments", "Parcelas no cartão", FieldType.INT, min = 1.0, max = 48.0, help = "O valor acima é o total da compra."),
            ),
            toForm = { t ->
                if (t == null) mapOf("kind" to (if (kind == "ENTRADA") "ENTRADA" else "SAIDA"), "date" to Init.today(), "category" to "Outros")
                else mapOf("kind" to t.kind, "description" to t.description, "amount" to Init.money(t.amount), "date" to Init.date(t.date), "category" to t.category, "incomeType" to (t.incomeType ?: ""), "payment" to (t.payment ?: ""), "card" to (t.cardId?.toString() ?: ""), "installments" to t.installments.toString())
            },
            save = { d, old ->
                val k = d.str("kind")
                c.db.tx().upsert(TxEntity(id = old?.id ?: 0, kind = k, description = d.str("description"), amount = d.money("amount")!!, date = d.date("date")!!,
                    category = d.str("category"), incomeType = if (k == "ENTRADA") d.text("incomeType") ?: "FIXA" else null,
                    payment = if (k == "SAIDA" && d.text("card") != null) "Crédito" else d.text("payment"), isDemo = old?.isDemo ?: false,
                    cardId = if (k == "SAIDA") d.text("card")?.toLongOrNull() else null, installments = (d.int("installments") ?: 1).coerceIn(1, 48), recurringId = old?.recurringId))
            },
            delete = { c.db.tx().delete(it) },
            row = { t ->
                RowSpec(
                    title = t.description,
                    subtitle = "${Dates.br(t.date)} · ${t.category}${t.payment?.let { " · $it" } ?: ""}" +
                        (t.cardId?.let { id -> " · 💳 " + (cards.firstOrNull { it.id == id }?.name ?: "cartão") + if (t.installments > 1) " em ${t.installments}x" else "" } ?: "") +
                        (if (t.recurringId != null) " · 🔁 automático" else ""),
                    amount = (if (t.kind == "ENTRADA") "+ " else "− ") + Money.format(t.amount),
                    tags = if (t.incomeType != null) { { Tag(INCOME_TYPES.firstOrNull { it.first == t.incomeType }?.second?.substringBefore(" (") ?: "Entrada", Tone.VERDE) } } else null,
                )
            },
            empty = EmptySpec("Nada por aqui ainda", "Registre sua renda — fixa, variável ou informal — e seus gastos. Os pequenos também contam!"),
            isDemo = { it.isDemo },
            addedMessage = "Anotado! Agora você sabe para onde o dinheiro vai.",
        )
    }
}

@Composable
private fun ContasTab(c: AppContainer) {
    val bills by c.db.bills().all().asState(emptyList())
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    val month = Dates.currentMonthKey()
    val pagas = bills.count { it.paidMonth == month }
    TabBody {
        if (bills.isNotEmpty()) BrandCard(tone = Tone.VERDE) {
            Text("$pagas de ${bills.size} contas pagas em ${Dates.monthLabel(month).lowercase()}", fontWeight = FontWeight.Bold)
            ProgressLine(pagas.toFloat() / bills.size, "Contas pagas")
            Text("Total previsto: ${Money.format(bills.sumOf { it.amount ?: 0 })}")
        }
        CrudList(
            items = bills.sortedBy { billStatus(it).days },
            itemName = "conta",
            title = "Contas recorrentes", description = "Aluguel, luz, internet, assinaturas… com o dia do vencimento.",
            fields = listOf(
                FieldSpec("name", "Nome da conta", required = true, placeholder = "Ex.: Energia"),
                FieldSpec("amount", "Valor aproximado", FieldType.MONEY, help = "Se varia, use a média."),
                FieldSpec("dueDay", "Dia do vencimento", FieldType.DAY, true),
                FieldSpec("category", "Categoria", FieldType.SELECT, options = opts(EXPENSE_CATEGORIES)),
                FieldSpec("autoDebit", "Está no débito automático", FieldType.CHECK),
            ),
            toForm = { b -> if (b == null) mapOf("category" to "Contas da casa") else mapOf("name" to b.name, "amount" to Init.money(b.amount), "dueDay" to b.dueDay.toString(), "category" to b.category, "autoDebit" to Init.bool(b.autoDebit)) },
            save = { d, old -> c.db.bills().upsert(BillEntity(id = old?.id ?: 0, name = d.str("name"), amount = d.money("amount"), dueDay = d.int("dueDay")!!, category = d.text("category") ?: "Contas da casa", autoDebit = d.bool("autoDebit"), paidMonth = old?.paidMonth, isDemo = old?.isDemo ?: false)) },
            delete = { c.db.bills().delete(it) },
            row = { b ->
                val s = billStatus(b)
                RowSpec(
                    title = b.name,
                    subtitle = when { s.paid -> "Paga este mês ✓ · próximo: ${Dates.br(s.due.toEpochDay())}"; s.late -> "Venceu ${Dates.relative(s.days)} (dia ${b.dueDay})"; else -> "Vence ${Dates.relative(s.days)} · dia ${b.dueDay}" },
                    amount = b.amount?.let { Money.format(it) },
                    tags = {
                        when { s.paid -> Tag("paga", Tone.VERDE); s.late -> Tag("atrasada", Tone.CORAL); s.days <= 5 -> Tag("vence logo", Tone.AMARELO) }
                        if (b.autoDebit) Tag("débito automático", Tone.AZUL)
                    },
                    extra = {
                        if (s.paid) TextButton(onClick = { scope.launch { c.db.bills().upsert(b.copy(paidMonth = null)) }; fb.toast("Marcada como não paga. O gasto lançado continua em Lançamentos.", Mood.TRANQUILA) }) { Text("Desfazer") }
                        else TextButton(onClick = {
                            scope.launch {
                                c.db.bills().upsert(b.copy(paidMonth = month))
                                if (b.amount != null) c.db.tx().upsert(TxEntity(kind = "SAIDA", description = b.name, amount = b.amount, date = Dates.todayEpoch(), category = b.category))
                            }
                            fb.celebrate("Boa! Mais uma conta organizada.")
                        }) { Text("Paguei") }
                        TextButton(onClick = {
                            scope.launch {
                                val due = billStatus(b).due
                                val r = ReminderEntity(title = "Vence: ${b.name}", kind = "CONTA", date = due.minusDays(2).toEpochDay().coerceAtLeast(Dates.todayEpoch()), repeat = "MENSAL")
                                val id = c.db.reminders().upsert(r)
                                ReminderScheduler.schedule(c.context, r.copy(id = id))
                            }
                            fb.toast("Lembrete criado 2 dias antes, todo mês. Ajuste em Lembretes.", Mood.DICA)
                        }) { Text("🔔") }
                    },
                )
            },
            empty = EmptySpec("Nenhuma conta cadastrada", "Cadastre as contas que se repetem todo mês para ver vencimentos no painel e no calendário."),
            isDemo = { it.isDemo },
        )
        AlertBox("Ao tocar em Paguei, o valor também entra em Lançamentos, para o resumo do mês ficar certo.", "DICA")
    }
}

@Composable
private fun DividasTab(c: AppContainer) {
    val debts by c.db.debts().all().asState(emptyList())
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    val restante = debts.sumOf { Finance.debtRemaining(it.installment, it.total, it.paid).valor }
    val mensal = debts.filter { it.paid < it.total }.sumOf { it.installment }
    TabBody {
        if (debts.isNotEmpty()) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BrandCard(Modifier.weight(1f), padding = 12.dp) { Stat("Falta pagar", Money.format(restante)) }
            BrandCard(Modifier.weight(1f), padding = 12.dp) { Stat("Por mês", Money.format(mensal)) }
        }
        CrudList(
            items = debts,
            itemName = "dívida",
            title = "Dívidas, parcelas e cartões", description = "Organizar é o primeiro passo — sem culpa.",
            fields = listOf(
                FieldSpec("name", "O que é", required = true, placeholder = "Ex.: Celular parcelado"),
                FieldSpec("kind", "Tipo", FieldType.SELECT, options = listOf("PARCELADO" to "Compra parcelada", "CARTAO" to "Cartão de crédito", "EMPRESTIMO" to "Empréstimo", "OUTRO" to "Outro")),
                FieldSpec("creditor", "Com quem (banco, loja…)"),
                FieldSpec("installment", "Valor da parcela", FieldType.MONEY, true),
                FieldSpec("total", "Total de parcelas", FieldType.INT, true, min = 1.0, max = 600.0),
                FieldSpec("paid", "Parcelas já pagas", FieldType.INT, min = 0.0, max = 600.0, validate = { d -> if ((d.int("paid") ?: 0) > (d.int("total") ?: 0)) "Não pode passar do total." else null }),
                FieldSpec("dueDay", "Dia do vencimento", FieldType.DAY),
                FieldSpec("monthlyRate", "Juros (% ao mês, se souber)", FieldType.DECIMAL, min = 0.0, max = 50.0),
                FieldSpec("note", "Anotações", FieldType.LONGTEXT),
            ),
            toForm = { d -> if (d == null) mapOf("kind" to "PARCELADO", "paid" to "0") else mapOf("name" to d.name, "kind" to d.kind, "creditor" to (d.creditor ?: ""), "installment" to Init.money(d.installment), "total" to d.total.toString(), "paid" to d.paid.toString(), "dueDay" to (d.dueDay?.toString() ?: ""), "monthlyRate" to Init.num(d.monthlyRate), "note" to (d.note ?: "")) },
            save = { f, old -> c.db.debts().upsert(DebtEntity(id = old?.id ?: 0, name = f.str("name"), kind = f.text("kind") ?: "PARCELADO", creditor = f.text("creditor"), installment = f.money("installment")!!, total = f.int("total")!!, paid = f.int("paid") ?: 0, dueDay = f.int("dueDay"), monthlyRate = f.double("monthlyRate"), note = f.text("note"), isDemo = old?.isDemo ?: false)) },
            delete = { c.db.debts().delete(it) },
            row = { d ->
                val r = Finance.debtRemaining(d.installment, d.total, d.paid)
                RowSpec(
                    title = d.name,
                    subtitle = "${d.creditor?.let { "$it · " } ?: ""}${d.paid}/${d.total} pagas · ${Money.format(d.installment)}/mês${d.dueDay?.let { " · dia $it" } ?: ""}${d.monthlyRate?.let { " · ${it.toString().replace('.', ',')}% a.m. (≈ ${Money.percent(Finance.annualFromMonthly(it / 100))} ao ano)" } ?: ""}",
                    amount = if (r.restantes > 0) Money.format(r.valor) else "✓ quitada",
                    tags = { if (d.kind == "CARTAO") Tag("cartão", Tone.AZUL); if ((d.monthlyRate ?: 0.0) >= 5.0) Tag("juros altos", Tone.CORAL) },
                    extra = { if (d.paid < d.total) TextButton(onClick = { scope.launch { c.db.debts().upsert(d.copy(paid = d.paid + 1)) }; fb.celebrate(if (d.paid + 1 >= d.total) "Dívida quitada! Que alívio! 🎉" else "Parcela paga. Uma a menos!") }) { Text("+1 paga") } },
                )
            },
            empty = EmptySpec("Nenhuma dívida cadastrada", "Se tiver alguma, cadastre aqui. Ver o quadro completo ajuda a priorizar e negociar.", Mood.TRANQUILA),
            isDemo = { it.isDemo },
        )
        InstallmentCalculator()
        AlertBox("Priorize o essencial e as dívidas com juros mais altos, como rotativo do cartão e cheque especial. Veja o guia \"Como entender juros e o custo de uma dívida\".", "INFO")
    }
}

/** Calculadora de juros de parcelamento (Tabela Price). */
@Composable
fun InstallmentCalculator() {
    var price by rememberSaveable { mutableStateOf("") }
    var n by rememberSaveable { mutableStateOf("12") }
    var rate by rememberSaveable { mutableStateOf("") }
    var parcela by rememberSaveable { mutableStateOf("") }
    BrandCard(tone = Tone.AZUL) {
        Text("🧮 Quanto custa parcelar?", style = MaterialTheme.typography.titleMedium)
        Text("Informe o preço à vista e, se souber, a taxa de juros ou o valor da parcela oferecida.", style = MaterialTheme.typography.bodySmall)
        MoneyInput("Preço à vista", price, { price = it })
        NumberInput("Número de parcelas", n, { n = it })
        NumberInput("Juros ao mês (%)", rate, { rate = it }, help = "Deixe vazio se não souber")
        MoneyInput("Ou: valor da parcela oferecida", parcela, { parcela = it })
        val pv = Money.parse(price) ?: 0
        val parcelas = n.toIntOrNull() ?: 0
        val r = rate.replace(',', '.').toDoubleOrNull()
        val p = Money.parse(parcela)
        if (pv > 0 && parcelas > 0) {
            if (p != null && p > 0) {
                val total = p * parcelas
                val taxa = Finance.impliedMonthlyRate(pv, p, parcelas) ?: 0.0
                Text("Total parcelado: ${Money.format(total)} · a mais: ${Money.format(total - pv)}", fontWeight = FontWeight.Bold)
                Text("Isso equivale a juros de cerca de ${Money.percent(taxa, 2)} ao mês (estimativa).")
            } else {
                val res = Finance.installmentCost(pv, parcelas, (r ?: 0.0) / 100)
                Text("Parcela estimada: ${Money.format(res.parcela)}", fontWeight = FontWeight.Bold)
                Text("Total: ${Money.format(res.totalPago)} · juros: ${Money.format(res.jurosPagos)}")
            }
            Text("Estimativa para comparar. As condições reais são as do contrato (confira o CET).", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun OrcamentoTab(c: AppContainer) {
    val budgets by c.db.budgets().all().asState(emptyList())
    val tx by c.db.tx().all().asState(emptyList())
    val month = Dates.currentMonthKey()
    val ym = Dates.yearMonth(month)
    val gastos = Finance.byCategory(tx.movs("SAIDA"), ym.atDay(1).toEpochDay(), ym.atEndOfMonth().toEpochDay()).toMap()
    val totalLim = budgets.sumOf { it.limitAmount }
    val totalGasto = budgets.sumOf { gastos[it.category] ?: 0 }
    TabBody {
        if (budgets.isNotEmpty()) BrandCard(tone = Tone.AMARELO) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Stat("Planejado", Money.format(totalLim), Modifier.weight(1f))
                Stat("Gasto", Money.format(totalGasto), Modifier.weight(1f))
            }
            Stat("Ainda pode gastar", Money.format(totalLim - totalGasto), color = if (totalLim < totalGasto) MaterialTheme.colorScheme.error else Brand.VerdeEscuro)
        }
        CrudList(
            items = budgets,
            itemName = "limite", masculine = true,
            title = "Orçamento de ${Dates.monthLabel(month).lowercase()}", description = "Um limite por categoria. O gasto vem dos seus lançamentos.",
            fields = listOf(
                FieldSpec("category", "Categoria", FieldType.SELECT, true, opts(EXPENSE_CATEGORIES)),
                FieldSpec("limit", "Limite por mês", FieldType.MONEY, true, validate = { d -> if ((d.money("limit") ?: 0) <= 0) "Informe um limite maior que zero." else null }),
            ),
            toForm = { b -> if (b == null) mapOf("category" to "Mercado") else mapOf("category" to b.category, "limit" to Init.money(b.limitAmount)) },
            save = { d, old -> c.db.budgets().upsert(BudgetEntity(id = old?.id ?: 0, category = d.str("category"), limitAmount = d.money("limit")!!, isDemo = old?.isDemo ?: false)) },
            delete = { c.db.budgets().delete(it) },
            row = { b ->
                val g = gastos[b.category] ?: 0
                val u = Finance.budget(b.limitAmount, g)
                RowSpec(
                    title = b.category,
                    subtitle = "${Money.format(g)} de ${Money.format(b.limitAmount)} · " + if (u.restante >= 0) "restam ${Money.format(u.restante)}" else "passou ${Money.format(-u.restante)}",
                    tags = {
                        ProgressLine(u.uso.toFloat().coerceAtMost(1f), "Uso de ${b.category}", when (u.status) { Finance.BudgetStatus.OK -> Brand.Verde; Finance.BudgetStatus.ATENCAO -> Brand.Amarelo; Finance.BudgetStatus.ESTOUROU -> Brand.Coral }, Modifier.fillMaxWidth())
                        when (u.status) { Finance.BudgetStatus.ESTOUROU -> Tag("passou do limite", Tone.CORAL); Finance.BudgetStatus.ATENCAO -> Tag("quase no limite", Tone.AMARELO); else -> {} }
                    },
                )
            },
            empty = EmptySpec("Nenhum limite ainda", "Escolha 3 categorias para começar: Mercado, Transporte e Lazer costumam ser boas."),
            isDemo = { it.isDemo },
        )
    }
}

@Composable
private fun MetasTab(c: AppContainer, nav: NavController) {
    val goals by c.db.goals().all().asState(emptyList())
    val tx by c.db.tx().all().asState(emptyList())
    val prefs by c.prefs.flow.asState(UserPrefs())
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    var move by remember { mutableStateOf<Pair<GoalEntity, String>?>(null) }
    var historyOf by remember { mutableStateOf<Long?>(null) }
    // custo de vida de referência: calculadora ou média dos gastos dos últimos 3 meses
    val calc = prefs.calc["custo"].orEmpty()
    val calcTotal = calc.values.sumOf { Money.parse(it)?.coerceAtLeast(0) ?: 0 }
    val ref = if (calcTotal > 0) calcTotal to "pelo seu custo mensal calculado" else {
        val saidas = tx.filter { it.kind == "SAIDA" }
        val months = saidas.map { Dates.monthKey(it.date) }.distinct().sorted().takeLast(3)
        if (months.isEmpty()) 0L to "" else saidas.filter { Dates.monthKey(it.date) in months }.sumOf { it.amount } / months.size to "pela média dos seus gastos"
    }
    TabBody {
        if (goals.isNotEmpty()) Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            goals.forEach { g ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(88.dp)) {
                    Jar(if (g.target > 0) g.saved.toFloat() / g.target else 0f, 72.dp, g.name)
                    Text(g.name, style = MaterialTheme.typography.labelMedium, maxLines = 2, textAlign = TextAlign.Center)
                }
            }
        }
        Text("Toque num cofrinho da lista para ver o histórico.", style = MaterialTheme.typography.bodySmall)
        CrudList(
            items = goals,
            itemName = "meta",
            title = "Cofrinhos (metas e reserva)", description = "Um cofrinho para cada objetivo. A reserva de emergência (dinheiro só para imprevistos) é o mais importante.",
            fields = listOf(
                FieldSpec("name", "Nome da meta", required = true, placeholder = "Ex.: Reserva de emergência"),
                FieldSpec("type", "Tipo", FieldType.SELECT, options = listOf("RESERVA" to "Reserva de emergência", "MUDANCA" to "Mudança", "COMPRA" to "Compra", "OUTRO" to "Outro")),
                FieldSpec("target", "Quanto quero juntar", FieldType.MONEY, true, validate = { d -> if ((d.money("target") ?: 0) <= 0) "A meta precisa ser maior que zero." else null }),
                FieldSpec("saved", "Já guardei", FieldType.MONEY),
                FieldSpec("monthly", "Quanto guardo por mês", FieldType.MONEY),
                FieldSpec("rate", "Rendimento (% ao mês, opcional)", FieldType.DECIMAL, min = 0.0, max = 5.0, help = "Deixe vazio se não souber. Não é garantia."),
                FieldSpec("deadline", "Quero chegar até", FieldType.DATE),
            ),
            toForm = { g -> if (g == null) mapOf("type" to "RESERVA") else mapOf("name" to g.name, "type" to g.type, "target" to Init.money(g.target), "saved" to Init.money(g.saved), "monthly" to Init.money(g.monthly), "rate" to Init.num(g.ratePct), "deadline" to Init.date(g.deadline)) },
            save = { d, old -> c.db.goals().upsert(GoalEntity(id = old?.id ?: 0, name = d.str("name"), type = d.text("type") ?: "OUTRO", target = d.money("target")!!, saved = d.money("saved") ?: 0, monthly = d.money("monthly"), ratePct = d.double("rate"), deadline = d.date("deadline"), isDemo = old?.isDemo ?: false)) },
            delete = { c.db.goals().delete(it) },
            row = { g ->
                val prazo = g.deadline?.let { val m = java.time.temporal.ChronoUnit.MONTHS.between(Dates.today(), Dates.of(it)).toInt(); if (m > 0) m else null }
                val r = Calculators.savings(g.target, g.saved, g.monthly ?: 0, (g.ratePct ?: 0.0) / 100, prazo)
                val cob = if (g.type == "RESERVA" && ref.first > 0) Finance.emergencyCoverage(g.saved, ref.first) else null
                RowSpec(
                    title = g.name,
                    subtitle = buildString {
                        append("${Money.format(g.saved)} de ${Money.format(g.target)}")
                        when {
                            r.meses == 0 -> append(" · meta batida! 🎉")
                            r.meses != null && r.dataPrevista != null -> append(" · previsão: ${Dates.monthYearLabel(r.dataPrevista!!)} (${r.meses} ${if (r.meses == 1) "mês" else "meses"})")
                            else -> append(" · informe quanto guarda por mês para ver a previsão")
                        }
                        if (r.aporteParaPrazo != null) append("\nPara chegar no prazo: guardar ${Money.format(r.aporteParaPrazo)} por mês.")
                        if (cob != null) append("\nCobre ${String.format(java.util.Locale.ROOT, "%.1f", cob).replace('.', ',')} mês(es) de custo de vida (${ref.second}).")
                    },
                    tags = {
                        ProgressLine(r.progresso.toFloat(), "Progresso de ${g.name}", if (g.type == "RESERVA") Brand.Azul else Brand.Verde, Modifier.fillMaxWidth())
                        Tag(when (g.type) { "RESERVA" -> "Reserva"; "MUDANCA" -> "Mudança"; "COMPRA" -> "Compra"; else -> "Outro" }, if (g.type == "RESERVA") Tone.AZUL else Tone.VERDE)
                        val pad = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                        Row(Modifier.fillMaxWidth()) {
                            TextButton(onClick = { move = g to "GUARDAR" }, contentPadding = pad) { Text("+ Guardar", maxLines = 1, softWrap = false) }
                            TextButton(onClick = { move = g to "RETIRAR" }, enabled = g.saved > 0, contentPadding = pad) { Text("− Retirar", maxLines = 1, softWrap = false) }
                            if (goals.size > 1) TextButton(onClick = { move = g to "TRANSFERIR" }, enabled = g.saved > 0, contentPadding = pad) { Text("↔ Mover", maxLines = 1, softWrap = false) }
                        }
                        if (historyOf == g.id) Column(Modifier.fillMaxWidth()) { GoalHistory(c, g) }
                    },
                    onClick = { historyOf = if (historyOf == g.id) null else g.id },
                )
            },
            empty = EmptySpec("Nenhuma meta ainda", "Que tal começar pela reserva de emergência? Qualquer valor conta."),
            isDemo = { it.isDemo },
        )
        if (ref.first > 0 && goals.none { it.type == "RESERVA" }) {
            MascotBubble("Sugestão: uma reserva de 3 meses seria cerca de ${Money.format(ref.first * 3)} (${ref.second}).", Mood.DICA) {
                Button(onClick = { scope.launch { c.db.goals().upsert(GoalEntity(name = "Reserva de emergência", type = "RESERVA", target = ref.first * 3)) }; fb.celebrate("Reserva criada! Comece com o que der.") }) { Text("Criar essa meta") }
            }
        }
        SecondaryButton("🧮 Simular uma meta (calculadora)", Modifier.fillMaxWidth()) { nav.navigate(Routes.calc("economia")) }
        SecondaryButton("🔔 Lembrete mensal para guardar dinheiro", Modifier.fillMaxWidth()) {
            scope.launch {
                val r = ReminderEntity(title = "Guardar dinheiro para as metas", kind = "META", date = Dates.todayEpoch() + 1, repeat = "MENSAL")
                val id = c.db.reminders().upsert(r); ReminderScheduler.schedule(c.context, r.copy(id = id))
            }
            fb.toast("Lembrete mensal criado. Ajuste o dia em Lembretes.", Mood.DICA)
        }
    }
    move?.let { (g, mode) -> GoalMoveDialog(c, g, goals, mode) { move = null } }
}

@Composable
private fun RelatoriosTab(c: AppContainer) {
    val tx by c.db.tx().all().asState(emptyList())
    var end by rememberSaveable { mutableIntStateOf(Dates.currentMonthKey()) }
    var months by rememberSaveable { mutableIntStateOf(6) }
    val e = tx.movs("ENTRADA"); val s = tx.movs("SAIDA")
    val series = Finance.lastMonths(e, s, end, months)
    val max = (series.maxOfOrNull { maxOf(it.second.entrou, it.second.saiu) } ?: 1L).coerceAtLeast(1L)
    val from = Dates.yearMonth(series.first().first).atDay(1).toEpochDay()
    val to = Dates.yearMonth(end).atEndOfMonth().toEpochDay()
    TabBody {
        MonthSwitcher(end) { end = it }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(3, 6, 12).forEach { m -> FilterChip(months == m, { months = m }, { Text("$m meses") }) }
        }
        SectionTitle("Entradas × saídas")
        val soft = LocalSoft.current
        Row(
            Modifier.fillMaxWidth().height(180.dp).semantics { contentDescription = series.joinToString("; ") { "${Dates.monthLabel(it.first)}: entrou ${Money.format(it.second.entrou)}, saiu ${Money.format(it.second.saiu)}" } },
            horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom,
        ) {
            series.forEach { (_, v) ->
                Row(Modifier.weight(1f).fillMaxHeight(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box(Modifier.weight(1f).fillMaxHeight((v.entrou.toFloat() / max).coerceAtLeast(0.01f)).background(Brand.Verde, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)).border(1.5.dp, soft.contorno, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)))
                    Box(Modifier.weight(1f).fillMaxHeight((v.saiu.toFloat() / max).coerceAtLeast(0.01f)).background(Brand.Coral, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)).border(1.5.dp, soft.contorno, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)))
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { series.forEach { (k, _) -> Text(Dates.monthShort(k), Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall) } }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Tag("■ Entrou", Tone.VERDE); Tag("■ Saiu", Tone.CORAL) }
        SectionTitle("Mês a mês")
        series.reversed().forEach { (k, v) ->
            Row(Modifier.fillMaxWidth()) {
                Text(Dates.monthLabel(k), Modifier.weight(1f))
                Text(Money.format(v.sobrou), fontWeight = FontWeight.Bold, color = if (v.sobrou < 0) MaterialTheme.colorScheme.error else Brand.VerdeEscuro)
            }
        }
        SectionTitle("Gastos por categoria no período")
        val cats = Finance.byCategory(s, from, to)
        if (cats.isEmpty()) Text("Sem gastos no período.") else Bars(cats, Money::format)
        Spacer(Modifier.height(8.dp))
    }
}
