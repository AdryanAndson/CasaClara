package br.com.casaclara.domain

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Dinheiro é guardado em centavos (Long) para evitar erros de arredondamento.
 */
object Money {
    /** 123456 -> "R$ 1.234,56" */
    fun format(cents: Long?): String {
        val c = cents ?: 0L
        val neg = c < 0
        val a = abs(c)
        val reais = a / 100
        val cent = (a % 100).toString().padStart(2, '0')
        val grouped = reais.toString().reversed().chunked(3).joinToString(".").reversed()
        return (if (neg) "-R$ " else "R$ ") + grouped + "," + cent
    }

    /** Formata para campo editável: 123456 -> "1234,56" */
    fun toInput(cents: Long?): String {
        if (cents == null) return ""
        val neg = cents < 0
        val a = abs(cents)
        return (if (neg) "-" else "") + (a / 100).toString() + "," + (a % 100).toString().padStart(2, '0')
    }

    /**
     * Converte texto digitado em centavos. Aceita "1.234,56", "1234,5", "1234.56", "R$ 50".
     * Retorna null se o texto for inválido.
     */
    fun parse(input: String?): Long? {
        if (input == null) return null
        var s = input.trim().replace("R$", "", ignoreCase = true).replace(" ", "").replace(" ", "")
        if (s.isEmpty()) return null
        var neg = false
        if (s.startsWith("-")) { neg = true; s = s.substring(1) }
        if (s.isEmpty() || !s.all { it.isDigit() || it == '.' || it == ',' }) return null
        val lastComma = s.lastIndexOf(',')
        val lastDot = s.lastIndexOf('.')
        s = when {
            lastComma >= 0 && lastDot >= 0 ->
                if (lastComma > lastDot) s.replace(".", "").replace(',', '.') else s.replace(",", "")
            lastComma >= 0 -> {
                if (s.count { it == ',' } > 1) return null
                s.replace(',', '.')
            }
            lastDot >= 0 -> {
                val parts = s.split('.')
                if (parts.size > 2 || (parts.size == 2 && parts[1].length == 3)) s.replace(".", "") else s
            }
            else -> s
        }
        if (s.startsWith(".")) s = "0$s"
        val d = s.toDoubleOrNull() ?: return null
        val cents = (d * 100).roundToLong()
        return if (neg) -cents else cents
    }

    fun fromReais(v: Double): Long = (v * 100).roundToLong()
    fun toReais(cents: Long): Double = cents / 100.0

    /** Percentual com vírgula: 0.305 -> "30,5%" */
    fun percent(fraction: Double, digits: Int = 0): String {
        if (fraction.isNaN() || fraction.isInfinite()) return "—"
        val factor = Math.pow(10.0, digits.toDouble())
        val v = Math.round(fraction * 100 * factor) / factor
        val txt = if (digits == 0) v.toLong().toString() else String.format(java.util.Locale.ROOT, "%.${digits}f", v)
        return txt.replace('.', ',') + "%"
    }
}
