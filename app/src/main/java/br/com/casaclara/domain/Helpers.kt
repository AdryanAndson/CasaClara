package br.com.casaclara.domain

import kotlin.math.roundToLong

/** Leituras de medidor (luz kWh, água m³, gás m³): consumo entre leituras e estimativa mensal. */
object Readings {
    data class Point(val date: Long, val value: Double)
    data class Period(val from: Long, val to: Long, val used: Double) { val days: Long get() = (to - from).coerceAtLeast(1) ; val perDay: Double get() = used / days }
    data class Stats(val periods: List<Period>, val perDay: Double?, val monthEstimate: Double?, val last: Period?, val trendPct: Double?, val error: String?)

    fun stats(points: List<Point>): Stats {
        val p = points.sortedBy { it.date }.distinctBy { it.date }
        if (p.size < 2) return Stats(emptyList(), null, null, null, null, null)
        val periods = p.zipWithNext { a, b -> Period(a.date, b.date, b.value - a.value) }
        if (periods.any { it.used < 0 }) return Stats(periods, null, null, periods.last(), null, "Uma leitura ficou menor que a anterior. Confira se digitou certo (ou se o medidor foi trocado).")
        val recent = periods.takeLast(3)
        val perDay = recent.sumOf { it.used } / recent.sumOf { it.days }
        val trend = if (periods.size >= 2) {
            val a = periods[periods.size - 2].perDay; val b = periods.last().perDay
            if (a > 0) (b - a) / a * 100 else null
        } else null
        return Stats(periods, perDay, perDay * 30, periods.last(), trend, null)
    }

    /** Custo estimado: consumo × tarifa (centavos por unidade). */
    fun cost(used: Double, tariffCents: Long): Long = (used * tariffCents).roundToLong()
}

/** Detector de golpe: perguntas sim/não com peso. */
object ScamCheck {
    data class Q(val id: String, val text: String, val weight: Int, val why: String)
    val QUESTIONS = listOf(
        Q("pressa", "Pedem para você agir rápido, \"agora\" ou \"só hoje\"?", 2, "Pressa é a ferramenta número 1 dos golpistas: tira seu tempo de pensar."),
        Q("dinheiro", "Pedem Pix, transferência, boleto ou pagamento antecipado?", 3, "Golpes quase sempre terminam num pagamento. Pagar antes para receber algo é sinal clássico."),
        Q("codigo", "Pedem senha, código recebido por SMS, token ou foto de documento?", 3, "Nenhum banco, loja ou órgão sério pede seu código ou senha."),
        Q("numero", "A mensagem veio de um número ou perfil novo, \"troquei de número\"?", 2, "Golpistas usam fotos de conhecidos com números novos."),
        Q("link", "Tem um link para clicar ou app para instalar?", 2, "Links levam a sites falsos; apps podem dar acesso remoto ao seu celular."),
        Q("bom-demais", "A oferta é boa demais: preço muito baixo, prêmio, emprego fácil, renda garantida?", 2, "Se parece bom demais para ser verdade, provavelmente é golpe."),
        Q("segredo", "Pedem segredo ou para não contar a ninguém?", 2, "Isolar a vítima evita que alguém de fora perceba o golpe."),
        Q("nome-diferente", "O Pix ou boleto está no nome de outra pessoa ou empresa?", 3, "O nome do recebedor deveria ser o de quem você conhece ou da loja."),
        Q("emocao", "A mensagem mexe com medo, culpa ou carinho (parente em apuros, conta bloqueada, dívida)?", 2, "Emoção forte é usada para você agir sem pensar."),
        Q("canal", "O contato chegou por ligação, SMS ou WhatsApp sem você ter procurado?", 1, "Contatos que você não iniciou merecem mais cuidado."),
    )
    enum class Level(val label: String) { BAIXO("Risco baixo"), ATENCAO("Atenção"), ALTO("Muito provável que seja golpe") }
    fun score(yes: Set<String>): Int = QUESTIONS.filter { it.id in yes }.sumOf { it.weight }
    fun level(yes: Set<String>): Level {
        val s = score(yes)
        val critical = yes.any { it == "codigo" || it == "nome-diferente" }
        return when { s >= 5 || critical -> Level.ALTO; s >= 2 -> Level.ATENCAO; else -> Level.BAIXO }
    }
    val WHAT_TO_DO = listOf(
        "Pare. Não pague, não clique e não passe código nenhum agora.",
        "Confirme por outro caminho: ligue para o número que você já tinha da pessoa, ou abra o app oficial da empresa.",
        "Converse com alguém de confiança antes de decidir.",
        "Se já pagou ou passou dados: avise o banco na hora, peça o MED (devolução do Pix) e faça boletim de ocorrência.",
        "Denuncie o número ou perfil no próprio aplicativo.",
    )
}

/** Números de emergência e utilidade pública (nacionais). */
object EmergencyNumbers {
    data class N(val number: String, val name: String, val what: String, val emoji: String)
    val ALL = listOf(
        N("192", "SAMU", "Emergência de saúde: desmaio, falta de ar, dor no peito, acidente.", "🚑"),
        N("193", "Bombeiros", "Incêndio, vazamento de gás, resgate, afogamento.", "🚒"),
        N("190", "Polícia Militar", "Crime acontecendo, violência, risco à segurança.", "🚓"),
        N("199", "Defesa Civil", "Deslizamento, alagamento, risco em construções.", "🌧️"),
        N("188", "CVV", "Apoio emocional e prevenção do suicídio, gratuito, 24 horas.", "💛"),
        N("180", "Central de Atendimento à Mulher", "Violência contra a mulher: orientação e denúncia.", "💜"),
        N("100", "Disque Direitos Humanos", "Violações de direitos humanos, inclusive de crianças e idosos.", "🤝"),
        N("136", "Disque Saúde", "Informações sobre o SUS e serviços de saúde.", "🏥"),
        N("197", "Polícia Civil", "Denúncias e informações (varia por estado).", "🕵️"),
    )
}
