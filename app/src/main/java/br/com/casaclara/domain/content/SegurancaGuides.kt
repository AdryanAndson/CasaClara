package br.com.casaclara.domain.content

import br.com.casaclara.domain.content.Difficulty.*

object SegurancaGuides {
    private val EMERGENCIA = danger("Emergência exige ajuda profissional: Bombeiros 193 · SAMU 192 · Polícia Militar 190 · Defesa Civil 199. Ligue de um local seguro.")

    val all = listOf(
        Guide(
            "gas", "seguranca", "Gás", "Cuidados básicos com gás", "🔥", 4, FACIL,
            problem = "Gás de cozinha é seguro quando bem instalado e cuidado. Pequenos descuidos, porém, podem causar acidentes graves.",
            steps = listOf(
                "Mantenha o botijão em local ventilado, de pé, longe de calor e fora de armários fechados, quando possível.",
                "Mangueira e regulador têm prazo de validade impresso: troque quando vencer ou se houver ressecamento.",
                "Use apenas mangueira e regulador certificados (com selo do Inmetro).",
                "Feche o registro quando for viajar ou ficar muitos dias fora.",
                "Nunca deixe panela no fogo sem ninguém por perto.",
                "Instalação e conserto de gás encanado: só com profissional ou a distribuidora.",
            ),
            checklist = listOf("Conferi a validade da mangueira", "Conferi o regulador", "Botijão em local ventilado"),
            related = listOf("vazamento-gas", "incendio"),
            alert = EMERGENCIA,
        ),
        Guide(
            "vazamento-gas", "seguranca", "Gás", "Como agir em caso de vazamento de gás", "🚨", 3, FACIL,
            problem = "Cheiro de gás é sinal de perigo. A forma de agir nos primeiros minutos faz toda a diferença.",
            steps = listOf(
                "Não acenda luzes, não ligue nem desligue aparelhos elétricos e não use fósforo, isqueiro ou celular dentro do local.",
                "Feche o registro do gás, se puder fazer isso com segurança.",
                "Abra portas e janelas para ventilar.",
                "Saia do imóvel e afaste as pessoas.",
                "De fora, ligue para os Bombeiros (193) ou para a distribuidora de gás.",
                "Só volte quando um profissional liberar o local.",
            ),
            mistakes = listOf("Procurar o vazamento com fogo.", "Ligar o exaustor ou o interruptor para ventilar."),
            checklist = listOf("Sei onde fica o registro", "Tenho o telefone da distribuidora salvo"),
            related = listOf("gas", "contatos-emergencia"),
            alert = EMERGENCIA,
        ),
        Guide(
            "eletricidade", "seguranca", "Eletricidade", "Cuidados básicos com eletricidade", "⚡", 4, FACIL,
            problem = "Sobrecarga, fios expostos e improvisos são causas comuns de choques e incêndios em casa.",
            steps = listOf(
                "Evite benjamins e extensões com muitos aparelhos, principalmente os que esquentam (ferro, aquecedor, air fryer).",
                "Não use aparelhos com fio descascado ou tomada derretida.",
                "Saiba onde fica o quadro de disjuntores e para que serve cada um.",
                "Tomada esquentando, cheiro de queimado ou disjuntor que desarma sempre: desligue e chame um eletricista.",
                "Não mexa em fios com mãos ou pés molhados.",
                "Troque lâmpadas com o interruptor desligado.",
            ),
            checklist = listOf("Sei onde fica o quadro de luz", "Sem benjamins sobrecarregados", "Sem fios expostos"),
            related = listOf("incendio", "chamar-profissional"),
            alert = danger("Choque elétrico: não toque na pessoa enquanto ela estiver em contato com a corrente. Desligue o disjuntor, se puder, e ligue 192 ou 193."),
        ),
        Guide(
            "incendio", "seguranca", "Incêndio", "Prevenção de incêndios", "🧯", 4, FACIL,
            problem = "A maioria dos incêndios domésticos começa na cozinha ou em problemas elétricos. Prevenção e reação rápida salvam vidas.",
            steps = listOf(
                "Não deixe panela no fogo sem supervisão, principalmente com óleo.",
                "Mantenha panos e papéis longe do fogão.",
                "Fogo em panela: desligue o fogo e abafe com uma tampa. Nunca jogue água em óleo pegando fogo.",
                "Não deixe velas acesas sem ninguém por perto.",
                "Tenha um plano de saída e saiba onde ficam as rotas de fuga do prédio.",
                "Se o fogo não puder ser controlado: saia, feche as portas atrás de você e ligue 193.",
            ),
            checklist = listOf("Sei como abafar fogo na panela", "Conheço a rota de saída", "Panos longe do fogão"),
            related = listOf("gas", "eletricidade"),
            alert = EMERGENCIA,
        ),
        Guide(
            "falta-energia-agua", "seguranca", "Imprevistos", "Como se preparar para falta de energia ou água", "🔦", 3, FACIL,
            problem = "Quedas de energia e falta de água acontecem. Uma pequena preparação deixa tudo mais tranquilo.",
            steps = listOf(
                "Tenha lanterna (ou luminária recarregável) e pilhas em local fácil.",
                "Mantenha um carregador portátil carregado.",
                "Guarde água potável para beber por um ou dois dias.",
                "Na falta de energia, desligue da tomada aparelhos sensíveis para evitar danos quando a energia voltar.",
                "Abra a geladeira o mínimo possível para conservar o frio.",
                "Salve o telefone da distribuidora de energia e da companhia de água.",
            ),
            checklist = listOf("Lanterna e pilhas", "Água guardada", "Telefones salvos"),
            related = listOf("contatos-emergencia"),
        ),
        Guide(
            "contatos-emergencia", "seguranca", "Imprevistos", "Como organizar contatos de emergência", "📞", 2, FACIL,
            problem = "Na hora do susto, a cabeça não ajuda. Ter os contatos prontos economiza segundos importantes.",
            steps = listOf(
                "Salve no app: Bombeiros 193, SAMU 192, Polícia Militar 190, Defesa Civil 199.",
                "Inclua a distribuidora de energia, a companhia de água e a de gás da sua região.",
                "Inclua uma pessoa de confiança, o síndico ou portaria e o proprietário.",
                "Configure o contato de emergência no seu celular (os sistemas permitem acesso pela tela de bloqueio).",
            ),
            checklist = listOf("Salvei os números oficiais", "Salvei serviços da região", "Salvei uma pessoa de confiança"),
            related = listOf("contatos-profissionais", "vazamento-gas"),
            alert = EMERGENCIA,
        ),
        Guide(
            "golpes-servicos", "seguranca", "Golpes", "Como evitar golpes em anúncios e serviços", "🕵️", 4, FACIL,
            problem = "Golpes em anúncios, falsas centrais de atendimento e prestadores falsos estão cada vez mais comuns.",
            steps = listOf(
                "Desconfie de urgência e de preços bons demais.",
                "Não pague antecipado a desconhecidos sem ver o produto ou o serviço.",
                "Confira o telefone e o site oficial da empresa antes de passar dados.",
                "Nunca informe senhas ou códigos recebidos por SMS ou aplicativo.",
                "Ao receber um técnico, peça identificação e confirme o agendamento com a empresa pelo canal oficial.",
                "Caiu em golpe? Avise o banco, registre boletim de ocorrência e denuncie na plataforma.",
            ),
            checklist = listOf("Confiro canais oficiais", "Não compartilho códigos", "Peço identificação de técnicos"),
            related = listOf("golpes-aluguel", "proteger-documentos"),
        ),
        Guide(
            "proteger-documentos", "seguranca", "Dados", "Como proteger documentos e informações pessoais", "🔒", 3, FACIL,
            problem = "Documentos perdidos ou dados vazados podem ser usados em fraudes. Alguns hábitos reduzem muito o risco.",
            steps = listOf(
                "Guarde documentos originais em local fixo e seguro em casa.",
                "Mantenha cópias digitais protegidas por senha.",
                "Use senha forte e diferente em cada serviço, e ative verificação em duas etapas.",
                "Rasgue papéis com dados pessoais antes de descartar.",
                "Em caso de perda ou roubo, registre ocorrência e avise os órgãos e bancos envolvidos.",
            ),
            checklist = listOf("Documentos em local fixo", "Cópias digitais protegidas", "Verificação em duas etapas ativada"),
            related = listOf("organizar-documentos", "golpes-servicos"),
        ),
    )
}
