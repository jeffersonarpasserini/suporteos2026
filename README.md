# Suporte OS 2026

API didática desenvolvida na disciplina de Programação da graduação em Sistemas de Informação.

## Domínio inicial

- Grupo de produto: classificação de produtos.
- Produto: item identificado por código de barras, com saldo e valor unitário.

## Requisitos

- Java 21
- Git
- IntelliJ IDEA com recursos Ultimate ativos para o roteiro principal; o Spring Initializr oferece o caminho independente da IDE
- Docker Desktop, utilizado nas aulas de PostgreSQL e containerização

## Organização do curso

O sistema será construído incrementalmente. Cada aula termina em um estado executável, registrado por um commit e, após validação, por uma tag Git no formato `aula-NN-*`.

## Material de aula

| Aula | Tema | Material |
|---|---|---|
| 00 | GitHub e início do projeto | [Abrir Aula 00](docs/00aula/00aula.md) |
| 01 | Configuração do ambiente | [Abrir Aula 01](docs/01aula/01aula.md) |
| 02 | Criação do projeto e definição do tema | [Abrir Aula 02](docs/02aula/02aula.md) |
| 03 | Modelagem de domínio com Java puro | [Abrir Aula 03](docs/03aula/03aula.md) |
| 04 | Persistência com JPA, PostgreSQL, profiles e Liquibase | [Abrir Aula 04](docs/04aula/04aula.md) |
| 05 | Spring Data JPA, repositories, serviços e transações | [Abrir Aula 05](docs/05aula/05aula.md) |
| 06 | Evolução do modelo e geração assistida de changelogs | [Abrir Aula 06](docs/06aula/06aula.md) |
| 07 | API REST, DTOs, mapeadores e testes com Postman | [Abrir Aula 07](docs/07aula/07aula.md) |
| 08 | CRUD completo, estoque, filtros e paginação | [Abrir Aula 08](docs/08aula/08aula.md) |
| 09 | OpenAPI: contrato executável e documentação da API | [Abrir Aula 09](docs/09aula/09aula.md) |
| 10 | Pessoas, clientes, colaboradores, fornecedores e vendas | [Abrir Aula 10](docs/10aula/10aula.md) |
| 11 | Concorrência de estoque, isolamento e locking | [Abrir Aula 11](docs/11aula/11aula.md) |
| 12 | Autenticação, proteção de senhas e Spring Security | [Abrir Aula 12](docs/12aula/12aula.md) |
| 13 | JWT, Bearer Token e Resource Server | [Abrir Aula 13](docs/13aula/13aula.md) |
| 14 | Autorização, perfis e controle de acesso | [Abrir Aula 14](docs/14aula/14aula.md) |
| 15 | Testes de software e cobertura com JaCoCo | [Abrir Aula 15](docs/15aula/15aula.md) |

## Organização pedagógica

As aulas combinam fundamentação conceitual, implementação incremental, evidências de execução, diagnóstico, atividade de transferência e avaliação. O material não deve apresentar código ou configuração como uma sequência isolada de procedimentos.

- [Padrão pedagógico obrigatório das aulas](docs/PADRAO-PEDAGOGICO.md)
- [Revisão pedagógica e acompanhamento das Aulas 00 a 15](docs/REVISAO-PEDAGOGICA-AULAS-00-15.md)
- [Plano de continuidade das Aulas 14 a 22](docs/PLANO-PROXIMAS-AULAS.md)

## Projeto de referência

O `suporteos2026` demonstra um controle simplificado de produtos organizados por grupos. Cada estudante deverá escolher um tema compatível, documentá-lo e manter esse mesmo domínio durante as próximas aulas.

- [Consultar o tema do projeto de referência](docs/tema-do-projeto.md)

## Executando o projeto

Na primeira execução, copie `.env.example` para `.env`, preencha as senhas dos bancos, gere `JWT_SECRET` conforme a Aula 13 e mantenha esse arquivo fora do Git. Para um banco vazio, configure também o administrador inicial conforme a Aula 14, execute uma vez com `INITIAL_ADMIN_ENABLED=true` e depois volte a variável para `false`.

No Windows:

```powershell
Copy-Item .env.example .env
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

No macOS ou Linux:

```bash
cp .env.example .env
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Com a aplicação iniciada, acesse <http://localhost:8080/api/health>. A resposta esperada é `OK`.

## Modelo de domínio atual

O modelo atual também representa pessoas, papéis comerciais, vendas e contas de acesso:

```text
GrupoProduto 1 ─────── N Produto
Fornecedor   1 ─────── N Produto
Pessoa       1 ───── 0..1 Cliente
Pessoa       1 ───── 0..1 Colaborador
Pessoa       1 ───── 0..1 ContaAcesso 1 ───── N PerfilAcesso
Cliente      1 ─────── N Venda
Colaborador  1 ─────── N Venda
Venda        1 ─────── N ItemVenda N ─────── 1 Produto
```

O fornecedor de um produto é opcional e possui CNPJ validado pelos dígitos verificadores. CPF pertence a `Pessoa`; `Cliente` e `Colaborador` são papéis por composição, sem herança. `ContaAcesso` mantém credenciais e perfis separados da função profissional. `ItemVenda` preserva quantidade e preço histórico. As classes estão no pacote `com.curso.suporteos.domain`, são mapeadas com JPA e preservam as regras de negócio. O Liquibase cria e versiona o esquema; o Hibernate apenas o valida. Os profiles `dev`, `test` e `prod` usam PostgreSQL, sem H2.

## API atual

Após iniciar a aplicação, estão disponíveis os cadastros, consultas por ID e listagens de:

- `http://localhost:8080/api/grupos-produtos`;
- `http://localhost:8080/api/fornecedores`;
- `http://localhost:8080/api/produtos`;
- `http://localhost:8080/api/pessoas`;
- `http://localhost:8080/api/clientes`;
- `http://localhost:8080/api/colaboradores`;
- `http://localhost:8080/api/vendas`;
- `http://localhost:8080/api/contas-acesso`.

Os contratos usam DTOs, validação de entrada e respostas de erro padronizadas. A [Aula 07](docs/07aula/07aula.md) contém um laboratório completo de testes com Postman. A [Aula 08](docs/08aula/08aula.md) acrescenta alteração, exclusão, status, movimentações de estoque, filtros, ordenação e paginação para grupos e produtos.

A [Aula 09](docs/09aula/09aula.md) publica o contrato OpenAPI em `/v3/api-docs` e `/v3/api-docs.yaml`. Durante o desenvolvimento, a documentação interativa está disponível em `/swagger-ui.html`; no profile `prod`, sua exposição permanece desabilitada por decisão explícita.

A [Aula 10](docs/10aula/10aula.md) evolui o domínio com CPF, CNPJ, papéis compostos, vendas, itens, preço histórico e baixa transacional de estoque, deixando explícita a ligação com uma futura implementação de contas e perfis de acesso.

A [Aula 11](docs/11aula/11aula.md) trata alterações simultâneas do estoque. Os produtos são bloqueados no PostgreSQL com `PESSIMISTIC_WRITE`; vendas com vários itens adquirem os locks em ordem de identificador. A estratégia protege finalizações, entradas e saídas manuais sem transformar consultas comuns em operações bloqueantes.

A [Aula 12](docs/12aula/12aula.md) adiciona contas vinculadas a pessoas, hash BCrypt e autenticação HTTP Basic stateless. Health, documentação e o fluxo inicial de criação de pessoa e conta permanecem públicos para fins didáticos; as demais rotas exigem credencial. Esse bootstrap público está explicitamente documentado como provisório e não representa uma configuração pronta para produção.

A [Aula 13](docs/13aula/13aula.md) substitui HTTP Basic por JWT. O endpoint `/api/autenticacao/login` valida a conta e emite um token assinado HS256 com duração de 30 minutos. As rotas protegidas passam a exigir `Authorization: Bearer <token>` e são validadas pelo OAuth2 Resource Server do Spring Security. A chave Base64 vem obrigatoriamente de `JWT_SECRET`.

A [Aula 14](docs/14aula/14aula.md) converte o claim `perfis` em authorities, diferencia `401` de `403` e aplica autorização para `OPERADOR` e `ADMINISTRADOR`. A criação e a gestão de contas, assim como exclusões, exigem perfil administrativo. Em banco vazio, o primeiro administrador é provisionado por variáveis de ambiente e o mecanismo permanece desativado por padrão.

A [Aula 15](docs/15aula/15aula.md) organiza a estratégia de testes do sistema, compara testes unitários, de integração, persistência, API, segurança e concorrência, e configura o JaCoCo. O build passa a exigir pelo menos 70% de cobertura de linhas e 40% de branches, sem tratar essas métricas como prova isolada de qualidade.

Para testar todo o fluxo em um cliente externo, importe no Postman a coleção [Suporte OS 2026 - API completa](postman/Suporte-OS.postman_collection.json). Ela autentica o administrador inicial, cria uma conta OPERADOR, demonstra o resultado `403`, captura o JWT operacional e o envia como Bearer nas chamadas protegidas. Os demais identificadores também são capturados automaticamente, permitindo a execução ordenada pelo Collection Runner.

## Executando os testes

Os testes de persistência acessam `suporteos2026_test` e leem `DB_TEST_PASSWORD` do `.env` local ou das variáveis do ambiente de execução. Os testes unitários e da camada web não dependem de uma porta HTTP aberta.

No Windows:

```powershell
.\mvnw.cmd test
```

No macOS ou Linux:

```bash
./mvnw test
```

Para executar a suíte, gerar o relatório JaCoCo e aplicar os limites de cobertura:

No Windows:

```powershell
.\mvnw.cmd clean verify
```

No macOS ou Linux:

```bash
./mvnw clean verify
```

O relatório HTML será gerado em `target/site/jacoco/index.html`. No marco da Aula 15, a suíte possui 79 execuções aprovadas, 75,85% de cobertura de linhas e 47,15% de branches.

## Histórico didático

Para consultar os marcos publicados:

```bash
git tag --list
```
