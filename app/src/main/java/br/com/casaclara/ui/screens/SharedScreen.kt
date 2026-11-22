package br.com.casaclara.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.casaclara.data.AgreementEntity
import br.com.casaclara.data.AppContainer
import br.com.casaclara.data.MemberEntity
import br.com.casaclara.data.ReimbursementEntity
import br.com.casaclara.data.SharedExpenseEntity
import br.com.casaclara.data.SharedTaskEntity
import br.com.casaclara.domain.Calculators
import br.com.casaclara.domain.Dates
import br.com.casaclara.domain.Money
import br.com.casaclara.domain.Routes
import br.com.casaclara.domain.Shared
import br.com.casaclara.ui.FREQ_LABEL
import br.com.casaclara.ui.asState
import br.com.casaclara.ui.components.Ill
import br.com.casaclara.ui.components.AlertBox
import br.com.casaclara.ui.components.BrandCard
import br.com.casaclara.ui.components.CrudList
import br.com.casaclara.ui.components.EmptySpec
import br.com.casaclara.ui.components.FieldSpec
import br.com.casaclara.ui.components.FieldType
import br.com.casaclara.ui.components.Init
import br.com.casaclara.ui.components.ItemRow
import br.com.casaclara.ui.components.LocalConfirm
import br.com.casaclara.ui.components.LocalFeedback
import br.com.casaclara.ui.components.MascotBubble
import br.com.casaclara.ui.components.Mood
import br.com.casaclara.ui.components.RowSpec
import br.com.casaclara.ui.components.ScreenHeader
import br.com.casaclara.ui.components.SecondaryButton
import br.com.casaclara.ui.components.SectionTitle
import br.com.casaclara.ui.components.Stat
import br.com.casaclara.ui.components.TabBody
import br.com.casaclara.ui.components.Tabs
import br.com.casaclara.ui.components.Tone
import br.com.casaclara.ui.components.opts
import br.com.casaclara.ui.theme.Brand
import kotlinx.coroutines.launch

@Composable
fun SharedScreen(c: AppContainer, nav: NavController, initialTab: Int) {
    var tab by rememberSaveable { mutableIntStateOf(initialTab) }
    val members by c.db.shared().members().asState(emptyList())
    Column {
        Row { TextButton(onClick = { nav.popBackStack() }) { Text("‹ Voltar") } }
        Box(Modifier.padding(horizontal = 16.dp)) { ScreenHeader("Casa compartilhada", "Contas divididas, tarefas combinadas e amizade preservada.", ill = Ill.GRUPO, tone = Tone.ROXO) }
        Tabs(listOf("Quem deve a quem", "Moradores", "Despesas", "Reembolsos", "Tarefas", "Acordos"), tab) { tab = it }
        if (members.size < 2 && tab in listOf(0, 2, 3)) {
            TabBody {
                MascotBubble("Primeiro, cadastre pelo menos duas pessoas — incluindo você.", Mood.PENSANDO) { TextButton(onClick = { tab = 1 }) { Text("Cadastrar moradores") } }
                AlertBox("Os dados da casa compartilhada ficam só neste aparelho. Cada morador não vê no próprio celular (isso exigiria um servidor).", "INFO")
            }
        } else when (tab) {
            0 -> ResumoShared(c, nav)
            1 -> MoradoresTab(c)
            2 -> DespesasTab(c)
            3 -> ReembolsosTab(c)
            4 -> TarefasShared(c)
            else -> AcordosTab(c)
        }
    }
}

@Composable
private fun names(c: AppContainer): (Long?) -> String {
    val members by c.db.shared().members().asState(emptyList())
    return { id -> members.firstOrNull { it.id == id }?.let { (it.emoji?.let { e -> "$e " } ?: "") + it.name } ?: "Ex-morador(a)" }
}

@Composable
private fun ResumoShared(c: AppContainer, nav: NavController) {
    val members by c.db.shared().members().asState(emptyList())
    val exps by c.db.shared().expenses().asState(emptyList())
    val reimb by c.db.shared().reimbursements().asState(emptyList())
    val name = names(c)
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    val confirm = LocalConfirm.current
    val bal = Shared.balances(members.map { it.id }, exps.map { Shared.Expense(it.amount, it.paidBy, it.splitIds()) }, reimb.map { Shared.Transfer(it.fromId, it.toId, it.amount) })
    val transfers = Shared.settle(bal)
    val month = Dates.currentMonthKey()
    TabBody {
        members.forEach { m ->
            val v = bal[m.id] ?: 0
            BrandCard(tone = if (v > 0) Tone.VERDE else if (v < 0) Tone.CORAL else Tone.NEUTRO, padding = 12.dp) {
                Stat(name(m.id), Money.format(kotlin.math.abs(v)), color = if (v > 0) Brand.VerdeEscuro else if (v < 0) MaterialTheme.colorScheme.error else null)
                Text(if (v > 0) "tem a receber" else if (v < 0) "precisa pagar" else "tudo certo", style = MaterialTheme.typography.bodySmall)
            }
        }
        SectionTitle("Acerto sugerido")
        if (transfers.isEmpty()) MascotBubble("Tudo acertado! Ninguém deve nada a ninguém. 🎉", Mood.COMEMORANDO, 64.dp)
        transfers.forEach { t ->
            ItemRow(
                title = "${name(t.de)} → ${name(t.para)}", subtitle = "Menor número de transferências para zerar", amount = Money.format(t.valor),
                extra = {
                    TextButton(onClick = {
                        confirm.ask("Registrar pagamento?", "Confirma que ${name(t.de)} pagou ${Money.format(t.valor)} para ${name(t.para)}?", "Foi pago", danger = false) {
                            scope.launch { c.db.shared().upsertReimbursement(ReimbursementEntity(fromId = t.de, toId = t.para, amount = t.valor, date = Dates.todayEpoch(), note = "Acerto sugerido")) }
                            fb.celebrate("Acerto registrado. Paz na casa!")
                        }
                    }) { Text("Foi pago") }
                },
            )
        }
        Text("Despesas da casa em ${Dates.monthLabel(month).lowercase()}: ${Money.format(exps.filter { Dates.monthKey(it.date) == month }.sumOf { it.amount })}", fontWeight = FontWeight.Bold)
        SecondaryButton("➗ Calculadora de divisão (proporcional)") { nav.navigate(Routes.calc("divisao")) }
        AlertBox("Os dados da casa compartilhada ficam só neste aparelho. A sincronização entre moradores depende de um servidor (veja Planos).", "INFO")
    }
}

@Composable
private fun MoradoresTab(c: AppContainer) {
    val members by c.db.shared().members().asState(emptyList())
    val exps by c.db.shared().expenses().asState(emptyList())
    val reimb by c.db.shared().reimbursements().asState(emptyList())
    TabBody {
        CrudList(
            items = members, itemName = "morador(a)", masculine = true, title = "Moradores", description = "Inclua você também.",
            fields = listOf(
                FieldSpec("name", "Nome", required = true),
                FieldSpec("emoji", "Emoji para identificar", FieldType.SELECT, options = listOf("" to "Nenhum") + listOf("🙂", "😎", "🌻", "🐱", "🐶", "🎸", "📚", "⚽", "🌈", "🍕").map { it to it }),
            ),
            toForm = { m -> if (m == null) emptyMap() else mapOf("name" to m.name, "emoji" to (m.emoji ?: "")) },
            save = { d, old -> c.db.shared().upsertMember(MemberEntity(id = old?.id ?: 0, name = d.str("name"), emoji = d.text("emoji"), isDemo = old?.isDemo ?: false)) },
            delete = { c.db.shared().deleteMember(it) },
            row = { m -> RowSpec((m.emoji?.let { "$it " } ?: "") + m.name, "${exps.count { it.paidBy == m.id }} despesa(s) paga(s)") },
            empty = EmptySpec("Ninguém cadastrado", "Cadastre você e quem divide a casa com você."),
            isDemo = { it.isDemo },
            canDelete = { m -> if (exps.any { it.paidBy == m.id || m.id in it.splitIds() } || reimb.any { it.fromId == m.id || it.toId == m.id }) "Essa pessoa tem despesas ou reembolsos registrados. Edite ou exclua esses registros antes." else null },
        )
    }
}

@Composable
private fun DespesasTab(c: AppContainer) {
    val members by c.db.shared().members().asState(emptyList())
    val exps by c.db.shared().expenses().asState(emptyList())
    val name = names(c)
    val memberOpts = members.map { it.id.toString() to ((it.emoji?.let { e -> "$e " } ?: "") + it.name) }
    TabBody {
        CrudList(
            items = exps, itemName = "despesa", title = "Despesas da casa", description = "Quem pagou e entre quem divide. Divisão em partes iguais (centavos distribuídos sem perder nada).",
            fields = listOf(
                FieldSpec("description", "O que foi", required = true, placeholder = "Ex.: Conta de luz"),
                FieldSpec("amount", "Valor", FieldType.MONEY, true, validate = { d -> if ((d.money("amount") ?: 0) <= 0) "O valor precisa ser maior que zero." else null }),
                FieldSpec("date", "Data", FieldType.DATE, true),
                FieldSpec("paidBy", "Quem pagou", FieldType.SELECT, true, memberOpts),
                FieldSpec("split", "Dividir entre", FieldType.MULTI, true, memberOpts),
            ),
            toForm = { e -> if (e == null) mapOf("date" to Init.today(), "paidBy" to (members.firstOrNull()?.id?.toString() ?: ""), "split" to members.joinToString(",") { it.id.toString() }) else mapOf("description" to e.description, "amount" to Init.money(e.amount), "date" to Init.date(e.date), "paidBy" to e.paidBy.toString(), "split" to e.splitAmong) },
            save = { d, old -> c.db.shared().upsertExpense(SharedExpenseEntity(id = old?.id ?: 0, description = d.str("description"), amount = d.money("amount")!!, date = d.date("date")!!, paidBy = d.str("paidBy").toLong(), splitAmong = d.multi("split").joinToString(","), isDemo = old?.isDemo ?: false)) },
            delete = { c.db.shared().deleteExpense(it) },
            groupBy = { Dates.monthLabel(Dates.monthKey(it.date)) },
            row = { e ->
                val ids = e.splitIds()
                val parts = Calculators.split(e.amount, List(ids.size.coerceAtLeast(1)) { 1.0 })
                RowSpec(e.description, "${Dates.br(e.date)} · pago por ${name(e.paidBy)} · ${Money.format(parts.firstOrNull() ?: 0)} para cada (${ids.size})", Money.format(e.amount))
            },
            empty = EmptySpec("Nenhuma despesa ainda", "Pagou a internet da casa? Registre aqui e o app calcula quem deve quanto."),
            isDemo = { it.isDemo },
        )
    }
}

@Composable
private fun ReembolsosTab(c: AppContainer) {
    val members by c.db.shared().members().asState(emptyList())
    val items by c.db.shared().reimbursements().asState(emptyList())
    val name = names(c)
    val memberOpts = members.map { it.id.toString() to it.name }
    TabBody {
        CrudList(
            items = items, itemName = "reembolso", masculine = true, title = "Reembolsos e acertos", description = "Quando alguém paga a parte que devia para outra pessoa.",
            fields = listOf(
                FieldSpec("from", "Quem pagou", FieldType.SELECT, true, memberOpts),
                FieldSpec("to", "Para quem", FieldType.SELECT, true, memberOpts, validate = { d -> if (d.str("to") == d.str("from")) "Escolha uma pessoa diferente." else null }),
                FieldSpec("amount", "Valor", FieldType.MONEY, true, validate = { d -> if ((d.money("amount") ?: 0) <= 0) "O valor precisa ser maior que zero." else null }),
                FieldSpec("date", "Data", FieldType.DATE, true),
                FieldSpec("note", "Anotação"),
            ),
            toForm = { r -> if (r == null) mapOf("date" to Init.today(), "from" to (members.getOrNull(1)?.id?.toString() ?: ""), "to" to (members.firstOrNull()?.id?.toString() ?: "")) else mapOf("from" to r.fromId.toString(), "to" to r.toId.toString(), "amount" to Init.money(r.amount), "date" to Init.date(r.date), "note" to (r.note ?: "")) },
            save = { d, old -> c.db.shared().upsertReimbursement(ReimbursementEntity(id = old?.id ?: 0, fromId = d.str("from").toLong(), toId = d.str("to").toLong(), amount = d.money("amount")!!, date = d.date("date")!!, note = d.text("note"), isDemo = old?.isDemo ?: false)) },
            delete = { c.db.shared().deleteReimbursement(it) },
            row = { r -> RowSpec("${name(r.fromId)} → ${name(r.toId)}", Dates.br(r.date) + (r.note?.let { " · $it" } ?: ""), Money.format(r.amount)) },
            empty = EmptySpec("Nenhum reembolso ainda", "Use o acerto sugerido em \"Quem deve a quem\" ou registre aqui.", Mood.TRANQUILA),
            isDemo = { it.isDemo },
        )
    }
}

@Composable
private fun TarefasShared(c: AppContainer) {
    val members by c.db.shared().members().asState(emptyList())
    val items by c.db.shared().tasks().asState(emptyList())
    val name = names(c)
    val scope = rememberCoroutineScope()
    val confirm = LocalConfirm.current
    TabBody {
        CrudList(
            items = items, itemName = "tarefa", title = "Tarefas compartilhadas", description = "Quem faz o quê. Comece uma nova rodada quando todas estiverem feitas.",
            fields = listOf(
                FieldSpec("title", "Tarefa", required = true),
                FieldSpec("assignee", "Responsável", FieldType.SELECT, options = listOf("" to "A combinar") + members.map { it.id.toString() to it.name }),
                FieldSpec("frequency", "Frequência", FieldType.SELECT, options = FREQ_LABEL.toList()),
            ),
            toForm = { t -> if (t == null) mapOf("frequency" to "SEMANAL") else mapOf("title" to t.title, "assignee" to (t.assignee?.toString() ?: ""), "frequency" to t.frequency) },
            save = { d, old -> c.db.shared().upsertTask(SharedTaskEntity(id = old?.id ?: 0, title = d.str("title"), assignee = d.text("assignee")?.toLongOrNull(), frequency = d.text("frequency") ?: "SEMANAL", done = old?.done ?: false, isDemo = old?.isDemo ?: false)) },
            delete = { c.db.shared().deleteTask(it) },
            groupBy = { t -> if (t.assignee == null) "A combinar" else name(t.assignee) },
            row = { t -> RowSpec(t.title, FREQ_LABEL[t.frequency], checked = t.done, onCheck = { v -> scope.launch { c.db.shared().upsertTask(t.copy(done = v)) } }) },
            empty = EmptySpec("Nenhuma tarefa combinada", "Lixo, louça, banheiro, compras… dividam aqui."),
            isDemo = { it.isDemo },
            headerActions = { if (items.any { it.done }) TextButton(onClick = { confirm.ask("Nova rodada?", "Todas as tarefas voltam para \"a fazer\".", "Nova rodada", danger = false) { scope.launch { c.db.shared().resetTasks() } } }) { Text("↺ Nova rodada") } },
        )
    }
}

@Composable
private fun AcordosTab(c: AppContainer) {
    val items by c.db.shared().agreements().asState(emptyList())
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    TabBody {
        CrudList(
            items = items, itemName = "acordo", masculine = true, title = "Acordos da casa", description = "Regras combinadas entre todos. Escrito é melhor que lembrado.",
            fields = listOf(FieldSpec("title", "Tema", required = true, placeholder = "Ex.: Visitas"), FieldSpec("text", "O combinado", FieldType.LONGTEXT, true)),
            toForm = { a -> if (a == null) emptyMap() else mapOf("title" to a.title, "text" to a.text) },
            save = { d, old -> c.db.shared().upsertAgreement(AgreementEntity(id = old?.id ?: 0, title = d.str("title"), text = d.str("text"), isDemo = old?.isDemo ?: false)) },
            delete = { c.db.shared().deleteAgreement(it) },
            row = { a -> RowSpec(a.title, a.text) },
            empty = EmptySpec("Nenhum acordo ainda", "Comece pelos modelos e adapte com quem mora com você.", Mood.DICA),
            isDemo = { it.isDemo },
            headerActions = { TextButton(onClick = { scope.launch { c.loadAgreements() }; fb.toast("Modelos adicionados. Adaptem à vontade!", Mood.COMEMORANDO) }) { Text("✨ Modelos") } },
        )
    }
}
