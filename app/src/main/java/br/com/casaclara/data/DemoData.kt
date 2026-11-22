package br.com.casaclara.data

import java.time.LocalDate

/**
 * Dados de demonstração: valores FICTÍCIOS, apenas para conhecer o app.
 * Todos marcados com isDemo = true e removíveis em Configurações.
 */
object DemoData {
    suspend fun load(db: AppDatabase, today: LocalDate) {
        fun day(n: Int) = today.withDayOfMonth(minOf(n, today.dayOfMonth)).toEpochDay()
        fun rel(n: Long) = today.plusDays(n).toEpochDay()
        val t = db.tx()
        t.upsert(TxEntity(kind = "ENTRADA", description = "Salário (exemplo)", amount = 280000, date = day(5), category = "Salário", incomeType = "FIXA", isDemo = true))
        t.upsert(TxEntity(kind = "ENTRADA", description = "Freela (exemplo)", amount = 45000, date = day(12), category = "Freela", incomeType = "VARIAVEL", isDemo = true))
        t.upsert(TxEntity(kind = "ENTRADA", description = "Venda de doces (exemplo)", amount = 18000, date = day(3), category = "Vendas", incomeType = "INFORMAL", isDemo = true))
        listOf(
            Triple("Mercado do mês (exemplo)", 52000L, "Mercado"), Triple("Passe de ônibus (exemplo)", 22000L, "Transporte"),
            Triple("Streaming (exemplo)", 4000L, "Assinaturas"), Triple("Farmácia (exemplo)", 6500L, "Saúde"),
            Triple("Lanche com amigos (exemplo)", 9000L, "Lazer"), Triple("Conta de celular (exemplo)", 5500L, "Contas da casa"),
        ).forEachIndexed { i, (d, v, c) -> t.upsert(TxEntity(kind = "SAIDA", description = d, amount = v, date = day(2 + i * 2), category = c, isDemo = true)) }
        db.bills().upsert(BillEntity(name = "Internet (exemplo)", amount = 10000, dueDay = 15, isDemo = true))
        db.bills().upsert(BillEntity(name = "Celular (exemplo)", amount = 5500, dueDay = 10, isDemo = true))
        db.debts().upsert(DebtEntity(name = "Notebook parcelado (exemplo)", creditor = "Loja", kind = "PARCELADO", installment = 18000, total = 10, paid = 4, dueDay = 20, isDemo = true))
        db.budgets().upsert(BudgetEntity(category = "Mercado", limitAmount = 60000, isDemo = true))
        db.budgets().upsert(BudgetEntity(category = "Lazer", limitAmount = 15000, isDemo = true))
        db.goals().upsert(GoalEntity(name = "Custo inicial da mudança (exemplo)", type = "MUDANCA", target = 600000, saved = 150000, monthly = 50000, isDemo = true))
        db.goals().upsert(GoalEntity(name = "Reserva de emergência (exemplo)", type = "RESERVA", target = 900000, saved = 80000, monthly = 20000, isDemo = true))
        db.properties().upsert(PropertyEntity(name = "Kitnet perto do metrô (exemplo)", rent = 110000, condo = 25000, iptu = 4000, area = 30.0, bedrooms = 1, distance = "15 min do trabalho", guarantee = "Caução", rating = 4, pros = "Perto do transporte", cons = "Pouca luz natural", isDemo = true))
        db.properties().upsert(PropertyEntity(name = "Apê 1 quarto no bairro vizinho (exemplo)", rent = 99000, condo = 38000, iptu = 6000, area = 42.0, bedrooms = 1, distance = "35 min do trabalho", transport = 8000, guarantee = "Seguro-fiança", pets = true, rating = 3, pros = "Mais espaço, aceita pet", cons = "Condomínio alto", isDemo = true))
        val s = db.shop()
        s.upsert(ShopEntity(name = "Arroz 5 kg", category = "Grãos e massas", price = 2500, isDemo = true, favorite = true))
        s.upsert(ShopEntity(name = "Feijão 1 kg", category = "Grãos e massas", qty = 2.0, price = 800, isDemo = true, favorite = true))
        s.upsert(ShopEntity(name = "Ovos (dúzia)", category = "Carnes e ovos", isDemo = true))
        s.upsert(ShopEntity(name = "Detergente", category = "Limpeza", qty = 2.0, price = 300, bought = true, isDemo = true))
        db.meals().upsert(MealEntity(day = 0, mealType = "Almoço", dish = "Arroz, feijão, ovo e salada (exemplo)", isDemo = true))
        db.meals().upsert(MealEntity(day = 1, mealType = "Jantar", dish = "Macarrão com sardinha (exemplo)", isDemo = true))
        db.pantry().upsert(PantryEntity(name = "Leite (exemplo)", qty = 2.0, unit = "L", location = "Geladeira", expiry = rel(3), isDemo = true))
        db.pantry().upsert(PantryEntity(name = "Macarrão (exemplo)", qty = 3.0, unit = "pct", location = "Armário", expiry = rel(200), isDemo = true))
        db.houseTasks().upsert(HouseTaskEntity(title = "Limpar o banheiro (exemplo)", frequency = "SEMANAL", room = "Banheiro", lastDone = rel(-9), isDemo = true))
        db.houseTasks().upsert(HouseTaskEntity(title = "Lavar a louça (exemplo)", frequency = "DIARIA", room = "Cozinha", lastDone = rel(-1), isDemo = true))
        db.contacts().upsert(ContactEntity(name = "Seu Zé (exemplo)", kind = "PROFISSIONAL", service = "Encanador", phone = "(00) 00000-0000", note = "Contato fictício", isDemo = true))
        db.furniture().upsert(FurnitureEntity(name = "Colchão (exemplo)", room = "Quarto", priority = "AGORA", estimate = 90000, bought = true, paid = 85000, installments = 5, installmentValue = 17000, store = "Loja exemplo", purchaseDate = rel(-30), warrantyMonths = 12, isDemo = true))
        db.furniture().upsert(FurnitureEntity(name = "Sofá (exemplo)", room = "Sala", priority = "LEGAL", estimate = 150000, isDemo = true))
        val sh = db.shared()
        val a = sh.upsertMember(MemberEntity(name = "Você (exemplo)", emoji = "🙂", isDemo = true))
        val b = sh.upsertMember(MemberEntity(name = "Bia (exemplo)", emoji = "🌻", isDemo = true))
        sh.upsertExpense(SharedExpenseEntity(description = "Internet da casa (exemplo)", amount = 12000, date = day(5), paidBy = a, splitAmong = "$a,$b", isDemo = true))
        sh.upsertExpense(SharedExpenseEntity(description = "Produtos de limpeza (exemplo)", amount = 4800, date = day(7), paidBy = b, splitAmong = "$a,$b", isDemo = true))
        sh.upsertTask(SharedTaskEntity(title = "Tirar o lixo (exemplo)", assignee = b, frequency = "DIARIA", isDemo = true))
        sh.upsertAgreement(AgreementEntity(title = "Contas (exemplo)", text = "Contas divididas igualmente e acertadas até o dia 10.", isDemo = true))
        db.milestones().upsert(MilestoneEntity(title = "Visitar apartamentos (exemplo)", date = rel(10), isDemo = true))
    }
}
