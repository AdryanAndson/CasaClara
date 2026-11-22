package br.com.casaclara.ui

import android.app.Activity
import android.app.KeyguardManager
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import br.com.casaclara.ui.components.Mascot
import br.com.casaclara.ui.components.Mood
import br.com.casaclara.ui.components.PrimaryButton

/** O aparelho tem bloqueio de tela (PIN, padrão, senha ou digital)? Sem isso não dá para usar o bloqueio do app. */
fun deviceSecure(ctx: android.content.Context) = ctx.getSystemService(KeyguardManager::class.java)?.isDeviceSecure == true

/**
 * Bloqueio do app com a mesma senha/digital do celular. Desenha uma capa por cima do conteúdo
 * (sem destruir as telas) e esconde o app na lista de recentes (FLAG_SECURE).
 */
@Suppress("DEPRECATION")
@Composable
fun LockGate(enabled: Boolean, content: @Composable () -> Unit) {
    val ctx = LocalContext.current
    val activity = ctx as? Activity
    var unlocked by rememberSaveable { mutableStateOf(false) }
    var stoppedAt by remember { mutableLongStateOf(0L) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, enabled) {
        val obs = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_STOP) stoppedAt = System.currentTimeMillis()
            if (e == Lifecycle.Event.ON_START && enabled && stoppedAt > 0 && System.currentTimeMillis() - stoppedAt > 30_000) unlocked = false
        }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }
    DisposableEffect(enabled) {
        if (enabled) activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE) else activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { if (it.resultCode == Activity.RESULT_OK) unlocked = true }
    fun ask() {
        val km = ctx.getSystemService(KeyguardManager::class.java)
        val i = km?.createConfirmDeviceCredentialIntent("Casa Clara", "Confirme que é você para abrir o app")
        if (i == null) unlocked = true else runCatching { launcher.launch(i) }.onFailure { unlocked = true }
    }
    Box(Modifier.fillMaxSize()) {
        content()
        if (enabled && !unlocked) {
            LaunchedEffect(Unit) { ask() }
            Surface(Modifier.fillMaxSize().clickable(remember { MutableInteractionSource() }, null) {}, color = MaterialTheme.colorScheme.background) {
                Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically), horizontalAlignment = Alignment.CenterHorizontally) {
                    Mascot(Mood.TRANQUILA, 120.dp)
                    Text("Casa Clara está protegido", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                    Text("Use a senha, o padrão ou a digital do celular para abrir.", textAlign = TextAlign.Center)
                    PrimaryButton("🔓 Desbloquear") { ask() }
                }
            }
        }
    }
}
