package br.com.casaclara

import android.app.Application
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import br.com.casaclara.data.AppContainer
import br.com.casaclara.notifications.ReminderScheduler
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.debounce
import androidx.glance.appwidget.updateAll
import br.com.casaclara.widget.CasaClaraWidget

@OptIn(kotlinx.coroutines.FlowPreview::class)
class CasaClaraApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        // Fonte de emojis embutida: só substitui os emojis que o sistema não conhece (Android antigo).
        EmojiCompat.init(BundledEmojiCompatConfig(this).setReplaceAll(false))
        container = AppContainer(this)
        ReminderScheduler.createChannel(this)
        container.scope.launch {
            runCatching { container.runRecurring() }
            runCatching { ReminderScheduler.rescheduleAll(this@CasaClaraApp, container.db.reminders().enabled()) }
        }
        // Mantém o widget da tela inicial atualizado (com intervalo para não pesar a bateria).
        container.scope.launch {
            container.snapshot.debounce(2_000).collect { runCatching { CasaClaraWidget().updateAll(this@CasaClaraApp) } }
        }
    }
}
