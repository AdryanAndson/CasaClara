package br.com.casaclara.ui.screens

import androidx.compose.material3.FilterChip

import androidx.compose.foundation.layout.FlowRow

import androidx.compose.material3.OutlinedTextField

import androidx.compose.material3.Switch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.casaclara.data.AppContainer
import br.com.casaclara.data.GoalEntity
import br.com.casaclara.domain.Calculators
import br.com.casaclara.domain.Labor
import br.com.casaclara.domain.Dates
import br.com.casaclara.domain.Money
import br.com.casaclara.domain.Routes
import br.com.casaclara.ui.asState
import br.com.casaclara.ui.components.AlertBox
import br.com.casaclara.ui.components.Bars
import br.com.casaclara.ui.components.BrandCard
import br.com.casaclara.ui.components.EmojiBadge
import br.com.casaclara.ui.components.EmptyState
import br.com.casaclara.ui.components.LocalFeedback
import br.com.casaclara.ui.components.MascotBubble
import br.com.casaclara.ui.components.MoneyInput
import br.com.casaclara.ui.components.Mood
import br.com.casaclara.ui.components.NumberInput
import br.com.casaclara.ui.components.Page
import br.com.casaclara.ui.components.ProgressLine
import br.com.casaclara.ui.components.ScreenHeader
import br.com.casaclara.ui.components.SecondaryButton
import br.com.casaclara.ui.components.SectionTitle
import br.com.casaclara.ui.components.Stat
import br.com.casaclara.ui.components.Tone
import br.com.casaclara.ui.theme.Brand
import kotlinx.coroutines.launch

private data class CalcInfo(val id: String, val emoji: String, val title: String, val desc: String, val route: String = Routes.calc(id))

private val CALCS = listOf(
    CalcInfo("custo", "🧮", "Custo mensal de morar sozinho", "Aluguel, contas, mercado, transporte e o resto."),
    CalcInfo("inicial", "📦", "Custo inicial da mudança", "Caução, frete, móveis e o básico da casa."),
    CalcInfo("renda", "💵", "Quanto preciso ganhar para o aluguel", "Regra dos 30% e folga para imprevistos."),
    CalcInfo("economia", "🐷", "Quanto economizar por mês", "Para chegar à meta no prazo que você quer."),
    CalcInfo("tempo", "⏳", "Quanto tempo falta para a meta", "Com o que você já guarda hoje."),
    CalcInfo("divisao", "➗", "Divisão de despesas entre moradores", "Partes iguais ou proporcionais."),
    CalcInfo("cenarios", "⚖️", "Sozinho × dividir × continuar juntando", "Os três caminhos lado a lado."),
    CalcInfo("imoveis", "🔎", "Comparação de imóveis", "Custo total, por m² e com transporte.", Routes.moving(Routes.MovingTab.IMOVEIS)),
    CalcInfo("moveis", "🛋️", "Custo de móveis e eletrodomésticos", "Orçamento por item e prioridades.", Routes.furniture(0)),
    CalcInfo("transporte", "🚌", "Custo mensal de transporte", "Ônibus, carro ou aplicativo."),
    CalcInfo("parcelamento", "💳", "Quanto custa parcelar", "Juros e total pago."),
    CalcInfo("salario", "🧾", "Salário líquido (CLT ${Labor.YEAR})", "INSS, Imposto de Renda e vale-transporte."),
    CalcInfo("ferias", "🏖️", "Férias", "Valor das férias com 1/3 e venda de dias."),
    CalcInfo("decimo", "🎁", "13º salário", "Primeira e segunda parcela, com descontos."),
    CalcInfo("quitar", "⛓️", "Plano para quitar dívidas", "Bola de neve × avalanche: ordem, prazo e juros."),
    CalcInfo("desafio52", "🐷", "Desafio das 52 semanas", "Guarde um pouco mais a cada semana."),
)

@Composable
fun CalculatorsScreen(nav: NavController) {
    Page(nav, "Calculadoras") {
        ScreenHeader("Calculadoras", "Todos os resultados são estimativas com os números que você informar.", mood = Mood.PENSANDO)
        CALCS.forEach { c ->
            BrandCard(onClick = { nav.navigate(c.route) }, padding = 12.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EmojiBadge(c.emoji, 44.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text(c.title, style = MaterialTheme.typography.titleMedium); Text(c.desc, style = MaterialTheme.typography.bodySmall) }
                    Text("›", style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
    }
}

/** Estado de uma calculadora: carregado das preferências e salvo a cada alteração (persiste ao fechar o app). */
@Composable
private fun rememberCalc(c: AppContainer, id: String): Pair<SnapshotStateMap<String, String>, (String, String) -> Unit>? {
    val values = remember(id) { mutableStateMapOf<String, String>() }
    var loaded by remember(id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(id) { values.putAll(c.prefs.current().calc[id].orEmpty()); loaded = true }
    if (!loaded) return null
    val setter: (String, String) -> Unit = { k, v ->
        values[k] = v
        scope.launch { c.prefs.setCalc(id, values.toMap()) }
    }
    return values to setter
}

@Composable
fun CalculatorScreen(c: AppContainer, nav: NavController, id: String) {
    val info = CALCS.firstOrNull { it.id == id }
    Page(nav, info?.title ?: "Calculadora") {
        when (id) {
            "custo" -> CustoCalc(c, nav)
            "inicial" -> InicialCalc(c)
            "renda" -> RendaCalc(c)
            "economia", "tempo" -> EconomiaCalc(c, id == "tempo")
            "divisao" -> DivisaoCalc(c)
            "cenarios" -> CenariosCalc(c)
            "transporte" -> TransporteCalc(c)
            "parcelamento" -> InstallmentCalculator()
            "salario" -> SalarioCalc(c)
            "ferias" -> FeriasCalc(c)
            "decimo" -> DecimoCalc(c)
            "quitar" -> DebtPlanCalc(c)
            "desafio52" -> Challenge52Calc(c)
            else -> EmptyState("Calculadora não encontrada", "Volte e escolha outra.", Mood.CONFUSA)
        }
        Text("Estimativa baseada nos números informados. Não é garantia de resultado.", style = MaterialTheme.typography.bodySmall)
    }
}

private fun m(v: Map<String, String>, k: String) = Money.parse(v[k])

@Composable
private fun ResultCard(content: @Composable () -> Unit) {
    BrandCard(tone = Tone.AMARELO, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) { content() }
}

private val CUSTO_FIELDS = listOf(
    "aluguel" to "Aluguel", "condominio" to "Condomínio", "iptu" to "IPTU (por mês)", "energia" to "Energia", "agua" to "Água", "gas" to "Gás",
    "internet" to "Internet", "celular" to "Celular", "mercado" to "Mercado", "transporte" to "Transporte", "saude" to "Saúde e farmácia",
    "limpeza" to "Limpeza e higiene", "lazer" to "Lazer", "outros" to "Outros",
)

@Composable
private fun CustoCalc(c: AppContainer, nav: NavController) {
    val (v, set) = rememberCalc(c, "custo") ?: return
    val r = Calculators.livingCost(Calculators.LivingCost(m(v, "aluguel"), m(v, "condominio"), m(v, "iptu"), m(v, "energia"), m(v, "agua"), m(v, "gas"), m(v, "internet"), m(v, "celular"), m(v, "mercado"), m(v, "transporte"), m(v, "saude"), m(v, "limpeza"), m(v, "lazer"), m(v, "outros")))
    Text("Preencha com valores que você pesquisou ou estima. Deixe em branco o que não se aplica — o total atualiza sozinho. IPTU: divida o valor anual por 12.", style = MaterialTheme.typography.bodyMedium)
    ResultCard {
        Stat("Custo mensal estimado", Money.format(r.total))
        Text("Só a moradia: ${Money.format(r.moradia)}", fontWeight = FontWeight.Bold)
        if (r.total > 0) Bars(r.grupos.filter { it.second > 0 }, Money::format)
    }
    CUSTO_FIELDS.forEach { (k, l) -> MoneyInput(l, v[k] ?: "", { set(k, it) }) }
    val prefs by c.prefs.flow.asState<br.com.casaclara.data.UserPrefs?>(null)
    val income = prefs?.income ?: 0
    if (income > 0 && r.total > 0) {
        val sobra = income - r.total
        Stat(if (sobra >= 0) "Sobraria por mês" else "Faltaria por mês", Money.format(kotlin.math.abs(sobra)), color = if (sobra >= 0) Brand.VerdeEscuro else MaterialTheme.colorScheme.error)
        MascotBubble(
            when { sobra >= income / 10 -> "Cabe no bolso e ainda sobra para guardar. Bora pro próximo passo!"; sobra >= 0 -> "Cabe, mas fica apertado. Uma folga para imprevistos ajuda muito."; else -> "Por enquanto não fecha — e tudo bem! Dá para ajustar: dividir casa, mudar de bairro ou juntar por mais tempo." },
            if (sobra >= 0) Mood.COMEMORANDO else Mood.DICA, 64.dp,
        )
    }
    SecondaryButton("Próximo: custo da mudança →", Modifier.fillMaxWidth()) { nav.navigate(Routes.calc("inicial")) }
}


private val INICIAL_FIELDS = listOf(
    "garantia" to "Seguro-fiança ou título (se for sua garantia)", "frete" to "Frete / carreto", "moveis" to "Móveis", "eletros" to "Eletrodomésticos",
    "utensilios" to "Utensílios e enxoval", "ligacoes" to "Ligação de luz, água e internet", "reparos" to "Pequenos reparos e pintura", "outros" to "Outros",
)

@Composable
private fun InicialCalc(c: AppContainer) {
    val (v, set) = rememberCalc(c, "inicial") ?: return
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    val r = Calculators.initialCost(Calculators.InitialCost(m(v, "aluguel"), (v["caucao"] ?: "0").toIntOrNull() ?: 0, v["primeiro"] == "1", m(v, "garantia"), m(v, "frete"), m(v, "moveis"), m(v, "eletros"), m(v, "utensilios"), m(v, "ligacoes"), m(v, "reparos"), m(v, "outros")))
    Text("O dinheiro que sai antes do primeiro mês. Caução é o depósito de garantia que volta no fim do contrato (até 3 aluguéis).", style = MaterialTheme.typography.bodyMedium)
    ResultCard {
        Stat("Você vai precisar de", Money.format(r.total))
        if (r.total > 0) Bars(r.itens, Money::format)
        if (r.total > 0) Button(onClick = {
            scope.launch { c.db.goals().upsert(GoalEntity(name = "Custo inicial da mudança", type = "MUDANCA", target = r.total)) }
            fb.celebrate("Meta criada! Veja em Finanças → Metas.")
        }) { Text("🎯 Transformar em meta") }
    }
    MoneyInput("Valor do aluguel", v["aluguel"] ?: "", { set("aluguel", it) })
    NumberInput("Caução (nº de aluguéis)", v["caucao"] ?: "", { set("caucao", it) }, help = "Use 0 se a garantia for fiador ou seguro. Limite legal: 3.")
    Row(Modifier.fillMaxWidth().toggleable(v["primeiro"] == "1", role = Role.Checkbox) { set("primeiro", if (it) "1" else "") }, verticalAlignment = Alignment.CenterVertically) {
        Checkbox(v["primeiro"] == "1", onCheckedChange = null); Text("Vou pagar o primeiro aluguel na entrada")
    }
    INICIAL_FIELDS.forEach { (k, l) -> MoneyInput(l, v[k] ?: "", { set(k, it) }) }
    MascotBubble("Calma, você não precisa comprar a casa inteira no primeiro dia. Comece com o essencial.", Mood.DICA, 64.dp)
}

@Composable
private fun RendaCalc(c: AppContainer) {
    val (v, set) = rememberCalc(c, "renda") ?: return
    val moradia = (m(v, "aluguel") ?: 0) + (m(v, "condominio") ?: 0) + (m(v, "iptu") ?: 0)
    val limite = (v["limite"]?.replace(',', '.')?.toDoubleOrNull() ?: 30.0).coerceIn(10.0, 60.0) / 100
    val r = Calculators.requiredIncome(moradia, m(v, "outros") ?: 0, limite)
    val renda = m(v, "renda") ?: 0
    val fit = Calculators.rentFit(renda, moradia)
    Text("Uma referência comum é que aluguel + condomínio + IPTU fiquem em até 30% da renda. É uma regra prática, não uma lei.", style = MaterialTheme.typography.bodyMedium)
    ResultCard {
        Stat("Renda recomendada", Money.format(r.recomendada))
        Text("• Pela regra dos ${(limite * 100).toInt()}%: ${Money.format(r.pelaRegra)}")
        Text("• Para cobrir todos os custos + 10% de folga: ${Money.format(r.paraCobrirTudo)}")
        if (renda > 0) {
            Text("Com sua renda, o ideal de moradia é até ${Money.format((renda * limite).toLong())}. Hoje ela seria ${Money.percent(moradia.toDouble() / renda)} da renda.")
            ProgressLine((moradia.toFloat() / renda / 0.6f), "Peso da moradia na renda", when (fit) { Calculators.RentFit.CABE -> Brand.Verde; Calculators.RentFit.APERTADO -> Brand.Amarelo; else -> Brand.Coral })
        }
    }
    MascotBubble(
        when (fit) {
            Calculators.RentFit.CABE -> "Cabe no bolso! O aluguel está dentro do recomendado."
            Calculators.RentFit.APERTADO -> "Dá, mas está apertado. Sobra pouco para imprevistos."
            Calculators.RentFit.PESADO -> "O aluguel cabe no bolso ou está fazendo cosplay de vilão? Aqui, está pesado. Vale buscar opções mais em conta ou dividir."
            Calculators.RentFit.SEM_RENDA -> "Informe sua renda para eu dizer se o aluguel cabe."
        },
        when (fit) { Calculators.RentFit.CABE -> Mood.COMEMORANDO; Calculators.RentFit.PESADO -> Mood.CONFUSA; else -> Mood.PENSANDO }, 64.dp,
    )
    MoneyInput("Aluguel", v["aluguel"] ?: "", { set("aluguel", it) })
    MoneyInput("Condomínio", v["condominio"] ?: "", { set("condominio", it) })
    MoneyInput("IPTU por mês", v["iptu"] ?: "", { set("iptu", it) })
    MoneyInput("Outros custos do mês", v["outros"] ?: "", { set("outros", it) }, help = "Contas, mercado, transporte…")
    MoneyInput("Sua renda atual (opcional)", v["renda"] ?: "", { set("renda", it) })
    NumberInput("Limite para moradia", v["limite"] ?: "30", { set("limite", it) }, suffix = "% da renda")
    val prefs by c.prefs.flow.asState<br.com.casaclara.data.UserPrefs?>(null)
    val custo = prefs?.calc?.get("custo").orEmpty()
    if (custo.isNotEmpty()) SecondaryButton("Preencher com meu custo mensal", Modifier.fillMaxWidth()) {
        val total = custo.values.sumOf { Money.parse(it)?.coerceAtLeast(0) ?: 0 }
        val mor = listOf("aluguel", "condominio", "iptu").sumOf { Money.parse(custo[it]) ?: 0 }
        set("aluguel", custo["aluguel"] ?: ""); set("condominio", custo["condominio"] ?: ""); set("iptu", custo["iptu"] ?: ""); set("outros", Money.toInput(total - mor))
    }
}

@Composable
private fun EconomiaCalc(c: AppContainer, tempo: Boolean) {
    val (v, set) = rememberCalc(c, "economia") ?: return
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    val meta = m(v, "meta") ?: 0
    val prazo = v["prazo"]?.toIntOrNull()
    val r = Calculators.savings(meta, m(v, "guardado") ?: 0, m(v, "aporte") ?: 0, (v["rend"]?.replace(',', '.')?.toDoubleOrNull() ?: 0.0) / 100, prazo)
    Text(if (tempo) "Quanto tempo até a sua meta guardando o que você já consegue?" else "Quanto guardar por mês para chegar à meta no prazo?", style = MaterialTheme.typography.bodyMedium)
    ResultCard {
        if (meta <= 0) Text("Informe uma meta para começar.")
        else {
            Stat("Falta juntar", Money.format(r.falta))
            Text(
                when {
                    r.meses == 0 -> "Meta batida! 🎉"
                    r.meses != null -> "Previsão: ${r.meses} ${if (r.meses == 1) "mês" else "meses"} — lá por ${Dates.monthYearLabel(r.dataPrevista!!)}."
                    else -> "Sem previsão: coloque quanto consegue guardar por mês, mesmo que pouco."
                }, fontWeight = FontWeight.Bold,
            )
            r.aporteParaPrazo?.let { Text("Para chegar em $prazo meses: guarde ${Money.format(it)} por mês.", fontWeight = FontWeight.Bold) }
            ProgressLine(r.progresso.toFloat(), "Progresso da meta")
            Button(onClick = {
                scope.launch { c.db.goals().upsert(GoalEntity(name = "Minha meta", type = "MUDANCA", target = meta, saved = m(v, "guardado") ?: 0, monthly = m(v, "aporte") ?: r.aporteParaPrazo, ratePct = v["rend"]?.replace(',', '.')?.toDoubleOrNull())) }
                fb.celebrate("Meta salva! Acompanhe em Finanças → Metas.")
            }) { Text("Salvar como meta") }
        }
    }
    MoneyInput("Meta", v["meta"] ?: "", { set("meta", it) })
    MoneyInput("Já guardei", v["guardado"] ?: "", { set("guardado", it) })
    MoneyInput("Consigo guardar por mês", v["aporte"] ?: "", { set("aporte", it) })
    NumberInput("Quero chegar em", v["prazo"] ?: "", { set("prazo", it) }, suffix = "meses", help = "Opcional")
    NumberInput("Rendimento", v["rend"] ?: "", { set("rend", it) }, suffix = "% ao mês", help = "Opcional. Rendimento não é garantido; deixe vazio se não souber.")
}

@Composable
private fun DivisaoCalc(c: AppContainer) {
    val (v, set) = rememberCalc(c, "divisao") ?: return
    val total = m(v, "total") ?: 0
    val n = (v["pessoas"]?.toIntOrNull() ?: 2).coerceIn(1, 10)
    val pesos = (0 until n).map { v["peso$it"]?.replace(',', '.')?.toDoubleOrNull() ?: 1.0 }
    val partes = Calculators.split(total, pesos)
    Text("Divida uma despesa em partes iguais ou com pesos (ex.: quarto maior = peso 1,3).", style = MaterialTheme.typography.bodyMedium)
    MoneyInput("Valor total", v["total"] ?: "", { set("total", it) })
    NumberInput("Quantas pessoas", v["pessoas"] ?: "2", { set("pessoas", it) })
    (0 until n).forEach { i ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Pessoa ${i + 1}", Modifier.width(80.dp), fontWeight = FontWeight.Bold)
            NumberInput("Peso", v["peso$i"] ?: "1", { set("peso$i", it) }, modifier = Modifier.weight(1f))
            Text(Money.format(partes.getOrNull(i) ?: 0), fontWeight = FontWeight.Black, modifier = Modifier.width(110.dp))
        }
    }
    AlertBox("A soma das partes é sempre igual ao total — os centavos que sobram são distribuídos.", "INFO")
}

@Composable
private fun CenariosCalc(c: AppContainer) {
    val (v, set) = rememberCalc(c, "cenarios") ?: return
    val res = Calculators.compareScenarios(m(v, "renda") ?: 0, m(v, "custoSozinho") ?: 0, m(v, "compartilhavel") ?: 0, v["pessoas"]?.toIntOrNull() ?: 2, m(v, "inicialSozinho") ?: 0, m(v, "inicialDividir") ?: 0, m(v, "atual") ?: 0)
    val best = res.maxOf { it.sobra }
    ResultCard {
        res.forEach { s ->
            Column(Modifier.padding(vertical = 4.dp)) {
                Text(s.nome + if (s.sobra == best && (m(v, "renda") ?: 0) > 0) "  ⭐ sobra mais" else "", style = MaterialTheme.typography.titleMedium)
                Text("Custo/mês: ${Money.format(s.custoMensal)} · sobra: ${Money.format(s.sobra)}", color = if (s.sobra < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                if (s.custoInicial > 0) Text("Custo inicial: ${Money.format(s.custoInicial)} · " + (s.mesesParaInicial?.let { "~$it mês(es) para juntar" } ?: "não sobra para juntar"))
            }
        }
    }
    AlertBox("Os números mostram só o lado financeiro. Privacidade, distância e convivência também pesam — e só você sabe quanto.", "INFO")
    MoneyInput("Sua renda mensal", v["renda"] ?: "", { set("renda", it) })
    MoneyInput("Custo mensal morando sozinho", v["custoSozinho"] ?: "", { set("custoSozinho", it) })
    MoneyInput("Quanto disso dá para dividir", v["compartilhavel"] ?: "", { set("compartilhavel", it) }, help = "Aluguel, condomínio, luz, água, internet.")
    NumberInput("Moradores ao dividir (contando você)", v["pessoas"] ?: "2", { set("pessoas", it) })
    MoneyInput("Custo inicial morando sozinho", v["inicialSozinho"] ?: "", { set("inicialSozinho", it) })
    MoneyInput("Sua parte do custo inicial ao dividir", v["inicialDividir"] ?: "", { set("inicialDividir", it) })
    MoneyInput("Quanto gasta hoje (com a família)", v["atual"] ?: "", { set("atual", it) })
    val prefs by c.prefs.flow.asState<br.com.casaclara.data.UserPrefs?>(null)
    val custo = prefs?.calc?.get("custo").orEmpty()
    if (custo.isNotEmpty()) SecondaryButton("Preencher com meus cálculos", Modifier.fillMaxWidth()) {
        val total = custo.values.sumOf { Money.parse(it)?.coerceAtLeast(0) ?: 0 }
        val shareable = listOf("aluguel", "condominio", "iptu", "energia", "agua", "gas", "internet").sumOf { Money.parse(custo[it]) ?: 0 }
        set("custoSozinho", Money.toInput(total)); set("compartilhavel", Money.toInput(shareable))
        prefs?.income?.let { set("renda", Money.toInput(it)) }
    }
}

@Composable
private fun TransporteCalc(c: AppContainer) {
    val (v, set) = rememberCalc(c, "transporte") ?: return
    fun d(k: String) = v[k]?.replace(',', '.')?.toDoubleOrNull() ?: 0.0
    val r = Calculators.transport(
        passagem = m(v, "passagem") ?: 0, viagensPorDia = d("viagens").toInt(), diasPorMes = d("dias").toInt(),
        kmPorMes = d("km"), consumoKmPorLitro = d("consumo"), precoLitro = m(v, "litro") ?: 0, estacionamento = m(v, "estac") ?: 0, fixosCarro = m(v, "fixos") ?: 0,
        corridasApp = d("corridas").toInt(), valorMedioCorrida = m(v, "corrida") ?: 0,
    )
    ResultCard {
        Stat("Transporte por mês", Money.format(r.total))
        Bars(listOf("Transporte público" to r.publico, "Carro/moto" to r.carro, "Aplicativo" to r.app).filter { it.second > 0 }, Money::format)
    }
    SectionTitle("🚌 Transporte público")
    MoneyInput("Valor de uma passagem", v["passagem"] ?: "", { set("passagem", it) })
    NumberInput("Viagens por dia", v["viagens"] ?: "2", { set("viagens", it) })
    NumberInput("Dias por mês", v["dias"] ?: "22", { set("dias", it) })
    SectionTitle("🚗 Carro ou moto")
    NumberInput("Km por mês", v["km"] ?: "", { set("km", it) })
    NumberInput("Consumo", v["consumo"] ?: "", { set("consumo", it) }, suffix = "km/l")
    MoneyInput("Preço do litro", v["litro"] ?: "", { set("litro", it) }, help = "Use o preço que você paga hoje.")
    MoneyInput("Estacionamento por mês", v["estac"] ?: "", { set("estac", it) })
    MoneyInput("Fixos por mês (seguro, IPVA/12, manutenção)", v["fixos"] ?: "", { set("fixos", it) })
    SectionTitle("📱 Aplicativo")
    NumberInput("Corridas por mês", v["corridas"] ?: "", { set("corridas", it) })
    MoneyInput("Valor médio da corrida", v["corrida"] ?: "", { set("corrida", it) })
}


// ------------------------------------------------------------------ Trabalhistas (CLT)
@Composable
private fun LaborNote() {
    AlertBox("Estimativa com as tabelas de ${Labor.YEAR} (INSS e Imposto de Renda com a isenção até R$ 5.000). O valor exato depende da empresa, de adicionais e de outros descontos. Confira sempre o seu holerite.", "INFO")
}

@Composable
private fun LaborLine(label: String, value: Long, minus: Boolean = false, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, Modifier.weight(1f), fontWeight = if (bold) FontWeight.Black else FontWeight.Normal)
        Text((if (minus && value > 0) "− " else "") + Money.format(value), fontWeight = if (bold) FontWeight.Black else FontWeight.Bold,
            color = if (minus && value > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SalarioCalc(c: AppContainer) {
    val (v, set) = rememberCalc(c, "salario") ?: return
    val bruto = m(v, "bruto") ?: 0
    val deps = v["deps"]?.toIntOrNull() ?: 0
    val vt = v["vt"] == "1"
    val r = Labor.liquido(bruto, deps, vt, m(v, "custovt"), m(v, "outros") ?: 0, m(v, "pensao") ?: 0)
    ResultCard {
        Stat("Salário líquido estimado", Money.format(r.liquido))
        LaborLine("Salário bruto", r.bruto)
        LaborLine("INSS", r.inss, minus = true)
        LaborLine("Imposto de Renda" + if (r.ir.reducao > 0) " (já com redução)" else "", r.irrf, minus = true)
        if (vt) LaborLine("Vale-transporte", r.valeTransporte, minus = true)
        if (r.outros > 0) LaborLine("Outros descontos", r.outros, minus = true)
        LaborLine("Líquido", r.liquido, bold = true)
        Text("FGTS depositado pela empresa (não sai do seu salário): ${Money.format(r.fgts)} por mês.", style = MaterialTheme.typography.bodySmall)
        if (bruto in 1..500000) Text("Até R$ 5.000 de salário, o Imposto de Renda na fonte fica zerado em ${Labor.YEAR}.", style = MaterialTheme.typography.bodySmall)
    }
    LaborNote()
    MoneyInput("Salário bruto mensal", v["bruto"] ?: "", { set("bruto", it) }, help = "O valor do contrato, antes dos descontos.")
    NumberInput("Dependentes para o Imposto de Renda", v["deps"] ?: "0", { set("deps", it) })
    Row(verticalAlignment = Alignment.CenterVertically) {
        Switch(checked = vt, onCheckedChange = { set("vt", if (it) "1" else "0") })
        Spacer(Modifier.width(8.dp)); Text("Uso vale-transporte")
    }
    if (vt) MoneyInput("Custo das passagens no mês (opcional)", v["custovt"] ?: "", { set("custovt", it) }, help = "O desconto é até 6% do salário ou o custo das passagens, o que for menor.")
    MoneyInput("Pensão alimentícia descontada (se houver)", v["pensao"] ?: "", { set("pensao", it) })
    MoneyInput("Outros descontos (plano, refeição…)", v["outros"] ?: "", { set("outros", it) })
}

@Composable
private fun FeriasCalc(c: AppContainer) {
    val (v, set) = rememberCalc(c, "ferias") ?: return
    val sal = m(v, "sal") ?: 0
    val vend = (v["vend"]?.toIntOrNull() ?: 0).coerceIn(0, 10)
    val dias = (v["dias"]?.toIntOrNull() ?: 30)
    val r = Labor.ferias(sal, m(v, "medias") ?: 0, dias, vend, v["deps"]?.toIntOrNull() ?: 0)
    ResultCard {
        Stat("Você recebe (estimado)", Money.format(r.liquido))
        LaborLine("Férias (${r.dias} dias)", r.valorFerias)
        LaborLine("1/3 constitucional", r.terco)
        if (r.abono > 0) { LaborLine("Abono (${vend} dias vendidos)", r.abono); LaborLine("1/3 do abono", r.tercoAbono) }
        LaborLine("INSS", r.inss, minus = true)
        LaborLine("Imposto de Renda", r.irrf, minus = true)
        LaborLine("Total líquido", r.liquido, bold = true)
        Text("O pagamento vem até 2 dias antes das férias. O salário do mês seguinte costuma vir menor — reserve uma parte.", style = MaterialTheme.typography.bodySmall)
    }
    LaborNote()
    MoneyInput("Salário bruto mensal", v["sal"] ?: "", { set("sal", it) })
    MoneyInput("Média de horas extras e adicionais (se houver)", v["medias"] ?: "", { set("medias", it) })
    NumberInput("Dias de férias", v["dias"] ?: "30", { set("dias", it) }, help = "Mínimo de 5 dias por período.")
    NumberInput("Dias vendidos (abono)", v["vend"] ?: "0", { set("vend", it) }, help = "Até 10 dias.")
    NumberInput("Dependentes para o Imposto de Renda", v["deps"] ?: "0", { set("deps", it) })
}

@Composable
private fun DecimoCalc(c: AppContainer) {
    val (v, set) = rememberCalc(c, "decimo") ?: return
    val r = Labor.decimoTerceiro(m(v, "sal") ?: 0, m(v, "medias") ?: 0, v["meses"]?.toIntOrNull() ?: 12, v["deps"]?.toIntOrNull() ?: 0)
    ResultCard {
        Stat("13º líquido (estimado)", Money.format(r.liquidoTotal))
        LaborLine("13º bruto (${r.meses}/12)", r.bruto)
        LaborLine("1ª parcela (até 30/11, sem descontos)", r.primeira)
        LaborLine("INSS (sai da 2ª parcela)", r.inss, minus = true)
        LaborLine("Imposto de Renda (sai da 2ª parcela)", r.irrf, minus = true)
        LaborLine("2ª parcela (até 20/12)", r.segunda, bold = true)
    }
    LaborNote()
    MoneyInput("Salário bruto mensal", v["sal"] ?: "", { set("sal", it) })
    MoneyInput("Média de horas extras e adicionais (se houver)", v["medias"] ?: "", { set("medias", it) })
    NumberInput("Meses trabalhados no ano", v["meses"] ?: "12", { set("meses", it) }, help = "Conta o mês em que trabalhou 15 dias ou mais.")
    NumberInput("Dependentes para o Imposto de Renda", v["deps"] ?: "0", { set("deps", it) })
}


// ------------------------------------------------------------------ Quitar dívidas
@Composable
private fun DebtPlanCalc(c: AppContainer) {
    val (v, set) = rememberCalc(c, "quitar") ?: return
    val debts = (1..4).mapNotNull { i ->
        val bal = m(v, "b$i") ?: return@mapNotNull null
        if (bal <= 0) return@mapNotNull null
        br.com.casaclara.domain.DebtPlan.Debt(v["n$i"]?.ifBlank { null } ?: "Dívida $i", bal, v["r$i"]?.replace(',', '.')?.toDoubleOrNull() ?: 0.0, m(v, "p$i") ?: 0)
    }
    val extra = m(v, "extra") ?: 0
    if (debts.isNotEmpty()) {
        val neve = br.com.casaclara.domain.DebtPlan.simulate(debts, extra, br.com.casaclara.domain.DebtPlan.Method.BOLA_DE_NEVE)
        val ava = br.com.casaclara.domain.DebtPlan.simulate(debts, extra, br.com.casaclara.domain.DebtPlan.Method.AVALANCHE)
        ResultCard {
            if (!ava.feasible) {
                Text("⚠️ Com esses valores a dívida não acaba: as parcelas não cobrem os juros.", fontWeight = FontWeight.Bold)
                Text("Aumente o valor extra ou tente renegociar a taxa. Juros altos (cartão, cheque especial) são prioridade.", style = MaterialTheme.typography.bodySmall)
            } else {
                Stat("Livre das dívidas em", "${ava.months} ${if (ava.months == 1) "mês" else "meses"}")
                listOf(ava to "🏔️ Avalanche (maior juros primeiro)", neve to "⛄ Bola de neve (menor saldo primeiro)").forEach { (r, label) ->
                    Text(label, fontWeight = FontWeight.Black)
                    Text("${r.months} meses · juros pagos ${Money.format(r.totalInterest)} · total ${Money.format(r.totalPaid)}", style = MaterialTheme.typography.bodySmall)
                    Text("Ordem: " + r.payoff.joinToString(" → ") { "${it.first} (mês ${it.second})" }, style = MaterialTheme.typography.bodySmall)
                }
                val dif = neve.totalInterest - ava.totalInterest
                Text(if (dif > 0) "A avalanche economiza ${Money.format(dif)} de juros. A bola de neve dá vitórias mais rápidas, o que ajuda a manter o ânimo." else "Os dois métodos dão praticamente o mesmo resultado aqui. Escolha o que te motiva mais.")
            }
        }
    }
    AlertBox("Informe o saldo devedor hoje, os juros ao mês e a parcela mínima de cada dívida. \"Extra\" é quanto a mais você consegue pagar por mês além das parcelas. Quando uma dívida acaba, a parcela dela vai para a próxima.", "INFO")
    MoneyInput("Valor extra por mês", v["extra"] ?: "", { set("extra", it) })
    (1..4).forEach { i ->
        SectionTitle("Dívida $i")
        OutlinedTextField(v["n$i"] ?: "", { set("n$i", it) }, label = { Text("Nome (ex.: cartão, empréstimo)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MoneyInput("Saldo", v["b$i"] ?: "", { set("b$i", it) }, modifier = Modifier.weight(1f))
            MoneyInput("Parcela mínima", v["p$i"] ?: "", { set("p$i", it) }, modifier = Modifier.weight(1f))
        }
        NumberInput("Juros ao mês", v["r$i"] ?: "", { set("r$i", it) }, suffix = "%")
    }
}

// ------------------------------------------------------------------ Desafio das 52 semanas
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun Challenge52Calc(c: AppContainer) {
    val (v, set) = rememberCalc(c, "desafio52") ?: return
    val fb = br.com.casaclara.ui.components.LocalFeedback.current
    val base = m(v, "base") ?: 0
    val weeks = v["weeks"].orEmpty().split(',').mapNotNull { it.toIntOrNull() }.toSet()
    val ch = br.com.casaclara.domain.Challenge52
    ResultCard {
        Stat("Guardado no desafio", Money.format(ch.saved(base, weeks)))
        ProgressLine(weeks.size / 52f, "Semanas concluídas")
        Text("${weeks.size} de 52 semanas · meta final ${Money.format(ch.total(base))}", style = MaterialTheme.typography.bodySmall)
        val next = (1..52).firstOrNull { it !in weeks }
        if (next != null && base > 0) Text("Próxima: semana $next → guarde ${Money.format(ch.deposit(base, next))}", fontWeight = FontWeight.Bold)
    }
    MoneyInput("Valor da semana 1", v["base"] ?: "", { set("base", it) }, help = "Na semana 2 você guarda o dobro, na 3 o triplo… Com R$ 5, chega a ${Money.format(ch.total(500))} no ano.")
    if (base > 0) {
        Text("Toque na semana quando guardar:", fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            (1..52).forEach { w ->
                val done = w in weeks
                FilterChip(done, {
                    val n = if (done) weeks - w else weeks + w
                    set("weeks", n.sorted().joinToString(","))
                    if (!done && n.size == 52) fb.celebrate("Desafio completo! ${Money.format(ch.total(base))} guardados! 🎉")
                    else if (!done && n.size % 10 == 0) fb.celebrate("${n.size} semanas! Continua assim.")
                }, { Text("$w") })
            }
        }
        Text("Dica: dá para fazer ao contrário (começar pela semana 52) para pegar o pesado no início do ano.", style = MaterialTheme.typography.bodySmall)
    }
}
