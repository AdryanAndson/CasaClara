package br.com.casaclara.premium

/**
 * Estrutura para planos gratuito e premium — PREPARADA, NÃO ATIVA.
 *
 * Regra do produto: nada essencial fica atrás de pagamento. O Premium só reunirá recursos
 * que dependem de servidor (sincronização, casa compartilhada em tempo real, relatórios avançados).
 *
 * Para ativar no futuro:
 *  1. Implementar um backend real (ex.: Supabase) com autenticação de verdade.
 *  2. Integrar o Google Play Billing (com.android.billingclient) e validar compras no servidor.
 *  3. Trocar [BillingGateway.status] para refletir o estado real.
 * Até lá, nenhuma cobrança é feita e nenhuma compra é simulada.
 */
enum class Plan { GRATUITO, PREMIUM }

data class PlanInfo(val plan: Plan, val name: String, val price: String, val features: List<String>)

object Plans {
    val ALL = listOf(
        PlanInfo(
            Plan.GRATUITO, "Gratuito", "R$ 0",
            listOf(
                "Todas as calculadoras e a jornada personalizada",
                "Finanças completas: lançamentos, contas, dívidas, orçamento, metas e relatórios",
                "Mudança, casa, mercado, móveis e casa compartilhada",
                "Biblioteca com 100 guias, favoritos e histórico",
                "Lembretes locais e exportação CSV",
                "Cópia de segurança local",
            ),
        ),
        PlanInfo(
            Plan.PREMIUM, "Premium", "em breve",
            listOf(
                "Sincronização entre aparelhos",
                "Casa compartilhada em tempo real, com cada morador no próprio celular",
                "Relatórios avançados",
                "Backup automático na nuvem",
            ),
        ),
    )

    /** Sem backend e sem loja configurada, todo mundo usa o gratuito — que já tem tudo o que é essencial. */
    fun current(): Plan = Plan.GRATUITO
}

object BillingGateway {
    data class Status(val available: Boolean, val reason: String)
    fun status() = Status(
        available = false,
        reason = "As assinaturas ainda não estão disponíveis: falta configurar a loja (Google Play Billing) e um servidor para sincronizar os dados. Nenhuma cobrança é feita.",
    )
}
