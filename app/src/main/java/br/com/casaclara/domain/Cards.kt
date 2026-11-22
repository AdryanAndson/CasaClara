package br.com.casaclara.domain

import java.time.LocalDate

/** Cartão de crédito: em qual fatura cai cada compra, quanto está usado do limite. */
object Cards {
    data class Purchase(val amount: Long, val date: LocalDate, val installments: Int = 1)

    /** Data de fechamento da fatura em que uma compra feita em [d] entra. */
    fun closingFor(d: LocalDate, closingDay: Int): LocalDate {
        val thisMonth = Dates.dueInMonth(closingDay, Dates.monthKey(d))
        // comprou depois do fechamento: vai para a fatura seguinte
        return if (d.isAfter(thisMonth)) Dates.dueInMonth(closingDay, Dates.shiftMonth(Dates.monthKey(d), 1)) else thisMonth
    }

    /** Vencimento correspondente a um fechamento. */
    fun dueFor(closing: LocalDate, dueDay: Int): LocalDate {
        val sameMonth = Dates.dueInMonth(dueDay, Dates.monthKey(closing))
        return if (sameMonth.isAfter(closing)) sameMonth else Dates.dueInMonth(dueDay, Dates.shiftMonth(Dates.monthKey(closing), 1))
    }

    /** Fatura identificada pelo mês do vencimento (yyyyMM). */
    fun invoiceKey(d: LocalDate, closingDay: Int, dueDay: Int): Int = Dates.monthKey(dueFor(closingFor(d, closingDay), dueDay))

    /** Soma por fatura, distribuindo compras parceladas nas faturas seguintes (centavos sem perda). */
    fun invoices(purchases: List<Purchase>, closingDay: Int, dueDay: Int): Map<Int, Long> {
        val out = sortedMapOf<Int, Long>()
        for (p in purchases) {
            val n = p.installments.coerceIn(1, 48)
            val parts = Calculators.split(p.amount, List(n) { 1.0 })
            val first = invoiceKey(p.date, closingDay, dueDay)
            parts.forEachIndexed { i, v -> val k = Dates.shiftMonth(first, i); out[k] = (out[k] ?: 0L) + v }
        }
        return out
    }

    /** Limite usado = tudo que está em faturas ainda não pagas. */
    fun usedLimit(invoices: Map<Int, Long>, paid: Set<Int>): Long = invoices.filterKeys { it !in paid }.values.sum()
}

/** Lançamentos que se repetem todo mês (salário, aluguel, assinaturas…). */
object Recurring {
    /**
     * Meses (yyyyMM) em que o lançamento ainda precisa ser criado, até hoje.
     * Gera o mês atual só se o dia já chegou. Limita a 12 meses para trás.
     */
    fun monthsToGenerate(startKey: Int, lastGenerated: Int?, day: Int, today: LocalDate): List<Int> {
        val current = Dates.monthKey(today)
        var k = if (lastGenerated == null) startKey else Dates.shiftMonth(lastGenerated, 1)
        val floor = Dates.shiftMonth(current, -11)
        if (k < floor) k = floor
        val out = mutableListOf<Int>()
        while (k <= current) {
            val due = Dates.dueInMonth(day, k)
            if (!due.isAfter(today)) out += k
            k = Dates.shiftMonth(k, 1)
        }
        return out
    }
}

/** Importação de extrato bancário em CSV (formatos variados de bancos brasileiros). */
object StatementImport {
    data class Row(val date: LocalDate, val description: String, val amount: Long) {
        val isIncome get() = amount > 0
    }
    data class Result(val rows: List<Row>, val skipped: Int, val error: String? = null)

    private val DATE_BR = Regex("^(\\d{1,2})[/.-](\\d{1,2})[/.-](\\d{2,4})$")
    private val DATE_ISO = Regex("^(\\d{4})-(\\d{1,2})-(\\d{1,2})")

    fun parseDate(s: String): LocalDate? {
        val t = s.trim().trim('"')
        DATE_ISO.find(t)?.let { m -> return runCatching { LocalDate.of(m.groupValues[1].toInt(), m.groupValues[2].toInt(), m.groupValues[3].toInt()) }.getOrNull() }
        DATE_BR.matchEntire(t)?.let { m ->
            var y = m.groupValues[3].toInt(); if (y < 100) y += 2000
            return runCatching { LocalDate.of(y, m.groupValues[2].toInt(), m.groupValues[1].toInt()) }.getOrNull()
        }
        return null
    }

    fun parseAmount(s: String): Long? {
        var t = s.trim().trim('"').replace("R$", "").replace(" ", "").replace(" ", "")
        if (t.isEmpty()) return null
        var neg = false
        if (t.endsWith("-")) { neg = true; t = t.dropLast(1) }
        if (t.startsWith("(") && t.endsWith(")")) { neg = true; t = t.drop(1).dropLast(1) }
        if (t.endsWith("D", ignoreCase = true)) { neg = true; t = t.dropLast(1) } else if (t.endsWith("C", ignoreCase = true)) t = t.dropLast(1)
        val v = Money.parse(t) ?: return null
        return if (neg) -kotlin.math.abs(v) else v
    }

    private fun splitLine(line: String, sep: Char): List<String> {
        val out = mutableListOf<String>(); val cur = StringBuilder(); var q = false
        for (ch in line) {
            when {
                ch == '"' -> q = !q
                ch == sep && !q -> { out += cur.toString(); cur.clear() }
                else -> cur.append(ch)
            }
        }
        out += cur.toString()
        return out.map { it.trim() }
    }

    fun parse(text: String): Result {
        val lines = text.removePrefix("﻿").lines().map { it.trimEnd('\r') }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return Result(emptyList(), 0, "O arquivo está vazio.")
        val sep = listOf(';', '\t', ',').maxBy { c -> lines.take(10).sumOf { l -> l.count { it == c } } }
        val table = lines.map { splitLine(it, sep) }
        val cols = table.maxOf { it.size }
        // descobre colunas pela maioria das linhas
        fun score(i: Int, ok: (String) -> Boolean) = table.count { r -> r.getOrNull(i)?.let(ok) == true }
        val dateCol = (0 until cols).maxByOrNull { score(it) { s -> parseDate(s) != null } } ?: return Result(emptyList(), lines.size, "Não encontrei uma coluna de datas.")
        if (score(dateCol) { parseDate(it) != null } == 0) return Result(emptyList(), lines.size, "Não encontrei uma coluna de datas.")
        val amountCandidates = (0 until cols).filter { it != dateCol }
        val amountCol = amountCandidates.maxByOrNull { i -> score(i) { s -> s.any { it.isDigit() } && (s.contains(',') || s.contains('.')) && parseAmount(s) != null } }
            ?: return Result(emptyList(), lines.size, "Não encontrei uma coluna de valores.")
        val descCol = (0 until cols).filter { it != dateCol && it != amountCol }
            .maxByOrNull { i -> table.sumOf { r -> r.getOrNull(i)?.count { it.isLetter() } ?: 0 } }
        val rows = mutableListOf<Row>(); var skipped = 0
        for (r in table) {
            val d = r.getOrNull(dateCol)?.let(::parseDate)
            val v = r.getOrNull(amountCol)?.let(::parseAmount)
            if (d == null || v == null || v == 0L) { skipped++; continue }
            val desc = descCol?.let { r.getOrNull(it) }?.trim('"')?.ifBlank { null } ?: "Lançamento do extrato"
            rows += Row(d, desc.take(80), v)
        }
        return Result(rows, skipped, if (rows.isEmpty()) "Nenhum lançamento reconhecido no arquivo." else null)
    }

    private val RULES = listOf(
        "Mercado" to listOf("mercado", "supermerc", "atacad", "assai", "carrefour", "hortifruti", "padaria", "acougue"),
        "Transporte" to listOf("uber", "99 ", "99app", "onibus", "metro", "posto", "combust", "shell", "ipiranga", "estacion", "bilhete"),
        "Contas da casa" to listOf("energia", "enel", "cemig", "coelba", "light", "copel", "celpe", "neoenergia", "sabesp", "saneam", "embasa", "agua", "gas ", "internet", "vivo", "claro", "tim ", "oi "),
        "Moradia" to listOf("aluguel", "condominio", "iptu", "imobili"),
        "Saúde" to listOf("farmac", "drogar", "droga", "clinica", "hospital", "laborat", "dentist", "unimed", "hapvida"),
        "Assinaturas" to listOf("netflix", "spotify", "disney", "prime", "youtube", "globoplay", "hbo", "max ", "deezer", "icloud", "google one"),
        "Lazer" to listOf("ifood", "rappi", "restaur", "lanchon", "bar ", "cinema", "ingresso", "show"),
        "Educação" to listOf("escola", "faculd", "curso", "udemy", "livrar"),
        "Roupas" to listOf("renner", "riachuelo", "c&a", "cea ", "shein", "zara", "calcad"),
        "Casa e móveis" to listOf("magazine", "magalu", "casas bahia", "leroy", "tok&stok", "mobly", "madeira"),
        "Pets" to listOf("petz", "cobasi", "veterin", "pet "),
        "Dívidas" to listOf("emprest", "financ", "juros", "parcela", "fatura"),
        "Salário" to listOf("salario", "folha", "pagamento empresa", "proventos"),
    )

    private fun norm(s: String) = java.text.Normalizer.normalize(s.lowercase(), java.text.Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "") + " "

    /** Sugere categoria pela descrição. Quem decide no fim é a pessoa (dá para editar). */
    fun guessCategory(description: String, income: Boolean): String {
        val d = norm(description)
        if (income) return if (RULES.first { it.first == "Salário" }.second.any { d.contains(it) }) "Salário" else "Outros"
        return RULES.filter { it.first != "Salário" }.firstOrNull { (_, keys) -> keys.any { d.contains(it) } }?.first ?: "Outros"
    }
}

/** Plano semanal: distribui as tarefas da casa pelos dias, sem sobrecarregar nenhum. */
object WeekPlan {
    data class Task(val id: Long, val everyDays: Int, val lastDone: LocalDate?)

    /** Dias 0..6 (segunda a domingo) com os ids das tarefas sugeridas para cada dia. */
    fun plan(tasks: List<Task>, weekStart: LocalDate, today: LocalDate = weekStart): Map<Int, List<Long>> {
        val days = Array(7) { mutableListOf<Long>() }
        val firstDay = Dates.daysBetween(weekStart, today).toInt().coerceIn(0, 6)
        // diárias primeiro
        tasks.filter { it.everyDays <= 1 }.forEach { t -> for (i in firstDay..6) days[i] += t.id }
        val others = tasks.filter { it.everyDays > 1 }.sortedBy { it.everyDays }
        for (t in others) {
            var next = t.lastDone?.plusDays(t.everyDays.toLong()) ?: today
            if (next.isBefore(today)) next = today
            var idx = Dates.daysBetween(weekStart, next).toInt()
            while (idx <= 6) {
                // se o dia já está cheio, empurra para o dia mais leve próximo (até 2 dias depois)
                val candidates = (idx..minOf(6, idx + 2)).filter { it >= firstDay }
                if (candidates.isEmpty()) break
                val target = candidates.minBy { days[it].size }
                days[target] += t.id
                idx = target + t.everyDays
            }
        }
        return (0..6).associateWith { days[it].toList() }
    }
}

/** Resumo da semana para o painel. */
object Weekly {
    data class Summary(val spent: Long, val spentLastWeek: Long, val earned: Long) {
        val diff get() = spent - spentLastWeek
    }

    fun weekStart(d: LocalDate): LocalDate = d.minusDays((d.dayOfWeek.value - 1).toLong())

    fun summary(expenses: List<Finance.Movement>, incomes: List<Finance.Movement>, today: LocalDate): Summary {
        val ws = weekStart(today).toEpochDay(); val we = ws + 6
        val ls = ws - 7; val le = ws - 1
        return Summary(
            expenses.filter { it.epochDay in ws..we }.sumOf { it.valor },
            expenses.filter { it.epochDay in ls..le }.sumOf { it.valor },
            incomes.filter { it.epochDay in ws..we }.sumOf { it.valor },
        )
    }
}
