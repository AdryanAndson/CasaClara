package br.com.casaclara.domain.content

/** Listas sugeridas: modelos que a pessoa escolhe carregar e pode editar. Não são dados de demonstração. */
object Templates {
    data class Item(val group: String, val title: String)

    val PREP = listOf(
        Item("1. Entender os números", "Calcular quanto custa morar sozinho"),
        Item("1. Entender os números", "Anotar renda e gastos de um mês"),
        Item("1. Entender os números", "Ver quanto preciso ganhar para o aluguel"),
        Item("2. Juntar o dinheiro", "Criar a meta do custo inicial da mudança"),
        Item("2. Juntar o dinheiro", "Começar a reserva de emergência"),
        Item("3. Aprender o básico", "Aprender 5 receitas simples"),
        Item("3. Aprender o básico", "Lavar e passar minhas roupas por um mês"),
        Item("3. Aprender o básico", "Fazer uma faxina completa sem ajuda"),
        Item("3. Aprender o básico", "Pagar minhas contas sozinho(a) por um mês"),
        Item("4. Procurar o lugar", "Escolher bairros que cabem no bolso"),
        Item("4. Procurar o lugar", "Visitar pelo menos 3 imóveis"),
        Item("4. Procurar o lugar", "Entender contrato e garantias"),
    )

    val MOVING = listOf(
        Item("60 dias antes", "Definir o orçamento da mudança"),
        Item("60 dias antes", "Separar o que vai, o que doa e o que vende"),
        Item("60 dias antes", "Pedir pelo menos 3 orçamentos de frete"),
        Item("30 dias antes", "Assinar o contrato e fazer a vistoria de entrada"),
        Item("30 dias antes", "Pedir ligação de energia e água"),
        Item("30 dias antes", "Agendar instalação de internet"),
        Item("30 dias antes", "Juntar caixas, fita, plástico bolha e canetão"),
        Item("1 semana antes", "Embalar por cômodo e etiquetar"),
        Item("1 semana antes", "Confirmar frete e avisar o condomínio"),
        Item("1 semana antes", "Montar a caixa do primeiro dia"),
        Item("No dia", "Fotografar os medidores"),
        Item("No dia", "Conferir se tudo chegou sem avaria"),
        Item("No dia", "Montar a cama primeiro"),
        Item("Primeira semana", "Atualizar endereço no banco e nos apps"),
        Item("Primeira semana", "Trocar o segredo da fechadura (se possível)"),
        Item("Primeira semana", "Conhecer mercado, farmácia e ponto de ônibus"),
    )

    val DOCUMENTS = listOf(
        Item("Pessoais", "Documento de identidade com foto"),
        Item("Pessoais", "CPF"),
        Item("Pessoais", "Comprovante de residência atual"),
        Item("Renda", "Comprovantes de renda (holerites, extratos ou declaração)"),
        Item("Renda", "Declaração de imposto de renda, se tiver"),
        Item("Aluguel", "Contrato de aluguel assinado (cópia)"),
        Item("Aluguel", "Laudo de vistoria de entrada com fotos"),
        Item("Aluguel", "Comprovante da garantia (caução, seguro ou título)"),
        Item("Aluguel", "Comprovantes de pagamento"),
        Item("Casa nova", "Protocolos de ligação de luz, água e internet"),
        Item("Casa nova", "Notas fiscais e garantias dos móveis"),
    )

    val VISIT = listOf(
        "Abrir torneiras e chuveiro: tem pressão e água quente?",
        "Dar descarga e procurar vazamentos",
        "Procurar mofo e infiltração (teto, cantos, armários)",
        "Testar tomadas e interruptores",
        "Abrir e fechar portas e janelas",
        "Testar o sinal do celular",
        "Observar iluminação natural e ventilação",
        "Checar o barulho da rua",
        "Ver onde ficam os medidores",
        "Conferir fechaduras e portaria",
        "Ver espaço para máquina de lavar e varal",
        "Medir os cômodos",
        "Perguntar sobre regras do condomínio",
        "Fotografar tudo",
    )

    val QUESTIONS = listOf(
        Item("Custos", "Qual o valor total por mês (aluguel + condomínio + IPTU)?"),
        Item("Custos", "O condomínio inclui água ou gás?"),
        Item("Custos", "Há alguma taxa extra (contrato, cadastro, chaves)?"),
        Item("Contrato", "Qual o prazo e a multa por saída antecipada?"),
        Item("Contrato", "Quais garantias são aceitas?"),
        Item("Contrato", "Como e quando é o reajuste? Por qual índice?"),
        Item("Contrato", "Quem paga pequenos reparos?"),
        Item("Imóvel", "Por que o último inquilino saiu?"),
        Item("Imóvel", "Já teve infiltração, vazamento ou problema elétrico?"),
        Item("Imóvel", "Aceita pets? Posso pintar ou furar paredes?"),
        Item("Imóvel", "A vistoria de entrada terá fotos e será por escrito?"),
        Item("Condomínio", "Quais as regras de barulho, mudança e visitas?"),
    )

    val CLEANING = mapOf(
        "Cozinha" to listOf("Lavar a louça", "Limpar o fogão", "Limpar pia e bancada", "Tirar o lixo", "Limpar a geladeira", "Varrer e passar pano"),
        "Banheiro" to listOf("Limpar o vaso sanitário", "Limpar pia e espelho", "Esfregar o box", "Trocar as toalhas", "Tirar o lixo", "Limpar o ralo"),
        "Quarto" to listOf("Arrumar a cama", "Trocar a roupa de cama", "Tirar o pó", "Organizar o guarda-roupa", "Varrer e passar pano"),
        "Sala" to listOf("Tirar o pó", "Varrer ou aspirar", "Organizar almofadas", "Limpar janelas"),
        "Área de serviço" to listOf("Lavar roupas", "Estender e recolher", "Limpar o tanque", "Limpar o filtro da máquina"),
    )

    data class Task(val title: String, val frequency: String, val room: String)
    val HOUSE_TASKS = listOf(
        Task("Lavar a louça", "DIARIA", "Cozinha"),
        Task("Arrumar a cama", "DIARIA", "Quarto"),
        Task("Tirar o lixo", "DIARIA", "Casa toda"),
        Task("Lavar roupas", "SEMANAL", "Área de serviço"),
        Task("Limpar o banheiro", "SEMANAL", "Banheiro"),
        Task("Varrer e passar pano", "SEMANAL", "Casa toda"),
        Task("Trocar a roupa de cama", "QUINZENAL", "Quarto"),
        Task("Limpar a geladeira", "MENSAL", "Cozinha"),
        Task("Limpar o filtro da máquina", "MENSAL", "Área de serviço"),
        Task("Conferir validade da mangueira de gás", "SEMESTRAL", "Cozinha"),
    )

    data class Essential(val name: String, val room: String, val priority: String)
    val ESSENTIALS = listOf(
        Essential("Cama ou colchão", "Quarto", "AGORA"), Essential("Travesseiro e roupa de cama", "Quarto", "AGORA"),
        Essential("Toalhas de banho", "Banheiro", "AGORA"), Essential("Geladeira", "Cozinha", "AGORA"),
        Essential("Fogão ou cooktop", "Cozinha", "AGORA"), Essential("Panelas básicas", "Cozinha", "AGORA"),
        Essential("Pratos, copos e talheres", "Cozinha", "AGORA"), Essential("Kit de limpeza", "Área de serviço", "AGORA"),
        Essential("Lâmpadas", "Casa toda", "AGORA"), Essential("Cortina do quarto", "Quarto", "DEPOIS"),
        Essential("Máquina de lavar", "Área de serviço", "DEPOIS"), Essential("Micro-ondas", "Cozinha", "DEPOIS"),
        Essential("Mesa e cadeiras", "Sala", "DEPOIS"), Essential("Guarda-roupa ou arara", "Quarto", "DEPOIS"),
        Essential("Sofá", "Sala", "LEGAL"), Essential("Televisão", "Sala", "LEGAL"), Essential("Tapete e decoração", "Sala", "LEGAL"),
    )

    val INVENTORY = listOf(
        "Papel higiênico" to "Banheiro", "Sabonete" to "Banheiro", "Detergente" to "Cozinha", "Esponja" to "Cozinha",
        "Sabão para roupas" to "Área de serviço", "Água sanitária" to "Área de serviço", "Saco de lixo" to "Cozinha",
        "Lâmpada reserva" to "Casa toda", "Pilhas" to "Casa toda", "Lanterna" to "Casa toda", "Kit de primeiros socorros" to "Banheiro",
    )

    val AGREEMENTS = listOf(
        "Contas" to "As contas fixas são divididas conforme o critério combinado e acertadas até o dia 10.",
        "Limpeza" to "Áreas comuns seguem a escala de tarefas. Quem cozinha não lava a louça naquele dia.",
        "Visitas" to "Avisar antes de receber visitas para dormir.",
        "Silêncio" to "Silêncio depois das 22h em dias de semana.",
        "Itens compartilhados" to "Limpeza e papel higiênico entram como despesa da casa.",
    )

    val EMERGENCY = listOf(
        Triple("Bombeiros", "193", "Incêndio, vazamento de gás, resgate"),
        Triple("SAMU", "192", "Emergência médica"),
        Triple("Polícia Militar", "190", "Segurança"),
        Triple("Defesa Civil", "199", "Desastres, alagamentos, risco estrutural"),
    )
}

object Tips {
    val ALL = listOf(
        "Anote os pequenos gastos por duas semanas. Eles costumam surpreender.",
        "Guarde dinheiro logo que receber, não o que sobrar no fim do mês.",
        "Regra dos 2 minutos: se leva menos que isso, faça agora.",
        "Nunca misture produtos de limpeza. Um por vez, com janela aberta.",
        "Antes de comprar um móvel, meça o espaço e a porta.",
        "Nada de pagar sinal antes de visitar o imóvel e assinar o contrato.",
        "Fotografe os medidores no dia em que pegar as chaves.",
        "Cozinhe em quantidade e congele porções para a semana.",
        "Compare o preço por quilo, não o da embalagem.",
        "Primeiro que entra, primeiro que sai: o mais velho vai na frente da despensa.",
        "A mangueira do gás tem validade impressa. Já conferiu a sua?",
        "Cheiro de gás? Não acenda nada, abra as janelas e ligue 193 de fora.",
        "Fogo na panela se abafa com tampa. Água em óleo quente, nunca.",
        "Benjamim lotado é convite para curto-circuito.",
        "Contas com lembrete não viram multa.",
        "Parcela pequena, total grande: sempre compare com o preço à vista.",
        "Reserva de emergência é para emergência. Viagem é meta.",
        "Revise suas assinaturas a cada três meses.",
        "Uma faxina por cômodo cansa bem menos que a casa toda de uma vez.",
        "Deixe a porta do banheiro aberta depois do banho para o vapor sair.",
        "Afaste os móveis alguns centímetros da parede para evitar mofo.",
        "Combinado escrito evita briga entre moradores.",
        "Regra das 48 horas: deixou no carrinho, decida depois.",
        "Guarde as notas fiscais: a garantia depende delas.",
        "Lanterna e água guardadas deixam a falta de luz menos assustadora.",
        "Banana madura vira bolo. Arroz de ontem vira bolinho.",
        "Você não precisa comprar a casa inteira no primeiro dia.",
        "Pausar a jornada também é uma escolha inteligente.",
        "Antes de assinar, confira quem é o dono do imóvel.",
        "Planeje a semana em 10 minutos no domingo.",
    )

    fun ofDay(epochDay: Long): String = ALL[((epochDay % ALL.size + ALL.size) % ALL.size).toInt()]
}
