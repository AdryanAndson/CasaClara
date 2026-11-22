package br.com.casaclara.domain

import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToLong

object Finance {
    data class Movement(val valor: Long, val epochDay: Long, val categoria: String = "Outros")
    data class Summary(val entrou: Long, val saiu: Long) { val sobrou get() = entrou - saiu }

    fun summary(entradas: List<Movement>, saidas: List<Movement>, monthKey: Int): Summary {
        val inMonth = { m: Movement -> Dates.monthKey(m.epochDay) == monthKey }
        return Summary(entradas.filter(inMonth).sumOf { it.valor }, saidas.filter(inMonth).sumOf { it.valor })
    }

    /** Soma por categoria, ordenado do maior para o menor. Filtros opcionais de período. */
    fun byCategory(items: List<Movement>, fromEpoch: Long? = null, toEpoch: Long? = null): List<Pair<String, Long>> =
        items.filter { (fromEpoch == null || it.epochDay >= fromEpoch) && (toEpoch == null || it.epochDay <= toEpoch) }
            .groupBy { it.categoria.ifBlank { "Outros" } }
            .map { (k, v) -> k to v.sumOf { it.valor } }
            .sortedByDescending { it.second }

    /** Últimos [n] meses terminando em [endKey], com entradas e saídas. */
    fun lastMonths(entradas: List<Movement>, saidas: List<Movement>, endKey: Int, n: Int = 6): List<Pair<Int, Summary>> =
        (n - 1 downTo 0).map { i -> val k = Dates.shiftMonth(endKey, -i); k to summary(entradas, saidas, k) }

    /** Diferença entre dois meses (variação absoluta e percentual dos gastos). */
    data class MonthCompare(val gastoAtual: Long, val gastoAnterior: Long) {
        val diferenca get() = gastoAtual - gastoAnterior
        val variacao: Double? get() = if (gastoAnterior > 0) diferenca.toDouble() / gastoAnterior else null
    }
    fun compareMonths(saidas: List<Movement>, key: Int): MonthCompare {
        val prev = Dates.shiftMonth(key, -1)
        return MonthCompare(saidas.filter { Dates.monthKey(it.epochDay) == key }.sumOf { it.valor }, saidas.filter { Dates.monthKey(it.epochDay) == prev }.sumOf { it.valor })
    }

    enum class BudgetStatus { OK, ATENCAO, ESTOUROU }
    data class BudgetUsage(val uso: Double, val restante: Long, val status: BudgetStatus)
    fun budget(limite: Long, gasto: Long): BudgetUsage {
        val uso = if (limite > 0) gasto.toDouble() / limite else if (gasto > 0) Double.POSITIVE_INFINITY else 0.0
        val st = when { uso > 1.0 -> BudgetStatus.ESTOUROU; uso >= 0.8 -> BudgetStatus.ATENCAO; else -> BudgetStatus.OK }
        return BudgetUsage(uso, limite - gasto, st)
    }

    data class DebtRemaining(val restantes: Int, val valor: Long)
    fun debtRemaining(valorParcela: Long, parcelas: Int, pagas: Int): DebtRemaining {
        val r = max(0, parcelas - pagas)
        return DebtRemaining(r, r * max(0L, valorParcela))
    }

    /**
     * Custo de uma compra parcelada com juros (tabela Price, parcelas iguais).
     * @param taxaMensal fração ao mês (0.03 = 3% a.m.)
     */
    data class InstallmentCost(val parcela: Long, val totalPago: Long, val jurosPagos: Long)
    fun installmentCost(valorAVista: Long, parcelas: Int, taxaMensal: Double): InstallmentCost {
        val n = max(1, parcelas)
        val pv = max(0L, valorAVista).toDouble()
        val pmt = if (taxaMensal <= 0) pv / n else pv * taxaMensal / (1 - (1 + taxaMensal).pow(-n))
        val parcela = pmt.roundToLong()
        val total = parcela * n
        return InstallmentCost(parcela, total, total - valorAVista)
    }

    /** Taxa efetiva aproximada quando a pessoa sabe o preço à vista, a parcela e o número de parcelas (busca binária). */
    fun impliedMonthlyRate(valorAVista: Long, parcela: Long, parcelas: Int): Double? {
        if (valorAVista <= 0 || parcela <= 0 || parcelas <= 0) return null
        if (parcela.toDouble() * parcelas <= valorAVista) return 0.0
        var lo = 0.0; var hi = 1.0
        repeat(100) {
            val mid = (lo + hi) / 2
            val pv = parcela * (1 - (1 + mid).pow(-parcelas)) / mid
            if (pv > valorAVista) lo = mid else hi = mid
        }
        return (lo + hi) / 2
    }

    /** Juros compostos anuais equivalentes a uma taxa mensal. */
    fun annualFromMonthly(m: Double): Double = (1 + m).pow(12) - 1

    /** Quantos meses de custo de vida a reserva cobre. */
    fun emergencyCoverage(guardado: Long, custoMensal: Long): Double = if (custoMensal > 0) guardado.toDouble() / custoMensal else 0.0
}

object Shared {
    data class Expense(val valor: Long, val pagoPor: Long, val dividirEntre: List<Long>)
    data class Transfer(val de: Long, val para: Long, val valor: Long)

    /** Saldo de cada morador em centavos: positivo = tem a receber; negativo = deve. */
    fun balances(moradores: List<Long>, despesas: List<Expense>, reembolsos: List<Transfer>): Map<Long, Long> {
        val bal = moradores.associateWith { 0L }.toMutableMap()
        for (d in despesas) {
            val entre = d.dividirEntre.filter { it in bal }
            if (entre.isEmpty() || d.pagoPor !in bal) continue
            val partes = Calculators.split(d.valor, List(entre.size) { 1.0 })
            bal[d.pagoPor] = bal.getValue(d.pagoPor) + d.valor
            entre.forEachIndexed { i, p -> bal[p] = bal.getValue(p) - partes[i] }
        }
        for (r in reembolsos) {
            if (r.de in bal) bal[r.de] = bal.getValue(r.de) + r.valor
            if (r.para in bal) bal[r.para] = bal.getValue(r.para) - r.valor
        }
        return bal
    }

    /** Sugere poucas transferências para zerar todos os saldos. */
    fun settle(bal: Map<Long, Long>): List<Transfer> {
        val dev = bal.filter { it.value < 0 }.map { it.key to -it.value }.sortedByDescending { it.second }.map { arrayOf(it.first, it.second) }
        val cred = bal.filter { it.value > 0 }.map { it.key to it.value }.sortedByDescending { it.second }.map { arrayOf(it.first, it.second) }
        val out = mutableListOf<Transfer>()
        var i = 0; var j = 0
        while (i < dev.size && j < cred.size) {
            val x = minOf(dev[i][1], cred[j][1])
            if (x > 0) out += Transfer(dev[i][0], cred[j][0], x)
            dev[i][1] -= x; cred[j][1] -= x
            if (dev[i][1] == 0L) i++
            if (cred[j][1] == 0L) j++
        }
        return out
    }
}

object Csv {
    /** CSV com ponto e vírgula (padrão do Excel em pt-BR) e proteção contra fórmulas. */
    fun build(header: List<String>, rows: List<List<String>>): String {
        fun esc(v: String): String {
            var s = v
            if (s.isNotEmpty() && s[0] in "=+-@" && s.toDoubleOrNull() == null && !s.matches(Regex("-?[\\d.]+,\\d+"))) s = "'$s"
            return if (s.any { it == ';' || it == '"' || it == '\n' || it == '\r' }) "\"" + s.replace("\"", "\"\"") + "\"" else s
        }
        return (listOf(header) + rows).joinToString("\r\n") { r -> r.joinToString(";") { esc(it) } }
    }
    fun money(cents: Long): String = Money.toInput(cents)
}
