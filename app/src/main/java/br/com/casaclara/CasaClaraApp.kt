package br.com.casaclara

import android.app.Application
import br.com.casaclara.data.AppContainer
import br.com.casaclara.notifications.ReminderScheduler
import kotlinx.coroutines.launch

class CasaClaraApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        ReminderScheduler.createChannel(this)
        container.scope.launch {
            runCatching { container.runRecurring() }
            runCatching { ReminderScheduler.rescheduleAll(this@CasaClaraApp, container.db.reminders().enabled()) }
        }
    }
}
