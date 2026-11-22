package br.com.casaclara.domain

import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/** Datas são guardadas como "epochDay" (Long) e meses como inteiro yyyyMM. */
object Dates {
    private val MESES = listOf("janeiro", "fevereiro", "março", "abril", "maio", "junho", "julho", "agosto", "setembro", "outubro", "novembro", "dezembro")
    private val DIAS = listOf("segunda", "terça", "quarta", "quinta", "sexta", "sábado", "domingo")

    fun today(): LocalDate = LocalDate.now()
    fun todayEpoch(): Long = today().toEpochDay()
    fun of(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)
    fun monthKey(d: LocalDate): Int = d.year * 100 + d.monthValue
    fun monthKey(epochDay: Long): Int = monthKey(of(epochDay))
    fun currentMonthKey(): Int = monthKey(today())
    fun yearMonth(key: Int): YearMonth = YearMonth.of(key / 100, key % 100)
    fun shiftMonth(key: Int, n: Int): Int { val ym = yearMonth(key).plusMonths(n.toLong()); return ym.year * 100 + ym.monthValue }

    fun br(epochDay: Long?): String {
        if (epochDay == null) return "—"
        val d = of(epochDay)
        return "%02d/%02d/%04d".format(d.dayOfMonth, d.monthValue, d.year)
    }
    fun monthLabel(key: Int): String {
        val ym = yearMonth(key)
        return MESES[ym.monthValue - 1].replaceFirstChar { it.uppercase() } + " de " + ym.year
    }
    fun monthShort(key: Int): String = MESES[yearMonth(key).monthValue - 1].take(3).replaceFirstChar { it.uppercase() }
    fun monthYearLabel(d: LocalDate): String = MESES[d.monthValue - 1] + " de " + d.year
    fun weekdayName(d: LocalDate): String = DIAS[d.dayOfWeek.value - 1]

    /** Converte "dd/mm/aaaa" em epochDay; null se inválido. */
    fun parseBr(s: String?): Long? {
        if (s.isNullOrBlank()) return null
        val p = s.trim().split('/', '-', '.')
        if (p.size != 3) return null
        val (a, b, c) = p.map { it.toIntOrNull() ?: return null }
        return try {
            if (p[0].length == 4) LocalDate.of(a, b, c).toEpochDay() else LocalDate.of(if (c < 100) 2000 + c else c, b, a).toEpochDay()
        } catch (e: Exception) { null }
    }

    fun daysBetween(from: LocalDate, to: LocalDate): Long = ChronoUnit.DAYS.between(from, to)

    /** Próximo vencimento de algo que vence todo mês no [day] (1..31), a partir de [from] (inclusive). */
    fun nextMonthlyDue(day: Int, from: LocalDate = today()): LocalDate {
        val d = day.coerceIn(1, 31)
        val thisMonth = from.withDayOfMonth(minOf(d, from.lengthOfMonth()))
        if (!thisMonth.isBefore(from)) return thisMonth
        val next = from.plusMonths(1)
        return next.withDayOfMonth(minOf(d, next.lengthOfMonth()))
    }

    fun dueInMonth(day: Int, key: Int): LocalDate {
        val ym = yearMonth(key)
        return ym.atDay(minOf(day.coerceIn(1, 31), ym.lengthOfMonth()))
    }

    fun relative(days: Long): String = when {
        days == 0L -> "hoje"
        days == 1L -> "amanhã"
        days == -1L -> "ontem"
        days < 0 -> "há ${-days} dias"
        else -> "em $days dias"
    }
}
