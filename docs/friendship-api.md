# API de Amizades — guia de consumo (Angular)

Documentação da API de amizades do backend (`app-dateca-backend`) para o time do frontend. Cobre
autenticação, todos os endpoints, formatos de request/response, enums, erros e interfaces
TypeScript sugeridas.

## Base URL

Mesma base já usada para os demais endpoints do app (`/aluno`, `/login`, etc.), variando por
ambiente (dev/prod).

## Autenticação

Todos os endpoints abaixo exigem um JWT válido no header `Authorization`. O usuário autenticado é
sempre resolvido pelo backend a partir do token — **nenhum endpoint recebe o id do usuário logado
no corpo da requisição**.

```
Authorization: Bearer <token>
```

O token é obtido via login (já existente, fora do escopo deste documento):

```
POST /login
Body:  { "login": "202410001", "password": "123456" }
Resp:  { "token": "<jwt>" }
```

## Convenções gerais

- Todos os ids de usuário/amizade são **UUID** (string).
- Datas são `LocalDateTime` do Java serializado como ISO-8601 sem timezone, ex.:
  `"2026-07-20T10:30:00"`.
- Listagens são paginadas usando o formato padrão do Spring Data (`Page<T>`):

```json
{
  "content": [ /* array de itens */ ],
  "totalElements": 42,
  "totalPages": 3,
  "number": 0,
  "size": 20,
  "first": true,
  "last": false,
  "empty": false
}
```

  Parâmetros de paginação aceitos em todo endpoint `GET` de listagem: `?page=0&size=20&sort=campo,asc`.

- Erros seguem sempre este formato (`StandardError`):

```json
{
  "timestamp": "2026-07-20T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Já existe uma solicitação pendente entre esses usuários",
  "path": "/friendships"
}
```

## Enums

### `FriendshipStatus` (status persistido da relação)

| Valor | Significado |
|---|---|
| `PENDING` | solicitação enviada, aguardando resposta |
| `ACCEPTED` | são amigos |
| `DECLINED` | solicitação recusada |
| `CANCELLED` | solicitação cancelada por quem enviou |
| `BLOCKED` | um dos dois bloqueou o outro |
| `REMOVED` | amizade desfeita (ou desbloqueio recente) |

### `RelationshipStatus` (status relativo ao usuário logado, usado só na busca de usuários)

| Valor | Significado |
|---|---|
| `NONE` | sem nenhuma relação |
| `PENDING_SENT` | você enviou uma solicitação para esse usuário |
| `PENDING_RECEIVED` | esse usuário te enviou uma solicitação |
| `FRIEND` | vocês são amigos |
| `BLOCKED` | há um bloqueio entre vocês |
| `SELF` | é o próprio usuário logado |

## DTOs

### `FriendshipResponseDTO`

Retornado por todas as ações sobre uma amizade (enviar, aceitar, recusar, cancelar, bloquear,
desbloquear) e pelas listagens de pendentes recebidos/enviados. Já vem com o nome de ambos os
lados para evitar chamada extra no frontend.

```ts
interface FriendshipResponseDTO {
  id: string;               // uuid da amizade
  requesterId: string;      // uuid de quem enviou
  requesterName: string;
  receiverId: string;       // uuid de quem recebeu
  receiverName: string;
  status: FriendshipStatus;
  createdAt: string;
  acceptedAt: string | null;
  updatedAt: string;
}
```

### `FriendDTO`

Item retornado em `GET /me/friends`.

```ts
interface FriendDTO {
  id: string;                // uuid do amigo (Student.id)
  registrationNumber: number;
  name: string;
  points: number;
  friendSince: string;       // data em que a amizade foi aceita
}
```

### `UserSearchDTO`

Item retornado em `GET /users/search`.

```ts
interface UserSearchDTO {
  id: string;
  registrationNumber: number;
  name: string;
  friendsCount: number;
  relationshipStatus: RelationshipStatus;
}
```

### `FriendshipRequestDTO` (body de envio de solicitação)

```ts
interface FriendshipRequestDTO {
  receiverId: string; // uuid do usuário que vai receber o pedido
}
```

## Endpoints

### Enviar solicitação de amizade

```
POST /friendships
Body: { "receiverId": "uuid" }
201 Created -> FriendshipResponseDTO
```

Erros possíveis: `400` (auto-amizade), `404` (`receiverId` não existe), `409` (já existe pedido
pendente ou já são amigos), `403` (há bloqueio entre os dois).

Reenviar um pedido depois de um `DECLINED`/`CANCELLED`/`REMOVED` anterior é permitido e reaproveita
a mesma amizade, resetando para `PENDING`.

### Aceitar solicitação

```
PATCH /friendships/{id}/accept
200 OK -> FriendshipResponseDTO (status = ACCEPTED)
```

Só quem **recebeu** o pedido pode aceitar. Erros: `403` (não é o destinatário), `404`, `409`
(não está mais `PENDING`).

### Recusar solicitação

```
PATCH /friendships/{id}/decline
200 OK -> FriendshipResponseDTO (status = DECLINED)
```

Só quem recebeu pode recusar. Mesmos erros do accept.

### Cancelar solicitação enviada

```
PATCH /friendships/{id}/cancel
200 OK -> FriendshipResponseDTO (status = CANCELLED)
```

Só quem **enviou** o pedido pode cancelar. Erros: `403` (não é o solicitante), `404`, `409`.

### Remover amizade

```
DELETE /friendships/{id}
204 No Content
```

Só funciona se o status atual for `ACCEPTED`. Qualquer um dos dois amigos pode remover. Erros:
`403` (não participa da amizade), `404`, `409` (não está `ACCEPTED`).

### Bloquear usuário

```
PATCH /friendships/{id}/block
200 OK -> FriendshipResponseDTO (status = BLOCKED)
```

Qualquer um dos dois participantes pode bloquear, a partir de qualquer status (exceto já
bloqueado). Enquanto bloqueado, nenhum novo pedido pode ser enviado entre os dois. Erros: `403`
(não participa), `404`, `409` (já está bloqueado).

> Observação: bloquear opera sobre uma amizade **já existente** (`{id}`). Para bloquear alguém com
> quem você nunca teve nenhuma interação, é necessário primeiro haver um registro de amizade
> (ex.: enviar uma solicitação) antes de poder bloqueá-lo — não há endpoint de bloqueio "a frio"
> por `receiverId` nesta primeira versão.

### Desbloquear

```
PATCH /friendships/{id}/unblock
200 OK -> FriendshipResponseDTO (status = REMOVED)
```

Só quem executou o bloqueio pode desbloquear. Depois de desbloqueado, um novo pedido de amizade
pode ser enviado normalmente. Erros: `403` (não foi quem bloqueou), `404`, `409` (não está
`BLOCKED`).

### Listar meus amigos

```
GET /me/friends?page=0&size=20
200 OK -> Page<FriendDTO>
```

### Contar meus amigos

```
GET /me/friends/count
200 OK -> 42
```

Retorna um número puro (`Long`), sem envelope JSON.

### Solicitações recebidas (pendentes)

```
GET /me/friendships/received?page=0&size=20
200 OK -> Page<FriendshipResponseDTO>
```

### Solicitações enviadas (pendentes)

```
GET /me/friendships/sent?page=0&size=20
200 OK -> Page<FriendshipResponseDTO>
```

### Buscar usuários

```
GET /users/search?q=texto&page=0&size=20
200 OK -> Page<UserSearchDTO>
```

Busca por nome (parcial, case-insensitive) ou matrícula (`registrationNumber`, também parcial).
O próprio usuário logado pode aparecer no resultado, com `relationshipStatus = "SELF"`.

## Tabela de erros

| Status | Quando acontece |
|---|---|
| `400 Bad Request` | tentar enviar amizade para si mesmo; corpo inválido (`receiverId` ausente) |
| `403 Forbidden` | ação feita por quem não tem permissão (ex. tentar aceitar um pedido que não é seu); há bloqueio entre os usuários |
| `404 Not Found` | id de amizade ou de usuário não encontrado |
| `409 Conflict` | já existe pedido pendente / já são amigos / já está bloqueado / ação não cabe no status atual da amizade |

## Fluxo de exemplo (Angular)

```ts
// 1. Enviar pedido
this.http.post<FriendshipResponseDTO>('/friendships', { receiverId });

// 2. Ver quem me pediu amizade
this.http.get<Page<FriendshipResponseDTO>>('/me/friendships/received');

// 3. Aceitar
this.http.patch<FriendshipResponseDTO>(`/friendships/${friendshipId}/accept`, {});

// 4. Listar amigos
this.http.get<Page<FriendDTO>>('/me/friends?page=0&size=20');

// 5. Buscar pessoas para adicionar
this.http.get<Page<UserSearchDTO>>('/users/search?q=joao');
```

## Fora do escopo desta versão

Os itens abaixo fazem parte do domínio de amizades planejado, mas **não estão implementados
ainda** — não modelar telas assumindo que existem:

- Sugestões de amigos
- Amigos em comum
- Posição em ranking entre amigos / status online / última atividade nos dados de `FriendDTO`
- Eventos de domínio (notificações em tempo real, feed de atividades)
