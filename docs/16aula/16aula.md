# Aula 16 — Ciclo de vida dos tokens: renovação, inatividade, rotação e revogação

[⬅ Voltar para o índice do curso](../../README.md)

---

## 1. Apresentação

Na Aula 13 aprendemos a representar uma autenticação por meio de um JWT assinado. Na Aula 14 transformamos os perfis desse token em decisões de autorização. Agora precisamos responder a uma questão que aparece em sistemas reais:

> Como oferecer uma sessão confortável sem deixar um token de acesso válido por tempo excessivo?

Um access token curto reduz a janela de uso de uma credencial roubada, mas obriga o usuário a autenticar-se novamente com frequência. Um refresh token permite obter um novo access token sem reenviar login e senha. Essa conveniência cria novas responsabilidades: persistência, expiração, rotação, revogação, concorrência e detecção de reutilização.

Nesta aula implementaremos um ciclo didático de sessão renovável. O projeto continuará usando o Spring Security como OAuth2 Resource Server para validar o access token, mas não será transformado em um Authorization Server completo.

### Problema orientador

> Como renovar a autenticação, encerrá-la e limitar sua duração sem transformar o access token em uma credencial permanente?

### Entrega da aula

Ao final, a aplicação terá:

- access token JWT com duração própria;
- refresh token opaco, aleatório e de uso único;
- apenas o hash do refresh token armazenado no banco;
- expiração por inatividade e expiração absoluta;
- rotação a cada renovação;
- identificação de uma família de tokens;
- revogação da família quando houver reutilização;
- logout da sessão atual e logout de todas as sessões;
- bloqueio da renovação quando a conta estiver inativa;
- migração Liquibase, contrato OpenAPI, coleção Postman e testes.

### Limite de escopo

Não implementaremos login social, OAuth2 Authorization Code, OpenID Connect, consentimento, descoberta de chaves ou um servidor de autorização completo. Esses recursos exigem protocolos e componentes próprios.

---

## 2. Resultados de aprendizagem

Ao concluir a aula, o estudante deverá ser capaz de:

1. diferenciar access token de refresh token;
2. explicar por que um refresh token introduz estado no servidor;
3. diferenciar expiração por inatividade de expiração absoluta;
4. justificar a rotação de refresh tokens;
5. explicar o que é uma família de tokens;
6. armazenar somente o hash de um token opaco;
7. usar `SecureRandom` para gerar credenciais imprevisíveis;
8. modelar o ciclo de uma sessão no domínio e no banco;
9. versionar o novo esquema com Liquibase;
10. impedir dois usos concorrentes do mesmo refresh token;
11. preservar uma revogação mesmo quando a requisição termina em `401`;
12. diferenciar logout local de logout global;
13. reconhecer a limitação de revogar apenas o refresh token;
14. testar regras temporais sem depender do relógio real;
15. testar o fluxo HTTP de login, renovação, reutilização e logout.

---

## 3. Pré-requisitos e marco inicial

Antes de começar, confirme:

```bash
git status
git tag --list
./mvnw clean verify
```

O ponto de partida esperado é a tag:

```text
aula-15-testes-cobertura
```

Também são necessários:

- Java 21;
- PostgreSQL do profile `test` disponível;
- `.env` configurado conforme as aulas anteriores;
- `JWT_SECRET` válido;
- compreensão das Aulas 12 a 15.

Crie a branch de trabalho usada pela sua turma, quando esse for o fluxo adotado:

```bash
git switch -c aula-16-refresh-token
```

---

## 4. Access token e refresh token

Os dois tokens possuem finalidades diferentes.

| Aspecto | Access token | Refresh token |
|---|---|---|
| Finalidade | acessar recursos protegidos | solicitar um novo par de tokens |
| Formato adotado | JWT assinado | valor opaco aleatório |
| Enviado para | APIs protegidas | somente endpoint de renovação/logout |
| Frequência de uso | em várias requisições | apenas quando necessário renovar |
| Duração desejada | curta | maior, mas limitada |
| Validação | assinatura e claims | consulta de hash e estado no banco |
| Revogação imediata neste projeto | não | sim |

O access token continua sendo apresentado no cabeçalho:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

O refresh token será enviado no corpo apenas aos endpoints de sessão:

```json
{
  "refreshToken": "valor-opaco-recebido-no-login"
}
```

O cliente não deve tentar interpretar o refresh token. Para ele, o valor é apenas uma credencial secreta.

### 4.1 Stateless e stateful

Um JWT de acesso pode ser validado por assinatura sem consultar uma sessão no banco. Por isso, essa validação é considerada *stateless*.

O refresh token desta aula é *stateful*. O servidor precisa consultar o banco para saber se ele:

- existe;
- já foi utilizado;
- foi revogado;
- expirou por inatividade;
- atingiu a expiração absoluta;
- pertence a uma conta que ainda pode autenticar.

Não existe contradição: um sistema pode usar access tokens stateless e manter estado apenas para renovação.

---

## 5. Três relógios diferentes

O sistema passa a observar três limites temporais.

### 5.1 Expiração do access token

É o claim `exp` do JWT. No projeto:

```properties
app.security.jwt.expiration=PT30M
```

`PT30M` significa trinta minutos no formato ISO-8601 de duração.

### 5.2 Expiração por inatividade

Define quanto tempo a sessão pode permanecer sem renovar:

```properties
app.security.refresh-token.idle-timeout=${REFRESH_TOKEN_IDLE_TIMEOUT:PT30M}
```

Cada rotação cria um novo refresh token e registra uma nova atividade. Se nenhuma renovação ocorrer dentro de trinta minutos, a família não poderá mais continuar.

### 5.3 Expiração absoluta

Limita a duração máxima da família, mesmo que ela permaneça ativa:

```properties
app.security.refresh-token.absolute-expiration=${REFRESH_TOKEN_ABSOLUTE_EXPIRATION:PT8H}
```

A renovação não desloca esse limite. Uma família criada às 08:00 expira às 16:00, ainda que tenha sido renovada às 15:59.

```text
08:00 login                         16:00 limite absoluto
  ├──── refresh ─── refresh ─── refresh ──┤
  └──────────── família da sessão ─────────┘
```

### 5.4 Regra de fronteira

Nesta implementação, o token expira ao atingir exatamente o limite:

```java
return !agora.isBefore(limite);
```

Assim, um limite `12:30:00Z` ainda aceita `12:29:59Z`, mas não aceita `12:30:00Z`.

---

## 6. Por que o refresh token é opaco?

Um refresh token poderia ser um JWT, mas isso não eliminaria a necessidade de estado se quisermos rotação, revogação e detecção de reutilização. Para tornar a aula clara, usamos um valor aleatório sem informações internas.

```java
private static final int TAMANHO_REFRESH_TOKEN_BYTES = 32;

private String gerarRefreshToken() {
    byte[] bytes = new byte[TAMANHO_REFRESH_TOKEN_BYTES];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes);
}
```

Decisões importantes:

- `SecureRandom` é apropriado para material de segurança;
- 32 bytes fornecem 256 bits antes da codificação;
- Base64 URL evita caracteres problemáticos em transporte;
- retirar o padding reduz caracteres sem reduzir a aleatoriedade original;
- o valor não contém login, perfil ou dado pessoal.

Não use `Random`, contador, data/hora ou UUID previsível como credencial.

---

## 7. Por que armazenar somente o hash?

Se o banco armazenasse o refresh token original, uma leitura indevida permitiria utilizá-lo imediatamente. O projeto calcula SHA-256 e persiste apenas o resultado hexadecimal:

```java
private String calcularHash(String refreshToken) {
    byte[] hash = MessageDigest.getInstance("SHA-256")
            .digest(refreshToken.getBytes(StandardCharsets.UTF_8));
    return HexFormat.of().formatHex(hash);
}
```

O fluxo é:

```text
cliente envia token
       ↓
aplicação calcula SHA-256
       ↓
consulta token_hash no banco
       ↓
valida o estado da sessão
```

### Hash de token não é hash de senha

Senhas humanas possuem baixa entropia e exigem algoritmos deliberadamente lentos, como BCrypt. O refresh token é gerado aleatoriamente com alta entropia; por isso, um hash determinístico permite localizá-lo no banco sem guardar o segredo original.

O hash reduz impacto, mas não elimina a necessidade de proteger banco, backups, logs e respostas.

---

## 8. Família, rotação e detecção de reutilização

No login, o servidor cria uma família identificada por `familiaId`. Toda renovação mantém a família, invalida o refresh token apresentado e cria outro.

```text
família S1

R1 ──uso──▶ R2 ──uso──▶ R3
│           │           │
usado       usado       atual
```

Se `R1` aparecer novamente, há duas possibilidades:

1. um cliente legítimo repetiu uma requisição antiga;
2. alguém copiou o token e está tentando reutilizá-lo.

O servidor não consegue distinguir com segurança. A resposta conservadora é revogar toda a família:

```text
R1 reutilizado
      ↓
revogar S1 inteira
      ↓
R2/R3 também não renovam
      ↓
nova autenticação será necessária
```

Essa estratégia é chamada de *refresh token rotation* com detecção de replay.

---

## 9. Modelo da sessão

Crie o enum de motivos:

```java
public enum MotivoRevogacaoRefreshToken {
    LOGOUT,
    LOGOUT_GLOBAL,
    INATIVIDADE,
    EXPIRACAO_ABSOLUTA,
    REUTILIZACAO,
    CONTA_INATIVA
}
```

A entidade `SessaoRefreshToken` registra cada geração dentro da família:

```java
@Entity
@Table(name = "sessao_refresh_token")
public class SessaoRefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ContaAcesso conta;

    private String familiaId;
    private String tokenHash;
    private Instant criadaEm;
    private Instant ultimaAtividadeEm;
    private Instant expiraEm;
    private Instant utilizadaEm;
    private Instant revogadaEm;

    @Enumerated(EnumType.STRING)
    private MotivoRevogacaoRefreshToken motivoRevogacao;
}
```

### Significado dos campos

| Campo | Responsabilidade |
|---|---|
| `familiaId` | agrupar todas as rotações da mesma sessão |
| `tokenHash` | localizar o token sem persistir o segredo |
| `criadaEm` | registrar quando esta geração nasceu |
| `ultimaAtividadeEm` | base do limite de inatividade desta geração |
| `expiraEm` | limite absoluto herdado da família |
| `utilizadaEm` | provar que o token já participou de uma rotação |
| `revogadaEm` | impedir usos posteriores |
| `motivoRevogacao` | explicar por que a sessão foi encerrada |

### Estados possíveis

```mermaid
stateDiagram-v2
    [*] --> Ativo: login ou rotação
    Ativo --> Utilizado: refresh aceito
    Ativo --> Revogado: logout / conta inativa
    Ativo --> Expirado: inatividade / limite absoluto
    Utilizado --> FamiliaRevogada: reutilização detectada
    Revogado --> [*]
    Expirado --> [*]
    FamiliaRevogada --> [*]
```

Um registro utilizado não é apagado imediatamente. Ele é evidência necessária para reconhecer uma tentativa posterior de replay.

---

## 10. Versionando o banco com Liquibase

Crie `src/main/resources/db/changelog/changes/007-sessoes-refresh-token.yaml`.

O primeiro changeSet cria a tabela e a chave estrangeira:

```yaml
- changeSet:
    id: 007-01-create-sessao-refresh-token
    author: curso-spring-2026
    changes:
      - createTable:
          tableName: sessao_refresh_token
          columns:
            - column:
                name: id
                type: BIGINT
                autoIncrement: true
                constraints:
                  primaryKey: true
                  nullable: false
            - column:
                name: conta_acesso_id
                type: BIGINT
                constraints:
                  nullable: false
            - column:
                name: familia_id
                type: VARCHAR(36)
                constraints:
                  nullable: false
            - column:
                name: token_hash
                type: VARCHAR(64)
                constraints:
                  nullable: false
                  unique: true
            - column:
                name: criada_em
                type: TIMESTAMP WITH TIME ZONE
                constraints:
                  nullable: false
            - column:
                name: ultima_atividade_em
                type: TIMESTAMP WITH TIME ZONE
                constraints:
                  nullable: false
            - column:
                name: expira_em
                type: TIMESTAMP WITH TIME ZONE
                constraints:
                  nullable: false
            - column:
                name: utilizada_em
                type: TIMESTAMP WITH TIME ZONE
            - column:
                name: revogada_em
                type: TIMESTAMP WITH TIME ZONE
            - column:
                name: motivo_revogacao
                type: VARCHAR(30)
      - addForeignKeyConstraint:
          baseTableName: sessao_refresh_token
          baseColumnNames: conta_acesso_id
          referencedTableName: conta_acesso
          referencedColumnNames: id
          constraintName: fk_sessao_refresh_token_conta
          onDelete: CASCADE
```

O arquivo do projeto também cria índices por família e conta e constraints para:

- exigir `expira_em > criada_em`;
- limitar os valores de `motivo_revogacao`;
- garantir unicidade de `token_hash`.

Inclua o arquivo no changelog mestre:

```yaml
- include:
    file: db/changelog/changes/007-sessoes-refresh-token.yaml
```

### Por que não usar `ddl-auto=update`?

O Hibernate continua apenas validando o esquema. A migração explicita a decisão, pode ser revisada, reproduzida e aplicada na mesma ordem em todos os ambientes.

### Checkpoint 1 — migração

```bash
./mvnw test -Dtest=PersistenciaJpaTest
```

O banco de teste deve reconhecer 36 changeSets.

---

## 11. Configurações tipadas

Crie `RefreshTokenProperties`:

```java
@ConfigurationProperties(prefix = "app.security.refresh-token")
public record RefreshTokenProperties(
        Duration idleTimeout,
        Duration absoluteExpiration) {

    public RefreshTokenProperties {
        if (idleTimeout == null || idleTimeout.isNegative() || idleTimeout.isZero()) {
            throw new IllegalArgumentException("Idle timeout deve ser positivo");
        }
        if (absoluteExpiration == null
                || absoluteExpiration.compareTo(idleTimeout) <= 0) {
            throw new IllegalArgumentException(
                    "Expiração absoluta deve ser maior que o idle timeout");
        }
    }
}
```

Habilite a classe na configuração de segurança e adicione ao `.env.example`:

```dotenv
REFRESH_TOKEN_IDLE_TIMEOUT=PT30M
REFRESH_TOKEN_ABSOLUTE_EXPIRATION=PT8H
```

Essas variáveis não são segredos, mas ficam no `.env` para demonstrar configuração por ambiente. Os valores padrão de desenvolvimento permanecem documentados em `application.properties`.

---

## 12. Consulta bloqueante e concorrência

Dois pedidos de renovação podem chegar quase ao mesmo tempo com o mesmo token. Sem coordenação, ambos poderiam observá-lo como disponível e gerar dois sucessores.

O repository usa lock pessimista:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("""
        select sessao
        from SessaoRefreshToken sessao
        join fetch sessao.conta conta
        join fetch conta.pessoa
        where sessao.tokenHash = :tokenHash
        """)
Optional<SessaoRefreshToken> buscarPorHashParaAtualizacao(String tokenHash);
```

Dentro da transação:

1. a primeira requisição bloqueia a linha;
2. marca o token como utilizado;
3. cria o sucessor;
4. confirma a transação;
5. a segunda requisição prossegue e encontra o token utilizado;
6. a família é revogada.

O lock protege uma decisão de negócio, não apenas uma gravação.

---

## 13. Serviço de sessão

O login inicia uma família:

```java
@Transactional
public TokensAutenticacao iniciar(Authentication authentication) {
    ContaAcesso conta = contaRepository
            .findByLoginIgnoreCase(authentication.getName())
            .orElseThrow(RefreshTokenInvalidoException::new);

    Instant agora = clock.instant();
    String familiaId = UUID.randomUUID().toString();
    Instant expiraEm = agora.plus(properties.absoluteExpiration());

    return emitirPar(conta, authentication, familiaId, agora, expiraEm);
}
```

A renovação preserva família e limite absoluto:

```java
@Transactional(noRollbackFor = RefreshTokenInvalidoException.class)
public TokensAutenticacao renovar(String refreshToken) {
    String hash = calcularHash(refreshToken);
    SessaoRefreshToken sessao = sessaoRepository
            .buscarPorHashParaAtualizacao(hash)
            .orElseThrow(RefreshTokenInvalidoException::new);

    Instant agora = clock.instant();
    validarSessaoParaRenovacao(sessao, agora);
    sessao.marcarUtilizada(agora);

    Authentication authentication = criarAuthentication(sessao.getConta());
    return emitirPar(
            sessao.getConta(),
            authentication,
            sessao.getFamiliaId(),
            agora,
            sessao.getExpiraEm());
}
```

### 13.1 Por que `noRollbackFor` é decisivo?

Quando uma reutilização é detectada, o caso de uso precisa realizar duas ações:

1. revogar a família;
2. responder `401` lançando uma exceção.

Por padrão, uma exceção de execução provoca rollback. Sem a exceção configurada em `noRollbackFor`, a revogação seria desfeita justamente quando mais precisamos preservá-la.

```text
UPDATE revoga família
        ↓
lança RefreshTokenInvalidoException
        ↓
sem noRollbackFor: ROLLBACK incorreto
com noRollbackFor: COMMIT da revogação + HTTP 401
```

Esse é um exemplo de como semântica transacional e segurança estão ligadas.

### 13.2 Conta inativa

Os perfis usados no novo JWT são lidos novamente da conta. Antes de emitir o par, o serviço verifica `conta.podeAutenticar()`. Assim, bloqueio ou inativação impede uma nova renovação.

---

## 14. Claim de sessão no access token

O `TokenService` recebe o identificador da família e adiciona o claim `sid`:

```java
JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer(properties.issuer())
        .issuedAt(agora)
        .expiresAt(expiraEm)
        .subject(authentication.getName())
        .claim("perfis", perfis)
        .claim("sid", sessaoId)
        .build();
```

O claim correlaciona o access token à sessão que o originou. Nesta aula ele fornece rastreabilidade e prepara o modelo para auditoria. A API ainda não consulta uma denylist por `sid` em cada requisição.

---

## 15. Contratos HTTP

### 15.1 Login

```http
POST /api/autenticacao/login
Content-Type: application/json
```

```json
{
  "login": "operador.postman",
  "senha": "senha-didatica-postman-2026"
}
```

Resposta `200 OK`:

```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 1800,
  "expiresAt": "2026-10-29T13:30:00Z",
  "refreshToken": "valor-opaco",
  "refreshTokenExpiresAt": "2026-10-29T21:00:00Z"
}
```

### 15.2 Renovação

```http
POST /api/autenticacao/refresh
Content-Type: application/json
```

```json
{
  "refreshToken": "valor-opaco-atual"
}
```

A resposta possui o mesmo formato do login. O cliente deve substituir atomicamente os dois tokens antigos pelos novos.

### 15.3 Logout local

```http
POST /api/autenticacao/logout
Content-Type: application/json
```

```json
{
  "refreshToken": "valor-opaco-atual"
}
```

Resposta: `204 No Content`.

O endpoint não exige access token e não informa se o refresh token existia. Isso permite ao cliente tentar limpar uma sessão mesmo quando seu access token já expirou.

### 15.4 Logout global

```http
POST /api/autenticacao/logout-todos
Authorization: Bearer <access-token>
```

Resposta: `204 No Content`. Todas as famílias ainda ativas da conta são revogadas.

### 15.5 Resposta genérica

Token ausente, desconhecido, expirado, revogado ou pertencente a conta inativa produz a mesma mensagem:

```json
{
  "status": 401,
  "message": "Refresh token inválido ou expirado"
}
```

Evitar detalhes reduz informação oferecida a quem testa credenciais roubadas.

---

## 16. Diagramas de sequência

### 16.1 Login

```mermaid
sequenceDiagram
    actor Cliente
    participant API as AutenticacaoController
    participant Auth as AuthenticationManager
    participant Sessao as SessaoAutenticacaoService
    participant Banco as PostgreSQL
    participant JWT as TokenService

    Cliente->>API: POST /login (login, senha)
    API->>Auth: autenticar credenciais
    Auth-->>API: Authentication
    API->>Sessao: iniciar(authentication)
    Sessao->>Sessao: gerar refresh opaco e hash
    Sessao->>Banco: INSERT sessão
    Sessao->>JWT: emitir access token com sid
    JWT-->>Sessao: JWT assinado
    Sessao-->>API: par de tokens
    API-->>Cliente: 200 access + refresh
```

### 16.2 Renovação normal

```mermaid
sequenceDiagram
    actor Cliente
    participant API
    participant Sessao as SessaoAutenticacaoService
    participant Banco as PostgreSQL
    participant JWT as TokenService

    Cliente->>API: POST /refresh (R1)
    API->>Sessao: renovar(R1)
    Sessao->>Sessao: SHA-256(R1)
    Sessao->>Banco: SELECT ... FOR UPDATE
    Banco-->>Sessao: sessão ativa
    Sessao->>Banco: marcar R1 utilizado
    Sessao->>Banco: INSERT R2 na mesma família
    Sessao->>JWT: emitir novo access token
    Sessao-->>API: access novo + R2
    API-->>Cliente: 200
```

### 16.3 Reutilização

```mermaid
sequenceDiagram
    actor ClienteOuAtacante
    participant API
    participant Sessao as SessaoAutenticacaoService
    participant Banco as PostgreSQL

    ClienteOuAtacante->>API: POST /refresh (R1 antigo)
    API->>Sessao: renovar(R1)
    Sessao->>Banco: buscar hash com lock
    Banco-->>Sessao: R1 utilizada
    Sessao->>Banco: revogar família inteira
    Note over Sessao,Banco: revogação confirma mesmo com a exceção
    Sessao-->>API: RefreshTokenInvalidoException
    API-->>ClienteOuAtacante: 401
```

### 16.4 Logout

```mermaid
sequenceDiagram
    actor Cliente
    participant API
    participant Sessao as SessaoAutenticacaoService
    participant Banco as PostgreSQL

    Cliente->>API: POST /logout (refresh atual)
    API->>Sessao: encerrar(refresh)
    Sessao->>Banco: localizar hash com lock
    Sessao->>Banco: revogar família
    API-->>Cliente: 204
    Note over Cliente: apagar tokens locais
```

---

## 17. Limitação consciente: o access token já emitido

Depois do logout, o refresh token não pode criar outro par. Entretanto, o access token já emitido continuará válido até seu próprio `exp`.

Isso acontece porque o Resource Server valida localmente:

- assinatura;
- emissor;
- instante de expiração;
- claims necessários.

Ele não consulta `sessao_refresh_token` em todas as requisições.

Para revogação imediata do access token seria necessário acrescentar alguma forma de estado à validação, por exemplo:

- denylist de `jti` ou `sid`;
- introspecção de token;
- consulta de versão da sessão/conta;
- access tokens ainda mais curtos.

Cada alternativa acrescenta custo, disponibilidade e decisões de cache. Nesta aula preferimos explicitar o compromisso arquitetural:

```text
logout = impedir renovação + apagar credenciais no cliente
access token emitido = válido até expirar
```

---

## 18. Testes de domínio e tempo controlado

Regras temporais não devem depender de `sleep`. O teste usa instantes fixos:

```java
private static final Instant CRIACAO =
        Instant.parse("2026-10-29T12:00:00Z");

@Test
void deveExpirarAoAtingirOLimiteDeInatividade() {
    SessaoRefreshToken sessao = novaSessao(
            CRIACAO.plus(Duration.ofHours(8)));

    assertFalse(sessao.expirouPorInatividade(
            CRIACAO.plus(Duration.ofMinutes(29)),
            Duration.ofMinutes(30)));

    assertTrue(sessao.expirouPorInatividade(
            CRIACAO.plus(Duration.ofMinutes(30)),
            Duration.ofMinutes(30)));
}
```

O teste verifica os dois lados da fronteira e executa rapidamente de forma determinística.

O código de produção recebe `Clock` por injeção. Isso separa a regra temporal do relógio global e permite, em testes específicos, usar `Clock.fixed(...)` ou um relógio controlável.

### Checkpoint 2 — domínio

```bash
./mvnw test -Dtest=SessaoRefreshTokenTest
```

---

## 19. Teste de integração do ciclo HTTP

`CicloTokenApiTest` executa o contexto completo com PostgreSQL e `MockMvc`.

O cenário de rotação comprova:

```java
TokensHttp primeiroPar = autenticar("rotacao.token");
TokensHttp segundoPar = renovar(primeiroPar.refreshToken());

assertNotEquals(
        primeiroPar.refreshToken(),
        segundoPar.refreshToken());

renovarEsperandoNaoAutorizado(primeiroPar.refreshToken());
renovarEsperandoNaoAutorizado(segundoPar.refreshToken());
```

O último passo é essencial: primeiro reutilizamos `R1`; depois verificamos que `R2`, embora fosse o token atual, também foi revogado porque pertence à mesma família.

O cenário de logout documenta a decisão arquitetural:

```java
logout(tokens.refreshToken());
renovarEsperandoNaoAutorizado(tokens.refreshToken());

mockMvc.perform(get("/api/contas-acesso/me")
        .header(AUTHORIZATION, "Bearer " + tokens.accessToken()))
        .andExpect(status().isOk());
```

### Checkpoint 3 — ciclo completo

```bash
./mvnw test \
  -Dtest=SessaoRefreshTokenTest,CicloTokenApiTest,PersistenciaJpaTest
```

---

## 20. Laboratório com Postman

Importe novamente:

```text
postman/Suporte-OS.postman_collection.json
```

Configure `adminPassword` e execute a coleção em ordem. A pasta **02 — Autorização e ciclo de tokens** realiza:

1. login do administrador e captura dos dois tokens;
2. criação da pessoa e da conta OPERADOR;
3. login do operador;
4. confirmação de uma rota autorizada;
5. confirmação do `403` em uma operação administrativa;
6. renovação e substituição dos tokens da coleção;
7. logout da sessão atual;
8. tentativa de renovação, esperando `401`;
9. continuidade das chamadas protegidas com o access token já emitido.

### Teste manual isolado

Após o login, salve o refresh token e envie:

```json
{
  "refreshToken": "{{refreshToken}}"
}
```

para `/api/autenticacao/refresh`. Copie o novo token. Se tentar utilizar o token anterior outra vez, toda a família será encerrada e nem o novo poderá renovar.

### Cuidado no cliente

Ao receber a resposta da renovação, o cliente deve substituir o par de maneira consistente. Manter dois processos renovando simultaneamente pode parecer, para o servidor, uma reutilização suspeita.

---

## 21. Diagnóstico

| Sintoma | Causa provável | Como verificar | Correção |
|---|---|---|---|
| `401` no primeiro refresh | token copiado incorretamente ou já expirado | conferir corpo e horários | repetir login e usar o valor completo |
| segundo refresh com o token antigo encerra o atual | comportamento de segurança esperado | observar a família no banco | autenticar novamente |
| logout retorna `204`, mas Bearer ainda funciona | access token ainda não expirou | decodificar claim `exp` | compreender o limite ou reduzir sua duração |
| revogação desaparece após `401` | rollback da exceção | consultar `revogada_em` | manter `noRollbackFor` |
| dois refreshes geram sucessores | ausência de lock/transação | executar chamadas concorrentes | usar `PESSIMISTIC_WRITE` |
| aplicação não inicia | duração inválida | observar mensagem de binding | usar ISO-8601 e absoluto maior que idle |
| Hibernate acusa tabela ausente | changelog 007 não incluído | consultar `databasechangelog` | incluir migração no master |
| refresh de conta bloqueada funciona | estado da conta não revalidado | bloquear conta entre login e refresh | testar `podeAutenticar()` |

Consultas úteis em ambiente de desenvolvimento:

```sql
SELECT id,
       conta_acesso_id,
       familia_id,
       criada_em,
       utilizada_em,
       revogada_em,
       motivo_revogacao
FROM sessao_refresh_token
ORDER BY id;
```

Não exponha `token_hash` em logs, DTOs ou telas administrativas.

---

## 22. Segurança: decisões e ameaças

| Ameaça | Controle desta aula | Risco residual |
|---|---|---|
| leitura indevida do banco | somente hash persistido | sessão e metadados ainda exigem proteção |
| roubo de refresh token | rotação e replay revogam família | primeiro uso do invasor pode vencer a corrida |
| sessão esquecida | timeout por inatividade | depende de política coerente |
| sessão renovada indefinidamente | expiração absoluta | novo login ainda pode ser realizado |
| conta bloqueada | revalidação no refresh | access token atual dura até `exp` |
| duas renovações simultâneas | lock pessimista | cliente precisa coordenar sua renovação |
| enumeração de tokens | resposta genérica | monitoramento será aprofundado na Aula 17 |
| roubo de access token | expiração curta | continua válido após logout até expirar |

Boas práticas adicionais:

- use HTTPS fora do ambiente local;
- nunca grave tokens em logs;
- não envie refresh token para APIs de negócio;
- limite CORS conforme o cliente real;
- em aplicações web, avalie cookies `HttpOnly`, `Secure` e política `SameSite`;
- trate XSS e CSRF de acordo com o local escolhido para armazenar/enviar credenciais;
- remova sessões antigas por uma política de retenção auditável.

---

## 23. Desenvolvimento incremental e pontos de quebra

Cada checkpoint deve compilar e possuir uma finalidade observável. Isso permite gerar commits intermediários durante a aula e localizar erros.

### Ponto de quebra A — modelo e migração

- enum de motivos;
- entidade da sessão;
- changelog 007;
- teste de persistência atualizado.

Mensagem sugerida:

```text
feat: modelar sessões de refresh token
```

### Ponto de quebra B — propriedades e repository

- durações tipadas;
- hash único;
- busca com lock;
- revogação por família e conta.

Mensagem sugerida:

```text
feat: persistir e bloquear refresh tokens
```

### Ponto de quebra C — emissão e renovação

- par de tokens;
- `sid` no access token;
- rotação;
- expiração e revalidação da conta.

Mensagem sugerida:

```text
feat: implementar rotação de refresh token
```

### Ponto de quebra D — logout e API

- endpoints `/refresh`, `/logout` e `/logout-todos`;
- tratamento genérico de token inválido;
- OpenAPI e Postman.

Mensagem sugerida:

```text
feat: concluir ciclo HTTP de sessões
```

### Ponto de quebra E — evidências

- testes de domínio;
- teste integrado do ciclo;
- documentação da aula;
- `clean verify` aprovado.

Mensagem sugerida para o marco consolidado:

```text
feat: concluir aula 16 sobre ciclo de tokens
```

---

## 24. Verificação final

Execute:

```bash
./mvnw clean verify
```

Verifique também:

```bash
git diff --check
git status --short
```

Checklist técnico:

- [ ] o Liquibase aplicou 36 changeSets;
- [ ] login devolve access e refresh token;
- [ ] o banco não armazena o refresh token original;
- [ ] refresh produz um par diferente;
- [ ] o token antigo não pode ser reutilizado;
- [ ] replay revoga a família;
- [ ] inatividade e limite absoluto são verificados;
- [ ] conta inativa não renova;
- [ ] logout local revoga uma família;
- [ ] logout global revoga todas as famílias da conta;
- [ ] access token atual permanece válido até `exp`;
- [ ] contrato OpenAPI contém os novos endpoints;
- [ ] coleção Postman é JSON válido;
- [ ] cobertura permanece acima do portão definido na Aula 15.

---

## 25. Transferência para o projeto temático

Implemente o mesmo ciclo no tema escolhido na Aula 02. A solução deve possuir:

- conta ou identidade autenticável;
- access token curto;
- refresh token opaco;
- hash persistido;
- rotação de uso único;
- limite de inatividade;
- limite absoluto;
- revogação local e global;
- migração de banco;
- testes de domínio e do fluxo HTTP.

O estudante deverá entregar também uma decisão arquitetural de até uma página respondendo:

1. Onde o cliente guardará o access token?
2. Onde guardará o refresh token?
3. Quais durações foram escolhidas e por quê?
4. O que acontece com um access token após logout?
5. Qual ameaça a rotação reduz?
6. Como duas renovações simultâneas são tratadas?

Não copie nomes de domínio do Suporte OS quando eles não fizerem sentido no projeto temático.

---

## 26. Atividade orientada

Em duplas:

1. desenhe uma linha do tempo com login, duas renovações, inatividade e limite absoluto;
2. identifique qual geração de refresh token está ativa em cada instante;
3. simule a reutilização da primeira geração;
4. explique por que a geração mais recente também precisa ser revogada;
5. execute o cenário no Postman;
6. consulte os registros da família no PostgreSQL;
7. compare `utilizada_em`, `revogada_em` e `motivo_revogacao`;
8. demonstre que o access token atual ainda funciona após logout;
9. proponha uma alternativa para revogação imediata e apresente seu custo.

### Evidências

- diagrama ou linha do tempo;
- requisições e status HTTP;
- consulta do banco sem exibir hashes;
- explicação do limite arquitetural;
- execução dos testes.

---

## 27. Atividade autônoma

Implemente um teste adicional para a seguinte regra:

> Uma conta bloqueada depois do login não pode renovar sua sessão.

O teste deverá:

1. criar uma conta ativa;
2. autenticar e capturar o refresh token;
3. bloquear a conta;
4. tentar renovar;
5. esperar `401`;
6. verificar no banco o motivo `CONTA_INATIVA`;
7. justificar por que o access token existente não é imediatamente revogado.

Extensão: crie um teste concorrente com duas transações tentando usar o mesmo refresh token e descreva o resultado esperado.

---

## 28. Avaliação sugerida

| Critério | Peso | Evidência esperada |
|---|---:|---|
| fundamentação conceitual | 20% | diferencia tokens, limites e estado |
| modelo e migração | 15% | tabela, constraints, índices e Liquibase |
| segurança do segredo | 15% | aleatoriedade e hash sem exposição |
| rotação e reutilização | 20% | família, uso único e revogação correta |
| transação e concorrência | 10% | lock e preservação da revogação |
| testes | 15% | fronteiras temporais e fluxo HTTP |
| documentação e Git | 5% | decisões, commits e marco reproduzível |

Erros críticos:

- persistir refresh token em texto puro;
- aceitar reutilização sem revogar a família;
- renovar sessão de conta inativa;
- remover o limite absoluto;
- afirmar que logout revoga imediatamente o JWT sem implementar essa verificação;
- ignorar rollback da revogação;
- reduzir testes ou cobertura apenas para fazer o build passar.

---

## 29. Orientações ao professor

Antes da aula:

- confirme o PostgreSQL de teste;
- execute `clean verify`;
- prepare dois clientes ou abas para demonstrar rotação;
- deixe uma consulta da tabela de sessões disponível;
- evite exibir tokens completos em projeção ou gravação.

Durante a aula:

- desenhe primeiro os três relógios;
- peça que a turma preveja o resultado de reutilizar `R1`;
- demonstre o problema de rollback antes de explicar `noRollbackFor`;
- relacione o lock com a Aula 11;
- contraste BCrypt para senha com SHA-256 para token aleatório;
- encerre mostrando a limitação do access token após logout.

Perguntas para discussão:

1. Por que não ampliar o access token para oito horas e eliminar o refresh?
2. Por que guardar registros já utilizados?
3. O que muda se duas instâncias da aplicação processarem refreshes?
4. Qual é o custo de consultar uma sessão em toda requisição?
5. Em qual situação um cookie seria preferível ao armazenamento JavaScript?
6. Por quanto tempo registros revogados devem ser preservados?

---

## 30. Referências

- IETF. [RFC 9700 — Best Current Practice for OAuth 2.0 Security](https://www.rfc-editor.org/rfc/rfc9700.html).
- OWASP. [Session Management Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html).
- Spring Security. [OAuth 2.0 Resource Server JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html).
- Java Platform. APIs `SecureRandom`, `MessageDigest`, `Clock`, `Instant` e `Duration`.

---

## 31. Marco da aula

Depois que material, código e testes forem revisados:

```bash
git add .
git commit -m "feat: concluir aula 16 sobre ciclo de tokens"
git tag -a aula-16-refresh-token \
  -m "Aula 16 - ciclo de vida dos tokens"
git push origin main
git push origin aula-16-refresh-token
```

Não crie a tag antes de confirmar o `clean verify`. A tag deve apontar para um estado reproduzível, com a migração, a API, os testes, o Postman e esta documentação sincronizados.
