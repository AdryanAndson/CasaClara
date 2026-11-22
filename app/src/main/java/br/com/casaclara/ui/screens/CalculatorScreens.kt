package br.com.casaclara.ui.screens

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
