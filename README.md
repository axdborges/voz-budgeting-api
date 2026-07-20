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
- **Gradle 9.4.1** (via wrapper, `./gradlew`)
- **Docker** e **Docker Compose**

## Como rodar

Pré-requisito: Docker Desktop instalado e rodando.

1. Copie o arquivo de exemplo de variáveis de ambiente:
   ```bash
   cp .env.example .env
   ```
2. Abra o `.env` e preencha sua chave da OpenAI (veja [Variáveis de ambiente](#variáveis-de-ambiente)).
3. Suba a aplicação:
   ```bash
   docker compose up -d --build
   ```
4. A API fica disponível em `http://localhost:8080`.

Para parar: `docker compose down`.

## Variáveis de ambiente

As variáveis ficam no arquivo `.env` (na raiz do projeto, **não é versionado** — cada pessoa cria o seu a partir de `.env.example`). O `compose.yml` injeta esse arquivo automaticamente no container (`env_file: .env`).

| Variável | Obrigatória | Descrição |
|---|---|---|
| `OPENAI_API_KEY` | Sim | Chave de API da OpenAI ([platform.openai.com](https://platform.openai.com/api-keys)). Usada tanto para o modelo de chat (`gpt-4o-mini`) quanto para a transcrição de áudio (`whisper-1`). |

**Nunca** commite o `.env` nem cole sua chave em nenhum lugar público — ele já está no `.gitignore`.

## Rotas disponíveis

### `POST /voice-commands`

Endpoint principal: recebe um arquivo de áudio, transcreve e envia o texto pro `ChatClient`, que classifica o comando em uma de três categorias e **executa a ação de verdade** através de Tool Calling:

1. **Registrar transação** — comando com ação financeira clara, seja gasto (ex.: "gastei", "paguei", "comprei") ou entrada/depósito (ex.: "coloquei", "depositei", "recebi") e um valor. Aciona a tool `registrarTransacao`, que persiste a transação.
2. **Consultar transações** — pergunta sobre gastos/saldo já existentes (ex.: "quanto eu gastei em mercado?"). Aciona a tool `consultarTransacoesPorCategoria`, que devolve a soma real das transações já registradas naquela categoria.
3. **Intenção não identificada** — o áudio não é um comando financeiro claro (ambíguo, incompleto, assunto não relacionado a finanças). Nesse caso o modelo **não chama nenhuma tool** — ele avisa que não entendeu e pede pra repetir.

Categorias usadas internamente: `MERCADO`, `TRANSPORTE`, `LAZER`, `SAUDE`, `MORADIA`, `EDUCACAO`, `OUTROS` (usada quando a categoria não é mencionada no comando).

- **Content-Type**: `multipart/form-data`
- **Campo**: `audio` (arquivo de áudio, ex.: mp3, wav, m4a)

```bash
curl -F "audio=@caminho/para/comando.mp3" http://localhost:8080/voice-commands
```

**Resposta** (`200 OK`) — exemplo registrando uma transação:
```json
{
  "transcription": "Eu quero colocar R$50,00 na minha conta hoje",
  "reply": "Transação registrada com sucesso: R$ 50,00 na categoria OUTROS (depósito na conta)."
}
```

**Resposta** (`200 OK`) — exemplo consultando transações já registradas:
```json
{
  "transcription": "Quanto eu tenho na minha conta?",
  "reply": "Você tem 1 transação(ões) na categoria OUTROS, totalizando R$ 50,00."
}
```

> Status atual: transcrição, interpretação e execução real (persistência/consulta) já funcionam de ponta a ponta. A persistência hoje é **em memória** (`InMemoryTransactionRepository`) — some quando a aplicação reinicia; troca por banco real é a próxima tarefa (6).

### `GET /voice-commands/mock`

Rota de conveniência **apenas para testes manuais em desenvolvimento** — não faz parte do desafio e será removida antes de qualquer publicação/deploy real. Roda o mesmo fluxo (transcrição + interpretação) sobre um arquivo de áudio já salvo localmente em `audios-java/` (pasta fora do controle de versão), sem precisar montar uma requisição multipart.

- **Parâmetro (query, opcional)**: `file` — nome do arquivo dentro de `audios-java/` (padrão: `meu-nome.mp3`)

```bash
curl "http://localhost:8080/voice-commands/mock?file=adicionar-saldo.mp3"
```

**Resposta** (`200 OK`):
```json
{
  "transcription": "Olá, meu nome é Alexandre e eu quero colocar R$50,00 na minha conta hoje. Preciso colocar R$50,00.",
  "reply": "Transação registrada com sucesso: R$ 50,00 na categoria OUTROS (depósito na conta)."
}
```

## Estrutura do projeto

Arquitetura em camadas (DDD), pacote base `com.axdborges.voz.budgeting`:

- `domain/` — modelo de domínio e contrato de repositório (`Transaction`, `TransactionId`, `Category`, `TransactionRepository`).
- `application/` — casos de uso, usados tanto pelo REST quanto pelo Tool Calling (`PersistTransactionUseCase`, `ListTransactionsByCategoryUseCase`).
- `infrastructure/http/` — controllers REST (`VoiceCommandController`, `TransactionController`).
- `infrastructure/ai/` — integração com os modelos de IA (`AudioTranscriptionService`, `ChatClientConfig`, `VoiceCommandInterpreter`, `TransactionTools`).
- `infrastructure/persistence/` — implementação do `TransactionRepository`. Hoje é `InMemoryTransactionRepository` (em memória, some ao reiniciar); vira JPA/banco real na Tarefa 6, sem mudar `domain`/`application`.

`infrastructure/persistence/entity` e `infrastructure/persistence/repository` (JPA) ainda são esqueletos até a Tarefa 6 — marcados com `// TODO (Tarefa 6)`.

## Testes

Ver `TESTES.md` para os comandos completos. Resumo:

```bash
docker build --target build -t voz-test .
docker run --rm voz-test ./gradlew test --no-daemon
```

> No Windows, rodar `./gradlew test` fora do Docker falha por um problema de encoding do path do projeto — sempre valide via Docker (detalhes em `TESTES.md`).

Ambas as rotas (`POST /voice-commands` e `GET /voice-commands/mock`, incluindo o parâmetro `file` e o valor default) têm testes unitários com mocks em `VoiceCommandControllerTest` — não dependem de áudio real nem de chave da OpenAI.

Testes de integração que fazem chamadas reais à OpenAI (`OpenAiChatModelIntegrationTest`, `AudioTranscriptionServiceIntegrationTest`, `VoiceCommandInterpreterIntegrationTest`) só rodam quando a variável `OPENAI_API_KEY` está definida no ambiente onde os testes executam:

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
- [ ] 6. Persistência das transações
- [ ] 7. Geração de voz a partir da resposta
- [ ] 8. Endpoints REST de transações
- [ ] 9. Logs/auditoria
- [ ] 10. Finalização deste README
