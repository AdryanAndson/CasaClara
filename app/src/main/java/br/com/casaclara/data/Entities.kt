package br.com.casaclara.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/* Valores em centavos (Long). Datas em epochDay (Long). isDemo marca dados de demonstração removíveis. */

@Entity(tableName = "transactions")
data class TxEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: String, // ENTRADA | SAIDA
    val description: String,
    val amount: Long,
    val date: Long,
    val category: String = "Outros",
    val incomeType: String? = null, // FIXA | VARIAVEL | INFORMAL
    val payment: String? = null,
    val isDemo: Boolean = false,
    /** v2: compra no cartão de crédito (id do cartão) e número de parcelas na fatura. */
    val cardId: Long? = null,
    val installments: Int = 1,
    /** v2: gerado automaticamente por um lançamento recorrente. */
    val recurringId: Long? = null,
)

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Long? = null,
    val dueDay: Int,
    val category: String = "Contas da casa",
    val autoDebit: Boolean = false,
    val paidMonth: Int? = null,
    val isDemo: Boolean = false,
)

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val creditor: String? = null,
    val kind: String = "PARCELADO", // CARTAO | EMPRESTIMO | PARCELADO | OUTRO
    val installment: Long,
    val total: Int,
    val paid: Int = 0,
    val dueDay: Int? = null,
    val monthlyRate: Double? = null, // % ao mês
    val note: String? = null,
    val isDemo: Boolean = false,
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val limitAmount: Long,
    val isDemo: Boolean = false,
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String = "OUTRO", // RESERVA | MUDANCA | COMPRA | OUTRO
    val target: Long,
    val saved: Long = 0,
    val monthly: Long? = null,
    val ratePct: Double? = null,
    val deadline: Long? = null,
    val isDemo: Boolean = false,
)

/** Listas marcáveis genéricas: preparação, mudança, documentos, visita, perguntas, limpeza por cômodo. */
@Entity(tableName = "checks")
data class CheckEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val list: String,
    val grp: String = "",
    val title: String,
    val note: String? = null,
    val done: Boolean = false,
    val isDemo: Boolean = false,
)

@Entity(tableName = "house_tasks")
data class HouseTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val frequency: String = "SEMANAL", // DIARIA | SEMANAL | QUINZENAL | MENSAL | SEMESTRAL
    val room: String? = null,
    val lastDone: Long? = null,
    val doneCount: Int = 0,
    val isDemo: Boolean = false,
)

@Entity(tableName = "shopping")
data class ShopEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val list: String = "MERCADO", // MERCADO | CASA | INVENTARIO
    val name: String,
    val category: String = "Outros",
    val qty: Double = 1.0,
    val unit: String = "un",
    val price: Long? = null,
    val bought: Boolean = false,
    val favorite: Boolean = false,
    /** Só para o inventário: OK | ACABANDO | FALTA */
    val state: String = "OK",
    val isDemo: Boolean = false,
)

@Entity(tableName = "pantry")
data class PantryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val qty: Double? = null,
    val unit: String = "un",
    val location: String = "Armário",
    val expiry: Long? = null,
    val isDemo: Boolean = false,
)

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val day: Int, // 0 = segunda
    val mealType: String,
    val dish: String,
    val note: String? = null,
    val isDemo: Boolean = false,
)

@Entity(tableName = "properties")
data class PropertyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val address: String? = null,
    val rent: Long,
    val condo: Long? = null,
    val iptu: Long? = null,
    val area: Double? = null,
    val bedrooms: Int? = null,
    val distance: String? = null,
    val transport: Long? = null,
    val guarantee: String? = null,
    val pets: Boolean = false,
    val furnished: Boolean = false,
    val rating: Int? = null,
    val pros: String? = null,
    val cons: String? = null,
    val link: String? = null,
    val isDemo: Boolean = false,
)

@Entity(tableName = "furniture")
data class FurnitureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val room: String? = null,
    val priority: String = "AGORA", // AGORA | DEPOIS | LEGAL
    val estimate: Long? = null,
    val bought: Boolean = false,
    val paid: Long? = null,
    val installments: Int? = null,
    val installmentValue: Long? = null,
    val store: String? = null,
    val purchaseDate: Long? = null,
    val warrantyMonths: Int? = null,
    val invoice: String? = null,
    val isDemo: Boolean = false,
)

@Entity(tableName = "options")
data class OptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val item: String,
    val option: String,
    val store: String? = null,
    val price: Long,
    val shipping: Long? = null,
    val installments: Int? = null,
    val rating: Int? = null,
    val link: String? = null,
    val note: String? = null,
    val isDemo: Boolean = false,
)

@Entity(tableName = "warranties")
data class WarrantyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val product: String,
    val store: String? = null,
    val purchaseDate: Long? = null,
    val months: Int? = null,
    val value: Long? = null,
    val invoice: String? = null,
    val note: String? = null,
    val isDemo: Boolean = false,
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val kind: String = "PROFISSIONAL", // PROFISSIONAL | EMERGENCIA | IMPORTANTE
    val service: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val note: String? = null,
    val isDemo: Boolean = false,
)

@Entity(tableName = "members")
data class MemberEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val emoji: String? = null, val isDemo: Boolean = false)

@Entity(tableName = "shared_expenses")
data class SharedExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val amount: Long,
    val date: Long,
    val paidBy: Long,
    val splitAmong: String, // ids separados por vírgula
    val isDemo: Boolean = false,
) {
    fun splitIds(): List<Long> = splitAmong.split(',').mapNotNull { it.trim().toLongOrNull() }
}

@Entity(tableName = "reimbursements")
data class ReimbursementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fromId: Long, val toId: Long, val amount: Long, val date: Long, val note: String? = null, val isDemo: Boolean = false,
)

@Entity(tableName = "shared_tasks")
data class SharedTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String, val assignee: Long? = null, val frequency: String = "SEMANAL", val done: Boolean = false, val isDemo: Boolean = false,
)

@Entity(tableName = "agreements")
data class AgreementEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val title: String, val text: String, val isDemo: Boolean = false)

@Entity(tableName = "milestones")
data class MilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, val title: String, val date: Long, val done: Boolean = false, val note: String? = null, val isDemo: Boolean = false,
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String? = null,
    val kind: String = "OUTRO", // CONTA | TAREFA | META | MUDANCA | MANUTENCAO | OUTRO
    val date: Long,
    val minuteOfDay: Int = 9 * 60,
    val repeat: String = "NENHUM", // NENHUM | DIARIO | SEMANAL | MENSAL
    val enabled: Boolean = true,
)

@Entity(tableName = "guide_state")
data class GuideStateEntity(
    @PrimaryKey val guideId: String,
    val favorite: Boolean = false,
    val lastRead: Long? = null,
    val readCount: Int = 0,
    val checked: String = "",
) {
    fun checkedSet(): Set<Int> = checked.split(',').mapNotNull { it.toIntOrNull() }.toSet()
}

@Entity(tableName = "achievements")
data class AchievementEntity(@PrimaryKey val id: String, val unlockedAt: Long)

// ---------------------------- v2 ----------------------------

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val limitAmount: Long? = null,
    val closingDay: Int,
    val dueDay: Int,
    val color: String = "AZUL",
    /** faturas pagas (yyyyMM separados por vírgula) */
    val paidInvoices: String = "",
    val isDemo: Boolean = false,
) {
    fun paidSet(): Set<Int> = paidInvoices.split(',').mapNotNull { it.trim().toIntOrNull() }.toSet()
}

@Entity(tableName = "recurring")
data class RecurringEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: String, // ENTRADA | SAIDA
    val description: String,
    val amount: Long,
    val category: String = "Outros",
    val day: Int,
    val startMonth: Int,
    val lastGenerated: Int? = null,
    val active: Boolean = true,
    val cardId: Long? = null,
    val incomeType: String? = null,
    val isDemo: Boolean = false,
)

@Entity(tableName = "goal_deposits")
data class GoalDepositEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long,
    val amount: Long,
    val date: Long,
    val note: String? = null,
)

@Entity(tableName = "task_log")
data class TaskLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val title: String,
    val date: Long,
)

/** Contagens usadas pela jornada e pelas conquistas (uma única consulta observável). */
data class Counts(
    val incomes: Int, val expenses: Int, val bills: Int, val billsPaid: Int, val debts: Int, val debtsPaidOff: Int,
    val budgets: Int, val goals: Int, val goalsCompleted: Int, val reserveSaved: Long,
    val prepDone: Int, val movingDone: Int, val movingTotal: Int, val documentsDone: Int, val visitDone: Int, val questionsDone: Int,
    val properties: Int, val furniture: Int, val furnitureBought: Int, val houseTasks: Int, val houseTasksDone: Int, val cleaningRoomsComplete: Int,
    val marketItems: Int, val marketBought: Int, val favoritesMarket: Int, val meals: Int, val pantry: Int,
    val contacts: Int, val emergencyContacts: Int, val warranties: Int, val members: Int, val sharedExpenses: Int, val reimbursements: Int,
    val agreements: Int, val guidesRead: Int, val guideFavorites: Int, val remindersOn: Int, val milestones: Int,
)
