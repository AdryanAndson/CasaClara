package br.com.casaclara.domain.content

/** Listas prontas para carregar no Mercado ou na lista da Casa. Sem preços: a pessoa preenche o que encontrar. */
object ListTemplates {
    data class Item(val name: String, val category: String, val qty: Double = 1.0, val unit: String = "un")
    data class ListT(val id: String, val title: String, val emoji: String, val description: String, val target: String, val items: List<Item>)

    val ALL = listOf(
        ListT("compras-mes", "Compras do mês (básico, 1 pessoa)", "🛒", "A base para não faltar comida.", "MERCADO", listOf(
            Item("Arroz", "Grãos e massas", 5.0, "kg"), Item("Feijão", "Grãos e massas", 2.0, "kg"), Item("Macarrão", "Grãos e massas", 2.0, "pct"),
            Item("Óleo", "Grãos e massas", 1.0, "un"), Item("Açúcar", "Grãos e massas", 1.0, "kg"), Item("Café", "Bebidas", 1.0, "pct"),
            Item("Sal", "Temperos", 1.0, "kg"), Item("Alho", "Temperos", 1.0, "un"), Item("Cebola", "Hortifrúti", 1.0, "kg"),
            Item("Ovos", "Carnes e ovos", 2.0, "dz"), Item("Frango (sobrecoxa)", "Carnes e ovos", 2.0, "kg"), Item("Carne moída", "Carnes e ovos", 1.0, "kg"),
            Item("Leite", "Laticínios", 6.0, "L"), Item("Farinha de mandioca", "Grãos e massas", 1.0, "kg"), Item("Molho de tomate", "Grãos e massas", 3.0, "un"),
            Item("Aveia", "Grãos e massas", 1.0, "pct"), Item("Banana", "Hortifrúti", 1.0, "dz"), Item("Batata", "Hortifrúti", 2.0, "kg"),
        )),
        ListT("feira", "Feira da semana", "🥕", "Frutas, verduras e legumes frescos.", "MERCADO", listOf(
            Item("Tomate", "Hortifrúti", 1.0, "kg"), Item("Alface", "Hortifrúti"), Item("Cenoura", "Hortifrúti", 1.0, "kg"),
            Item("Abobrinha", "Hortifrúti", 2.0), Item("Laranja ou fruta da época", "Hortifrúti", 2.0, "kg"), Item("Limão", "Hortifrúti", 6.0),
            Item("Cheiro-verde", "Hortifrúti"), Item("Mamão ou melancia", "Hortifrúti"),
        )),
        ListT("cafe", "Café da manhã da semana", "☕", "Para não sair de casa de estômago vazio.", "MERCADO", listOf(
            Item("Pão", "Padaria", 1.0, "pct"), Item("Manteiga ou margarina", "Laticínios"), Item("Queijo", "Laticínios", 300.0, "g"),
            Item("Café", "Bebidas"), Item("Leite", "Laticínios", 3.0, "L"), Item("Frutas", "Hortifrúti", 1.0, "kg"), Item("Ovos", "Carnes e ovos", 1.0, "dz"),
        )),
        ListT("limpeza", "Produtos de limpeza", "🧴", "O kit para a casa ficar em dia.", "MERCADO", listOf(
            Item("Detergente", "Limpeza", 3.0), Item("Esponja", "Limpeza", 3.0), Item("Sabão em pó ou líquido", "Limpeza"),
            Item("Água sanitária", "Limpeza"), Item("Desinfetante", "Limpeza"), Item("Multiuso", "Limpeza"),
            Item("Saco de lixo", "Limpeza", 1.0, "rolo"), Item("Pano de chão", "Limpeza", 2.0), Item("Flanela", "Limpeza", 2.0),
            Item("Luvas de borracha", "Limpeza", 1.0, "par"), Item("Amaciante", "Limpeza"),
        )),
        ListT("higiene", "Higiene pessoal", "🪥", "O básico do banheiro.", "MERCADO", listOf(
            Item("Papel higiênico", "Higiene", 1.0, "pct"), Item("Sabonete", "Higiene", 4.0), Item("Pasta de dente", "Higiene", 2.0),
            Item("Escova de dente", "Higiene"), Item("Xampu", "Higiene"), Item("Desodorante", "Higiene"), Item("Fio dental", "Higiene"),
        )),
        ListT("primeiro-dia", "Kit do primeiro dia na casa nova", "📦", "O que precisa estar à mão na primeira noite.", "CASA", listOf(
            Item("Papel higiênico", "Higiene"), Item("Sabonete e toalha", "Higiene"), Item("Roupa de cama", "Outros"),
            Item("Carregador de celular", "Outros"), Item("Lanterna", "Outros"), Item("Copo, prato e talher", "Utensílios"),
            Item("Água e lanche", "Utensílios"), Item("Remédios de uso contínuo", "Outros"), Item("Ferramentas básicas (chave de fenda e fita)", "Outros"),
            Item("Saco de lixo", "Outros"),
        )),
        ListT("enxoval-cozinha", "Enxoval básico de cozinha", "🍳", "Para começar a cozinhar sem gastar demais.", "CASA", listOf(
            Item("Panela média", "Utensílios"), Item("Frigideira antiaderente", "Utensílios"), Item("Panela de pressão", "Utensílios"),
            Item("Faca de cozinha", "Utensílios"), Item("Tábua de corte", "Utensílios"), Item("Colher de pau e escumadeira", "Utensílios"),
            Item("Pratos, copos e talheres (kit)", "Utensílios"), Item("Potes com tampa", "Utensílios", 6.0), Item("Escorredor de louça", "Utensílios"),
            Item("Pano de prato", "Utensílios", 3.0), Item("Lixeira", "Utensílios"),
        )),
        ListT("faxina-pesada", "Faxina pesada", "🧹", "Para aquele dia de deixar tudo brilhando.", "MERCADO", listOf(
            Item("Limpa-vidros", "Limpeza"), Item("Desengordurante", "Limpeza"), Item("Escova de cerdas duras", "Limpeza"),
            Item("Rodo e vassoura (se precisar trocar)", "Limpeza"), Item("Balde", "Limpeza"), Item("Álcool 70%", "Limpeza"),
        )),
    )
}

/** Frases da mascote: variam por dia, para o app não falar sempre a mesma coisa. */
object MascotLines {
    val DAILY = listOf(
        "Pequenos passos todo dia constroem uma casa inteira.",
        "Hoje vale o básico bem feito. O resto pode esperar.",
        "Anotar um gasto leva 10 segundos e evita um susto no fim do mês.",
        "Sua casa não precisa ser perfeita. Precisa funcionar para você.",
        "Que tal 15 minutos de arrumação com uma música boa?",
        "Água na garrafa, celular carregado e boleto em dia: dia ganho.",
        "Errou no orçamento? Normal. Ajusta e segue.",
        "Uma refeição feita em casa hoje já é uma vitória.",
        "Guardar um pouco, mesmo que pouco, é o que faz a reserva crescer.",
        "Lembrou de beber água hoje?",
        "Mandou mensagem para alguém querido essa semana?",
        "Confira a data das contas: hoje é um bom dia para isso.",
        "Pedir ajuda também é coisa de gente independente.",
        "Antes de comprar por impulso, espera um dia. Se ainda quiser, compra.",
        "Lixo pra fora e pia vazia: a casa agradece amanhã.",
        "Tá tudo bem descansar. Descanso também é parte da rotina.",
        "Uma conta paga em dia é dinheiro que não vai para os juros.",
        "Cozinhou a mais? Congela em porções e agradece na quarta-feira.",
        "Seu eu do futuro vai agradecer o que você organizar hoje.",
        "Desconfie de pressa em mensagem pedindo dinheiro. Confirme antes.",
        "Abriu a janela hoje? Casa arejada não cria mofo.",
        "Comparar preço por quilo é o truque mais simples do mercado.",
        "Conquista do dia: cama arrumada já conta!",
        "Domingo é um ótimo dia para planejar a semana em 5 minutos.",
    )
    fun of(epochDay: Long): String = DAILY[Math.floorMod(epochDay, DAILY.size.toLong()).toInt()]
}
