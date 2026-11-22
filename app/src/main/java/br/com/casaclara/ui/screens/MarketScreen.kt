package br.com.casaclara.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
import br.com.casaclara.data.MealEntity
import br.com.casaclara.data.PantryEntity
import br.com.casaclara.data.ShopEntity
import br.com.casaclara.domain.Dates
import br.com.casaclara.domain.content.CozinhaGuides
import br.com.casaclara.domain.content.Recipe
import br.com.casaclara.ui.MARKET_CATEGORIES
import br.com.casaclara.ui.MEAL_TYPES
import br.com.casaclara.ui.WEEKDAYS
import br.com.casaclara.ui.asState
import br.com.casaclara.ui.components.AlertBox
import br.com.casaclara.ui.components.BrandCard
import br.com.casaclara.ui.components.CrudList
import br.com.casaclara.ui.components.EmptySpec
import br.com.casaclara.ui.components.FieldSpec
import br.com.casaclara.ui.components.FieldType
import br.com.casaclara.ui.components.Init
import br.com.casaclara.ui.components.LocalConfirm
import br.com.casaclara.ui.components.LocalFeedback
import br.com.casaclara.ui.components.Mood
import br.com.casaclara.ui.components.RowSpec
import br.com.casaclara.ui.components.ScreenHeader
import br.com.casaclara.ui.components.SecondaryButton
import br.com.casaclara.ui.components.TabBody
import br.com.casaclara.ui.components.Tabs
import br.com.casaclara.ui.components.Tag
import br.com.casaclara.ui.components.Tone
import br.com.casaclara.ui.components.opts
import kotlinx.coroutines.launch

@Composable
fun MarketScreen(c: AppContainer, nav: NavController, initialTab: Int) {
    var tab by rememberSaveable { mutableIntStateOf(initialTab) }
    Column {
        Row { TextButton(onClick = { nav.popBackStack() }) { Text("‹ Voltar") } }
        Box(Modifier.padding(horizontal = 16.dp)) { ScreenHeader("Mercado e alimentação", "Lista na mão, cardápio planejado e nada estragando.", emoji = "🛒") }
        Tabs(listOf("Lista", "Favoritos", "Cardápio", "Despensa", "Receitas"), tab) { tab = it }
        when (tab) {
            0 -> TabBody { ShoppingList(c, "MERCADO", MARKET_CATEGORIES, "Lista de compras", "Por categoria. Informe os preços que encontrar — o app não inventa preços.") }
            1 -> TabBody { ShoppingList(c, "MERCADO", MARKET_CATEGORIES, "Itens favoritos", "O que você sempre compra. Ao limpar os comprados, os favoritos voltam desmarcados.", favoritesOnly = true) }
            2 -> CardapioTab(c)
            3 -> DespensaTab(c)
            else -> ReceitasTab(c, nav)
        }
    }
}

@Composable
private fun CardapioTab(c: AppContainer) {
    val meals by c.db.meals().all().asState(emptyList())
    val scope = rememberCoroutineScope()
    val confirm = LocalConfirm.current
    val fb = LocalFeedback.current
    TabBody {
        CrudList(
            items = meals.sortedWith(compareBy({ it.day }, { MEAL_TYPES.indexOf(it.mealType) })), itemName = "refeição",
            title = "Cardápio da semana", description = "Planeje só o que fizer sentido. Não precisa preencher tudo!",
            fields = listOf(
                FieldSpec("day", "Dia", FieldType.SELECT, true, WEEKDAYS.mapIndexed { i, d -> i.toString() to d }),
                FieldSpec("mealType", "Refeição", FieldType.SELECT, true, opts(MEAL_TYPES)),
                FieldSpec("dish", "Prato", required = true, placeholder = "Ex.: Arroz, feijão e ovo", help = "Ideias: " + CozinhaGuides.RECIPES.take(4).joinToString { it.name }),
                FieldSpec("note", "Anotação", placeholder = "Ex.: usar a sobra de frango"),
            ),
            toForm = { m -> if (m == null) mapOf("day" to ((Dates.today().dayOfWeek.value - 1).toString()), "mealType" to "Almoço") else mapOf("day" to m.day.toString(), "mealType" to m.mealType, "dish" to m.dish, "note" to (m.note ?: "")) },
            save = { d, old -> c.db.meals().upsert(MealEntity(id = old?.id ?: 0, day = d.int("day") ?: 0, mealType = d.str("mealType"), dish = d.str("dish"), note = d.text("note"), isDemo = old?.isDemo ?: false)) },
            delete = { c.db.meals().delete(it) },
            groupBy = { WEEKDAYS[it.day.coerceIn(0, 6)] }, groupOrder = WEEKDAYS,
            row = { m -> RowSpec(m.dish, m.mealType + (m.note?.let { " · $it" } ?: "")) },
            empty = EmptySpec("Cardápio vazio", "Planejar 3 refeições já evita o delivery por desespero."),
            addedMessage = "No cardápio! Menos um \"o que eu janto hoje?\".",
            isDemo = { it.isDemo },
            headerActions = { if (meals.isNotEmpty()) TextButton(onClick = { confirm.ask("Limpar o cardápio?", "Todas as refeições planejadas serão apagadas.", "Limpar") { scope.launch { c.db.meals().clear() }; fb.toast("Semana nova, cardápio novo!", Mood.TRANQUILA) } }) { Text("Limpar semana") } },
        )
    }
}

@Composable
private fun DespensaTab(c: AppContainer) {
    val items by c.db.pantry().all().asState(emptyList())
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    val today = Dates.todayEpoch()
    val vencidos = items.count { (it.expiry ?: Long.MAX_VALUE) < today }
    TabBody {
        if (vencidos > 0) AlertBox("$vencidos item(ns) passaram da validade. Na dúvida, não consuma.", "PERIGO")
        CrudList(
            items = items, itemName = "item", masculine = true, title = "Controle da despensa", description = "O que tem em casa e quando vence. Os que vencem primeiro aparecem antes.",
            fields = listOf(
                FieldSpec("name", "Item", required = true),
                FieldSpec("location", "Onde está", FieldType.SELECT, true, opts("Armário", "Geladeira", "Freezer", "Fruteira")),
                FieldSpec("qty", "Quantidade", FieldType.DECIMAL, min = 0.0, max = 9999.0),
                FieldSpec("unit", "Unidade", FieldType.SELECT, options = opts("un", "kg", "g", "L", "ml", "pct", "cx", "dz")),
                FieldSpec("expiry", "Validade", FieldType.DATE),
            ),
            toForm = { p -> if (p == null) mapOf("location" to "Armário", "unit" to "un") else mapOf("name" to p.name, "location" to p.location, "qty" to Init.num(p.qty), "unit" to p.unit, "expiry" to Init.date(p.expiry)) },
            save = { d, old -> c.db.pantry().upsert(PantryEntity(id = old?.id ?: 0, name = d.str("name"), location = d.str("location"), qty = d.double("qty"), unit = d.text("unit") ?: "un", expiry = d.date("expiry"), isDemo = old?.isDemo ?: false)) },
            delete = { c.db.pantry().delete(it) },
            groupBy = { it.location }, groupOrder = listOf("Geladeira", "Freezer", "Armário", "Fruteira"),
            row = { p ->
                val days = p.expiry?.let { it - today }
                RowSpec(
                    p.name, p.qty?.let { "${Init.num(it)} ${p.unit}" },
                    tags = if (days == null) null else { {
                        when {
                            days < 0 -> Tag("venceu", Tone.CORAL)
                            days <= 3 -> Tag(if (days == 0L) "vence hoje" else "vence em $days dia(s)", Tone.AMARELO)
                            else -> Tag("até ${Dates.br(p.expiry)}", Tone.VERDE)
                        }
                    } },
                    extra = { TextButton(onClick = { scope.launch { c.db.shop().upsert(ShopEntity(name = p.name, category = "Outros")) }; fb.toast("Na lista de mercado!") }) { Text("🛒") } },
                )
            },
            empty = EmptySpec("Despensa vazia (no app, pelo menos)", "Cadastre principalmente o que estraga rápido para receber o aviso de validade."),
            isDemo = { it.isDemo },
        )
    }
}

@Composable
private fun ReceitasTab(c: AppContainer, nav: NavController) {
    var open by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val fb = LocalFeedback.current
    TabBody {
        AlertBox("Receitas simples com ingredientes comuns. O custo depende dos preços da sua região — o app não inventa preços.", "DICA")
        CozinhaGuides.RECIPES.forEach { r ->
            BrandCard(onClick = { open = if (open == r.id) null else r.id }) {
                Text("${r.emoji} ${r.name}", style = MaterialTheme.typography.titleMedium)
                Text("${r.servings} porção(ões) · ${r.minutes} min · ${r.difficulty.label}", style = MaterialTheme.typography.bodySmall)
                if (open == r.id) RecipeDetail(r) {
                    scope.launch { r.ingredients.forEach { c.db.shop().upsert(ShopEntity(name = it, category = "Outros")) } }
                    fb.toast("Ingredientes na lista de mercado!", Mood.COMEMORANDO)
                }
            }
        }
        SecondaryButton("📚 Guias de cozinha e alimentação") { nav.navigate("library") }
    }
}

@Composable
private fun RecipeDetail(r: Recipe, onAddToList: () -> Unit) {
    Text("Ingredientes", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
    r.ingredients.forEach { Text("• $it") }
    Text("Modo de preparo", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
    r.steps.forEachIndexed { i, s -> Text("${i + 1}. $s") }
    Text("💡 ${r.tip}", modifier = Modifier.padding(top = 6.dp))
    Text("🧊 ${r.storage}")
    TextButton(onClick = onAddToList) { Text("🛒 Ingredientes para a lista") }
}
