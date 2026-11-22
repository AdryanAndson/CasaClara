package br.com.casaclara.domain.content

import br.com.casaclara.domain.content.Difficulty.*

object CompartilhadaGuides {
    val all = listOf(
        Guide(
            "dividir-contas", "compartilhada", "Dinheiro", "Como dividir contas", "➗", 4, FACIL,
            problem = "Dividir em partes iguais nem sempre parece justo: um quarto pode ser maior, alguém pode passar mais tempo em casa.",
            steps = listOf(
                "Listem as despesas comuns: aluguel, condomínio, luz, água, gás, internet, limpeza.",
                "Escolham o critério: partes iguais, por tamanho do quarto ou proporcional à renda.",
                "Usem a calculadora de divisão do app para testar os critérios.",
                "Registrem quem pagou cada conta no app.",
                "Acertem a diferença sempre na mesma data do mês.",
            ),
            example = "Exemplo ilustrativo: aluguel de R$ 1.800 dividido por tamanho do quarto (pesos 1,2 e 1,0) fica cerca de R$ 982 e R$ 818.",
            checklist = listOf("Listamos as despesas", "Escolhemos o critério", "Definimos a data de acerto"),
            related = listOf("dinheiro-dividindo", "atrasos-pagamento"),
        ),
        Guide(
            "combinar-tarefas", "compartilhada", "Tarefas", "Como combinar tarefas domésticas", "🧹", 3, FACIL,
            problem = "Tarefa sem dono não é feita. Combinar quem faz o quê evita acúmulo e mágoa.",
            steps = listOf(
                "Listem as tarefas comuns e a frequência de cada uma.",
                "Dividam considerando a rotina de cada pessoa.",
                "Alternem as tarefas que ninguém gosta, semana a semana.",
                "Registrem no app e marquem quando fizerem.",
                "Revisem a divisão quando alguém mudar de rotina.",
            ),
            checklist = listOf("Lista de tarefas pronta", "Responsáveis definidos", "Rodízio combinado"),
            related = listOf("dividir-tarefas", "evitar-conflitos"),
        ),
        Guide(
            "regras-casa", "compartilhada", "Convivência", "Como conversar sobre regras da casa", "🗣️", 4, FACIL,
            problem = "Cada pessoa vem de uma casa com costumes diferentes. Conversar antes evita que pequenas diferenças virem grandes brigas.",
            steps = listOf(
                "Marquem uma conversa no começo da convivência (ou agora, se ainda não fizeram).",
                "Falem sobre visitas, barulho, uso de áreas comuns, comida compartilhada e limpeza.",
                "Cada pessoa diz o que é importante para ela, sem julgamento.",
                "Cheguem a acordos simples e possíveis de cumprir.",
                "Registrem os acordos no app e combinem quando revisar.",
            ),
            checklist = listOf("Conversamos sobre as regras", "Registramos os acordos"),
            related = listOf("registrar-acordos", "evitar-conflitos"),
        ),
        Guide(
            "compras-compartilhadas", "compartilhada", "Dinheiro", "Como organizar compras compartilhadas", "🛒", 3, FACIL,
            problem = "Quem comprou o papel higiênico? Quem acabou com o detergente? Itens comuns precisam de combinado.",
            steps = listOf(
                "Definam o que é de uso comum (limpeza, papel, temperos básicos) e o que é individual.",
                "Mantenham uma lista compartilhada de itens que estão acabando.",
                "Quem comprar registra a despesa no app, dividida entre todos.",
                "Acertem os valores na data combinada.",
            ),
            checklist = listOf("Definimos itens comuns", "Registramos as compras no app"),
            related = listOf("dividir-contas"),
        ),
        Guide(
            "atrasos-pagamento", "compartilhada", "Dinheiro", "Como lidar com atrasos de pagamento", "⏳", 4, MEDIO,
            problem = "Quando alguém atrasa a parte das contas, a pressão cai sobre quem paga. Conversar cedo, com respeito, é o melhor caminho.",
            steps = listOf(
                "Converse em particular, sem expor a pessoa no grupo.",
                "Mostre os números registrados no app, de forma objetiva.",
                "Pergunte se está tudo bem e se há uma dificuldade — às vezes, há.",
                "Combinem um prazo ou um parcelamento possível e registrem.",
                "Se os atrasos se repetirem, revejam o acordo de convivência.",
            ),
            mistakes = listOf("Cobrar em público.", "Deixar acumular por meses antes de falar."),
            checklist = listOf("Conversei em particular", "Combinamos um prazo", "Registramos o combinado"),
            related = listOf("dividir-contas", "evitar-conflitos"),
        ),
        Guide(
            "evitar-conflitos", "compartilhada", "Convivência", "Como evitar conflitos por limpeza e barulho", "🔇", 4, FACIL,
            problem = "Limpeza e barulho são os motivos mais comuns de briga entre moradores.",
            steps = listOf(
                "Combinem um horário de silêncio e o que fazer em exceções (avisar antes).",
                "Usem fones de ouvido em horários combinados.",
                "Sigam a escala de limpeza e combinem um padrão mínimo para áreas comuns.",
                "Falem do incômodo logo, de forma específica: \"a louça ficou na pia desde ontem\" em vez de \"você nunca limpa nada\".",
                "Reconheçam quando o outro cumpre o combinado.",
            ),
            checklist = listOf("Horário de silêncio combinado", "Padrão de limpeza definido"),
            related = listOf("regras-casa", "combinar-tarefas"),
        ),
        Guide(
            "registrar-acordos", "compartilhada", "Convivência", "Como registrar acordos de convivência", "🤝", 3, FACIL,
            problem = "Combinado de boca se perde e vira \"eu não disse isso\". Escrever deixa todos alinhados.",
            steps = listOf(
                "Escreva cada acordo de forma curta e clara.",
                "Inclua contas, tarefas, visitas, silêncio e itens compartilhados.",
                "Todos leem e concordam.",
                "Guarde na aba Acordos do app e revisem quando algo mudar.",
            ),
            checklist = listOf("Acordos escritos", "Todos concordaram", "Data de revisão marcada"),
            related = listOf("regras-casa"),
        ),
    )
}
