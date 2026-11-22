package br.com.casaclara.domain.content

import br.com.casaclara.domain.content.Difficulty.*

object DigitalGuides {
    private val CAIU = danger("Caiu num golpe? Avise o banco na hora pelo app ou pelo telefone do verso do cartão, peça a devolução do Pix (MED) e registre boletim de ocorrência.")

    val all = listOf(
        Guide(
            "golpe-pix", "digital", "Pix", "Golpes com Pix: como reconhecer e se proteger", "⚡", 5, FACIL,
            problem = "O Pix é rápido para você e para o golpista. A maioria dos golpes usa pressa e emoção para fazer você transferir antes de pensar.",
            steps = listOf(
                "Antes de confirmar qualquer Pix, leia o nome do recebedor que aparece na tela.",
                "Desconfie de urgência: \"paga agora senão perde\", \"é só hoje\", \"seu parente precisa\".",
                "Defina no app do banco um limite menor para Pix à noite e para contatos novos.",
                "Nunca faça Pix para \"desbloquear\" prêmio, empréstimo ou entrega.",
                "Comprovante de Pix enviado por foto pode ser falso: confira no extrato se o dinheiro entrou.",
            ),
            mistakes = listOf("Confiar só no comprovante mandado pelo comprador.", "Manter limite alto de Pix noturno sem precisar."),
            checklist = listOf("Ajustei meus limites de Pix", "Sei acionar o MED no meu banco"),
            related = listOf("pix-errado", "golpe-falso-parente", "golpe-falsa-central"),
            alert = CAIU,
        ),
        Guide(
            "golpe-falso-parente", "digital", "WhatsApp", "\"Oi, troquei de número\": o golpe do falso parente", "🎭", 4, FACIL,
            problem = "Golpistas usam a foto de alguém da sua família, dizem que trocaram de número e logo pedem dinheiro emprestado. Funciona porque mexe com o afeto.",
            steps = listOf(
                "Ligue para o número antigo da pessoa ou para outro familiar antes de qualquer coisa.",
                "Faça uma pergunta que só a pessoa saberia responder.",
                "Desconfie se a conta para Pix estiver no nome de outra pessoa.",
                "Combine com a família uma palavra-código para pedidos de dinheiro.",
                "Denuncie o número no próprio WhatsApp e avise a pessoa que foi usada.",
            ),
            checklist = listOf("Combinei uma palavra-código com a família"),
            related = listOf("golpe-pix", "whatsapp-clonado"),
            alert = CAIU,
        ),
        Guide(
            "golpe-falsa-central", "digital", "Telefone", "Ligação da \"central do banco\": o golpe da falsa central", "📞", 4, FACIL,
            problem = "Alguém liga dizendo ser do banco, fala de uma compra suspeita e pede senha, código ou que você faça um Pix para \"conta segura\". Banco não faz isso.",
            steps = listOf(
                "Banco nunca pede senha, código de SMS, token ou transferência para proteger sua conta.",
                "Desligue e ligue você mesmo para o número oficial do banco, que fica no verso do cartão.",
                "Não instale aplicativos que pedirem por telefone: eles podem dar acesso remoto ao celular.",
                "Mesmo que o número que ligou pareça do banco, ele pode estar falsificado.",
            ),
            mistakes = listOf("Seguir instruções por telefone com pressa.", "Passar o código que chegou por SMS."),
            checklist = listOf("Salvei o número oficial do meu banco"),
            related = listOf("golpe-pix", "senhas-fortes"),
            alert = CAIU,
        ),
        Guide(
            "whatsapp-clonado", "digital", "WhatsApp", "Proteger o WhatsApp contra clonagem", "🔒", 3, FACIL,
            problem = "Com o código de seis dígitos do SMS, alguém consegue ativar seu WhatsApp em outro aparelho e pedir dinheiro aos seus contatos.",
            steps = listOf(
                "Ative a confirmação em duas etapas: Configurações > Conta > Confirmação em duas etapas.",
                "Cadastre um e-mail de recuperação na mesma tela.",
                "Nunca repasse o código de seis dígitos que chega por SMS, nem para \"pesquisa\" ou \"promoção\".",
                "Se perder o acesso, reinstale o app e entre com seu número para derrubar o invasor, e avise seus contatos.",
            ),
            checklist = listOf("Ativei duas etapas no WhatsApp", "Cadastrei e-mail de recuperação"),
            related = listOf("golpe-falso-parente", "senhas-fortes"),
        ),
        Guide(
            "senhas-fortes", "digital", "Contas", "Senhas fortes sem precisar decorar tudo", "🗝️", 4, FACIL,
            problem = "Usar a mesma senha em tudo é o jeito mais comum de perder várias contas de uma vez. Dá para ter senhas fortes sem sofrimento.",
            steps = listOf(
                "Use frases longas como senha: quatro palavras aleatórias são mais fortes que uma palavra com símbolos.",
                "Tenha senhas diferentes pelo menos para e-mail, banco e gov.br.",
                "Use o gerenciador de senhas do próprio celular ou do navegador para guardar o resto.",
                "Ative a verificação em duas etapas onde puder, principalmente no e-mail.",
                "Troque a senha imediatamente se receber aviso de login que você não fez.",
            ),
            mistakes = listOf("Usar data de nascimento como senha.", "Anotar senhas em papel colado no celular."),
            checklist = listOf("Senha única no e-mail", "Senha única no banco", "Duas etapas ativas no e-mail"),
            related = listOf("gov-br-conta", "whatsapp-clonado"),
        ),
        Guide(
            "links-falsos", "digital", "Mensagens", "Links falsos por SMS, e-mail e redes", "🎣", 4, FACIL,
            problem = "Mensagens que imitam banco, correios, loja ou órgãos públicos levam para sites falsos que roubam dados e senhas.",
            steps = listOf(
                "Desconfie de mensagens com pressa: \"sua conta será bloqueada\", \"pagamento pendente\", \"encomenda retida\".",
                "Não clique: abra o app oficial ou digite o endereço do site você mesmo.",
                "Olhe o endereço com calma: letras trocadas e domínios estranhos são sinais de golpe.",
                "Órgãos do governo usam endereços terminados em .gov.br.",
                "Se já digitou dados, troque a senha e avise o banco.",
            ),
            checklist = listOf("Sei verificar o endereço de um site"),
            related = listOf("boleto-falso", "senhas-fortes"),
            alert = CAIU,
        ),
        Guide(
            "boleto-falso", "digital", "Pagamentos", "Boleto falso: confira antes de pagar", "🧾", 4, MEDIO,
            problem = "Boletos adulterados mudam o beneficiário e o dinheiro vai para o golpista. Acontece com boletos recebidos por e-mail, WhatsApp e até impressos.",
            steps = listOf(
                "Na tela de pagamento, confira se o nome do beneficiário é a empresa que você deve pagar.",
                "Confira se o valor e o vencimento batem com o que foi combinado.",
                "Prefira baixar a segunda via direto no site ou app da empresa.",
                "Desconfie de boleto com desconto grande ou que chegou por remetente estranho.",
            ),
            mistakes = listOf("Pagar sem olhar o beneficiário."),
            checklist = listOf("Confiro o beneficiário antes de pagar"),
            related = listOf("links-falsos", "evitar-atrasos"),
            alert = CAIU,
        ),
        Guide(
            "golpe-vaga-emprego", "digital", "Trabalho", "Golpes de vaga de emprego e renda fácil", "🚩", 4, FACIL,
            problem = "Ofertas de \"ganhe dinheiro curtindo vídeos\", vagas que pedem pagamento e tarefas por Pix são golpes comuns, principalmente para quem procura renda.",
            steps = listOf(
                "Empresa séria não cobra para você trabalhar: nem curso, nem uniforme, nem cadastro.",
                "Desconfie de pagamento alto por tarefa fácil e de grupos que pedem para você \"investir\" antes de sacar.",
                "Pesquise o nome e o CNPJ da empresa e procure reclamações.",
                "Não envie fotos de documentos antes de uma entrevista real.",
            ),
            checklist = listOf("Pesquisei a empresa antes de mandar documentos"),
            related = listOf("entrevista", "golpe-pix"),
            alert = CAIU,
        ),
        Guide(
            "golpe-nome-sujo", "digital", "Crédito", "Proteger o CPF de empréstimos e contas abertas em seu nome", "🛡️", 5, MEDIO,
            problem = "Com seus dados, golpistas abrem contas, fazem crediário e empréstimos. Você só descobre quando a dívida aparece. Alguns cuidados ajudam a perceber cedo.",
            steps = listOf(
                "Consulte o Registrato, no site do Banco Central, para ver contas e empréstimos em seu nome (é gratuito).",
                "Consulte de vez em quando seu CPF nos birôs de crédito gratuitos.",
                "Não poste fotos de documentos, cartão ou comprovantes nas redes.",
                "Encontrou algo que não é seu? Faça boletim de ocorrência e conteste na empresa e no consumidor.gov.br.",
            ),
            checklist = listOf("Consultei o Registrato", "Consultei meu CPF"),
            related = listOf("perdi-documentos", "consumidor-gov"),
            legal = true,
        ),
        Guide(
            "pix-errado", "digital", "Pix", "Mandei Pix errado ou caí num golpe: o que fazer", "↩️", 4, MEDIO,
            problem = "Agir rápido aumenta muito a chance de recuperar o dinheiro. O Pix tem um mecanismo oficial de devolução para casos de golpe.",
            steps = listOf(
                "Pix para a pessoa errada por engano: peça a devolução educadamente ao recebedor e procure seu banco.",
                "Golpe ou fraude: avise seu banco imediatamente e peça para acionar o MED (Mecanismo Especial de Devolução).",
                "Faça um boletim de ocorrência e anote o número de protocolo do banco.",
                "Guarde prints das conversas e o comprovante da transação.",
                "Se o banco não ajudar, registre reclamação no Banco Central e no consumidor.gov.br.",
            ),
            checklist = listOf("Sei onde fica a opção de contestar Pix no meu banco"),
            related = listOf("golpe-pix", "consumidor-gov"),
            alert = CAIU, legal = true,
        ),
    )
}
