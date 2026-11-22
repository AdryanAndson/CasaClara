package br.com.casaclara.ui.screens

import br.com.casaclara.data.settings

import androidx.compose.material.icons.filled.VisibilityOff

import androidx.compose.material.icons.filled.Visibility

import androidx.compose.ui.unit.sp

import androidx.compose.foundation.layout.fillMaxHeight

import androidx.compose.foundation.layout.IntrinsicSize

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.casaclara.data.AppContainer
import br.com.casaclara.data.BillEntity
import br.com.casaclara.data.GoalEntity
import br.com.casaclara.data.HouseTaskEntity
import br.com.casaclara.data.ShopEntity
import br.com.casaclara.data.TxEntity
import br.com.casaclara.data.UserPrefs
import br.com.casaclara.domain.Achievements
import br.com.casaclara.domain.Dates
import br.com.casaclara.domain.Finance
import br.com.casaclara.domain.JourneyState
import br.com.casaclara.domain.Money
import br.com.casaclara.domain.Routes
import br.com.casaclara.domain.Situation
import br.com.casaclara.domain.Snapshot
import br.com.casaclara.domain.Weekly
import br.com.casaclara.domain.content.Tips
import br.com.casaclara.ui.HomeCatalog
import br.com.casaclara.ui.Tools
import br.com.casaclara.ui.asState
import br.com.casaclara.ui.billStatus
import br.com.casaclara.ui.components.AccentButton
import br.com.casaclara.ui.components.BrandCard
import br.com.casaclara.ui.components.Donut
import br.com.casaclara.ui.components.EmojiBadge
import br.com.casaclara.ui.components.ItemRow
import br.com.casaclara.ui.components.LocalFeedback
import br.com.casaclara.ui.components.Mascot
import br.com.casaclara.ui.components.Mood
import br.com.casaclara.ui.components.Page
import br.com.casaclara.ui.components.PrimaryButton
import br.com.casaclara.ui.components.ProgressLine
import br.com.casaclara.ui.components.Stat
import br.com.casaclara.ui.components.Tag
import br.com.casaclara.ui.components.Tone
import br.com.casaclara.ui.components.rememberReduceMotion
import br.com.casaclara.ui.greeting
import br.com.casaclara.ui.journeyOf
import br.com.casaclara.ui.shareText
import br.com.casaclara.ui.taskStatus
import br.com.casaclara.ui.theme.Brand
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Dados que os cartões do Início usam (lidos uma vez e compartilhados). */
private class HomeData(
    val p: UserPrefs, val j: JourneyState, val tx: List<TxEntity>, val bills: List<BillEntity>, val tasks: List<HouseTaskEntity>,
    val goals: List<GoalEntity>, val shop: List<ShopEntity>, val unlocked: List<String>, val doneThisWeek: Int,
)

@Composable
fun HomeScreen(c: AppContainer, nav: NavController) {
    val p by c.prefs.flow.asState(UserPrefs())
    val snap by c.snapshot.asState(Snapshot())
    val tx by c.db.tx().all().asState(emptyList())
    val bills by c.db.bills().all().asState(emptyList())
    val tasks by c.db.houseTasks().all().asState(emptyList())
    val goals by c.db.goals().all().asState(emptyList())
    val shop by c.db.shop().byList("MERCADO").asState(emptyList())
    val unlocked by c.db.achievements().all().asState(emptyList())
    val weekStart = Weekly.weekStart(Dates.today()).toEpochDay()
    val log by remember(weekStart) { c.db.taskLog().since(weekStart) }.asState(emptyList(), weekStart)
    val planning = p.situation == Situation.PAIS || p.situation == Situation.JUNTANDO
    val d = HomeData(p, journeyOf(snap, p), tx, bills, tasks, goals, shop, unlocked.map { it.id }, log.size)
    val simple = p.settings.simpleMode
    val order = HomeCatalog.resolve(p.homeOrder, planning).filter { it !in p.homeHidden }
        .let { o -> if (simple) o.filter { it in setOf("proximo", "contas", "tarefas", "mercado", "atalhos") } else o }

    // entrada em cascata dos cartões (desligada se o sistema pede menos movimento)
    val reduce = rememberReduceMotion()
    var shown by remember { mutableIntStateOf(if (reduce) 99 else 0) }
    LaunchedEffect(Unit) { if (!reduce) while (shown < 20) { delay(70); shown++ } }

    Page {
        HeroWithC(c, d, nav)
        order.forEachIndexed { i, id ->
            AnimatedVisibility(visible = i < shown, enter = fadeIn() + slideInVertically { it / 3 }) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    when (id) {
                        "proximo" -> NextStepCard(d, nav)
                        "semana" -> WeekCard(d, nav)
                        "mes" -> MonthCard(d, nav)
                        "contas" -> BillsCard(c, d, nav)
                        "tarefas" -> TasksCard(c, d, nav)
                        "metas" -> GoalsCard(d, nav)
                        "mercado" -> MarketCard(d, nav)
                        "atalhos" -> ShortcutsCard(d, nav, planning)
                        "dica" -> TipCard()
                        "conquistas" -> AchievementsCard(d, nav)
                    }
                }
            }
        }
        TextButton(onClick = { nav.navigate(Routes.CUSTOMIZE) }, modifier = Modifier.fillMaxWidth()) { Text("🎛️ Personalizar o Início") }
    }
}

private val LocalAppContainer = androidx.compose.runtime.staticCompositionLocalOf<br.com.casaclara.data.AppContainer?> { null }

@Composable
private fun HeroWithC(c: br.com.casaclara.data.AppContainer, d: HomeData, nav: NavController) {
    androidx.compose.runtime.CompositionLocalProvider(LocalAppContainer provides c) { Hero(d, nav) }
}

@Composable
private fun Hero(d: HomeData, nav: NavController) {
    val month = Dates.currentMonthKey()
    val sum = Finance.summary(d.tx.filter { it.kind == "ENTRADA" }.map { Finance.Movement(it.amount, it.date) }, d.tx.filter { it.kind == "SAIDA" }.map { Finance.Movement(it.amount, it.date) }, month)
    val msg = when {
        d.p.journeyPaused -> "Jornada pausada. As ferramentas continuam aqui quando você precisar."
        sum.sobrou < 0 && sum.entrou > 0 -> "Este mês saiu mais do que entrou. Sem pânico: bora olhar os gastos juntos."
        d.j.next == null -> "Jornada completa! Agora é manter a casa no ritmo. 🎉"
        d.j.doneCount <= 1 -> if (d.p.situation == Situation.PAIS || d.p.situation == Situation.JUNTANDO) "Vamos descobrir quanto custa morar sozinho?" else "Bora deixar a casa e as contas no jeito?"
        else -> "Você já fez ${d.j.doneCount} de ${d.j.total} passos. Bora pro próximo?"
    }
    BrandCard(Modifier.padding(top = 8.dp), tone = Tone.AMARELO, padding = 14.dp) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text("${greeting()}${if (d.p.name.isNotBlank()) ", ${d.p.name}" else ""}! 👋", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
                d.p.situation?.let { Text("Você está ${it.phrase}.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) }
            }
            val hidden = br.com.casaclara.ui.UiFlags.hideValues.value
            val scopeH = rememberCoroutineScope()
            val appC = LocalAppContainer.current
            IconButton(onClick = { scopeH.launch { appC?.prefs?.setSetting("hideValues", !hidden) } }) {
                Icon(if (hidden) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = if (hidden) "Mostrar valores" else "Esconder valores")
            }
            IconButton(onClick = { nav.navigate(Routes.SEARCH) }) { Icon(Icons.Filled.Search, contentDescription = "Buscar no app") }
            IconButton(onClick = { nav.navigate(Routes.CUSTOMIZE) }) { Icon(Icons.Filled.Tune, contentDescription = "Personalizar o Início") }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
            Mascot(if (d.j.next == null) Mood.COMEMORANDO else Mood.ACENANDO, 96.dp)
            Spacer(Modifier.width(8.dp))
            Text(msg, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        }
        val last = d.p.lastRoute
        if (last.isNotBlank() && !last.startsWith("home") && !last.startsWith("onboarding")) {
            TextButton(onClick = { runCatching { nav.navigate(last) } }) { Text("↩ Continuar de onde parei") }
        }
    }
}

@Composable
private fun CardTitle(emoji: String, title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("$emoji  $title", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f).semantics { heading() })
        if (action != null && onAction != null) TextButton(onClick = onAction) { Text(action) }
    }
}

@Composable
private fun NextStepCard(d: HomeData, nav: NavController) {
    if (d.p.journeyPaused) return
    BrandCard(tone = Tone.VERDE) {
        CardTitle("⭐", "Próximo passo")
        val n = d.j.next
        if (n != null) {
            Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                EmojiBadge(n.step.emoji, 56.dp, Tone.AMARELO)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(n.step.title, style = MaterialTheme.typography.titleLarge)
                    Text(n.step.why, style = MaterialTheme.typography.bodyMedium)
                }
            }
            PrimaryButton("Fazer agora →", Modifier.fillMaxWidth()) { nav.navigate(n.step.route) }
        } else Text("Tudo feito por aqui! 🎉")
        Spacer(Modifier.height(8.dp))
        ProgressLine(d.j.progress.toFloat(), "Progresso da jornada")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${d.j.doneCount} de ${d.j.total} passos", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            TextButton(onClick = { nav.navigate(Routes.JOURNEY) }) { Text("Ver jornada") }
        }
    }
}

@Composable
private fun WeekCard(d: HomeData, nav: NavController) {
    val today = Dates.today()
    val s = Weekly.summary(d.tx.filter { it.kind == "SAIDA" }.map { Finance.Movement(it.amount, it.date) }, d.tx.filter { it.kind == "ENTRADA" }.map { Finance.Movement(it.amount, it.date) }, today)
    val ws = Weekly.weekStart(today)
    val billsWeek = d.bills.count { val st = billStatus(it); !st.paid && !st.due.isBefore(ws) && !st.due.isAfter(ws.plusDays(6)) }
    BrandCard(tone = Tone.AZUL) {
        CardTitle("📆", "Esta semana", "Plano da semana") { nav.navigate(Routes.WEEKPLAN) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Stat("Gastei", Money.format(s.spent), Modifier.weight(1f))
            Stat("Tarefas feitas", "${d.doneThisWeek}", Modifier.weight(1f), Brand.VerdeEscuro)
        }
        val comp = when {
            s.spentLastWeek == 0L && s.spent == 0L -> "Nenhum gasto anotado nesta semana nem na passada."
            s.spentLastWeek == 0L -> "Semana passada não teve gasto anotado."
            s.diff > 0 -> "▲ ${Money.format(s.diff)} a mais que na semana passada."
            s.diff < 0 -> "▼ ${Money.format(-s.diff)} a menos que na semana passada. 👏"
            else -> "Igual à semana passada."
        }
        Text(comp, fontWeight = FontWeight.Bold)
        Text(if (billsWeek > 0) "📅 $billsWeek conta(s) vencem nesta semana." else "📅 Nenhuma conta vence nesta semana.", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun MonthCard(d: HomeData, nav: NavController) {
    val month = Dates.currentMonthKey()
    val ent = d.tx.filter { it.kind == "ENTRADA" }.map { Finance.Movement(it.amount, it.date, it.category) }
    val sai = d.tx.filter { it.kind == "SAIDA" }.map { Finance.Movement(it.amount, it.date, it.category) }
    val sum = Finance.summary(ent, sai, month)
    val ym = Dates.yearMonth(month)
    val cats = Finance.byCategory(sai, ym.atDay(1).toEpochDay(), ym.atEndOfMonth().toEpochDay())
    BrandCard(onClick = { nav.navigate(Routes.finance(0)) }) {
        CardTitle("🍩", Dates.monthLabel(month))
        if (d.tx.isEmpty()) {
            Text("Ainda não há registros. Anote sua renda e seus gastos para ver o gráfico.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            AccentButton("Registrar agora", Modifier.fillMaxWidth()) { nav.navigate(Routes.finance(Routes.FinanceTab.LANCAMENTOS)) }
            return@BrandCard
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Stat("Entrou", Money.format(sum.entrou), Modifier.weight(1f), Brand.VerdeEscuro)
            Stat("Saiu", Money.format(sum.saiu), Modifier.weight(1f))
        }
        Stat("Sobrou", Money.format(sum.sobrou), color = if (sum.sobrou < 0) MaterialTheme.colorScheme.error else Brand.VerdeEscuro)
        if (cats.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Donut(cats, Money::format, Money.format(sum.saiu), "gastos")
        }
    }
}

@Composable
private fun BillsCard(c: AppContainer, d: HomeData, nav: NavController) {
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    val month = Dates.currentMonthKey()
    val proximas = d.bills.map { it to billStatus(it) }.filter { !it.second.paid && it.second.days <= 7 }.sortedBy { it.second.days }.take(4)
    BrandCard {
        CardTitle("📅", "Próximas contas", "Todas") { nav.navigate(Routes.finance(Routes.FinanceTab.CONTAS)) }
        if (proximas.isEmpty()) Text(if (d.bills.isEmpty()) "Nenhuma conta cadastrada ainda." else "Nada vencendo nos próximos 7 dias. 😌", color = MaterialTheme.colorScheme.onSurfaceVariant)
        proximas.forEach { (b, s) ->
            ItemRow(
                title = b.name,
                subtitle = if (s.late) "Venceu ${Dates.relative(s.days)}" else "Vence ${Dates.relative(s.days)} (${Dates.br(s.due.toEpochDay())})",
                amount = b.amount?.let { Money.format(it) },
                tags = if (s.late) { { Tag("atrasada", Tone.CORAL) } } else null,
                extra = { TextButton(onClick = { scope.launch { c.db.bills().upsert(b.copy(paidMonth = month)) }; fb.celebrate("Boa! Mais uma conta organizada.") }) { Text("Paguei") } },
            )
        }
    }
}

@Composable
private fun TasksCard(c: AppContainer, d: HomeData, nav: NavController) {
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    val hoje = d.tasks.map { it to taskStatus(it) }.filter { it.second.days <= 0 }.sortedBy { it.second.days }.take(4)
    BrandCard {
        CardTitle("🧹", "Para hoje na casa", "⏱️ Faxina") { nav.navigate(Routes.TIMER) }
        if (hoje.isEmpty()) Text(if (d.tasks.isEmpty()) "Monte sua rotina em Casa." else "Nada pendente. Casa em dia! ✨", color = MaterialTheme.colorScheme.onSurfaceVariant)
        hoje.forEach { (t, s) ->
            ItemRow(
                title = t.title,
                subtitle = if (s.never) "Ainda não registrada" else if (s.late) "Atrasada — sem culpa, é só fazer quando der" else "Hoje",
                extra = { TextButton(onClick = { scope.launch { c.markTaskDone(t) }; fb.toast("Feito! Sua casa agradece.", Mood.COMEMORANDO) }) { Text("Feito") } },
            )
        }
    }
}

@Composable
private fun GoalsCard(d: HomeData, nav: NavController) {
    BrandCard(tone = Tone.ROXO) {
        CardTitle("🐷", "Cofrinhos", "Abrir") { nav.navigate(Routes.finance(Routes.FinanceTab.METAS)) }
        if (d.goals.isEmpty()) {
            Text("Crie um cofrinho — como a reserva de emergência. Qualquer valor conta.")
            AccentButton("Criar cofrinho", Modifier.fillMaxWidth()) { nav.navigate(Routes.finance(Routes.FinanceTab.METAS)) }
        }
        d.goals.take(3).forEach { g ->
            val pr = if (g.target > 0) g.saved.toFloat() / g.target else 0f
            Column(Modifier.padding(vertical = 4.dp)) {
                Row { Text(g.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text("${(pr.coerceAtMost(1f) * 100).toInt()}%", fontWeight = FontWeight.Black) }
                ProgressLine(pr, "Progresso de ${g.name}", if (g.type == "RESERVA") Brand.Azul else Brand.Verde)
                Text("${Money.format(g.saved)} de ${Money.format(g.target)}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun MarketCard(d: HomeData, nav: NavController) {
    val ctx = LocalContext.current
    val pend = d.shop.filter { !it.bought }
    BrandCard(tone = Tone.VERDE) {
        CardTitle("🛒", "Lista de mercado", "Abrir") { nav.navigate(Routes.market(0)) }
        if (pend.isEmpty()) Text("Nada pendente na lista.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        else {
            Text(pend.take(6).joinToString(" · ") { it.name } + if (pend.size > 6) " e mais ${pend.size - 6}" else "", fontWeight = FontWeight.SemiBold)
            TextButton(onClick = { shareText(ctx, "Lista de mercado", br.com.casaclara.ui.marketListText(d.shop)) }) { Text("📤 Mandar no WhatsApp") }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShortcutsCard(d: HomeData, nav: NavController, planning: Boolean) {
    val ids = d.p.shortcuts.ifEmpty { Tools.defaults(planning) }
    val tools = ids.mapNotNull { id -> Tools.ALL.firstOrNull { it.id == id } }
    BrandCard {
        CardTitle("🚀", "Atalhos", "Editar") { nav.navigate(Routes.CUSTOMIZE) }
        // grade de 3 colunas com a mesma largura; palavras longas diminuem a letra em vez de quebrar no meio
        tools.chunked(3).forEach { rowTools ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                rowTools.forEach { t ->
                    val longWord = t.label.split(' ').maxOf { it.length }
                    BrandCard(Modifier.weight(1f).fillMaxHeight(), tone = Tone.AMARELO, onClick = { nav.navigate(t.route) }, padding = 8.dp) {
                        Text(t.emoji, style = MaterialTheme.typography.headlineSmall)
                        Text(t.label, style = MaterialTheme.typography.labelMedium, fontSize = if (longWord > 10) 11.sp else if (longWord > 8) 12.sp else 13.sp, maxLines = 2, lineHeight = 15.sp)
                    }
                }
                repeat(3 - rowTools.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun TipCard() {
    BrandCard(tone = Tone.CORAL) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Mascot(Mood.DICA, 72.dp)
            Spacer(Modifier.width(8.dp))
            Column { Text("Dica do dia", style = MaterialTheme.typography.titleLarge); Text(Tips.ofDay(Dates.todayEpoch()), fontWeight = FontWeight.SemiBold) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AchievementsCard(d: HomeData, nav: NavController) {
    BrandCard {
        CardTitle("🏆", "Conquistas (${d.unlocked.size}/${Achievements.ALL.size})", "Ver todas") { nav.navigate(Routes.ACHIEVEMENTS) }
        if (d.unlocked.isEmpty()) Text("Suas conquistas aparecem aqui conforme você usa as ferramentas de verdade.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        else FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            d.unlocked.take(6).forEach { id -> Achievements.ALL.firstOrNull { it.id == id }?.let { Tag("${it.emoji} ${it.title}", Tone.AMARELO) } }
        }
    }
}
