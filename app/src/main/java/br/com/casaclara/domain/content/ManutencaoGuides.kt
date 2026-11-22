package br.com.casaclara.domain.content

import br.com.casaclara.domain.content.Difficulty.*

object ManutencaoGuides {
    private val RISCO = danger("Não faça reparos elétricos, de gás ou estruturais por conta própria. Em situação de risco, pare de usar o equipamento e procure um profissional qualificado ou o serviço de emergência.")

    val all = listOf(
        Guide(
            "sinais-vazamento", "manutencao", "Água", "Como identificar sinais de vazamento", "💧", 4, FACIL,
            problem = "Vazamentos escondidos aumentam a conta de água e estragam paredes e móveis. Alguns sinais ajudam a perceber cedo.",
            steps = listOf(
                "Conta de água mais alta sem mudança de uso é um alerta.",
                "Teste do hidrômetro: feche todas as torneiras e não use água; se o relógio continuar girando, pode haver vazamento.",
                "Procure manchas de umidade, bolhas na pintura e piso estufado.",
                "Observe se a descarga fica escorrendo água no vaso.",
                "Avise o proprietário (se for alugado) e chame um encanador para localizar e consertar.",
            ),
            checklist = listOf("Fiz o teste do hidrômetro", "Verifiquei manchas", "Conferi a descarga"),
            related = listOf("umidade", "chamar-profissional"),
        ),
        Guide(
            "umidade", "manutencao", "Água", "Como observar problemas de umidade e mofo", "🌫️", 4, FACIL,
            problem = "Mofo causa cheiro ruim, estraga roupas e pode afetar a saúde. Ele aparece onde há umidade e pouca ventilação.",
            steps = listOf(
                "Abra janelas todos os dias, principalmente depois do banho e de cozinhar.",
                "Afaste móveis alguns centímetros da parede.",
                "Não seque roupas dentro de cômodos fechados.",
                "Limpe pontos pequenos de mofo com produto adequado, usando luvas e ventilação, seguindo o rótulo.",
                "Se o mofo volta sempre ou há infiltração, a causa precisa ser investigada por um profissional e comunicada ao proprietário.",
            ),
            checklist = listOf("Ventilo a casa todos os dias", "Afastei móveis da parede", "Comuniquei infiltrações"),
            related = listOf("cuidar-banheiro", "sinais-vazamento"),
            alert = danger("Não misture produtos para limpar mofo. Use luvas e mantenha o ambiente ventilado."),
        ),
        Guide(
            "eletrodomesticos", "manutencao", "Equipamentos", "Como cuidar de eletrodomésticos", "🔌", 4, FACIL,
            problem = "Eletrodomésticos bem cuidados duram mais e gastam menos energia.",
            steps = listOf(
                "Geladeira: não encoste na parede (respeite a distância do manual), limpe a borracha e não coloque comida quente dentro.",
                "Máquina de lavar: limpe o filtro e não ultrapasse a carga indicada.",
                "Micro-ondas: limpe por dentro e nunca use metal.",
                "Leia o manual e guarde em local fácil (ou baixe o PDF do fabricante).",
                "Ligue cada aparelho em tomada adequada e evite benjamins.",
            ),
            checklist = listOf("Limpei a borracha da geladeira", "Limpei o filtro da máquina", "Guardei os manuais"),
            related = listOf("manutencao-preventiva", "eletricidade"),
            alert = RISCO,
        ),
        Guide(
            "manutencao-preventiva", "manutencao", "Planejamento", "Como organizar manutenção preventiva", "📆", 4, FACIL,
            problem = "Prevenir é mais barato do que consertar. Um calendário simples de cuidados evita surpresas.",
            steps = listOf(
                "Mensal: filtro da máquina, ralos, borracha da geladeira.",
                "Trimestral: limpar caixa de gordura ou sifões, checar torneiras e descargas.",
                "Semestral: limpar ar-condicionado (filtros) e conferir mangueira e registro do gás (validade impressa).",
                "Anual: revisar a pintura em áreas úmidas e conversar com o proprietário sobre manutenções maiores.",
                "Cadastre lembretes de manutenção no app.",
            ),
            checklist = listOf("Criei os lembretes mensais", "Anotei a validade da mangueira de gás"),
            related = listOf("eletrodomesticos", "gas"),
        ),
        Guide(
            "chamar-profissional", "manutencao", "Ajuda", "Quando chamar um profissional", "👷", 3, FACIL,
            problem = "Saber parar também é ser adulto. Alguns problemas exigem conhecimento técnico e oferecem risco.",
            steps = listOf(
                "Chame sem pensar duas vezes: qualquer coisa com gás, fios, tomada esquentando, disjuntor desarmando sempre, troca de resistência do chuveiro.",
                "Também: vazamento que não para ao fechar o registro, rachaduras, mofo que sempre volta.",
                "Peça indicação e mais de um orçamento.",
                "Combine preço, prazo e garantia do serviço por escrito.",
                "Se for inquilino, avise o proprietário antes: alguns reparos são responsabilidade dele.",
            ),
            checklist = listOf("Tenho contatos de confiança salvos", "Peço orçamento por escrito"),
            related = listOf("contatos-profissionais", "registrar-defeitos"),
            alert = RISCO,
        ),
        Guide(
            "registrar-defeitos", "manutencao", "Registros", "Como registrar defeitos e reparos", "🗒️", 3, FACIL,
            problem = "Sem registro, você esquece quando consertou, quanto pagou e quem fez o serviço.",
            steps = listOf(
                "Ao notar um defeito, fotografe e anote a data.",
                "Guarde o orçamento, o recibo e a garantia do serviço.",
                "Anote quem fez o serviço e o contato.",
                "Se for alugado, comunique o proprietário por escrito.",
                "Use as garantias e contatos do app para centralizar tudo.",
            ),
            checklist = listOf("Fotografei e anotei a data", "Guardei o recibo", "Salvei o contato"),
            related = listOf("documentar-problemas", "acompanhar-garantias"),
        ),
        Guide(
            "contatos-profissionais", "manutencao", "Registros", "Como organizar contatos de profissionais", "📇", 2, FACIL,
            problem = "Na emergência, não é hora de procurar encanador. Ter a lista pronta poupa tempo e dinheiro.",
            steps = listOf(
                "Salve no app: eletricista, encanador, chaveiro, técnico de eletrodomésticos, síndico ou portaria, proprietário ou imobiliária.",
                "Anote quem indicou e o que achou do serviço.",
                "Inclua os contatos de emergência: 190, 192, 193 e a distribuidora de gás e energia da sua região.",
            ),
            checklist = listOf("Salvei profissionais", "Salvei números de emergência"),
            related = listOf("contatos-emergencia", "chamar-profissional"),
        ),
        Guide(
            "acompanhar-garantias", "manutencao", "Registros", "Como acompanhar garantias e notas fiscais", "🧾", 2, FACIL,
            problem = "Garantia que vence sem você perceber é dinheiro perdido se o produto der defeito logo depois.",
            steps = listOf(
                "Cadastre cada garantia com a data de compra e o prazo.",
                "O app avisa quando faltar pouco para vencer.",
                "Antes de vencer, teste o produto e acione a assistência se algo estiver errado.",
            ),
            checklist = listOf("Cadastrei minhas garantias"),
            related = listOf("garantias-notas"),
        ),
    )
}
