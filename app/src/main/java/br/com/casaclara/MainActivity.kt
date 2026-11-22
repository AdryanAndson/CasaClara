package br.com.casaclara

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import br.com.casaclara.ui.CasaClaraRoot

class MainActivity : ComponentActivity() {
    /** Tela pedida por um atalho do ícone (segurar o ícone do app). */
    private val pendingRoute = androidx.compose.runtime.mutableStateOf<String?>(null)

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra("route")?.let { pendingRoute.value = it }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = (application as CasaClaraApp).container
        if (savedInstanceState == null) intent?.getStringExtra("route")?.let { pendingRoute.value = it }
        setContent { CasaClaraRoot(container, pendingRoute) }
    }
}
