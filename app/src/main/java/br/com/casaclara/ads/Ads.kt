package br.com.casaclara.ads

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import br.com.casaclara.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Anúncios do Casa Clara (AdMob).
 * - Pede o consentimento exigido pelo Google (UMP) antes de carregar qualquer anúncio.
 * - Banner discreto só em telas de leitura/ferramentas (nunca em Emergência, golpe, bloqueio ou formulários).
 * - Vídeo premiado opcional que dá 3 dias sem anúncios.
 * Nenhum dado do usuário (finanças, listas…) é passado aos anúncios.
 */
object Ads {
    /** Pode pedir anúncios (consentimento resolvido e SDK iniciado). Observável pela interface. */
    val ready = mutableStateOf(false)
    /** O usuário precisa de um botão de "opções de privacidade dos anúncios" (ex.: Europa). */
    val privacyOptionsRequired = mutableStateOf(false)
    private val started = AtomicBoolean(false)
    private var consent: ConsentInformation? = null

    fun start(activity: Activity) {
        val info = UserMessagingPlatform.getConsentInformation(activity)
        consent = info
        info.requestConsentInfoUpdate(activity, ConsentRequestParameters.Builder().build(), {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                privacyOptionsRequired.value = info.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
                if (info.canRequestAds()) initSdk(activity)
            }
        }, { /* sem internet ou erro: tenta com o consentimento anterior */ if (info.canRequestAds()) initSdk(activity) })
        if (info.canRequestAds()) initSdk(activity)
    }

    private fun initSdk(context: Context) {
        if (!started.compareAndSet(false, true)) return
        Thread { MobileAds.initialize(context.applicationContext) { ready.value = true } }.start()
    }

    fun showPrivacyOptions(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { }
    }

    /** Carrega e mostra um vídeo premiado. [onReward] só é chamado se a pessoa assistir até o fim. */
    fun showRewarded(activity: Activity, onReward: () -> Unit, onFail: (String) -> Unit) {
        if (!ready.value) { onFail("Os anúncios ainda não estão disponíveis. Verifique a internet."); return }
        RewardedAd.load(activity, activity.getString(R.string.ad_rewarded_id), AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) { ad.show(activity) { onReward() } }
            override fun onAdFailedToLoad(e: LoadAdError) { onFail("Nenhum vídeo disponível agora. Tente mais tarde.") }
        })
    }
}

/** Banner adaptativo no rodapé. Não ocupa espaço quando não há anúncio (sem internet, sem consentimento ou período sem anúncios). */
@Composable
fun AdBanner(enabled: Boolean, modifier: Modifier = Modifier) {
    if (!enabled || !Ads.ready.value) return
    val ctx = LocalContext.current
    BoxWithConstraints(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val width = maxWidth.value.toInt().coerceAtLeast(320)
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { c ->
                AdView(c).apply {
                    setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(c, width))
                    adUnitId = c.getString(R.string.ad_banner_id)
                    loadAd(AdRequest.Builder().build())
                }
            },
            onRelease = { it.destroy() },
        )
    }
}

/** Telas onde o banner pode aparecer (prefixos de rota). */
val AD_ROUTES = listOf("library", "guide", "calc", "market", "moving", "furniture", "quiz", "more", "search", "achievements")
