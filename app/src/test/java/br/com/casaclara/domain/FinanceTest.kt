package br.com.casaclara.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class FinanceTest {
    private fun d(y: Int, m: Int, day: Int) = LocalDate.of(y, m, day).toEpochDay()

    @Test fun resumo_e_categorias() {
        val e = listOf(Finance.Movement(300000, d(2026, 3, 5)), Finance.Movement(50000, d(2026, 4, 1)))
        val s = listOf(Finance.Movement(120000, d(2026, 3, 10), "Moradia"), Finance.Movement(30000, d(2026, 3, 31), "Mercado"), Finance.Movement(10000, d(2026, 2, 1), "Mercado"))
        val r = Finance.summary(e, s, 202603)
        assertEquals(300000L, r.entrou); assertEquals(150000L, r.saiu); assertEquals(150000L, r.sobrou)
        val cat = Finance.byCategory(s, d(2026, 3, 1), d(2026, 3, 31))
        assertEquals("Moradia" to 120000L, cat[0])
        val cmp = Finance.compareMonths(s, 202603)
        assertEquals(140000L, cmp.diferenca)
        assertEquals(6, Finance.lastMonths(e, s, 202603).size)
    }

    @Test fun orcamento_e_dividas() {
        assertEquals(Finance.BudgetStatus.ATENCAO, Finance.budget(50000, 45000).status)
        assertEquals(Finance.BudgetStatus.ESTOUROU, Finance.budget(50000, 60000).status)
        assertEquals(40000L, Finance.budget(50000, 10000).restante)
        assertEquals(Finance.DebtRemaining(6, 90000), Finance.debtRemaining(15000, 10, 4))
    }

    @Test fun juros() {
        val c = Finance.installmentCost(100000, 12, 0.03)
        assertEquals(10046L, c.parcela)
        assertEquals(c.parcela * 12 - 100000, c.jurosPagos)
        assertEquals(0L, Finance.installmentCost(120000, 12, 0.0).jurosPagos)
        val taxa = Finance.impliedMonthlyRate(180000, 18000, 12)!!
        assertTrue(taxa > 0.025 && taxa < 0.03)
        assertEquals(0.0, Finance.impliedMonthlyRate(120000, 10000, 12)!!, 0.0)
        assertEquals(0.4258, Finance.annualFromMonthly(0.03), 0.001)
    }

    @Test fun casa_compartilhada() {
        val bal = Shared.balances(listOf(1, 2, 3), listOf(Shared.Expense(30000, 1, listOf(1, 2, 3)), Shared.Expense(6000, 2, listOf(2, 3))), emptyList())
        assertEquals(mapOf(1L to 20000L, 2L to -7000L, 3L to -13000L), bal)
        val t = Shared.settle(bal)
        assertEquals(20000L, t.sumOf { it.valor })
        assertTrue(t.all { it.para == 1L })
        val zerado = Shared.balances(listOf(1, 2), listOf(Shared.Expense(10000, 1, listOf(1, 2))), listOf(Shared.Transfer(2, 1, 5000)))
        assertEquals(mapOf(1L to 0L, 2L to 0L), zerado)
        assertTrue(Shared.settle(zerado).isEmpty())
        // valor que não divide exato: a soma das partes bate com o total
        val impar = Shared.balances(listOf(1, 2, 3), listOf(Shared.Expense(1000, 1, listOf(1, 2, 3))), emptyList())
        assertEquals(0L, impar.values.sum())
    }

    @Test fun csv() {
        val s = Csv.build(listOf("a", "b", "c"), listOf(listOf("x;y", "1,50", "=SOMA(1)")))
        assertEquals("a;b;c\r\n\"x;y\";1,50;'=SOMA(1)", s)
        assertEquals("-12,00", Csv.build(listOf("v"), listOf(listOf(Csv.money(-1200)))).lines().last())
    }
}
