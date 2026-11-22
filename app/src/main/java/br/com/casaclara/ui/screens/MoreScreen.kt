package br.com.casaclara.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.casaclara.data.AppContainer
import br.com.casaclara.domain.Routes
import br.com.casaclara.ui.components.BrandCard
import br.com.casaclara.ui.components.EmojiBadge
import br.com.casaclara.ui.components.Mood
import br.com.casaclara.ui.components.Page
import br.com.casaclara.ui.components.ScreenHeader
import br.com.casaclara.ui.components.SectionTitle

private data class Entry(val emoji: String, val title: String, val sub: String, val route: String)

@Composable
fun MoreScreen(@Suppress("UNUSED_PARAMETER") c: AppContainer, nav: NavController) {
    val groups = listOf(
        "Ferramentas" to listOf(
            Entry("📦", "Mudança e aluguel", "Checklist, cronograma, documentos, imóveis", Routes.moving(0)),
            Entry("🛒", "Mercado e alimentação", "Lista, favoritos, cardápio, despensa, receitas", Routes.market(0)),
            Entry("🛋️", "Móveis e compras", "Essenciais, prioridades, comparação, parcelas", Routes.furniture(0)),
            Entry("🧑‍🤝‍🧑", "Casa compartilhada", "Moradores, despesas, reembolsos, acordos", Routes.shared(0)),
            Entry("🗓️", "Plano da semana", "Tarefas distribuídas pelos dias", Routes.WEEKPLAN),
            Entry("⏱️", "Faxina cronometrada", "Escolha um cômodo e um tempo", Routes.TIMER),
            Entry("📥", "Importar extrato", "Traga lançamentos do banco (CSV)", Routes.IMPORT),
            Entry("🧮", "Calculadoras", "Custo de vida, mudança, renda, metas, transporte", Routes.CALCULATORS),
            Entry("📇", "Contatos e emergência", "Profissionais, emergência, endereços importantes", Routes.CONTACTS),
            Entry("🔔", "Lembretes", "Contas, tarefas, metas, mudança, manutenção", Routes.REMINDERS),
        ),
        "Aprender" to listOf(
            Entry("📚", "Dicas e guias", "100 guias práticos com busca e favoritos", Routes.LIBRARY),
            Entry("🏆", "Conquistas", "O que você já conquistou de verdade", Routes.ACHIEVEMENTS),
            Entry("🔎", "Buscar no app", "Guias, contas, tarefas e registros", Routes.SEARCH),
        ),
        "Você" to listOf(
            Entry("🙂", "Perfil", "Sua situação, objetivos e jornada", Routes.PROFILE),
            Entry("🎛️", "Personalizar o Início", "Cartões, ordem e atalhos", Routes.CUSTOMIZE),
            Entry("⚙️", "Configurações", "Tema, texto, notificações, dados e backup", Routes.SETTINGS),
            Entry("⭐", "Planos", "Gratuito e o que vem por aí", Routes.PLANS),
        ),
    )
    Page {
        ScreenHeader("Mais", "Tudo o que o Casa Clara oferece, em um só lugar.", mood = Mood.ACENANDO)
        groups.forEach { (g, list) ->
            SectionTitle(g)
            list.forEach { e ->
                BrandCard(onClick = { nav.navigate(e.route) }, padding = 12.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        EmojiBadge(e.emoji, 44.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(e.title, style = MaterialTheme.typography.titleMedium)
                            Text(e.sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("›", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }
    }
}
