package br.com.casaclara.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import br.com.casaclara.domain.MainGoal
import br.com.casaclara.domain.Situation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "casaclara_prefs")

enum class ThemeMode(val label: String) { SISTEMA("Automático"), CLARO("Claro"), ESCURO("Escuro") }

/** Preferências e perfil da jornada (DataStore). Registros estruturados ficam no Room. */
data class UserPrefs(
    val onboarded: Boolean = false,
    val onboardingStep: Int = 0,
    val name: String = "",
    val situation: Situation? = null,
    val goal: MainGoal? = null,
    val interests: Set<String> = emptySet(),
    val income: Long? = null,
    val saved: Long? = null,
    val moveGoal: Long? = null,
    val theme: ThemeMode = ThemeMode.SISTEMA,
    val textScale: Float = 1f,
    val notifications: Boolean = true,
    val journeyPaused: Boolean = false,
    val manualSteps: Set<String> = emptySet(),
    val skippedSteps: Set<String> = emptySet(),
    val lastRoute: String = "",
    val demoLoaded: Boolean = false,
    val calc: Map<String, Map<String, String>> = emptyMap(),
    /** Início personalizável: ordem e cartões escondidos; atalhos favoritos. Vazio = padrão. */
    val homeOrder: List<String> = emptyList(),
    val homeHidden: Set<String> = emptySet(),
    val shortcuts: List<String> = emptyList(),
    val cleanMinutes: Int = 15,
)

class Prefs(private val context: Context) {
    private object K {
        val onboarded = booleanPreferencesKey("onboarded")
        val step = longPreferencesKey("onboarding_step")
        val name = stringPreferencesKey("name")
        val situation = stringPreferencesKey("situation")
        val goal = stringPreferencesKey("goal")
        val interests = stringPreferencesKey("interests")
        val income = longPreferencesKey("income")
        val saved = longPreferencesKey("saved")
        val moveGoal = longPreferencesKey("move_goal")
        val theme = stringPreferencesKey("theme")
        val textScale = floatPreferencesKey("text_scale")
        val notifications = booleanPreferencesKey("notifications")
        val paused = booleanPreferencesKey("journey_paused")
        val manual = stringPreferencesKey("manual_steps")
        val skipped = stringPreferencesKey("skipped_steps")
        val lastRoute = stringPreferencesKey("last_route")
        val demo = booleanPreferencesKey("demo_loaded")
        val homeOrder = stringPreferencesKey("home_order")
        val homeHidden = stringPreferencesKey("home_hidden")
        val shortcuts = stringPreferencesKey("shortcuts")
        val cleanMinutes = longPreferencesKey("clean_minutes")
        val calcPrefix = "calc_"
    }

    val flow: Flow<UserPrefs> = context.dataStore.data.map { p ->
        UserPrefs(
            onboarded = p[K.onboarded] ?: false,
            onboardingStep = (p[K.step] ?: 0L).toInt(),
            name = p[K.name] ?: "",
            situation = p[K.situation]?.let { runCatching { Situation.valueOf(it) }.getOrNull() },
            goal = p[K.goal]?.let { runCatching { MainGoal.valueOf(it) }.getOrNull() },
            interests = p[K.interests].toSet(),
            income = p[K.income],
            saved = p[K.saved],
            moveGoal = p[K.moveGoal],
            theme = p[K.theme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SISTEMA,
            textScale = p[K.textScale] ?: 1f,
            notifications = p[K.notifications] ?: true,
            journeyPaused = p[K.paused] ?: false,
            manualSteps = p[K.manual].toSet(),
            skippedSteps = p[K.skipped].toSet(),
            lastRoute = p[K.lastRoute] ?: "",
            demoLoaded = p[K.demo] ?: false,
            calc = p.asMap().filterKeys { it.name.startsWith(K.calcPrefix) }
                .mapNotNull { (k, v) -> (v as? String)?.let { k.name.removePrefix(K.calcPrefix) to decodeMap(it) } }.toMap(),
            homeOrder = p[K.homeOrder].toList(),
            homeHidden = p[K.homeHidden].toSet(),
            shortcuts = p[K.shortcuts].toList(),
            cleanMinutes = (p[K.cleanMinutes] ?: 15L).toInt(),
        )
    }

    suspend fun current(): UserPrefs = flow.first()

    suspend fun update(block: (UserPrefs) -> UserPrefs) {
        context.dataStore.edit { p ->
            val cur = UserPrefs(
                onboarded = p[K.onboarded] ?: false, onboardingStep = (p[K.step] ?: 0L).toInt(), name = p[K.name] ?: "",
                situation = p[K.situation]?.let { runCatching { Situation.valueOf(it) }.getOrNull() },
                goal = p[K.goal]?.let { runCatching { MainGoal.valueOf(it) }.getOrNull() },
                interests = p[K.interests].toSet(), income = p[K.income], saved = p[K.saved], moveGoal = p[K.moveGoal],
                theme = p[K.theme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SISTEMA,
                textScale = p[K.textScale] ?: 1f, notifications = p[K.notifications] ?: true, journeyPaused = p[K.paused] ?: false,
                manualSteps = p[K.manual].toSet(), skippedSteps = p[K.skipped].toSet(), lastRoute = p[K.lastRoute] ?: "", demoLoaded = p[K.demo] ?: false,
                homeOrder = p[K.homeOrder].toList(), homeHidden = p[K.homeHidden].toSet(), shortcuts = p[K.shortcuts].toList(), cleanMinutes = (p[K.cleanMinutes] ?: 15L).toInt(),
            )
            val n = block(cur)
            p[K.onboarded] = n.onboarded
            p[K.step] = n.onboardingStep.toLong()
            p[K.name] = n.name
            if (n.situation != null) p[K.situation] = n.situation.name else p.remove(K.situation)
            if (n.goal != null) p[K.goal] = n.goal.name else p.remove(K.goal)
            p[K.interests] = n.interests.joinToString(",")
            if (n.income != null) p[K.income] = n.income else p.remove(K.income)
            if (n.saved != null) p[K.saved] = n.saved else p.remove(K.saved)
            if (n.moveGoal != null) p[K.moveGoal] = n.moveGoal else p.remove(K.moveGoal)
            p[K.theme] = n.theme.name
            p[K.textScale] = n.textScale
            p[K.notifications] = n.notifications
            p[K.paused] = n.journeyPaused
            p[K.manual] = n.manualSteps.joinToString(",")
            p[K.skipped] = n.skippedSteps.joinToString(",")
            p[K.lastRoute] = n.lastRoute
            p[K.demo] = n.demoLoaded
            p[K.homeOrder] = n.homeOrder.joinToString(",")
            p[K.homeHidden] = n.homeHidden.joinToString(",")
            p[K.shortcuts] = n.shortcuts.joinToString(",")
            p[K.cleanMinutes] = n.cleanMinutes.toLong()
        }
    }

    suspend fun setCalc(id: String, values: Map<String, String>) {
        context.dataStore.edit { it[stringPreferencesKey(K.calcPrefix + id)] = encodeMap(values) }
    }

    suspend fun clearAll() { context.dataStore.edit { it.clear() } }

    suspend fun clearCalcs() {
        context.dataStore.edit { p -> p.asMap().keys.filter { it.name.startsWith(K.calcPrefix) }.forEach { k -> p.remove(k) } }
    }

    private fun String?.toSet(): Set<String> = this?.split(',')?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
    private fun String?.toList(): List<String> = this?.split(',')?.filter { it.isNotBlank() } ?: emptyList()

    companion object {
        fun encodeMap(m: Map<String, String>): String =
            m.entries.joinToString("&") { java.net.URLEncoder.encode(it.key, "UTF-8") + "=" + java.net.URLEncoder.encode(it.value, "UTF-8") }

        fun decodeMap(s: String): Map<String, String> = if (s.isBlank()) emptyMap() else
            s.split('&').mapNotNull { part ->
                val i = part.indexOf('=')
                if (i < 0) null else java.net.URLDecoder.decode(part.substring(0, i), "UTF-8") to java.net.URLDecoder.decode(part.substring(i + 1), "UTF-8")
            }.toMap()
    }
}
