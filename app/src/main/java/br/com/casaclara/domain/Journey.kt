package br.com.casaclara.domain

enum class Situation(val label: String, val emoji: String, val phrase: String) {
    PAIS("Moro com a família e quero me preparar para sair", "🏡", "se preparando para sair de casa"),
    JUNTANDO("Estou juntando dinheiro para me mudar", "🐷", "juntando para a mudança"),
    MUDOU("Acabei de me mudar", "📦", "começando na casa nova"),
    SOZINHO("Já moro sozinho(a)", "🔑", "morando sozinho(a)"),
    DIVIDE("Divido a casa com outras pessoas", "🧑‍🤝‍🧑", "dividindo a casa"),
}

enum class MainGoal(val label: String, val emoji: String) {
    DINHEIRO("Organizar meu dinheiro", "💰"),
    MUDANCA("Preparar uma mudança", "📦"),
    CASA("Cuidar da casa e da rotina", "🧹"),
    APRENDER("Aprender tarefas domésticas", "🍳"),
}

enum class Area { PERFIL, DINHEIRO, MUDANCA, CASA, APRENDER }

/** Fotografia dos números do app, usada pela jornada e pelas conquistas. */
data class Snapshot(
    val onboarded: Boolean = false,
    val livingCostDone: Boolean = false,
    val initialCostDone: Boolean = false,
    val requiredIncomeDone: Boolean = false,
    val scenariosDone: Boolean = false,
    val incomes: Int = 0,
    val expenses: Int = 0,
    val bills: Int = 0,
    val billsPaidThisMonth: Int = 0,
    val debts: Int = 0,
    val debtsPaidOff: Int = 0,
    val budgets: Int = 0,
    val goals: Int = 0,
    val goalsCompleted: Int = 0,
    val reserveSaved: Long = 0,
    val prepDone: Int = 0,
    val movingDone: Int = 0,
    val movingTotal: Int = 0,
    val documentsDone: Int = 0,
    val visitDone: Int = 0,
    val properties: Int = 0,
    val questionsDone: Int = 0,
    val furniture: Int = 0,
    val furnitureBought: Int = 0,
    val houseTasks: Int = 0,
    val houseTasksDone: Int = 0,
    val cleaningRoomsComplete: Int = 0,
    val marketItems: Int = 0,
    val marketBought: Int = 0,
    val favoritesMarket: Int = 0,
    val meals: Int = 0,
    val pantry: Int = 0,
    val contacts: Int = 0,
    val emergencyContacts: Int = 0,
    val warranties: Int = 0,
    val members: Int = 0,
    val sharedExpenses: Int = 0,
    val reimbursements: Int = 0,
    val agreements: Int = 0,
    val guidesRead: Int = 0,
    val guideFavorites: Int = 0,
    val remindersOn: Int = 0,
    val milestones: Int = 0,
    val docs: Int = 0,
    val readings: Int = 0,
    val quizBest: Int = 0,
    val emergencyCard: Boolean = false,
    val scamChecks: Int = 0,
    val laborCalc: Boolean = false,
)

data class JourneyStep(
    val id: String, val area: Area, val title: String, val why: String, val emoji: String, val route: String,
    val check: (Snapshot) -> Boolean,
)

data class StepState(val step: JourneyStep, val done: Boolean, val manual: Boolean, val skipped: Boolean)
data class JourneyState(val steps: List<StepState>, val next: StepState?) {
    val doneCount get() = steps.count { it.done }
    val total get() = steps.count { !it.skipped }
    val progress get() = if (total == 0) 0.0 else steps.count { it.done && !it.skipped }.toDouble() / total
}

object Journey {
    val STEPS: Map<String, JourneyStep> = listOf(
        JourneyStep("perfil", Area.PERFIL, "Contar sua situação", "Assim o Casa Clara mostra o que faz sentido para você.", "👋", Routes.PROFILE) { it.onboarded },
        JourneyStep("custo", Area.DINHEIRO, "Descobrir quanto custa morar sozinho", "É o número que guia o resto do plano.", "🧮", Routes.calc("custo")) { it.livingCostDone },
        JourneyStep("renda", Area.DINHEIRO, "Registrar sua renda", "Fixa, variável ou bico: tudo conta.", "💵", Routes.finance(Routes.FinanceTab.LANCAMENTOS)) { it.incomes > 0 },
        JourneyStep("gastos", Area.DINHEIRO, "Anotar 5 gastos", "Saber para onde o dinheiro vai é o primeiro superpoder.", "🧾", Routes.finance(Routes.FinanceTab.LANCAMENTOS)) { it.expenses >= 5 },
        JourneyStep("rendaNecessaria", Area.DINHEIRO, "Ver se o aluguel cabe no bolso", "Ou se está fazendo cosplay de vilão.", "🦹", Routes.calc("renda")) { it.requiredIncomeDone },
        JourneyStep("inicial", Area.MUDANCA, "Calcular o custo inicial da mudança", "Caução, frete e o básico da casa somam rápido.", "📦", Routes.calc("inicial")) { it.initialCostDone },
        JourneyStep("meta", Area.DINHEIRO, "Criar uma meta de economia", "Com meta e prazo, guardar fica mais fácil.", "🎯", Routes.finance(Routes.FinanceTab.METAS)) { it.goals > 0 },
        JourneyStep("cenarios", Area.MUDANCA, "Comparar sozinho, dividir ou esperar", "Ver os caminhos lado a lado ajuda a decidir.", "⚖️", Routes.calc("cenarios")) { it.scenariosDone },
        JourneyStep("reserva", Area.DINHEIRO, "Começar a reserva de emergência", "É o colchão para os imprevistos. Qualquer valor conta.", "🛟", Routes.finance(Routes.FinanceTab.METAS)) { it.reserveSaved > 0 },
        JourneyStep("orcamento", Area.DINHEIRO, "Montar um orçamento", "Um limite por categoria evita sustos no fim do mês.", "🎚️", Routes.finance(Routes.FinanceTab.ORCAMENTO)) { it.budgets > 0 },
        JourneyStep("preparo", Area.APRENDER, "Fazer 3 tarefas do plano de preparação", "Pequenos treinos de vida adulta.", "🪜", Routes.moving(Routes.MovingTab.CHECKLIST)) { it.prepDone >= 3 },
        JourneyStep("documentos", Area.MUDANCA, "Separar os documentos da mudança", "Na hora do contrato, ninguém quer caçar papel.", "🗂️", Routes.moving(Routes.MovingTab.DOCUMENTOS)) { it.documentsDone >= 3 },
        JourneyStep("imoveis", Area.MUDANCA, "Comparar pelo menos 2 imóveis", "Comparar evita decidir pela foto bonita.", "🔎", Routes.moving(Routes.MovingTab.IMOVEIS)) { it.properties >= 2 },
        JourneyStep("checklist", Area.MUDANCA, "Completar metade do checklist de mudança", "Mudança organizada é mudança sem caos.", "✅", Routes.moving(Routes.MovingTab.CHECKLIST)) { it.movingTotal > 0 && it.movingDone * 2 >= it.movingTotal },
        JourneyStep("contas", Area.DINHEIRO, "Cadastrar as contas da casa", "Vencimento anotado é multa evitada.", "📅", Routes.finance(Routes.FinanceTab.CONTAS)) { it.bills > 0 },
        JourneyStep("lembrete", Area.CASA, "Ativar um lembrete", "Deixa o celular lembrar por você.", "🔔", Routes.REMINDERS) { it.remindersOn > 0 },
        JourneyStep("moveis", Area.MUDANCA, "Listar o essencial para começar", "Você não precisa comprar a casa inteira no primeiro dia.", "🛋️", Routes.furniture(0)) { it.furniture > 0 },
        JourneyStep("rotina", Area.CASA, "Criar sua rotina de tarefas", "Pouco todo dia vence a faxina de seis horas.", "🧹", Routes.house(Routes.HouseTab.TAREFAS)) { it.houseTasks >= 3 },
        JourneyStep("tarefaFeita", Area.CASA, "Concluir 5 tarefas da casa", "Essa tarefa parece chata, mas vai deixar sua vida mais fácil.", "✨", Routes.house(Routes.HouseTab.TAREFAS)) { it.houseTasksDone >= 5 },
        JourneyStep("mercado", Area.APRENDER, "Fazer uma lista de mercado", "Lista na mão, carteira mais feliz.", "🛒", Routes.market(Routes.MarketTab.LISTA)) { it.marketItems > 0 },
        JourneyStep("cardapio", Area.APRENDER, "Planejar 3 refeições da semana", "Planejar evita o delivery por desespero.", "🍲", Routes.market(Routes.MarketTab.CARDAPIO)) { it.meals >= 3 },
        JourneyStep("guia", Area.APRENDER, "Ler 3 guias da biblioteca", "Cinco minutos de leitura poupam horas de dor de cabeça.", "📚", Routes.LIBRARY) { it.guidesRead >= 3 },
        JourneyStep("contatos", Area.CASA, "Salvar contatos de emergência e de profissionais", "Quando o cano estoura, não é hora de procurar encanador.", "📇", Routes.CONTACTS) { it.contacts > 0 },
        JourneyStep("garantias", Area.CASA, "Anotar uma garantia ou nota fiscal", "A nota some justo quando o produto quebra.", "🧾", Routes.house(Routes.HouseTab.GARANTIAS)) { it.warranties > 0 },
        JourneyStep("moradores", Area.CASA, "Cadastrar quem mora com você", "Para dividir contas e tarefas de forma justa.", "🧑‍🤝‍🧑", Routes.shared(Routes.SharedTab.MORADORES)) { it.members >= 2 },
        JourneyStep("acordos", Area.CASA, "Escrever os acordos da casa", "Combinado escrito não sai caro.", "🤝", Routes.shared(Routes.SharedTab.ACORDOS)) { it.agreements > 0 },
        JourneyStep("despesaComp", Area.DINHEIRO, "Registrar uma despesa dividida", "O app calcula quem deve a quem.", "➗", Routes.shared(Routes.SharedTab.DESPESAS)) { it.sharedExpenses > 0 },
    ).associateBy { it.id }

    val PATHS: Map<Situation, List<String>> = mapOf(
        Situation.PAIS to listOf("perfil", "custo", "renda", "gastos", "rendaNecessaria", "inicial", "meta", "cenarios", "preparo", "guia", "reserva", "orcamento", "documentos", "imoveis", "checklist", "moveis", "mercado"),
        Situation.JUNTANDO to listOf("perfil", "custo", "renda", "gastos", "meta", "inicial", "rendaNecessaria", "cenarios", "reserva", "orcamento", "lembrete", "guia", "documentos", "imoveis", "checklist", "moveis"),
        Situation.MUDOU to listOf("perfil", "contas", "lembrete", "renda", "gastos", "moveis", "orcamento", "rotina", "mercado", "cardapio", "contatos", "garantias", "reserva", "tarefaFeita", "guia"),
        Situation.SOZINHO to listOf("perfil", "renda", "gastos", "contas", "lembrete", "orcamento", "reserva", "rotina", "tarefaFeita", "mercado", "cardapio", "contatos", "garantias", "guia", "meta"),
        Situation.DIVIDE to listOf("perfil", "moradores", "acordos", "despesaComp", "renda", "gastos", "contas", "lembrete", "orcamento", "rotina", "mercado", "reserva", "contatos", "guia"),
    )

    private val GOAL_AREA = mapOf(MainGoal.DINHEIRO to Area.DINHEIRO, MainGoal.MUDANCA to Area.MUDANCA, MainGoal.CASA to Area.CASA, MainGoal.APRENDER to Area.APRENDER)

    /** Caminho personalizado: começa pela situação e puxa para frente os passos ligados ao objetivo principal. */
    fun path(situation: Situation?, goal: MainGoal?): List<JourneyStep> {
        val ids = PATHS[situation ?: Situation.JUNTANDO]!!
        val steps = ids.mapNotNull { STEPS[it] }
        val area = goal?.let { GOAL_AREA[it] } ?: return steps
        val first = steps.take(1)
        val rest = steps.drop(1)
        val (focus, others) = rest.partition { it.area == area }
        // mantém no máximo 3 passos do objetivo à frente, para não bagunçar a ordem lógica
        return first + focus.take(3) + (focus.drop(3) + others).sortedBy { ids.indexOf(it.id) }
    }

    fun compute(snapshot: Snapshot, situation: Situation?, goal: MainGoal?, manual: Set<String>, skipped: Set<String>): JourneyState {
        val list = path(situation, goal).map { s ->
            val auto = runCatching { s.check(snapshot) }.getOrDefault(false)
            StepState(s, auto || s.id in manual, !auto && s.id in manual, s.id in skipped)
        }
        return JourneyState(list, list.firstOrNull { !it.done && !it.skipped })
    }
}

data class Achievement(val id: String, val title: String, val description: String, val emoji: String, val check: (Snapshot) -> Boolean)

object Achievements {
    val ALL = listOf(
        Achievement("primeiros-passos", "Primeiros passos", "Contou sua situação e montou sua jornada.", "👣") { it.onboarded },
        Achievement("calculista", "Calculista de respeito", "Descobriu quanto custa morar sozinho.", "🧮") { it.livingCostDone },
        Achievement("de-olho", "De olho no dinheiro", "Registrou 10 gastos.", "🧾") { it.expenses >= 10 },
        Achievement("renda", "Toda renda conta", "Registrou uma entrada.", "💵") { it.incomes > 0 },
        Achievement("conta-paga", "Conta em dia", "Marcou uma conta como paga.", "✅") { it.billsPaidThisMonth > 0 },
        Achievement("orcamento", "Dono(a) do orçamento", "Criou limites para 3 categorias.", "🎚️") { it.budgets >= 3 },
        Achievement("meta", "Com meta é diferente", "Criou uma meta de economia.", "🎯") { it.goals > 0 },
        Achievement("meta-batida", "Meta batida!", "Alcançou uma meta de economia.", "🏆") { it.goalsCompleted > 0 },
        Achievement("colchao", "Colchão de segurança", "Começou a reserva de emergência.", "🛟") { it.reserveSaved > 0 },
        Achievement("quitou", "Uma a menos", "Quitou uma dívida ou compra parcelada.", "🎉") { it.debtsPaidOff > 0 },
        Achievement("cacador", "Caçador(a) de imóveis", "Comparou 3 imóveis.", "🔎") { it.properties >= 3 },
        Achievement("vistoria", "Olho de vistoria", "Conferiu 10 itens numa visita.", "🔦") { it.visitDone >= 10 },
        Achievement("papelada", "Papelada em ordem", "Separou 5 documentos da mudança.", "🗂️") { it.documentsDone >= 5 },
        Achievement("mudanca", "Mudança organizada", "Completou o checklist de mudança.", "📦") { it.movingTotal > 0 && it.movingDone == it.movingTotal },
        Achievement("rotina", "Rotina de casa", "Concluiu 10 tarefas domésticas.", "🧹") { it.houseTasksDone >= 10 },
        Achievement("brilhando", "Cômodo brilhando", "Completou a limpeza de um cômodo.", "✨") { it.cleaningRoomsComplete > 0 },
        Achievement("feira", "Lista na mão", "Marcou 10 itens como comprados.", "🛒") { it.marketBought >= 10 },
        Achievement("chef", "Chef da semana", "Planejou 7 refeições.", "🍳") { it.meals >= 7 },
        Achievement("despensa", "Nada se perde", "Controla 5 itens na despensa.", "🥫") { it.pantry >= 5 },
        Achievement("essencial", "O essencial primeiro", "Comprou 5 itens da lista da casa.", "🛋️") { it.furnitureBought >= 5 },
        Achievement("previdente", "Previdente", "Salvou um contato de emergência.", "📇") { it.emergencyContacts > 0 },
        Achievement("garantia", "Nota guardada", "Anotou uma garantia.", "🧾") { it.warranties > 0 },
        Achievement("convivencia", "Boa convivência", "Registrou os acordos da casa.", "🤝") { it.agreements > 0 },
        Achievement("acerto", "Contas acertadas", "Registrou um reembolso entre moradores.", "🔁") { it.reimbursements > 0 },
        Achievement("leitor", "Leitor(a) curioso(a)", "Leu 5 guias.", "📚") { it.guidesRead >= 5 },
        Achievement("biblioteca", "Rato de biblioteca", "Leu 20 guias.", "🦉") { it.guidesRead >= 20 },
        Achievement("lembrete", "Memória de elefante", "Ativou um lembrete.", "🔔") { it.remindersOn > 0 },
        Achievement("papelada", "Papelada em dia", "Cadastrou um documento com validade.", "🪪") { it.docs > 0 },
        Achievement("medidor", "Olho no medidor", "Anotou duas leituras de consumo.", "📟") { it.readings >= 2 },
        Achievement("sabichao", "Sabe-tudo da casa", "Acertou 8 ou mais no teste rápido.", "🧠") { it.quizBest >= 8 },
        Achievement("preparado", "Prevenido(a)", "Preencheu o cartão de emergência.", "🆘") { it.emergencyCard },
        Achievement("anti-golpe", "Escudo anti-golpe", "Usou o detector de golpe.", "🛡️") { it.scamChecks > 0 },
        Achievement("holerite", "Entendi meu salário", "Simulou o salário líquido.", "🧾") { it.laborCalc },
    )

    fun unlocked(s: Snapshot): Set<String> = ALL.filter { runCatching { it.check(s) }.getOrDefault(false) }.map { it.id }.toSet()
}
