package br.com.casaclara.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

/** Estrutura padrão de tela: largura máxima confortável em tablets e rolagem vertical. */
@Composable
fun Page(nav: NavController? = null, title: String? = null, actions: (@Composable RowScope.() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 840.dp).fillMaxWidth()) {
            if (nav != null || title != null) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (nav != null) IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") }
                    if (title != null) Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f).padding(start = 4.dp).semantics { heading() })
                    else Spacer(Modifier.weight(1f))
                    actions?.invoke(this)
                }
            }
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
        }
    }
}

/** Conteúdo de uma aba (sem cabeçalho próprio). */
@Composable
fun TabBody(content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = 840.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

data class RowSpec(
    val title: String,
    val subtitle: String? = null,
    val amount: String? = null,
    val tags: (@Composable () -> Unit)? = null,
    val checked: Boolean? = null,
    val onCheck: ((Boolean) -> Unit)? = null,
    val extra: (@Composable RowScope.() -> Unit)? = null,
    val onClick: (() -> Unit)? = null,
)

data class EmptySpec(val title: String, val text: String, val mood: Mood = Mood.PENSANDO)

/**
 * Lista com criar, editar e excluir (com confirmação), formulário validado e estado vazio.
 * Reutilizada por quase todas as ferramentas do app.
 */
@Composable
fun <T> CrudList(
    items: List<T>,
    itemName: String,
    masculine: Boolean = false,
    fields: List<FieldSpec>,
    toForm: (T?) -> Map<String, String>,
    save: suspend (FormData, T?) -> Unit,
    delete: suspend (T) -> Unit,
    row: (T) -> RowSpec,
    empty: EmptySpec,
    title: String? = null,
    description: String? = null,
    groupBy: ((T) -> String)? = null,
    groupOrder: List<String> = emptyList(),
    addedMessage: String? = null,
    headerActions: (@Composable () -> Unit)? = null,
    isDemo: (T) -> Boolean = { false },
    canDelete: (T) -> String? = { null },
) {
    val scope = rememberCoroutineScope()
    val feedback = LocalFeedback.current
    val confirm = LocalConfirm.current
    var editing by remember { mutableStateOf<Any?>(null) } // NEW ou item
    val novo = if (masculine) "Novo" else "Nova"

    if (title != null) SectionTitle(title)
    if (description != null) Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End), verticalAlignment = Alignment.CenterVertically) {
        headerActions?.invoke()
        OutlinedButton(onClick = { editing = NEW }) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text(" Adicionar")
        }
    }
    if (items.isEmpty()) {
        EmptyState(empty.title, empty.text, empty.mood, actionText = "Adicionar $itemName", onAction = { editing = NEW })
    } else {
        val groups: List<Pair<String, List<T>>> = if (groupBy == null) listOf("" to items) else
            items.groupBy(groupBy).toList().sortedWith(compareBy({ g -> groupOrder.indexOf(g.first).let { if (it < 0) 999 else it } }, { it.first }))
        groups.forEach { (g, list) ->
            if (g.isNotEmpty()) Text("$g (${list.size})", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 6.dp).semantics { heading() })
            list.forEach { it ->
                val r = row(it)
                ItemRow(
                    title = r.title, subtitle = r.subtitle, amount = r.amount,
                    tags = if (r.tags != null || isDemo(it)) { { DemoTag(isDemo(it)); r.tags?.invoke() } } else null,
                    checked = r.checked, onCheck = r.onCheck, extra = r.extra, onClick = r.onClick, itemLabel = itemName,
                    onEdit = { editing = it },
                    onDelete = {
                        val block = canDelete(it)
                        if (block != null) feedback.toast(block, Mood.CONFUSA)
                        else confirm.ask("Excluir $itemName?", "Vamos apagar \"${r.title}\". Isso não pode ser desfeito.") {
                            scope.launch { delete(it); feedback.toast("Excluído.", Mood.TRANQUILA) }
                        }
                    },
                )
            }
        }
    }
    val e = editing
    if (e != null) {
        @Suppress("UNCHECKED_CAST") val item = if (e === NEW) null else e as T
        FormSheet(
            title = if (item == null) "$novo $itemName" else "Editar $itemName",
            fields = fields,
            initial = toForm(item),
            submitText = if (item == null) "Adicionar" else "Salvar",
            onDismiss = { editing = null },
        ) { data ->
            scope.launch {
                runCatching { save(data, item) }
                    .onSuccess {
                        if (item == null) feedback.toast(addedMessage ?: "Boa! ${itemName.replaceFirstChar { it.uppercase() }} ${if (masculine) "adicionado" else "adicionada"}.", Mood.COMEMORANDO)
                        else feedback.toast("Alterações salvas.")
                    }
                    .onFailure { feedback.toast("Não consegui salvar. Tente de novo.", Mood.CONFUSA) }
                editing = null
            }
        }
    }
    Spacer(Modifier.height(4.dp))
}

private val NEW = Any()
