package br.com.casaclara.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.casaclara.domain.Dates
import br.com.casaclara.domain.Money
import java.time.LocalDate

enum class FieldType { TEXT, LONGTEXT, MONEY, INT, DECIMAL, DATE, DAY, SELECT, MULTI, CHECK, PHONE, URL }

data class FieldSpec(
    val key: String,
    val label: String,
    val type: FieldType = FieldType.TEXT,
    val required: Boolean = false,
    val options: List<Pair<String, String>> = emptyList(),
    val help: String? = null,
    val min: Double? = null,
    val max: Double? = null,
    val placeholder: String? = null,
    val validate: ((FormData) -> String?)? = null,
)

fun opts(vararg values: String) = values.map { it to it }
fun opts(values: List<String>) = values.map { it to it }

/** Acesso tipado aos valores digitados (todos guardados como texto no formulário). */
class FormData(val values: Map<String, String>) {
    fun text(k: String): String? = values[k]?.trim()?.takeIf { it.isNotEmpty() }
    fun str(k: String): String = text(k) ?: ""
    fun money(k: String): Long? = Money.parse(values[k])
    fun int(k: String): Int? = values[k]?.trim()?.toIntOrNull()
    fun double(k: String): Double? = values[k]?.trim()?.replace(',', '.')?.toDoubleOrNull()
    fun date(k: String): Long? = Dates.parseBr(values[k])
    fun bool(k: String): Boolean = values[k] == "1"
    fun multi(k: String): List<String> = values[k]?.split(',')?.filter { it.isNotBlank() } ?: emptyList()
}

/** Helpers para montar os valores iniciais de um formulário a partir de um registro. */
object Init {
    fun money(v: Long?) = Money.toInput(v)
    fun date(v: Long?) = v?.let { Dates.br(it) } ?: ""
    fun bool(v: Boolean) = if (v) "1" else ""
    fun num(v: Number?) = v?.let { if (it is Double && it % 1.0 == 0.0) it.toLong().toString() else it.toString().replace('.', ',') } ?: ""
    fun today() = Dates.br(Dates.todayEpoch())
}

fun validateForm(fields: List<FieldSpec>, values: Map<String, String>): Map<String, String> {
    val data = FormData(values)
    val errors = mutableMapOf<String, String>()
    for (f in fields) {
        val raw = values[f.key]?.trim() ?: ""
        if (raw.isEmpty()) {
            if (f.required && f.type != FieldType.CHECK) errors[f.key] = if (f.type == FieldType.MULTI) "Escolha pelo menos uma opção." else "Esse campo é obrigatório."
            continue
        }
        val err = when (f.type) {
            FieldType.MONEY -> {
                val c = Money.parse(raw)
                when {
                    c == null -> "Digite um valor, por exemplo 1.250,00"
                    c < ((f.min ?: 0.0) * 100).toLong() -> "O valor não pode ser menor que ${Money.format(((f.min ?: 0.0) * 100).toLong())}."
                    else -> null
                }
            }
            FieldType.INT, FieldType.DAY -> {
                val n = raw.toIntOrNull()
                val min = if (f.type == FieldType.DAY) 1.0 else f.min
                val max = if (f.type == FieldType.DAY) 31.0 else f.max
                when {
                    n == null -> if (f.type == FieldType.DAY) "Use um dia entre 1 e 31." else "Digite um número inteiro."
                    min != null && n < min -> "Mínimo: ${min.toInt()}."
                    max != null && n > max -> "Máximo: ${max.toInt()}."
                    else -> null
                }
            }
            FieldType.DECIMAL -> {
                val n = raw.replace(',', '.').toDoubleOrNull()
                when {
                    n == null -> "Digite um número."
                    f.min != null && n < f.min -> "Mínimo: ${f.min.toString().replace('.', ',')}."
                    f.max != null && n > f.max -> "Máximo: ${f.max.toString().replace('.', ',')}."
                    else -> null
                }
            }
            FieldType.DATE -> if (Dates.parseBr(raw) == null) "Data inválida. Use dd/mm/aaaa." else null
            FieldType.URL -> if (!Regex("^https?://\\S+$", RegexOption.IGNORE_CASE).matches(raw)) "Use um link começando com http:// ou https://" else null
            FieldType.PHONE -> if (raw.count { it.isDigit() } < 3) "Telefone incompleto." else null
            else -> null
        }
        val custom = err ?: f.validate?.invoke(data)
        if (custom != null) errors[f.key] = custom
    }
    return errors
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormSheet(
    title: String,
    fields: List<FieldSpec>,
    initial: Map<String, String>,
    submitText: String = "Salvar",
    onDismiss: () -> Unit,
    onSubmit: (FormData) -> Unit,
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val values = remember { mutableStateMapOf<String, String>().apply { putAll(initial) } }
    var errors by remember { mutableStateOf(emptyMap<String, String>()) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state) {
        Column(
            Modifier.fillMaxWidth().imePadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            fields.forEach { f ->
                FieldInput(f, values[f.key] ?: "", errors[f.key]) { v -> values[f.key] = v; if (errors.containsKey(f.key)) errors = errors - f.key }
            }
            Text("* obrigatório. O resto você pode pular.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton("Cancelar", Modifier.weight(1f)) { onDismiss() }
                PrimaryButton(submitText, Modifier.weight(1f)) {
                    val e = validateForm(fields, values)
                    errors = e
                    if (e.isEmpty()) onSubmit(FormData(values.toMap()))
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FieldInput(f: FieldSpec, value: String, error: String?, onChange: (String) -> Unit) {
    val label = f.label + if (f.required) " *" else ""
    val support: @Composable (() -> Unit)? = when {
        error != null -> { { Text(error) } }
        f.help != null -> { { Text(f.help) } }
        else -> null
    }
    when (f.type) {
        FieldType.CHECK -> Row(
            Modifier.fillMaxWidth().clickable(role = Role.Checkbox) { onChange(if (value == "1") "" else "1") },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = value == "1", onCheckedChange = { onChange(if (it) "1" else "") })
            Column { Text(f.label, style = MaterialTheme.typography.bodyLarge); f.help?.let { Text(it, style = MaterialTheme.typography.bodySmall) } }
        }
        FieldType.MULTI -> Column {
            Text(label, style = MaterialTheme.typography.labelLarge)
            val sel = value.split(',').filter { it.isNotBlank() }.toSet()
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                f.options.forEach { (v, l) ->
                    FilterChip(selected = v in sel, onClick = { onChange((if (v in sel) sel - v else sel + v).joinToString(",")) }, label = { Text(l) })
                }
            }
            if (error != null) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        FieldType.SELECT -> SelectField(label, value, f.options, error, f.help, onChange)
        FieldType.DATE -> {
            val ctx = LocalContext.current
            OutlinedTextField(
                value = value, onValueChange = onChange, label = { Text(label) }, placeholder = { Text("dd/mm/aaaa") },
                isError = error != null, supportingText = support, singleLine = true, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    IconButton(onClick = {
                        val d = Dates.parseBr(value)?.let { LocalDate.ofEpochDay(it) } ?: LocalDate.now()
                        DatePickerDialog(ctx, { _, y, m, day -> onChange(Dates.br(LocalDate.of(y, m + 1, day).toEpochDay())) }, d.year, d.monthValue - 1, d.dayOfMonth).show()
                    }) { Icon(Icons.Filled.DateRange, contentDescription = "Abrir calendário") }
                },
            )
        }
        else -> OutlinedTextField(
            value = value,
            onValueChange = onChange,
            label = { Text(label) },
            placeholder = (f.placeholder ?: if (f.type == FieldType.MONEY) "0,00" else null)?.let { p -> { Text(p) } },
            prefix = if (f.type == FieldType.MONEY) { { Text("R$ ") } } else null,
            isError = error != null,
            supportingText = support,
            singleLine = f.type != FieldType.LONGTEXT,
            minLines = if (f.type == FieldType.LONGTEXT) 3 else 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = when (f.type) {
                    FieldType.MONEY, FieldType.DECIMAL -> KeyboardType.Decimal
                    FieldType.INT, FieldType.DAY -> KeyboardType.Number
                    FieldType.PHONE -> KeyboardType.Phone
                    FieldType.URL -> KeyboardType.Uri
                    else -> KeyboardType.Text
                },
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun SelectField(label: String, value: String, options: List<Pair<String, String>>, error: String? = null, help: String? = null, onChange: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val shown = options.firstOrNull { it.first == value }?.second ?: value
    Box {
        OutlinedTextField(
            value = shown, onValueChange = {}, readOnly = true, label = { Text(label) }, isError = error != null,
            supportingText = (error ?: help)?.let { t -> { Text(t) } },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(Modifier.matchParentSize().clickable(role = Role.Button) { open = true }.semantics { contentDescription = "$label: ${shown.ifBlank { "nada escolhido" }}. Toque para escolher." })
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { (v, l) -> DropdownMenuItem(text = { Text(l.ifBlank { "—" }) }, onClick = { onChange(v); open = false }) }
        }
    }
}

/** Campo de dinheiro solto, para calculadoras que atualizam enquanto a pessoa digita. */
@Composable
fun MoneyInput(label: String, value: String, onChange: (String) -> Unit, help: String? = null, modifier: Modifier = Modifier) {
    val invalid = value.isNotBlank() && Money.parse(value) == null
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) }, prefix = { Text("R$ ") }, placeholder = { Text("0,00") },
        isError = invalid, supportingText = (if (invalid) "Valor inválido" else help)?.let { t -> { Text(t) } },
        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun NumberInput(label: String, value: String, onChange: (String) -> Unit, help: String? = null, suffix: String? = null, modifier: Modifier = Modifier) {
    val invalid = value.isNotBlank() && value.replace(',', '.').toDoubleOrNull() == null
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) }, suffix = suffix?.let { s -> { Text(s) } },
        isError = invalid, supportingText = (if (invalid) "Número inválido" else help)?.let { t -> { Text(t) } },
        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = modifier.fillMaxWidth(),
    )
}
