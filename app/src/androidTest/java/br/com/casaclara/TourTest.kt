package br.com.casaclara

import android.graphics.Bitmap
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * Passeio pelo app num emulador: faz a primeira abertura, carrega os exemplos,
 * visita as telas principais e salva prints (usados para revisão visual no CI).
 */
@RunWith(AndroidJUnit4::class)
class TourTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private val inst get() = InstrumentationRegistry.getInstrumentation()
    private var n = 0

    private fun shot(name: String) {
        rule.waitForIdle()
        Thread.sleep(700)
        val bmp: Bitmap = inst.uiAutomation.takeScreenshot() ?: return
        val dir = File(inst.targetContext.filesDir, "shots").apply { mkdirs() }
        n++
        FileOutputStream(File(dir, "%02d-%s.png".format(n, name))).use { bmp.compress(Bitmap.CompressFormat.PNG, 90, it) }
    }

    private fun node(text: String, substring: Boolean = false): SemanticsNodeInteraction {
        rule.waitUntil(15_000) { rule.onAllNodes(hasText(text, substring = substring)).fetchSemanticsNodes().isNotEmpty() }
        return rule.onAllNodes(hasText(text, substring = substring)).onFirst()
    }

    private fun tap(text: String, substring: Boolean = false) {
        val n = node(text, substring)
        runCatching { n.performScrollTo() }
        n.performClick(); rule.waitForIdle()
    }
    private fun scrollDown() { rule.onRoot().performTouchInput { swipeUp() }; rule.waitForIdle() }
    private fun back() { rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }; rule.waitForIdle() }

    @Test
    fun passeioPeloApp() {
        Tour(this).run()
    }

    /** Roteiro separado para ficar fácil de ajustar quando as telas mudarem. */
    private class Tour(val t: TourTest) {
        fun run() = with(t) {
            node("Começar"); shot("boas-vindas")
            tap("Começar")
            tap("Pular esta pergunta")
            tap("Estou juntando dinheiro", substring = true); shot("situacao")
            tap("Continuar")
            repeat(5) { tap("Pular esta pergunta") }
            shot("jornada-pronta")
            tap("Explorar com dados de exemplo")
            Thread.sleep(2500)
            shot("inicio"); scrollDown(); shot("inicio-2"); scrollDown(); shot("inicio-3"); scrollDown(); shot("inicio-4")
            tap("Jornada"); shot("jornada")
            tap("Finanças"); shot("financas"); scrollDown(); shot("financas-2")
            tap("Cartões"); shot("cartoes")
            tap("Cofrinhos"); shot("cofrinhos")
            tap("Casa"); shot("casa")
            tap("Plano da semana", substring = true); shot("plano-semana"); back()
            tap("Mais"); shot("mais")
            tap("Faxina cronometrada"); shot("faxina"); back()
            tap("Importar extrato"); shot("importar"); back()
            tap("Personalizar o Início"); shot("personalizar"); back()
            tap("Dicas e guias"); shot("biblioteca")
            tap("Segurança doméstica", substring = true); shot("categoria")
            tap("Como agir em caso de vazamento de gás"); shot("guia")
        }
    }
}
