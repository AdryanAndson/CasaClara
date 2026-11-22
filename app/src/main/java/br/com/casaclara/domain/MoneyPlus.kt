package br.com.casaclara.domain

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

/** Previsão do saldo no fim do mês (estimativa simples e explicável). Valores em centavos. */
object Forecast {
    data class Result(
        val saldoAgora: Long, val entradasPrevistas: Long, val fixosPrevistos: Long, val contasAPagar: Long,
        val gastoVariavelPorDia: Long, val variavelRestante: Long, val diasRestantes: Int, val previsto: Long,
    )

    /**
     * @param entrouAteAgora / saiuAteAgora: totais do mês até hoje
     * @param variavelAteAgora: gastos do mês que não são fixos (sem recorrentes)
     */
    fun endOfMonth(entrouAteAgora: Long, saiuAteAgora: Long, variavelAteAgora: Long, entradasPrevistas: Long, fixosPrevistos: Long, contasAPagar: Long, diaDoMes: Int, diasNoMes: Int): Result {
        val dia = diaDoMes.coerceIn(1, diasNoMes)
        val restantes = diasNoMes - dia
        val porDia = if (dia > 0) variavelAteAgora / dia else 0
        val variavelRest = porDia * restantes
        val agora = entrouAteAgora - saiuAteAgora
        return Result(agora, entradasPrevistas, fixosPrevistos, contasAPagar, porDia, variavelRest, restantes,
            agora + entradasPrevistas - fixosPrevistos - contasAPagar - variavelRest)
    }
}

/** Simulador de quitação de dívidas: bola de neve (menor saldo primeiro) ou avalanche (maior juros primeiro). */
object DebtPlan {
    data class Debt(val name: String, val balance: Long, val ratePct: Double, val minPayment: Long)
    enum class Method(val label: String) { BOLA_DE_NEVE("Bola de neve (menor saldo primeiro)"), AVALANCHE("Avalanche (maior juros primeiro)") }
    data class Result(val months: Int, val totalInterest: Long, val totalPaid: Long, val payoff: List<Pair<String, Int>>, val feasible: Boolean)

    fun simulate(debts: List<Debt>, extra: Long, method: Method, maxMonths: Int = 600): Result {
        val list = debts.filter { it.balance > 0 }
        if (list.isEmpty()) return Result(0, 0, 0, emptyList(), true)
        val bal = list.map { it.balance.toDouble() }.toMutableList()
        val order = when (method) {
            Method.BOLA_DE_NEVE -> list.indices.sortedWith(compareBy({ list[it].balance }, { -list[it].ratePct }))
            Method.AVALANCHE -> list.indices.sortedWith(compareBy({ -list[it].ratePct }, { list[it].balance }))
        }
        val budget = list.sumOf { it.minPayment } + max(0, extra)
        var interest = 0.0; var paid = 0.0; var month = 0
        val payoff = mutableListOf<Pair<String, Int>>()
        while (bal.any { it > 0.5 } && month < maxMonths) {
            month++
            for (i in list.indices) if (bal[i] > 0) { val j = bal[i] * list[i].ratePct / 100.0; bal[i] += j; interest += j }
            var left = budget.toDouble()
            for (i in list.indices) if (bal[i] > 0) { val p = min(bal[i], list[i].minPayment.toDouble()); bal[i] -= p; left -= p; paid += p }
            for (i in order) { if (left <= 0) break; if (bal[i] > 0) { val p = min(bal[i], left); bal[i] -= p; left -= p; paid += p } }
            for (i in list.indices) if (bal[i] <= 0.5 && payoff.none { it.first == list[i].name }) { bal[i] = 0.0; payoff += list[i].name to month }
        }
        val ok = bal.all { it <= 0.5 }
        return Result(month, interest.roundToLong(), paid.roundToLong(), payoff, ok)
    }
}

/** Desafio das 52 semanas: na semana N você guarda N × valor base. */
object Challenge52 {
    fun deposit(base: Long, week: Int) = base * week.coerceIn(1, 52)
    fun total(base: Long) = base * (52 * 53 / 2)
    fun saved(base: Long, weeks: Set<Int>) = weeks.filter { it in 1..52 }.sumOf { deposit(base, it) }
}
