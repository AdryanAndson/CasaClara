package br.com.casaclara.domain.content

import br.com.casaclara.domain.content.Difficulty.*

object TransporteGuides {
    val all = listOf(
        Guide(
            "transporte-custo-real", "transporte", "Custos", "Quanto custa de verdade ir e voltar todo dia", "🚌", 4, FACIL,
            problem = "O transporte é o segundo maior gasto de muita gente depois da moradia. Morar mais longe para pagar menos aluguel pode sair mais caro no fim do mês.",
            steps = listOf(
                "Some passagens de ida e volta de todos os dias úteis do mês.",
                "Inclua as corridas de aplicativo da noite e do fim de semana.",
                "Considere o tempo: duas horas por dia no trânsito são mais de 40 horas por mês.",
                "Use a calculadora de transporte do app para comparar ônibus, carro e aplicativo.",
                "Coloque o transporte na comparação de imóveis antes de escolher onde morar.",
            ),
            checklist = listOf("Calculei meu transporte mensal", "Considerei o transporte na escolha do imóvel"),
            related = listOf("avaliar-localizacao", "comparar-imoveis", "bilhete-integracao"),
        ),
        Guide(
            "bilhete-integracao", "transporte", "Transporte público", "Economizar com integração e bilhete", "🎫", 4, FACIL,
            problem = "Muitas cidades têm integração entre ônibus, metrô e trem que reduz o preço de quem faz baldeação. Pouca gente aproveita tudo.",
            steps = listOf(
                "Pesquise no site da prefeitura ou da empresa de transporte as regras de integração e tempo máximo.",
                "Use o cartão de transporte em vez de dinheiro: muitas integrações só funcionam com ele.",
                "Veja se há passe mensal ou desconto para estudantes e outros grupos.",
                "Se trabalha com carteira assinada, o vale-transporte é direito; o desconto máximo é 6% do salário-base.",
                "Use apps de rota para comparar caminhos com menos passagens.",
            ),
            checklist = listOf("Tenho o cartão de transporte", "Conheço as regras de integração"),
            related = listOf("transporte-custo-real", "holerite"),
            legal = true,
        ),
        Guide(
            "app-transporte-seguro", "transporte", "Segurança", "Aplicativo de corrida com segurança", "🚗", 3, FACIL,
            problem = "Aplicativo de corrida é prático, principalmente à noite. Alguns hábitos deixam a viagem mais segura.",
            steps = listOf(
                "Confira placa, modelo e foto do motorista antes de entrar.",
                "Pergunte \"para quem é a corrida?\" em vez de dizer seu nome.",
                "Compartilhe a viagem em tempo real com alguém de confiança pelo próprio app.",
                "Sente no banco de trás e mantenha o celular com bateria.",
                "Se algo parecer errado, peça para descer num local movimentado e use o botão de emergência do app.",
            ),
            checklist = listOf("Sei compartilhar a corrida", "Conheço o botão de emergência do app"),
            related = listOf("voltar-noite", "transporte-custo-real"),
        ),
        Guide(
            "voltar-noite", "transporte", "Segurança", "Voltar para casa à noite com mais tranquilidade", "🌙", 4, FACIL,
            problem = "Quem mora sozinho não tem ninguém esperando em casa. Combinar avisos simples e planejar o trajeto ajuda a se sentir mais seguro.",
            steps = listOf(
                "Combine com alguém de avisar quando sair e quando chegar.",
                "Planeje o trajeto antes, por ruas movimentadas e iluminadas.",
                "Tenha o dinheiro da passagem ou do aplicativo separado.",
                "Chegando em casa, prepare a chave antes de chegar ao portão.",
                "Evite usar fones nos dois ouvidos em ruas desertas.",
            ),
            checklist = listOf("Combinei aviso de chegada com alguém"),
            related = listOf("app-transporte-seguro", "contatos-emergencia"),
        ),
        Guide(
            "bike-cidade", "transporte", "Alternativas", "Bicicleta como transporte do dia a dia", "🚲", 5, MEDIO,
            problem = "Para distâncias curtas, a bicicleta pode economizar passagem, ser exercício e às vezes ser mais rápida que o ônibus.",
            steps = listOf(
                "Teste o trajeto num fim de semana antes de usar para trabalhar.",
                "Use capacete, luz dianteira branca e traseira vermelha à noite.",
                "Prefira ciclovias e ruas mais calmas, mesmo que o caminho fique um pouco maior.",
                "Use um cadeado bom e prenda o quadro, não só a roda.",
                "Veja se a cidade tem bicicletas compartilhadas para começar sem comprar.",
            ),
            checklist = listOf("Testei o trajeto", "Tenho luz e capacete"),
            related = listOf("transporte-custo-real", "exercicio-sem-academia"),
        ),
        Guide(
            "carro-vale-a-pena", "transporte", "Custos", "Carro ou moto: vale a pena para mim?", "🛵", 6, MEDIO,
            problem = "O custo de um carro vai muito além da parcela e da gasolina. Seguro, IPVA, manutenção e estacionamento podem dobrar o valor.",
            steps = listOf(
                "Some custos fixos do ano: IPVA, licenciamento, seguro e manutenção preventiva, e divida por 12.",
                "Some custos variáveis do mês: combustível, estacionamento, pedágio e lavagem.",
                "Inclua a perda de valor do veículo com o tempo.",
                "Compare com o custo mensal de transporte público mais algumas corridas de aplicativo.",
                "Se for financiar, veja o total pago com juros, não só a parcela.",
            ),
            checklist = listOf("Somei os custos fixos", "Comparei com outras opções"),
            related = listOf("transporte-custo-real", "compras-parceladas"),
        ),
        Guide(
            "conhecer-bairro", "transporte", "Bairro", "Conhecer o bairro novo nos primeiros dias", "🗺️", 4, FACIL,
            problem = "Saber onde ficam mercado, farmácia, posto de saúde e ponto de ônibus deixa a vida mais fácil desde a primeira semana.",
            steps = listOf(
                "Dê uma volta a pé de dia pelo entorno de casa.",
                "Marque no mapa do celular: mercado, farmácia, padaria, UBS, ponto de ônibus e lotérica.",
                "Descubra os dias da feira e da coleta de lixo e recicláveis.",
                "Converse com vizinhos e porteiro: eles conhecem os melhores serviços do bairro.",
                "Salve os contatos úteis na aba Contatos do app.",
            ),
            checklist = listOf("Marquei os lugares no mapa", "Sei o dia da coleta de lixo"),
            related = listOf("primeiro-dia", "sus-como-usar", "vizinhos"),
        ),
        Guide(
            "viagem-casa-fechada", "transporte", "Viagens", "Viajar e deixar a casa fechada com segurança", "🧳", 4, FACIL,
            problem = "Viajar morando sozinho significa deixar a casa sem ninguém. Uma checklist rápida evita surpresas na volta.",
            steps = listOf(
                "Feche o registro do gás e, se for muitos dias, o registro geral da água.",
                "Tire da tomada aparelhos que não precisam ficar ligados.",
                "Esvazie o lixo e tire da geladeira o que vai estragar.",
                "Peça a alguém de confiança para recolher correspondências.",
                "Não poste nas redes que está fora enquanto viaja.",
            ),
            checklist = listOf("Gás fechado", "Aparelhos desligados", "Lixo retirado", "Alguém avisado"),
            related = listOf("gas", "regras-casa"),
        ),
    )
}
