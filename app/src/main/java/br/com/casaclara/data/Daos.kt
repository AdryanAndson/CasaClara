package br.com.casaclara.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao interface TxDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC") fun all(): Flow<List<TxEntity>>
    @Upsert suspend fun upsert(e: TxEntity): Long
    @Delete suspend fun delete(e: TxEntity)
    @Query("SELECT * FROM transactions ORDER BY date") suspend fun list(): List<TxEntity>
}

@Dao interface BillDao {
    @Query("SELECT * FROM bills ORDER BY dueDay") fun all(): Flow<List<BillEntity>>
    @Upsert suspend fun upsert(e: BillEntity): Long
    @Delete suspend fun delete(e: BillEntity)
}

@Dao interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY id DESC") fun all(): Flow<List<DebtEntity>>
    @Upsert suspend fun upsert(e: DebtEntity): Long
    @Delete suspend fun delete(e: DebtEntity)
}

@Dao interface BudgetDao {
    @Query("SELECT * FROM budgets ORDER BY category") fun all(): Flow<List<BudgetEntity>>
    @Upsert suspend fun upsert(e: BudgetEntity): Long
    @Delete suspend fun delete(e: BudgetEntity)
}

@Dao interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY id") fun all(): Flow<List<GoalEntity>>
    @Upsert suspend fun upsert(e: GoalEntity): Long
    @Delete suspend fun delete(e: GoalEntity)
    @Query("SELECT * FROM goals WHERE id = :id") suspend fun get(id: Long): GoalEntity?
}

@Dao interface CheckDao {
    @Query("SELECT * FROM checks WHERE list = :list ORDER BY id") fun byList(list: String): Flow<List<CheckEntity>>
    @Query("SELECT * FROM checks ORDER BY id") fun all(): Flow<List<CheckEntity>>
    @Upsert suspend fun upsert(e: CheckEntity): Long
    @Delete suspend fun delete(e: CheckEntity)
    @Query("UPDATE checks SET done = 0 WHERE list = :list AND (:grp IS NULL OR grp = :grp)") suspend fun reset(list: String, grp: String?)
    @Query("SELECT COUNT(*) FROM checks WHERE list = :list") suspend fun count(list: String): Int
}

@Dao interface HouseTaskDao {
    @Query("SELECT * FROM house_tasks ORDER BY id") fun all(): Flow<List<HouseTaskEntity>>
    @Upsert suspend fun upsert(e: HouseTaskEntity): Long
    @Delete suspend fun delete(e: HouseTaskEntity)
    @Query("UPDATE house_tasks SET lastDone = :day, doneCount = doneCount + 1 WHERE id = :id") suspend fun markDone(id: Long, day: Long)
}

@Dao interface ShopDao {
    @Query("SELECT * FROM shopping WHERE list = :list ORDER BY bought, category, name") fun byList(list: String): Flow<List<ShopEntity>>
    @Upsert suspend fun upsert(e: ShopEntity): Long
    @Delete suspend fun delete(e: ShopEntity)
    @Query("DELETE FROM shopping WHERE list = :list AND bought = 1 AND favorite = 0") suspend fun clearBought(list: String)
    @Query("UPDATE shopping SET bought = 0 WHERE list = :list AND bought = 1 AND favorite = 1") suspend fun resetFavorites(list: String)
}

@Dao interface PantryDao {
    @Query("SELECT * FROM pantry ORDER BY CASE WHEN expiry IS NULL THEN 1 ELSE 0 END, expiry, name") fun all(): Flow<List<PantryEntity>>
    @Upsert suspend fun upsert(e: PantryEntity): Long
    @Delete suspend fun delete(e: PantryEntity)
}

@Dao interface MealDao {
    @Query("SELECT * FROM meals ORDER BY day, mealType") fun all(): Flow<List<MealEntity>>
    @Upsert suspend fun upsert(e: MealEntity): Long
    @Delete suspend fun delete(e: MealEntity)
    @Query("DELETE FROM meals") suspend fun clear()
}

@Dao interface PropertyDao {
    @Query("SELECT * FROM properties ORDER BY id") fun all(): Flow<List<PropertyEntity>>
    @Upsert suspend fun upsert(e: PropertyEntity): Long
    @Delete suspend fun delete(e: PropertyEntity)
}

@Dao interface FurnitureDao {
    @Query("SELECT * FROM furniture ORDER BY bought, id") fun all(): Flow<List<FurnitureEntity>>
    @Upsert suspend fun upsert(e: FurnitureEntity): Long
    @Delete suspend fun delete(e: FurnitureEntity)
}

@Dao interface OptionDao {
    @Query("SELECT * FROM options ORDER BY item, price") fun all(): Flow<List<OptionEntity>>
    @Upsert suspend fun upsert(e: OptionEntity): Long
    @Delete suspend fun delete(e: OptionEntity)
}

@Dao interface WarrantyDao {
    @Query("SELECT * FROM warranties ORDER BY id DESC") fun all(): Flow<List<WarrantyEntity>>
    @Upsert suspend fun upsert(e: WarrantyEntity): Long
    @Delete suspend fun delete(e: WarrantyEntity)
}

@Dao interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY kind, name") fun all(): Flow<List<ContactEntity>>
    @Upsert suspend fun upsert(e: ContactEntity): Long
    @Delete suspend fun delete(e: ContactEntity)
    @Query("SELECT COUNT(*) FROM contacts WHERE phone = :phone") suspend fun countPhone(phone: String): Int
}

@Dao interface SharedDao {
    @Query("SELECT * FROM members ORDER BY id") fun members(): Flow<List<MemberEntity>>
    @Upsert suspend fun upsertMember(e: MemberEntity): Long
    @Delete suspend fun deleteMember(e: MemberEntity)
    @Query("SELECT * FROM shared_expenses ORDER BY date DESC, id DESC") fun expenses(): Flow<List<SharedExpenseEntity>>
    @Upsert suspend fun upsertExpense(e: SharedExpenseEntity): Long
    @Delete suspend fun deleteExpense(e: SharedExpenseEntity)
    @Query("SELECT * FROM reimbursements ORDER BY date DESC, id DESC") fun reimbursements(): Flow<List<ReimbursementEntity>>
    @Upsert suspend fun upsertReimbursement(e: ReimbursementEntity): Long
    @Delete suspend fun deleteReimbursement(e: ReimbursementEntity)
    @Query("SELECT * FROM shared_tasks ORDER BY id") fun tasks(): Flow<List<SharedTaskEntity>>
    @Upsert suspend fun upsertTask(e: SharedTaskEntity): Long
    @Delete suspend fun deleteTask(e: SharedTaskEntity)
    @Query("UPDATE shared_tasks SET done = 0") suspend fun resetTasks()
    @Query("SELECT * FROM agreements ORDER BY id") fun agreements(): Flow<List<AgreementEntity>>
    @Upsert suspend fun upsertAgreement(e: AgreementEntity): Long
    @Delete suspend fun deleteAgreement(e: AgreementEntity)
}

@Dao interface MilestoneDao {
    @Query("SELECT * FROM milestones ORDER BY date") fun all(): Flow<List<MilestoneEntity>>
    @Upsert suspend fun upsert(e: MilestoneEntity): Long
    @Delete suspend fun delete(e: MilestoneEntity)
}

@Dao interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY enabled DESC, date, minuteOfDay") fun all(): Flow<List<ReminderEntity>>
    @Query("SELECT * FROM reminders WHERE id = :id") suspend fun get(id: Long): ReminderEntity?
    @Query("SELECT * FROM reminders WHERE enabled = 1") suspend fun enabled(): List<ReminderEntity>
    @Upsert suspend fun upsert(e: ReminderEntity): Long
    @Delete suspend fun delete(e: ReminderEntity)
}

@Dao interface GuideDao {
    @Query("SELECT * FROM guide_state") fun all(): Flow<List<GuideStateEntity>>
    @Query("SELECT * FROM guide_state WHERE guideId = :id") suspend fun get(id: String): GuideStateEntity?
    @Upsert suspend fun upsert(e: GuideStateEntity)
    @Query("UPDATE guide_state SET lastRead = NULL, readCount = 0") suspend fun clearHistory()
}

@Dao interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY unlockedAt DESC") fun all(): Flow<List<AchievementEntity>>
    @Upsert suspend fun upsert(e: AchievementEntity)
}

@Dao interface StatsDao {
    @Query(
        """SELECT
        (SELECT COUNT(*) FROM transactions WHERE kind = 'ENTRADA') AS incomes,
        (SELECT COUNT(*) FROM transactions WHERE kind = 'SAIDA') AS expenses,
        (SELECT COUNT(*) FROM bills) AS bills,
        (SELECT COUNT(*) FROM bills WHERE paidMonth = :month) AS billsPaid,
        (SELECT COUNT(*) FROM debts) AS debts,
        (SELECT COUNT(*) FROM debts WHERE paid >= total) AS debtsPaidOff,
        (SELECT COUNT(*) FROM budgets) AS budgets,
        (SELECT COUNT(*) FROM goals WHERE type != 'RESERVA') AS goals,
        (SELECT COUNT(*) FROM goals WHERE saved >= target AND target > 0) AS goalsCompleted,
        (SELECT IFNULL(SUM(saved), 0) FROM goals WHERE type = 'RESERVA') AS reserveSaved,
        (SELECT COUNT(*) FROM checks WHERE list = 'PREP' AND done = 1) AS prepDone,
        (SELECT COUNT(*) FROM checks WHERE list = 'MOVING' AND done = 1) AS movingDone,
        (SELECT COUNT(*) FROM checks WHERE list = 'MOVING') AS movingTotal,
        (SELECT COUNT(*) FROM checks WHERE list = 'DOCS' AND done = 1) AS documentsDone,
        (SELECT COUNT(*) FROM checks WHERE list = 'VISIT' AND done = 1) AS visitDone,
        (SELECT COUNT(*) FROM checks WHERE list = 'QUESTIONS' AND done = 1) AS questionsDone,
        (SELECT COUNT(*) FROM properties) AS properties,
        (SELECT COUNT(*) FROM furniture) AS furniture,
        (SELECT COUNT(*) FROM furniture WHERE bought = 1) AS furnitureBought,
        (SELECT COUNT(*) FROM house_tasks) AS houseTasks,
        (SELECT IFNULL(SUM(doneCount), 0) FROM house_tasks) AS houseTasksDone,
        (SELECT COUNT(*) FROM (SELECT grp FROM checks WHERE list = 'CLEANING' GROUP BY grp HAVING SUM(done) = COUNT(*))) AS cleaningRoomsComplete,
        (SELECT COUNT(*) FROM shopping WHERE list = 'MERCADO') AS marketItems,
        (SELECT COUNT(*) FROM shopping WHERE bought = 1 AND list != 'INVENTARIO') AS marketBought,
        (SELECT COUNT(*) FROM shopping WHERE favorite = 1) AS favoritesMarket,
        (SELECT COUNT(*) FROM meals) AS meals,
        (SELECT COUNT(*) FROM pantry) AS pantry,
        (SELECT COUNT(*) FROM contacts) AS contacts,
        (SELECT COUNT(*) FROM contacts WHERE kind = 'EMERGENCIA') AS emergencyContacts,
        (SELECT COUNT(*) FROM warranties) + (SELECT COUNT(*) FROM furniture WHERE warrantyMonths IS NOT NULL) AS warranties,
        (SELECT COUNT(*) FROM members) AS members,
        (SELECT COUNT(*) FROM shared_expenses) AS sharedExpenses,
        (SELECT COUNT(*) FROM reimbursements) AS reimbursements,
        (SELECT COUNT(*) FROM agreements) AS agreements,
        (SELECT COUNT(*) FROM guide_state WHERE readCount > 0) AS guidesRead,
        (SELECT COUNT(*) FROM guide_state WHERE favorite = 1) AS guideFavorites,
        (SELECT COUNT(*) FROM reminders WHERE enabled = 1) AS remindersOn,
        (SELECT COUNT(*) FROM milestones) AS milestones"""
    )
    fun counts(month: Int): Flow<Counts>

    @Query("DELETE FROM transactions WHERE isDemo = 1") suspend fun d1()
    @Query("DELETE FROM bills WHERE isDemo = 1") suspend fun d2()
    @Query("DELETE FROM debts WHERE isDemo = 1") suspend fun d3()
    @Query("DELETE FROM budgets WHERE isDemo = 1") suspend fun d4()
    @Query("DELETE FROM goals WHERE isDemo = 1") suspend fun d5()
    @Query("DELETE FROM checks WHERE isDemo = 1") suspend fun d6()
    @Query("DELETE FROM house_tasks WHERE isDemo = 1") suspend fun d7()
    @Query("DELETE FROM shopping WHERE isDemo = 1") suspend fun d8()
    @Query("DELETE FROM pantry WHERE isDemo = 1") suspend fun d9()
    @Query("DELETE FROM meals WHERE isDemo = 1") suspend fun d10()
    @Query("DELETE FROM properties WHERE isDemo = 1") suspend fun d11()
    @Query("DELETE FROM furniture WHERE isDemo = 1") suspend fun d12()
    @Query("DELETE FROM options WHERE isDemo = 1") suspend fun d13()
    @Query("DELETE FROM warranties WHERE isDemo = 1") suspend fun d14()
    @Query("DELETE FROM contacts WHERE isDemo = 1") suspend fun d15()
    @Query("DELETE FROM shared_expenses WHERE isDemo = 1") suspend fun d16()
    @Query("DELETE FROM reimbursements WHERE isDemo = 1") suspend fun d17()
    @Query("DELETE FROM shared_tasks WHERE isDemo = 1") suspend fun d18()
    @Query("DELETE FROM agreements WHERE isDemo = 1") suspend fun d19()
    @Query("DELETE FROM milestones WHERE isDemo = 1") suspend fun d20()
    @Query("DELETE FROM members WHERE isDemo = 1") suspend fun d21()
}
