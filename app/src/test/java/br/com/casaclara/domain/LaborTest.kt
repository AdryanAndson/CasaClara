package br.com.casaclara.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LaborTest {
    @Test fun inss_2026() {
        assertEquals(12158L, Labor.inss(162100))   // salário mínimo: 7,5%
        assertEquals(24859L, Labor.inss(300000))   // R$ 3.000 → R$ 248,59
        assertEquals(98808L, Labor.inss(847555))   // teto
        assertEquals(98808L, Labor.inss(2000000))  // acima do teto, mesmo desconto
        assertEquals(0L, Labor.inss(0))
    }

    @Test fun irrf_isencao_ate_5000() {
        val b = 500000L
        assertEquals(0L, Labor.irrf(b, Labor.inss(b)).imposto)
        assertEquals(0L, Labor.irrf(300000, Labor.inss(300000)).imposto)
    }

    @Test fun irrf_faixa_de_transicao() {
        // R$ 6.000: INSS 641,50 → base 5.358,50 → 564,8575; redução 978,62 − 0,133145×6.000 = 179,75 → 385,11
        val r = Labor.irrf(600000, Labor.inss(600000))
        assertEquals(64150L, Labor.inss(600000))
        assertEquals(38511L, r.imposto)
        assertEquals(17975L, r.reducao)
    }

    @Test fun irrf_sem_reducao_acima_7350() {
        val r = Labor.irrf(1000000, Labor.inss(1000000))
        assertEquals(156955L, r.imposto)
        assertEquals(0L, r.reducao)
    }

    @Test fun liquido_e_vt() {
        val l = Labor.liquido(250000, usaVT = true)
        assertEquals(15000L, l.valeTransporte)
        assertEquals(250000L - l.inss - 15000L, l.liquido)
        assertEquals(20000L, l.fgts)
        assertEquals(4000L, Labor.liquido(250000, usaVT = true, custoVT = 4000).valeTransporte)
    }

    @Test fun ferias_e_decimo() {
        val f = Labor.ferias(240000)
        assertEquals(240000L, f.valorFerias); assertEquals(80000L, f.terco)
        val v = Labor.ferias(300000, diasVendidos = 10)
        assertEquals(20, v.dias); assertEquals(100000L, v.abono)
        val d = Labor.decimoTerceiro(300000, meses = 6)
        assertEquals(150000L, d.bruto); assertEquals(75000L, d.primeira)
        assertEquals(d.bruto - d.inss - d.irrf, d.liquidoTotal)
        assertTrue(Labor.decimoTerceiro(300000).irrf == 0L)
    }
}

class HelpersTest {
    @Test fun leituras() {
        val s = Readings.stats(listOf(Readings.Point(0, 1000.0), Readings.Point(30, 1150.0), Readings.Point(60, 1330.0)))
        assertEquals(2, s.periods.size)
        assertEquals(165.0, s.monthEstimate!!, 0.01)
        assertEquals(20.0, s.trendPct!!, 0.01)
        assertTrue(Readings.stats(listOf(Readings.Point(0, 10.0), Readings.Point(5, 8.0))).error != null)
        assertEquals(12000L, Readings.cost(150.0, 80))
    }

    @Test fun golpe() {
        assertEquals(ScamCheck.Level.BAIXO, ScamCheck.level(emptySet()))
        assertEquals(ScamCheck.Level.ALTO, ScamCheck.level(setOf("codigo")))
        assertEquals(ScamCheck.Level.ATENCAO, ScamCheck.level(setOf("pressa")))
    }

    @Test fun quiz_aponta_para_guias() {
        assertEquals(Quiz.ALL.size, Quiz.ALL.map { it.id }.toSet().size)
        Quiz.ALL.forEach {
            assertTrue("${it.id}: guia ${it.guide}", br.com.casaclara.domain.content.Library.BY_ID.containsKey(it.guide))
            assertTrue(it.correct in it.options.indices)
        }
        assertEquals(10, Quiz.draw(10, 1).size)
    }
}
