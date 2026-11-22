package br.com.casaclara.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import br.com.casaclara.CasaClaraApp
import br.com.casaclara.MainActivity
import br.com.casaclara.R
import br.com.casaclara.data.ReminderEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * Lembretes 100% locais: o WorkManager agenda e o aparelho mostra a notificação.
 * Nada é enviado para servidores.
 */
object ReminderScheduler {
    const val CHANNEL = "lembretes"
    private const val KEY_ID = "reminder_id"
    private fun workName(id: Long) = "reminder-$id"

    fun createChannel(context: Context) {
        val ch = NotificationChannel(CHANNEL, "Lembretes", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Contas, tarefas da casa, metas, mudança e manutenção"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
    }

    fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    /** Próximo disparo a partir de agora, respeitando a repetição. null = já passou e não repete. */
    fun nextTrigger(r: ReminderEntity, now: LocalDateTime = LocalDateTime.now()): LocalDateTime? {
        val time = LocalTime.of((r.minuteOfDay / 60).coerceIn(0, 23), (r.minuteOfDay % 60).coerceIn(0, 59))
        var t = LocalDateTime.of(LocalDate.ofEpochDay(r.date), time)
        var guard = 0
        while (!t.isAfter(now) && guard++ < 5000) {
            t = when (r.repeat) {
                "DIARIO" -> t.plusDays(1)
                "SEMANAL" -> t.plusWeeks(1)
                "MENSAL" -> t.plusMonths(1)
                else -> return null
            }
        }
        return t
    }

    fun schedule(context: Context, r: ReminderEntity) {
        val wm = WorkManager.getInstance(context)
        if (!r.enabled) { wm.cancelUniqueWork(workName(r.id)); return }
        val next = nextTrigger(r) ?: run { wm.cancelUniqueWork(workName(r.id)); return }
        val delay = java.time.Duration.between(LocalDateTime.now(), next).toMillis().coerceAtLeast(0)
        val req = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(KEY_ID to r.id))
            .build()
        wm.enqueueUniqueWork(workName(r.id), ExistingWorkPolicy.REPLACE, req)
    }

    fun cancel(context: Context, id: Long) = WorkManager.getInstance(context).cancelUniqueWork(workName(id))

    fun show(context: Context, id: Long, title: String, text: String) {
        if (!canNotify(context)) return
        val open = PendingIntent.getActivity(
            context, id.toInt(), Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val n = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        try { NotificationManagerCompat.from(context).notify(id.toInt(), n) } catch (_: SecurityException) { }
    }

    class ReminderWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
        override suspend fun doWork(): Result {
            val id = inputData.getLong(KEY_ID, -1)
            val app = applicationContext as CasaClaraApp
            val r = app.container.db.reminders().get(id) ?: return Result.success()
            if (!r.enabled) return Result.success()
            val prefs = app.container.prefs.current()
            if (prefs.notifications) {
                val msg = r.message?.takeIf { it.isNotBlank() } ?: when (r.kind) {
                    "CONTA" -> "Hora de conferir essa conta. Pagou? Marque no app!"
                    "TAREFA" -> "Essa tarefa parece chata, mas vai deixar sua vida mais fácil."
                    "META" -> "Que tal guardar um pouquinho para a sua meta hoje?"
                    "MUDANCA" -> "Tem coisa da mudança para hoje."
                    "MANUTENCAO" -> "Hora de um cuidado preventivo na casa."
                    else -> "Lembrete do Casa Clara."
                }
                show(applicationContext, r.id, r.title, msg)
            }
            if (r.repeat == "NENHUM") app.container.db.reminders().upsert(r.copy(enabled = false))
            else schedule(applicationContext, r)
            return Result.success()
        }
    }

    // ---------- timer da faxina ----------
    private const val TIMER_WORK = "faxina-timer"
    fun scheduleTimerDone(context: Context, delayMs: Long, room: String) {
        val req = OneTimeWorkRequestBuilder<TimerWorker>().setInitialDelay(delayMs.coerceAtLeast(0), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("room" to room)).build()
        WorkManager.getInstance(context).enqueueUniqueWork(TIMER_WORK, ExistingWorkPolicy.REPLACE, req)
    }
    fun cancelTimer(context: Context) = WorkManager.getInstance(context).cancelUniqueWork(TIMER_WORK)

    class TimerWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
        override suspend fun doWork(): Result {
            val app = applicationContext as CasaClaraApp
            if (app.container.prefs.current().notifications) {
                show(applicationContext, 999_001, "Faxina concluída! ✨", "Tempo de ${inputData.getString("room") ?: "faxina"} acabou. Pode descansar — você mandou bem.")
            }
            return Result.success()
        }
    }

    /** Reagenda tudo (ao abrir o app ou após restaurar backup). */
    suspend fun rescheduleAll(context: Context, list: List<ReminderEntity>) = list.forEach { schedule(context, it) }
}

