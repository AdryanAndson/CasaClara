package br.com.casaclara.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.casaclara.data.AppContainer
import br.com.casaclara.data.UserPrefs
import br.com.casaclara.domain.Routes
import br.com.casaclara.domain.Snapshot
import br.com.casaclara.ui.asState
import br.com.casaclara.ui.components.BrandCard
import br.com.casaclara.ui.components.EmojiBadge
import br.com.casaclara.ui.components.ItemRow
import br.com.casaclara.ui.components.LocalConfirm
import br.com.casaclara.ui.components.LocalFeedback
import br.com.casaclara.ui.components.MascotBubble
import br.com.casaclara.ui.components.Mood
import br.com.casaclara.ui.components.Page
import br.com.casaclara.ui.components.ProgressLine
import br.com.casaclara.ui.components.ScreenHeader
import br.com.casaclara.ui.components.SecondaryButton
import br.com.casaclara.ui.components.SectionTitle
import br.com.casaclara.ui.components.Tag
import br.com.casaclara.ui.components.Tone
import br.com.casaclara.ui.journeyOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun JourneyScreen(c: AppContainer, nav: NavController) {
    val p by c.prefs.flow.asState(UserPrefs())
    val snap by c.snapshot.asState(Snapshot())
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    val confirm = LocalConfirm.current
    val j = journeyOf(snap, p)
    val pct = (j.progress * 100).toInt()

    Page {
        ScreenHeader("Minha jornada", p.situation?.let { "Caminho para quem está ${it.phrase}. Sem pressa e sem sequência obrigatória." }, mood = if (j.next == null) Mood.COMEMORANDO else Mood.FELIZ)
        BrandCard(tone = Tone.VERDE) {
            Row { Text("${j.doneCount} de ${j.total} passos", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f)); Tag("$pct%") }
            ProgressLine(j.progress.toFloat(), "Progresso da jornada")
            if (p.journeyPaused) Text("⏸ Jornada pausada: o painel não mostra recomendações até você retomar.", style = MaterialTheme.typography.bodyMedium)
        }
        val motivacao = when {
            pct == 0 -> "Todo mundo começa do zero. O primeiro passo é o mais importante."
            pct < 40 -> "Você já começou — e isso é o mais difícil. Siga no seu ritmo."
            pct < 80 -> "Metade do caminho! Sua independência está tomando forma."
            pct < 100 -> "Falta pouco. Olha só o quanto você já organizou!"
            else -> "Jornada completa! Agora é manter a casa no ritmo — e comemorar."
        }
        MascotBubble(motivacao, Mood.DICA, 64.dp)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryButton(if (p.journeyPaused) "▶ Retomar" else "⏸ Pausar", Modifier.weight(1f)) {
                scope.launch { c.prefs.update { it.copy(journeyPaused = !it.journeyPaused) } }
                fb.toast(if (p.journeyPaused) "Jornada retomada. Bora!" else "Jornada pausada. Ela espera você.", Mood.TRANQUILA)
            }
            SecondaryButton("✏️ Editar", Modifier.weight(1f)) { nav.navigate(Routes.PROFILE) }
        }
        Row(Modifier.fillMaxWidth()) {
            SecondaryButton("↺ Reiniciar jornada", Modifier.fillMaxWidth()) {
                confirm.ask("Reiniciar a jornada?", "Você responde as perguntas iniciais de novo e os passos marcados à mão são zerados. Seus registros (dinheiro, listas, contas) NÃO são apagados.", "Reiniciar", danger = false) {
                    scope.launch { c.restartJourney(); withContext(Dispatchers.Main) { nav.navigate(Routes.ONBOARDING) { popUpTo(0) { inclusive = true } } } }
                }
            }
        }

        SectionTitle("Pendentes")
        val pend = j.steps.filter { !it.done && !it.skipped }
        if (pend.isEmpty()) Text("Nenhum passo pendente. 🎉")
        pend.forEach { st ->
            val isNext = j.next?.step?.id == st.step.id
            ItemRow(
                title = st.step.title, subtitle = st.step.why,
                leading = { EmojiBadge(st.step.emoji, 40.dp, if (isNext) Tone.AMARELO else Tone.NEUTRO) },
                tags = if (isNext) { { Tag("próximo", Tone.AMARELO) } } else null,
                onClick = { nav.navigate(st.step.route) },
                extra = {
                    TextButton(onClick = {
                        scope.launch { c.prefs.update { it.copy(manualSteps = it.manualSteps + st.step.id) } }
                        fb.celebrate("Mais um passo rumo à independência!")
                    }) { Text("Já fiz") }
                    TextButton(onClick = { scope.launch { c.prefs.update { it.copy(skippedSteps = it.skippedSteps + st.step.id) } } }) { Text("Pular") }
                },
            )
        }
        val skipped = j.steps.filter { it.skipped && !it.done }
        if (skipped.isNotEmpty()) {
            SectionTitle("Pulados")
            skipped.forEach { st ->
                ItemRow(title = st.step.title, leading = { EmojiBadge(st.step.emoji, 40.dp) },
                    extra = { TextButton(onClick = { scope.launch { c.prefs.update { it.copy(skippedSteps = it.skippedSteps - st.step.id) } } }) { Text("Voltar") } })
            }
        }
        SectionTitle("Concluídos")
        val done = j.steps.filter { it.done }
        if (done.isEmpty()) Text("Os passos aparecem aqui quando você usa as ferramentas — ou marca \"Já fiz\".", color = MaterialTheme.colorScheme.onSurfaceVariant)
        done.forEach { st ->
            ItemRow(
                title = st.step.title, subtitle = if (st.manual) "Marcado por você" else "Feito pelo que você registrou", checked = true,
                onCheck = if (st.manual) { _ -> scope.launch { c.prefs.update { it.copy(manualSteps = it.manualSteps - st.step.id) } } } else null,
            )
        }
        SectionTitle("Metas e conquistas")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryButton("🎯 Metas", Modifier.weight(1f)) { nav.navigate(Routes.finance(Routes.FinanceTab.METAS)) }
            SecondaryButton("🏆 Conquistas", Modifier.weight(1f)) { nav.navigate(Routes.ACHIEVEMENTS) }
        }
    }
}
