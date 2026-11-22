package br.com.casaclara.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.casaclara.data.AppContainer
import br.com.casaclara.data.UserPrefs
import br.com.casaclara.domain.Dates
import br.com.casaclara.domain.Money
import br.com.casaclara.domain.Routes
import br.com.casaclara.domain.WeekPlan
import br.com.casaclara.domain.Weekly
import br.com.casaclara.notifications.ReminderScheduler
import br.com.casaclara.ui.FREQ_DAYS
import br.com.casaclara.ui.WEEKDAYS
import br.com.casaclara.ui.asState
import br.com.casaclara.ui.components.AlertBox
import br.com.casaclara.ui.components.BrandCard
import br.com.casaclara.ui.components.EmptyState
import br.com.casaclara.ui.components.Ill
import br.com.casaclara.ui.components.ItemRow
import br.com.casaclara.ui.components.LocalFeedback
import br.com.casaclara.ui.components.Mascot
import br.com.casaclara.ui.components.Mood
import br.com.casaclara.ui.components.Page
import br.com.casaclara.ui.components.PrimaryButton
import br.com.casaclara.ui.components.ScreenHeader
import br.com.casaclara.ui.components.SecondaryButton
import br.com.casaclara.ui.components.SectionTitle
import br.com.casaclara.ui.components.SelectField
import br.com.casaclara.ui.components.Tag
import br.com.casaclara.ui.components.Tone
import br.com.casaclara.ui.shareText
import br.com.casaclara.ui.billStatus
import br.com.casaclara.ui.theme.Brand
import br.com.casaclara.ui.theme.LocalSoft
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ------------------------------------------------------------------ Plano da semana
@Composable
fun WeekPlanScreen(c: AppContainer, nav: NavController) {
    val tasks by c.db.houseTasks().all().asState(emptyList())
    val bills by c.db.bills().all().asState(emptyList())
    val milestones by c.db.milestones().all().asState(emptyList())
    val today = Dates.today()
    val ws = Weekly.weekStart(today)
    val log by remember(ws) { c.db.taskLog().since(ws.toEpochDay()) }.asState(emptyList())
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    val ctx = LocalContext.current
    val plan = WeekPlan.plan(tasks.map { WeekPlan.Task(it.id, (FREQ_DAYS[it.frequency] ?: 7L).toInt(), it.lastDone?.let(Dates::of)) }, ws, today)
    val byId = tasks.associateBy { it.id }
    val todayIdx = (today.dayOfWeek.value - 1)
    Page(nav, "Plano da semana") {
        ScreenHeader("Plano da semana", "Suas tarefas distribuídas pelos dias, sem sobrecarregar nenhum. Atrasou? Ele se reorganiza sozinho.", ill = Ill.CALENDARIO, tone = Tone.AZUL)
        if (tasks.isEmpty()) {
            EmptyState("Sem tarefas para planejar", "Crie sua rotina em Casa → Tarefas (tem uma rotina sugerida pronta).", Mood.PENSANDO, "Ir para Tarefas", { nav.navigate(Routes.house(Routes.HouseTab.TAREFAS)) })
            return@Page
        }
        (0..6).forEach { i ->
            val day = ws.plusDays(i.toLong())
            val ids = plan[i].orEmpty()
            val doneToday = log.filter { it.date == day.toEpochDay() }.map { it.taskId }.toSet()
            val billsDay = bills.filter { billStatus(it).due == day && !billStatus(it).paid }
            val miles = milestones.filter { it.date == day.toEpochDay() }
            if (i < todayIdx && ids.isEmpty() && doneToday.isEmpty()) return@forEach
            BrandCard(tone = if (i == todayIdx) Tone.AMARELO else Tone.NEUTRO, padding = 12.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${WEEKDAYS[i]} ${day.dayOfMonth}/${day.monthValue}", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    if (i == todayIdx) Tag("hoje", Tone.CORAL)
                    Tag("${ids.size} tarefa(s)")
                }
                if (ids.isEmpty() && billsDay.isEmpty() && miles.isEmpty()) Text("Dia livre. 😌", color = MaterialTheme.colorScheme.onSurfaceVariant)
                ids.mapNotNull { byId[it] }.forEach { t ->
                    val done = t.id in doneToday
                    ItemRow(
                        title = t.title, subtitle = t.room, checked = done,
                        onCheck = if (i == todayIdx && !done) { _ -> scope.launch { c.markTaskDone(t) }; fb.toast("Feito! Sua casa agradece.", Mood.COMEMORANDO) } else null,
                    )
                }
                billsDay.forEach { Text("📅 Vence: ${it.name}${it.amount?.let { v -> " (${Money.format(v)})" } ?: ""}", fontWeight = FontWeight.Bold) }
                miles.forEach { Text("📦 ${it.title}", fontWeight = FontWeight.Bold) }
            }
        }
        SecondaryButton("📤 Compartilhar o plano", Modifier.fillMaxWidth()) {
            val txt = buildString {
                appendLine("🗓️ Plano da semana da casa")
                (todayIdx..6).forEach { i ->
                    val ids = plan[i].orEmpty(); if (ids.isEmpty()) return@forEach
                    appendLine(); appendLine("*${WEEKDAYS[i]}*")
                    ids.mapNotNull { byId[it] }.forEach { appendLine("☐ ${it.title}") }
                }
                appendLine(); append("Enviado pelo app Casa Clara")
            }
            shareText(ctx, "Plano da semana", txt)
        }
        AlertBox("As diárias aparecem todo dia. As outras entram no dia em que vencem — e, se o dia estiver cheio, vão para o dia mais leve ao lado.", "DICA")
    }
}

// ------------------------------------------------------------------ Faxina cronometrada
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimerScreen(c: AppContainer, nav: NavController) {
    val prefs by c.prefs.flow.asState(UserPrefs())
    val checks by c.db.checks().byList("CLEANING").asState(emptyList())
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    val rooms = (checks.map { it.grp }.distinct() + "Casa toda").distinct()
    var room by rememberSaveable { mutableStateOf("") }
    var minutes by rememberSaveable { mutableStateOf(0) }
    var endAt by rememberSaveable { mutableLongStateOf(0L) }     // > 0: rodando
    var pausedLeft by rememberSaveable { mutableLongStateOf(0L) } // > 0: pausado
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val mins = if (minutes > 0) minutes else prefs.cleanMinutes
    val selRoom = room.ifBlank { rooms.first() }
    val total = mins * 60_000L
    val left = when { endAt > 0 -> (endAt - now).coerceAtLeast(0); pausedLeft > 0 -> pausedLeft; else -> total }
    val running = endAt > 0
    LaunchedEffect(running) { while (endAt > 0) { now = System.currentTimeMillis(); if (endAt - now <= 0) break; delay(250) } }
    LaunchedEffect(left, running) {
        if (running && left == 0L) {
            endAt = 0; pausedLeft = 0
            ReminderScheduler.cancelTimer(ctx)
            fb.celebrate("Faxina concluída! Você mandou bem. ✨")
        }
    }
    Page(nav, "Faxina cronometrada") {
        ScreenHeader("Faxina com tempo marcado", "Escolha um cômodo e um tempo. Quando acabar, acabou — sem culpa pelo resto.", ill = Ill.CRONOMETRO, tone = Tone.CORAL)
        if (!running && pausedLeft == 0L) {
            SelectField("Cômodo", selRoom, rooms.map { it to it }) { room = it }
            Text("Tempo", fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(10, 15, 25, 45).forEach { m -> FilterChip(mins == m, { minutes = m; scope.launch { c.prefs.update { it.copy(cleanMinutes = m) } } }, { Text("$m min") }) }
            }
        }
        // relógio caricato
        val soft = LocalSoft.current
        val frac = if (total > 0) left.toFloat() / total else 0f
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(240.dp)) {
                val sw = 26f
                drawCircle(soft.amarelo, size.minDimension / 2 - 2f)
                drawArc(Brand.Verde, -90f, 360f * frac, false, Offset(sw, sw), Size(size.width - sw * 2, size.height - sw * 2), style = Stroke(sw, cap = StrokeCap.Round))
                drawCircle(soft.contorno, size.minDimension / 2 - 2f, style = Stroke(6f))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Mascot(if (running) Mood.FELIZ else if (pausedLeft > 0) Mood.TRANQUILA else Mood.ACENANDO, 90.dp)
                val s = (left / 1000).toInt()
                Text("%02d:%02d".format(s / 60, s % 60), style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite; contentDescription = "Faltam ${s / 60} minutos e ${s % 60} segundos" })
            }
        }
        when {
            running -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SecondaryButton("⏸ Pausar", Modifier.weight(1f)) { pausedLeft = (endAt - System.currentTimeMillis()).coerceAtLeast(1); endAt = 0; ReminderScheduler.cancelTimer(ctx) }
                SecondaryButton("⏹ Parar", Modifier.weight(1f)) { endAt = 0; pausedLeft = 0; ReminderScheduler.cancelTimer(ctx); fb.toast("Parou. O que deu pra fazer já valeu!", Mood.TRANQUILA) }
            }
            pausedLeft > 0 -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PrimaryButton("▶ Continuar", Modifier.weight(1f)) { endAt = System.currentTimeMillis() + pausedLeft; now = System.currentTimeMillis(); ReminderScheduler.scheduleTimerDone(ctx, pausedLeft, selRoom); pausedLeft = 0 }
                SecondaryButton("⏹ Parar", Modifier.weight(1f)) { pausedLeft = 0 }
            }
            else -> PrimaryButton("▶ Começar $mins minutos", Modifier.fillMaxWidth()) {
                endAt = System.currentTimeMillis() + total; now = System.currentTimeMillis()
                ReminderScheduler.scheduleTimerDone(ctx, total, selRoom)
                fb.toast("Bora! Eu aviso quando acabar.", Mood.FELIZ)
            }
        }
        val items = checks.filter { it.grp == selRoom }
        if (items.isNotEmpty()) {
            SectionTitle("Checklist: $selRoom (${items.count { it.done }}/${items.size})")
            items.forEach { e -> ItemRow(e.title, checked = e.done, onCheck = { v -> scope.launch { c.db.checks().upsert(e.copy(done = v)) } }) }
        } else if (selRoom != "Casa toda") Unit
        else AlertBox("Dica: carregue a lista de limpeza por cômodo em Casa → Limpeza por cômodo para ter um checklist aqui.", "DICA")
        AlertBox("Se você sair do app, eu aviso por notificação quando o tempo acabar.", "INFO")
    }
}
