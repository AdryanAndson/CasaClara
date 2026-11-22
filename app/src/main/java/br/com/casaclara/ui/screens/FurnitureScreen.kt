package br.com.casaclara.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.casaclara.data.AppContainer
import br.com.casaclara.data.FurnitureEntity
import br.com.casaclara.data.OptionEntity
import br.com.casaclara.data.UserPrefs
import br.com.casaclara.domain.Calculators
import br.com.casaclara.domain.Dates
import br.com.casaclara.domain.Money
import br.com.casaclara.ui.ROOMS
import br.com.casaclara.ui.asState
import br.com.casaclara.ui.components.BrandCard
import br.com.casaclara.ui.components.CrudList
import br.com.casaclara.ui.components.EmptySpec
import br.com.casaclara.ui.components.FieldSpec
import br.com.casaclara.ui.components.FieldType
import br.com.casaclara.ui.components.Init
import br.com.casaclara.ui.components.LocalFeedback
import br.com.casaclara.ui.components.MascotBubble
import br.com.casaclara.ui.components.MoneyInput
import br.com.casaclara.ui.components.Mood
import br.com.casaclara.ui.components.ProgressLine
import br.com.casaclara.ui.components.RowSpec
import br.com.casaclara.ui.components.ScreenHeader
import br.com.casaclara.ui.components.Stat
import br.com.casaclara.ui.components.TabBody
import br.com.casaclara.ui.components.Tabs
import br.com.casaclara.ui.components.Tag
import br.com.casaclara.ui.components.Tone
import br.com.casaclara.ui.components.opts
import br.com.casaclara.ui.theme.Brand
import kotlinx.coroutines.launch

private val PRIORITIES = listOf("AGORA" to "🔥 Preciso agora", "DEPOIS" to "⏳ Posso comprar depois", "LEGAL" to "✨ Seria legal ter")

@Composable
fun FurnitureScreen(c: AppContainer, nav: NavController, initialTab: Int) {
    var tab by rememberSaveable { mutableIntStateOf(initialTab) }
    Column {
        Row { TextButton(onClick = { nav.popBackStack() }) { Text("‹ Voltar") } }
        Box(Modifier.padding(horizontal = 16.dp)) { ScreenHeader("Móveis e compras", "Calma, você não precisa comprar a casa inteira no primeiro dia.", emoji = "🛋️") }
        Tabs(listOf("Lista e orçamento", "Comparar opções"), tab) { tab = it }
        if (tab == 0) ItensTab(c) else CompararTab(c)
    }
}

@Composable
private fun ItensTab(c: AppContainer) {
    val items by c.db.furniture().all().asState(emptyList())
    val prefs by c.prefs.flow.asState(UserPrefs())
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    val budgetTxt = prefs.calc["moveis"]?.get("orcamento") ?: ""
    var budget by remember(budgetTxt) { mutableStateOf(budgetTxt) }
    val t = Calculators.furnitureTotals(items.map { Calculators.FurnitureLine(it.estimate, it.paid, it.bought, it.priority == "AGORA", it.installments, it.installmentValue) })
    val orc = Money.parse(budget) ?: 0
    TabBody {
        BrandCard(tone = Tone.AMARELO) {
            MoneyInput("Quanto posso gastar com a casa", budget, { budget = it; scope.launch { c.prefs.setCalc("moveis", mapOf("orcamento" to it)) } })
            if (orc > 0) {
                Text("Gasto: ${Money.format(t.pago)} · resta: ${Money.format(orc - t.pago)}", fontWeight = FontWeight.Bold)
                ProgressLine(t.pago.toFloat() / orc, "Uso do orçamento da casa", if (t.pago > orc) Brand.Coral else Brand.Verde)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BrandCard(Modifier.weight(1f), padding = 12.dp) { Stat("Falta (estimado)", Money.format(t.estimadoPendente)) }
            BrandCard(Modifier.weight(1f), padding = 12.dp) { Stat("Do \"agora\"", Money.format(t.pendenteAgora)) }
        }
        if (t.parcelasMes > 0) Text("Parcelas por mês: ${Money.format(t.parcelasMes)}. Lembre de cadastrar em Finanças → Dívidas.", fontWeight = FontWeight.SemiBold)
        CrudList(
            items = items, itemName = "item", masculine = true, title = "O que preciso para a casa",
            fields = listOf(
                FieldSpec("name", "Item", required = true),
                FieldSpec("priority", "Prioridade", FieldType.SELECT, true, PRIORITIES),
                FieldSpec("room", "Cômodo", FieldType.SELECT, options = opts(listOf("") + ROOMS)),
                FieldSpec("estimate", "Quanto acho que custa (orçamento do item)", FieldType.MONEY),
                FieldSpec("bought", "Já comprei", FieldType.CHECK),
                FieldSpec("paid", "Quanto paguei (total)", FieldType.MONEY),
                FieldSpec("installments", "Em quantas vezes", FieldType.INT, min = 1.0, max = 48.0),
                FieldSpec("installmentValue", "Valor da parcela", FieldType.MONEY),
                FieldSpec("store", "Loja"),
                FieldSpec("purchaseDate", "Data da compra", FieldType.DATE),
                FieldSpec("warrantyMonths", "Garantia (meses)", FieldType.INT, min = 0.0, max = 120.0),
                FieldSpec("invoice", "Nº da nota fiscal ou onde guardei"),
            ),
            toForm = { f ->
                if (f == null) mapOf("priority" to "AGORA") else mapOf(
                    "name" to f.name, "priority" to f.priority, "room" to (f.room ?: ""), "estimate" to Init.money(f.estimate), "bought" to Init.bool(f.bought), "paid" to Init.money(f.paid),
                    "installments" to (f.installments?.toString() ?: ""), "installmentValue" to Init.money(f.installmentValue), "store" to (f.store ?: ""),
                    "purchaseDate" to Init.date(f.purchaseDate), "warrantyMonths" to (f.warrantyMonths?.toString() ?: ""), "invoice" to (f.invoice ?: ""),
                )
            },
            save = { d, old ->
                c.db.furniture().upsert(FurnitureEntity(id = old?.id ?: 0, name = d.str("name"), priority = d.str("priority"), room = d.text("room"), estimate = d.money("estimate"), bought = d.bool("bought"),
                    paid = d.money("paid"), installments = d.int("installments"), installmentValue = d.money("installmentValue"), store = d.text("store"), purchaseDate = d.date("purchaseDate"),
                    warrantyMonths = d.int("warrantyMonths"), invoice = d.text("invoice"), isDemo = old?.isDemo ?: false))
            },
            delete = { c.db.furniture().delete(it) },
            groupBy = { f -> PRIORITIES.first { it.first == f.priority }.second }, groupOrder = PRIORITIES.map { it.second },
            row = { f ->
                RowSpec(
                    title = f.name,
                    subtitle = listOfNotNull(f.room, f.store.takeIf { f.bought }, f.installments?.takeIf { f.bought && it > 1 }?.let { "${it}x de ${Money.format(f.installmentValue)}" }, f.invoice?.let { "NF: $it" }, f.purchaseDate?.takeIf { f.bought }?.let { "comprado em ${Dates.br(it)}" }).joinToString(" · "),
                    amount = if (f.bought) Money.format(f.paid ?: f.estimate) else f.estimate?.let { "~" + Money.format(it) },
                    checked = f.bought,
                    onCheck = { v -> scope.launch { c.db.furniture().upsert(f.copy(bought = v, purchaseDate = if (v) (f.purchaseDate ?: Dates.todayEpoch()) else f.purchaseDate)) }; if (v) fb.celebrate("Casa ficando com a sua cara!") },
                    tags = warrantyTag(f.purchaseDate, f.warrantyMonths),
                )
            },
            empty = EmptySpec("Nenhum item ainda", "Use a lista do essencial — dividida em \"preciso agora\", \"posso comprar depois\" e \"seria legal ter\"."),
            isDemo = { it.isDemo },
            headerActions = { TextButton(onClick = { scope.launch { c.loadEssentials() }; fb.toast("Lista do essencial adicionada!", Mood.COMEMORANDO) }) { Text("✨ Essencial") } },
        )
    }
}

@Composable
private fun CompararTab(c: AppContainer) {
    val items by c.db.options().all().asState(emptyList())
    val total = { o: OptionEntity -> o.price + (o.shipping ?: 0) }
    val minBy = items.groupBy { it.item }.mapValues { (_, l) -> l.minOf { total(it) } }
    TabBody {
        CrudList(
            items = items, itemName = "opção", title = "Comparar opções", description = "Para cada item, cadastre as opções que encontrou. O total inclui o frete.",
            fields = listOf(
                FieldSpec("item", "Item que estou comparando", required = true, placeholder = "Ex.: Geladeira"),
                FieldSpec("option", "Opção (modelo/marca)", required = true),
                FieldSpec("store", "Loja"),
                FieldSpec("price", "Preço", FieldType.MONEY, true),
                FieldSpec("shipping", "Frete", FieldType.MONEY),
                FieldSpec("installments", "Parcelas sem juros", FieldType.INT, min = 1.0, max = 48.0),
                FieldSpec("rating", "Minha nota (1 a 5)", FieldType.INT, min = 1.0, max = 5.0),
                FieldSpec("link", "Link", FieldType.URL),
                FieldSpec("note", "Anotações (garantia, consumo de energia…)", FieldType.LONGTEXT),
            ),
            toForm = { o -> if (o == null) emptyMap() else mapOf("item" to o.item, "option" to o.option, "store" to (o.store ?: ""), "price" to Init.money(o.price), "shipping" to Init.money(o.shipping), "installments" to (o.installments?.toString() ?: ""), "rating" to (o.rating?.toString() ?: ""), "link" to (o.link ?: ""), "note" to (o.note ?: "")) },
            save = { d, old -> c.db.options().upsert(OptionEntity(id = old?.id ?: 0, item = d.str("item"), option = d.str("option"), store = d.text("store"), price = d.money("price")!!, shipping = d.money("shipping"), installments = d.int("installments"), rating = d.int("rating"), link = d.text("link"), note = d.text("note"), isDemo = old?.isDemo ?: false)) },
            delete = { c.db.options().delete(it) },
            groupBy = { it.item },
            row = { o ->
                RowSpec(
                    title = o.option,
                    subtitle = listOfNotNull(o.store, o.shipping?.let { "frete ${Money.format(it)}" } ?: "frete não informado", o.installments?.takeIf { it > 1 }?.let { "${it}x de ${Money.format(total(o) / it)}" }, o.note, o.link).joinToString(" · "),
                    amount = Money.format(total(o)),
                    tags = {
                        if (items.count { it.item == o.item } > 1 && total(o) == minBy[o.item]) Tag("menor preço", Tone.VERDE)
                        o.rating?.let { Tag("★".repeat(it)) }
                    },
                )
            },
            empty = EmptySpec("Nada para comparar ainda", "Pesquisou uma geladeira em três lojas? Cadastre as três e veja qual sai mais em conta com frete."),
            isDemo = { it.isDemo },
        )
        MascotBubble("O mais barato nem sempre é o melhor: veja garantia, consumo de energia e se cabe no espaço.", Mood.DICA, 64.dp)
    }
}
