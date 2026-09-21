# Play Your List — MS-AV-01 (Microservices Development, ESPM)

Implementação da atividade **MS-AV-01-Endpoints**: um app de montagem de
playlists para o sistema **HPWM** (High Productivity With Music), composto
por 4 "microsserviços" (musicas, playlists, reproducao e api).

## Decisão de arquitetura

O enunciado pede: *"o ideal seria criar um projeto para cada microsserviço,
mas, para facilitar, criar todos os endpoints dentro do mesmo projeto"*.
Por isso este repositório contém **um único projeto Spring Boot** (uma
única aplicação, uma única porta, um único banco H2 em memória), com os
endpoints organizados em pacotes por domínio:

```
com.espm.playyourlist
├── musica       → microsserviço "musicas" (CRUD)
├── playlist     → microsserviço "playlists"
├── reproducao   → microsserviço "reproducao"
├── api          → microsserviço "api" (orquestrador, usa OpenFeign)
├── exception    → tratamento de erros centralizado
└── config       → (reservado para configurações adicionais)
```

O microsserviço `api` usa **OpenFeign** normalmente (clients declarativos,
`@FeignClient`), só que os clients apontam para `http://localhost:8080`
(a própria aplicação, configurável em `app.base-url` no
`application.yml`) em vez de hosts remotos — o que permite demonstrar o
uso do OpenFeign para orquestração/validação entre "serviços" sem a
complexidade operacional de subir 4 processos e um service discovery
separados. Migrar para microsserviços realmente distribuídos exigiria
apenas: (1) separar cada pacote em seu próprio módulo/projeto Spring Boot,
(2) dar a cada um seu próprio banco de dados e porta, e (3) trocar
`app.base-url` pela URL real (ou nome no Eureka/Consul) de cada serviço.

> **Observação sobre `/statistic`:** o enunciado menciona que
> `PUT /api/executar/{playlistId}` deve gerar o registro de execução
> "pelo endpoint `POST /statistic`", mas a lista de endpoints do
> microsserviço `reproducao` define esse endpoint como `POST /reproducao`.
> O `ReproducaoClient` chama `POST /reproducao`, que é o endpoint
> realmente especificado e implementado.

## Stack

- Java 17
- Spring Boot 3.3.5 (Web, Data JPA, Validation)
- Spring Cloud OpenFeign 2023.0.4
- H2 (banco em memória, populado via `data.sql`)
- Maven

## Como rodar

```bash
mvn spring-boot:run
```

A aplicação sobe em `http://localhost:8080`. O console do H2 fica
disponível em `http://localhost:8080/h2-console`
(JDBC URL: `jdbc:h2:mem:playyourlist`, usuário `sa`, senha em branco).

Os dados de exemplo (`data.sql`) são recriados a cada `run`.

## Endpoints

### `musicas` (CRUD)

| Método | Caminho          | Descrição                  |
|--------|-------------------|-----------------------------|
| POST   | `/musicas`         | Cadastrar uma nova música   |
| GET    | `/musicas`         | Listar todas as músicas     |
| GET    | `/musicas/{id}`    | Buscar uma música pelo ID   |
| PUT    | `/musicas/{id}`    | Atualizar uma música        |
| DELETE | `/musicas/{id}`    | Excluir uma música          |

Corpo de exemplo (POST/PUT):

```json
{
  "titulo": "Hotel California",
  "artista": "Eagles",
  "album": "Hotel California",
  "duracao": 391,
  "genero": "Rock"
}
```

Validações: `titulo` e `artista` obrigatórios (não vazios/whitespace);
`album` opcional (máx. 150); `duracao` obrigatória e > 0; `genero`
opcional (máx. 50).

### `playlists`

| Método | Caminho                                     | Descrição                                                  |
|--------|-----------------------------------------------|--------------------------------------------------------------|
| POST   | `/playlists`                                   | Criar uma nova playlist                                       |
| GET    | `/playlists`                                   | Listar todas as playlists                                     |
| GET    | `/playlists/{playlistid}`                      | Buscar uma playlist pelo id                                   |
| PUT    | `/playlists/{playlistid}`                      | Atualizar nome/descrição                                      |
| DELETE | `/playlists/{playlistid}`                      | Excluir a playlist e suas músicas associadas                  |
| POST   | `/playlists/{playlistid}/musicas/{musicaId}`   | Adicionar uma música à playlist                                |
| DELETE | `/playlists/{playlistid}/musicas/{musicaId}`   | Remover uma música da playlist                                 |
| GET    | `/playlists/{playlistid}/musicas`              | Listar os ids das músicas da playlist (sem título/artista/etc) |

Validações: `nome` obrigatório (não vazio/whitespace); `descricao`
opcional (máx. 255).

### `reproducao`

| Método | Caminho                        | Descrição                                       |
|--------|----------------------------------|---------------------------------------------------|
| POST   | `/reproducao`                    | Registra uma execução da playlist (`playlistId`, `dataHora` opcional) |
| GET    | `/reproducao/{playlistid}`       | Lista todas as reproduções de uma playlist         |
| GET    | `/reproducao/total/{playlistid}` | Retorna o total de execuções de uma playlist       |

### `api` (orquestrador, via OpenFeign)

| Método | Caminho                                       | Descrição |
|--------|--------------------------------------------------|-----------|
| POST   | `/api/adicionar/{playlistId}/musicas/{musicaId}` | Valida música e playlist (Feign) e adiciona a música à playlist. Retorna `"Música {titulo} adicionada com sucesso à playlist {nome}"` |
| PUT    | `/api/executar/{playlistId}`                     | Valida a playlist (Feign) e registra sua execução via `POST /reproducao` |

Erros de validação retornam `400`; recursos inexistentes retornam `404`
com um corpo padronizado (`timestamp`, `status`, `erro`, `mensagem`,
`detalhes`).

## Testando rapidamente

```bash
# criar música
curl -X POST http://localhost:8080/musicas -H "Content-Type: application/json" \
  -d '{"titulo":"Hotel California","artista":"Eagles","album":"Hotel California","duracao":391,"genero":"Rock"}'

# adicionar música 1 à playlist 2 via orquestrador
curl -X POST http://localhost:8080/api/adicionar/2/musicas/1

# executar (reproduzir) a playlist 2
curl -X PUT http://localhost:8080/api/executar/2

# total de execuções da playlist 1
curl http://localhost:8080/reproducao/total/1
```

## Entrega

Após revisar o código, basta inicializar o repositório git e enviar o
link, conforme pedido no enunciado:

```bash
git init
git add .
git commit -m "MS-AV-01: implementação dos 4 microsserviços (musicas, playlists, reproducao, api)"
git branch -M main
git remote add origin <URL_DO_SEU_REPOSITORIO>
git push -u origin main
```
