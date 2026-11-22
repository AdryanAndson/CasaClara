package br.com.casaclara.domain.content

enum class Difficulty(val label: String) { FACIL("Fácil"), MEDIO("Médio"), AVANCADO("Avançado") }
enum class AlertKind { PERIGO, INFO, DICA }
data class GuideAlert(val kind: AlertKind, val text: String)

data class Category(val id: String, val name: String, val emoji: String, val color: Long, val description: String)

/**
 * Formato de cada conteúdo: título, problema, passos práticos, exemplo, erros comuns,
 * checklist (marcável no app) e conteúdos relacionados.
 */
data class Guide(
    val id: String,
    val category: String,
    val sub: String,
    val title: String,
    val emoji: String,
    val minutes: Int,
    val difficulty: Difficulty,
    val problem: String,
    val steps: List<String>,
    val example: String? = null,
    val mistakes: List<String> = emptyList(),
    val checklist: List<String> = emptyList(),
    val related: List<String> = emptyList(),
    val alert: GuideAlert? = null,
    /** Conteúdo que depende de lei ou regra oficial: mostra aviso para conferir fontes oficiais. */
    val legal: Boolean = false,
) {
    fun searchText(): String = buildString {
        append(title).append(' ').append(sub).append(' ').append(problem).append(' ')
        steps.forEach { append(it).append(' ') }
        example?.let { append(it).append(' ') }
        checklist.forEach { append(it).append(' ') }
    }
}

data class Recipe(
    val id: String, val name: String, val emoji: String, val servings: Int, val minutes: Int, val difficulty: Difficulty,
    val ingredients: List<String>, val steps: List<String>, val tip: String, val storage: String,
)

fun danger(t: String) = GuideAlert(AlertKind.PERIGO, t)
fun info(t: String) = GuideAlert(AlertKind.INFO, t)
fun tip(t: String) = GuideAlert(AlertKind.DICA, t)

object Library {
    val CATEGORIES = listOf(
        Category("preparo", "Preparação para morar sozinho", "🚀", 0xFFD9F5E8, "Do primeiro cálculo à conversa com a família."),
        Category("dinheiro", "Dinheiro e orçamento", "💰", 0xFFFFF4C7, "Orçamento, contas, dívidas e reserva."),
        Category("aluguel", "Aluguel e escolha de imóvel", "🔑", 0xFFDFE9FF, "Procurar, visitar, comparar e assinar com segurança."),
        Category("mudanca", "Mudança", "📦", 0xFFFFE2DE, "Planejar, embalar e chegar bem na casa nova."),
        Category("limpeza", "Organização e limpeza", "🧹", 0xFFD9F5E8, "Rotina, cômodos, roupas e manchas."),
        Category("cozinha", "Cozinha e alimentação", "🍳", 0xFFFFF4C7, "Cozinhar, planejar, armazenar e não desperdiçar."),
        Category("moveis", "Móveis e compras", "🛋️", 0xFFDFE9FF, "Comprar o essencial, comparar e guardar notas."),
        Category("manutencao", "Manutenção e cuidados", "🔧", 0xFFEFE6FF, "Prevenir problemas e saber quando chamar alguém."),
        Category("seguranca", "Segurança doméstica", "🛡️", 0xFFFFE2DE, "Gás, eletricidade, incêndio e golpes."),
        Category("compartilhada", "Vida compartilhada", "🧑‍🤝‍🧑", 0xFFD9F5E8, "Contas, tarefas e convivência."),
        Category("rotina", "Rotina e bem-estar", "🌱", 0xFFEFE6FF, "Uma rotina possível, com tempo para descansar."),
    )

    val GUIDES: List<Guide> by lazy {
        PreparoGuides.all + DinheiroGuides.all + AluguelGuides.all + MudancaGuides.all + LimpezaGuides.all +
            CozinhaGuides.all + MoveisGuides.all + ManutencaoGuides.all + SegurancaGuides.all + CompartilhadaGuides.all + RotinaGuides.all
    }
    val BY_ID: Map<String, Guide> by lazy { GUIDES.associateBy { it.id } }

    fun category(id: String) = CATEGORIES.first { it.id == id }
    fun byCategory(id: String) = GUIDES.filter { it.category == id }

    private fun norm(s: String) = java.text.Normalizer.normalize(s.lowercase(), java.text.Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")

    /** Busca simples: todas as palavras precisam aparecer; título pesa mais. */
    fun search(query: String): List<Guide> {
        val words = norm(query).split(Regex("\\s+")).filter { it.length >= 2 }
        if (words.isEmpty()) return emptyList()
        return GUIDES.mapNotNull { g ->
            val t = norm(g.title)
            val all = norm(g.searchText())
            if (words.all { all.contains(it) }) g to words.sumOf { (if (t.contains(it)) 3 else 0) + 1 } else null
        }.sortedByDescending { it.second }.map { it.first }
    }

    /** Relacionados declarados + outros da mesma subcategoria. */
    fun related(g: Guide): List<Guide> =
        (g.related.mapNotNull { BY_ID[it] } + GUIDES.filter { it.category == g.category && it.sub == g.sub && it.id != g.id }).distinct().take(4)
}
