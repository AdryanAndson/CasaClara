package br.com.casaclara.domain.content

import br.com.casaclara.domain.content.Difficulty.*

object BemEstarGuides {
    private val APOIO = info("Se estiver difícil, você não precisa passar por isso sozinho(a). O CVV atende de graça pelo 188, 24 horas, e também por chat em cvv.org.br. A UBS e o CAPS também acolhem.")

    val all = listOf(
        Guide(
            "solidao", "emocional", "Sentimentos", "Quando a solidão aperta", "🫂", 4, FACIL,
            problem = "Morar sozinho traz liberdade, mas em alguns dias o silêncio pesa. Sentir isso é comum, e existem jeitos de cuidar desse sentimento.",
            steps = listOf(
                "Reconheça o sentimento sem se culpar: solidão é um sinal de que você precisa de conexão.",
                "Mande mensagem ou ligue para uma pessoa hoje, mesmo que seja só para conversar sobre nada.",
                "Crie um compromisso fixo na semana com alguém: almoço de domingo, chamada de vídeo, caminhada.",
                "Frequente lugares com gente: biblioteca, praça, curso gratuito, grupo da igreja ou do bairro.",
                "Se a tristeza durar muitos dias ou atrapalhar sua rotina, procure ajuda.",
            ),
            checklist = listOf("Falei com alguém hoje", "Tenho um compromisso semanal com alguém"),
            related = listOf("fazer-amizades", "pedir-ajuda", "descanso-lazer"),
            alert = APOIO,
        ),
        Guide(
            "fazer-amizades", "emocional", "Conexões", "Fazer amizades numa cidade ou bairro novo", "👋", 5, FACIL,
            problem = "Depois da escola e da faculdade, fazer amigos fica mais difícil. Grupos com encontros repetidos são o jeito mais natural de criar laços.",
            steps = listOf(
                "Escolha uma atividade com encontros semanais: esporte, coral, voluntariado, curso, grupo de leitura.",
                "Vá pelo menos quatro vezes antes de decidir se gostou: a intimidade vem com a repetição.",
                "Tome a iniciativa de convidar para um café depois da atividade.",
                "Seja a pessoa que lembra do aniversário e manda mensagem.",
                "Aceite convites mesmo quando der preguiça, de vez em quando.",
            ),
            checklist = listOf("Escolhi uma atividade semanal"),
            related = listOf("solidao", "vizinhos"),
        ),
        Guide(
            "vizinhos", "emocional", "Conexões", "Boa convivência com os vizinhos", "🏘️", 4, FACIL,
            problem = "Um bom vizinho pode receber encomenda, avisar de um vazamento e ajudar numa emergência. Pequenas atitudes constroem essa relação.",
            steps = listOf(
                "Apresente-se nos primeiros dias, com um bom-dia e seu nome.",
                "Respeite o horário de silêncio e avise quando for fazer barulho, como mudança ou furadeira.",
                "Troque contatos com um vizinho de confiança para emergências.",
                "Resolva incômodos conversando com calma antes de reclamar no condomínio.",
            ),
            checklist = listOf("Me apresentei aos vizinhos", "Tenho o contato de um vizinho"),
            related = listOf("conhecer-bairro", "evitar-conflitos"),
        ),
        Guide(
            "ansiedade-contas", "emocional", "Dinheiro e emoção", "Ansiedade com dinheiro: como não travar", "😮‍💨", 5, FACIL,
            problem = "Quando as contas apertam, dá vontade de não abrir o app do banco. Evitar piora o problema. Olhar em pedaços pequenos deixa tudo mais possível.",
            steps = listOf(
                "Escolha um horário fixo e curto na semana para olhar o dinheiro, com algo gostoso junto: um café, uma música.",
                "Comece só listando as contas, sem tentar resolver tudo de uma vez.",
                "Separe o que vence esta semana do que pode esperar.",
                "Comemore cada pequena ação: uma conta paga, um gasto anotado.",
                "Converse com alguém de confiança: falar diminui o peso.",
            ),
            checklist = listOf("Defini meu horário semanal do dinheiro"),
            related = listOf("orcamento-nao-fecha", "sobrecarregado"),
            alert = APOIO,
        ),
        Guide(
            "pedir-ajuda", "emocional", "Sentimentos", "Pedir ajuda não é fraqueza", "🙋", 4, FACIL,
            problem = "Muita gente que mora sozinha sente que precisa dar conta de tudo. Mas ninguém faz isso sozinho, e saber pedir ajuda é uma habilidade de adulto.",
            steps = listOf(
                "Faça uma lista de três pessoas para quem você pode ligar num dia difícil.",
                "Peça coisas concretas: \"pode me ajudar a carregar o sofá sábado?\" funciona melhor que \"me ajuda?\".",
                "Ofereça ajuda também: relações se fortalecem nos dois sentidos.",
                "Para sofrimento emocional, a UBS pode encaminhar para atendimento psicológico gratuito.",
                "Em momentos de crise, o CVV atende pelo 188, de graça e a qualquer hora.",
            ),
            checklist = listOf("Listei três pessoas de confiança"),
            related = listOf("solidao", "sus-como-usar"),
            alert = APOIO,
        ),
        Guide(
            "exercicio-sem-academia", "emocional", "Corpo", "Movimentar o corpo sem academia", "🏃", 4, FACIL,
            problem = "Exercício ajuda no humor, no sono e na disposição. Não precisa de academia cara: caminhadas e atividades gratuitas já fazem diferença.",
            steps = listOf(
                "Comece com caminhadas de 20 minutos, três vezes por semana.",
                "Procure academias ao ar livre e programas gratuitos da prefeitura.",
                "Suba escadas e desça um ponto antes do ônibus.",
                "Escolha algo que você gosta: dança, futebol, bicicleta.",
                "Se tiver alguma condição de saúde, converse com a UBS antes de começar algo intenso.",
            ),
            checklist = listOf("Caminhei três vezes nesta semana"),
            related = listOf("sono", "bike-cidade"),
        ),
        Guide(
            "lazer-barato", "emocional", "Lazer", "Lazer bom e barato (ou de graça)", "🎈", 4, FACIL,
            problem = "Orçamento apertado não precisa significar vida sem lazer. Toda cidade tem opções gratuitas que pouca gente conhece.",
            steps = listOf(
                "Procure a agenda cultural da prefeitura: shows, cinema e teatro gratuitos ou populares.",
                "Bibliotecas públicas emprestam livros, filmes e às vezes têm oficinas.",
                "Museus costumam ter dias de entrada gratuita.",
                "Parques, trilhas e praças são ótimos para encontros.",
                "Reserve no orçamento um valor pequeno e fixo para lazer, sem culpa.",
            ),
            checklist = listOf("Encontrei uma opção gratuita para este mês"),
            related = listOf("descanso-lazer", "fazer-amizades"),
        ),
        Guide(
            "limites-familia", "emocional", "Relações", "Morar sozinho(a) e manter uma boa relação com a família", "👨‍👩‍👧", 5, MEDIO,
            problem = "Sair de casa muda a relação com a família. Pode haver cobranças, visitas sem aviso ou pedidos de dinheiro. Combinar limites com carinho ajuda todos.",
            steps = listOf(
                "Combine uma forma de contato frequente, como uma ligação semanal.",
                "Explique com calma o que mudou: horários, visitas, privacidade.",
                "Se pedirem ajuda financeira, decida um valor possível sem comprometer suas contas.",
                "Convide a família para conhecer a casa: isso diminui a preocupação.",
            ),
            checklist = listOf("Combinei uma ligação semanal"),
            related = listOf("conversar-familia", "pedir-ajuda"),
        ),
    )
}
