package br.com.casaclara.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import br.com.casaclara.CasaClaraApp
import br.com.casaclara.MainActivity
import br.com.casaclara.R
import br.com.casaclara.domain.Dates
import br.com.casaclara.domain.Finance
import br.com.casaclara.domain.Money
import br.com.casaclara.ui.billStatus
import br.com.casaclara.ui.taskStatus
import kotlinx.coroutines.flow.first

/** Widget da tela inicial: tarefas de hoje, próximas contas e o saldo do mês. Toque para abrir o app. */
class CasaClaraWidget : GlanceAppWidget() {
    private data class Data(val tasks: List<String>, val bills: List<String>, val sobrou: Long?)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val c = (context.applicationContext as CasaClaraApp).container
        val tasks = c.db.houseTasks().all().first().map { it to taskStatus(it) }.filter { it.second.days <= 0 }.sortedBy { it.second.days }.map { it.first.title }
        val bills = c.db.bills().all().first().map { it to billStatus(it) }.filter { !it.second.paid && it.second.days <= 7 }.sortedBy { it.second.days }
            .map { (b, s) -> "${b.name} · ${if (s.late) "atrasada" else Dates.relative(s.days)}" }
        val tx = c.db.tx().all().first()
        val sum = if (tx.isEmpty()) null else Finance.summary(
            tx.filter { it.kind == "ENTRADA" }.map { Finance.Movement(it.amount, it.date) }, tx.filter { it.kind == "SAIDA" }.map { Finance.Movement(it.amount, it.date) }, Dates.currentMonthKey(),
        ).sobrou
        val data = Data(tasks, bills, sum)
        provideContent { Content(data) }
    }

    @androidx.compose.runtime.Composable
    private fun Content(d: Data) {
        val ink = ColorProvider(Color(0xFF1C2B39))
        val title = TextStyle(color = ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        val body = TextStyle(color = ink, fontSize = 13.sp)
        Column(
            GlanceModifier.fillMaxSize().background(Color(0xFFFFEDA6)).cornerRadius(22.dp).padding(12.dp).clickable(actionStartActivity<MainActivity>()),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = GlanceModifier.fillMaxWidth()) {
                Image(ImageProvider(R.drawable.ic_launcher_foreground), contentDescription = "Casa Clara", modifier = GlanceModifier.size(40.dp))
                Spacer(GlanceModifier.width(6.dp))
                Column {
                    Text("Casa Clara", style = title)
                    d.sobrou?.let { Text("Sobrou no mês: ${Money.format(it)}", style = body) }
                }
            }
            Spacer(GlanceModifier.height(6.dp))
            Text("🧹 Para hoje", style = title)
            if (d.tasks.isEmpty()) Text("Casa em dia! ✨", style = body)
            d.tasks.take(3).forEach { Text("• $it", style = body, maxLines = 1) }
            Spacer(GlanceModifier.height(4.dp))
            Text("📅 Contas", style = title)
            if (d.bills.isEmpty()) Text("Nada vencendo nos próximos dias.", style = body)
            d.bills.take(2).forEach { Text("• $it", style = body, maxLines = 1) }
        }
    }
}

class CasaClaraWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CasaClaraWidget()
}
