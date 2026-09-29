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

## Organização pedagógica

As aulas combinam fundamentação conceitual, implementação incremental, evidências de execução, diagnóstico, atividade de transferência e avaliação. O material não deve apresentar código ou configuração como uma sequência isolada de procedimentos.

- [Padrão pedagógico obrigatório das aulas](docs/PADRAO-PEDAGOGICO.md)
- [Revisão pedagógica e acompanhamento das Aulas 00 a 08](docs/REVISAO-PEDAGOGICA-AULAS-00-08.md)

## Projeto de referência

O `suporteos2026` demonstra um controle simplificado de produtos organizados por grupos. Cada estudante deverá escolher um tema compatível, documentá-lo e manter esse mesmo domínio durante as próximas aulas.

- [Consultar o tema do projeto de referência](docs/tema-do-projeto.md)

## Executando o projeto

Na primeira execução, copie `.env.example` para `.env`, preencha `DB_DEV_PASSWORD` e `DB_TEST_PASSWORD` e mantenha esse arquivo fora do Git.

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

Na Aula 06, o modelo passou a representar grupos, produtos e fornecedores:

```text
GrupoProduto 1 ─────── N Produto
Fornecedor   1 ─────── N Produto
```

O fornecedor de um produto é opcional. As classes estão no pacote `com.curso.suporteos.domain`, são mapeadas com JPA e preservam as regras de negócio. O Liquibase cria e versiona o esquema; o Hibernate apenas o valida. Os profiles `dev`, `test` e `prod` usam PostgreSQL, sem H2.

## API atual

Após iniciar a aplicação, estão disponíveis os cadastros, consultas por ID e listagens de:

- `http://localhost:8080/api/grupos-produtos`;
- `http://localhost:8080/api/fornecedores`;
- `http://localhost:8080/api/produtos`.

Os contratos usam DTOs, validação de entrada e respostas de erro padronizadas. A [Aula 07](docs/07aula/07aula.md) contém um laboratório completo de testes com Postman. A [Aula 08](docs/08aula/08aula.md) acrescenta alteração, exclusão, status, movimentações de estoque, filtros, ordenação e paginação para grupos e produtos.

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

## Histórico didático

Para consultar os marcos publicados:

```bash
git tag --list
```
