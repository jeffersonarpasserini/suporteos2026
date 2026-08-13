# Revisão pedagógica — Aulas 00 a 02

[⬅ Voltar para o índice do curso](../README.md)

---

## Escopo da revisão

Foram revisadas todas as aulas atualmente existentes no repositório do curso 2026:

- Aula 00 — GitHub e início do projeto;
- Aula 01 — configuração do ambiente;
- Aula 02 — criação do projeto e definição do tema.

A análise considerou correção técnica, fundamentação conceitual, vínculo entre teoria e prática, verificação, avaliação, segurança, transferência para o projeto temático e adequação ao fluxo de tags do GitHub.

## Síntese

| Dimensão | Aula 00 | Aula 01 | Aula 02 |
|---|---|---|---|
| Problema e contexto | Atende | Atende após revisão | Atende |
| Resultados observáveis | Atende | Atende após revisão | Atende |
| Fundamentos teóricos | Reforçado | Reestruturado | Atende |
| Teoria ligada à prática | Reforçado | Incluído | Atende |
| Passo a passo | Atende | Atende; trilhas separadas | Atende |
| Verificação objetiva | Atende | Incluída | Atende |
| Diagnóstico | Atende | Incluído | Atende |
| Segurança | Atende | Corrigida | Atende |
| Atividade de transferência | Parcial | Atende | Atende |
| Rubrica | Incluída | Incluída | Incluída |
| Ponto de quebra | Atende | Incluído | Atende |
| Referências primárias | Atende | Incluídas | Atende |

## Aula 00 — GitHub e início do projeto

### Pontos fortes

- começa pelo problema de preservação e colaboração do projeto;
- diferencia repositório local e remoto;
- apresenta estados, comandos, segurança e recuperação de falhas;
- termina com commit e tag verificáveis;
- contém atividade, questões, checklists e orientação docente.

### Lacunas encontradas e tratadas

- faltava fundamentar o Git como sistema distribuído;
- commit aparecia sobretudo como operação, sem o modelo de snapshot e grafo;
- integridade por hash poderia ser confundida com backup ou segurança completa;
- faltava rubrica explícita de avaliação.

### Próxima melhoria recomendada

Na aplicação em sala, acrescentar uma pequena atividade de transferência: cada estudante deve propor uma política de commits e tags para o próprio projeto e justificar o tamanho de uma unidade lógica.

## Aula 01 — configuração do ambiente

### Pontos fortes preservados

- riqueza de detalhes operacionais e acessibilidade das descrições de tela;
- instalação documentada passo a passo;
- presença de Java, Maven, servidores e IDEs, útil para comparação de ecossistemas;
- valor histórico para disciplinas de JSP e Jakarta EE.

### Problemas encontrados

- o material operacional não possuía resultados de aprendizagem, atividade, avaliação ou referências;
- JDK, JRE e JVM apareciam sem um modelo completo de compilação e execução;
- ferramentas obrigatórias e opcionais estavam misturadas;
- Java 17, NetBeans 24 e a antiga separação do IntelliJ eram tratados como atuais;
- havia recomendação de remover todas as instalações Java;
- a apostila afirmava incorretamente que Tomcat 10+ não deve ser usado com servlets;
- sugeria a credencial insegura `admin/admin`;
- instalação global de Maven e `M2_HOME` pareciam obrigatórios, apesar do Wrapper;
- não havia diagnóstico orientado por evidências.

### Correções aplicadas

- decisão tecnológica explícita para 2026: Java 21, IntelliJ, Maven Wrapper e servidor incorporado;
- trilhas Tomcat externo, Payara e NetBeans preservadas como opcionais;
- fundamentos sobre bytecode, JVM, variáveis de ambiente, build e servidores;
- explicação correta da mudança `javax.*` para `jakarta.*`;
- orientação segura sobre credenciais e coexistência de JDKs;
- comandos de validação, diagnóstico, atividade, questões, rubrica e referências oficiais.

### Débito editorial preservado conscientemente

As descrições de capturas antigas ainda mencionam versões e interfaces históricas. Elas foram mantidas para não descartar o material existente e estão subordinadas a uma nota de atualização. Em uma revisão visual futura, recomenda-se substituir as capturas da trilha obrigatória por imagens do ambiente usado no laboratório em 2026 e mover Tomcat/Payara/NetBeans para um apêndice independente.

## Aula 02 — criação do projeto e definição do tema

### Pontos fortes

- apresenta teoria de aplicações, APIs, cliente–servidor, HTTP, recursos e representações;
- discute semântica de métodos, códigos de status, REST, estado e contrato;
- introduz IoC, injeção de dependência, beans, Spring Boot e fluxo da requisição;
- oferece caminhos equivalentes pelo Spring Initializr e IntelliJ;
- relaciona o projeto de referência ao tema individual;
- explica `pom.xml`, Wrapper, classe principal, teste e endpoint;
- contém verificação, diagnóstico, perguntas e ponto de quebra.

### Lacunas ainda existentes

1. **Teste automatizado do endpoint:** o teste inicial valida o contexto; uma aula futura deverá demonstrar teste HTTP com MockMvc ou abordagem equivalente. Não é necessário antecipar toda a infraestrutura nesta aula.
2. **Contrato formal OpenAPI:** o conceito é apresentado, mas a especificação executável deve entrar quando houver operações suficientes para justificar sua manutenção.
3. **Representação de erros:** o endpoint `health` é intencionalmente simples; semântica padronizada de erros deve aparecer junto à validação e tratamento de exceções.
4. **Acessibilidade dos diagramas:** diagramas Mermaid devem ser acompanhados por texto que expresse as relações essenciais.

### Ação aplicada nesta revisão

Foi adicionada uma rubrica com os critérios: modelo conceitual de API, compatibilidade do tema, configuração do projeto, endpoint verificável, Git/reprodutibilidade e argumentação técnica.

## Conteúdos que precisam ser previstos nas próximas aulas

| Conteúdo prático futuro | Fundamentação que deve antecedê-lo |
|---|---|
| entidades JPA | identidade, estado, persistência, ORM e impedância objeto-relacional |
| repositories | abstração de acesso, coleção versus consulta e inversão de dependência |
| services | regra de negócio, coesão, transação e fronteiras de responsabilidade |
| controllers CRUD | contrato HTTP, idempotência, status e representações |
| DTOs | acoplamento, contrato externo, validação e evolução da API |
| relacionamentos | cardinalidade, integridade referencial, agregados e serialização |
| Liquibase | migração incremental, changeset, checksum, ordem, rollback e ambientes |
| variáveis de ambiente | configuração externa, precedência, segredos e princípio doze fatores |
| tratamento de erros | taxonomia de falhas, Problem Details, observabilidade e não vazamento |
| testes | pirâmide de testes, isolamento, falso positivo e evidência de comportamento |
| segurança | autenticação, autorização, ameaça, senha, token e menor privilégio |
| Docker | imagem, contêiner, camadas, processo, rede, volume e reprodutibilidade |
| implantação | artefato, configuração, health check, logs e ciclo de entrega |

## Decisões para continuidade

1. Usar o [padrão pedagógico](PADRAO-PEDAGOGICO.md) como checklist obrigatório de autoria.
2. Introduzir um conceito apenas quando houver prática capaz de torná-lo observável, salvo organizadores prévios indispensáveis.
3. Não entregar código sem explicar responsabilidade, contrato e forma de teste.
4. Manter cada aula executável e gerar a tag somente após validação.
5. Registrar simplificações didáticas e indicar em qual aula serão superadas.
6. Atualizar versões e telas antes de cada oferta anual, sem alterar silenciosamente contratos do curso.

---

[⬅ Voltar para o índice do curso](../README.md)
