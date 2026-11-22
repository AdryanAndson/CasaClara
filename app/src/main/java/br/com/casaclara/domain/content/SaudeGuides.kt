package br.com.casaclara.domain.content

import br.com.casaclara.domain.content.Difficulty.*

object SaudeGuides {
    private val NAO_SUBSTITUI = info("Estes conteúdos ajudam a se organizar e não substituem atendimento profissional. Em emergência, ligue 192 (SAMU).")

    val all = listOf(
        Guide(
            "sus-como-usar", "saude", "SUS", "Como usar o SUS: por onde começar", "🏥", 5, FACIL,
            problem = "Muita gente só procura o SUS na emergência e não sabe que tem uma unidade de saúde de referência perto de casa, com consulta, vacina e remédios.",
            steps = listOf(
                "Descubra qual é a sua UBS (Unidade Básica de Saúde) de referência pelo endereço, na prefeitura ou no app Meu SUS Digital.",
                "Vá à UBS com documento com foto, CPF e comprovante de residência para atualizar o cadastro.",
                "Na UBS você marca consultas, faz vacinas, exames básicos e pega muitos remédios.",
                "Para o que não é urgente, comece sempre pela UBS: ela encaminha para especialistas quando precisa.",
                "UPA e pronto-socorro são para urgências; em emergência grave, ligue 192.",
            ),
            mistakes = listOf("Ir ao pronto-socorro para tudo e esperar horas.", "Não atualizar o endereço depois da mudança."),
            checklist = listOf("Sei qual é minha UBS", "Atualizei meu cadastro", "Instalei o Meu SUS Digital"),
            related = listOf("meu-sus-digital", "quando-upa"),
            alert = NAO_SUBSTITUI,
        ),
        Guide(
            "meu-sus-digital", "saude", "SUS", "App Meu SUS Digital: vacinas, exames e cartão", "📲", 3, FACIL,
            problem = "O app oficial do SUS guarda sua carteira de vacinação, número do cartão SUS e parte do histórico de atendimentos. Ajuda muito em consultas e viagens.",
            steps = listOf(
                "Instale o app Meu SUS Digital e entre com a conta gov.br.",
                "Veja o número do seu Cartão Nacional de Saúde e salve um print.",
                "Confira a carteira de vacinação digital e o que está em atraso.",
                "Mostre o app quando for atendido em outra cidade.",
            ),
            checklist = listOf("Instalei o app", "Salvei o número do cartão SUS"),
            related = listOf("sus-como-usar", "vacinas-adulto"),
        ),
        Guide(
            "quando-upa", "saude", "Atendimento", "UBS, UPA, pronto-socorro ou SAMU: para onde ir", "🚑", 4, FACIL,
            problem = "Saber o lugar certo de atendimento economiza tempo e ajuda quem está em situação grave a ser atendido primeiro.",
            steps = listOf(
                "UBS (posto de saúde): consultas marcadas, acompanhamento, vacinas, receitas e exames de rotina.",
                "UPA: urgências que não podem esperar a consulta marcada, como febre alta persistente ou corte que precisa de pontos.",
                "Pronto-socorro de hospital: casos graves e complexos.",
                "SAMU 192: emergência com risco à vida, como desmaio, falta de ar intensa, dor forte no peito ou sinais de AVC.",
                "Na dúvida, ligue 192: a equipe orienta o que fazer e se é preciso mandar ambulância.",
            ),
            checklist = listOf("Sei onde fica a UPA mais próxima", "Salvei o 192 nos favoritos"),
            related = listOf("sus-como-usar", "cartao-emergencia-guia"),
            alert = danger("Sinais como dor forte no peito, falta de ar intensa, rosto torto, fala enrolada ou fraqueza de um lado do corpo: ligue 192 imediatamente."),
        ),
        Guide(
            "farmacia-popular", "saude", "Remédios", "Remédios mais baratos ou gratuitos", "💊", 4, FACIL,
            problem = "Remédio de uso contínuo pesa no orçamento de quem mora sozinho. Programas públicos e genéricos podem reduzir muito esse gasto.",
            steps = listOf(
                "Pergunte na UBS quais remédios da sua receita estão disponíveis de graça na farmácia do SUS.",
                "O programa Farmácia Popular oferece remédios gratuitos ou com desconto em farmácias credenciadas, com receita e documento.",
                "Peça ao médico para receitar pelo nome do princípio ativo, o que permite comprar o genérico.",
                "Compare preços em mais de uma farmácia e pergunte por descontos com CPF.",
                "Nunca troque a dose ou o remédio sem orientação médica.",
            ),
            checklist = listOf("Perguntei na UBS pelos meus remédios", "Conferi o Farmácia Popular"),
            related = listOf("sus-como-usar", "remedios-casa"),
            alert = NAO_SUBSTITUI, legal = true,
        ),
        Guide(
            "remedios-casa", "saude", "Remédios", "Caixinha de remédios e primeiros socorros em casa", "🩹", 4, FACIL,
            problem = "Ter um kit básico em casa ajuda nos pequenos imprevistos. Guardar bem e conferir a validade evita usar algo estragado.",
            steps = listOf(
                "Monte um kit básico: curativos adesivos, gaze, esparadrapo, soro fisiológico, termômetro e luvas descartáveis.",
                "Guarde remédios em local seco, fresco e longe do banheiro, onde há muito vapor.",
                "Mantenha os remédios na caixa original com a bula.",
                "Uma vez a cada seis meses, confira validades e descarte os vencidos em farmácias ou UBS que recebem.",
                "Remédios só com orientação de profissional de saúde; evite automedicação.",
            ),
            mistakes = listOf("Guardar remédios no banheiro.", "Jogar remédio vencido no lixo comum ou no vaso."),
            checklist = listOf("Montei o kit", "Conferi as validades"),
            related = listOf("farmacia-popular", "cartao-emergencia-guia"),
            alert = NAO_SUBSTITUI,
        ),
        Guide(
            "vacinas-adulto", "saude", "Prevenção", "Vacinas de adulto em dia", "💉", 3, FACIL,
            problem = "Vacina não é só coisa de criança. Adultos também precisam de reforços, e muitas estão disponíveis de graça na UBS.",
            steps = listOf(
                "Confira sua carteira de vacinação no app Meu SUS Digital ou leve a carteira de papel à UBS.",
                "Pergunte na UBS quais vacinas estão indicadas para sua idade, como tétano, hepatite B e gripe nas campanhas.",
                "Guarde o comprovante de cada dose.",
                "Coloque um lembrete no app para o próximo reforço.",
            ),
            checklist = listOf("Conferi minha carteira", "Coloquei lembrete do próximo reforço"),
            related = listOf("meu-sus-digital", "check-up"),
            alert = NAO_SUBSTITUI,
        ),
        Guide(
            "check-up", "saude", "Prevenção", "Cuidar da saúde quando ninguém lembra por você", "🩺", 4, FACIL,
            problem = "Morando sozinho, ninguém marca a consulta ou lembra do dentista. Criar lembretes simples evita que problemas pequenos virem grandes.",
            steps = listOf(
                "Marque uma consulta de rotina na UBS uma vez por ano, ou como o profissional orientar.",
                "Cuide dos dentes: muitas UBS têm dentista e há Centros de Especialidades Odontológicas.",
                "Use o app para criar lembretes anuais de consulta, dentista e exames.",
                "Anote sintomas que se repetem, com datas, para mostrar na consulta.",
                "Guarde exames e receitas numa pasta, física ou no celular.",
            ),
            checklist = listOf("Marquei consulta de rotina", "Criei lembrete anual"),
            related = listOf("vacinas-adulto", "sus-como-usar"),
            alert = NAO_SUBSTITUI,
        ),
        Guide(
            "doente-sozinho", "saude", "Imprevistos", "Fiquei doente morando sozinho(a)", "🤒", 4, FACIL,
            problem = "Ficar doente sem ninguém em casa é difícil. Um pouco de preparo antes faz muita diferença na hora em que você não tem energia para nada.",
            steps = listOf(
                "Tenha sempre em casa algumas comidas fáceis: sopa congelada, bolacha, chá e água.",
                "Combine com uma pessoa de confiança de avisar quando estiver doente e de mandar mensagem todo dia.",
                "Deixe o número de alguém como contato de emergência na tela de bloqueio do celular.",
                "Se piorar ou tiver sinais de alerta, procure atendimento ou ligue 192 sem esperar.",
                "Peça atestado se precisar faltar ao trabalho.",
            ),
            checklist = listOf("Tenho comida fácil congelada", "Combinei com alguém de me checar", "Contato de emergência no celular"),
            related = listOf("cartao-emergencia-guia", "quando-upa", "solidao"),
            alert = NAO_SUBSTITUI,
        ),
        Guide(
            "sono", "saude", "Bem-estar", "Dormir melhor sem complicar", "😴", 4, FACIL,
            problem = "Sono ruim afeta humor, trabalho e até o controle dos gastos. Pequenos ajustes no quarto e na rotina ajudam bastante.",
            steps = listOf(
                "Tente dormir e acordar em horários parecidos, inclusive no fim de semana.",
                "Deixe o quarto escuro, silencioso e arejado.",
                "Diminua telas e luzes fortes na última hora antes de deitar.",
                "Evite café e energético no fim da tarde.",
                "Se a insônia durar semanas, procure a UBS.",
            ),
            checklist = listOf("Defini um horário de dormir"),
            related = listOf("descanso-lazer", "check-up"),
            alert = NAO_SUBSTITUI,
        ),
        Guide(
            "alimentacao-simples", "saude", "Bem-estar", "Comer bem gastando pouco", "🥗", 4, FACIL,
            problem = "Morando sozinho é fácil cair no lanche e no delivery. Uma base simples de comida de verdade faz bem para a saúde e para o bolso.",
            steps = listOf(
                "Monte o prato com metade de verduras e legumes, um quarto de arroz ou outro cereal e um quarto de feijão, ovo ou carne.",
                "Arroz e feijão juntos formam uma base nutritiva e barata.",
                "Compre frutas e legumes da época na feira: costumam ser mais baratos.",
                "Beba água ao longo do dia; deixe uma garrafa à vista.",
                "Cozinhe em quantidade no fim de semana e congele porções.",
            ),
            checklist = listOf("Fiz uma lista de pratos simples da semana"),
            related = listOf("planejar-refeicoes", "refeicoes-economicas"),
        ),
    )
}
