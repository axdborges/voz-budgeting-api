# voz-budgeting-api

API de orçamento que usa IA para processar comandos de voz relacionados a transações financeiras. O cliente envia um áudio, a aplicação transcreve o comando e interpreta a intenção com um LLM, que classifica o comando em uma de três categorias — **registrar transação**, **consultar transações** ou **intenção não identificada** (quando o áudio não é um comando financeiro claro) — e **executa a ação de verdade via Tool Calling** (persiste ou consulta as transações), devolvendo uma resposta em **texto e em áudio** (texto-para-voz).

Desafio de Projeto da [DIO](https://dio.me), baseado no repositório [dio-spring-boot-learning-track](https://github.com/axdborges/dio-spring-boot-learning-track/tree/main)/05-spring-ai.

> 🚧 Projeto em andamento. O andamento das tarefas está em `TODO.md`; este README é atualizado conforme o projeto avança e será fechado na tarefa final.

## Tecnologias

- **Java 21** (LTS)
- **Spring Boot 4.0.5**
- **Spring AI 2.0.0**, com [OpenAI](https://platform.openai.com/) como provedor:
  - Chat: `gpt-4o-mini`
  - Transcrição de áudio (speech-to-text): `whisper-1`
  - Geração de voz (text-to-speech): `gpt-4o-mini-tts`
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

A resposta padrão (JSON) traz três campos: `transcription` (o que foi entendido do áudio), `reply` (o resultado em texto) e **`audioBase64`** (o `reply` convertido em voz pela OpenAI — `gpt-4o-mini-tts`, formato mp3 — já codificado em Base64, pronto pra decodificar e tocar no cliente).

> **Testando manualmente (Insomnia/curl)**: colar/receber uma string Base64 grande trava a tela. Se você mandar o header `Accept: audio/mpeg` na requisição, a API devolve o **MP3 puro** (sem JSON) — no Insomnia o preview já toca o áudio, e com `curl` dá pra salvar direto: `curl -H "Accept: audio/mpeg" -o resposta.mp3 ...`. Sem esse header (ou com `Accept: application/json`/`*/*`), a resposta continua sendo o JSON de sempre.

- **Content-Type**: `multipart/form-data`
- **Campo**: `audio` (arquivo de áudio, ex.: mp3, wav, m4a)

```bash
curl -F "audio=@caminho/para/comando.mp3" http://localhost:8080/voice-commands
```

**Resposta** (`200 OK`) — exemplo registrando uma transação (sem data explícita, usa hoje; `audioBase64` abreviado aqui por brevidade):
```json
{
  "transcription": "Eu quero colocar R$50,00 na minha conta hoje",
  "reply": "Transação registrada com sucesso: R$ 50,00 em OUTROS no dia 2026-07-20, id 3f2a1b9c-.....",
  "audioBase64": "//PExABaTDm8AVrIAD11wjOaCcez0..."
}
```

**Resposta** (`200 OK`) — exemplo com data relativa ("ontem"):
```json
{
  "transcription": "Comprei um hambúrguer ontem por R$33,25",
  "reply": "Transação registrada com sucesso: R$ 33,25 em OUTROS (hambúrguer) no dia 2026-07-19, id 9c8b7a6d-.....",
  "audioBase64": "//PExABaTDm8AVrIAD11wjOaCcez0..."
}
```

**Resposta** (`200 OK`) — exemplo consultando transações de uma categoria específica:
```json
{
  "transcription": "Quanto eu tenho na minha conta?",
  "reply": "Você tem 1 transação(ões) na categoria OUTROS, totalizando R$ 50,00.",
  "audioBase64": "//PExABaTDm8AVrIAD11wjOaCcez0..."
}
```

**Resposta** (`200 OK`) — exemplo consultando TODAS as transações, agrupadas por categoria:
```json
{
  "transcription": "Quais foram todas as minhas transações feitas de todas as categorias?",
  "reply": "Resumo de todas as suas transações por categoria:\n- EDUCACAO: 1 transação(ões), totalizando R$ 0,55\n- MERCADO: 1 transação(ões), totalizando R$ 669,29\n- TRANSPORTE: 1 transação(ões), totalizando R$ 12,50",
  "audioBase64": "//PExABaTDm8AVrIAD11wjOaCcez0..."
}
```

**Resposta com `Accept: audio/mpeg`** — mesmo endpoint, mp3 puro em vez de JSON:
```bash
curl -H "Accept: audio/mpeg" -o resposta.mp3 -F "audio=@caminho/para/comando.mp3" http://localhost:8080/voice-commands
```

#### Formato de retorno conforme o header `Accept`

O corpo é o mesmo conteúdo (a resposta em voz), só muda a **forma de entrega**, decidida pelo header `Accept` da requisição:

| Header `Accept` enviado | Status | `Content-Type` da resposta | Corpo |
|---|---|---|---|
| `audio/mpeg` | `200 OK` | `audio/mpeg` | Bytes crus do MP3 (pronto pra tocar/salvar) |
| `application/json` | `200 OK` | `application/json` | JSON `{ transcription, reply, audioBase64 }` |
| `*/*` (padrão de curl/browser) | `200 OK` | `application/json` | JSON (o mesmo de cima) |
| ausente (nenhum `Accept`) | `200 OK` | `application/json` | JSON (o mesmo de cima) |

Regra: a API só devolve o MP3 puro quando o `Accept` pede **explicitamente** um tipo `audio/*` concreto. Qualquer outra coisa (inclusive o coringa `*/*`) cai no JSON — comportamento determinístico, decidido no `VoiceCommandController` (não pela negociação por `produces` do Spring, que trata `*/*` de forma ambígua).

#### Erros possíveis (requisição mal formada, antes da lógica de IA)

O upload é processado pelo `MultipartResolver` do Spring (Tomcat) **antes** de chegar na transcrição. Se a requisição estiver mal montada, o erro vem daí. Todos os erros da API são normalizados para **JSON consistente** por um handler global (`GlobalExceptionHandler`, um `@RestControllerAdvice`) — inclusive quando o cliente manda `Accept: audio/mpeg` (o erro sai como JSON legível, não como MP3 nem como página HTML). Mapa dos casos:

| O que está errado na requisição | Status | Causa (erro no log do servidor) |
|---|---|---|
| Campo do arquivo com nome diferente de `audio` (ou linha desativada/vazia) | `400 Bad Request` | `MissingServletRequestPartException: Required part 'audio' is not present` |
| `Content-Type: multipart/form-data` setado na mão **sem** `boundary` | `400 Bad Request` | `MultipartException: Failed to parse multipart servlet request` |
| `Content-Type` não-multipart (ex.: sobrou `application/json` de outro teste) | `415 Unsupported Media Type` | `HttpMediaTypeNotSupportedException: Content-Type '...' is not supported` |
| Arquivo maior que **1 MB** (limite default do Spring, não configurado neste projeto) | `413 Content Too Large` | `MaxUploadSizeExceededException` |
| Tudo certo: campo `audio`, arquivo válido, boundary automático | `200 OK` | — |

Formato do corpo de erro (mesmo para todas as rotas):
```json
{
  "timestamp": "2026-07-22T00:48:52.79",
  "status": 400,
  "error": "Bad Request",
  "message": "Required part 'audio' is not present.",
  "path": "/voice-commands"
}
```

> Dica de teste no Insomnia: o campo do arquivo tem que se chamar exatamente `audio` (minúsculo, sem espaços), com tipo **File** e a linha marcada; e **não** defina `Content-Type` manualmente — deixe o Insomnia gerar o `multipart/form-data; boundary=...` sozinho. O `Accept: audio/mpeg` pode ficar, é ele que faz a resposta vir como MP3 (e, em caso de erro, o JSON acima).

> Status atual: transcrição, interpretação, execução real (persistência/consulta) e geração de voz já funcionam de ponta a ponta. A persistência é **real, em MySQL** (`JpaTransactionRepository` + `TransactionEntity`) — os dados sobrevivem a reinícios da aplicação.

### `GET /voice-commands/mock`

Rota de conveniência **apenas para testes manuais em desenvolvimento** — não faz parte do desafio e será removida antes de qualquer publicação/deploy real. Roda o mesmo fluxo (transcrição + interpretação + geração de voz) sobre um arquivo de áudio já salvo localmente em `audios-java/` (pasta fora do controle de versão), sem precisar montar uma requisição multipart.

- **Parâmetro (query, opcional)**: `file` — nome do arquivo dentro de `audios-java/` (padrão: `mercado.mp3`)

```bash
curl "http://localhost:8080/voice-commands/mock?file=adicionar-saldo.mp3"
```

**Resposta** (`200 OK`):
```json
{
  "transcription": "Olá, meu nome é Alexandre e eu quero colocar R$50,00 na minha conta hoje. Preciso colocar R$50,00.",
  "reply": "Transação registrada com sucesso: R$ 50,00 em OUTROS no dia 2026-07-20, id 3f2a1b9c-.....",
  "audioBase64": "//PExABaTDm8AVrIAD11wjOaCcez0..."
}
```

Aqui também funciona o `Accept: audio/mpeg` pra pegar o mp3 puro em vez do JSON — é a forma mais rápida de ouvir um áudio de teste sem montar multipart:
```bash
curl -H "Accept: audio/mpeg" -o resposta.mp3 "http://localhost:8080/voice-commands/mock?file=uber.mp3"
```

### `GET /transactions`

Lista transações sem passar por IA/áudio — acesso direto ao que já foi registrado. Sem parâmetro, devolve todas; com `category`, filtra por categoria (mesmos valores usados pelo comando de voz).

- **Parâmetro (query, opcional)**: `category` — um de `MERCADO`, `TRANSPORTE`, `LAZER`, `SAUDE`, `MORADIA`, `EDUCACAO`, `OUTROS`. Valor inválido devolve `400 Bad Request`.

```bash
curl "http://localhost:8080/transactions"
curl "http://localhost:8080/transactions?category=MERCADO"
```

**Resposta** (`200 OK`):
```json
[
  {
    "id": "3f2a1b9c-...",
    "description": "hambúrguer",
    "category": "OUTROS",
    "amount": 33.25,
    "occurredAt": "2026-07-19T12:00:00",
    "updatedAt": null
  }
]
```

### `POST /transactions`

Cria uma transação direto via JSON, sem passar por áudio. `category` e `amount` são obrigatórios (`amount` deve ser maior que zero); `description` e `date` são opcionais — sem `date`, é usada a data de hoje, igual ao fluxo por voz.

```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"description": "Livro de história", "category": "EDUCACAO", "amount": 89.90, "date": "2026-07-21"}'
```

**Resposta** (`201 Created`):
```json
{
  "id": "9c8b7a6d-...",
  "description": "Livro de história",
  "category": "EDUCACAO",
  "amount": 89.90,
  "occurredAt": "2026-07-21T12:00:00",
  "updatedAt": null
}
```

Requisição inválida (`category` ou `amount` ausente, ou `amount` menor ou igual a zero) devolve `400 Bad Request` com a mensagem do erro no corpo.

### `GET /transactions/{id}`

Busca uma transação específica pelo `id` (UUID). Devolve `404 Not Found` se não existir, ou `400 Bad Request` se `{id}` não for um UUID válido.

```bash
curl "http://localhost:8080/transactions/9c8b7a6d-1234-4a1b-8abc-1234567890ab"
```

### `PATCH /transactions/{id}`

Edita uma transação existente — **atualização parcial**: só os campos enviados no corpo são alterados, os demais permanecem como estavam. Ao editar, o campo `updatedAt` é preenchido com o momento da edição (fica `null` enquanto a transação nunca foi editada). Se `date` for enviada, só a data muda — o horário original (`occurredAt`) é preservado; se não for enviada, a data também permanece a mesma.

```bash
curl -X PATCH http://localhost:8080/transactions/9c8b7a6d-1234-4a1b-8abc-1234567890ab \
  -H "Content-Type: application/json" \
  -d '{"amount": 99.90}'
```

**Resposta** (`200 OK`):
```json
{
  "id": "9c8b7a6d-...",
  "description": "Livro de história",
  "category": "EDUCACAO",
  "amount": 99.90,
  "occurredAt": "2026-07-21T12:00:00",
  "updatedAt": "2026-07-21T15:30:00"
}
```

`404 Not Found` se o `id` não existir; `400 Bad Request` se `id` for inválido ou `amount` (quando enviado) for menor ou igual a zero.

### `DELETE /transactions/{id}`

Apaga uma transação. Devolve `204 No Content` em caso de sucesso, `404 Not Found` se o `id` não existir, `400 Bad Request` se `{id}` não for um UUID válido.

```bash
curl -X DELETE http://localhost:8080/transactions/9c8b7a6d-1234-4a1b-8abc-1234567890ab
```

## Estrutura do projeto

Arquitetura em camadas (DDD), pacote base `com.axdborges.voz.budgeting`:

- `domain/` — modelo de domínio e contrato de repositório (`Transaction`, `TransactionId`, `Category`, `TransactionRepository`).
- `application/` — casos de uso, usados tanto pelo REST quanto pelo Tool Calling (`PersistTransactionUseCase`, `ListTransactionsByCategoryUseCase`, `ListAllTransactionsUseCase`).
- `infrastructure/http/` — controllers REST (`VoiceCommandController`, `TransactionController`) e o tratamento de erros central (`GlobalExceptionHandler`, um `@RestControllerAdvice` que normaliza exceções da aplicação e do Spring para o mesmo JSON `ErrorResponse`).
- `infrastructure/ai/` — integração com os modelos de IA (`AudioTranscriptionService`, `TextToSpeechService`, `ChatClientConfig`, `VoiceCommandInterpreter`, `TransactionTools`).
- `infrastructure/persistence/` — implementação real do `TransactionRepository`: `JpaTransactionRepository` (adapter que converte entre o `Transaction` do domínio e a `TransactionEntity` JPA), `entity/TransactionEntity` (entidade JPA) e `repository/TransactionJpaRepository` (repositório Spring Data).

## Testes

Ver `TESTES.md` para os comandos completos. Resumo:

```bash
docker build --target build -t voz-test .
docker run --rm voz-test ./gradlew test --no-daemon
```

> No Windows, rodar `./gradlew test` fora do Docker falha por um problema de encoding do path do projeto — sempre valide via Docker (detalhes em `TESTES.md`).

Ambas as rotas (`POST /voice-commands` e `GET /voice-commands/mock`, incluindo o parâmetro `file` e o valor default) têm testes unitários com mocks em `VoiceCommandControllerTest` — não dependem de áudio real nem de chave da OpenAI. Todas as rotas de `/transactions` (listar tudo, listar por categoria, criar, buscar/editar/apagar por `id`, e os casos de erro 400/404) têm testes equivalentes em `TransactionControllerTest`. `JpaTransactionRepositoryTest` cobre `findById`/`deleteById`/atualização (`save` sobrescrevendo um registro existente com o mesmo `id`) contra o H2 real.

`JpaTransactionRepositoryTest` (`@DataJpaTest`) valida o mapeamento entidade ↔ domínio e as queries reais contra um banco **H2 em memória** — os testes automatizados não dependem do MySQL real do `compose.yml`, só a aplicação em produção/dev usa MySQL de fato.

Testes de integração que fazem chamadas reais à OpenAI (`OpenAiChatModelIntegrationTest`, `AudioTranscriptionServiceIntegrationTest`, `TextToSpeechServiceIntegrationTest`, `VoiceCommandInterpreterIntegrationTest`, `ToolCallingIntegrationTest`, `TransactionVoiceCommandFlowIntegrationTest`) só rodam quando a variável `OPENAI_API_KEY` está definida no ambiente onde os testes executam. O último roda o fluxo completo com áudio real (`mercado.mp3`, `uber.mp3`, `livros.mp3`) registrando transações em categorias diferentes e depois consulta tudo de uma vez com `consulta-todos.mp3`, conferindo tanto a resposta em texto quanto o estado real do repositório:

```bash
docker run --rm --env-file .env voz-test ./gradlew test --no-daemon
```

## Melhorias extras implementadas

Além das 10 tarefas principais do desafio, o `TODO.md` lista uma seção de "evoluções opcionais" — melhorias sugeridas, mas não obrigatórias. A maioria delas acabou surgindo organicamente enquanto as tarefas principais eram desenvolvidas e validadas com áudios reais, não como um bloco separado de trabalho:

- **Novos tipos de consulta financeira**: além de consultar uma categoria específica (`consultarTransacoesPorCategoria`), a API também consulta **todas as transações de uma vez, agrupadas por categoria e com o total de cada uma** (`consultarTodasAsTransacoes`) — surgiu ao testar um comando de voz do tipo "quais foram todas as minhas transações".
- **Respostas da IA mais confiáveis**: o prompt de classificação de intenção passou por várias rodadas de ajuste depois de testes com áudio real — ganhou uma terceira categoria explícita ("intenção não identificada", pra evitar que o modelo "chute" registrar ou consultar quando o áudio é ambíguo), passou a cobrir relatos indiretos de gasto (não só verbos diretos como "gastei"/"paguei") e resolve datas relativas ("ontem", "segunda passada") para a data real antes de registrar.
- **Novas ferramentas no Tool Calling**: `consultarTodasAsTransacoes` é uma tool adicional, além das duas do escopo original (`registrarTransacao`, `consultarTransacoesPorCategoria`).
- **Validações antes de salvar uma transação**: as rotas REST diretas (`POST`/`PATCH /transactions`) validam `category` obrigatória e `amount` maior que zero antes de persistir, devolvendo `400 Bad Request` com mensagem clara em caso de dado inválido.
- **Endpoints REST mais completos**: a Tarefa 8 pedia só "revisar e organizar" as rotas existentes; foi além disso — CRUD completo por `{id}` (`GET`/`PATCH`/`DELETE`), IDs fortemente tipados como UUID (`TransactionId`, rejeita formato inválido automaticamente), atualização parcial (`PATCH`) com campo `updatedAt`, e um handler de erros global (`GlobalExceptionHandler`) que padroniza **todas** as respostas de erro da API em um único formato JSON — antes, erros do próprio framework (upload mal formado, tipo inválido, parâmetro ausente) caíam numa página HTML genérica em vez do JSON da aplicação.
- **Testes para os principais fluxos**: cobertura ampla em todas as camadas — testes unitários (casos de uso, tools, domínio), testes de fatia web (`@WebMvcTest`) e de persistência (`@DataJpaTest` contra H2), e testes de integração que rodam o fluxo completo com **áudio real** (transcrição → interpretação → execução → persistência), incluindo o cenário de registrar em categorias diferentes e depois consultar tudo de uma vez.
- **Documentação mais completa da API**: o README documenta cada rota com exemplos de `curl`, uma tabela explicando os diferentes formatos de retorno conforme o header `Accept` (JSON vs. MP3 puro) e uma tabela de erros possíveis com status/causa de cada um; o `TESTES.md` traz um checklist de como validar qualquer mudança antes de considerá-la pronta.

Não implementada: **propor uma nova ideia de assistente usando a mesma base técnica** — é uma proposta de conteúdo (não uma mudança de código) e ficou fora do escopo até agora.

## Status do desafio

Progresso detalhado em `TODO.md`. Resumo:

- [x] 1. Estrutura base do projeto
- [x] 2. Spring AI + integração com o modelo de linguagem
- [x] 3. Recebimento e transcrição de áudio
- [x] 4. `ChatClient` e interpretação de intenção
- [x] 5. Tool Calling
- [x] 6. Persistência das transações (MySQL + JPA)
- [x] 7. Geração de voz a partir da resposta (TTS)
- [x] 8. Endpoints REST de transações
- [ ] 9. Logs/auditoria
- [ ] 10. Finalização deste README
