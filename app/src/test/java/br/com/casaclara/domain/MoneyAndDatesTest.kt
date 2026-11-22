package br.com.casaclara.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class MoneyAndDatesTest {
    @Test fun formata() {
        assertEquals("R$ 1.234,56", Money.format(123456))
        assertEquals("R$ 0,05", Money.format(5))
        assertEquals("-R$ 10,00", Money.format(-1000))
        assertEquals("R$ 1.000.000,00", Money.format(100000000))
    }

    @Test fun interpreta() {
        assertEquals(123456L, Money.parse("1.234,56"))
        assertEquals(123450L, Money.parse("1234,5"))
        assertEquals(123456L, Money.parse("1234.56"))
        assertEquals(5000L, Money.parse("R$ 50"))
        assertEquals(150000L, Money.parse("1.500"))
        assertEquals(1250L, Money.parse("12.5"))
        assertEquals(123456L, Money.parse("1,234.56"))
        assertNull(Money.parse("abc"))
        assertNull(Money.parse(""))
        assertNull(Money.parse("1,2,3"))
    }

    @Test fun ida_e_volta() {
        assertEquals(98765L, Money.parse(Money.toInput(98765)))
    }

    @Test fun vencimento_mensal() {
        val hoje = LocalDate.of(2026, 2, 20)
        assertEquals(LocalDate.of(2026, 3, 10), Dates.nextMonthlyDue(10, hoje))
        assertEquals(LocalDate.of(2026, 2, 25), Dates.nextMonthlyDue(25, hoje))
        assertEquals(LocalDate.of(2026, 2, 28), Dates.nextMonthlyDue(31, hoje))
    }

    @Test fun datas_brasileiras() {
        assertEquals(LocalDate.of(2026, 3, 5).toEpochDay(), Dates.parseBr("05/03/2026"))
        assertNull(Dates.parseBr("31/02/2026"))
        assertEquals("05/03/2026", Dates.br(LocalDate.of(2026, 3, 5).toEpochDay()))
        assertEquals(202601, Dates.shiftMonth(202512, 1))
        assertEquals("Março de 2026", Dates.monthLabel(202603))
    }
}
