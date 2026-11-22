package br.com.casaclara.domain.content

import br.com.casaclara.domain.content.Difficulty.*

object MudancaGuides {
    val all = listOf(
        Guide(
            "planejar-mudanca", "mudanca", "Planejamento", "Como planejar uma mudança", "🗓️", 4, FACIL,
            problem = "Mudança deixada para a última semana vira noite sem dormir e caixa sem tampa. Com algumas semanas de antecedência, tudo fica mais leve.",
            steps = listOf(
                "Defina a data da mudança e marque no cronograma do app.",
                "Faça um inventário rápido do que vai levar e do que vai doar ou vender.",
                "Peça orçamentos de frete com antecedência.",
                "Comece a embalar o que você menos usa, umas 2 semanas antes.",
                "Agende a ligação de luz, água e internet no endereço novo.",
                "Use o checklist de mudança do app para não esquecer nada.",
            ),
            checklist = listOf("Data marcada", "Inventário feito", "Orçamentos de frete pedidos", "Serviços agendados"),
            related = listOf("o-que-embalar", "custo-transporte", "semana-antes"),
        ),
        Guide(
            "o-que-embalar", "mudanca", "Embalagem", "O que separar e embalar primeiro", "📦", 3, FACIL,
            problem = "Embalar tudo de uma vez é impossível. A ordem certa deixa você viver normalmente até o último dia.",
            steps = listOf(
                "Primeiro: coisas de estação, livros, decoração, objetos guardados.",
                "Depois: roupas que você não vai usar nas próximas semanas e utensílios extras.",
                "Por último: itens do dia a dia, roupa de cama do uso, itens de higiene.",
                "Separe à parte a \"caixa do primeiro dia\" e os documentos.",
                "Doe ou venda o que não vai usar — é menos coisa para carregar e pagar frete.",
            ),
            mistakes = listOf("Embalar documentos e carregadores junto com o resto.", "Caixas pesadas demais."),
            checklist = listOf("Separei o que vai, doa e vende", "Embalei o que menos uso", "Separei a caixa do primeiro dia"),
            related = listOf("caixas-comodo", "organizar-documentos"),
        ),
        Guide(
            "organizar-documentos", "mudanca", "Documentos", "Como organizar documentos", "🗂️", 3, FACIL,
            problem = "Na mudança, documentos se perdem fácil. E é justamente quando você mais vai precisar deles: contrato, comprovantes, ligação de serviços.",
            steps = listOf(
                "Separe uma pasta só para documentos e leve com você, não no caminhão.",
                "Inclua documentos pessoais, contrato de aluguel, laudo de vistoria, comprovantes de pagamento e garantias.",
                "Tire fotos ou digitalize e guarde em local seguro e com senha.",
                "Use a lista de documentos do app para conferir o que já separou.",
            ),
            checklist = listOf("Pasta separada", "Contrato e vistoria na pasta", "Cópias digitais seguras"),
            related = listOf("proteger-documentos", "o-que-embalar"),
        ),
        Guide(
            "custo-transporte", "mudanca", "Custos", "Como estimar o custo do transporte da mudança", "🚚", 3, FACIL,
            problem = "Frete pode variar muito de preço. Saber o que influencia o valor ajuda a pedir orçamentos certos e comparar.",
            steps = listOf(
                "Liste os itens grandes (geladeira, cama, sofá, máquina) e o número aproximado de caixas.",
                "Informe distância, andar, se tem elevador e se precisa de montagem e desmontagem.",
                "Peça pelo menos três orçamentos, com o mesmo pedido para todos.",
                "Pergunte o que está incluso: ajudantes, embalagem, seguro, montagem.",
                "Combine tudo por escrito, com data e horário.",
            ),
            mistakes = listOf("Comparar orçamentos com serviços diferentes.", "Esquecer de avisar sobre escadas."),
            checklist = listOf("Listei os itens grandes", "Pedi 3 orçamentos", "Combinei por escrito"),
            related = listOf("planejar-mudanca", "dia-mudanca"),
        ),
        Guide(
            "semana-antes", "mudanca", "Reta final", "O que fazer na semana anterior", "⏳", 3, FACIL,
            problem = "A última semana é corrida. Com uma lista clara, nada importante fica para a véspera.",
            steps = listOf(
                "Confirme data e horário com o frete e com o condomínio (muitos têm regras de horário para mudança).",
                "Termine de embalar, deixando só o essencial.",
                "Descongele e limpe a geladeira um dia antes, se for levar.",
                "Separe dinheiro ou forma de pagamento do frete.",
                "Confira se a energia e a água já estarão ligadas no novo endereço.",
            ),
            checklist = listOf("Frete confirmado", "Condomínio avisado", "Geladeira preparada", "Serviços ligados"),
            related = listOf("dia-mudanca", "atualizar-endereco"),
        ),
        Guide(
            "dia-mudanca", "mudanca", "Reta final", "O que conferir no dia da mudança", "🏁", 3, FACIL,
            problem = "No dia, a correria faz a gente esquecer de conferir. Alguns minutos de atenção evitam prejuízo.",
            steps = listOf(
                "Antes de sair, passe por todos os cômodos e armários do lugar antigo.",
                "Fotografe os medidores do imóvel novo ao chegar.",
                "Acompanhe a descarga e confira se cada caixa e móvel chegou sem avaria.",
                "Só libere o pagamento depois de conferir.",
                "Monte a cama primeiro: você vai agradecer à noite.",
            ),
            checklist = listOf("Revisei o lugar antigo", "Fotografei os medidores", "Conferi itens e avarias", "Montei a cama"),
            related = listOf("primeiro-dia", "registrar-chaves"),
        ),
        Guide(
            "primeiro-dia", "mudanca", "Casa nova", "O que resolver no primeiro dia na casa nova", "🏠", 3, FACIL,
            problem = "O primeiro dia é cansativo. Resolver só o essencial deixa a casa funcionando sem você se esgotar.",
            steps = listOf(
                "Teste luz, água, gás e chuveiro.",
                "Abra a caixa do primeiro dia: roupa de cama, toalha, higiene, carregadores.",
                "Garanta uma refeição simples e água para beber.",
                "Tranque bem portas e janelas. Se possível, troque o segredo da fechadura nos próximos dias.",
                "Deixe o resto das caixas para os próximos dias, cômodo por cômodo.",
            ),
            checklist = listOf("Testei luz, água e gás", "Abri a caixa do primeiro dia", "Comi e descansei"),
            related = listOf("atualizar-endereco", "comprar-antes-depois"),
        ),
        Guide(
            "atualizar-endereco", "mudanca", "Casa nova", "Como atualizar endereço e serviços", "📮", 3, FACIL,
            problem = "Correspondência e compras indo para o endereço antigo podem virar problema. Atualizar tudo logo evita perdas.",
            steps = listOf(
                "Atualize no banco, no trabalho ou faculdade e nos aplicativos de entrega e compras.",
                "Atualize em planos de celular, internet, saúde e assinaturas.",
                "Coloque as contas de luz e água no seu nome, se o contrato prevê isso.",
                "Avise familiares e amigos próximos.",
                "Confira se algum documento ou cadastro oficial precisa de atualização, consultando os canais oficiais.",
            ),
            checklist = listOf("Banco atualizado", "Trabalho ou estudo atualizado", "Apps de entrega atualizados", "Contas no meu nome"),
            related = listOf("primeiro-dia", "organizar-documentos"),
        ),
        Guide(
            "caixas-comodo", "mudanca", "Embalagem", "Como organizar caixas por cômodo", "🏷️", 3, FACIL,
            problem = "Caixa sem etiqueta vira caça ao tesouro na casa nova. Uma regra simples poupa horas.",
            steps = listOf(
                "Escolha uma cor ou um nome para cada cômodo do novo endereço.",
                "Etiquete cada caixa no lado e em cima, com o cômodo e um resumo do conteúdo.",
                "Numere as caixas e anote a lista no celular.",
                "Marque \"FRÁGIL\" e \"ESTE LADO PARA CIMA\" quando precisar.",
                "Na chegada, peça para cada caixa ir direto para o cômodo certo.",
            ),
            checklist = listOf("Etiquetas por cômodo", "Caixas numeradas", "Lista no celular"),
            related = listOf("o-que-embalar", "dia-mudanca"),
        ),
        Guide(
            "comprar-antes-depois", "mudanca", "Compras", "O que comprar antes e o que pode esperar", "🛍️", 3, FACIL,
            problem = "Calma, você não precisa comprar a casa inteira no primeiro dia. Comprar com prioridade evita dívida e arrependimento.",
            steps = listOf(
                "Antes: lugar para dormir, itens de banho, panela, prato, talher, itens de limpeza, lâmpadas.",
                "Primeiras semanas: geladeira e fogão (se não houver), cortina do quarto, máquina de lavar quando der.",
                "Pode esperar: sofá, TV, decoração, eletrodomésticos extras.",
                "Use a lista de móveis do app com as prioridades \"preciso agora\", \"posso comprar depois\" e \"seria legal ter\".",
            ),
            checklist = listOf("Lista de prioridades feita", "Comprei só o essencial primeiro"),
            related = listOf("essencial-casa", "montar-aos-poucos"),
        ),
    )
}
