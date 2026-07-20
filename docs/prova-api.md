# API de Provas (ProvaHub) — guia de consumo (Angular)

Documentação da API de provas do backend (`app-dateca-backend`) para o time do frontend. Cobre
autenticação, todos os endpoints, formatos de request/response, enums, erros e interfaces
TypeScript sugeridas — para substituir o mock em `localStorage` de
`src/app/service/prova/prova.service.ts` pela API real.

## Base URL

Mesma base já usada para os demais endpoints do app (`/aluno`, `/login`, `/questao`, `/enade`
etc.), variando por ambiente (dev/prod).

## Autenticação

Todos os endpoints abaixo exigem um JWT válido no header `Authorization`. O usuário autenticado é
sempre resolvido pelo backend a partir do token — **nenhum endpoint recebe o id do usuário logado
no corpo da requisição**.

```
Authorization: Bearer <token>
```

## Convenções gerais

- Todos os ids de prova/questão/submissão são **UUID** (string).
- Datas (`dataAbertura`, `dataEncerramento`) são `LocalDate`, formato `"YYYY-MM-DD"`.
- Hora (`horaAbertura`) é `LocalTime`, formato `"HH:mm"` (ou `"HH:mm:ss"`).
- Timestamps (`criadaEm`, `respondidoEm`) são `LocalDateTime` sem timezone, ex.:
  `"2026-07-20T10:30:00"`.
- Listagens (`/prova/minhas`, `/prova/publicas`, `/prova/{id}/ranking`) retornam **array simples**
  (`T[]`), sem paginação e sem envelope — diferente do módulo de amizades.
- `status` **nunca** é um valor que o cliente define diretamente (exceto ao optar por salvar como
  `"Rascunho"` — ver seção de status). Ele é sempre recalculado pelo backend a partir das datas e
  do fato de a prova estar publicada ou não.
- Erros seguem sempre este formato (`StandardError`), igual ao resto da API:

```json
{
  "timestamp": "2026-07-20T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Você já respondeu esta prova",
  "path": "/prova/9c2b.../responder"
}
```

## Enums

### `dificuldade` (reaproveita o `PointsEnum` já usado em Questão/Enade)

| Valor aceito/retornado | Pontuação por acerto |
|---|---|
| `"Fácil"` | 5 |
| `"Médio"` | 7 |
| `"Difícil"` | 10 |

Aceito como string exatamente nesse formato (com acento), tanto no envio quanto no retorno.

### `visibilidade`

| Valor | Significado |
|---|---|
| `"TODOS"` | qualquer usuário autenticado pode ver/responder |
| `"AMIGOS"` | somente amigos do criador (usa o sistema de amizades já existente) |
| `"GRUPO"` | reservado para o futuro — **hoje o backend trata `GRUPO` exatamente como `TODOS`**, pois ainda não existe entidade de grupo |

### `status` (sempre calculado pelo backend, nunca enviado em request além do caso "Rascunho")

| Valor | Quando ocorre |
|---|---|
| `"Rascunho"` | prova não publicada. Só o criador consegue ver/editar/excluir |
| `"Agendado"` | publicada, mas `dataAbertura` ainda está no futuro |
| `"Ativo"` | publicada, `dataAbertura` já passou (ou não foi definida) e `dataEncerramento` está vazio ou no futuro |
| `"Encerrado"` | publicada e `dataEncerramento` já passou |

`rankingDisponivel` (booleano calculado, presente em todo DTO de listagem/detalhe) só é `true`
quando `status` é `"Ativo"` ou `"Encerrado"`.

## DTOs

### `ProvaQuestaoForm` (usado dentro do body de criação/edição)

```ts
interface ProvaQuestaoForm {
  statement: string;
  alternativeA: string;
  alternativeB: string;
  alternativeC: string;
  alternativeD: string;
  alternativeE: string;
  correctAnswer: 'A' | 'B' | 'C' | 'D' | 'E';
  comment: string | null;
}
```

A ordem de exibição das questões é a ordem em que elas aparecem no array `questoes` — não existe
campo de ordem no request, ele é atribuído automaticamente pelo backend.

### `ProvaForm` (body de `POST /prova` e `PUT /prova/{id}`)

```ts
interface ProvaForm {
  titulo: string;
  descricao: string | null;
  disciplina: string;
  dificuldade: 'Fácil' | 'Médio' | 'Difícil';
  capaUrl: string | null;              // data URI base64, ex: "data:image/png;base64,..."
  questoes: ProvaQuestaoForm[];        // pode vir vazio se status = "Rascunho"
  dataAbertura: string | null;         // "YYYY-MM-DD"
  horaAbertura: string | null;         // "HH:mm"
  dataEncerramento: string | null;     // "YYYY-MM-DD"
  maxParticipantes: number | null;
  visibilidade: 'TODOS' | 'AMIGOS' | 'GRUPO';
  status: string | null;               // "Rascunho" mantém como rascunho; qualquer outro valor publica
}
```

> **Como decidir entre "salvar rascunho" e "publicar":** envie `status: "Rascunho"` para salvar
> sem publicar. Qualquer outro valor em `status` (ou o campo omitido/`null`) faz o backend publicar
> a prova imediatamente — o `status` final exibido, porém, ainda será recalculado a partir das
> datas (`Agendado`/`Ativo`/`Encerrado`), o valor enviado aqui só decide **rascunho vs. publicar**.
> Publicar uma prova sem nenhuma questão retorna `400`.

### `ProvaQuestaoDTO` (retornado para o criador — inclui gabarito)

```ts
interface ProvaQuestaoDTO {
  id: string;
  statement: string;
  alternativeA: string;
  alternativeB: string;
  alternativeC: string;
  alternativeD: string;
  alternativeE: string;
  correctAnswer: string;   // 'A'..'E'
  comment: string | null;
  ordem: number;
}
```

### `ProvaResumoDTO` (itens de `GET /prova/minhas` e `GET /prova/publicas`)

```ts
interface ProvaResumoDTO {
  id: string;
  titulo: string;
  disciplina: string;
  dificuldade: 'Fácil' | 'Médio' | 'Difícil';
  capaUrl: string | null;
  status: 'Rascunho' | 'Agendado' | 'Ativo' | 'Encerrado';
  participantes: number;
  rankingDisponivel: boolean;
  dataAbertura: string | null;
  horaAbertura: string | null;
  dataEncerramento: string | null;
  maxParticipantes: number | null;
  visibilidade: 'TODOS' | 'AMIGOS' | 'GRUPO';
  criadaEm: string;
}
```

### `ProvaDetalheDTO` (retorno de `GET /prova/{id}`, `POST /prova`, `PUT /prova/{id}`, `POST /prova/{id}/publicar`)

Tudo que tem em `ProvaResumoDTO`, mais:

```ts
interface ProvaDetalheDTO extends ProvaResumoDTO {
  descricao: string | null;
  criadorId: string;
  questoes: ProvaQuestaoDTO[];
}
```

### `ProvaQuestaoAlunoDTO` / `ProvaAlunoDTO` (retorno de `GET /prova/{id}/questoes/aluno`)

**Sem gabarito** — usado para a tela de resolução da prova pelo aluno.

```ts
interface ProvaQuestaoAlunoDTO {
  id: string;
  statement: string;
  alternativeA: string;
  alternativeB: string;
  alternativeC: string;
  alternativeD: string;
  alternativeE: string;
  ordem: number;
  // sem correctAnswer, sem comment
}

interface ProvaAlunoDTO {
  id: string;
  titulo: string;
  descricao: string | null;
  disciplina: string;
  dificuldade: 'Fácil' | 'Médio' | 'Difícil';
  capaUrl: string | null;
  questoes: ProvaQuestaoAlunoDTO[];
}
```

### `ProvaResponderForm` (body de `POST /prova/{id}/responder`)

```ts
interface ProvaRespostaForm {
  provaQuestaoId: string;
  respostaEscolhida: 'A' | 'B' | 'C' | 'D' | 'E';
}

interface ProvaResponderForm {
  respostas: ProvaRespostaForm[];
}
```

Não é obrigatório responder todas as questões — as que não vierem no array contam como erradas no
resultado. Enviar um `provaQuestaoId` que não pertence à prova não quebra a resposta, ele
simplesmente é ignorado no cálculo.

### `ProvaResultadoDTO` (retorno de `POST /prova/{id}/responder`)

Já vem com o gabarito completo (correção + comentário de cada questão), para a tela de resultado.

```ts
interface ProvaResultadoItemDTO {
  provaQuestaoId: string;
  correctAnswer: string;             // 'A'..'E'
  respostaEscolhida: string | null;  // null se o aluno não respondeu essa questão
  correta: boolean;
  comment: string | null;
}

interface ProvaResultadoDTO {
  pontuacao: number;       // acertos * peso da dificuldade (5/7/10)
  totalQuestoes: number;
  acertos: number;
  gabarito: ProvaResultadoItemDTO[];
}
```

### `ProvaRankingDTO` (itens de `GET /prova/{id}/ranking`)

```ts
interface ProvaRankingDTO {
  posicao: number;       // 1-based
  studentId: string;
  nomeAluno: string;
  pontuacao: number;
  acertos: number;
  respondidoEm: string;
}
```

### `ProvaCapaDTO` (retorno de `POST /prova/{id}/capa`)

```ts
interface ProvaCapaDTO {
  capaUrl: string;  // data URI base64 já pronta para usar em <img [src]>
}
```

## Endpoints

### Minhas provas

```
GET /prova/minhas
200 OK -> ProvaResumoDTO[]
```

Lista todas as provas criadas pelo usuário logado (qualquer status, incluindo rascunhos), mais
recentes primeiro.

### Biblioteca de provas públicas

```
GET /prova/publicas
200 OK -> ProvaResumoDTO[]
```

Lista provas **publicadas** de outros usuários que o usuário logado tem permissão de ver
(`TODOS`, `GRUPO`, ou `AMIGOS` quando há amizade aceita com o criador). Não inclui as próprias
provas do usuário nem rascunhos de ninguém.

### Detalhe de uma prova

```
GET /prova/{id}
200 OK -> ProvaDetalheDTO
```

Usado tanto para abrir o formulário de edição quanto para a tela "Visualizar Prova". Inclui o
gabarito (`correctAnswer`/`comment`) — não usar essa resposta para renderizar a tela de resolução
do aluno, usar `GET /prova/{id}/questoes/aluno` para isso.

Erros: `404` (não existe), `403` (é rascunho de outro usuário, ou é `AMIGOS` e você não é amigo do
criador).

### Criar prova

```
POST /prova
Body: ProvaForm
201 Created -> ProvaDetalheDTO
```

Erros: `400` (campo obrigatório ausente, ou `status` diferente de `"Rascunho"` com `questoes`
vazio).

### Editar prova

```
PUT /prova/{id}
Body: ProvaForm
200 OK -> ProvaDetalheDTO
```

Substitui todos os dados da prova, **incluindo a lista de questões inteira** (não é um merge —
envie sempre o array completo de questões, mesmo as que não mudaram). Funciona tanto para editar
uma prova publicada quanto para "salvar rascunho novamente".

Só o criador pode editar. Erros: `404`, `403` (não é o criador), `400`.

### Excluir prova

```
DELETE /prova/{id}
204 No Content
```

Só o criador pode excluir. Erros: `404`, `403`.

### Publicar rascunho

```
POST /prova/{id}/publicar
200 OK -> ProvaDetalheDTO
```

Alternativa a fazer isso via `PUT` mudando `status`. Só o criador pode publicar. Erros: `404`,
`403`, `409` (já estava publicada), `400` (prova sem nenhuma questão).

### Upload da capa

```
POST /prova/{id}/capa
Content-Type: multipart/form-data
Campo: file (PNG ou JPG, até 5MB)
200 OK -> ProvaCapaDTO
```

```ts
const formData = new FormData();
formData.append('file', arquivo);
this.http.post<ProvaCapaDTO>(`/prova/${id}/capa`, formData);
```

Só o criador pode alterar a capa. Erros: `404`, `403`, `400` (tipo de arquivo inválido ou maior
que 5MB).

> Alternativa: `capaUrl` também pode ser enviado direto como base64 dentro do `ProvaForm` em
> `POST`/`PUT /prova`, sem precisar desse endpoint — útil quando o wizard já monta a imagem como
> data URI no passo de "Informações" antes de a prova existir no backend.

### Buscar prova para o aluno responder

```
GET /prova/{id}/questoes/aluno
200 OK -> ProvaAlunoDTO
```

Só retorna dados quando a prova está com `status = "Ativo"` — fora dessa janela, retorna erro.
Erros: `404`, `403` (sem permissão de visibilidade), `409` (prova não está ativa no momento —
ainda não abriu, já encerrou, ou está em rascunho).

### Responder prova

```
POST /prova/{id}/responder
Body: ProvaResponderForm
200 OK -> ProvaResultadoDTO
```

Erros: `404`, `403`, `409` — três causas possíveis, diferenciáveis pelo campo `message`:
prova não está `"Ativo"`; usuário já respondeu essa prova antes (**apenas uma submissão por aluno
por prova é permitida**); ou `maxParticipantes` foi atingido.

### Ranking da prova

```
GET /prova/{id}/ranking
200 OK -> ProvaRankingDTO[]
```

Ordenado por pontuação decrescente (empate desempatado por quem respondeu primeiro). Erros:
`404`, `403`, `409` (`rankingDisponivel` é `false` — prova ainda não está `Ativo`/`Encerrado`).

## Tabela de erros

| Status | Quando acontece |
|---|---|
| `400 Bad Request` | campo obrigatório ausente/inválido no `ProvaForm`; tentar publicar prova sem questões; capa com tipo/tamanho inválido |
| `403 Forbidden` | acessar rascunho de outro usuário; acessar prova `AMIGOS` sem ser amigo do criador; editar/excluir/publicar/subir capa sem ser o criador |
| `404 Not Found` | id de prova não encontrado |
| `409 Conflict` | publicar prova já publicada; responder prova fora do status `Ativo`; responder prova já respondida antes; limite de participantes atingido; consultar ranking antes de `rankingDisponivel = true` |

## Fluxo de exemplo (Angular)

```ts
// 1. Criar como rascunho (wizard "Salvar rascunho" em qualquer passo)
this.http.post<ProvaDetalheDTO>('/prova', { ...form, status: 'Rascunho' });

// 2. Continuar editando o rascunho
this.http.put<ProvaDetalheDTO>(`/prova/${id}`, { ...form, status: 'Rascunho' });

// 3. Publicar ao final do wizard
this.http.put<ProvaDetalheDTO>(`/prova/${id}`, { ...form, status: 'Ativo' });
// (ou, se já tinha sido criada como rascunho antes: this.http.post(`/prova/${id}/publicar`, {}))

// 4. Tela "Minhas Provas"
this.http.get<ProvaResumoDTO[]>('/prova/minhas');

// 5. Tela "Visualizar Prova" (dono ou quem tem permissão de visibilidade)
this.http.get<ProvaDetalheDTO>(`/prova/${id}`);

// 6. Aluno resolvendo a prova
this.http.get<ProvaAlunoDTO>(`/prova/${id}/questoes/aluno`);
this.http.post<ProvaResultadoDTO>(`/prova/${id}/responder`, { respostas });

// 7. Ranking da prova
this.http.get<ProvaRankingDTO[]>(`/prova/${id}/ranking`);
```

## Fora do escopo desta versão

Os itens abaixo **não estão implementados** — não modelar telas assumindo que existem:

- Entidade/CRUD de "Grupo" — `visibilidade: "GRUPO"` é aceita, mas hoje se comporta de forma
  idêntica a `"TODOS"`.
- Compartilhamento por link não precisa de endpoint dedicado: `GET /prova/{id}` já respeita a
  regra de visibilidade, então o link `client/provas/visualizar/:id` do frontend funciona direto.
- Edição parcial de questões (PATCH por questão) — `PUT /prova/{id}` sempre substitui a lista
  inteira de questões.
- Retentativa de submissão: uma vez respondida, a prova não pode ser refeita pelo mesmo aluno.
