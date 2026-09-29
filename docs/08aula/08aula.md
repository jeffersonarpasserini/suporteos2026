# Java – Spring Boot – Aula 08 – CRUD completo, estoque e consultas paginadas

[⬅ Voltar para o índice](../../README.md)

---

## Apresentação

Na Aula 07 a aplicação passou a oferecer cadastro, consulta por identificador e listagem de grupos, fornecedores e produtos. Esse primeiro contrato permitiu estudar DTOs, mapeadores, controllers, códigos HTTP e testes com MockMvc e Postman.

Nesta aula completaremos os casos de uso de **grupo de produto** e **produto**. Serão adicionados alteração com `PUT`, exclusão com `DELETE`, ativação e inativação, entrada e saída de estoque, pesquisa por filtros, ordenação e paginação.

O objetivo não é apenas criar mais rotas. Cada operação será posicionada na camada correta e terá uma semântica HTTP explícita. Ao final, o estudante deverá conseguir justificar por que atualizar um cadastro usa `PUT`, por que uma movimentação de estoque usa `POST` e por que inativar não é equivalente a excluir.

## Problema orientador

Como evoluir uma API de cadastro para que ela possa atualizar, desativar, excluir, movimentar estoque e pesquisar grandes conjuntos de dados sem expor entidades JPA, sem transferir regras de negócio para o controller e sem carregar todos os registros na memória?

## Resultados de aprendizagem

Ao concluir esta aula, o estudante deverá ser capaz de:

1. diferenciar substituição de dados, mudança de estado, movimentação e exclusão;
2. explicar a idempotência de `PUT` e a não idempotência de uma movimentação;
3. implementar atualização sem permitir que o cliente altere campos controlados pelo servidor;
4. manter os dois lados de uma associação bidirecional durante a troca de grupo;
5. impedir a exclusão de um grupo que ainda possui produtos;
6. implementar ativação e inativação como transições explícitas;
7. implementar entrada e saída de estoque preservando as invariantes do domínio;
8. construir filtros combináveis com `Specification`;
9. aplicar paginação e ordenação com `Pageable`;
10. devolver um contrato de página estável, sem expor diretamente objetos internos do Spring;
11. proteger a consulta com limite de página e campos de ordenação permitidos;
12. testar caminhos de sucesso e de erro pela interface HTTP.

## Pré-requisitos

- Aulas 00 a 07 concluídas;
- Java 21 e Maven Wrapper funcionando;
- PostgreSQL em execução;
- bancos `suporteos2026_dev` e `suporteos2026_test` disponíveis;
- `.env` local preenchido;
- compreensão de DTO, controller, service, repository, JPA e transação.

Confirme o ponto inicial:

```bash
git status
./mvnw test
```

Se os testes falharem por conexão, não altere o código para esconder o problema. Confira PostgreSQL, nome do banco, usuário, senha e profile ativo.

---

## 1. Contrato que será desenvolvido

### 1.1 Grupos de produto

| Operação | Método e rota | Resultado |
|---|---|---|
| cadastrar | `POST /api/grupos-produtos` | `201 Created` |
| consultar | `GET /api/grupos-produtos/{id}` | `200` ou `404` |
| pesquisar | `GET /api/grupos-produtos` | página com `200` |
| alterar | `PUT /api/grupos-produtos/{id}` | grupo atualizado |
| ativar/inativar | `PUT /api/grupos-produtos/{id}/status` | grupo atualizado |
| excluir | `DELETE /api/grupos-produtos/{id}` | `204`, `404` ou `409` |

### 1.2 Produtos

| Operação | Método e rota | Resultado |
|---|---|---|
| cadastrar | `POST /api/produtos` | `201 Created` |
| consultar | `GET /api/produtos/{id}` | `200` ou `404` |
| pesquisar | `GET /api/produtos` | página com `200` |
| alterar | `PUT /api/produtos/{id}` | produto atualizado |
| ativar/inativar | `PUT /api/produtos/{id}/status` | produto atualizado |
| registrar entrada | `POST /api/produtos/{id}/estoque/entradas` | saldo atualizado |
| registrar saída | `POST /api/produtos/{id}/estoque/saidas` | saldo atualizado ou `400` |
| excluir | `DELETE /api/produtos/{id}` | `204` ou `404` |

> [!IMPORTANT]
> A paginação altera o contrato das listagens da Aula 07. Antes, `GET /api/grupos-produtos` e `GET /api/produtos` devolviam um array. A partir desta aula, devolvem `PaginaResponse`, com `conteudo` e metadados. Clientes, testes e coleções que consumiam o array precisam ser atualizados conscientemente.

### 1.3 O que o `PUT` do produto pode alterar

O corpo de atualização contém a representação completa dos dados editáveis:

```json
{
  "descricao": "Mouse ergonômico sem fio",
  "valorUnitario": 129.90,
  "estoqueMinimo": 4.000,
  "grupoId": 2,
  "fornecedorId": null
}
```

O código de barras, o saldo, a data de cadastro e o status não aparecem nesse DTO:

- código de barras é a chave de negócio escolhida pelo curso;
- saldo muda somente por entrada ou saída;
- data de cadastro é definida pelo servidor;
- status muda somente pela operação específica de status.

Essa separação impede que um `PUT` genérico contorne regras importantes.

### 1.4 Ordem de construção da aula

Implemente o incremento nesta ordem para que cada falha permaneça localizada:

```text
1. comportamentos de GrupoProduto e Produto
2. testes unitários desses comportamentos
3. migração 004 e teste de integridade
4. DTOs de alteração, status e estoque
5. métodos adicionais dos repositories
6. services de alteração, exclusão, status e movimentação
7. Specifications, paginação e validação de ordenação
8. controllers e tratamento de erros
9. MockMvc, Postman e suíte completa
```

Não avance automaticamente quando um checkpoint falhar. Registre a evidência, identifique a camada e corrija a causa antes de acrescentar outro componente.

---

## 2. Semântica HTTP antes do código

### 2.1 Por que alteração usa `PUT`

`PUT` representa a substituição da representação editável do recurso naquele endereço. Enviar duas vezes exatamente o mesmo corpo deve deixar o cadastro no mesmo estado final. Essa propriedade é chamada **idempotência**.

```text
PUT descrição=A → descrição=A
PUT descrição=A novamente → descrição continua A
```

### 2.2 Por que estoque usa `POST`

Uma entrada de 5 unidades é um evento. Repetir a requisição registra mais 5 unidades:

```text
saldo 10 → entrada 5 → saldo 15
saldo 15 → entrada 5 → saldo 20
```

Portanto, a movimentação não é idempotente. Usamos `POST` em uma coleção conceitual de entradas ou saídas. Em um sistema futuro, cada movimento poderia se tornar uma entidade com ID, data, motivo e usuário.

### 2.3 Inativar não é excluir

| Inativação | Exclusão |
|---|---|
| mantém a linha e seu identificador | remove fisicamente a linha |
| preserva referências e histórico | pode ser impedida por relacionamentos |
| pode ser revertida por ativação | normalmente não é reversível pela API |
| usa alteração de status | usa `DELETE` |

Um grupo que possui produtos será apenas inativável. Sua exclusão retorna `409 Conflict`, pois a operação entra em conflito com o estado atual dos relacionamentos.

---

## 3. Checkpoint 1 – Evoluir o domínio

Controllers não devem escrever diretamente em atributos das entidades. Primeiro criamos comportamentos que protegem as invariantes.

### 3.1 Alterar o nome do grupo

Em `GrupoProduto`, adicione:

```java
public void alterarNome(String novoNome) {
    this.nome = validarTextoObrigatorio(
            novoNome,
            "Nome do grupo é obrigatório");
}
```

O método reutiliza a mesma validação empregada no construtor. Assim, criação e alteração obedecem à mesma regra.

### 3.2 Alterar os dados do produto

Em `Produto`, adicione comportamentos separados:

```java
public void alterarDescricao(String novaDescricao) {
    this.descricao = validarTextoObrigatorio(
            novaDescricao,
            "Descrição é obrigatória");
}

public void alterarValorUnitario(BigDecimal novoValor) {
    this.valorUnitario = validarNaoNegativo(
            novoValor,
            "Valor unitário não pode ser negativo");
}

public void alterarEstoqueMinimo(BigDecimal novoEstoqueMinimo) {
    this.estoqueMinimo = validarNaoNegativo(
            novoEstoqueMinimo,
            "Estoque mínimo não pode ser negativo");
}
```

Evite setters públicos genéricos. Os nomes dos métodos comunicam a intenção e concentram as regras.

### 3.3 Trocar o grupo mantendo a associação consistente

`Produto` é o lado dono da associação JPA porque possui `grupo_produto_id`. Entretanto, o modelo também mantém uma coleção em `GrupoProduto`. Ao trocar de grupo, os dois lados em memória precisam ser atualizados.

Em `GrupoProduto`, extraia operações internas:

```java
void validarInclusao(Produto produto) {
    boolean codigoJaUtilizado = produtos.stream()
            .anyMatch(item -> item != produto
                    && item.getCodigoBarras().equals(produto.getCodigoBarras()));

    if (codigoJaUtilizado) {
        throw new IllegalArgumentException(
                "Código de barras já utilizado no grupo");
    }
}

void adicionarInternamente(Produto produto) {
    if (!produtos.contains(produto)) {
        produtos.add(produto);
    }
}

void removerInternamente(Produto produto) {
    produtos.remove(produto);
}
```

Em `Produto`, implemente:

```java
public void alterarGrupo(GrupoProduto novoGrupo) {
    Objects.requireNonNull(
            novoGrupo,
            "Grupo de produto é obrigatório");

    if (this.grupo == novoGrupo) {
        return;
    }

    novoGrupo.validarInclusao(this);
    if (this.grupo != null) {
        this.grupo.removerInternamente(this);
    }
    this.grupo = novoGrupo;
    novoGrupo.adicionarInternamente(this);
}
```

O fornecedor é opcional; por isso, sua alteração aceita `null`:

```java
public void alterarFornecedor(Fornecedor fornecedor) {
    this.fornecedor = fornecedor;
}
```

### 3.4 Estoque e status já são comportamentos do domínio

Reutilize:

```java
produto.receberEstoque(quantidade);
produto.retirarEstoque(quantidade);
produto.ativar();
produto.inativar();
```

Não replique no service a comparação de saldo. O objeto `Produto` já sabe impedir quantidade não positiva e saída maior que o saldo.

### 3.5 Testar o domínio

Adicione testes para alteração de nome e dados editáveis. Execute apenas os testes rápidos:

```bash
./mvnw -Dtest=GrupoProdutoTest,ProdutoTest test
```

Checkpoint esperado: as regras funcionam sem iniciar Spring ou PostgreSQL.

---

## 4. Checkpoint 2 – Garantir unicidade no banco

O serviço verifica se o nome do grupo já existe, mas uma verificação em Java não substitui a constraint. Duas transações concorrentes podem consultar ao mesmo tempo e tentar inserir o mesmo nome.

Crie `004-unique-nome-grupo-produto.yaml`:

```yaml
databaseChangeLog:
  - changeSet:
      id: 004-01-unique-nome-normalizado-grupo-produto
      author: curso-spring-2026
      comment: Garante unicidade do nome sem diferenciar maiúsculas.
      changes:
        - sql:
            sql: >
              CREATE UNIQUE INDEX uk_grupo_produto_nome_lower
              ON grupo_produto (LOWER(BTRIM(nome)))
      rollback:
        - sql:
            sql: >
              DROP INDEX uk_grupo_produto_nome_lower
```

Inclua o arquivo no final do changelog mestre:

```yaml
  - include:
      file: db/changelog/changes/004-unique-nome-grupo-produto.yaml
```

### Por que um índice funcional?

Uma constraint simples sobre `nome` trataria `Papelaria` e `PAPELARIA` como valores diferentes no PostgreSQL. O índice sobre `LOWER(BTRIM(nome))` ignora caixa e espaços externos, protegendo a normalização adotada pelo serviço.

Antes de aplicar a migração em um banco existente, procure duplicidades:

```sql
SELECT LOWER(BTRIM(nome)), COUNT(*)
FROM grupo_produto
GROUP BY LOWER(BTRIM(nome))
HAVING COUNT(*) > 1;
```

Se houver resultados, decida conscientemente qual registro deve permanecer. Não apague dados automaticamente em uma migração didática sem conhecer as referências.

---

## 5. Checkpoint 3 – DTOs específicos

Não reutilize o DTO de cadastro quando o conjunto de campos permitidos é diferente.

### 5.1 Atualização de grupo

```java
public record GrupoProdutoAtualizacaoRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 120,
                message = "Nome deve possuir no máximo 120 caracteres")
        String nome) {
}
```

### 5.2 Atualização de produto

```java
public record ProdutoAtualizacaoRequest(
        @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 150,
                message = "Descrição deve possuir no máximo 150 caracteres")
        String descricao,

        @NotNull(message = "Valor unitário é obrigatório")
        @PositiveOrZero(message = "Valor unitário não pode ser negativo")
        BigDecimal valorUnitario,

        @NotNull(message = "Estoque mínimo é obrigatório")
        @PositiveOrZero(message = "Estoque mínimo não pode ser negativo")
        BigDecimal estoqueMinimo,

        @NotNull(message = "Grupo é obrigatório")
        @Positive(message = "Identificador do grupo deve ser positivo")
        Long grupoId,

        @Positive(message = "Identificador do fornecedor deve ser positivo")
        Long fornecedorId) {
}
```

### 5.3 Status

```java
public record StatusRequest(
        @NotNull(message = "Status é obrigatório")
        Status status) {
}
```

Corpos válidos:

```json
{ "status": "ATIVO" }
```

```json
{ "status": "INATIVO" }
```

Outro texto não pertence ao enum e produz `400 Bad Request` durante a leitura do JSON.

### 5.4 Movimentação de estoque

```java
public record MovimentacaoEstoqueRequest(
        @NotNull(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade deve ser maior que zero")
        BigDecimal quantidade) {
}
```

O DTO protege a fronteira HTTP; o domínio mantém a regra porque também pode ser chamado por outros adaptadores.

---

## 6. Checkpoint 4 – Repositories para pesquisa e integridade

### 6.1 Habilitar Specifications

Estenda `JpaSpecificationExecutor`:

```java
public interface ProdutoRepository extends
        JpaRepository<Produto, Long>,
        JpaSpecificationExecutor<Produto> {
}
```

Faça o mesmo em `GrupoProdutoRepository`.

Uma `Specification` representa um predicado de consulta. Ela permite começar sem filtros e acrescentar condições apenas quando o parâmetro foi informado.

### 6.2 Consultas auxiliares

Em `GrupoProdutoRepository`:

```java
boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
```

O `AndIdNot` evita considerar o próprio grupo como duplicado durante a alteração.

Em `ProdutoRepository`:

```java
boolean existsByGrupoId(Long grupoId);
```

Essa consulta permite produzir uma mensagem de conflito antes de tentar excluir um grupo referenciado.

### 6.3 Evitar associações lazy fora da transação

O mapper de produto lê grupo e fornecedor. Como `open-in-view=false`, a consulta paginada deve carregar esses relacionamentos ainda no repository:

```java
@Override
@EntityGraph(attributePaths = {"grupo", "fornecedor"})
Page<Produto> findAll(
        Specification<Produto> specification,
        Pageable pageable);
```

`@EntityGraph` descreve quais associações precisam estar disponíveis. Não reative Open Session in View para esconder uma consulta incompleta.

---

## 7. Checkpoint 5 – Serviços de aplicação

### 7.1 Atualizar um grupo

Normalize o texto antes de consultar e salvar:

```java
@Transactional
public GrupoProduto alterar(Long id, String nome) {
    String nomeNormalizado = nome == null ? null : nome.trim();

    if (repository.existsByNomeIgnoreCaseAndIdNot(
            nomeNormalizado, id)) {
        throw new RecursoDuplicadoException(
                "Nome do grupo já cadastrado");
    }

    GrupoProduto grupo = buscarEntidade(id);
    grupo.alterarNome(nomeNormalizado);
    return grupo;
}
```

Não é necessário chamar `save` para uma entidade carregada na mesma transação. O dirty checking detectará a alteração.

### 7.2 Alterar status

```java
@Transactional
public GrupoProduto alterarStatus(Long id, Status status) {
    GrupoProduto grupo = buscarEntidade(id);
    if (status == Status.ATIVO) {
        grupo.ativar();
    } else {
        grupo.inativar();
    }
    return grupo;
}
```

O produto usa a mesma estrutura.

### 7.3 Excluir grupo com verificação de uso

Crie `RecursoEmUsoException` e implemente:

```java
@Transactional
public void excluir(Long id) {
    GrupoProduto grupo = buscarEntidade(id);

    if (produtoRepository.existsByGrupoId(id)) {
        throw new RecursoEmUsoException(
                "Grupo de produto não pode ser excluído porque possui produtos");
    }

    repository.delete(grupo);
    repository.flush();
}
```

O `flush()` antecipa para dentro do caso de uso uma eventual violação de integridade. A constraint do banco continua sendo a proteção definitiva contra concorrência e acessos externos.

### 7.4 Atualizar produto

```java
@Transactional
public Produto alterar(
        Long id,
        String descricao,
        BigDecimal valorUnitario,
        BigDecimal estoqueMinimo,
        Long grupoId,
        Long fornecedorId) {

    Produto produto = buscarEntidade(id);
    GrupoProduto grupo = buscarGrupo(grupoId);
    Fornecedor fornecedor = fornecedorId == null
            ? null
            : buscarFornecedor(fornecedorId);

    produto.alterarDescricao(descricao);
    produto.alterarValorUnitario(valorUnitario);
    produto.alterarEstoqueMinimo(estoqueMinimo);
    produto.alterarGrupo(grupo);
    produto.alterarFornecedor(fornecedor);
    return produto;
}
```

Observe a ordem:

1. localiza o produto;
2. resolve IDs relacionados;
3. somente então altera a entidade;
4. deixa a transação confirmar tudo como uma unidade.

### 7.5 Entrada e saída de estoque

```java
@Transactional
public Produto receberEstoque(Long id, BigDecimal quantidade) {
    Produto produto = buscarEntidade(id);
    produto.receberEstoque(quantidade);
    return produto;
}

@Transactional
public Produto retirarEstoque(Long id, BigDecimal quantidade) {
    Produto produto = buscarEntidade(id);
    produto.retirarEstoque(quantidade);
    return produto;
}
```

Uma saída maior que o saldo lança `IllegalArgumentException`. O handler da API transformará essa regra conhecida em `400`, e não em `500`.

---

## 8. Checkpoint 6 – Filtros combináveis

### 8.1 Filtros de grupos

Parâmetros aceitos:

- `nome`: trecho do nome, sem diferenciar maiúsculas;
- `status`: `ATIVO` ou `INATIVO`.

No serviço:

```java
Specification<GrupoProduto> filtros =
        (root, query, cb) -> cb.conjunction();

if (nome != null && !nome.isBlank()) {
    String trecho = "%" + nome.trim().toLowerCase(Locale.ROOT) + "%";
    filtros = filtros.and((root, query, cb) ->
            cb.like(cb.lower(root.get("nome")), trecho));
}

if (status != null) {
    filtros = filtros.and((root, query, cb) ->
            cb.equal(root.get("status"), status));
}

return repository.findAll(filtros, pageable);
```

`cb.conjunction()` representa uma condição inicialmente verdadeira. Cada `and` acrescenta um filtro opcional.

### 8.2 Filtros de produtos

Parâmetros aceitos:

- `descricao`: trecho da descrição;
- `status`: `ATIVO` ou `INATIVO`;
- `grupoId`: grupo exato;
- `fornecedorId`: fornecedor exato;
- `abaixoEstoqueMinimo=true`: saldo menor que estoque mínimo.

O filtro de estoque compara duas colunas:

```java
if (Boolean.TRUE.equals(abaixoEstoqueMinimo)) {
    filtros = filtros.and((root, query, cb) ->
            cb.lessThan(
                    root.get("saldoEstoque"),
                    root.get("estoqueMinimo")));
}
```

O parâmetro ausente ou `false` não restringe a consulta.

O trecho isolado acima não é suficiente para montar o caso de uso. O método completo de `ProdutoService` fica assim:

```java
@Transactional(readOnly = true)
public Page<Produto> pesquisar(
        String descricao,
        Status status,
        Long grupoId,
        Long fornecedorId,
        Boolean abaixoEstoqueMinimo,
        Pageable pageable) {
    validarOrdenacao(pageable, Set.of(
            "id", "codigoBarras", "descricao", "saldoEstoque",
            "valorUnitario", "estoqueMinimo", "dataCadastro", "status"));

    Specification<Produto> filtros =
            (root, query, cb) -> cb.conjunction();

    if (descricao != null && !descricao.isBlank()) {
        String trecho = "%" + descricao.trim()
                .toLowerCase(Locale.ROOT) + "%";
        filtros = filtros.and((root, query, cb) ->
                cb.like(cb.lower(root.get("descricao")), trecho));
    }
    if (status != null) {
        filtros = filtros.and((root, query, cb) ->
                cb.equal(root.get("status"), status));
    }
    if (grupoId != null) {
        filtros = filtros.and((root, query, cb) ->
                cb.equal(root.get("grupo").get("id"), grupoId));
    }
    if (fornecedorId != null) {
        filtros = filtros.and((root, query, cb) ->
                cb.equal(root.get("fornecedor").get("id"), fornecedorId));
    }
    if (Boolean.TRUE.equals(abaixoEstoqueMinimo)) {
        filtros = filtros.and((root, query, cb) ->
                cb.lessThan(root.get("saldoEstoque"),
                        root.get("estoqueMinimo")));
    }

    return produtoRepository.findAll(filtros, pageable);
}
```

Imports novos desse arquivo:

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.util.Locale;
import java.util.Set;
```

Compile neste ponto. Erros em `root.get(...)` normalmente só aparecem durante a execução; por isso, além da compilação, o teste de pesquisa precisa iniciar o contexto JPA.

### 8.3 Por que não filtrar uma lista em Java?

Esta abordagem é inadequada:

```java
repository.findAll().stream()
        .filter(...)
        .toList();
```

Ela carrega todas as linhas e somente depois descarta dados. Com `Specification` e `Pageable`, PostgreSQL aplica `WHERE`, `ORDER BY`, `LIMIT` e `OFFSET`.

---

## 9. Checkpoint 7 – Paginação e ordenação

### 9.1 Parâmetros padrão do Spring

Uma rota com `Pageable` aceita:

```text
page=0
size=20
sort=descricao,asc
```

As páginas começam em zero. Para ordenar por mais de um campo, repita `sort`:

```text
?sort=status,asc&sort=descricao,asc
```

### 9.2 Limitar o tamanho máximo

Em `application.properties`:

```properties
spring.data.web.pageable.default-page-size=20
spring.data.web.pageable.max-page-size=100
```

Sem limite, um cliente poderia solicitar uma resposta excessivamente grande.

### 9.3 Validar campos de ordenação

Nunca aceite cegamente qualquer propriedade:

```java
private void validarOrdenacao(
        Pageable pageable,
        Set<String> camposPermitidos) {
    pageable.getSort().forEach(ordem -> {
        if (!camposPermitidos.contains(ordem.getProperty())) {
            throw new IllegalArgumentException(
                    "Campo de ordenação inválido: "
                            + ordem.getProperty());
        }
    });
}
```

Para grupos, permita `id`, `nome` e `status`. Para produtos, permita somente atributos simples definidos pelo contrato. Não permita caminhos arbitrários enviados pelo cliente.

### 9.4 Criar uma resposta de página estável

Evite devolver `Page` diretamente. Crie:

```java
public record PaginaResponse<T>(
        List<T> conteudo,
        int pagina,
        int tamanho,
        long totalElementos,
        int totalPaginas,
        boolean primeira,
        boolean ultima) {

    public static <S, T> PaginaResponse<T> de(
            Page<S> pagina,
            Function<S, T> conversor) {
        return new PaginaResponse<>(
                pagina.getContent().stream()
                        .map(conversor)
                        .toList(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages(),
                pagina.isFirst(),
                pagina.isLast());
    }
}
```

Exemplo de resposta:

```json
{
  "conteudo": [
    {
      "id": 8,
      "codigoBarras": "MOUSE-001",
      "descricao": "Mouse ergonômico"
    }
  ],
  "pagina": 0,
  "tamanho": 10,
  "totalElementos": 1,
  "totalPaginas": 1,
  "primeira": true,
  "ultima": true
}
```

Esse contrato pertence à aplicação e não muda acidentalmente quando a representação interna do Spring muda.

---

## 10. Checkpoint 8 – Controllers

### 10.1 Alteração, status e exclusão de grupo

```java
@PutMapping("/{id}")
public GrupoProdutoResponse alterar(
        @PathVariable Long id,
        @Valid @RequestBody GrupoProdutoAtualizacaoRequest request) {
    return mapper.toResponse(service.alterar(id, request.nome()));
}

@PutMapping("/{id}/status")
public GrupoProdutoResponse alterarStatus(
        @PathVariable Long id,
        @Valid @RequestBody StatusRequest request) {
    return mapper.toResponse(
            service.alterarStatus(id, request.status()));
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> excluir(@PathVariable Long id) {
    service.excluir(id);
    return ResponseEntity.noContent().build();
}
```

`204 No Content` não deve possuir corpo.

### 10.2 Pesquisa de grupos

```java
@GetMapping
public PaginaResponse<GrupoProdutoResponse> pesquisar(
        @RequestParam(required = false) String nome,
        @RequestParam(required = false) Status status,
        @PageableDefault(
                size = 20,
                sort = "id",
                direction = Sort.Direction.ASC)
        Pageable pageable) {
    return PaginaResponse.de(
            service.pesquisar(nome, status, pageable),
            mapper::toResponse);
}
```

### 10.3 Alteração de produto

```java
@PutMapping("/{id}")
public ProdutoResponse alterar(
        @PathVariable Long id,
        @Valid @RequestBody ProdutoAtualizacaoRequest request) {
    return mapper.toResponse(service.alterar(
            id,
            request.descricao(),
            request.valorUnitario(),
            request.estoqueMinimo(),
            request.grupoId(),
            request.fornecedorId()));
}
```

### 10.4 Movimentações

```java
@PostMapping("/{id}/estoque/entradas")
public ProdutoResponse receberEstoque(
        @PathVariable Long id,
        @Valid @RequestBody MovimentacaoEstoqueRequest request) {
    return mapper.toResponse(
            service.receberEstoque(id, request.quantidade()));
}

@PostMapping("/{id}/estoque/saidas")
public ProdutoResponse retirarEstoque(
        @PathVariable Long id,
        @Valid @RequestBody MovimentacaoEstoqueRequest request) {
    return mapper.toResponse(
            service.retirarEstoque(id, request.quantidade()));
}
```

Crie também as rotas de status, exclusão e pesquisa. As implementações completas estão abaixo.

Para que o aluno não dependa de inferir essas rotas, implemente-as explicitamente em `ProdutoController`:

```java
@PutMapping("/{id}/status")
public ProdutoResponse alterarStatus(
        @PathVariable Long id,
        @Valid @RequestBody StatusRequest request) {
    return mapper.toResponse(
            service.alterarStatus(id, request.status()));
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> excluir(@PathVariable Long id) {
    service.excluir(id);
    return ResponseEntity.noContent().build();
}

@GetMapping
public PaginaResponse<ProdutoResponse> pesquisar(
        @RequestParam(required = false) String descricao,
        @RequestParam(required = false) Status status,
        @RequestParam(required = false) Long grupoId,
        @RequestParam(required = false) Long fornecedorId,
        @RequestParam(required = false) Boolean abaixoEstoqueMinimo,
        @PageableDefault(
                size = 20,
                sort = "id",
                direction = Sort.Direction.ASC)
        Pageable pageable) {
    return PaginaResponse.de(
            service.pesquisar(descricao, status, grupoId, fornecedorId,
                    abaixoEstoqueMinimo, pageable),
            mapper::toResponse);
}
```

Adicione os imports de `Pageable`, `Sort`, `PageableDefault`, `RequestParam` e `DeleteMapping`. Se o IDE importar `java.awt.print.Pageable`, remova-o: o tipo correto pertence a `org.springframework.data.domain`.

### 10.5 Carregar a tela de alteração

Antes de enviar um `PUT`, a interface precisa consultar o estado atual do recurso. As rotas por ID criadas na Aula 07 continuam disponíveis:

```text
GET /api/grupos-produtos/{id}
GET /api/produtos/{id}
```

Fluxo recomendado para uma tela de alteração:

```text
usuário abre a tela
    → interface executa GET por ID
    → API devolve os dados atuais
    → interface preenche o formulário
    → usuário altera os campos permitidos
    → interface executa PUT no mesmo ID
    → API devolve a representação atualizada
```

Consulta de grupo:

```bash
curl -i http://localhost:8080/api/grupos-produtos/1
```

Resposta:

```json
{
  "id": 1,
  "nome": "Periféricos",
  "status": "ATIVO"
}
```

O campo `nome` alimenta o formulário. O `id` permanece na rota do `PUT`:

```text
PUT /api/grupos-produtos/1
```

Consulta de produto:

```bash
curl -i http://localhost:8080/api/produtos/10
```

Resposta abreviada:

```json
{
  "id": 10,
  "codigoBarras": "MOUSE-001",
  "descricao": "Mouse sem fio",
  "saldoEstoque": 10.000,
  "valorUnitario": 89.90,
  "estoqueMinimo": 2.000,
  "dataCadastro": "2026-09-29",
  "status": "ATIVO",
  "grupoId": 1,
  "grupoNome": "Periféricos",
  "fornecedorId": 3,
  "fornecedorRazaoSocial": "Distribuidora Acadêmica Ltda"
}
```

Para preencher o formulário de produto, utilize `descricao`, `valorUnitario`, `estoqueMinimo`, `grupoId` e `fornecedorId`. Os demais campos podem ser exibidos como somente leitura ou tratados pelas operações específicas de status e estoque.

Se o ID não existir, a API devolve `404`. A interface deve apresentar uma mensagem de recurso não encontrado e impedir o envio de um formulário vazio.

---

## 11. Checkpoint 9 – Tratamento de erros

Associe `RecursoEmUsoException` a `409 Conflict`. Trate regras inválidas do domínio como `400 Bad Request`:

```java
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<ApiError> tratarRegraInvalida(
        IllegalArgumentException exception,
        HttpServletRequest request) {
    return resposta(
            HttpStatus.BAD_REQUEST,
            exception.getMessage(),
            request,
            Map.of());
}
```

Casos esperados:

| Situação | Status |
|---|---:|
| corpo inválido | `400` |
| saída maior que o saldo | `400` |
| recurso inexistente | `404` |
| nome duplicado | `409` |
| grupo ainda possui produtos | `409` |
| exclusão concluída | `204` |

Uma falha inesperada continua sendo `500`; não converta todas as exceções indiscriminadamente em `400`.

---

## 12. Executar e testar manualmente

Inicie a aplicação:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Nos exemplos seguintes, substitua os IDs pelos valores do seu banco.

### 12.1 Alterar grupo

```bash
curl -i -X PUT http://localhost:8080/api/grupos-produtos/1 \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Periféricos e acessórios"}'
```

### 12.2 Inativar grupo

```bash
curl -i -X PUT http://localhost:8080/api/grupos-produtos/1/status \
  -H 'Content-Type: application/json' \
  -d '{"status":"INATIVO"}'
```

### 12.3 Pesquisar grupos

```bash
curl -i 'http://localhost:8080/api/grupos-produtos?nome=perif&status=ATIVO&page=0&size=10&sort=nome,asc'
```

### 12.4 Alterar produto

```bash
curl -i -X PUT http://localhost:8080/api/produtos/1 \
  -H 'Content-Type: application/json' \
  -d '{
    "descricao":"Mouse ergonômico sem fio",
    "valorUnitario":129.90,
    "estoqueMinimo":4.000,
    "grupoId":1,
    "fornecedorId":null
  }'
```

### 12.5 Entrada de estoque

```bash
curl -i -X POST http://localhost:8080/api/produtos/1/estoque/entradas \
  -H 'Content-Type: application/json' \
  -d '{"quantidade":5.000}'
```

### 12.6 Saída de estoque

```bash
curl -i -X POST http://localhost:8080/api/produtos/1/estoque/saidas \
  -H 'Content-Type: application/json' \
  -d '{"quantidade":2.000}'
```

Repita com uma quantidade superior ao saldo e confira o `400` e a mensagem `Saldo de estoque insuficiente`.

### 12.7 Pesquisa combinada

```bash
curl -i 'http://localhost:8080/api/produtos?descricao=mouse&status=ATIVO&grupoId=1&abaixoEstoqueMinimo=true&page=0&size=10&sort=descricao,asc'
```

### 12.8 Exclusão

```bash
curl -i -X DELETE http://localhost:8080/api/produtos/1
```

Resposta esperada: `204 No Content`. Uma consulta posterior ao mesmo ID deve retornar `404`.

Para grupos, tente primeiro excluir um grupo com produtos e observe o `409`. Exclua ou transfira os produtos somente em um banco de aula e repita.

---

## 13. Testes automatizados

### 13.1 Domínio

Não registre apenas a lista de casos. Implemente primeiro um comportamento completo em `ProdutoTest`:

```java
@Test
void deveAlterarDadosEditaveis() {
    Produto produto = novoProduto("PRODUTO-ALTERACAO");
    GrupoProduto novoGrupo = new GrupoProduto("Novo grupo");

    produto.alterarDescricao("Descrição alterada");
    produto.alterarValorUnitario(new BigDecimal("25.90"));
    produto.alterarEstoqueMinimo(new BigDecimal("3.000"));
    produto.alterarGrupo(novoGrupo);

    assertEquals("Descrição alterada", produto.getDescricao());
    assertEquals(new BigDecimal("25.90"), produto.getValorUnitario());
    assertEquals(new BigDecimal("3.000"), produto.getEstoqueMinimo());
    assertSame(novoGrupo, produto.getGrupo());
    assertTrue(novoGrupo.getProdutos().contains(produto));
}
```

O teste verifica valores e os dois lados da associação. Depois acrescente, como métodos independentes:

- alteração válida de nome;
- rejeição de nome em branco;
- alteração de descrição, valor e estoque mínimo;
- troca de grupo nos dois lados da associação;
- remoção de fornecedor;
- entrada e saída válidas;
- saída maior que o saldo.

### 13.2 Persistência

Em `PersistenciaJpaTest`, injete `JdbcTemplate` e escreva:

```java
@Test
@Transactional
void bancoDeveImpedirNomeDeGrupoDuplicadoIgnorandoMaiusculas() {
    jdbcTemplate.update(
            "INSERT INTO grupo_produto (nome, status) VALUES (?, 'ATIVO')",
            "Papelaria");

    assertThrows(DataIntegrityViolationException.class, () -> {
        jdbcTemplate.update(
                "INSERT INTO grupo_produto (nome, status) VALUES (?, 'ATIVO')",
                "PAPELARIA");
    });
}
```

Esse teste precisa do PostgreSQL. H2 ou um mock não comprovariam o comportamento do índice funcional `LOWER(BTRIM(nome))`.

Atualize também a quantidade esperada de changeSets para 18.

### 13.3 MockMvc – alteração e status

Prepare grupo e produto pelo repository para que o teste se concentre no contrato HTTP. Anote a classe com `@SpringBootTest`, `@AutoConfigureMockMvc`, `@ActiveProfiles("test")` e `@Transactional`.

```java
String jsonAlteracao = """
        {
          "descricao": "Produto alterado",
          "valorUnitario": 59.90,
          "estoqueMinimo": 4.000,
          "grupoId": %d,
          "fornecedorId": null
        }
        """.formatted(grupo.getId());

mockMvc.perform(put("/api/produtos/{id}", produto.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonAlteracao))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.descricao")
                .value("Produto alterado"));

mockMvc.perform(put("/api/produtos/{id}/status", produto.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"INATIVO\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("INATIVO"));
```

### 13.4 MockMvc – paginação

```java
mockMvc.perform(get("/api/produtos")
                .param("descricao", "mouse")
                .param("page", "0")
                .param("size", "5")
                .param("sort", "descricao,asc"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.conteudo.length()").value(1))
        .andExpect(jsonPath("$.pagina").value(0))
        .andExpect(jsonPath("$.tamanho").value(5));
```

O teste deve criar ao menos dois produtos, um compatível e outro incompatível com o filtro. Se houver apenas um registro, o teste pode passar mesmo que o filtro seja ignorado.

### 13.5 Testar o erro sem perder o estado anterior

```java
mockMvc.perform(post("/api/produtos/{id}/estoque/saidas", produto.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantidade\":10.001}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message")
                .value("Saldo de estoque insuficiente"));

Produto recarregado = produtoRepository.findById(produto.getId())
        .orElseThrow();
assertEquals(new BigDecimal("10.000"), recarregado.getSaldoEstoque());
```

Não basta confirmar o `400`: a evidência precisa mostrar que uma operação rejeitada não alterou o saldo.

### 13.6 Casos mínimos da suíte

- `PUT` válido retorna `200`;
- `PUT` de ID inexistente retorna `404`;
- atualização duplicada retorna `409`;
- status inválido retorna `400`;
- entrada positiva altera o saldo;
- saída maior que o saldo retorna `400` e não altera o saldo;
- filtro retorna somente registros compatíveis;
- página contém metadados corretos;
- ordenação inválida retorna `400`;
- exclusão de produto retorna `204`;
- exclusão de grupo utilizado retorna `409`.

Execute:

```bash
./mvnw test
```

---

## 14. Atualizar a coleção Postman

Na coleção da Aula 07, acrescente:

1. `06 - Alterar grupo`;
2. `07 - Inativar grupo`;
3. `08 - Alterar produto`;
4. `09 - Entrada de estoque`;
5. `10 - Saída de estoque`;
6. `11 - Pesquisar produtos`;
7. `12 - Excluir produto`;
8. cenários negativos de saldo, ordenação e grupo em uso.

Exemplo de teste para exclusão:

```javascript
pm.test("Exclusão deve retornar 204", function () {
    pm.response.to.have.status(204);
});

pm.test("Resposta não deve possuir corpo", function () {
    pm.expect(pm.response.text()).to.eql("");
});
```

Não coloque a exclusão antes de requisições que ainda dependem do produto.

A versão consolidada e importável está em [`postman/Suporte-OS.postman_collection.json`](../../postman/Suporte-OS.postman_collection.json). Ela organiza a exclusão em uma pasta final, captura os IDs automaticamente e inclui os cenários negativos sem armazenar credenciais.

---

## 15. Diagnóstico orientado por evidências

| Sintoma | Hipótese provável | Evidência e correção |
|---|---|---|
| `404` no `PUT` | ID não existe | consulte o recurso antes da alteração |
| `400` no status | texto não pertence ao enum | use exatamente `ATIVO` ou `INATIVO` |
| `400` na saída | quantidade inválida ou saldo insuficiente | leia `message` e consulte o saldo |
| `409` ao renomear grupo | nome já existe ignorando caixa | pesquise o nome e escolha outro |
| `409` ao excluir grupo | existem produtos relacionados | inative o grupo ou transfira os produtos |
| `LazyInitializationException` | mapper acessou relação não carregada | revise transação e `@EntityGraph` |
| erro de propriedade no sort | campo não permitido | use os campos documentados |
| página vazia | índice começa em zero ou filtros não combinam | confira `page`, IDs e status |
| migração 004 falha | já existem nomes duplicados | execute a consulta de diagnóstico antes da migração |
| resultado enorme | limite de paginação ausente | configure `max-page-size` |

Não remova constraints, não ative `open-in-view` e não troque para `ddl-auto=update` apenas para fazer o erro desaparecer. Primeiro determine qual contrato foi violado.

---

## 16. Atividade orientada

### Parte A – leitura do contrato

Para cada rota nova, registre:

```text
método | URI | entrada | saída | status de sucesso | erros esperados
```

Explique quais operações são idempotentes.

### Parte B – domínio

1. implemente alteração de grupo e produto;
2. preserve as validações existentes;
3. mantenha os dois lados da associação;
4. escreva testes sem Spring.

### Parte C – aplicação e persistência

1. crie a migração de unicidade;
2. amplie os repositories;
3. implemente alteração, status, estoque e exclusão;
4. demonstre dirty checking;
5. demonstre conflito de exclusão.

### Parte D – consultas

1. implemente dois filtros para a classificação;
2. implemente pelo menos quatro filtros para a entidade principal;
3. adicione paginação;
4. permita ordenação apenas por campos documentados;
5. limite o tamanho da página.

### Parte E – API e evidências

1. crie DTOs específicos;
2. implemente controllers;
3. padronize os erros;
4. escreva testes MockMvc;
5. atualize a coleção Postman;
6. registre comandos, status e respostas observadas.

---

## 17. Transferência para o tema individual

No projeto individual, identifique:

- quais campos da entidade principal podem ser alterados;
- quais campos são imutáveis;
- qual medida possui operações equivalentes a entrada e saída;
- quando inativar é preferível a excluir;
- quais relacionamentos impedem exclusão;
- quais filtros são relevantes para o usuário;
- quais campos podem ser usados para ordenação.

Entregáveis:

- contrato das rotas;
- código de domínio, aplicação e API;
- migração necessária;
- pelo menos dez testes novos;
- coleção Postman;
- texto justificando `PUT`, `POST`, `DELETE`, `400`, `404` e `409`.

---

## 18. Questões de revisão

1. Por que o saldo não faz parte do DTO de atualização do produto?
2. O que torna `PUT` idempotente?
3. Por que uma entrada de estoque usa `POST`?
4. Qual é a diferença entre inativação e exclusão?
5. Por que a exclusão de um grupo utilizado retorna `409`?
6. Por que verificar duplicidade no serviço e também no banco?
7. Para que serve `existsByNomeIgnoreCaseAndIdNot`?
8. O que o dirty checking faz durante uma alteração?
9. Por que o mapper não deve acessar uma relação lazy depois da transação?
10. Que problema `@EntityGraph` resolve nesta consulta?
11. Por que não devemos carregar todos os registros e filtrar com `stream()`?
12. Como uma `Specification` permite combinar filtros?
13. Por que a primeira página possui índice zero?
14. Por que limitar `size`?
15. Por que validar os campos de `sort`?
16. Por que devolver `PaginaResponse` em vez de `Page` diretamente?
17. O que deve acontecer quando a saída supera o saldo?
18. Por que `DELETE` bem-sucedido retorna `204` sem corpo?

---

## 19. Rubrica de avaliação

| Critério | 4 — Pleno | 3 — Adequado | 2 — Parcial | 1 — Insuficiente |
|---|---|---|---|---|
| domínio e invariantes | mudanças usam comportamentos e mantêm associações consistentes | regras principais preservadas | parte das regras migra para service/controller | setters ou alterações deixam estado inválido |
| atualização com `PUT` | contrato editável completo, idempotente e sem campos controlados | atualização correta com pequena inconsistência | comportamento semelhante a patch sem justificativa | altera saldo/status ou usa método inadequado |
| status, estoque e exclusão | semântica, transação e conflitos corretos | operações funcionam nos casos principais | falhas conhecidas retornam status inconsistentes | saldo negativo ou integridade violada |
| filtros | Specifications combináveis executadas no banco | filtros principais corretos | parte da filtragem ocorre em memória | carrega todos os dados para filtrar |
| paginação e ordenação | contrato estável, limite e whitelist de sort | paginação e ordenação funcionam | metadados ou limites incompletos | retorna lista completa sem controle |
| erros | `400`, `404`, `409` e `204` coerentes e padronizados | principais erros corretos | alguns erros viram `500` ou formato divergente | falhas conhecidas não são tratadas |
| testes | domínio, service, persistência e HTTP cobrem sucesso e falha | cobre operações principais | cobre apenas caminhos felizes | não apresenta evidência automatizada |
| transferência e comunicação | adapta ao tema e justifica cada decisão | implementação e justificativas centrais | cópia mecânica com pouca análise | não transfere ou não explica |

Uma solução que apenas cria rotas, mas coloca regras no controller, filtra listas em memória ou remove constraints do banco, não atende aos objetivos da aula.

---

## 20. Ponto de quebra

Antes de versionar:

```bash
./mvnw test
git diff --check
git status
```

Confirme:

- alteração usa `PUT`;
- movimentações usam `POST`;
- exclusão usa `DELETE`;
- grupo em uso não é excluído;
- saída nunca deixa saldo negativo;
- filtros são aplicados pelo banco;
- resposta contém metadados de página;
- tamanho máximo é 100;
- relações necessárias são carregadas;
- a migração possui rollback;
- nenhum segredo foi versionado.

Commit e tag sugeridos:

```bash
git add README.md docs src
git commit -m "Aula 08: completa operações e consultas paginadas"
git tag -a aula-08-crud-filtros-paginacao \
  -m "Conclusão da Aula 08"
git push origin main
git push origin aula-08-crud-filtros-paginacao
```

Não crie a tag enquanto os testes obrigatórios estiverem falhando.

## 21. Orientações para o professor

### Sequência sugerida

| Bloco | Tempo sugerido | Ênfase |
|---|---:|---|
| contrato e semântica HTTP | 30 min | PUT, POST, DELETE e idempotência |
| evolução do domínio | 35 min | campos editáveis e associação bidirecional |
| services e integridade | 40 min | transação, dirty checking e conflito |
| filtros e Specifications | 40 min | predicados no banco |
| paginação e ordenação | 35 min | contrato, limite e whitelist |
| controllers e erros | 35 min | status e representações |
| testes e Postman | 45 min | evidências e compatibilidade |

Para uma turma iniciante, distribua o conteúdo em dois encontros: operações de escrita no primeiro; filtros, paginação e testes no segundo.

### Demonstrações essenciais

- repetir o mesmo `PUT` e comparar o estado final;
- repetir uma entrada de estoque e contrastar a não idempotência;
- tentar excluir um grupo com produtos;
- comparar filtro no banco com `findAll().stream()`;
- solicitar páginas diferentes e inspecionar metadados;
- enviar campo de ordenação não permitido;
- carregar um produto por ID, preencher o formulário e enviar o `PUT`;
- mostrar a quebra consciente do array da Aula 07 para `PaginaResponse`.

### Perguntas para discussão

- Por que saldo, status e código de barras não pertencem ao DTO geral de atualização?
- Inativar e excluir expressam a mesma intenção de negócio?
- Qual risco existe ao permitir qualquer texto no parâmetro `sort`?
- Por que `abaixoEstoqueMinimo` deve ser calculado pelo banco?
- Em que cenário paginação por cursor seria preferível à paginação por offset?

### Falhas controladas

- tentar retirar quantidade superior ao saldo;
- omitir `grupoId` no `PUT`;
- informar fornecedor inexistente;
- remover temporariamente o `@EntityGraph` e observar o acesso lazy;
- solicitar `sort=fornecedor.razaoSocial`;
- tentar aplicar a migração 004 sobre nomes duplicados em banco descartável.

### Extensões opcionais

Para turmas que avançarem mais rápido:

- registrar movimentações como entidade imutável;
- discutir locking otimista com `@Version`;
- comparar `PUT` e `PATCH` sem implementar patch;
- projetar links de navegação entre páginas;
- medir o SQL produzido por combinações de filtros.

## 22. Referências para aprofundamento

- Spring Data JPA — Specifications: <https://docs.spring.io/spring-data/jpa/reference/jpa/specifications.html>
- Spring Data Commons — paginação e ordenação: <https://docs.spring.io/spring-data/commons/reference/repositories/query-methods-details.html>
- MDN — métodos HTTP: <https://developer.mozilla.org/pt-BR/docs/Web/HTTP/Methods>
- RFC 9110 — semântica HTTP: <https://www.rfc-editor.org/rfc/rfc9110>

---

**Resultado da aula:** grupos e produtos passam a possuir operações completas de manutenção, transições de status, movimentações seguras de estoque e consultas escaláveis com filtros, ordenação e paginação.

[⬅ Voltar para o índice](../../README.md)
