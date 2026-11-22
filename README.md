# Casa Clara 🏠 — app Android

**Sua casa, seu jeito. Vamos nessa!**

Aplicativo Android nativo que ajuda a conquistar a independência: organizar o dinheiro, preparar a mudança, cuidar da casa e resolver os desafios da vida adulta. A guia do app é a Clarinha, uma casinha mascote.

- **Stack:** Kotlin 2.0.21 · Jetpack Compose (BOM 2024.12.01) · Material 3 · Navigation Compose · Room · DataStore · WorkManager
- **Android:** minSdk 26 (Android 8.0) · targetSdk/compileSdk 35
- **Funciona offline e sem conta.** Nenhum dado é enviado para servidores.

---

## Como compilar e instalar

### Opção A — Android Studio (mais fácil)
1. Instale o **Android Studio** (Ladybug 2024.2 ou mais novo). No Lubuntu: baixe o `.tar.gz` em developer.android.com/studio, extraia e rode `bin/studio.sh`.
2. *File → Open* → escolha a pasta `CasaClara`.
3. Espere o *Gradle Sync* (na primeira vez ele baixa o Gradle 8.11.1, o Android Gradle Plugin e as bibliotecas).
4. Conecte um celular com **Depuração USB** ligada (ou crie um emulador em *Device Manager*) e clique em **Run ▶**.

### Opção B — linha de comando
Requisitos: **JDK 17** e **Android SDK** (plataforma 35 e build-tools).
```bash
# 1. SDK (se ainda não tiver), usando as command-line tools do Android:
sdkmanager "platforms;android-35" "build-tools;35.0.0" "platform-tools"

# 2. Diga ao projeto onde está o SDK:
echo "sdk.dir=$HOME/Android/Sdk" > local.properties

# 3. Gere o APK de teste:
./gradlew assembleDebug
#    -> app/build/outputs/apk/debug/app-debug.apk

# 4. Instale no celular conectado:
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
Também dá para copiar o `app-debug.apk` para o celular e abrir (permita "instalar apps desconhecidos").

**Versão de publicação:** `./gradlew assembleRelease` gera um APK sem assinatura. Para publicar, crie sua chave (*Build → Generate Signed Bundle/APK*) e configure `signingConfigs` em `app/build.gradle.kts`.

### Testes
```bash
./gradlew testDebugUnitTest
```
São 25 testes de unidade, cobrindo dinheiro e datas, calculadoras, finanças, divisão entre moradores, CSV, jornada, conquistas e a consistência da biblioteca de guias.

---

## O que tem no app

**Navegação inferior:** Início · Jornada · Finanças · Casa · Mais.

| Área | O que faz |
|---|---|
| **Primeira abertura** | Introdução ilustrada e perguntas em etapas: situação, objetivo, renda, quanto já guardou, meta de mudança e assuntos. Cada pergunta explica por que é feita e as não essenciais podem ser puladas. Dá para retomar de onde parou e explorar com dados de exemplo. |
| **Início** | Saudação da Clarinha, próxima tarefa recomendada, progresso, resumo do mês, próximas contas e tarefas, metas, dica do dia, ferramentas e conquistas. Tem botão "continuar de onde parei" e se adapta à situação da pessoa. |
| **Minha jornada** | Missões ligadas a ações reais, que se marcam sozinhas quando você usa as ferramentas. Dá para marcar "já fiz", pular, pausar, editar e reiniciar sem apagar registros. Não há sequência punitiva. |
| **Finanças** | Lançamentos (renda fixa, variável e informal; gastos por categoria) com filtros de período, tipo e categoria. Também tem contas recorrentes com "Paguei" e lembrete, dívidas, parcelas e cartões (com calculadora de juros), orçamento por categoria, metas e reserva de emergência (previsão e cobertura em meses), relatórios de 3, 6 ou 12 meses, comparação entre meses e exportação CSV. |
| **Calculadoras** | 11 calculadoras: custo mensal, custo da mudança, renda necessária, quanto economizar por mês, tempo até a meta, divisão entre moradores, sozinho × dividir × juntar, imóveis, móveis, transporte e parcelamento. Os valores ficam salvos e todos os resultados aparecem como estimativas. |
| **Casa** | Tarefas por frequência com "Feito", limpeza por cômodo, calendário (contas, tarefas, mudança e lembretes), compras da casa, inventário e garantias e notas fiscais. |
| **Mudança e aluguel** | Checklist de mudança e plano de preparação, cronograma com lembrete na véspera, documentos, estimativa de custos, checklist de visita, comparador de imóveis (lista e tabela lado a lado), perguntas para o proprietário e guias de contrato e golpes. |
| **Mercado** | Lista por categoria com estimativa a partir dos preços que você informa, favoritos, cardápio da semana, despensa com validade e 10 receitas passo a passo. |
| **Móveis** | Lista do essencial com prioridades ("preciso agora", "posso comprar depois", "seria legal ter"), orçamento, parcelas, garantias e comparação de opções. |
| **Casa compartilhada** | Moradores, despesas divididas (os centavos fecham exatos), saldos e acerto sugerido com o mínimo de transferências, reembolsos, tarefas e acordos. |
| **Biblioteca** | **100 guias** em 11 categorias e subcategorias, com busca, favoritos, histórico, tempo de leitura, nível, passos numerados, exemplo, erros comuns, checklist marcável, alertas de segurança, avisos de fonte oficial, conteúdo relacionado e compartilhamento. A lista completa está em [`docs/GUIAS.md`](docs/GUIAS.md). |
| **Lembretes** | Locais, para contas, tarefas, metas, mudança e manutenção. Dá para criar, editar, apagar, ativar, desativar e repetir (dia, semana ou mês). A permissão de notificação só é pedida quando um lembrete é ativado, com explicação. |
| **Busca global** | Procura em guias, contas, lançamentos, metas, tarefas, checklists, contatos e imóveis. |
| **Conquistas** | 27 conquistas por ações reais, com histórico e comemoração com confete. |
| **Configurações** | Tema claro, escuro ou automático, tamanho do texto (85% a 150%), notificações, cópia de segurança e restauração, CSV, dados de demonstração, reinício da jornada mantendo registros e apagar tudo (com confirmação). |
| **Planos** | Estrutura gratuito/premium. O premium aparece como "em breve", desativado e sem cobrança. |

**Acessibilidade:** descrições para leitor de tela, títulos marcados como cabeçalho, alvos de toque grandes, contraste reforçado, texto ajustável (além da fonte do sistema) e animações desligadas quando o Android pede menos movimento.

---

## Arquitetura

```
app/src/main/java/br/com/casaclara/
├─ domain/            Kotlin puro (sem Android), testado
│  ├─ Money, Dates, Calculators, Finance (+Shared, Csv), Journey (+Achievements), Routes
│  └─ content/        Biblioteca: 11 arquivos de guias, receitas, listas sugeridas, dicas do dia
├─ data/              Room (24 tabelas, DAOs, consulta única de contagens), DataStore (Prefs),
│                     Repository/AppContainer (templates, demo, backup, CSV), DemoData
├─ notifications/     ReminderScheduler + ReminderWorker (WorkManager)
├─ premium/           Estrutura de planos (inativa)
└─ ui/                Tema, mascote (Canvas), componentes reutilizáveis (CrudList, FormSheet…), telas
```
- **CrudList + FormSheet:** um único componente de lista com criar, editar, excluir (com confirmação), validação e estado vazio. É usado em quase todas as ferramentas.
- **Dinheiro em centavos (Long)** e datas em `epochDay`, para não ter erro de arredondamento.
- **Injeção de dependências** manual (`AppContainer`), sem frameworks extras.

## Dados e privacidade
- Os registros ficam no Room (`casaclara.db`) e as preferências no DataStore, no próprio aparelho.
- Não há login nem servidor, e nenhum dado financeiro sai do aparelho.
- **Cópia de segurança:** em Configurações, salva o banco num arquivo escolhido por você. A restauração valida o arquivo antes de substituir os dados.
- O backup automático do Android (conta Google do próprio usuário) está permitido nas regras de backup; dá para desligar em `res/xml`.

## Integrações que dependem de credenciais (preparadas, não ativas)
- **Sincronização e casa compartilhada em tempo real:** precisam de um backend real com autenticação (ex.: Supabase). Não existe login simulado.
- **Assinaturas (Premium):** precisam do Google Play Billing e de validação no servidor. Veja `premium/Plans.kt`. Nenhuma compra é simulada.

## Limitações conhecidas
- **O APK não foi gerado no ambiente de desenvolvimento.** Lá, os repositórios Google Maven e Maven Central e o download do Android SDK estavam bloqueados. A camada `domain` foi compilada e testada com o compilador Kotlin. O restante passou por checagem de sintaxe, mas o primeiro `assembleDebug` na sua máquina ainda pode apontar algum ajuste pequeno de API. Se acontecer, o erro do Gradle diz o arquivo e a linha.
- A cópia de segurança inclui os registros, mas não as preferências (tema, respostas do perfil).
- Os lembretes usam WorkManager: o horário é aproximado (pode atrasar alguns minutos com economia de bateria).
- A casa compartilhada funciona em um aparelho só, até existir backend.
- O conteúdo é educativo e geral. Os guias de aluguel e de dívidas indicam conferir fontes oficiais.

## Novidades da versão 2

- **Início personalizável**: cartões (próximo passo, semana, mês com gráfico, contas, tarefas, cofrinhos, mercado, atalhos, dica, conquistas) que você reordena, esconde e combina com até 8 atalhos favoritos.
- **Dinheiro**: cartões de crédito (fatura por fechamento/vencimento, parcelas, limite usado, "paguei"), lançamentos recorrentes automáticos, cofrinhos com guardar/retirar/transferir e histórico, importação de extrato CSV (detecção de colunas, categoria sugerida, aviso de duplicados).
- **Casa e rotina**: plano da semana que distribui tarefas, faxina cronometrada com notificação, compartilhar lista de mercado/plano pelo WhatsApp, widget de tela inicial.
- **Visual cartunesco**: fontes Baloo 2 + Nunito (OFL), contornos e sombras deslocadas, ilustrações vetoriais próprias.
- **Desempenho e compatibilidade**: Android 8.0+ (API 26), emojis embutidos (sem "quadradinhos" no Android 8), splash screen, listas paginadas, banco v2 com migração automática (não perde dados).
- **Testes**: testes de domínio (JVM) + passeio automatizado em emulador Android 8 e Android 14 no GitHub Actions, incluindo atualização da v1 para a v2.

Baixe o APK: https://github.com/AdryanAndson/CasaClara/releases/download/apk/CasaClara.apk
