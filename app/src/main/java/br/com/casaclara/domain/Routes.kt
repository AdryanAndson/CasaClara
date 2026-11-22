package br.com.casaclara.domain

/** Rotas de navegação compartilhadas entre a lógica (jornada, busca) e a interface. */
object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val JOURNEY = "journey"
    const val FINANCE = "finance?tab={tab}"
    const val HOUSE = "house?tab={tab}"
    const val MORE = "more"
    const val MOVING = "moving?tab={tab}"
    const val MARKET = "market?tab={tab}"
    const val FURNITURE = "furniture?tab={tab}"
    const val SHARED = "shared?tab={tab}"
    const val LIBRARY = "library"
    const val GUIDE = "guide/{id}"
    const val CALCULATORS = "calculators"
    const val CALC = "calc/{id}"
    const val ACHIEVEMENTS = "achievements"
    const val REMINDERS = "reminders"
    const val SETTINGS = "settings"
    const val PROFILE = "profile"
    const val SEARCH = "search"
    const val PLANS = "plans"
    const val CONTACTS = "contacts"

    fun finance(tab: Int = 0) = "finance?tab=$tab"
    fun house(tab: Int = 0) = "house?tab=$tab"
    fun moving(tab: Int = 0) = "moving?tab=$tab"
    fun market(tab: Int = 0) = "market?tab=$tab"
    fun furniture(tab: Int = 0) = "furniture?tab=$tab"
    fun shared(tab: Int = 0) = "shared?tab=$tab"
    fun guide(id: String) = "guide/$id"
    fun calc(id: String) = "calc/$id"

    // Índices das abas (mantidos aqui para a jornada e a busca apontarem para o lugar certo)
    object FinanceTab { const val RESUMO = 0; const val LANCAMENTOS = 1; const val CONTAS = 2; const val DIVIDAS = 3; const val ORCAMENTO = 4; const val METAS = 5; const val RELATORIOS = 6 }
    object HouseTab { const val TAREFAS = 0; const val LIMPEZA = 1; const val CALENDARIO = 2; const val COMPRAS = 3; const val INVENTARIO = 4; const val GARANTIAS = 5 }
    object MovingTab { const val CHECKLIST = 0; const val CRONOGRAMA = 1; const val DOCUMENTOS = 2; const val CUSTOS = 3; const val VISITA = 4; const val IMOVEIS = 5; const val PERGUNTAS = 6 }
    object MarketTab { const val LISTA = 0; const val FAVORITOS = 1; const val CARDAPIO = 2; const val DESPENSA = 3; const val RECEITAS = 4 }
    object SharedTab { const val RESUMO = 0; const val MORADORES = 1; const val DESPESAS = 2; const val REEMBOLSOS = 3; const val TAREFAS = 4; const val ACORDOS = 5 }
}
