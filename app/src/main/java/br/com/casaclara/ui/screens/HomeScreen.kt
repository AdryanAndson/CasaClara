package br.com.casaclara.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.casaclara.data.AppContainer
import br.com.casaclara.data.UserPrefs
import br.com.casaclara.domain.Achievements
import br.com.casaclara.domain.Dates
import br.com.casaclara.domain.Finance
import br.com.casaclara.domain.Money
import br.com.casaclara.domain.Routes
import br.com.casaclara.domain.Situation
import br.com.casaclara.domain.Snapshot
import br.com.casaclara.domain.content.Tips
import br.com.casaclara.ui.asState
import br.com.casaclara.ui.billStatus
import br.com.casaclara.ui.components.BrandCard
import br.com.casaclara.ui.components.EmojiBadge
import br.com.casaclara.ui.components.ItemRow
import br.com.casaclara.ui.components.LocalFeedback
import br.com.casaclara.ui.components.Mascot
import br.com.casaclara.ui.components.MascotBubble
import br.com.casaclara.ui.components.Mood
import br.com.casaclara.ui.components.Page
import br.com.casaclara.ui.components.PrimaryButton
import br.com.casaclara.ui.components.ProgressLine
import br.com.casaclara.ui.components.SectionTitle
import br.com.casaclara.ui.components.Stat
import br.com.casaclara.ui.components.Tag
import br.com.casaclara.ui.components.Tone
import br.com.casaclara.ui.greeting
import br.com.casaclara.ui.journeyOf
import br.com.casaclara.ui.taskStatus
import br.com.casaclara.ui.theme.Brand
import kotlinx.coroutines.launch

data class Tool(val emoji: String, val label: String, val route: String)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(c: AppContainer, nav: NavController) {
    val p by c.prefs.flow.asState(UserPrefs())
    val snap by c.snapshot.asState(Snapshot())
    val tx by c.db.tx().all().asState(emptyList())
    val bills by c.db.bills().all().asState(emptyList())
    val tasks by c.db.houseTasks().all().asState(emptyList())
    val goals by c.db.goals().all().asState(emptyList())
    val unlocked by c.db.achievements().all().asState(emptyList())
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    val j = journeyOf(snap, p)
    val month = Dates.currentMonthKey()
    val sum = Finance.summary(
        tx.filter { it.kind == "ENTRADA" }.map { Finance.Movement(it.amount, it.date) },
        tx.filter { it.kind == "SAIDA" }.map { Finance.Movement(it.amount, it.date) }, month,
    )
    val planning = p.situation == Situation.PAIS || p.situation == Situation.JUNTANDO

    Page {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${greeting()}${if (p.name.isNotBlank()) ", ${p.name}" else ""}! 👋", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
                p.situation?.let { Text("Você está ${it.phrase}.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            IconButton(onClick = { nav.navigate(Routes.SEARCH) }) { Icon(Icons.Filled.Search, contentDescription = "Buscar no app") }
        }
        val msg = when {
            p.journeyPaused -> "Jornada pausada. Quando quiser voltar, é só retomar em Minha jornada. Enquanto isso, as ferramentas continuam aqui."
            sum.sobrou < 0 && sum.entrou > 0 -> "Este mês saiu mais do que entrou. Sem pânico: vamos olhar os gastos juntos e ajustar o que der."
            j.next == null -> "Você completou todos os passos da jornada! Agora é manter a casa no ritmo."
            j.doneCount <= 1 -> if (planning) "Vamos descobrir quanto custa morar sozinho?" else "Bora deixar a casa e as contas no jeito?"
            else -> "Você já fez ${j.doneCount} de ${j.total} passos. Bora pro próximo?"
        }
        MascotBubble(msg, if (j.next == null) Mood.COMEMORANDO else Mood.FELIZ)

        // Próxima tarefa recomendada
        if (!p.journeyPaused) {
            BrandCard(tone = Tone.AMARELO) {
                Text("⭐ Próximo passo recomendado", style = MaterialTheme.typography.titleMedium)
                val n = j.next
                if (n != null) {
                    Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        EmojiBadge(n.step.emoji, 52.dp, Tone.NEUTRO)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(n.step.title, style = MaterialTheme.typography.titleLarge)
                            Text(n.step.why, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    PrimaryButton("Fazer agora →", Modifier.fillMaxWidth()) { nav.navigate(n.step.route) }
                } else Text("Tudo feito por aqui! 🎉")
                Spacer(Modifier.padding(4.dp))
                ProgressLine(j.progress.toFloat(), "Progresso da jornada")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${j.doneCount} de ${j.total} passos", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    TextButton(onClick = { nav.navigate(Routes.JOURNEY) }) { Text("Ver jornada") }
                }
            }
        }
        val last = p.lastRoute
        if (last.isNotBlank() && !last.startsWith("home") && !last.startsWith("onboarding")) {
            OutlinedButton(onClick = { runCatching { nav.navigate(last) } }, modifier = Modifier.fillMaxWidth()) { Text("↩ Continuar de onde parei") }
        }

        // Resumo financeiro
        BrandCard(onClick = { nav.navigate(Routes.finance(0)) }) {
            Text("💰 ${Dates.monthLabel(month)}", style = MaterialTheme.typography.titleMedium)
            if (tx.isEmpty()) Text("Ainda não há registros. Toque para anotar sua renda e seus gastos.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            else Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Stat("Entrou", Money.format(sum.entrou), Modifier.weight(1f), Brand.VerdeEscuro)
                Stat("Saiu", Money.format(sum.saiu), Modifier.weight(1f))
                Stat("Sobrou", Money.format(sum.sobrou), Modifier.weight(1f), if (sum.sobrou < 0) MaterialTheme.colorScheme.error else Brand.VerdeEscuro)
            }
        }

        // Próximas contas e tarefas
        val proximas = bills.map { it to billStatus(it) }.filter { !it.second.paid && it.second.days <= 7 }.sortedBy { it.second.days }.take(4)
        val hoje = tasks.map { it to taskStatus(it) }.filter { it.second.days <= 0 }.sortedBy { it.second.days }.take(4)
        val contasBlock = @Composable {
            SectionTitle("📅 Próximas contas")
            if (proximas.isEmpty()) Text(if (bills.isEmpty()) "Nenhuma conta cadastrada ainda." else "Nada vencendo nos próximos 7 dias. 😌", color = MaterialTheme.colorScheme.onSurfaceVariant)
            proximas.forEach { (b, s) ->
                ItemRow(
                    title = b.name,
                    subtitle = if (s.late) "Venceu ${Dates.relative(s.days)}" else "Vence ${Dates.relative(s.days)} (${Dates.br(s.due.toEpochDay())})",
                    amount = b.amount?.let { Money.format(it) },
                    tags = if (s.late) { { Tag("atrasada", Tone.CORAL) } } else null,
                    extra = { TextButton(onClick = { scope.launch { c.db.bills().upsert(b.copy(paidMonth = month)); fb.celebrate("Boa! Mais uma conta organizada.") } }) { Text("Paguei") } },
                )
            }
        }
        val tarefasBlock = @Composable {
            SectionTitle("🧹 Para hoje na casa")
            if (hoje.isEmpty()) Text(if (tasks.isEmpty()) "Monte sua rotina em Casa." else "Nada pendente. Casa em dia! ✨", color = MaterialTheme.colorScheme.onSurfaceVariant)
            hoje.forEach { (t, s) ->
                ItemRow(
                    title = t.title,
                    subtitle = if (s.never) "Ainda não registrada" else if (s.late) "Atrasada — sem culpa, é só fazer quando der" else "Hoje",
                    extra = { TextButton(onClick = { scope.launch { c.db.houseTasks().markDone(t.id, Dates.todayEpoch()); fb.toast("Feito! Sua casa agradece.", Mood.COMEMORANDO) } }) { Text("Feito") } },
                )
            }
        }
        // Quem mora sozinho vê primeiro rotina e contas; quem planeja vê primeiro as metas.
        if (planning) {
            GoalsBlock(goals, nav)
            contasBlock()
            tarefasBlock()
        } else {
            contasBlock()
            tarefasBlock()
            GoalsBlock(goals, nav)
        }

        // Dica do dia
        BrandCard(tone = Tone.VERDE) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Mascot(Mood.DICA, 56.dp)
                Spacer(Modifier.width(8.dp))
                Column { Text("Dica do dia", style = MaterialTheme.typography.titleSmall); Text(Tips.ofDay(Dates.todayEpoch()), fontWeight = FontWeight.SemiBold) }
            }
        }

        // Acesso rápido (ordem muda conforme a situação)
        SectionTitle("Ferramentas")
        val planningTools = listOf(
            Tool("🧮", "Calculadoras", Routes.CALCULATORS), Tool("🎯", "Metas", Routes.finance(Routes.FinanceTab.METAS)),
            Tool("📦", "Mudança", Routes.moving(0)), Tool("🔎", "Imóveis", Routes.moving(Routes.MovingTab.IMOVEIS)),
        )
        val routineTools = listOf(
            Tool("📅", "Contas", Routes.finance(Routes.FinanceTab.CONTAS)), Tool("🧹", "Tarefas", Routes.house(0)),
            Tool("🛒", "Mercado", Routes.market(0)), Tool("🔧", "Manutenção", Routes.CONTACTS),
        )
        val common = listOf(Tool("📚", "Guias", Routes.LIBRARY), Tool("🔔", "Lembretes", Routes.REMINDERS), Tool("🛋️", "Móveis", Routes.furniture(0)), Tool("🧑‍🤝‍🧑", "Casa dividida", Routes.shared(0)))
        val tools = (if (planning) planningTools + routineTools else routineTools + planningTools) + common
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tools.forEach { t ->
                BrandCard(Modifier.widthIn(min = 100.dp, max = 160.dp).width(104.dp), onClick = { nav.navigate(t.route) }, padding = 10.dp) {
                    Text(t.emoji, style = MaterialTheme.typography.headlineSmall)
                    Text(t.label, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        // Conquistas
        SectionTitle("🏆 Conquistas") { TextButton(onClick = { nav.navigate(Routes.ACHIEVEMENTS) }) { Text("Ver todas") } }
        if (unlocked.isEmpty()) Text("Suas conquistas aparecem aqui conforme você usa as ferramentas de verdade.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        else FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            unlocked.take(6).forEach { u -> Achievements.ALL.firstOrNull { it.id == u.id }?.let { Tag("${it.emoji} ${it.title}", Tone.AMARELO) } }
        }
    }
}

@Composable
private fun GoalsBlock(goals: List<br.com.casaclara.data.GoalEntity>, nav: NavController) {
    SectionTitle("🎯 Metas de economia") { TextButton(onClick = { nav.navigate(Routes.finance(Routes.FinanceTab.METAS)) }) { Text("Abrir") } }
    if (goals.isEmpty()) Text("Crie uma meta — como a reserva de emergência — em Finanças.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    goals.take(3).forEach { g ->
        val pr = if (g.target > 0) g.saved.toFloat() / g.target else 0f
        Column {
            Row { Text(g.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text("${(pr.coerceAtMost(1f) * 100).toInt()}%") }
            ProgressLine(pr, "Progresso de ${g.name}", if (g.type == "RESERVA") Brand.Azul else Brand.Verde)
            Text("${Money.format(g.saved)} de ${Money.format(g.target)}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
