package br.com.casaclara.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class NewFeaturesTest {
    private fun d(y: Int, m: Int, day: Int) = LocalDate.of(y, m, day)

    @Test fun fatura_do_cartao() {
        // fecha dia 3, vence dia 10
        assertEquals(202603, Cards.invoiceKey(d(2026, 3, 2), 3, 10))
        assertEquals(202604, Cards.invoiceKey(d(2026, 3, 4), 3, 10))
        // fecha dia 25, vence dia 5 do mês seguinte
        assertEquals(202604, Cards.invoiceKey(d(2026, 3, 20), 25, 5))
        assertEquals(202605, Cards.invoiceKey(d(2026, 3, 26), 25, 5))
        // fechamento dia 31 em fevereiro
        assertEquals(d(2026, 2, 28), Cards.closingFor(d(2026, 2, 15), 31))
    }

    @Test fun parcelas_nas_faturas() {
        val inv = Cards.invoices(listOf(Cards.Purchase(30000, d(2026, 3, 1), 3), Cards.Purchase(1000, d(2026, 3, 2))), 3, 10)
        assertEquals(mapOf(202603 to 11000L, 202604 to 10000L, 202605 to 10000L), inv.toMap())
        assertEquals(20000L, Cards.usedLimit(inv, setOf(202603)))
        val impar = Cards.invoices(listOf(Cards.Purchase(1000, d(2026, 3, 1), 3)), 3, 10)
        assertEquals(1000L, impar.values.sum())
    }

    @Test fun recorrentes() {
        val hoje = d(2026, 5, 10)
        assertEquals(listOf(202603, 202604, 202605), Recurring.monthsToGenerate(202603, null, 5, hoje))
        assertEquals(listOf(202603, 202604), Recurring.monthsToGenerate(202603, null, 15, hoje))
        assertEquals(listOf(202605), Recurring.monthsToGenerate(202603, 202604, 5, hoje))
        assertTrue(Recurring.monthsToGenerate(202603, 202605, 5, hoje).isEmpty())
        assertEquals(12, Recurring.monthsToGenerate(202001, null, 1, hoje).size)
    }

    @Test fun extrato_csv_brasileiro() {
        val csv = """
            Data;Histórico;Valor
            01/03/2026;SUPERMERCADO BOM PRECO;-152,30
            02/03/2026;PIX RECEBIDO SALARIO;3.000,00
            03/03/2026;UBER *TRIP;-23,90
            Saldo do dia;;1.000,00
        """.trimIndent()
        val r = StatementImport.parse(csv)
        assertNull(r.error)
        assertEquals(3, r.rows.size)
        assertEquals(-15230L, r.rows[0].amount)
        assertEquals(300000L, r.rows[1].amount)
        assertEquals("Mercado", StatementImport.guessCategory(r.rows[0].description, false))
        assertEquals("Transporte", StatementImport.guessCategory(r.rows[2].description, false))
        assertEquals("Salário", StatementImport.guessCategory(r.rows[1].description, true))
    }

    @Test fun extrato_csv_iso_virgula() {
        val csv = "date,description,amount\n2026-03-01,\"Netflix, mensal\",-39.90\n2026-03-05,Farmácia Popular,-12.00\n"
        val r = StatementImport.parse(csv)
        assertEquals(2, r.rows.size)
        assertEquals("Netflix, mensal", r.rows[0].description)
        assertEquals(-3990L, r.rows[0].amount)
        assertEquals("Assinaturas", StatementImport.guessCategory(r.rows[0].description, false))
        assertEquals("Saúde", StatementImport.guessCategory(r.rows[1].description, false))
        assertTrue(StatementImport.parse("bla bla\nnada aqui").error != null)
        assertEquals(-5000L, StatementImport.parseAmount("50,00 D"))
        assertEquals(-5000L, StatementImport.parseAmount("(50,00)"))
    }

    @Test fun plano_semanal() {
        val seg = d(2026, 3, 2)
        val p = WeekPlan.plan(listOf(WeekPlan.Task(1, 1, null), WeekPlan.Task(2, 7, seg.minusDays(7)), WeekPlan.Task(3, 7, seg.minusDays(7)), WeekPlan.Task(4, 7, seg.minusDays(7))), seg)
        assertEquals(7, (0..6).count { 1L in p[it]!! })
        // três semanais vencendo segunda não ficam todas no mesmo dia
        assertTrue((0..6).all { day -> p[day]!!.count { it != 1L } <= 1 })
        assertEquals(3, p.values.flatten().count { it != 1L })
        // começando na quarta, nada é colocado antes de hoje
        val q = WeekPlan.plan(listOf(WeekPlan.Task(1, 1, null)), seg, seg.plusDays(2))
        assertTrue(q[0]!!.isEmpty() && q[2]!!.isNotEmpty())
    }

    @Test fun resumo_semanal() {
        val hoje = d(2026, 3, 5) // quinta
        val s = Weekly.summary(
            listOf(Finance.Movement(1000, d(2026, 3, 2).toEpochDay()), Finance.Movement(500, d(2026, 2, 27).toEpochDay())),
            listOf(Finance.Movement(9000, d(2026, 3, 3).toEpochDay())), hoje,
        )
        assertEquals(1000L, s.spent); assertEquals(500L, s.spentLastWeek); assertEquals(9000L, s.earned); assertEquals(500L, s.diff)
    }
}
