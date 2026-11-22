package br.com.casaclara.domain.content

import br.com.casaclara.domain.content.Difficulty.*

object MoveisGuides {
    val all = listOf(
        Guide(
            "essencial-casa", "moveis", "Prioridades", "O que é essencial para começar uma casa", "🛏️", 3, FACIL,
            problem = "A lista de \"coisas de casa\" parece infinita. Mas o essencial para viver bem nas primeiras semanas é bem menor.",
            steps = listOf(
                "Dormir: cama ou colchão, travesseiro, roupa de cama.",
                "Higiene: toalhas, papel higiênico, itens de banho.",
                "Comer: geladeira, fogão ou cooktop, uma panela, uma frigideira, pratos, copos e talheres.",
                "Limpar: vassoura, rodo, balde, panos, produtos básicos.",
                "Luz e segurança: lâmpadas e cortina no quarto.",
                "Use a lista do essencial do app e marque o que já tem.",
            ),
            checklist = listOf("Tenho onde dormir", "Tenho itens de banho", "Consigo cozinhar o básico", "Tenho kit de limpeza"),
            related = listOf("comprar-primeiro", "montar-aos-poucos"),
        ),
        Guide(
            "comprar-usado", "moveis", "Economia", "O que pode ser comprado usado", "♻️", 4, FACIL,
            problem = "Comprar usado economiza bastante, mas alguns itens exigem mais cuidado do que outros.",
            steps = listOf(
                "Ótimos usados: mesas, cadeiras, estantes, armários, sofás em bom estado, utensílios de cozinha.",
                "Com atenção: eletrodomésticos — teste funcionando, pergunte a idade e se há nota.",
                "Prefira novo: colchão e travesseiro (por higiene), capacetes e itens de segurança.",
                "Combine retirada em local seguro e, se possível, vá acompanhado.",
                "Desconfie de preço bom demais e de quem exige pagamento antecipado sem você ver o produto.",
            ),
            mistakes = listOf("Comprar eletrodoméstico usado sem testar.", "Pagar antes de ver o produto."),
            checklist = listOf("Testei antes de pagar", "Conferi o estado", "Combinei retirada segura"),
            related = listOf("comparar-produtos", "golpes-servicos"),
        ),
        Guide(
            "comprar-primeiro", "moveis", "Prioridades", "O que vale a pena comprar primeiro", "🥇", 3, FACIL,
            problem = "Com dinheiro limitado, a ordem das compras faz diferença no conforto do dia a dia.",
            steps = listOf(
                "Primeiro, o que você usa todo dia e não tem como improvisar: cama, geladeira, fogão.",
                "Depois, o que economiza tempo ou dinheiro: máquina de lavar, micro-ondas.",
                "Por último, conforto e estética: sofá, TV, decoração.",
                "Classifique cada item no app como \"preciso agora\", \"posso comprar depois\" ou \"seria legal ter\".",
            ),
            checklist = listOf("Classifiquei os itens", "Defini o orçamento", "Comprei os de \"preciso agora\""),
            related = listOf("essencial-casa", "evitar-impulso"),
        ),
        Guide(
            "medir-espacos", "moveis", "Antes de comprar", "Como medir espaços antes de comprar móveis", "📏", 3, FACIL,
            problem = "Sofá que não passa na porta, geladeira que não cabe no vão... medir antes evita troca, frete extra e frustração.",
            steps = listOf(
                "Meça largura, altura e profundidade do espaço onde o móvel vai ficar.",
                "Meça também portas, corredores, escadas e elevador por onde ele vai passar.",
                "Deixe folga para abrir portas e gavetas e, em geladeiras, para ventilação (veja o manual).",
                "Anote as medidas no celular e leve para a loja.",
                "Confira as medidas do produto na ficha técnica, não só na foto.",
            ),
            checklist = listOf("Medi o espaço", "Medi portas e corredores", "Anotei no celular"),
            related = listOf("comparar-produtos"),
        ),
        Guide(
            "comparar-produtos", "moveis", "Antes de comprar", "Como comparar produtos", "⚖️", 3, FACIL,
            problem = "O menor preço da vitrine nem sempre é a melhor compra. Frete, garantia e consumo de energia mudam a conta.",
            steps = listOf(
                "Compare o preço final: produto + frete + montagem.",
                "Veja a garantia oferecida e a reputação da loja.",
                "Em eletrodomésticos, compare o consumo de energia (selo do Procel/Inmetro).",
                "Leia avaliações de outros compradores.",
                "Cadastre as opções na aba Comparar do app para ver lado a lado.",
            ),
            checklist = listOf("Comparei o preço final", "Conferi garantia", "Conferi consumo de energia"),
            related = listOf("medir-espacos", "parcelar-moveis"),
        ),
        Guide(
            "parcelar-moveis", "moveis", "Pagamento", "Como planejar compras parceladas de móveis", "💳", 3, MEDIO,
            problem = "Mobiliar a casa toda parcelado pode comprometer a renda por muito tempo.",
            steps = listOf(
                "Some todas as parcelas que você já tem antes de assumir novas.",
                "Prefira parcelamento sem juros e confira se o preço parcelado é igual ao à vista.",
                "Evite parcelas maiores que o tempo de vida do produto.",
                "Cadastre cada compra parcelada no app para acompanhar.",
            ),
            checklist = listOf("Somei minhas parcelas", "Comparei à vista e parcelado", "Cadastrei a compra"),
            related = listOf("compras-parceladas", "juros-divida"),
        ),
        Guide(
            "garantias-notas", "moveis", "Depois da compra", "Como organizar garantias e notas fiscais", "🧾", 3, FACIL,
            problem = "Quando o produto quebra, a nota fiscal e o prazo de garantia são o que garante o conserto ou a troca.",
            steps = listOf(
                "Guarde as notas em uma pasta física ou digital (foto ou PDF).",
                "Anote no app o produto, a data da compra e o prazo de garantia.",
                "Saiba que existe a garantia legal (pelo Código de Defesa do Consumidor, 90 dias para produtos duráveis) e a garantia do fabricante, que costuma ser maior.",
                "Antes de a garantia vencer, teste o produto e acione a assistência se houver problema.",
            ),
            checklist = listOf("Notas guardadas", "Garantias cadastradas no app"),
            related = listOf("acompanhar-garantias", "comparar-produtos"),
            alert = info("Prazos legais podem mudar: confira no Código de Defesa do Consumidor e no Procon da sua cidade."),
            legal = true,
        ),
        Guide(
            "evitar-impulso", "moveis", "Hábitos", "Como evitar comprar por impulso", "🛑", 3, FACIL,
            problem = "Promoção relâmpago, frete grátis só hoje... o comércio é feito para você decidir rápido. Uma pausa protege o seu dinheiro.",
            steps = listOf(
                "Regra das 48 horas: deixe o item no carrinho e decida depois.",
                "Pergunte: eu já tinha planejado isso? Cabe no orçamento deste mês?",
                "Desative notificações de promoções de lojas.",
                "Mantenha uma lista de desejos no app e compre só o que está nela.",
            ),
            checklist = listOf("Uso a regra das 48 horas", "Tenho lista de desejos"),
            related = listOf("montar-aos-poucos", "compras-parceladas"),
        ),
        Guide(
            "montar-aos-poucos", "moveis", "Prioridades", "Como montar uma casa aos poucos", "🧩", 3, FACIL,
            problem = "Casa montada leva tempo — e tudo bem. Ir aos poucos permite comprar melhor e descobrir o que você realmente usa.",
            steps = listOf(
                "Viva algumas semanas com o essencial e anote o que realmente faz falta.",
                "Defina um valor mensal para a casa e siga a ordem de prioridades.",
                "Aproveite doações, usados e trocas com amigos e família.",
                "Comemore cada item conquistado — sem comparar com casas de revista.",
            ),
            checklist = listOf("Defini um valor mensal", "Anotei o que faz falta"),
            related = listOf("essencial-casa", "comprar-usado"),
        ),
    )
}
