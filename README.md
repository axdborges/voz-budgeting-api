# voz-budgeting-api

API de orçamento que usa IA para processar comandos de voz relacionados a transações financeiras. O cliente envia um áudio, a aplicação transcreve o comando e interpreta a intenção com um LLM, que classifica o comando em uma de três categorias — **registrar transação**, **consultar transações** ou **intenção não identificada** (quando o áudio não é um comando financeiro claro) — e **executa a ação de verdade via Tool Calling** (persiste ou consulta as transações), devolvendo uma resposta em texto e, futuramente, em áudio.

Desafio de Projeto da [DIO](https://dio.me), baseado no repositório [dio-spring-boot-learning-track](https://github.com/axdborges/dio-spring-boot-learning-track/tree/main)/05-spring-ai.

> 🚧 Projeto em andamento. O andamento das tarefas está em `TODO.md`; este README é atualizado conforme o projeto avança e será fechado na tarefa final.

## Tecnologias

- **Java 21** (LTS)
- **Spring Boot 4.0.5**
- **Spring AI 2.0.0**, com [OpenAI](https://platform.openai.com/) como provedor:
  - Chat: `gpt-4o-mini`
  - Transcrição de áudio (speech-to-text): `whisper-1`
- **Spring Data JPA** + **MySQL 8.4** (persistência real das transações; testes automatizados usam H2 em memória)
- **Gradle 9.4.1** (via wrapper, `./gradlew`)
- **Docker** e **Docker Compose**

## Como rodar

Pré-requisito: Docker Desktop instalado e rodando.

1. Copie o arquivo de exemplo de variáveis de ambiente:
   ```bash
   cp .env.example .env
   ```
2. Abra o `.env` e preencha sua chave da OpenAI e as credenciais do banco (veja [Variáveis de ambiente](#variáveis-de-ambiente)).
3. Suba a aplicação (sobe também o MySQL, num serviço `db` separado):
   ```bash
   docker compose up -d --build
   ```
4. A API fica disponível em `http://localhost:8080`.

Para parar: `docker compose down` (os dados do banco continuam guardados no volume `db-data`; para apagá-los também, use `docker compose down -v`).

## Variáveis de ambiente

As variáveis ficam no arquivo `.env` (na raiz do projeto, **não é versionado** — cada pessoa cria o seu a partir de `.env.example`). O `compose.yml` injeta esse arquivo automaticamente nos containers (`env_file: .env`).

| Variável | Obrigatória | Descrição |
|---|---|---|
| `OPENAI_API_KEY` | Sim | Chave de API da OpenAI ([platform.openai.com](https://platform.openai.com/api-keys)). Usada tanto para o modelo de chat (`gpt-4o-mini`) quanto para a transcrição de áudio (`whisper-1`). |
| `DB_NAME` | Sim | Nome do banco MySQL (ex.: `voz_budgeting`). |
| `DB_USERNAME` | Sim | Usuário da aplicação no MySQL. |
| `DB_PASSWORD` | Sim | Senha desse usuário. |
| `DB_ROOT_PASSWORD` | Sim | Senha do usuário `root` do container MySQL (usada só na inicialização/healthcheck do container `db`). |

**Nunca** commite o `.env` nem cole sua chave/senhas em nenhum lugar público — ele já está no `.gitignore`.

## Rotas disponíveis

### `POST /voice-commands`

Endpoint principal: recebe um arquivo de áudio, transcreve e envia o texto pro `ChatClient`, que classifica o comando em uma de três categorias e **executa a ação de verdade** através de Tool Calling:

1. **Registrar transação** — comando com ação financeira clara, seja gasto (ex.: "gastei", "paguei", "comprei") ou entrada/depósito (ex.: "coloquei", "depositei", "recebi") e um valor. Aciona a tool `registrarTransacao`, que persiste a transação. Toda transação tem obrigatoriamente **valor**, **categoria** e **data**:
   - Se o comando mencionar uma data (absoluta ou relativa, ex.: "ontem", "segunda passada"), o modelo resolve para a data real antes de registrar.
   - Se nenhuma data for mencionada, é usada a data de **hoje** automaticamente.
   - A descrição (ex.: nome do estabelecimento) é opcional — só é preenchida quando o comando traz essa informação.
2. **Consultar transações** — pergunta sobre gastos/saldo já existentes. Duas variações:
   - Categoria específica (ex.: "quanto eu gastei em mercado?") → aciona `consultarTransacoesPorCategoria`, que devolve a soma real das transações já registradas naquela categoria.
   - Visão geral, todas as categorias (ex.: "quais foram todas as minhas transações?", "resumo de tudo que eu gastei") → aciona `consultarTodasAsTransacoes`, que devolve a lista de transações **agrupadas por categoria**, com a quantidade e o total gasto em cada uma.
3. **Intenção não identificada** — o áudio não é um comando financeiro claro (ambíguo, incompleto, assunto não relacionado a finanças). Nesse caso o modelo **não chama nenhuma tool** — ele avisa que não entendeu e pede pra repetir.

Categorias usadas internamente: `MERCADO`, `TRANSPORTE`, `LAZER`, `SAUDE`, `MORADIA`, `EDUCACAO`, `OUTROS` (usada quando a categoria não é mencionada no comando).

- **Content-Type**: `multipart/form-data`
- **Campo**: `audio` (arquivo de áudio, ex.: mp3, wav, m4a)

```bash
curl -F "audio=@caminho/para/comando.mp3" http://localhost:8080/voice-commands
```

**Resposta** (`200 OK`) — exemplo registrando uma transação (sem data explícita, usa hoje):
```json
{
  "transcription": "Eu quero colocar R$50,00 na minha conta hoje",
  "reply": "Transação registrada com sucesso: R$ 50,00 em OUTROS no dia 2026-07-20, id 3f2a1b9c-....."
}
```

**Resposta** (`200 OK`) — exemplo com data relativa ("ontem"):
```json
{
  "transcription": "Comprei um hambúrguer ontem por R$33,25",
  "reply": "Transação registrada com sucesso: R$ 33,25 em OUTROS (hambúrguer) no dia 2026-07-19, id 9c8b7a6d-....."
}
```

**Resposta** (`200 OK`) — exemplo consultando transações de uma categoria específica:
```json
{
  "transcription": "Quanto eu tenho na minha conta?",
  "reply": "Você tem 1 transação(ões) na categoria OUTROS, totalizando R$ 50,00."
}
```

**Resposta** (`200 OK`) — exemplo consultando TODAS as transações, agrupadas por categoria:
```json
{
  "transcription": "Quais foram todas as minhas transações feitas de todas as categorias?",
  "reply": "Resumo de todas as suas transações por categoria:\n- EDUCACAO: 1 transação(ões), totalizando R$ 0,55\n- MERCADO: 1 transação(ões), totalizando R$ 669,29\n- TRANSPORTE: 1 transação(ões), totalizando R$ 12,50"
}
```

> Status atual: transcrição, interpretação e execução real (persistência/consulta) já funcionam de ponta a ponta. A persistência é **real, em MySQL** (`JpaTransactionRepository` + `TransactionEntity`) — os dados sobrevivem a reinícios da aplicação.

### `GET /voice-commands/mock`

Rota de conveniência **apenas para testes manuais em desenvolvimento** — não faz parte do desafio e será removida antes de qualquer publicação/deploy real. Roda o mesmo fluxo (transcrição + interpretação) sobre um arquivo de áudio já salvo localmente em `audios-java/` (pasta fora do controle de versão), sem precisar montar uma requisição multipart.

- **Parâmetro (query, opcional)**: `file` — nome do arquivo dentro de `audios-java/` (padrão: `mercado.mp3`)

```bash
curl "http://localhost:8080/voice-commands/mock?file=adicionar-saldo.mp3"
```

**Resposta** (`200 OK`):
```json
{
  "transcription": "Olá, meu nome é Alexandre e eu quero colocar R$50,00 na minha conta hoje. Preciso colocar R$50,00.",
  "reply": "Transação registrada com sucesso: R$ 50,00 em OUTROS no dia 2026-07-20, id 3f2a1b9c-....."
}
```

## Estrutura do projeto

Arquitetura em camadas (DDD), pacote base `com.axdborges.voz.budgeting`:

- `domain/` — modelo de domínio e contrato de repositório (`Transaction`, `TransactionId`, `Category`, `TransactionRepository`).
- `application/` — casos de uso, usados tanto pelo REST quanto pelo Tool Calling (`PersistTransactionUseCase`, `ListTransactionsByCategoryUseCase`, `ListAllTransactionsUseCase`).
- `infrastructure/http/` — controllers REST (`VoiceCommandController`, `TransactionController`).
- `infrastructure/ai/` — integração com os modelos de IA (`AudioTranscriptionService`, `ChatClientConfig`, `VoiceCommandInterpreter`, `TransactionTools`).
- `infrastructure/persistence/` — implementação real do `TransactionRepository`: `JpaTransactionRepository` (adapter que converte entre o `Transaction` do domínio e a `TransactionEntity` JPA), `entity/TransactionEntity` (entidade JPA) e `repository/TransactionJpaRepository` (repositório Spring Data).

## Testes

Ver `TESTES.md` para os comandos completos. Resumo:

```bash
docker build --target build -t voz-test .
docker run --rm voz-test ./gradlew test --no-daemon
```

> No Windows, rodar `./gradlew test` fora do Docker falha por um problema de encoding do path do projeto — sempre valide via Docker (detalhes em `TESTES.md`).

Ambas as rotas (`POST /voice-commands` e `GET /voice-commands/mock`, incluindo o parâmetro `file` e o valor default) têm testes unitários com mocks em `VoiceCommandControllerTest` — não dependem de áudio real nem de chave da OpenAI.

`JpaTransactionRepositoryTest` (`@DataJpaTest`) valida o mapeamento entidade ↔ domínio e as queries reais contra um banco **H2 em memória** — os testes automatizados não dependem do MySQL real do `compose.yml`, só a aplicação em produção/dev usa MySQL de fato.

Testes de integração que fazem chamadas reais à OpenAI (`OpenAiChatModelIntegrationTest`, `AudioTranscriptionServiceIntegrationTest`, `VoiceCommandInterpreterIntegrationTest`, `ToolCallingIntegrationTest`, `TransactionVoiceCommandFlowIntegrationTest`) só rodam quando a variável `OPENAI_API_KEY` está definida no ambiente onde os testes executam. O último roda o fluxo completo com áudio real (`mercado.mp3`, `uber.mp3`, `livros.mp3`) registrando transações em categorias diferentes e depois consulta tudo de uma vez com `consulta-todos.mp3`, conferindo tanto a resposta em texto quanto o estado real do repositório:

```bash
docker run --rm --env-file .env voz-test ./gradlew test --no-daemon
```

## Status do desafio

Progresso detalhado em `TODO.md`. Resumo:

- [x] 1. Estrutura base do projeto
- [x] 2. Spring AI + integração com o modelo de linguagem
- [x] 3. Recebimento e transcrição de áudio
- [x] 4. `ChatClient` e interpretação de intenção
- [x] 5. Tool Calling
- [x] 6. Persistência das transações (MySQL + JPA)
- [ ] 7. Geração de voz a partir da resposta
- [ ] 8. Endpoints REST de transações
- [ ] 9. Logs/auditoria
- [ ] 10. Finalização deste README
