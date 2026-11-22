package br.com.casaclara.domain.content

import br.com.casaclara.domain.content.Difficulty.*

object RotinaGuides {
    private val CUIDADO = info("Este conteúdo é acolhedor e geral. Se você se sente sobrecarregado(a) com frequência, sem conseguir dar conta do básico, conversar com um profissional de saúde pode ajudar. Em sofrimento intenso, o CVV atende gratuitamente pelo 188, 24 horas.")

    val all = listOf(
        Guide(
            "organizar-semana", "rotina", "Planejamento", "Como organizar a semana", "🗓️", 4, FACIL,
            problem = "Sem uma visão da semana, tudo vira urgência. Dez minutos de planejamento no domingo deixam os outros dias mais leves.",
            steps = listOf(
                "Anote os compromissos fixos: trabalho, estudo, deslocamento.",
                "Encaixe as tarefas da casa nos espaços que sobram, com folga.",
                "Defina 3 prioridades da semana — não 15.",
                "Reserve um tempo livre de verdade, sem tarefas.",
                "Use o calendário do app para ver contas e tarefas juntas.",
            ),
            checklist = listOf("Compromissos anotados", "3 prioridades definidas", "Tempo livre reservado"),
            related = listOf("rotina-realista", "equilibrar"),
        ),
        Guide(
            "equilibrar", "rotina", "Equilíbrio", "Como equilibrar trabalho, estudo e tarefas domésticas", "⚖️", 4, MEDIO,
            problem = "Quando trabalho e estudo ocupam o dia, a casa parece mais um emprego. O segredo é simplificar e aceitar o \"bom o suficiente\".",
            steps = listOf(
                "Simplifique: menos objetos, refeições simples, roupas fáceis de cuidar.",
                "Agrupe tarefas: lave roupa enquanto cozinha, por exemplo.",
                "Use os dias mais leves para as tarefas maiores.",
                "Se dividir casa, renegocie tarefas em semanas de prova ou trabalho pesado.",
                "Aceite que nem toda semana vai ser perfeita.",
            ),
            checklist = listOf("Simplifiquei a rotina", "Agrupei tarefas"),
            related = listOf("organizar-semana", "sobrecarregado"),
        ),
        Guide(
            "rotina-realista", "rotina", "Planejamento", "Como criar uma rotina realista", "🌤️", 3, FACIL,
            problem = "Rotinas perfeitas duram três dias. Uma rotina realista cabe na sua vida como ela é.",
            steps = listOf(
                "Comece com 2 ou 3 hábitos pequenos, não com 10.",
                "Encaixe cada hábito depois de algo que você já faz (\"depois do café, lavo a louça\").",
                "Prefira frequência a intensidade: 10 minutos todo dia vencem 2 horas uma vez por mês.",
                "Falhou um dia? Siga no próximo. O app não tem sequência punitiva — você também não precisa ter.",
            ),
            checklist = listOf("Escolhi 2 ou 3 hábitos", "Encaixei em momentos fixos"),
            related = listOf("evitar-acumular", "plano-sem-pressa"),
        ),
        Guide(
            "evitar-acumular", "rotina", "Hábitos", "Como evitar acumular tarefas", "📚", 3, FACIL,
            problem = "Tarefa adiada cresce: a louça de um dia vira a pilha de três, e a roupa vira montanha.",
            steps = listOf(
                "Faça as tarefas curtas na hora (regra dos 2 minutos).",
                "Deixe as tarefas recorrentes com dia fixo e lembrete.",
                "Quebre tarefas grandes em partes: \"organizar o armário\" vira \"organizar uma gaveta\".",
                "Comece pela mais fácil para ganhar embalo.",
            ),
            checklist = listOf("Lembretes ativados", "Tarefas grandes quebradas"),
            related = listOf("pouco-tempo", "rotina-realista"),
        ),
        Guide(
            "descanso-lazer", "rotina", "Bem-estar", "Como manter tempo para descanso e lazer", "🛋️", 3, FACIL,
            problem = "Morar sozinho traz liberdade, mas também muita responsabilidade. Sem pausas, a rotina cansa e desanima.",
            steps = listOf(
                "Marque o descanso na agenda como um compromisso.",
                "Tenha opções de lazer que cabem no orçamento: parque, biblioteca, eventos gratuitos, encontros em casa.",
                "Durma o suficiente: a casa espera, o corpo não.",
                "Mantenha contato com amigos e família.",
            ),
            checklist = listOf("Reservei tempo de descanso", "Tenho opções de lazer acessíveis"),
            related = listOf("sobrecarregado", "organizar-semana"),
        ),
        Guide(
            "sobrecarregado", "rotina", "Bem-estar", "Como lidar com a sensação de estar sobrecarregado", "🫶", 4, FACIL,
            problem = "Contas, casa, trabalho, estudo... às vezes tudo parece demais. Sentir isso é comum, e há formas de aliviar o peso.",
            steps = listOf(
                "Pare e escreva tudo o que está na sua cabeça. Ver no papel diminui a sensação de caos.",
                "Separe o que é urgente de verdade do que pode esperar.",
                "Escolha uma única tarefa pequena e faça. O resto pode ficar para depois.",
                "Pause a jornada do app, se precisar. Ela espera você.",
                "Peça ajuda: dividir tarefas, conversar com alguém de confiança.",
            ),
            checklist = listOf("Escrevi o que me preocupa", "Escolhi uma tarefa pequena", "Conversei com alguém"),
            related = listOf("descanso-lazer", "equilibrar"),
            alert = CUIDADO,
        ),
        Guide(
            "adaptar-casa", "rotina", "Bem-estar", "Como adaptar a casa às próprias necessidades", "🏡", 3, FACIL,
            problem = "A casa precisa funcionar para você, não para uma foto de revista. Pequenas adaptações deixam o dia a dia mais fácil.",
            steps = listOf(
                "Observe onde você perde tempo ou se irrita (ex.: chaves sempre sumindo).",
                "Crie soluções simples: gancho na entrada, cesto para roupa suja no quarto, cantinho de estudo.",
                "Deixe o que usa todo dia ao alcance das mãos.",
                "Considere acessibilidade se precisar: iluminação, barras de apoio, tapetes antiderrapantes.",
                "Ajuste com o tempo — a casa muda junto com você.",
            ),
            checklist = listOf("Identifiquei os pontos de atrito", "Fiz uma adaptação"),
            related = listOf("armarios-pequenos", "rotina-realista"),
        ),
    )
}
