package br.com.casaclara.data

import android.content.Context
import android.net.Uri
import br.com.casaclara.domain.Achievements
import br.com.casaclara.domain.Csv
import br.com.casaclara.domain.Dates
import br.com.casaclara.domain.Money
import br.com.casaclara.domain.Snapshot
import br.com.casaclara.domain.content.Templates
import br.com.casaclara.notifications.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

/** Injeção de dependências simples: um único container para o app. */
class AppContainer(val context: Context) {
    val db: AppDatabase = AppDatabase.build(context)
    val prefs = Prefs(context)
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val snapshot: Flow<Snapshot> = combine(db.stats().counts(Dates.currentMonthKey()), prefs.flow) { c, p ->
        Snapshot(
            onboarded = p.onboarded,
            livingCostDone = (p.calc["custo"]?.values?.any { (Money.parse(it) ?: 0) > 0 }) == true,
            initialCostDone = (p.calc["inicial"]?.filterKeys { it != "caucao" && it != "primeiro" }?.values?.any { (Money.parse(it) ?: 0) > 0 }) == true,
            requiredIncomeDone = (Money.parse(p.calc["renda"]?.get("aluguel")) ?: 0) > 0,
            scenariosDone = (Money.parse(p.calc["cenarios"]?.get("custoSozinho")) ?: 0) > 0,
            incomes = c.incomes, expenses = c.expenses, bills = c.bills, billsPaidThisMonth = c.billsPaid,
            debts = c.debts, debtsPaidOff = c.debtsPaidOff, budgets = c.budgets, goals = c.goals, goalsCompleted = c.goalsCompleted,
            reserveSaved = c.reserveSaved, prepDone = c.prepDone, movingDone = c.movingDone, movingTotal = c.movingTotal,
            documentsDone = c.documentsDone, visitDone = c.visitDone, properties = c.properties, questionsDone = c.questionsDone,
            furniture = c.furniture, furnitureBought = c.furnitureBought, houseTasks = c.houseTasks, houseTasksDone = c.houseTasksDone,
            cleaningRoomsComplete = c.cleaningRoomsComplete, marketItems = c.marketItems, marketBought = c.marketBought,
            favoritesMarket = c.favoritesMarket, meals = c.meals, pantry = c.pantry, contacts = c.contacts,
            emergencyContacts = c.emergencyContacts, warranties = c.warranties, members = c.members,
            sharedExpenses = c.sharedExpenses, reimbursements = c.reimbursements, agreements = c.agreements,
            guidesRead = c.guidesRead, guideFavorites = c.guideFavorites, remindersOn = c.remindersOn, milestones = c.milestones,
        )
    }

    /** Registra conquistas novas e devolve as que acabaram de ser desbloqueadas. */
    suspend fun checkAchievements(s: Snapshot): List<String> {
        val have = db.achievements().all().first().map { it.id }.toSet()
        val novas = Achievements.unlocked(s) - have
        val now = System.currentTimeMillis()
        novas.forEach { db.achievements().upsert(AchievementEntity(it, now)) }
        return novas.toList()
    }

    // ------------------- ações com histórico -------------------
    suspend fun markTaskDone(t: HouseTaskEntity) {
        val today = Dates.todayEpoch()
        db.houseTasks().markDone(t.id, today)
        db.taskLog().insert(TaskLogEntity(taskId = t.id, title = t.title, date = today))
    }

    /** Guarda (ou retira, com valor negativo) dinheiro de uma meta, registrando no histórico. */
    suspend fun depositGoal(g: GoalEntity, amount: Long, note: String? = null) {
        val cur = db.goals().get(g.id) ?: g
        db.goals().upsert(cur.copy(saved = (cur.saved + amount).coerceAtLeast(0)))
        db.goalDeposits().insert(GoalDepositEntity(goalId = g.id, amount = amount, date = Dates.todayEpoch(), note = note))
    }

    /** Move dinheiro entre cofrinhos. */
    suspend fun transferGoal(from: GoalEntity, to: GoalEntity, amount: Long) {
        depositGoal(from, -amount, "Transferido para ${to.name}")
        depositGoal(to, amount, "Veio de ${from.name}")
    }

    /** Cria os lançamentos recorrentes que já venceram (idempotente). Devolve quantos foram criados. */
    suspend fun runRecurring(): Int {
        val today = Dates.today()
        var n = 0
        for (r in db.recurring().active()) {
            val months = br.com.casaclara.domain.Recurring.monthsToGenerate(r.startMonth, r.lastGenerated, r.day, today)
            if (months.isEmpty()) continue
            months.forEach { k ->
                db.tx().upsert(
                    TxEntity(kind = r.kind, description = r.description, amount = r.amount, date = Dates.dueInMonth(r.day, k).toEpochDay(), category = r.category,
                        incomeType = r.incomeType, cardId = r.cardId, payment = if (r.cardId != null) "Crédito" else null, recurringId = r.id, isDemo = r.isDemo),
                )
                n++
            }
            db.recurring().upsert(r.copy(lastGenerated = months.last()))
        }
        return n
    }

    // ------------------- listas sugeridas -------------------
    suspend fun loadChecklist(list: String) {
        val existing = db.checks().byList(list).first().map { it.grp + "|" + it.title }.toSet()
        val items: List<Pair<String, String>> = when (list) {
            "PREP" -> Templates.PREP.map { it.group to it.title }
            "MOVING" -> Templates.MOVING.map { it.group to it.title }
            "DOCS" -> Templates.DOCUMENTS.map { it.group to it.title }
            "VISIT" -> Templates.VISIT.map { "" to it }
            "QUESTIONS" -> Templates.QUESTIONS.map { it.group to it.title }
            "CLEANING" -> Templates.CLEANING.flatMap { (room, l) -> l.map { room to it } }
            else -> emptyList()
        }
        items.filter { (g, t) -> "$g|$t" !in existing }.forEach { (g, t) -> db.checks().upsert(CheckEntity(list = list, grp = g, title = t)) }
    }

    suspend fun loadHouseTasks() {
        val have = db.houseTasks().all().first().map { it.title }.toSet()
        Templates.HOUSE_TASKS.filter { it.title !in have }.forEach { db.houseTasks().upsert(HouseTaskEntity(title = it.title, frequency = it.frequency, room = it.room)) }
    }

    suspend fun loadEssentials() {
        val have = db.furniture().all().first().map { it.name }.toSet()
        Templates.ESSENTIALS.filter { it.name !in have }.forEach { db.furniture().upsert(FurnitureEntity(name = it.name, room = it.room, priority = it.priority)) }
    }

    suspend fun loadInventory() {
        val have = db.shop().byList("INVENTARIO").first().map { it.name }.toSet()
        Templates.INVENTORY.filter { it.first !in have }.forEach { db.shop().upsert(ShopEntity(list = "INVENTARIO", name = it.first, category = it.second, qty = 1.0, state = "OK")) }
    }

    suspend fun loadAgreements() {
        val have = db.shared().agreements().first().map { it.title }.toSet()
        Templates.AGREEMENTS.filter { it.first !in have }.forEach { db.shared().upsertAgreement(AgreementEntity(title = it.first, text = it.second)) }
    }

    suspend fun loadEmergencyContacts() {
        Templates.EMERGENCY.forEach { (nome, tel, obs) ->
            if (db.contacts().countPhone(tel) == 0) db.contacts().upsert(ContactEntity(name = nome, kind = "EMERGENCIA", service = obs, phone = tel))
        }
    }

    // ------------------- dados de demonstração -------------------
    suspend fun loadDemo() {
        DemoData.load(db, Dates.today())
        prefs.update { it.copy(demoLoaded = true) }
    }

    suspend fun removeDemo() {
        val s = db.stats()
        s.d1(); s.d2(); s.d3(); s.d4(); s.d5(); s.d6(); s.d7(); s.d8(); s.d9(); s.d10(); s.d11()
        s.d12(); s.d13(); s.d14(); s.d15(); s.d16(); s.d17(); s.d18(); s.d19(); s.d20(); s.d21(); s.d22(); s.d23()
        prefs.update { it.copy(demoLoaded = false) }
    }

    // ------------------- apagar / reiniciar -------------------
    /** Reinicia a jornada sem apagar registros financeiros nem listas. */
    suspend fun restartJourney() = prefs.update { it.copy(onboarded = false, onboardingStep = 0, manualSteps = emptySet(), skippedSteps = emptySet(), journeyPaused = false) }

    suspend fun deleteEverything() = withContext(Dispatchers.IO) {
        db.reminders().enabled().forEach { ReminderScheduler.cancel(context, it.id) }
        db.clearAllTables()
        prefs.clearAll()
    }

    // ------------------- exportação e cópia de segurança -------------------
    suspend fun exportFinanceCsv(uri: Uri) = withContext(Dispatchers.IO) {
        val rows = db.tx().list().map {
            listOf(Dates.br(it.date), if (it.kind == "ENTRADA") "Entrada" else "Gasto", it.description, it.category,
                Csv.money(if (it.kind == "ENTRADA") it.amount else -it.amount), if (it.isDemo) "sim" else "")
        }
        val csv = "﻿" + Csv.build(listOf("data", "tipo", "descricao", "categoria", "valor", "demo"), rows)
        context.contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray(Charsets.UTF_8)) } ?: error("Não foi possível abrir o arquivo")
    }

    /** Copia o banco de dados para o arquivo escolhido pela pessoa (no próprio aparelho ou onde ela preferir). */
    suspend fun backup(uri: Uri) = withContext(Dispatchers.IO) {
        db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
        val file = context.getDatabasePath(AppDatabase.NAME)
        context.contentResolver.openOutputStream(uri)?.use { out -> file.inputStream().use { it.copyTo(out) } } ?: error("Não foi possível salvar")
    }

    /** Restaura um backup. Valida o arquivo antes de substituir. O app precisa reiniciar em seguida. */
    suspend fun restore(uri: Uri) = withContext(Dispatchers.IO) {
        val tmp = File(context.cacheDir, "restore.db")
        context.contentResolver.openInputStream(uri)?.use { input -> tmp.outputStream().use { input.copyTo(it) } } ?: error("Não foi possível ler o arquivo")
        val header = tmp.inputStream().use { s -> ByteArray(16).also { s.read(it) } }
        if (String(header, Charsets.US_ASCII) != "SQLite format 3\u0000") { tmp.delete(); error("Esse arquivo não é um backup do Casa Clara.") }
        db.close()
        val target = context.getDatabasePath(AppDatabase.NAME)
        File(target.path + "-wal").delete(); File(target.path + "-shm").delete()
        tmp.copyTo(target, overwrite = true)
        tmp.delete()
    }
}
