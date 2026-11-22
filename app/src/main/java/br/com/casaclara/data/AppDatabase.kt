package br.com.casaclara.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TxEntity::class, BillEntity::class, DebtEntity::class, BudgetEntity::class, GoalEntity::class, CheckEntity::class,
        HouseTaskEntity::class, ShopEntity::class, PantryEntity::class, MealEntity::class, PropertyEntity::class,
        FurnitureEntity::class, OptionEntity::class, WarrantyEntity::class, ContactEntity::class, MemberEntity::class,
        SharedExpenseEntity::class, ReimbursementEntity::class, SharedTaskEntity::class, AgreementEntity::class,
        MilestoneEntity::class, ReminderEntity::class, GuideStateEntity::class, AchievementEntity::class,
        CardEntity::class, RecurringEntity::class, GoalDepositEntity::class, TaskLogEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tx(): TxDao
    abstract fun bills(): BillDao
    abstract fun debts(): DebtDao
    abstract fun budgets(): BudgetDao
    abstract fun goals(): GoalDao
    abstract fun checks(): CheckDao
    abstract fun houseTasks(): HouseTaskDao
    abstract fun shop(): ShopDao
    abstract fun pantry(): PantryDao
    abstract fun meals(): MealDao
    abstract fun properties(): PropertyDao
    abstract fun furniture(): FurnitureDao
    abstract fun options(): OptionDao
    abstract fun warranties(): WarrantyDao
    abstract fun contacts(): ContactDao
    abstract fun shared(): SharedDao
    abstract fun milestones(): MilestoneDao
    abstract fun reminders(): ReminderDao
    abstract fun guides(): GuideDao
    abstract fun achievements(): AchievementDao
    abstract fun stats(): StatsDao
    abstract fun cards(): CardDao
    abstract fun recurring(): RecurringDao
    abstract fun goalDeposits(): GoalDepositDao
    abstract fun taskLog(): TaskLogDao

    companion object {
        const val NAME = "casaclara.db"
        /** v1 → v2: novas tabelas e colunas. Nada é apagado. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `transactions` ADD COLUMN `cardId` INTEGER")
                db.execSQL("ALTER TABLE `transactions` ADD COLUMN `installments` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `transactions` ADD COLUMN `recurringId` INTEGER")
                db.execSQL("CREATE TABLE IF NOT EXISTS `cards` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `limitAmount` INTEGER, `closingDay` INTEGER NOT NULL, `dueDay` INTEGER NOT NULL, `color` TEXT NOT NULL, `paidInvoices` TEXT NOT NULL, `isDemo` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `recurring` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `kind` TEXT NOT NULL, `description` TEXT NOT NULL, `amount` INTEGER NOT NULL, `category` TEXT NOT NULL, `day` INTEGER NOT NULL, `startMonth` INTEGER NOT NULL, `lastGenerated` INTEGER, `active` INTEGER NOT NULL, `cardId` INTEGER, `incomeType` TEXT, `isDemo` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `goal_deposits` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `goalId` INTEGER NOT NULL, `amount` INTEGER NOT NULL, `date` INTEGER NOT NULL, `note` TEXT)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `task_log` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `taskId` INTEGER NOT NULL, `title` TEXT NOT NULL, `date` INTEGER NOT NULL)")
            }
        }

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, NAME).addMigrations(MIGRATION_1_2).build()
    }
}
