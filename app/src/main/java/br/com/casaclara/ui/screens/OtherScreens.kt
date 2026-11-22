package br.com.casaclara.ui.screens

import androidx.compose.foundation.layout.padding

import br.com.casaclara.ui.theme.LocalSoft

import androidx.compose.foundation.layout.size

import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.border

import androidx.compose.foundation.background

import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.layout.FlowRow

import br.com.casaclara.data.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.casaclara.data.AppContainer
import br.com.casaclara.data.ContactEntity
import br.com.casaclara.data.ReminderEntity
import br.com.casaclara.data.ThemeMode
import br.com.casaclara.data.UserPrefs
import br.com.casaclara.domain.Achievements
import br.com.casaclara.domain.Dates
import br.com.casaclara.domain.MainGoal
import br.com.casaclara.domain.Money
import br.com.casaclara.domain.Routes
import br.com.casaclara.domain.Situation
import br.com.casaclara.domain.content.Library
import br.com.casaclara.notifications.ReminderScheduler
import br.com.casaclara.premium.BillingGateway
import br.com.casaclara.premium.Plan
import br.com.casaclara.premium.Plans
import br.com.casaclara.ui.asState
import br.com.casaclara.ui.components.Ill
import br.com.casaclara.ui.components.AlertBox
import br.com.casaclara.ui.components.BrandCard
import br.com.casaclara.ui.components.CrudList
import br.com.casaclara.ui.components.EmptySpec
import br.com.casaclara.ui.components.EmptyState
import br.com.casaclara.ui.components.FieldSpec
import br.com.casaclara.ui.components.FieldType
import br.com.casaclara.ui.components.Init
import br.com.casaclara.ui.components.ItemRow
import br.com.casaclara.ui.components.LocalConfirm
import br.com.casaclara.ui.components.LocalFeedback
import br.com.casaclara.ui.components.Mascot
import br.com.casaclara.ui.components.MascotBubble
import br.com.casaclara.ui.components.MoneyInput
import br.com.casaclara.ui.components.Mood
import br.com.casaclara.ui.components.Page
import br.com.casaclara.ui.components.PrimaryButton
import br.com.casaclara.ui.components.RowSpec
import br.com.casaclara.ui.components.ScreenHeader
import br.com.casaclara.ui.components.SecondaryButton
import br.com.casaclara.ui.components.SectionTitle
import br.com.casaclara.ui.components.SelectField
import br.com.casaclara.ui.components.Tag
import br.com.casaclara.ui.components.Tone
import br.com.casaclara.ui.components.opts
import br.com.casaclara.ui.billStatus
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date
import kotlin.system.exitProcess

// ------------------------------------------------------------------ Conquistas
@Composable
fun AchievementsScreen(c: AppContainer, nav: NavController) {
    val unlocked by c.db.achievements().all().asState(emptyList())
    val map = unlocked.associateBy { it.id }
    Page(nav, "Conquistas") {
        ScreenHeader("Conquistas", "${unlocked.size} de ${Achievements.ALL.size}. Cada uma vem de uma ação real — nada de pontos que imitam dinheiro.", mood = Mood.COMEMORANDO)
        SectionTitle("Histórico")
        if (unlocked.isEmpty()) Text("Ainda nenhuma. Use as ferramentas e elas aparecem aqui.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        unlocked.forEach { u ->
            val a = Achievements.ALL.firstOrNull { it.id == u.id } ?: return@forEach
            ItemRow("${a.emoji} ${a.title}", "${a.description} · ${DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(u.unlockedAt))}", tags = { Tag("desbloqueada", Tone.VERDE) })
        }
        SectionTitle("Para conquistar")
        Achievements.ALL.filter { it.id !in map }.forEach { a -> ItemRow("🔒 ${a.title}", a.description) }
    }
}

// ------------------------------------------------------------------ Lembretes
private val REMINDER_KINDS = listOf("CONTA" to "Conta", "TAREFA" to "Tarefa da casa", "META" to "Meta de economia", "MUDANCA" to "Mudança", "MANUTENCAO" to "Manutenção", "OUTRO" to "Outro")
private val REPEATS = listOf("NENHUM" to "Não repete", "DIARIO" to "Todo dia", "SEMANAL" to "Toda semana", "MENSAL" to "Todo mês")

@Composable
fun RemindersScreen(c: AppContainer, nav: NavController) {
    val items by c.db.reminders().all().asState(emptyList())
    val prefs by c.prefs.flow.asState(UserPrefs())
    val defMinute = prefs.settings.reminderMinute
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    var granted by remember { mutableStateOf(ReminderScheduler.canNotify(ctx)) }
    var explain by remember { mutableStateOf(false) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        granted = ok
        fb.toast(if (ok) "Pronto! Os lembretes vão aparecer nas notificações." else "Sem permissão, os lembretes ficam só no app. Dá para mudar nas configurações do Android.", if (ok) Mood.COMEMORANDO else Mood.TRANQUILA)
    }
    fun ensurePermission() { if (!granted && Build.VERSION.SDK_INT >= 33) explain = true }
    Page(nav, "Lembretes") {
        ScreenHeader("Lembretes", "Contas, tarefas, metas, mudança e manutenção. Tudo local, sem internet.", mood = Mood.DICA)
        if (!granted && Build.VERSION.SDK_INT >= 33) {
            if (explain || items.any { it.enabled }) MascotBubble("Para avisar na hora certa, preciso da permissão de notificações. Uso só para os lembretes que você criar — nada de propaganda.", Mood.DICA) {
                Button(onClick = { permission.launch(Manifest.permission.POST_NOTIFICATIONS) }) { Text("Permitir notificações") }
            }
        }
        if (!prefs.notifications) AlertBox("As notificações estão desligadas nas Configurações do app. Os lembretes continuam salvos, mas não vão tocar.", "DICA")
        CrudList(
            items = items, itemName = "lembrete", masculine = true,
            fields = listOf(
                FieldSpec("title", "Título", required = true, placeholder = "Ex.: Pagar a internet"),
                FieldSpec("kind", "Tipo", FieldType.SELECT, true, REMINDER_KINDS),
                FieldSpec("date", "Data", FieldType.DATE, true),
                FieldSpec("time", "Horário (hh:mm)", required = true, placeholder = "09:00", validate = { d -> if (parseTime(d.str("time")) == null) "Use o formato hh:mm, por exemplo 09:30." else null }),
                FieldSpec("repeat", "Repetir", FieldType.SELECT, true, REPEATS),
                FieldSpec("message", "Mensagem (opcional)"),
                FieldSpec("enabled", "Ativado", FieldType.CHECK),
            ),
            toForm = { r -> if (r == null) mapOf("kind" to "OUTRO", "date" to Init.today(), "time" to "%02d:%02d".format(defMinute / 60, defMinute % 60), "repeat" to "NENHUM", "enabled" to "1") else mapOf("title" to r.title, "kind" to r.kind, "date" to Init.date(r.date), "time" to "%02d:%02d".format(r.minuteOfDay / 60, r.minuteOfDay % 60), "repeat" to r.repeat, "message" to (r.message ?: ""), "enabled" to Init.bool(r.enabled)) },
            save = { d, old ->
                val r = ReminderEntity(id = old?.id ?: 0, title = d.str("title"), kind = d.str("kind"), date = d.date("date")!!, minuteOfDay = parseTime(d.str("time"))!!, repeat = d.str("repeat"), message = d.text("message"), enabled = d.bool("enabled"))
                val id = c.db.reminders().upsert(r)
                ReminderScheduler.schedule(ctx, r.copy(id = if (r.id == 0L) id else r.id))
                if (r.enabled) ensurePermission()
            },
            delete = { r -> ReminderScheduler.cancel(ctx, r.id); c.db.reminders().delete(r) },
            row = { r ->
                val next = ReminderScheduler.nextTrigger(r)
                RowSpec(
                    title = r.title,
                    subtitle = "${REMINDER_KINDS.first { it.first == r.kind }.second} · ${REPEATS.first { it.first == r.repeat }.second}" +
                        (next?.let { " · próximo: ${Dates.br(it.toLocalDate().toEpochDay())} às %02d:%02d".format(it.hour, it.minute) } ?: " · já passou"),
                    tags = if (!r.enabled) { { Tag("desativado") } } else null,
                    extra = {
                        Switch(checked = r.enabled, onCheckedChange = { on ->
                            scope.launch { val n = r.copy(enabled = on); c.db.reminders().upsert(n); ReminderScheduler.schedule(ctx, n) }
                            if (on) ensurePermission()
                        }, modifier = Modifier.semantics { contentDescription = if (r.enabled) "Desativar ${r.title}" else "Ativar ${r.title}" })
                    },
                )
            },
            empty = EmptySpec("Nenhum lembrete", "Crie lembretes aqui ou toque no 🔔 em contas e tarefas da casa."),
        )
    }
}

private fun parseTime(s: String): Int? {
    val p = s.trim().split(':', 'h', 'H').filter { it.isNotBlank() }
    val h = p.getOrNull(0)?.toIntOrNull() ?: return null
    val m = p.getOrNull(1)?.toIntOrNull() ?: 0
    if (h !in 0..23 || m !in 0..59) return null
    return h * 60 + m
}

// ------------------------------------------------------------------ Configurações
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(c: AppContainer, nav: NavController) {
    val p by c.prefs.flow.asState(UserPrefs())
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    val confirm = LocalConfirm.current
    val ctx = LocalContext.current
    val backup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri != null) scope.launch { runCatching { c.backup(uri) }.onSuccess { fb.toast("Cópia de segurança salva! Guarde bem esse arquivo.", Mood.COMEMORANDO) }.onFailure { fb.toast("Não consegui salvar a cópia.", Mood.CONFUSA) } }
    }
    val restore = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) confirm.ask("Restaurar cópia de segurança?", "Os registros atuais serão substituídos pelos do arquivo e o app vai reiniciar.", "Restaurar") {
            scope.launch {
                runCatching { c.restore(uri) }
                    .onSuccess { restartApp(ctx) }
                    .onFailure { fb.toast(it.message ?: "Não consegui restaurar.", Mood.CONFUSA) }
            }
        }
    }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) scope.launch { runCatching { c.exportFinanceCsv(uri) }.onSuccess { fb.toast("Planilha exportada! 📄") }.onFailure { fb.toast("Não consegui exportar.", Mood.CONFUSA) } }
    }
    Page(nav, "Configurações") {
        SectionTitle("Aparência")
        Text("Tema", fontWeight = FontWeight.Bold)
        ThemeMode.values().forEach { t ->
            Row(Modifier.fillMaxWidth().selectable(p.theme == t, role = Role.RadioButton) { scope.launch { c.prefs.update { it.copy(theme = t) } } }, verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = p.theme == t, onClick = null); Text(t.label)
            }
        }
        Text("Tamanho do texto: ${(p.textScale * 100).toInt()}%", fontWeight = FontWeight.Bold)
        Slider(
            value = p.textScale, onValueChange = { v -> scope.launch { c.prefs.update { it.copy(textScale = (Math.round(v * 20) / 20f)) } } },
            valueRange = 0.85f..1.5f, steps = 12, modifier = Modifier.semantics { contentDescription = "Tamanho do texto" },
        )
        Text("Assim fica o texto do app. O tamanho de fonte do Android também é respeitado.", style = MaterialTheme.typography.bodySmall)
        val st = p.settings
        fun set(k: String, v: Boolean) { scope.launch { c.prefs.setSetting(k, v) } }
        Text("Cor do app", fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            br.com.casaclara.ui.theme.ACCENTS.forEach { (k, v) ->
                val sel = st.accent == k
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.selectable(sel, role = Role.RadioButton) { scope.launch { c.prefs.setSetting("accent", k) } }.semantics { contentDescription = "Cor ${br.com.casaclara.ui.theme.ACCENT_LABELS[k]}" }) {
                    Box(Modifier.size(44.dp).background(androidx.compose.ui.graphics.Color(v[0]), CircleShape).border(if (sel) 4.dp else 2.dp, LocalSoft.current.contorno, CircleShape), contentAlignment = Alignment.Center) {
                        if (sel) Text("✓", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Black)
                    }
                    Text(br.com.casaclara.ui.theme.ACCENT_LABELS[k] ?: k, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Text("Estilo das letras", fontWeight = FontWeight.Bold)
        listOf(false to "Divertidas (Baloo e Nunito)", true to "Clássicas (as do seu celular, mais sóbrias)").forEach { (v, l) ->
            Row(Modifier.fillMaxWidth().selectable(st.classicFont == v, role = Role.RadioButton) { set("classicFont", v) }, verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = st.classicFont == v, onClick = null); Text(l)
            }
        }
        SettingSwitch("Modo simples", "Esconde a aba Jornada e deixa o Início só com o essencial.", st.simpleMode) { set("simpleMode", it) }
        SettingSwitch("Animações", "Movimentos da mascote, gráficos e transições.", st.animations) { set("animations", it) }
        SettingSwitch("Confete nas conquistas", "Chuva de confete quando você conclui algo.", st.confetti, enabled = st.animations) { set("confetti", it) }
        SettingSwitch("Vibração", "Uma vibração curta ao comemorar.", st.haptics) { set("haptics", it) }

        SectionTitle("Privacidade")
        SettingSwitch("Esconder valores", "Mostra R$ ••••• no lugar dos valores. Bom para usar o app em público. Também dá para alternar pelo olho no Início.", st.hideValues) { set("hideValues", it) }
        val secure = br.com.casaclara.ui.deviceSecure(ctx)
        SettingSwitch("Bloquear o app", if (secure) "Pede a senha, o padrão ou a digital do celular ao abrir (e depois de 30 segundos fora). O app também fica oculto na tela de apps recentes."
            else "Para usar, primeiro configure um bloqueio de tela no celular (PIN, padrão, senha ou digital).", st.lock, enabled = secure || st.lock) { set("lock", it) }

        SectionTitle("Notificações")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("Lembretes ativados", fontWeight = FontWeight.Bold); Text("Contas, tarefas, metas, mudança e manutenção.", style = MaterialTheme.typography.bodySmall) }
            Switch(checked = p.notifications, onCheckedChange = { on -> scope.launch { c.prefs.update { it.copy(notifications = on) } } })
        }
        val stn = p.settings
        Text("Horário padrão dos novos lembretes: %02d:%02d".format(stn.reminderMinute / 60, stn.reminderMinute % 60), fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(7, 8, 9, 12, 18, 20).forEach { h -> FilterChip(stn.reminderMinute == h * 60, { scope.launch { c.prefs.setSetting("reminderMinute", (h * 60).toString()) } }, { Text("%02d:00".format(h)) }) }
        }
        SettingSwitch("Resumo da semana", "Todo domingo às 19h: quanto gastou e quantas tarefas fez.", stn.weeklySummary) { on ->
            scope.launch { c.prefs.setSetting("weeklySummary", on) }
            br.com.casaclara.notifications.WeeklySummary.schedule(ctx, on)
        }
        SecondaryButton("🔔 Gerenciar lembretes", Modifier.fillMaxWidth()) { nav.navigate(Routes.REMINDERS) }

        SectionTitle("Seus dados")
        AlertBox("Seus dados ficam guardados neste aparelho, no banco de dados do app. Não há conta nem servidor do Casa Clara: nenhum dado financeiro é enviado para a internet. Se o backup do Android estiver ligado na sua conta Google, o próprio Android pode incluir o app nele.", "INFO")
        SecondaryButton("💾 Fazer cópia de segurança", Modifier.fillMaxWidth()) { backup.launch("casa-clara-backup-${Dates.today()}.db") }
        SecondaryButton("📂 Restaurar cópia de segurança", Modifier.fillMaxWidth()) { restore.launch(arrayOf("*/*")) }
        SecondaryButton("📄 Exportar finanças (CSV)", Modifier.fillMaxWidth()) { export.launch("casa-clara-financas-${Dates.today()}.csv") }
        Text("A cópia de segurança inclui os registros (finanças, listas, guias lidos). Preferências como tema e respostas do perfil não entram.", style = MaterialTheme.typography.bodySmall)

        SectionTitle("Dados de demonstração")
        Text("Exemplos fictícios para conhecer o app. Ficam marcados como \"demo\".", style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryButton("Carregar exemplos", Modifier.weight(1f)) { scope.launch { c.loadDemo() }; fb.toast("Exemplos carregados!", Mood.COMEMORANDO) }
            SecondaryButton("Remover exemplos", Modifier.weight(1f), enabled = p.demoLoaded) {
                confirm.ask("Remover os exemplos?", "Só os itens marcados como demo serão apagados. O que você cadastrou fica.", "Remover") { scope.launch { c.removeDemo() }; fb.toast("Exemplos removidos.", Mood.TRANQUILA) }
            }
        }

        SectionTitle("Jornada")
        SecondaryButton("↺ Reiniciar jornada (mantém os registros)", Modifier.fillMaxWidth()) {
            confirm.ask("Reiniciar a jornada?", "Você responde as perguntas iniciais de novo. Finanças, listas e demais registros NÃO são apagados.", "Reiniciar", danger = false) {
                scope.launch { c.restartJourney(); withContext(Dispatchers.Main) { nav.navigate(Routes.ONBOARDING) { popUpTo(0) { inclusive = true } } } }
            }
        }
        SecondaryButton("Limpar histórico de leitura dos guias", Modifier.fillMaxWidth()) {
            confirm.ask("Limpar histórico?", "Favoritos e checklists dos guias ficam. Só o histórico de leitura é apagado.", "Limpar") { scope.launch { c.db.guides().clearHistory() } }
        }
        SectionTitle("Zona de cuidado")
        Button(
            onClick = {
                confirm.ask("Apagar TODOS os dados?", "Isso apaga tudo: perfil, finanças, listas, lembretes e preferências. Não dá para desfazer. Faça uma cópia de segurança antes, se quiser.", "Apagar tudo") {
                    scope.launch { c.deleteEverything(); restartApp(ctx) }
                }
            },
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Apagar todos os dados") }
        SectionTitle("Sobre")
        Text("Casa Clara 1.0.0 — Sua casa, seu jeito. Vamos nessa!", fontWeight = FontWeight.Bold)
        Text("Conteúdo educativo e geral. Não substitui orientação médica, psicológica, jurídica, financeira ou técnica profissional. Em emergências: Bombeiros 193 · SAMU 192 · Polícia 190.", style = MaterialTheme.typography.bodySmall)
    }
}

private fun restartApp(ctx: android.content.Context) {
    val intent = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    if (intent != null) ctx.startActivity(intent)
    Process.killProcess(Process.myPid())
    exitProcess(0)
}

// ------------------------------------------------------------------ Perfil
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(c: AppContainer, nav: NavController) {
    val p by c.prefs.flow.asState(UserPrefs())
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    var name by remember(p.name) { mutableStateOf(p.name) }
    var income by remember(p.income) { mutableStateOf(Money.toInput(p.income)) }
    var saved by remember(p.saved) { mutableStateOf(Money.toInput(p.saved)) }
    Page(nav, "Perfil") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Mascot(Mood.ACENANDO, 80.dp); Spacer(Modifier.width(12.dp))
            Column { Text(p.name.ifBlank { "Você" }, style = MaterialTheme.typography.headlineSmall); p.situation?.let { Text("Está ${it.phrase}.") } }
        }
        SectionTitle("Perfil da jornada")
        OutlinedTextField(name, { name = it.take(40) }, label = { Text("Nome ou apelido") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        SelectField("Situação", p.situation?.name ?: "", Situation.values().map { it.name to "${it.emoji} ${it.label}" }) { v -> scope.launch { c.prefs.update { it.copy(situation = Situation.valueOf(v)) } } }
        SelectField("Objetivo principal", p.goal?.name ?: "", listOf("" to "Nenhum em especial") + MainGoal.values().map { it.name to "${it.emoji} ${it.label}" }) { v -> scope.launch { c.prefs.update { it.copy(goal = if (v.isBlank()) null else MainGoal.valueOf(v)) } } }
        MoneyInput("Renda mensal aproximada (opcional)", income, { income = it }, help = "Usada só nos cálculos, neste aparelho.")
        MoneyInput("Quanto já guardei (opcional)", saved, { saved = it })
        Text("Assuntos que quero aprender", fontWeight = FontWeight.Bold)
        androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Library.CATEGORIES.forEach { cat ->
                val on = cat.id in p.interests
                FilterChip(on, { scope.launch { c.prefs.update { it.copy(interests = if (on) it.interests - cat.id else it.interests + cat.id) } } }, { Text("${cat.emoji} ${cat.name}") })
            }
        }
        PrimaryButton("Salvar", Modifier.fillMaxWidth()) {
            scope.launch { c.prefs.update { it.copy(name = name.trim(), income = Money.parse(income), saved = Money.parse(saved)) } }
            fb.toast("Perfil atualizado. Sua jornada foi ajustada.")
        }
        SecondaryButton("🗺️ Ver minha jornada", Modifier.fillMaxWidth()) { nav.navigate(Routes.JOURNEY) }
        SecondaryButton("⚙️ Configurações e dados", Modifier.fillMaxWidth()) { nav.navigate(Routes.SETTINGS) }
    }
}

// ------------------------------------------------------------------ Busca global
@Composable
fun SearchScreen(c: AppContainer, nav: NavController) {
    var q by rememberSaveable { mutableStateOf("") }
    val tx by c.db.tx().all().asState(emptyList())
    val bills by c.db.bills().all().asState(emptyList())
    val tasks by c.db.houseTasks().all().asState(emptyList())
    val checks by c.db.checks().all().asState(emptyList())
    val contacts by c.db.contacts().all().asState(emptyList())
    val props by c.db.properties().all().asState(emptyList())
    val goals by c.db.goals().all().asState(emptyList())
    fun norm(s: String) = java.text.Normalizer.normalize(s.lowercase(), java.text.Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")
    val nq = norm(q.trim())
    fun hit(s: String?) = nq.length >= 2 && s != null && norm(s).contains(nq)
    Page(nav, "Buscar") {
        OutlinedTextField(q, { q = it }, label = { Text("Buscar guias, contas, tarefas, registros…") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        if (nq.length < 2) { EmptyState("O que você procura?", "Digite pelo menos 2 letras. Ex.: luz, caução, banheiro, internet.", Mood.PENSANDO); return@Page }
        val guides = Library.search(q).take(8)
        val results = buildList<Triple<String, String, String>> {
            guides.forEach { add(Triple("📚 Guia", it.title, Routes.guide(it.id))) }
            bills.filter { hit(it.name) }.forEach { add(Triple("📅 Conta", "${it.name} · dia ${it.dueDay}" + (if (billStatus(it).paid) " · paga" else ""), Routes.finance(Routes.FinanceTab.CONTAS))) }
            tx.filter { hit(it.description) || hit(it.category) }.take(15).forEach { add(Triple(if (it.kind == "ENTRADA") "💵 Entrada" else "🧾 Gasto", "${it.description} · ${Money.format(it.amount)} · ${Dates.br(it.date)}", Routes.finance(Routes.FinanceTab.LANCAMENTOS))) }
            goals.filter { hit(it.name) }.forEach { add(Triple("🎯 Meta", it.name, Routes.finance(Routes.FinanceTab.METAS))) }
            tasks.filter { hit(it.title) || hit(it.room) }.forEach { add(Triple("🧹 Tarefa", it.title, Routes.house(0))) }
            checks.filter { hit(it.title) || hit(it.note) }.forEach { e ->
                val (label, route) = when (e.list) {
                    "CLEANING" -> "🚪 Limpeza" to Routes.house(Routes.HouseTab.LIMPEZA)
                    "DOCS" -> "🗂️ Documento" to Routes.moving(Routes.MovingTab.DOCUMENTOS)
                    "VISIT" -> "🔦 Visita" to Routes.moving(Routes.MovingTab.VISITA)
                    "QUESTIONS" -> "❓ Pergunta" to Routes.moving(Routes.MovingTab.PERGUNTAS)
                    else -> "✅ Checklist" to Routes.moving(0)
                }
                add(Triple(label, e.title, route))
            }
            contacts.filter { hit(it.name) || hit(it.service) }.forEach { add(Triple("📇 Contato", "${it.name}${it.phone?.let { p -> " · $p" } ?: ""}", Routes.CONTACTS)) }
            props.filter { hit(it.name) || hit(it.address) }.forEach { add(Triple("🔎 Imóvel", it.name, Routes.moving(Routes.MovingTab.IMOVEIS))) }
        }
        SectionTitle("${results.size} resultado(s)")
        if (results.isEmpty()) EmptyState("Nada encontrado", "Tente outra palavra.", Mood.CONFUSA)
        results.forEach { (tag, title, route) -> ItemRow(title, tags = { Tag(tag) }, onClick = { nav.navigate(route) }) }
    }
}

// ------------------------------------------------------------------ Planos
@Composable
fun PlansScreen(nav: NavController) {
    val status = BillingGateway.status()
    Page(nav, "Planos") {
        MascotBubble("Tudo o que é essencial é e continua gratuito. O Premium vai reunir só o que precisa de servidor.", Mood.DICA)
        Plans.ALL.forEach { pl ->
            BrandCard(tone = if (pl.plan == Plan.GRATUITO) Tone.VERDE else Tone.ROXO) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(pl.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    Tag(if (pl.plan == Plans.current()) "seu plano" else "em breve", Tone.AMARELO)
                }
                Text(pl.price, style = MaterialTheme.typography.headlineSmall)
                pl.features.forEach { Text("• $it") }
                if (pl.plan == Plan.PREMIUM) {
                    Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) { Text("Assinar Premium") }
                    Text(status.reason, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Text("Sem anúncios. Sem compras simuladas.", fontWeight = FontWeight.Bold)
    }
}

// ------------------------------------------------------------------ Contatos e emergência
@Composable
fun ContactsScreen(c: AppContainer, nav: NavController) {
    val items by c.db.contacts().all().asState(emptyList())
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    val ctx = LocalContext.current
    val kinds = listOf("EMERGENCIA" to "🚨 Emergência", "PROFISSIONAL" to "👷 Profissional", "IMPORTANTE" to "⭐ Importante (proprietário, síndico, imobiliária…)")
    Page(nav, "Contatos") {
        ScreenHeader("Contatos e emergência", "Profissionais de confiança, contatos importantes da mudança e números de emergência.", ill = Ill.FERRAMENTA, tone = Tone.CORAL)
        AlertBox("Emergência: Bombeiros 193 · SAMU 192 · Polícia 190 · Defesa Civil 199. Em situação de risco, saia do local e ligue de um lugar seguro.", "PERIGO")
        CrudList(
            items = items, itemName = "contato", masculine = true,
            fields = listOf(
                FieldSpec("name", "Nome", required = true),
                FieldSpec("kind", "Tipo", FieldType.SELECT, true, kinds),
                FieldSpec("service", "Serviço ou função", FieldType.TEXT, placeholder = "Ex.: Eletricista, Síndico"),
                FieldSpec("phone", "Telefone", FieldType.PHONE),
                FieldSpec("address", "Endereço"),
                FieldSpec("note", "Anotações", FieldType.LONGTEXT, placeholder = "Ex.: indicado pela vizinha"),
            ),
            toForm = { e -> if (e == null) mapOf("kind" to "PROFISSIONAL") else mapOf("name" to e.name, "kind" to e.kind, "service" to (e.service ?: ""), "phone" to (e.phone ?: ""), "address" to (e.address ?: ""), "note" to (e.note ?: "")) },
            save = { d, old -> c.db.contacts().upsert(ContactEntity(id = old?.id ?: 0, name = d.str("name"), kind = d.str("kind"), service = d.text("service"), phone = d.text("phone"), address = d.text("address"), note = d.text("note"), isDemo = old?.isDemo ?: false)) },
            delete = { c.db.contacts().delete(it) },
            groupBy = { e -> kinds.first { it.first == e.kind }.second.substringBefore(" (") }, groupOrder = kinds.map { it.second.substringBefore(" (") },
            row = { e ->
                RowSpec(e.name, listOfNotNull(e.service, e.phone, e.address, e.note).joinToString(" · "),
                    extra = { if (!e.phone.isNullOrBlank()) TextButton(onClick = { runCatching { ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + e.phone.filter { it.isDigit() || it == '+' }))) } }) { Text("📞") } })
            },
            empty = EmptySpec("Nenhum contato ainda", "Quando o cano estoura, não é hora de procurar encanador. Salve os contatos agora."),
            isDemo = { it.isDemo },
            headerActions = { TextButton(onClick = { scope.launch { c.loadEmergencyContacts() }; fb.toast("Números de emergência adicionados.", Mood.COMEMORANDO) }) { Text("🚨 Emergência") } },
        )
        SectionTitle("Garantias e notas")
        SecondaryButton("🧾 Abrir garantias e notas fiscais", Modifier.fillMaxWidth()) { nav.navigate(Routes.house(Routes.HouseTab.GARANTIAS)) }
        SectionTitle("Guias de manutenção e segurança")
        listOf("chamar-profissional", "sinais-vazamento", "vazamento-gas", "eletricidade", "manutencao-preventiva").forEach { id ->
            Library.BY_ID[id]?.let { g -> ItemRow("${g.emoji} ${g.title}", onClick = { nav.navigate(Routes.guide(g.id)) }) }
        }
    }
}


@Composable
private fun SettingSwitch(title: String, sub: String, checked: Boolean, enabled: Boolean = true, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().selectable(checked, enabled = enabled, role = Role.Switch) { onChange(!checked) }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 8.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}
