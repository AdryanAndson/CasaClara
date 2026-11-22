package br.com.casaclara.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CalculatorsTest {
    @Test fun custo_mensal() {
        val r = Calculators.livingCost(Calculators.LivingCost(aluguel = 100000, condominio = 20000, energia = 10000, mercado = 50000, lazer = 5000))
        assertEquals(185000L, r.total)
        assertEquals(120000L, r.moradia)
        assertEquals(0L, Calculators.livingCost(Calculators.LivingCost(aluguel = -100)).total)
    }

    @Test fun custo_inicial() {
        val r = Calculators.initialCost(Calculators.InitialCost(aluguel = 100000, mesesCaucao = 3, primeiroAluguel = true, frete = 40000))
        assertEquals(440000L, r.total)
        assertEquals(300000L, r.itens.first { it.first == "Caução" }.second)
    }

    @Test fun renda_necessaria() {
        val r = Calculators.requiredIncome(90000, 150000)
        assertEquals(300000L, r.pelaRegra)
        assertEquals(264000L, r.paraCobrirTudo)
        assertEquals(300000L, r.recomendada)
        assertEquals(Calculators.RentFit.CABE, Calculators.rentFit(300000, 90000))
        assertEquals(Calculators.RentFit.APERTADO, Calculators.rentFit(300000, 110000))
        assertEquals(Calculators.RentFit.PESADO, Calculators.rentFit(300000, 150000))
        assertEquals(Calculators.RentFit.SEM_RENDA, Calculators.rentFit(0, 90000))
    }

    @Test fun economia() {
        val inicio = LocalDate.of(2026, 1, 15)
        val r = Calculators.savings(600000, 100000, 50000, inicio = inicio)
        assertEquals(500000L, r.falta)
        assertEquals(10, r.meses)
        assertEquals(LocalDate.of(2026, 11, 15), r.dataPrevista)
        assertNull(Calculators.savings(10000, 0, 0).meses)
        assertEquals(0, Calculators.savings(10000, 15000, 0).meses)
        assertEquals(10000L, Calculators.savings(120000, 0, 0, prazoMeses = 12).aporteParaPrazo)
        val comJuros = Calculators.savings(120000, 0, 0, rendimentoMensal = 0.01, prazoMeses = 12).aporteParaPrazo!!
        assertTrue(comJuros in 9000L..9999L)
        val a = Calculators.savings(1000000, 0, 50000).meses!!
        val b = Calculators.savings(1000000, 0, 50000, rendimentoMensal = 0.01).meses!!
        assertTrue(b < a)
    }

    @Test fun divisao() {
        assertEquals(listOf(334L, 333L, 333L), Calculators.split(1000, listOf(1.0, 1.0, 1.0)))
        val p = Calculators.split(180000, listOf(1.2, 1.0))
        assertEquals(180000L, p.sum())
        assertEquals(98182L, p[0])
        assertEquals(listOf(500L, 500L), Calculators.split(1000, listOf(0.0, 0.0)))
    }

    @Test fun cenarios() {
        val s = Calculators.compareScenarios(300000, 200000, 120000, 2, 500000, 200000, 30000)
        assertEquals(100000L, s[0].sobra)
        assertEquals(5, s[0].mesesParaInicial)
        assertEquals(140000L, s[1].custoMensal)
        assertEquals(270000L, s[2].sobra)
    }

    @Test fun imoveis() {
        val r = Calculators.compareProperties(listOf(
            Calculators.PropertyInput(1, 110000, 25000, 4000, 30.0, transporteMensal = 0),
            Calculators.PropertyInput(2, 90000, 30000, 6000, 42.0, transporteMensal = 30000),
        ))
        assertEquals(1L, r[0].id)
        assertEquals(139000L, r[0].totalMensal)
        assertEquals(4633L, r[0].porM2)
    }

    @Test fun transporte() {
        val r = Calculators.transport(passagem = 500, viagensPorDia = 2, diasPorMes = 22, kmPorMes = 300.0, consumoKmPorLitro = 10.0, precoLitro = 600, corridasApp = 4, valorMedioCorrida = 2000)
        assertEquals(22000L, r.publico)
        assertEquals(18000L, r.carro)
        assertEquals(8000L, r.app)
        assertEquals(48000L, r.total)
    }

    @Test fun moveis() {
        val t = Calculators.furnitureTotals(listOf(
            Calculators.FurnitureLine(90000, 85000, true, true, 5, 17000),
            Calculators.FurnitureLine(150000, null, false, false, null, null),
            Calculators.FurnitureLine(40000, null, false, true, null, null),
        ))
        assertEquals(190000L, t.estimadoPendente)
        assertEquals(85000L, t.pago)
        assertEquals(17000L, t.parcelasMes)
        assertEquals(40000L, t.pendenteAgora)
    }
}
