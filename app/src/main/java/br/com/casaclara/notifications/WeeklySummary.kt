package br.com.casaclara.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import br.com.casaclara.CasaClaraApp
import br.com.casaclara.domain.Dates
import br.com.casaclara.domain.Finance
import br.com.casaclara.domain.Money
import br.com.casaclara.domain.Weekly
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit

/** Resumo da semana, todo domingo às 19h (opcional, nas Configurações). */
object WeeklySummary {
    private const val NAME = "resumo-semanal"

    fun schedule(context: Context, on: Boolean) {
        val wm = WorkManager.getInstance(context)
        if (!on) { wm.cancelUniqueWork(NAME); return }
        val now = LocalDateTime.now()
        var next = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).withHour(19).withMinute(0).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusWeeks(1)
        val delay = java.time.Duration.between(now, next).toMillis()
        val req = PeriodicWorkRequestBuilder<Worker>(7, TimeUnit.DAYS).setInitialDelay(delay, TimeUnit.MILLISECONDS).build()
        wm.enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.UPDATE, req)
    }

    class Worker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
        override suspend fun doWork(): Result {
            val c = (applicationContext as CasaClaraApp).container
            val tx = c.db.tx().all().first()
            val today = Dates.today()
            val s = Weekly.summary(
                tx.filter { it.kind == "SAIDA" }.map { Finance.Movement(it.amount, it.date, it.category) },
                tx.filter { it.kind == "ENTRADA" }.map { Finance.Movement(it.amount, it.date, it.category) }, today,
            )
            val done = c.db.taskLog().since(Weekly.weekStart(today).toEpochDay()).first().size
            val hide = br.com.casaclara.data.AppSettings.from(c.prefs.current().calc[br.com.casaclara.data.AppSettings.KEY].orEmpty()).hideValues
            val gasto = if (hide) "seus gastos" else "você gastou ${Money.plain(s.spent)}"
            val cmp = when { s.spentLastWeek == 0L -> ""; s.diff < 0 -> " — menos que na semana passada 👏"; s.diff > 0 -> " — um pouco mais que na semana passada"; else -> "" }
            ReminderScheduler.show(applicationContext, 999_002, "Sua semana no Casa Clara",
                (if (hide) "Veja $gasto da semana" else "Nesta semana $gasto$cmp") + ". Tarefas feitas: $done.")
            return Result.success()
        }
    }
}
