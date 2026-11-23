package br.com.casaclara.domain.content

/**
 * Modelos prontos de mensagens e cartas para situações comuns da vida independente.
 * Campos entre chaves, como {nome}, viram campos para preencher. O texto é sempre editável pela pessoa.
 */
object MessageTemplates {
    enum class Channel(val label: String, val emoji: String) { WHATSAPP("Mensagem", "💬"), EMAIL("E-mail", "✉️"), CARTA("Carta/documento", "📄") }

    data class Cat(val id: String, val title: String, val emoji: String)
    data class Template(
        val id: String, val cat: String, val title: String, val emoji: String, val description: String,
        val channel: Channel, val body: String, val tip: String? = null, val guide: String? = null,
    ) {
        /** Campos na ordem em que aparecem no texto. */
        val fields: List<String> get() = FIELD_RE.findAll(body).map { it.groupValues[1] }.distinct().toList()
        fun fill(values: Map<String, String>): String = FIELD_RE.replace(body) { m ->
            val k = m.groupValues[1]
            values[k]?.trim()?.ifBlank { null } ?: "[${LABELS[k] ?: k}]"
        }
    }

    private val FIELD_RE = Regex("\\{([a-zA-Z0-9_]+)\\}")

    /** Campos que vêm do "Meus dados" (preenchidos uma vez, reaproveitados em todos os modelos). */
    val PROFILE_FIELDS = listOf("nome", "endereco", "cidade", "telefone")

    val LABELS = mapOf(
        "nome" to "Seu nome", "endereco" to "Seu endereço", "cidade" to "Sua cidade", "telefone" to "Seu telefone", "data" to "Data",
        "destinatario" to "Para quem (nome)", "problema" to "Qual é o problema", "desde" to "Desde quando", "dataSaida" to "Data da saída",
        "valorAtual" to "Valor atual", "valorProposto" to "Valor que você propõe", "motivo" to "Motivo", "loja" to "Loja ou empresa",
        "produto" to "Produto ou serviço", "pedido" to "Nº do pedido", "dataCompra" to "Data da compra", "dataRecebimento" to "Data em que recebeu",
        "protocolo" to "Nº do protocolo", "valor" to "Valor", "dataCobranca" to "Data da cobrança", "servico" to "Serviço", "contrato" to "Nº do contrato ou cliente",
        "divida" to "Qual dívida", "parcela" to "Parcela que cabe no seu bolso", "vezes" to "Em quantas vezes", "empresa" to "Empresa",
        "cargo" to "Seu cargo", "periodo" to "Período desejado", "dias" to "Quantos dias", "ultimoDia" to "Último dia de trabalho",
        "vaga" to "Vaga", "finalidade" to "Para que serve", "conta" to "Conta", "total" to "Valor total", "pessoas" to "Quantas pessoas",
        "porPessoa" to "Valor por pessoa", "pix" to "Sua chave Pix", "vencimento" to "Vencimento", "tarefa" to "Tarefa", "dia" to "Dia",
        "horario" to "Horário", "assunto" to "Assunto", "bairro" to "Bairro ou endereço do imóvel", "dataMudanca" to "Data da mudança",
        "ajuda" to "Que ajuda você precisa", "profissional" to "Tipo de profissional", "estabelecimento" to "Estabelecimento",
        "apto" to "Seu apartamento/casa", "local" to "Onde é o vazamento", "imovel" to "Imóvel (anúncio)", "cartao" to "Cartão (final)",
        "dataEntrevista" to "Data da entrevista", "detalhe" to "Algo que você gostou na conversa", "regras" to "Combinados propostos",
    )

    val CATS = listOf(
        Cat("aluguel", "Aluguel e imóvel", "🔑"),
        Cat("consumidor", "Compras e consumidor", "🛍️"),
        Cat("dinheiro", "Dinheiro e dívidas", "💰"),
        Cat("trabalho", "Trabalho", "💼"),
        Cat("compartilhada", "Casa compartilhada", "👥"),
        Cat("pessoas", "Família, vizinhos e serviços", "🤝"),
    )

    val ALL = listOf(
        // ------------------------------------------------ Aluguel e imóvel
        Template("pedir-conserto", "aluguel", "Pedir conserto ao proprietário", "🛠️", "Problema estrutural ou que já veio com o imóvel.", Channel.WHATSAPP,
            "Olá, {destinatario}! Tudo bem? Aqui é {nome}, do imóvel em {endereco}.\n\nQueria avisar que {problema}. Isso está acontecendo desde {desde}. Tirei fotos e posso enviar.\n\nPodemos combinar uma data para alguém olhar? Tenho disponibilidade nos próximos dias.\n\nObrigado(a)!",
            "Mande fotos junto e guarde a conversa. Problemas estruturais costumam ser responsabilidade do proprietário.", "documentar-problemas"),
        Template("aviso-saida", "aluguel", "Aviso de saída do imóvel", "📦", "Avisar com antecedência que vai desocupar.", Channel.CARTA,
            "{cidade}, {data}.\n\nPrezado(a) {destinatario},\n\nEu, {nome}, locatário(a) do imóvel situado em {endereco}, comunico que pretendo desocupar o imóvel em {dataSaida}, conforme previsto no contrato de locação.\n\nPeço que seja agendada a vistoria de saída e combinada a devolução das chaves e da garantia, quando for o caso.\n\nAtenciosamente,\n{nome}\n{telefone}",
            "Confira no contrato o prazo de aviso (é comum ser de 30 dias) e se há multa. Envie por escrito e guarde a confirmação.", "direitos-inquilino"),
        Template("negociar-aluguel", "aluguel", "Negociar o reajuste do aluguel", "🤝", "Pedir para manter ou reduzir o valor.", Channel.WHATSAPP,
            "Olá, {destinatario}! Aqui é {nome}, do imóvel em {endereco}.\n\nRecebi o aviso de reajuste. Gosto muito de morar aqui, pago sempre em dia e cuido bem do imóvel. Hoje o aluguel é {valorAtual}, e queria propor manter em {valorProposto}, porque {motivo}.\n\nPodemos conversar sobre isso? Fico à disposição.",
            "Pesquise o preço de imóveis parecidos no bairro antes de conversar. Inquilino bom pagador é um argumento forte.", "custos-alem-aluguel"),
        Template("vistoria-saida", "aluguel", "Agendar vistoria e devolução da caução", "🔍", "Para a saída correr sem surpresas.", Channel.WHATSAPP,
            "Olá, {destinatario}! Aqui é {nome}. Como combinado, vou entregar o imóvel em {endereco} no dia {dataSaida}.\n\nPodemos agendar a vistoria de saída nesse dia? Também queria combinar a devolução da caução.\n\nObrigado(a)!",
            "Leve as fotos da vistoria de entrada para comparar.", "registrar-chaves"),
        Template("pedir-recibo", "aluguel", "Pedir recibo do aluguel", "🧾", "Comprovante de pagamento.", Channel.WHATSAPP,
            "Olá, {destinatario}! Tudo bem? Aqui é {nome}. Paguei o aluguel de {data} no valor de {valor}. Você pode me enviar o recibo, por favor? Obrigado(a)!",
            "Guarde todos os recibos e comprovantes até o fim do contrato.", "organizar-papeis"),
        Template("perguntas-anuncio", "aluguel", "Perguntas para o anúncio de um imóvel", "🏘️", "Antes de marcar visita.", Channel.WHATSAPP,
            "Olá! Vi o anúncio do imóvel em {imovel} e tenho interesse. Algumas perguntas:\n\n1. O valor do aluguel inclui condomínio e IPTU?\n2. Quais garantias vocês aceitam (caução, fiador, seguro)?\n3. Aceita pets?\n4. Como é a água quente e a pressão do chuveiro?\n5. Quando posso visitar?\n\nObrigado(a)! {nome}",
            "Desconfie de quem pede depósito antes da visita.", "golpes-aluguel"),
        Template("aviso-sindico", "aluguel", "Avisar o síndico de um vazamento", "💧", "Problema que afeta o prédio.", Channel.WHATSAPP,
            "Olá, {destinatario}! Aqui é {nome}, do {apto}. Estou com um vazamento em {local} que parece vir da área comum (ou de outro apartamento). Começou em {desde}.\n\nPodemos ver isso o quanto antes? Tenho fotos.",
            "Feche o registro da sua casa se a água vier de dentro dela.", "cano-estourado"),
        Template("barulho-vizinho", "pessoas", "Conversar com o vizinho sobre barulho", "🔊", "Educado e sem briga.", Channel.WHATSAPP,
            "Oi, {destinatario}! Aqui é {nome}, do {apto}. Tudo bem? Queria comentar, numa boa, que o barulho {problema} tem chegado bastante aqui em casa, principalmente {horario}. Será que dá para a gente ver um jeito? Obrigado(a) pela compreensão!",
            "Comece conversando. Se não resolver, procure o síndico e o regimento interno.", "vizinhos"),

        // ------------------------------------------------ Compras e consumidor
        Template("arrependimento", "consumidor", "Desistir de compra on-line (7 dias)", "↩️", "Direito de arrependimento.", Channel.EMAIL,
            "Assunto: Desistência da compra — pedido {pedido}\n\nOlá, {loja}.\n\nEu, {nome}, comprei {produto} pelo site/aplicativo em {dataCompra} (pedido {pedido}) e recebi em {dataRecebimento}.\n\nCom base no artigo 49 do Código de Defesa do Consumidor, exerço meu direito de arrependimento dentro do prazo de 7 dias e solicito o cancelamento da compra e a devolução integral do valor pago, incluindo o frete.\n\nAguardo as instruções para devolução do produto.\n\nAtenciosamente,\n{nome}\n{telefone}",
            "Envie por escrito e guarde o protocolo. O prazo conta a partir do recebimento.", "direito-arrependimento"),
        Template("produto-defeito", "consumidor", "Produto com defeito", "🔧", "Pedir conserto, troca ou devolução.", Channel.EMAIL,
            "Assunto: Produto com defeito — pedido {pedido}\n\nOlá, {loja}.\n\nComprei {produto} em {dataCompra} e ele apresentou o seguinte problema: {problema}.\n\nConforme o Código de Defesa do Consumidor, solicito o reparo dentro do prazo de 30 dias. Caso não seja resolvido nesse prazo, peço a troca por outro em perfeitas condições ou a devolução do valor pago.\n\nAtenciosamente,\n{nome}\n{telefone}",
            "Guarde a nota fiscal e fotografe o defeito.", "garantia-legal"),
        Template("cancelar-servico", "consumidor", "Cancelar internet, celular ou assinatura", "✂️", "Cancelamento por escrito.", Channel.EMAIL,
            "Assunto: Pedido de cancelamento — {servico}\n\nOlá, {empresa}.\n\nEu, {nome}, titular do contrato/cliente nº {contrato}, solicito o cancelamento do serviço {servico} a partir de hoje, {data}.\n\nPeço a confirmação do cancelamento com número de protocolo e que nenhuma cobrança seja feita após esta data.\n\nAtenciosamente,\n{nome}",
            "Anote o protocolo e confira a fatura seguinte.", "cancelar-servicos"),
        Template("cobranca-indevida", "consumidor", "Contestar cobrança indevida", "💸", "Valor errado ou serviço não contratado.", Channel.EMAIL,
            "Assunto: Cobrança indevida — {servico}\n\nOlá, {empresa}.\n\nIdentifiquei na minha fatura de {dataCobranca} a cobrança de {valor} referente a {servico}, que não contratei / está incorreta.\n\nSolicito o cancelamento dessa cobrança e a devolução do valor pago indevidamente, conforme o artigo 42 do Código de Defesa do Consumidor.\n\nCliente/contrato: {contrato}\n\nAtenciosamente,\n{nome}",
            "Se a empresa não resolver, registre no consumidor.gov.br.", "cobranca-indevida"),
        Template("sem-solucao", "consumidor", "Reclamação sem solução (com protocolo)", "📣", "Quando o SAC não resolveu.", Channel.EMAIL,
            "Assunto: Reclamação sem solução — protocolo {protocolo}\n\nOlá, {empresa}.\n\nEm {data} registrei a reclamação de protocolo {protocolo} sobre {problema}. Até agora o problema não foi resolvido.\n\nPeço uma solução em até 5 dias úteis. Caso contrário, vou registrar reclamação no consumidor.gov.br e no Procon.\n\nAtenciosamente,\n{nome}\n{telefone}",
            "Junte prints e números de protocolo anteriores.", "consumidor-gov"),
        Template("atraso-entrega", "consumidor", "Entrega atrasada", "🚚", "Cobrar a loja pelo atraso.", Channel.WHATSAPP,
            "Olá, {loja}! Fiz o pedido {pedido} em {dataCompra} e a entrega já passou do prazo informado. Podem me dizer quando chega? Se não houver previsão, quero cancelar e receber o valor de volta. Obrigado(a). {nome}",
            "Se a loja não cumprir o prazo, você pode pedir o cancelamento com devolução.", "compras-online-seguras"),

        // ------------------------------------------------ Dinheiro e dívidas
        Template("negociar-divida", "dinheiro", "Negociar uma dívida", "📉", "Propor parcelas que cabem no bolso.", Channel.EMAIL,
            "Assunto: Proposta de negociação — {divida}\n\nOlá, {empresa}.\n\nEu, {nome}, tenho a dívida {divida} com vocês e quero regularizar a situação.\n\nHoje consigo pagar parcelas de até {parcela}, em {vezes} vezes, sem comprometer minhas contas básicas. Gostaria de saber se é possível um acordo nessas condições, com desconto de juros e multa.\n\nAguardo retorno.\n\nAtenciosamente,\n{nome}\n{telefone}",
            "Só aceite parcela que cabe de verdade. Negocie pelos canais oficiais do credor.", "nome-negativado"),
        Template("cobrar-amigo", "dinheiro", "Lembrar alguém de um empréstimo", "🙂", "Sem estragar a amizade.", Channel.WHATSAPP,
            "Oi, {destinatario}! Tudo bem? Passando pra lembrar daquele valor de {valor} que te emprestei. Consegue me devolver até {vencimento}? Se ficar difícil, me fala que a gente combina em partes. Meu Pix: {pix}. Abraço!",
            "Combinar data e forma de pagamento evita mal-entendidos.", null),
        Template("desconto-vista", "dinheiro", "Pedir desconto à vista", "🏷️", "Para compras maiores.", Channel.WHATSAPP,
            "Olá! Tenho interesse em {produto}. Se eu pagar à vista no Pix, qual é o melhor preço que vocês fazem? Obrigado(a)! {nome}",
            "Compare com o total parcelado na calculadora de parcelamento.", "compras-parceladas"),
        Template("contestar-cartao", "dinheiro", "Contestar compra no cartão", "💳", "Compra que você não reconhece.", Channel.EMAIL,
            "Assunto: Contestação de compra — cartão final {cartao}\n\nOlá.\n\nNão reconheço a compra de {valor} feita em {estabelecimento} no dia {dataCobranca}, no meu cartão final {cartao}.\n\nSolicito a contestação, o estorno do valor e o bloqueio do cartão, se necessário.\n\nAtenciosamente,\n{nome}",
            "Também conteste pelo app do banco, que costuma ser mais rápido.", "golpe-nome-sujo"),

        // ------------------------------------------------ Trabalho
        Template("aviso-falta", "trabalho", "Avisar falta por doença", "🤒", "Para a chefia ou o RH.", Channel.WHATSAPP,
            "Bom dia, {destinatario}. Aqui é {nome}. Não estou me sentindo bem e vou procurar atendimento hoje, por isso não vou conseguir trabalhar. Assim que tiver o atestado, envio para vocês. Obrigado(a) pela compreensão.",
            "Envie o atestado assim que possível, pelo canal que a empresa usa.", "doente-sozinho"),
        Template("pedir-ferias", "trabalho", "Pedir férias", "🏖️", "Combinar o período com antecedência.", Channel.EMAIL,
            "Assunto: Solicitação de férias\n\nOlá, {destinatario}.\n\nGostaria de solicitar minhas férias para o período de {periodo} ({dias} dias). Já organizei minhas atividades para que nada fique pendente.\n\nFico à disposição para ajustar as datas, se necessário.\n\nObrigado(a),\n{nome}\n{cargo}",
            "Lembre: o pagamento vem antes e o salário seguinte costuma vir menor.", "ferias-direitos"),
        Template("pedido-demissao", "trabalho", "Pedido de demissão", "🚪", "Carta formal.", Channel.CARTA,
            "{cidade}, {data}.\n\nÀ {empresa}\n\nEu, {nome}, ocupante do cargo de {cargo}, venho por meio desta comunicar meu pedido de demissão.\n\nInformo que cumprirei o aviso prévio conforme a legislação, sendo meu último dia de trabalho {ultimoDia}, salvo acordo diferente com a empresa.\n\nAgradeço pela oportunidade e pelo aprendizado.\n\nAtenciosamente,\n\n_______________________\n{nome}",
            "Peça uma cópia assinada com a data de recebimento. Confira a rescisão com calma.", "seguro-desemprego"),
        Template("agradecer-entrevista", "trabalho", "Agradecer depois da entrevista", "🌟", "Deixa uma boa impressão.", Channel.EMAIL,
            "Assunto: Obrigado pela entrevista — {vaga}\n\nOlá, {destinatario}.\n\nObrigado(a) pela conversa de {dataEntrevista} sobre a vaga de {vaga}. Gostei muito de conhecer mais sobre a equipe, principalmente {detalhe}.\n\nSigo muito interessado(a) e à disposição para os próximos passos.\n\nAbraço,\n{nome}\n{telefone}",
            "Mande no mesmo dia ou no dia seguinte.", "entrevista"),
        Template("declaracao-renda", "trabalho", "Pedir declaração de renda ao RH", "📑", "Para aluguel, financiamento ou crédito.", Channel.EMAIL,
            "Assunto: Solicitação de declaração de vínculo e renda\n\nOlá, {destinatario}.\n\nPreciso de uma declaração de vínculo empregatício com cargo e salário para {finalidade}. Seria possível emitir até {vencimento}?\n\nObrigado(a),\n{nome}",
            "Os últimos holerites também costumam servir como comprovante.", "holerite"),

        // ------------------------------------------------ Casa compartilhada
        Template("dividir-conta", "compartilhada", "Dividir uma conta da casa", "🧮", "Mensagem para o grupo.", Channel.WHATSAPP,
            "Oi, pessoal! Chegou a conta de {conta}: {total} no total, que dividido por {pessoas} dá {porPessoa} para cada um. Vence em {vencimento}. Meu Pix: {pix}. Valeu! 🙌",
            "Use a aba Casa compartilhada para calcular quem deve quanto.", "dividir-contas"),
        Template("combinados", "compartilhada", "Propor combinados da casa", "📋", "Regras claras evitam brigas.", Channel.WHATSAPP,
            "Oi, gente! Pra casa funcionar bem pra todo mundo, queria propor uns combinados:\n\n{regras}\n\nO que vocês acham? Dá pra ajustar o que precisar. Podemos conversar {dia}?",
            "Escreva em tópicos curtos e revise depois de um mês.", "regras-casa"),
        Template("lembrar-tarefa", "compartilhada", "Lembrar alguém de uma tarefa", "🧽", "Sem soar como cobrança.", Channel.WHATSAPP,
            "Oi, {destinatario}! Só lembrando que essa semana a {tarefa} é sua, combinado? Se precisar trocar de dia, me avisa que a gente ajeita. 😊",
            "Uma escala visível para todos evita esse tipo de mensagem.", "evitar-conflitos"),
        Template("reuniao-casa", "compartilhada", "Chamar para uma conversa da casa", "🗣️", "Resolver juntos.", Channel.WHATSAPP,
            "Oi, pessoal! Que tal uma conversa rápida {dia} às {horario} para alinhar {assunto}? Prometo que é coisa de 20 minutos. Traz o que quiser comentar também!",
            "Comece pelo que está funcionando antes dos problemas.", "evitar-conflitos"),

        // ------------------------------------------------ Família, vizinhos e serviços
        Template("cheguei", "pessoas", "Avisar que chegou em casa", "🏠", "Para alguém de confiança.", Channel.WHATSAPP,
            "Cheguei em casa, tudo certo! 😊 Obrigado(a) por se preocupar. Beijo!",
            "Combine esse aviso com alguém quando voltar tarde.", "voltar-noite"),
        Template("ajuda-mudanca", "pessoas", "Pedir ajuda na mudança", "📦", "Pedido concreto funciona melhor.", Channel.WHATSAPP,
            "Oi, {destinatario}! Vou me mudar no dia {dataMudanca} e queria saber se você pode me ajudar com {ajuda}. Seria das {horario}. O lanche é por minha conta! 😄",
            "Diga o quê, quando e por quanto tempo.", "pedir-ajuda"),
        Template("contato-emergencia", "pessoas", "Pedir para ser seu contato de emergência", "🆘", "Combine antes de precisar.", Channel.WHATSAPP,
            "Oi, {destinatario}! Como moro sozinho(a), queria te pedir uma coisa: posso deixar você como meu contato de emergência no celular? Se acontecer alguma coisa comigo, é para você que vão ligar. Tudo bem pra você?",
            "Depois, preencha o cartão de emergência do app.", "cartao-emergencia-guia"),
        Template("orcamento-profissional", "pessoas", "Pedir orçamento a um profissional", "🧰", "Encanador, eletricista, pintor…", Channel.WHATSAPP,
            "Olá! Preciso de um(a) {profissional} para {problema}, no bairro {bairro}. Você consegue me passar um orçamento e a disponibilidade? Posso mandar fotos. Obrigado(a)! {nome}",
            "Peça pelo menos 3 orçamentos e combine tudo por escrito.", "chamar-profissional"),
        Template("endereco-novo", "pessoas", "Avisar o endereço novo", "📬", "Para família e amigos.", Channel.WHATSAPP,
            "Oi! Mudei de casa 🎉 Meu endereço novo é {endereco}, {cidade}. Quando quiserem, apareçam para um café!",
            "Lembre de atualizar o endereço no banco, nos apps e nos documentos.", "atualizar-endereco"),
    )

    val BY_ID = ALL.associateBy { it.id }
}
