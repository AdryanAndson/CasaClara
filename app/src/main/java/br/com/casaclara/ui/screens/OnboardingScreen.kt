package br.com.casaclara.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import br.com.casaclara.data.AppContainer
import br.com.casaclara.data.GoalEntity
import br.com.casaclara.domain.Journey
import br.com.casaclara.domain.MainGoal
import br.com.casaclara.domain.Money
import br.com.casaclara.domain.Routes
import br.com.casaclara.domain.Situation
import br.com.casaclara.domain.content.Library
import br.com.casaclara.ui.components.AlertBox
import br.com.casaclara.ui.components.LocalFeedback
import br.com.casaclara.ui.components.Mascot
import br.com.casaclara.ui.components.MoneyInput
import br.com.casaclara.ui.components.Mood
import br.com.casaclara.ui.components.PrimaryButton
import br.com.casaclara.ui.components.ProgressLine
import br.com.casaclara.ui.components.SecondaryButton
import br.com.casaclara.ui.theme.LocalSoft
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private enum class OStep { INTRO, NOME, SITUACAO, OBJETIVO, RENDA, GUARDADO, META, ASSUNTOS, PRONTO }

@Composable
fun OnboardingScreen(c: AppContainer, onDone: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    val feedback = LocalFeedback.current
    var loaded by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var situation by remember { mutableStateOf<Situation?>(null) }
    var goal by remember { mutableStateOf<MainGoal?>(null) }
    var income by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf("") }
    var moveGoal by remember { mutableStateOf("") }
    var interests by remember { mutableStateOf(setOf<String>()) }
    var createGoal by remember { mutableStateOf(true) }
    var index by remember { mutableIntStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val p = c.prefs.current()
        name = p.name; situation = p.situation; goal = p.goal; interests = p.interests
        income = Money.toInput(p.income); saved = Money.toInput(p.saved); moveGoal = Money.toInput(p.moveGoal)
        index = p.onboardingStep
        loaded = true
    }
    if (!loaded) return

    val steps = buildList {
        add(OStep.INTRO); add(OStep.NOME); add(OStep.SITUACAO); add(OStep.OBJETIVO); add(OStep.RENDA); add(OStep.GUARDADO)
        if (situation == Situation.PAIS || situation == Situation.JUNTANDO) add(OStep.META)
        add(OStep.ASSUNTOS); add(OStep.PRONTO)
    }
    val i = index.coerceIn(0, steps.lastIndex)
    val step = steps[i]

    fun persist(n: Int) = scope.launch {
        c.prefs.update {
            it.copy(
                onboardingStep = n, name = name.trim(), situation = situation, goal = goal, interests = interests,
                income = Money.parse(income), saved = Money.parse(saved), moveGoal = Money.parse(moveGoal),
            )
        }
    }
    fun go(n: Int) { error = null; index = n; persist(n) }
    fun next() = go(i + 1)
    fun back() = go((i - 1).coerceAtLeast(0))

    fun finish(demo: Boolean) = scope.launch {
        val inc = Money.parse(income); val sav = Money.parse(saved); val mg = Money.parse(moveGoal)
        c.prefs.update {
            it.copy(onboarded = true, onboardingStep = 0, name = name.trim(), situation = situation, goal = goal, interests = interests, income = inc, saved = sav, moveGoal = mg)
        }
        if (inc != null && inc > 0) {
            val cur = c.prefs.current().calc["renda"].orEmpty()
            c.prefs.setCalc("renda", cur + ("renda" to Money.toInput(inc)))
        }
        if (createGoal && mg != null && mg > 0 && c.db.goals().all().first().none { it.name == "Meta de mudança" }) {
            c.db.goals().upsert(GoalEntity(name = "Meta de mudança", type = "MUDANCA", target = mg, saved = sav ?: 0))
        }
        if (demo) c.loadDemo()
        withContext(Dispatchers.Main) {
            feedback.celebrate("Bem-vindo(a) ao Casa Clara!")
            onDone(Routes.HOME)
        }
    }

    val mood = when (step) { OStep.INTRO -> Mood.ACENANDO; OStep.PRONTO -> Mood.COMEMORANDO; OStep.SITUACAO, OStep.OBJETIVO -> Mood.PENSANDO; OStep.RENDA, OStep.GUARDADO, OStep.META -> Mood.DICA; else -> Mood.FELIZ }
    val bubble = when (step) {
        OStep.INTRO -> "Oi! Eu sou a Clarinha, sua casinha guia. Vou te ajudar a conquistar sua independência, um passo de cada vez."
        OStep.NOME -> "Como posso te chamar? É só para deixar a conversa mais próxima."
        OStep.SITUACAO -> "Me conta em que momento você está. Não tem resposta errada."
        OStep.OBJETIVO -> "Qual é o seu principal objetivo agora? Eu monto o caminho a partir disso."
        OStep.RENDA -> "Quanto você recebe por mês, mais ou menos? Uso só para os cálculos. Pode pular."
        OStep.GUARDADO -> "Já conseguiu guardar algum dinheiro? Qualquer valor conta — e pode pular."
        OStep.META -> "Tem ideia de quanto precisa juntar para se mudar? Se não souber, a gente calcula depois."
        OStep.ASSUNTOS -> "O que você quer aprender primeiro? Escolha quantos quiser."
        OStep.PRONTO -> "Prontinho${if (name.isNotBlank()) ", ${name.trim()}" else ""}! Sua jornada está montada."
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = 640.dp).fillMaxWidth().verticalScroll(rememberScrollState()).imePadding().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProgressLine((i + 1f) / steps.size, "Passo ${i + 1} de ${steps.size}")
            Text("Passo ${i + 1} de ${steps.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.Bottom) {
                Mascot(mood, 110.dp)
                Spacer(Modifier.width(8.dp))
                Card(
                    shape = RoundedCornerShape(18.dp), border = BorderStroke(2.dp, LocalSoft.current.contorno),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.weight(1f),
                ) { Text(bubble, Modifier.padding(14.dp), fontWeight = FontWeight.SemiBold) }
            }

            when (step) {
                OStep.INTRO -> {
                    Heading("Sua casa, seu jeito. Vamos nessa!")
                    listOf(
                        "🧮" to "Descubra quanto custa morar sozinho e se o aluguel cabe no bolso.",
                        "💰" to "Organize dinheiro, contas, dívidas e metas sem planilha assustadora.",
                        "🧹" to "Cuide da casa com rotinas, listas e lembretes.",
                        "📚" to "Consulte ${br.com.casaclara.domain.content.Library.GUIDES.size} guias práticos para a vida adulta.",
                    ).forEach { (e, t) -> Row(verticalAlignment = Alignment.CenterVertically) { Text(e, style = MaterialTheme.typography.headlineSmall); Spacer(Modifier.width(12.dp)); Text(t) } }
                    AlertBox("Sem cadastro e sem internet: seus dados ficam guardados só neste aparelho.", "OK")
                    PrimaryButton("Começar", Modifier.fillMaxWidth()) { next() }
                }
                OStep.NOME -> {
                    Heading("Como posso te chamar?")
                    OutlinedTextField(
                        value = name, onValueChange = { if (it.length <= 40) name = it }, label = { Text("Nome ou apelido (opcional)") },
                        singleLine = true, keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words), modifier = Modifier.fillMaxWidth(),
                    )
                    Nav(onBack = ::back, onNext = ::next, onSkip = { name = ""; next() })
                }
                OStep.SITUACAO -> {
                    Heading("Qual é a sua situação hoje?")
                    Situation.values().forEach { s -> Choice(s.emoji, s.label, situation == s) { situation = s } }
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
                    Nav(onBack = ::back, onNext = { if (situation == null) error = "Escolha uma opção para eu montar sua jornada." else next() })
                }
                OStep.OBJETIVO -> {
                    Heading("Qual é seu principal objetivo?")
                    MainGoal.values().forEach { g -> Choice(g.emoji, g.label, goal == g) { goal = g } }
                    Nav(onBack = ::back, onNext = ::next, onSkip = { goal = null; next() })
                }
                OStep.RENDA -> {
                    Heading("Renda mensal aproximada")
                    MoneyInput("Quanto entra por mês (opcional)", income, { income = it }, help = "Some salário, bicos e outras rendas. Serve para calcular se o aluguel cabe.")
                    WhyBox("Por que pergunto? Para mostrar se o aluguel cabe no seu bolso e sugerir metas realistas. Fica só no seu aparelho.")
                    Nav(onBack = ::back, onNext = { if (income.isNotBlank() && Money.parse(income) == null) error = "Valor inválido." else next() }, onSkip = { income = ""; next() })
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
                OStep.GUARDADO -> {
                    Heading("Quanto já guardou?")
                    MoneyInput("Valor guardado (opcional)", saved, { saved = it })
                    WhyBox("Por que pergunto? Para calcular quanto tempo falta para suas metas. Sem julgamento: zero também é um começo.")
                    Nav(onBack = ::back, onNext = { if (saved.isNotBlank() && Money.parse(saved) == null) error = "Valor inválido." else next() }, onSkip = { saved = ""; next() })
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
                OStep.META -> {
                    Heading("Meta para a mudança")
                    MoneyInput("Quanto quero juntar (opcional)", moveGoal, { moveGoal = it }, help = "Caução, frete, o básico da casa e um pouco de folga.")
                    WhyBox("Por que pergunto? Para criar sua meta de economia e mostrar a previsão de quando você chega lá. Não sabe ainda? Pule — a calculadora do custo da mudança ajuda depois.")
                    Nav(onBack = ::back, onNext = { if (moveGoal.isNotBlank() && Money.parse(moveGoal) == null) error = "Valor inválido." else next() }, onSkip = { moveGoal = ""; next() })
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
                OStep.ASSUNTOS -> {
                    Heading("O que quer aprender primeiro?")
                    Library.CATEGORIES.forEach { cat ->
                        val on = cat.id in interests
                        Choice(cat.emoji, cat.name, on, multi = true) { interests = if (on) interests - cat.id else interests + cat.id }
                    }
                    Nav(onBack = ::back, onNext = ::next, onSkip = { next() })
                }
                OStep.PRONTO -> {
                    Heading("Sua jornada")
                    val path = Journey.path(situation, goal).drop(1).take(4)
                    path.forEachIndexed { n, s -> Row(verticalAlignment = Alignment.CenterVertically) { Text("${n + 1}.", fontWeight = FontWeight.Black); Spacer(Modifier.width(8.dp)); Text("${s.emoji} ${s.title}") } }
                    Text("…e mais passos depois. Você pode pausar, editar ou reiniciar a jornada quando quiser.", style = MaterialTheme.typography.bodySmall)
                    if (Money.parse(moveGoal) != null) {
                        Row(Modifier.fillMaxWidth().toggleable(createGoal, role = Role.Checkbox) { createGoal = it }, verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(createGoal, onCheckedChange = null); Text("Criar a meta de mudança com os valores que informei")
                        }
                    }
                    PrimaryButton("Bora começar! 🚀", Modifier.fillMaxWidth()) { finish(false) }
                    SecondaryButton("Explorar com dados de exemplo", Modifier.fillMaxWidth()) { finish(true) }
                    Text("Os exemplos ficam marcados como \"demo\" e saem com um toque em Configurações.", style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = ::back) { Text("Voltar") }
                }
            }
        }
    }
}

@Composable
private fun Heading(t: String) = Text(t, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })

@Composable
private fun WhyBox(t: String) = AlertBox(t, "INFO")

@Composable
private fun Choice(emoji: String, label: String, selected: Boolean, multi: Boolean = false, onClick: () -> Unit) {
    val mod = if (multi) Modifier.toggleable(selected, role = Role.Checkbox) { onClick() } else Modifier.selectable(selected, role = Role.RadioButton, onClick = onClick)
    Card(
        modifier = Modifier.fillMaxWidth().then(mod),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (selected) 3.dp else 2.dp, if (selected) MaterialTheme.colorScheme.primary else LocalSoft.current.contorno),
        colors = CardDefaults.cardColors(containerColor = if (selected) LocalSoft.current.verde else MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.width(12.dp))
            Text(label, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (selected) Text("✓", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun Nav(onBack: () -> Unit, onNext: () -> Unit, onSkip: (() -> Unit)? = null) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        SecondaryButton("Voltar", Modifier.weight(1f)) { onBack() }
        PrimaryButton("Continuar", Modifier.weight(1.4f)) { onNext() }
    }
    if (onSkip != null) TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) { Text("Pular esta pergunta") }
}
