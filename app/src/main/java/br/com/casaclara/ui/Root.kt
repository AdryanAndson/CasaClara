package br.com.casaclara.ui

import br.com.casaclara.ui.components.cartoonShadow

import androidx.compose.foundation.clickable

import kotlinx.coroutines.flow.debounce

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.casaclara.data.AppContainer
import br.com.casaclara.data.UserPrefs
import br.com.casaclara.domain.Achievements
import br.com.casaclara.domain.Routes
import br.com.casaclara.ui.components.ConfirmHost
import br.com.casaclara.ui.components.ConfirmState
import br.com.casaclara.ui.components.Feedback
import br.com.casaclara.ui.components.Mascot
import br.com.casaclara.ui.components.Mood
import br.com.casaclara.ui.components.ProvideFeedback
import br.com.casaclara.ui.components.rememberReduceMotion
import br.com.casaclara.ui.screens.AchievementsScreen
import br.com.casaclara.ui.screens.CalculatorScreen
import br.com.casaclara.ui.screens.CalculatorsScreen
import br.com.casaclara.ui.screens.ContactsScreen
import br.com.casaclara.ui.screens.CustomizeScreen
import br.com.casaclara.ui.screens.TimerScreen
import br.com.casaclara.ui.screens.WeekPlanScreen
import br.com.casaclara.ui.screens.ImportScreen
import br.com.casaclara.ui.screens.FinanceScreen
import br.com.casaclara.ui.screens.FurnitureScreen
import br.com.casaclara.ui.screens.GuideScreen
import br.com.casaclara.ui.screens.HomeScreen
import br.com.casaclara.ui.screens.HouseScreen
import br.com.casaclara.ui.screens.JourneyScreen
import br.com.casaclara.ui.screens.LibraryScreen
import br.com.casaclara.ui.screens.MarketScreen
import br.com.casaclara.ui.screens.MoreScreen
import br.com.casaclara.ui.screens.MovingScreen
import br.com.casaclara.ui.screens.OnboardingScreen
import br.com.casaclara.ui.screens.PlansScreen
import br.com.casaclara.ui.screens.ProfileScreen
import br.com.casaclara.ui.screens.RemindersScreen
import br.com.casaclara.ui.screens.SearchScreen
import br.com.casaclara.ui.screens.SettingsScreen
import br.com.casaclara.ui.screens.SharedScreen
import br.com.casaclara.ui.theme.Brand
import br.com.casaclara.ui.theme.CasaClaraTheme
import br.com.casaclara.ui.theme.LocalSoft
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class Toast(val id: Long, val text: String, val mood: Mood)
private data class BottomItem(val route: String, val label: String, val icon: ImageVector, val prefix: String)

private val BOTTOM = listOf(
    BottomItem(Routes.HOME, "Início", Icons.Filled.Home, "home"),
    BottomItem(Routes.JOURNEY, "Jornada", Icons.Filled.Map, "journey"),
    BottomItem(Routes.finance(0), "Finanças", Icons.Filled.AccountBalanceWallet, "finance"),
    BottomItem(Routes.house(0), "Casa", Icons.Filled.Weekend, "house"),
    BottomItem(Routes.MORE, "Mais", Icons.Filled.MoreHoriz, "more"),
)

@Composable
fun CasaClaraRoot(c: AppContainer) {
    val prefs by c.prefs.flow.collectAsState(initial = null)
    val p = prefs
    if (p == null) {
        // carregando preferências (muito rápido): tela neutra com a mascote
        CasaClaraTheme(br.com.casaclara.data.ThemeMode.SISTEMA, 1f) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Box(contentAlignment = Alignment.Center) { Mascot(Mood.ACENANDO, 120.dp) }
            }
        }
        return
    }
    CasaClaraTheme(p.theme, p.textScale) { AppContent(c, p) }
}

@OptIn(kotlinx.coroutines.FlowPreview::class)
@Composable
private fun AppContent(c: AppContainer, p: UserPrefs) {
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    var toast by remember { mutableStateOf<Toast?>(null) }
    var confettiKey by remember { mutableStateOf(0L) }
    val confirm = remember { ConfirmState() }
    val feedback = remember {
        object : Feedback {
            override fun toast(message: String, mood: Mood) { toast = Toast(System.nanoTime(), message, mood) }
            override fun celebrate(message: String) { toast = Toast(System.nanoTime(), message, Mood.COMEMORANDO); confettiKey = System.nanoTime() }
        }
    }

    // Conquistas: verifica a cada mudança nos dados e comemora as novas (a primeira leitura só registra).
    LaunchedEffect(Unit) {
        // debounce: várias mudanças seguidas (ex.: dados de exemplo) viram um único aviso
        c.snapshot.debounce(1_200).collect { s ->
            val novas = c.checkAchievements(s)
            when {
                novas.size == 1 -> Achievements.ALL.firstOrNull { it.id == novas[0] }?.let { feedback.celebrate("Conquista: ${it.emoji} ${it.title}") }
                novas.size > 1 -> feedback.celebrate("${novas.size} conquistas desbloqueadas! 🏆")
            }
        }
    }
    LaunchedEffect(toast) { val t = toast ?: return@LaunchedEffect; delay((2200L + t.text.length * 25L).coerceAtMost(4500L)); toast = null }
    LaunchedEffect(Unit) { runCatching { c.runRecurring() } }

    val back by nav.currentBackStackEntryAsState()
    val route = back?.destination?.route ?: ""
    LaunchedEffect(route, back?.arguments) {
        if (route.isNotBlank() && route != Routes.ONBOARDING) {
            val tab = back?.arguments?.getInt("tab") ?: 0
            val concrete = route.replace("{tab}", tab.toString()).replace("{id}", back?.arguments?.getString("id") ?: "")
            if (concrete != p.lastRoute) scope.launch { c.prefs.update { it.copy(lastRoute = concrete) } }
        }
    }
    val showBottom = p.onboarded && route != Routes.ONBOARDING

    ProvideFeedback(feedback, confirm) {
        Box(Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = { if (showBottom) BottomBar(nav, route) },
            ) { pad ->
                NavHost(nav, startDestination = if (p.onboarded) Routes.HOME else Routes.ONBOARDING, modifier = Modifier.padding(pad)) {
                    composable(Routes.ONBOARDING) { OnboardingScreen(c) { nav.navigate(it) { popUpTo(0) { inclusive = true } } } }
                    composable(Routes.HOME) { HomeScreen(c, nav) }
                    composable(Routes.JOURNEY) { JourneyScreen(c, nav) }
                    tabRoute(Routes.FINANCE) { FinanceScreen(c, nav, it) }
                    tabRoute(Routes.HOUSE) { HouseScreen(c, nav, it) }
                    composable(Routes.MORE) { MoreScreen(c, nav) }
                    tabRoute(Routes.MOVING) { MovingScreen(c, nav, it) }
                    tabRoute(Routes.MARKET) { MarketScreen(c, nav, it) }
                    tabRoute(Routes.FURNITURE) { FurnitureScreen(c, nav, it) }
                    tabRoute(Routes.SHARED) { SharedScreen(c, nav, it) }
                    composable(Routes.LIBRARY) { LibraryScreen(c, nav) }
                    composable(Routes.GUIDE, arguments = listOf(navArgument("id") { type = NavType.StringType })) { e -> GuideScreen(c, nav, e.arguments?.getString("id") ?: "") }
                    composable(Routes.CALCULATORS) { CalculatorsScreen(nav) }
                    composable(Routes.CALC, arguments = listOf(navArgument("id") { type = NavType.StringType })) { e -> CalculatorScreen(c, nav, e.arguments?.getString("id") ?: "") }
                    composable(Routes.ACHIEVEMENTS) { AchievementsScreen(c, nav) }
                    composable(Routes.REMINDERS) { RemindersScreen(c, nav) }
                    composable(Routes.SETTINGS) { SettingsScreen(c, nav) }
                    composable(Routes.PROFILE) { ProfileScreen(c, nav) }
                    composable(Routes.SEARCH) { SearchScreen(c, nav) }
                    composable(Routes.PLANS) { PlansScreen(nav) }
                    composable(Routes.CONTACTS) { ContactsScreen(c, nav) }
                    composable(Routes.CUSTOMIZE) { CustomizeScreen(c, nav) }
                    composable(Routes.TIMER) { TimerScreen(c, nav) }
                    composable(Routes.WEEKPLAN) { WeekPlanScreen(c, nav) }
                    composable(Routes.IMPORT) { ImportScreen(c, nav) }
                    composable(Routes.EMERGENCY) { br.com.casaclara.ui.screens.EmergencyScreen(c, nav) }
                    composable(Routes.DOCS) { br.com.casaclara.ui.screens.DocsScreen(c, nav) }
                    composable(Routes.CONSUMO) { br.com.casaclara.ui.screens.ConsumptionScreen(c, nav) }
                    composable(Routes.GOLPE) { br.com.casaclara.ui.screens.ScamScreen(c, nav) }
                    composable(Routes.QUIZ) { br.com.casaclara.ui.screens.QuizScreen(c, nav) }
                }
            }
            if (confettiKey != 0L) Confetti(confettiKey)
            ToastHost(toast, { toast = null }, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = if (showBottom) 88.dp else 16.dp, start = 16.dp, end = 16.dp))
            ConfirmHost(confirm)
        }
    }
}

private fun androidx.navigation.NavGraphBuilder.tabRoute(route: String, content: @Composable (Int) -> Unit) {
    composable(route, arguments = listOf(navArgument("tab") { type = NavType.IntType; defaultValue = 0 })) { e -> content(e.arguments?.getInt("tab") ?: 0) }
}

@Composable
private fun BottomBar(nav: NavHostController, route: String) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        BOTTOM.forEach { item ->
            val selected = route.startsWith(item.prefix) || (item.prefix == "more" && listOf("moving", "market", "furniture", "shared", "library", "guide", "achievements", "settings", "profile", "reminders", "calc", "search", "plans", "contacts", "customize", "timer", "weekplan", "import", "emergency", "docs", "consumo", "golpe", "quiz").any { route.startsWith(it) })
            NavigationBarItem(
                selected = selected,
                onClick = {
                    nav.navigate(item.route) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(item.label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
            )
        }
    }
}

@Composable
private fun ToastHost(t: Toast?, onDismiss: () -> Unit, modifier: Modifier) {
    AnimatedVisibility(t != null, modifier = modifier, enter = slideInVertically { it } + fadeIn(), exit = slideOutVertically { it } + fadeOut()) {
        val shown = t ?: return@AnimatedVisibility
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(2.dp, LocalSoft.current.contorno),
            modifier = Modifier.widthIn(max = 520.dp).cartoonShadow(LocalSoft.current.contorno, 18.dp).clickable(onClick = onDismiss).semantics { liveRegion = LiveRegionMode.Polite },
        ) {
            Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Mascot(shown.mood, 36.dp, describe = false)
                Spacer(Modifier.width(8.dp))
                Text(shown.text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
            }
        }
    }
}

@Composable
private fun Confetti(key: Long) {
    if (rememberReduceMotion()) return
    val progress = remember(key) { Animatable(0f) }
    LaunchedEffect(key) { progress.snapTo(0f); progress.animateTo(1f, tween(1700)) }
    if (progress.value >= 1f) return
    val colors = listOf(Brand.Amarelo, Brand.Verde, Brand.Azul, Brand.Coral, Color(0xFF9B7BE0))
    Canvas(Modifier.fillMaxSize()) {
        val n = 40
        for (i in 0 until n) {
            val x = size.width * ((i * 37 % 100) / 100f)
            val speed = 0.7f + (i % 5) * 0.12f
            val y = -20f + (size.height + 40f) * (progress.value * speed).coerceAtMost(1f)
            val w = 14f; val h = 20f
            drawRect(colors[i % colors.size], topLeft = Offset(x + 18f * kotlin.math.sin((progress.value * 10 + i).toDouble()).toFloat(), y), size = Size(w, h))
        }
    }
}
