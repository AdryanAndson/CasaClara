package br.com.casaclara.domain

import kotlin.math.max
import kotlin.math.min

/**
 * Cálculos trabalhistas (CLT) com as tabelas de 2026 — sempre estimativas.
 * INSS: Portaria Interministerial MPS/MF nº 13/2026 (teto R$ 8.475,55).
 * IRRF: tabela progressiva mensal + redução da Lei nº 15.270/2025 (isenção até R$ 5.000).
 * Valores em centavos.
 */
object Labor {
    const val YEAR = 2026
    const val SALARIO_MINIMO = 162100L
    const val TETO_INSS = 847555L
    const val DEDUCAO_DEPENDENTE = 18959L
    const val DESCONTO_SIMPLIFICADO = 60720L

    private data class Faixa(val ate: Long?, val aliquota: Double, val deduzir: Double)

    private val INSS = listOf(Faixa(162100, 0.075, 0.0), Faixa(290284, 0.09, 24.32), Faixa(435427, 0.12, 111.41), Faixa(TETO_INSS, 0.14, 198.50))
    private val IRRF = listOf(Faixa(242880, 0.0, 0.0), Faixa(282665, 0.075, 182.16), Faixa(375105, 0.15, 394.16), Faixa(466468, 0.225, 675.49), Faixa(null, 0.275, 908.73))

    private fun cents(reais: Double): Long = java.math.BigDecimal(reais.toString()).movePointRight(2).setScale(0, java.math.RoundingMode.HALF_UP).toLong()

    /** Contribuição do empregado ao INSS (progressiva, limitada ao teto). */
    fun inss(bruto: Long): Long {
        if (bruto <= 0) return 0
        val base = min(bruto, TETO_INSS)
        val f = INSS.first { base <= it.ate!! }
        val v = java.math.BigDecimal.valueOf(base, 2).multiply(java.math.BigDecimal(f.aliquota.toString())).subtract(java.math.BigDecimal(f.deduzir.toString()))
        return max(0, v.movePointRight(2).setScale(0, java.math.RoundingMode.HALF_UP).toLong())
    }

    private fun tabela(base: Long): Double {
        if (base <= 0) return 0.0
        val f = IRRF.first { it.ate == null || base <= it.ate }
        return max(0.0, base / 100.0 * f.aliquota - f.deduzir)
    }

    /** Redução da Lei 15.270/2025, calculada sobre os rendimentos tributáveis (bruto). */
    fun reducao(rendimentos: Long, imposto: Double): Double = when {
        rendimentos <= 500000 -> min(imposto, 312.89)
        rendimentos <= 735000 -> min(imposto, max(0.0, 978.62 - 0.133145 * rendimentos / 100.0))
        else -> 0.0
    }

    data class Ir(val imposto: Long, val base: Long, val simplificado: Boolean, val reducao: Long)

    /** IRRF: usa a dedução mais vantajosa (legal ou desconto simplificado) e aplica a redução de 2026. */
    fun irrf(bruto: Long, inss: Long, dependentes: Int = 0, pensao: Long = 0): Ir {
        if (bruto <= 0) return Ir(0, 0, false, 0)
        val baseLegal = bruto - inss - dependentes * DEDUCAO_DEPENDENTE - pensao
        val baseSimpl = bruto - DESCONTO_SIMPLIFICADO
        val tLegal = tabela(baseLegal); val tSimpl = tabela(baseSimpl)
        val simpl = tSimpl < tLegal
        val t = if (simpl) tSimpl else tLegal
        val red = reducao(bruto, t)
        return Ir(max(0, cents(t - red)), max(0, if (simpl) baseSimpl else baseLegal), simpl, cents(red))
    }

    data class Liquido(val bruto: Long, val inss: Long, val irrf: Long, val valeTransporte: Long, val outros: Long, val liquido: Long, val fgts: Long, val ir: Ir)

    /** Salário líquido mensal. vtPct: vale-transporte descontado (máx. 6% do salário-base) — informe 0 se não usa. */
    fun liquido(bruto: Long, dependentes: Int = 0, usaVT: Boolean = false, custoVT: Long? = null, outros: Long = 0, pensao: Long = 0): Liquido {
        val i = inss(bruto)
        val ir = irrf(bruto, i, dependentes, pensao)
        val vtMax = cents(bruto / 100.0 * 0.06)
        val vt = if (!usaVT) 0 else if (custoVT != null && custoVT > 0) min(vtMax, custoVT) else vtMax
        val liq = bruto - i - ir.imposto - vt - outros - pensao
        return Liquido(bruto, i, ir.imposto, vt, outros + pensao, liq, cents(bruto / 100.0 * 0.08), ir)
    }

    data class Ferias(val dias: Int, val valorFerias: Long, val terco: Long, val abono: Long, val tercoAbono: Long, val inss: Long, val irrf: Long, val liquido: Long)

    /**
     * Férias: (salário + médias) × dias/30 + 1/3. Abono (venda de até 10 dias) não tem INSS nem IR.
     * INSS e IR incidem sobre férias + 1/3, em separado do salário do mês.
     */
    fun ferias(salario: Long, medias: Long = 0, dias: Int = 30, diasVendidos: Int = 0, dependentes: Int = 0): Ferias {
        val vend = diasVendidos.coerceIn(0, 10)
        val d = dias.coerceIn(5, 30 - vend)
        val base = salario + medias
        val v = cents(base / 100.0 * d / 30.0)
        val terco = cents(v / 100.0 / 3.0)
        val ab = cents(base / 100.0 * vend / 30.0)
        val tab = cents(ab / 100.0 / 3.0)
        val tributavel = v + terco
        val i = inss(tributavel)
        val ir = irrf(tributavel, i, dependentes).imposto
        return Ferias(d, v, terco, ab, tab, i, ir, tributavel + ab + tab - i - ir)
    }

    data class Decimo(val meses: Int, val bruto: Long, val primeira: Long, val inss: Long, val irrf: Long, val segunda: Long, val liquidoTotal: Long)

    /** 13º proporcional: meses com 15 dias ou mais trabalhados. Tributação exclusiva (com a redução de 2026). */
    fun decimoTerceiro(salario: Long, medias: Long = 0, meses: Int = 12, dependentes: Int = 0): Decimo {
        val m = meses.coerceIn(0, 12)
        val bruto = cents((salario + medias) / 100.0 * m / 12.0)
        val primeira = cents(bruto / 100.0 / 2.0)
        val i = inss(bruto)
        val ir = irrf(bruto, i, dependentes).imposto
        val segunda = bruto - primeira - i - ir
        return Decimo(m, bruto, primeira, i, ir, segunda, primeira + segunda)
    }
}
