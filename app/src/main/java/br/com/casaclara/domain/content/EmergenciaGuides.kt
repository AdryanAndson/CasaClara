package br.com.casaclara.domain.content

import br.com.casaclara.domain.content.Difficulty.*

object EmergenciaGuides {
    private val NUMEROS = danger("Risco à vida ou à segurança: SAMU 192 · Bombeiros 193 · Polícia 190 · Defesa Civil 199. Sua segurança vem antes do patrimônio.")

    val all = listOf(
        Guide(
            "cartao-emergencia-guia", "emergencias", "Preparo", "Monte seu cartão de emergência", "🆘", 4, FACIL,
            problem = "Se algo acontecer com você, quem ajudar precisa saber para quem ligar e informações básicas de saúde. Morando sozinho, isso é ainda mais importante.",
            steps = listOf(
                "Preencha o Cartão de emergência no app (Mais > Emergência) com um ou dois contatos de confiança.",
                "Anote alergias, remédios de uso contínuo e plano de saúde, se tiver.",
                "Configure as informações de emergência no próprio celular: elas aparecem na tela de bloqueio.",
                "Deixe uma cópia em papel na carteira.",
                "Combine com a pessoa de contato que ela é sua referência.",
            ),
            checklist = listOf("Preenchi o cartão no app", "Configurei a emergência do celular", "Avisei meu contato"),
            related = listOf("contatos-emergencia", "doente-sozinho"),
            alert = NUMEROS,
        ),
        Guide(
            "cano-estourado", "emergencias", "Água", "Cano estourou ou vazamento forte: o que fazer agora", "💦", 3, FACIL,
            problem = "Água jorrando estraga piso, móveis e o apartamento de baixo em minutos. Saber onde fica o registro é o que mais importa.",
            steps = listOf(
                "Feche o registro geral de água da casa ou do apartamento imediatamente.",
                "Se a água estiver perto de tomadas ou aparelhos, desligue o disjuntor geral antes de pisar na área molhada.",
                "Tire móveis e eletrônicos do caminho da água e seque o que der.",
                "Avise o proprietário ou a imobiliária e, em prédio, o síndico e o vizinho de baixo.",
                "Fotografe tudo para registrar o problema.",
                "Chame um encanador para o conserto.",
            ),
            mistakes = listOf("Não saber onde fica o registro geral.", "Pisar na água com aparelhos ligados na tomada."),
            checklist = listOf("Sei onde fica o registro geral", "Sei onde fica o disjuntor geral"),
            related = listOf("sinais-vazamento", "chamar-profissional", "documentar-problemas"),
            alert = NUMEROS,
        ),
        Guide(
            "cheiro-queimado", "emergencias", "Eletricidade", "Cheiro de queimado, faísca ou tomada esquentando", "⚡", 3, FACIL,
            problem = "Cheiro de plástico queimado, faísca ou tomada quente são sinais de problema elétrico que pode virar incêndio.",
            steps = listOf(
                "Desligue o disjuntor do circuito ou o geral se não souber qual é.",
                "Tire o aparelho da tomada somente se for seguro e sem tocar em partes queimadas.",
                "Nunca jogue água em fogo de origem elétrica.",
                "Se houver fogo que você não consegue controlar com segurança, saia e ligue 193.",
                "Antes de religar, chame um eletricista para verificar.",
            ),
            checklist = listOf("Sei onde fica o quadro de disjuntores"),
            related = listOf("eletricidade", "incendio"),
            alert = NUMEROS,
        ),
        Guide(
            "trancado-fora", "emergencias", "Chaves", "Fiquei trancado(a) do lado de fora", "🔐", 3, FACIL,
            problem = "Perder ou esquecer a chave acontece com todo mundo. Um pouco de preparo evita pagar caro num chaveiro de madrugada.",
            steps = listOf(
                "Deixe uma cópia da chave com alguém de confiança que more perto.",
                "Salve o contato de um chaveiro com boas referências antes de precisar.",
                "Peça o orçamento antes de o chaveiro começar o serviço.",
                "Em apartamento alugado, avise a imobiliária se precisar trocar a fechadura.",
                "Se perdeu a chave junto com documentos com endereço, troque o miolo da fechadura.",
            ),
            checklist = listOf("Deixei uma cópia com alguém", "Salvei um chaveiro de confiança"),
            related = listOf("contatos-profissionais", "perdi-documentos"),
        ),
        Guide(
            "chuva-forte", "emergencias", "Clima", "Chuva forte, alagamento e deslizamento", "⛈️", 4, MEDIO,
            problem = "Temporais podem alagar ruas e casas e, em áreas de encosta, causar deslizamentos. Saber os sinais e ter um plano salva vidas.",
            steps = listOf(
                "Cadastre seu CEP para receber alertas da Defesa Civil por SMS no número 40199.",
                "Em alerta de chuva forte, evite atravessar ruas alagadas a pé ou de carro.",
                "Rachaduras novas, portas emperrando e barulhos no morro podem indicar risco de deslizamento: saia e ligue 199.",
                "Desligue a energia se a água entrar em casa.",
                "Tenha uma mochila com documentos, remédios, lanterna e carregador.",
            ),
            checklist = listOf("Cadastrei meu CEP no 40199", "Montei a mochila de emergência"),
            related = listOf("cheiro-queimado", "falta-energia-agua"),
            alert = NUMEROS,
        ),
        Guide(
            "queimadura-leve", "emergencias", "Primeiros cuidados", "Queimadura leve na cozinha", "🔥", 3, FACIL,
            problem = "Encostar na panela ou espirrar óleo quente é comum na cozinha. O primeiro cuidado certo diminui a dor e evita piorar.",
            steps = listOf(
                "Coloque a área sob água corrente fria (não gelada) por cerca de 20 minutos.",
                "Retire anéis ou pulseiras perto da área antes de inchar.",
                "Não estoure bolhas e não passe manteiga, pasta de dente, pó de café ou óleo.",
                "Cubra com pano limpo ou gaze sem apertar.",
                "Queimadura grande, no rosto, nas mãos inteiras, nas partes íntimas ou com pele branca ou escura: procure atendimento ou ligue 192.",
            ),
            mistakes = listOf("Usar receitas caseiras na queimadura.", "Colocar gelo direto na pele."),
            checklist = listOf("Tenho gaze no kit de primeiros socorros"),
            related = listOf("remedios-casa", "quando-upa"),
            alert = NUMEROS,
        ),
        Guide(
            "esgoto-voltando", "emergencias", "Água", "Ralo ou vaso voltando esgoto", "🚽", 4, MEDIO,
            problem = "Esgoto voltando pelo ralo ou vaso é desagradável e pode trazer doenças. Às vezes é entupimento seu, às vezes é da rede.",
            steps = listOf(
                "Pare de usar água na casa para não piorar.",
                "Use luvas e calçado fechado para limpar a área.",
                "Se só um ralo entupiu, tente o desentupidor de borracha.",
                "Se vários ralos estão voltando ao mesmo tempo, o problema pode ser da coluna do prédio ou da rede: avise o síndico ou a companhia de saneamento.",
                "Limpe e desinfete o piso com água sanitária diluída, sem misturar com outros produtos.",
            ),
            mistakes = listOf("Jogar soda cáustica sem proteção.", "Misturar produtos de limpeza para desinfetar."),
            checklist = listOf("Tenho desentupidor e luvas em casa"),
            related = listOf("cuidar-banheiro", "chamar-profissional"),
        ),
        Guide(
            "animais-peconhentos", "emergencias", "Primeiros cuidados", "Escorpião, aranha ou cobra em casa", "🦂", 4, MEDIO,
            problem = "Escorpiões aparecem em muitas cidades, principalmente perto de ralos e entulho. Saber prevenir e o que fazer numa picada é importante.",
            steps = listOf(
                "Use telas ou tampas nos ralos e vede frestas em portas e rodapés.",
                "Não acumule entulho, caixas e lixo perto de casa.",
                "Sacuda roupas, toalhas e sapatos antes de usar.",
                "Em caso de picada, lave com água e sabão e procure atendimento imediato ou ligue 192.",
                "Não corte, não aperte e não faça torniquete no local.",
                "Se possível e seguro, fotografe o animal para mostrar à equipe de saúde.",
            ),
            checklist = listOf("Coloquei tampa nos ralos"),
            related = listOf("quando-upa", "rotina-limpeza"),
            alert = NUMEROS,
        ),
        Guide(
            "invasao-casa", "emergencias", "Segurança", "Cheguei e a porta estava arrombada", "🚪", 3, FACIL,
            problem = "Chegar em casa e perceber sinais de arrombamento assusta. O mais importante é não entrar e não enfrentar ninguém.",
            steps = listOf(
                "Não entre: a pessoa pode ainda estar lá dentro.",
                "Afaste-se e ligue 190 de um local seguro.",
                "Espere a polícia antes de mexer em qualquer coisa.",
                "Depois, faça o boletim de ocorrência e liste o que foi levado.",
                "Troque a fechadura e avise o proprietário ou a imobiliária.",
            ),
            checklist = listOf("Sei para quem ligar numa emergência"),
            related = listOf("trancado-fora", "perdi-documentos"),
            alert = NUMEROS,
        ),
    )
}
