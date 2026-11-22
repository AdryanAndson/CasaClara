package br.com.casaclara.ui

import br.com.casaclara.domain.Routes

/** Cartões que podem aparecer no Início (a pessoa escolhe e ordena). */
data class HomeCard(val id: String, val emoji: String, val title: String, val desc: String)

object HomeCatalog {
    val CARDS = listOf(
        HomeCard("proximo", "⭐", "Próximo passo", "A tarefa recomendada da sua jornada"),
        HomeCard("semana", "📆", "Resumo da semana", "Gastos, tarefas feitas e contas da semana"),
        HomeCard("mes", "🍩", "Mês em gráfico", "Entrou, saiu, sobrou e para onde foi"),
        HomeCard("contas", "📅", "Próximas contas", "O que vence nos próximos 7 dias"),
        HomeCard("tarefas", "🧹", "Para hoje na casa", "Tarefas de hoje e atrasadas"),
        HomeCard("metas", "🐷", "Cofrinhos", "Suas metas de economia"),
        HomeCard("mercado", "🛒", "Lista de mercado", "O que falta comprar"),
        HomeCard("atalhos", "🚀", "Atalhos favoritos", "Suas ferramentas preferidas"),
        HomeCard("dica", "💡", "Dica do dia", "Uma dica rápida da Clarinha"),
        HomeCard("conquistas", "🏆", "Conquistas", "O que você já conquistou"),
    )

    /** Ordem padrão muda conforme a situação: quem planeja vê planejamento antes; quem já mora, a rotina. */
    fun defaultOrder(planning: Boolean): List<String> =
        if (planning) listOf("proximo", "metas", "semana", "atalhos", "mes", "contas", "tarefas", "dica", "mercado", "conquistas")
        else listOf("proximo", "semana", "contas", "tarefas", "atalhos", "mes", "metas", "mercado", "dica", "conquistas")

    fun resolve(order: List<String>, planning: Boolean): List<String> {
        val base = if (order.isEmpty()) defaultOrder(planning) else order
        return base.filter { id -> CARDS.any { it.id == id } } + CARDS.map { it.id }.filter { it !in base }
    }
}

/** Ferramentas que podem virar atalho favorito. */
data class ToolDef(val id: String, val emoji: String, val label: String, val route: String)

object Tools {
    val ALL = listOf(
        ToolDef("lancar", "🧾", "Lançar gasto", Routes.finance(Routes.FinanceTab.LANCAMENTOS)),
        ToolDef("emergencia", "🆘", "Emergência", Routes.EMERGENCY),
        ToolDef("golpe", "🛡️", "É golpe?", Routes.GOLPE),
        ToolDef("docs", "🪪", "Documentos", Routes.DOCS),
        ToolDef("consumo", "💡", "Consumo", Routes.CONSUMO),
        ToolDef("quiz", "🧠", "Teste rápido", Routes.QUIZ),
        ToolDef("salario", "💵", "Salário líquido", Routes.calc("salario")),
        ToolDef("contas", "📅", "Contas", Routes.finance(Routes.FinanceTab.CONTAS)),
        ToolDef("cartoes", "💳", "Cartões", Routes.finance(Routes.FinanceTab.CARTOES)),
        ToolDef("recorrentes", "🔁", "Recorrentes", Routes.finance(Routes.FinanceTab.RECORRENTES)),
        ToolDef("metas", "🐷", "Cofrinhos", Routes.finance(Routes.FinanceTab.METAS)),
        ToolDef("orcamento", "🎚️", "Orçamento", Routes.finance(Routes.FinanceTab.ORCAMENTO)),
        ToolDef("relatorios", "📊", "Relatórios", Routes.finance(Routes.FinanceTab.RELATORIOS)),
        ToolDef("importar", "📥", "Importar extrato", Routes.IMPORT),
        ToolDef("calculadoras", "🧮", "Calculadoras", Routes.CALCULATORS),
        ToolDef("tarefas", "🧹", "Tarefas", Routes.house(Routes.HouseTab.TAREFAS)),
        ToolDef("plano", "🗓️", "Plano da semana", Routes.WEEKPLAN),
        ToolDef("timer", "⏱️", "Faxina cronometrada", Routes.TIMER),
        ToolDef("calendario", "📆", "Calendário", Routes.house(Routes.HouseTab.CALENDARIO)),
        ToolDef("mercado", "🛒", "Mercado", Routes.market(Routes.MarketTab.LISTA)),
        ToolDef("cardapio", "🍽️", "Cardápio", Routes.market(Routes.MarketTab.CARDAPIO)),
        ToolDef("despensa", "🥫", "Despensa", Routes.market(Routes.MarketTab.DESPENSA)),
        ToolDef("mudanca", "📦", "Mudança", Routes.moving(0)),
        ToolDef("imoveis", "🔎", "Imóveis", Routes.moving(Routes.MovingTab.IMOVEIS)),
        ToolDef("moveis", "🛋️", "Móveis", Routes.furniture(0)),
        ToolDef("dividida", "🤝", "Casa dividida", Routes.shared(0)),
        ToolDef("contatos", "📇", "Contatos", Routes.CONTACTS),
        ToolDef("lembretes", "🔔", "Lembretes", Routes.REMINDERS),
        ToolDef("guias", "📚", "Guias", Routes.LIBRARY),
    )

    fun defaults(planning: Boolean) =
        if (planning) listOf("calculadoras", "metas", "mudanca", "imoveis", "lancar", "guias")
        else listOf("lancar", "contas", "tarefas", "mercado", "timer", "plano")
}
