package br.com.casaclara.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TxEntity::class, BillEntity::class, DebtEntity::class, BudgetEntity::class, GoalEntity::class, CheckEntity::class,
        HouseTaskEntity::class, ShopEntity::class, PantryEntity::class, MealEntity::class, PropertyEntity::class,
        FurnitureEntity::class, OptionEntity::class, WarrantyEntity::class, ContactEntity::class, MemberEntity::class,
        SharedExpenseEntity::class, ReimbursementEntity::class, SharedTaskEntity::class, AgreementEntity::class,
        MilestoneEntity::class, ReminderEntity::class, GuideStateEntity::class, AchievementEntity::class,
    ],
    version = 1,
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

    companion object {
        const val NAME = "casaclara.db"
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, NAME).build()
    }
}
