package br.com.casaclara.domain

import java.time.LocalDate
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Calculadoras do Casa Clara. Funções puras, valores em centavos.
 * Todo resultado é ESTIMATIVA baseada nos números que a pessoa informa.
 */
object Calculators {

    private fun pos(v: Long?): Long = if (v != null && v > 0) v else 0L

    // ---------------- custo mensal de morar sozinho ----------------
    data class LivingCost(
        val aluguel: Long? = null, val condominio: Long? = null, val iptu: Long? = null,
        val energia: Long? = null, val agua: Long? = null, val gas: Long? = null, val internet: Long? = null, val celular: Long? = null,
        val mercado: Long? = null, val transporte: Long? = null, val saude: Long? = null, val limpeza: Long? = null,
        val lazer: Long? = null, val outros: Long? = null,
    )
    data class LivingCostResult(val total: Long, val moradia: Long, val grupos: List<Pair<String, Long>>)

    fun livingCost(i: LivingCost): LivingCostResult {
        val moradia = pos(i.aluguel) + pos(i.condominio) + pos(i.iptu)
        val contas = pos(i.energia) + pos(i.agua) + pos(i.gas) + pos(i.internet) + pos(i.celular)
        val diaADia = pos(i.mercado) + pos(i.transporte) + pos(i.saude) + pos(i.limpeza)
        val lazer = pos(i.lazer) + pos(i.outros)
        return LivingCostResult(
            total = moradia + contas + diaADia + lazer,
            moradia = moradia,
            grupos = listOf("Moradia" to moradia, "Contas da casa" to contas, "Dia a dia" to diaADia, "Lazer e outros" to lazer),
        )
    }

    // ---------------- custo inicial da mudança ----------------
    data class InitialCost(
        val aluguel: Long? = null, val mesesCaucao: Int = 0, val primeiroAluguel: Boolean = false,
        val garantia: Long? = null, val frete: Long? = null, val moveis: Long? = null, val eletros: Long? = null,
        val utensilios: Long? = null, val ligacoes: Long? = null, val reparos: Long? = null, val outros: Long? = null,
    )
    data class InitialCostResult(val total: Long, val itens: List<Pair<String, Long>>)

    fun initialCost(i: InitialCost): InitialCostResult {
        val caucao = pos(i.aluguel) * i.mesesCaucao.coerceIn(0, 12)
        val itens = listOf(
            "Caução" to caucao,
            "Primeiro aluguel" to (if (i.primeiroAluguel) pos(i.aluguel) else 0L),
            "Garantia (seguro/título)" to pos(i.garantia),
            "Frete / mudança" to pos(i.frete),
            "Móveis" to pos(i.moveis),
            "Eletrodomésticos" to pos(i.eletros),
            "Utensílios e enxoval" to pos(i.utensilios),
            "Ligação de serviços" to pos(i.ligacoes),
            "Pequenos reparos" to pos(i.reparos),
            "Outros" to pos(i.outros),
        )
        return InitialCostResult(itens.sumOf { it.second }, itens.filter { it.second > 0 })
    }

    // ---------------- renda necessária ----------------
    data class RequiredIncome(val pelaRegra: Long, val paraCobrirTudo: Long, val recomendada: Long, val limite: Double, val folga: Double)

    /** Regra prática: moradia (aluguel + condomínio + IPTU) até [limite] da renda, e custos totais + [folga]. */
    fun requiredIncome(moradia: Long, outrosCustos: Long, limite: Double = 0.30, folga: Double = 0.10): RequiredIncome {
        val l = if (limite > 0) limite else 0.30
        val f = if (folga >= 0) folga else 0.10
        val pelaRegra = (pos(moradia) / l).roundToLong()
        val tudo = ((pos(moradia) + pos(outrosCustos)) * (1 + f)).roundToLong()
        return RequiredIncome(pelaRegra, tudo, max(pelaRegra, tudo), l, f)
    }

    enum class RentFit { CABE, APERTADO, PESADO, SEM_RENDA }
    fun rentFit(renda: Long, moradia: Long): RentFit {
        if (renda <= 0) return RentFit.SEM_RENDA
        val r = pos(moradia).toDouble() / renda
        return when { r <= 0.30 -> RentFit.CABE; r <= 0.40 -> RentFit.APERTADO; else -> RentFit.PESADO }
    }

    // ---------------- metas / economia ----------------
    data class SavingsResult(
        val falta: Long, val meses: Int?, val dataPrevista: LocalDate?, val progresso: Double, val aporteParaPrazo: Long?,
    )

    /**
     * @param rendimentoMensal taxa ao mês em fração (0.008 = 0,8% a.m.). Opcional; não é garantia.
     * @param prazoMeses se informado, calcula quanto guardar por mês para chegar no prazo.
     */
    fun savings(meta: Long, guardado: Long, aporteMensal: Long, rendimentoMensal: Double = 0.0, prazoMeses: Int? = null, inicio: LocalDate = LocalDate.now()): SavingsResult {
        val m = pos(meta); val g = pos(guardado); val a = pos(aporteMensal)
        val r = if (rendimentoMensal > 0) rendimentoMensal else 0.0
        val falta = max(0L, m - g)
        val progresso = if (m > 0) min(1.0, g.toDouble() / m) else 0.0
        var meses: Int? = null
        if (falta == 0L) meses = 0
        else if (a > 0 || (r > 0 && g > 0)) {
            var saldo = g.toDouble(); var n = 0
            while (saldo < m && n < 1200) { saldo = saldo * (1 + r) + a; n++ }
            meses = if (saldo >= m) n else null
        }
        var aporte: Long? = null
        if (prazoMeses != null && prazoMeses > 0) {
            aporte = if (r > 0) {
                val fv = g * (1 + r).pow(prazoMeses)
                val fator = ((1 + r).pow(prazoMeses) - 1) / r
                max(0.0, (m - fv) / fator).roundToLong()
            } else ceil(falta.toDouble() / prazoMeses).toLong()
        }
        return SavingsResult(falta, meses, meses?.let { inicio.plusMonths(it.toLong()) }, progresso, aporte)
    }

    // ---------------- divisão de despesas ----------------
    /** Divide [total] em partes iguais ou por pesos (ex.: tamanho do quarto). Garante que a soma bate com o total. */
    fun split(total: Long, pesos: List<Double>): List<Long> {
        if (pesos.isEmpty()) return emptyList()
        val ps = pesos.map { if (it > 0) it else 0.0 }
        val soma = ps.sum()
        if (soma <= 0) return split(total, List(pesos.size) { 1.0 })
        val partes = ps.map { (total * it / soma).toLong() }.toMutableList()
        var resto = total - partes.sum()
        var i = 0
        while (resto > 0) { if (ps[i % ps.size] > 0) { partes[i % ps.size]++; resto-- }; i++ }
        return partes
    }

    // ---------------- sozinho x dividir x continuar juntando ----------------
    data class Scenario(val nome: String, val custoMensal: Long, val custoInicial: Long, val sobra: Long, val mesesParaInicial: Int?)

    fun compareScenarios(renda: Long, custoSozinho: Long, compartilhavel: Long, pessoas: Int, custoInicialSozinho: Long, custoInicialDividir: Long, custoAtual: Long): List<Scenario> {
        val p = max(1, pessoas)
        val comp = min(pos(compartilhavel), pos(custoSozinho))
        val dividir = (comp.toDouble() / p).roundToLong() + (pos(custoSozinho) - comp)
        fun mk(nome: String, custo: Long, inicial: Long): Scenario {
            val sobra = renda - custo
            return Scenario(nome, custo, pos(inicial), sobra, if (sobra > 0 && inicial > 0) ceil(inicial.toDouble() / sobra).toInt() else if (inicial <= 0) 0 else null)
        }
        return listOf(
            mk("Morar sozinho", pos(custoSozinho), custoInicialSozinho),
            mk("Dividir casa", dividir, custoInicialDividir),
            mk("Continuar juntando", pos(custoAtual), 0),
        )
    }

    // ---------------- imóveis ----------------
    data class PropertyInput(val id: Long, val aluguel: Long, val condominio: Long?, val iptu: Long?, val area: Double?, val transporteMensal: Long? = null)
    data class PropertyScore(val id: Long, val totalMensal: Long, val porM2: Long?, val comTransporte: Long)

    fun compareProperties(list: List<PropertyInput>): List<PropertyScore> = list.map {
        val total = pos(it.aluguel) + pos(it.condominio) + pos(it.iptu)
        PropertyScore(it.id, total, it.area?.takeIf { a -> a > 0 }?.let { a -> (total / a).roundToLong() }, total + pos(it.transporteMensal))
    }.sortedBy { it.comTransporte }

    // ---------------- móveis e eletros ----------------
    data class FurnitureTotals(val estimadoPendente: Long, val pago: Long, val parcelasMes: Long, val pendenteAgora: Long)
    data class FurnitureLine(val estimado: Long?, val pago: Long?, val comprado: Boolean, val prioridadeAgora: Boolean, val parcelas: Int?, val valorParcela: Long?)

    fun furnitureTotals(items: List<FurnitureLine>) = FurnitureTotals(
        estimadoPendente = items.filter { !it.comprado }.sumOf { pos(it.estimado) },
        pago = items.filter { it.comprado }.sumOf { pos(it.pago ?: it.estimado) },
        parcelasMes = items.filter { it.comprado && (it.parcelas ?: 1) > 1 }.sumOf { pos(it.valorParcela) },
        pendenteAgora = items.filter { !it.comprado && it.prioridadeAgora }.sumOf { pos(it.estimado) },
    )

    // ---------------- transporte ----------------
    data class TransportResult(val publico: Long, val carro: Long, val app: Long, val total: Long)

    /**
     * @param passagem valor de UMA passagem; [viagensPorDia] e [diasPorMes] definem o uso.
     * @param kmPorMes, [consumoKmPorLitro] e [precoLitro] estimam combustível; [fixosCarro] = seguro, IPVA/12, manutenção etc.
     */
    fun transport(
        passagem: Long = 0, viagensPorDia: Int = 0, diasPorMes: Int = 0,
        kmPorMes: Double = 0.0, consumoKmPorLitro: Double = 0.0, precoLitro: Long = 0, estacionamento: Long = 0, fixosCarro: Long = 0,
        corridasApp: Int = 0, valorMedioCorrida: Long = 0,
    ): TransportResult {
        val publico = pos(passagem) * max(0, viagensPorDia) * max(0, diasPorMes)
        val combustivel = if (consumoKmPorLitro > 0 && kmPorMes > 0) (kmPorMes / consumoKmPorLitro * pos(precoLitro)).roundToLong() else 0L
        val carro = combustivel + pos(estacionamento) + pos(fixosCarro)
        val app = max(0, corridasApp) * pos(valorMedioCorrida)
        return TransportResult(publico, carro, app, publico + carro + app)
    }
}
