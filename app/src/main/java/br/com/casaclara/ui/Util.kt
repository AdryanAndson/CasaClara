package br.com.casaclara.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.casaclara.data.BillEntity
import br.com.casaclara.data.HouseTaskEntity
import br.com.casaclara.domain.Dates
import br.com.casaclara.domain.Journey
import br.com.casaclara.domain.JourneyState
import br.com.casaclara.domain.Snapshot
import br.com.casaclara.data.UserPrefs
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Coleta um Flow como estado. O Flow é lembrado entre recomposições (as consultas do Room criam um Flow novo
 * a cada chamada; sem isso a coleta reiniciava a cada recomposição, piscando listas vazias e gastando bateria).
 * Passe [keys] quando a consulta depende de parâmetros que mudam.
 */
@androidx.compose.runtime.Composable
fun <T> Flow<T>.asState(initial: T, vararg keys: Any?): State<T> {
    val stable = androidx.compose.runtime.remember(*keys) { this }
    return stable.collectAsStateWithLifecycle(initialValue = initial)
}

val FREQ_LABEL = linkedMapOf("DIARIA" to "Diária", "SEMANAL" to "Semanal", "QUINZENAL" to "Quinzenal", "MENSAL" to "Mensal", "SEMESTRAL" to "Semestral")
val FREQ_DAYS = mapOf("DIARIA" to 1L, "SEMANAL" to 7L, "QUINZENAL" to 15L, "MENSAL" to 30L, "SEMESTRAL" to 182L)

data class BillStatus(val due: LocalDate, val days: Long, val paid: Boolean, val late: Boolean)

fun billStatus(b: BillEntity, today: LocalDate = Dates.today()): BillStatus {
    val month = Dates.monthKey(today)
    val paid = b.paidMonth == month
    val thisMonthDue = Dates.dueInMonth(b.dueDay, month)
    val late = !paid && thisMonthDue.isBefore(today)
    val due = when {
        late -> thisMonthDue
        paid -> Dates.dueInMonth(b.dueDay, Dates.shiftMonth(month, 1))
        else -> Dates.nextMonthlyDue(b.dueDay, today)
    }
    return BillStatus(due, Dates.daysBetween(today, due), paid, late)
}

data class TaskStatus(val next: LocalDate, val days: Long, val late: Boolean, val never: Boolean)

fun taskStatus(t: HouseTaskEntity, today: LocalDate = Dates.today()): TaskStatus {
    val last = t.lastDone ?: return TaskStatus(today, 0, false, true)
    val next = LocalDate.ofEpochDay(last + (FREQ_DAYS[t.frequency] ?: 7L))
    val d = Dates.daysBetween(today, next)
    return TaskStatus(next, d, d < 0, false)
}

fun journeyOf(s: Snapshot, p: UserPrefs): JourneyState = Journey.compute(s, p.situation, p.goal, p.manualSteps, p.skippedSteps)

fun greeting(): String {
    val h = java.time.LocalTime.now().hour
    return when { h < 12 -> "Bom dia"; h < 18 -> "Boa tarde"; else -> "Boa noite" }
}

/** Categorias padrão. */
val EXPENSE_CATEGORIES = listOf("Moradia", "Contas da casa", "Mercado", "Transporte", "Saúde", "Educação", "Lazer", "Roupas", "Casa e móveis", "Dívidas", "Assinaturas", "Pets", "Outros")
val INCOME_CATEGORIES = listOf("Salário", "Freela", "Vendas", "Bico", "Benefício", "Mesada/ajuda", "Outros")
val MARKET_CATEGORIES = listOf("Hortifrúti", "Grãos e massas", "Carnes e ovos", "Laticínios", "Padaria", "Bebidas", "Limpeza", "Higiene", "Congelados", "Temperos", "Outros")
val ROOMS = listOf("Cozinha", "Banheiro", "Quarto", "Sala", "Área de serviço", "Varanda", "Casa toda")
val WEEKDAYS = listOf("Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo")
val MEAL_TYPES = listOf("Café da manhã", "Almoço", "Lanche", "Jantar")

/** Abre o menu de compartilhar do Android (WhatsApp, Telegram, e-mail…). */
fun shareText(ctx: android.content.Context, title: String, text: String) {
    val send = android.content.Intent(android.content.Intent.ACTION_SEND).setType("text/plain").putExtra(android.content.Intent.EXTRA_TEXT, text)
    ctx.startActivity(android.content.Intent.createChooser(send, title).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
}

/** Lista de compras em texto, agrupada por categoria, pronta para mandar por mensagem. */
fun marketListText(items: List<br.com.casaclara.data.ShopEntity>, title: String = "Lista de mercado"): String = buildString {
    val pend = items.filter { !it.bought }
    appendLine("🛒 $title")
    if (pend.isEmpty()) { appendLine("Nada pendente! 🎉"); return@buildString }
    pend.groupBy { it.category }.toSortedMap().forEach { (cat, l) ->
        appendLine(); appendLine("*$cat*")
        l.forEach { s ->
            val q = if (s.qty == 1.0 && s.unit == "un") "" else " (${br.com.casaclara.ui.components.Init.num(s.qty)} ${s.unit})"
            appendLine("☐ ${s.name}$q")
        }
    }
    val est = pend.sumOf { ((it.price ?: 0L) * it.qty).toLong() }
    if (est > 0) { appendLine(); appendLine("Estimativa: ${br.com.casaclara.domain.Money.format(est)}") }
    appendLine(); append("Enviado pelo app Casa Clara")
}
