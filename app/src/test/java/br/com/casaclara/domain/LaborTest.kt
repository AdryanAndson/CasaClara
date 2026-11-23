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

class MoneyPlusTest {
    @Test fun previsao() {
        // dia 10 de 30: entrou 3000, saiu 1000 (500 variável) → 50/dia × 20 = 1000; + 200 a receber − 300 fixos − 100 contas
        val r = Forecast.endOfMonth(300000, 100000, 50000, 20000, 30000, 10000, 10, 30)
        assertEquals(5000L, r.gastoVariavelPorDia)
        assertEquals(100000L, r.variavelRestante)
        assertEquals(200000L + 20000 - 30000 - 10000 - 100000, r.previsto)
    }

    @Test fun dividas() {
        val ds = listOf(DebtPlan.Debt("Cartão", 100000, 10.0, 10000), DebtPlan.Debt("Loja", 30000, 2.0, 5000))
        val neve = DebtPlan.simulate(ds, 20000, DebtPlan.Method.BOLA_DE_NEVE)
        val ava = DebtPlan.simulate(ds, 20000, DebtPlan.Method.AVALANCHE)
        assertTrue(neve.feasible && ava.feasible)
        assertEquals("Loja", neve.payoff.first().first)
        assertEquals("Cartão", ava.payoff.first().first)
        assertTrue(ava.totalInterest <= neve.totalInterest)
        // parcela menor que os juros e sem extra: não quita
        assertTrue(!DebtPlan.simulate(listOf(DebtPlan.Debt("X", 100000, 10.0, 5000)), 0, DebtPlan.Method.AVALANCHE).feasible)
    }

    @Test fun desafio52() {
        assertEquals(137800L, Challenge52.total(100))
        assertEquals(300L, Challenge52.saved(100, setOf(1, 2)))
    }
}

class MessageTemplatesTest {
    @Test fun modelos_consistentes() {
        val t = br.com.casaclara.domain.content.MessageTemplates
        assertEquals(t.ALL.size, t.ALL.map { it.id }.toSet().size)
        assertTrue(t.ALL.size >= 30)
        val cats = t.CATS.map { it.id }.toSet()
        t.ALL.forEach { m ->
            assertTrue("${m.id}: categoria", m.cat in cats)
            m.fields.forEach { f -> assertTrue("${m.id}: campo $f sem rótulo", t.LABELS.containsKey(f)) }
            m.guide?.let { assertTrue("${m.id}: guia $it", br.com.casaclara.domain.content.Library.BY_ID.containsKey(it)) }
        }
        val m = t.BY_ID.getValue("cheguei")
        assertEquals(m.body, m.fill(emptyMap()))
        val p = t.BY_ID.getValue("pedir-recibo").fill(mapOf("nome" to "Ana"))
        assertTrue(p.contains("Ana") && p.contains("[Para quem (nome)]"))
    }
}
