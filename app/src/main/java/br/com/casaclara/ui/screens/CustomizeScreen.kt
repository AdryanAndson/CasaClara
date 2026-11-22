package br.com.casaclara.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.casaclara.data.AppContainer
import br.com.casaclara.data.UserPrefs
import br.com.casaclara.domain.Situation
import br.com.casaclara.ui.HomeCatalog
import br.com.casaclara.ui.Tools
import br.com.casaclara.ui.asState
import br.com.casaclara.ui.components.ItemRow
import br.com.casaclara.ui.components.LocalFeedback
import br.com.casaclara.ui.components.Mood
import br.com.casaclara.ui.components.Page
import br.com.casaclara.ui.components.ScreenHeader
import br.com.casaclara.ui.components.SecondaryButton
import br.com.casaclara.ui.components.SectionTitle
import br.com.casaclara.ui.components.Tone
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomizeScreen(c: AppContainer, nav: NavController) {
    val p by c.prefs.flow.asState(UserPrefs())
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    val planning = p.situation == Situation.PAIS || p.situation == Situation.JUNTANDO
    val order = HomeCatalog.resolve(p.homeOrder, planning)
    val shortcuts = p.shortcuts.ifEmpty { Tools.defaults(planning) }
    fun move(i: Int, delta: Int) {
        val j = i + delta
        if (j !in order.indices) return
        val n = order.toMutableList(); val t = n[i]; n[i] = n[j]; n[j] = t
        scope.launch { c.prefs.update { it.copy(homeOrder = n) } }
    }
    Page(nav, "Personalizar o Início") {
        ScreenHeader("Seu Início, seu jeito", "Escolha o que aparece, em que ordem, e seus atalhos favoritos.", mood = Mood.PENSANDO, tone = Tone.ROXO)
        SectionTitle("Cartões")
        order.forEachIndexed { i, id ->
            val card = HomeCatalog.CARDS.first { it.id == id }
            val visible = id !in p.homeHidden
            ItemRow(
                title = "${card.emoji} ${card.title}", subtitle = card.desc,
                extra = {
                    IconButton(onClick = { move(i, -1) }, enabled = i > 0) { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Subir ${card.title}") }
                    IconButton(onClick = { move(i, 1) }, enabled = i < order.lastIndex) { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Descer ${card.title}") }
                    Switch(
                        checked = visible,
                        onCheckedChange = { on -> scope.launch { c.prefs.update { it.copy(homeHidden = if (on) it.homeHidden - id else it.homeHidden + id) } } },
                        modifier = Modifier.semantics { contentDescription = (if (visible) "Esconder " else "Mostrar ") + card.title },
                    )
                },
            )
        }
        SectionTitle("Atalhos favoritos (${shortcuts.size}/8)")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Tools.ALL.forEach { t ->
                val on = t.id in shortcuts
                FilterChip(on, {
                    val n = if (on) shortcuts - t.id else if (shortcuts.size >= 8) { fb.toast("Até 8 atalhos. Tire um para pôr outro.", Mood.TRANQUILA); shortcuts } else shortcuts + t.id
                    scope.launch { c.prefs.update { it.copy(shortcuts = n) } }
                }, { Text("${t.emoji} ${t.label}") })
            }
        }
        SecondaryButton("↺ Voltar ao padrão") {
            scope.launch { c.prefs.update { it.copy(homeOrder = emptyList(), homeHidden = emptySet(), shortcuts = emptyList()) } }
            fb.toast("Início no jeito padrão.", Mood.TRANQUILA)
        }
    }
}
