# Revisão pedagógica e acompanhamento — Aulas 00 a 09

[⬅ Voltar para o índice do curso](../README.md)

---

## Identificação da revisão

| Item | Situação |
|---|---|
| Escopo | Aulas 00 a 09 |
| Tipo de revisão | Pedagógica, técnica e longitudinal |
| Última atualização | 29 de setembro de 2026 |
| Java adotado | Java 21 |
| Spring Boot adotado | 4.0.7 |
| Projeto de referência | `suporteos2026` |
| Estado técnico verificado | 49 testes aprovados no PostgreSQL 16 |

## Escopo da revisão

Esta revisão avalia as dez aulas atualmente existentes no curso:

- Aula 00 — GitHub e início do projeto;
- Aula 01 — configuração do ambiente;
- Aula 02 — criação do projeto e definição do tema;
- Aula 03 — modelagem de domínio com Java puro;
- Aula 04 — persistência com JPA, PostgreSQL, profiles e Liquibase;
- Aula 05 — Spring Data JPA, repositories, serviços e transações;
- Aula 06 — evolução do modelo e geração assistida de changelogs;
- Aula 07 — API REST, DTOs, mapeadores e testes com Postman;
- Aula 08 — CRUD completo, estoque, filtros e paginação;
- Aula 09 — OpenAPI, documentação executável e contrato verificável.

A análise considera:

- correção técnica;
- resultados de aprendizagem observáveis;
- vínculo entre teoria e prática;
- desenvolvimento incremental;
- verificação e diagnóstico;
- segurança e qualidade;
- transferência para o projeto temático;
- avaliação;
- reprodutibilidade por commits e tags;
- coerência entre documentação e código atual.

O [padrão pedagógico do curso](PADRAO-PEDAGOGICO.md) foi usado como referência de autoria e verificação.

## Evidências verificadas

Foram confrontados:

- os materiais Markdown das Aulas 00 a 09;
- o `README.md` e seu índice;
- a estrutura Maven;
- as classes de domínio, repositories, services, DTOs e controllers;
- os changelogs Liquibase;
- os testes unitários, de persistência, aplicação, API e contrato OpenAPI;
- a coleção Postman consolidada e suas variáveis de execução;
- a execução da suíte com PostgreSQL real.

Na verificação técnica mais recente, o Liquibase reconheceu 18 changeSets e a suíte terminou com:

```text
Tests run: 49
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

Essa evidência confirma o estado atual do protótipo. Ela não substitui as evidências menores exigidas ao final de cada aula.

---

## Síntese das Aulas 00 a 09

| Aula | Contexto e teoria | Prática e verificação | Transferência e avaliação | Situação geral |
|---:|---|---|---|---|
| 00 | fundamentos de Git e rastreabilidade | fluxo completo de versionamento | transferência parcial; rubrica presente | Atende |
| 01 | ambiente Java e responsabilidades das ferramentas | instalação, inventário e diagnóstico | comparação de arquiteturas; rubrica presente | Atende com débito visual |
| 02 | API, HTTP, Spring Boot e contrato do tema | projeto, health check e Git | projeto temático e rubrica | Atende |
| 03 | domínio, invariantes, associação e precisão | classes Java e testes unitários | implementação temática e avaliação | Atende |
| 04 | persistência, ORM, migração e integridade | JPA, PostgreSQL, Liquibase e testes | atividade autônoma e rubrica | Atende |
| 05 | repositories, transações e dirty checking | services e testes transacionais | transferência e avaliação concisa | Atende |
| 06 | evolução segura e revisão de automação | diff, migração 003 e convergência | atividades, rubrica e orientação docente | Atende |
| 07 | REST, DTOs, validação e erros | controllers, MockMvc e Postman | atividade, rubrica, checklist e orientação docente | Atende |
| 08 | semântica de atualização, exclusão e consulta | PUT, DELETE, estoque, filtros e paginação | atividade, rubrica, checklist e orientação docente | Atende |
| 09 | contrato explícito e documentação executável | OpenAPI, Swagger UI, schemas e teste do contrato | laboratório, transferência, rubrica e orientação docente | Atende |

### Conclusão da síntese

As dez aulas formam uma progressão coerente:

```text
Aula 00: preservar e publicar o trabalho
    ↓
Aula 01: compreender e validar o ambiente
    ↓
Aula 02: criar uma aplicação executável e definir o domínio
    ↓
Aula 03: modelar regras em Java puro
    ↓
Aula 04: persistir o modelo com esquema versionado
    ↓
Aula 05: coordenar casos de uso e transações
    ↓
Aula 06: evoluir classes e banco sem perder dados
    ↓
Aula 07: expor casos de uso por uma API REST
    ↓
Aula 08: completar operações e consultas escaláveis
    ↓
Aula 09: tornar o contrato explícito, navegável e verificável
```

Cada aula parte de um estado executável e acrescenta uma responsabilidade principal. A sequência evita misturar domínio, persistência, transação e HTTP antes que cada fundamento possa ser observado e testado isoladamente.

---

## Aula 00 — GitHub e início do projeto

### Finalidade pedagógica observada

A aula apresenta controle de versão antes da criação da aplicação. Essa decisão permite registrar inclusive o estado anterior ao código e estabelece desde o início que o histórico é parte do produto de software.

### Pontos fortes

- diferencia Git de GitHub;
- diferencia repositório local de remoto;
- apresenta Git como sistema distribuído;
- explica commit como snapshot ligado a um grafo;
- distingue diretório de trabalho, índice, histórico local e remoto;
- relaciona `status`, `diff`, `add`, `commit`, `push`, branch e tag;
- apresenta commit como unidade lógica e rastreável;
- usa tags como pontos de quebra recuperáveis;
- inclui segurança antes do primeiro commit;
- explica que `.gitignore` não remove um segredo já publicado;
- apresenta problemas frequentes com hipótese e correção;
- termina com atividade, revisão, avaliação e checklist.

### Alinhamento entre resultado, atividade e evidência

| Resultado pretendido | Atividade | Evidência |
|---|---|---|
| diferenciar Git e GitHub | registrar explicação no README | texto produzido pelo estudante |
| interpretar estados | executar `git status` e revisar diffs | saída dos comandos |
| criar unidade lógica | alterar documentação em branch própria | commit local com mensagem significativa |
| distinguir commit e push | consultar log e remoto | histórico local e publicação no GitHub |
| reconhecer arquivos sensíveis | revisar `.gitignore` e staged diff | ausência de credenciais no commit |

### Melhorias já aplicadas

- inclusão do modelo de snapshots e grafo;
- fundamentação do caráter distribuído do Git;
- delimitação do que hashes garantem e do que não garantem;
- explicação de reprodutibilidade por tags;
- rubrica explícita;
- diagnóstico de autenticação, identidade, branch e push rejeitado.

### Débitos remanescentes

1. **Problema orientador explícito:** a apresentação cumpre essa função, mas uma seção nomeada facilitaria a uniformidade com as aulas posteriores.
2. **Atividade autônoma:** a atividade é predominantemente orientada. Pode-se pedir ao estudante que proponha uma política de commits e tags para o projeto temático.
3. **Transferência:** o exercício altera o README do projeto de aula; a transferência pode exigir uma justificativa de como o mesmo fluxo será usado no projeto individual.

### Recomendação

Acrescentar, em futura revisão editorial, uma atividade curta em que o estudante analise três históricos fictícios, identifique commits mal delimitados e proponha uma divisão melhor. Isso exercita julgamento, não apenas execução de comandos.

---

## Aula 01 — Configuração do ambiente

### Finalidade pedagógica observada

A aula trata o ambiente como uma composição de responsabilidades verificáveis. Instalar programas não é apresentado como fim em si mesmo: o estudante precisa explicar como código-fonte, compilador, bytecode, JVM, build, IDE e servidor participam da execução.

### Pontos fortes

- diferencia JDK, JRE e JVM;
- relaciona `.java`, `javac`, `.class` e processo `java`;
- mostra que terminal, Maven e IDE podem selecionar JDKs diferentes;
- explica `PATH` e `JAVA_HOME` sem tratá-los como sinônimos;
- distingue IDE, linguagem, compilador e framework;
- explica build, dependências e Maven Wrapper;
- compara servidor incorporado, contêiner web e servidor de aplicações;
- explica corretamente a mudança de `javax.*` para `jakarta.*`;
- identifica ferramentas obrigatórias e trilhas opcionais;
- exige evidências por comandos;
- inclui experimento de compilação manual;
- oferece diagnóstico orientado por sintomas.

### Correções históricas consolidadas

O material foi atualizado para evitar recomendações imprecisas ou inseguras:

- Java 21 substituiu versões antigas como referência obrigatória;
- IntelliJ com recursos Ultimate foi definido como caminho principal;
- Spring Initializr preserva um caminho independente da IDE;
- Maven Wrapper é obrigatório, enquanto Maven global é opcional;
- Tomcat externo, Payara e NetBeans foram mantidos como trilhas opcionais;
- a compatibilidade Tomcat 10/Jakarta foi corrigida;
- credenciais triviais deixaram de ser recomendadas;
- a remoção indiscriminada de instalações Java foi substituída por diagnóstico de caminhos e versões.

### Alinhamento entre resultado, atividade e evidência

| Resultado pretendido | Atividade | Evidência |
|---|---|---|
| diferenciar JDK, JRE e JVM | mapa conceitual e experimento | relações nomeadas e saída do programa |
| verificar o Java efetivo | inventário de comandos | versões e caminhos registrados |
| explicar o build | executar Wrapper e testes | versão do Maven/JDK e build observado |
| comparar arquiteturas | texto WAR versus JAR | argumento de 150 a 250 palavras |
| diagnosticar ambiente | interpretar falhas propostas | hipótese sustentada por evidência |

### Débito editorial preservado conscientemente

As capturas históricas de Java 17, NetBeans 24, Tomcat 9 e distribuições antigas do IntelliJ ainda ocupam grande parte do material. Uma nota estabelece que o texto atualizado prevalece, mas o volume visual pode induzir o estudante a tratar a trilha histórica como obrigatória.

### Recomendações

1. Mover NetBeans, Tomcat externo e Payara para um apêndice de Jakarta EE.
2. Substituir as capturas da trilha obrigatória por imagens produzidas no ambiente de 2026.
3. Revisar os arquivos de imagem não referenciados e corrigir nomes sem extensão.
4. Manter a atividade de comparação entre WAR externo e JAR com servidor incorporado, pois ela justifica a preservação conceitual das ferramentas opcionais.

---

## Aula 02 — Criação do projeto e definição do tema

### Finalidade pedagógica observada

A aula combina dois resultados: compreender o tipo de aplicação que será construído e produzir o primeiro incremento executável. O endpoint de health é deliberadamente simples para que o estudante observe o fluxo HTTP antes da introdução de domínio, persistência e contratos mais complexos.

### Pontos fortes

- diferencia aplicação, API, API Web e aplicação com páginas;
- apresenta cliente, servidor, requisição e resposta;
- diferencia HTTP, JSON, REST e CRUD;
- discute recursos, URI, endpoint, métodos e códigos de status;
- introduz segurança e idempotência dos métodos;
- explica stateless e interface uniforme;
- apresenta API como contrato;
- introduz controller, service e repository sem antecipar sua implementação completa;
- explica IoC, container Spring, bean e injeção de dependência;
- relaciona classe principal, pacote raiz e component scanning;
- explica `pom.xml`, starters, scope de teste e plugin Spring Boot;
- oferece caminhos pelo site Spring Initializr e pelo IntelliJ;
- define um contrato mínimo para o tema individual;
- implementa e verifica `GET /api/health`;
- vincula execução, documentação, Git e ponto de quebra.

### Alinhamento entre resultado, atividade e evidência

| Resultado pretendido | Atividade | Evidência |
|---|---|---|
| explicar uma API Web | questões e modelo de requisição | resposta argumentada |
| projetar tema compatível | ficha de domínio | classificação, entidade, medida, valor e relação |
| configurar Spring Boot | gerar e revisar o projeto | `pom.xml` e estrutura coerentes |
| implementar primeiro endpoint | criar `HealthController` | HTTP `200` e corpo `OK` |
| usar o Wrapper | executar teste e aplicação | build reproduzível fora da IDE |
| publicar o incremento | revisar e enviar commit | repositório temático atualizado |

### Limitações didáticas declaradas ou adequadas ao momento

- o health check confirma que o processo HTTP responde, mas ainda não verifica banco ou dependências externas;
- não há entidade de domínio na Aula 02;
- não há persistência, repository ou transação;
- o controller ainda não recebe DTO complexo;
- não há contrato padronizado de erro;
- o teste inicial verifica o contexto, não o contrato HTTP completo.

Essas limitações são apropriadas porque isolam o objetivo da aula e são superadas progressivamente nas aulas posteriores.

### Recomendações

1. Manter explícito que o health inicial é um indicador de vivacidade, não uma prova completa da saúde de todas as dependências.
2. Evitar antecipar dependências que só serão explicadas em aulas futuras.
3. Preservar o contrato de compatibilidade dos temas, pois ele permite transferir os incrementos posteriores sem obrigar todos os estudantes a usar o mesmo domínio.
4. Em futura revisão, incluir uma pequena tabela que ligue cada dependência selecionada no Initializr ao primeiro momento em que será utilizada.

---

## Aula 03 — Modelagem de domínio com Java puro

### Finalidade pedagógica observada

A aula isola as regras do negócio antes de introduzir banco ou framework de persistência. O estudante transforma a descrição informal do tema em objetos que mantêm estado válido e demonstram suas regras por testes unitários.

### Pontos fortes

- apresenta domínio e modelo de domínio antes das anotações JPA;
- diferencia classe, objeto, estado, comportamento, entidade e valor;
- trata encapsulamento como proteção de invariantes;
- explica construtores que impedem objetos inválidos;
- modela associação `1:N` e os dois lados da relação;
- usa `enum` para estados finitos;
- justifica `BigDecimal` para quantidade e dinheiro;
- discute `equals` e `compareTo` em valores decimais;
- usa `LocalDate` para datas sem horário;
- apresenta testes unitários como especificação executável;
- inclui cenários válidos e inválidos;
- transfere a estrutura para o tema individual sem exigir mera troca de nomes.

### Alinhamento entre resultado, atividade e evidência

| Resultado pretendido | Atividade | Evidência |
|---|---|---|
| identificar invariantes | localizar regras no código | arquivo, método e justificativa |
| proteger estado | experimentar operações inválidas | exceção e estado preservado |
| modelar associação | implementar grupo e produto | relação consistente nos dois sentidos |
| usar precisão decimal | calcular valor de estoque | teste com `BigDecimal` |
| transferir o modelo | criar domínio temático | classes, regras e pelo menos oito testes |

### Situação perante o padrão pedagógico

A aula atende apresentação, problema orientador, resultados, fundamentos, desenvolvimento incremental, diagnóstico, segurança/qualidade, transferência, avaliação, checklist, orientação docente e referências.

### Recomendações

1. Acrescentar uma pequena comparação entre modelo rico e modelo anêmico sem antecipar services.
2. Explicitar que a ausência de `equals/hashCode` é uma decisão desta etapa, não uma regra universal para entidades.
3. Manter os testes da aula independentes de Spring para preservar feedback rápido.

---

## Aula 04 — Persistência com JPA, PostgreSQL, profiles e Liquibase

### Finalidade pedagógica observada

A aula conecta o domínio ao PostgreSQL e estabelece o Liquibase como fonte de verdade do esquema. O Hibernate passa a validar a correspondência entre classes e banco, em vez de alterar silenciosamente a estrutura.

### Pontos fortes

- diferencia persistência, JDBC, JPA, Hibernate, Spring Data JPA e Liquibase;
- explica o desencontro objeto-relacional;
- trata migração como histórico executável;
- distribui integridade entre domínio, mapeamento e banco;
- separa `dev`, `test` e `prod` com PostgreSQL em todos os ambientes;
- mantém segredos fora do Git;
- cria usuário de aplicação com menor privilégio;
- explica changelog, changeSet, checksum e lock;
- implementa PK, FK, `UNIQUE`, `NOT NULL` e `CHECK`;
- usa `ddl-auto=validate`;
- verifica o histórico em `databasechangelog`;
- testa mapeamentos e constraints no PostgreSQL real;
- inclui atividade orientada, atividade autônoma e rubrica.

### Alinhamento entre resultado, atividade e evidência

| Resultado pretendido | Atividade | Evidência |
|---|---|---|
| explicar responsabilidades | mapear Java, Hibernate, Liquibase e banco | tabela teoria–prática |
| criar esquema reproduzível | escrever changelogs YAML | 8 changeSets aplicados no marco da aula |
| configurar ambientes | criar profiles e `.env` | aplicação e testes conectados aos bancos corretos |
| validar integridade | provocar violações controladas | erro do PostgreSQL interpretado |
| transferir persistência | criar esquema do tema próprio | diagrama, testes e justificativa das constraints |

### Situação perante o padrão pedagógico

A aula é uma das mais completas do conjunto: combina fundamentação, checkpoints, diagnóstico, segurança, atividade autônoma, rubrica e ponto recuperável.

### Recomendações

1. Manter explícito que os 17 testes citados representam o marco histórico da Aula 04, não a quantidade atual da suíte.
2. Preservar a regra de nunca apontar profiles destrutivos para bancos compartilhados.
3. Acrescentar, quando houver infraestrutura de CI, um exemplo de aplicação das migrações em banco descartável do pipeline.

---

## Aula 05 — Spring Data JPA, repositories, serviços e transações

### Finalidade pedagógica observada

A aula introduz a camada de aplicação sem acrescentar HTTP. Essa restrição permite observar repositories, proxies, contexto de persistência, transações, rollback e dirty checking antes de lidar com serialização e status HTTP.

### Pontos fortes

- separa responsabilidades de entidade, repository, service e banco;
- explica como o Spring cria implementações de interfaces repository;
- introduz consultas derivadas de forma proporcional;
- posiciona a transação em torno do caso de uso;
- diferencia leitura e escrita;
- apresenta os estados transient, managed, detached e removed;
- demonstra dirty checking sem incentivar setters arbitrários;
- cria exceções próprias da aplicação sem acoplá-las a HTTP;
- testa sucesso, duplicidade, recurso inexistente e rollback;
- mantém PostgreSQL como infraestrutura real de teste;
- contém atividade de transferência e avaliação objetiva.

### Alinhamento entre resultado, atividade e evidência

| Resultado pretendido | Atividade | Evidência |
|---|---|---|
| implementar acesso abstrato | criar `JpaRepository` | operações e queries derivadas funcionando |
| coordenar caso de uso | criar services | cadastro com regras e relacionamentos |
| explicar transação | provocar falha após operação | rollback demonstrado |
| observar dirty checking | alterar entidade managed | `UPDATE` sem `save` adicional |
| transferir arquitetura | aplicar ao tema | repositories, services e justificativas |

### Situação perante o padrão pedagógico

O material é mais conciso que as Aulas 03 e 04, mas atende aos elementos essenciais. A avaliação usa três níveis, suficiente para o incremento, embora o padrão recomende quatro níveis quando possível.

### Recomendações

1. Acrescentar checklist editorial explícito antes do ponto de quebra.
2. Separar visualmente atividade orientada e atividade autônoma.
3. Incluir orientação ao professor sobre demonstração de proxy transacional e chamadas internas que não atravessam o proxy.

---

## Aula 06 — Evolução do modelo e geração assistida de changelogs

### Finalidade pedagógica observada

A aula ensina que automação produz um rascunho, não uma decisão de migração. O estudante compara um banco anterior com um schema de referência, identifica ruídos e transforma o diff em uma migração revisada e segura.

### Pontos fortes

- diferencia geração de schema, diff e migração oficial;
- declara limitações reais da integração Liquibase/Hibernate utilizada;
- usa dois bancos PostgreSQL descartáveis;
- evolui o domínio com fornecedor e estoque mínimo;
- preserva temporariamente compatibilidade de construtor;
- demonstra o risco de `ddl-auto=create` fora de ambiente descartável;
- identifica operações perigosas no diff automático;
- aplica expand–migrate–contract para coluna obrigatória;
- preserva dados existentes com backfill;
- adiciona checks, nomes estáveis e rollbacks ausentes na geração;
- verifica convergência entre classes e banco migrado;
- exige registro dos problemas encontrados pelo estudante.

### Alinhamento entre resultado, atividade e evidência

| Resultado pretendido | Atividade | Evidência |
|---|---|---|
| avaliar automação | gerar e inspecionar diff | problemas do rascunho registrados |
| preservar dados | adicionar coluna em etapas | linhas antigas mantidas e preenchidas |
| revisar migração | escrever changeSet 003 | nomes, checks e rollback explícitos |
| provar convergência | executar testes e novo diff | schema compatível com as classes |
| transferir técnica | evoluir tema próprio | rascunho criticado e migração revisada |

### Correções pedagógicas incorporadas

- seção própria de segurança e qualidade, com confirmação dos bancos antes de operações destrutivas;
- atividade orientada para comparação entre rascunho e migração final;
- atividade autônoma com entregáveis e teste de preservação;
- rubrica de quatro níveis;
- checklist operacional e editorial;
- orientações ao professor com tempo, demonstrações e falhas controladas.

### Situação perante o padrão pedagógico

A aula passa a atender os elementos previstos no padrão. Deve-se preservar, em cada oferta, a preparação prévia dos bancos descartáveis e a proibição de demonstrações destrutivas sobre dados relevantes.

---

## Aula 07 — API REST, DTOs, mapeadores e testes com Postman

### Finalidade pedagógica observada

A aula expõe os casos de uso por HTTP e separa contrato externo, transformação, aplicação e persistência. MockMvc verifica a integração automatizada; Postman permite explorar e demonstrar o mesmo contrato como cliente externo.

### Pontos fortes

- retoma API, HTTP, recursos, representações e idempotência;
- diferencia controller, DTO, mapper, service, repository e domínio;
- explica por que entidades JPA não devem ser o contrato HTTP;
- usa records de entrada e saída;
- combina Bean Validation com invariantes do domínio;
- mantém resolução de IDs relacionados no service;
- retorna `201 Created` e `Location` no cadastro;
- padroniza `400`, `404` e `409`;
- evita vazamento de stack trace, SQL e credenciais;
- usa MockMvc sem abrir porta, mas com aplicação e PostgreSQL reais;
- oferece laboratório Postman completo com variáveis e scripts;
- inclui caminho feliz e cenários negativos;
- explica exportação segura da coleção;
- apresenta alternativa equivalente com `curl`.

### Alinhamento entre resultado, atividade e evidência

| Resultado pretendido | Atividade | Evidência |
|---|---|---|
| projetar contrato externo | criar DTOs | JSON independente das entidades |
| mapear representações | implementar mappers | conversão explícita e testável |
| aplicar semântica HTTP | criar controllers | status, corpo e `Location` corretos |
| padronizar falhas | criar advice | `ApiError` previsível |
| testar contrato | MockMvc e Postman | testes automatizados e coleção executada |

### Correções pedagógicas incorporadas

- apresentação e problema orientador explícitos;
- tabela de mapeamento teoria–prática;
- rubrica de quatro níveis para contrato, validação, semântica, testes e segurança;
- checklist de encerramento;
- orientações para o professor com sequência, perguntas e falhas controladas;
- delimitação explícita entre o contrato da Aula 07 e as operações reservadas à Aula 08.

### Situação perante o padrão pedagógico

A aula passa a atender o padrão editorial. Permanece como extensão opcional a publicação de uma coleção Postman revisada e sem segredos; o roteiro textual já é suficiente para desenvolver e testar a aula sem esse artefato.

---

## Aula 08 — CRUD completo, estoque, filtros e paginação

### Finalidade pedagógica observada

A aula completa as operações de grupo e produto e diferencia substituição de dados, transição de status, movimentação e exclusão. Também introduz consultas escaláveis, impedindo que filtros sejam realizados sobre listas inteiras em memória.

### Pontos fortes

- apresenta contrato completo das novas rotas;
- fundamenta `PUT` pela idempotência;
- justifica `POST` para movimentações não idempotentes;
- diferencia inativação de exclusão física;
- separa campos editáveis de campos controlados pelo servidor;
- mantém consistência dos dois lados ao trocar o grupo;
- protege exclusão de grupo em uso com `409` e FK;
- reforça unicidade normalizada no PostgreSQL;
- cria DTOs específicos para atualização, status e estoque;
- usa `Specification` para filtros combináveis;
- usa `Pageable` para paginação e ordenação;
- limita a página a 100 elementos;
- valida campos permitidos para ordenação;
- devolve contrato próprio `PaginaResponse`;
- documenta o fluxo `GET por ID → formulário → PUT`;
- inclui testes de domínio, service, persistência e MockMvc;
- apresenta `curl`, atualização da coleção Postman e diagnóstico;
- contém transferência, questões, critérios e ponto de quebra.

### Alinhamento entre resultado, atividade e evidência

| Resultado pretendido | Atividade | Evidência |
|---|---|---|
| aplicar semântica de métodos | implementar PUT, POST e DELETE | efeitos e status coerentes |
| preservar invariantes | movimentar estoque pelo domínio | saldo nunca negativo |
| proteger integridade | excluir grupo em uso | resposta `409` e linha preservada |
| consultar com escala | combinar filtros e página | SQL executado pelo banco e metadados corretos |
| alimentar tela de alteração | consultar por ID antes do PUT | DTO completo e teste MockMvc |
| transferir solução | adaptar ao tema individual | contrato, código, testes e justificativa |

### Evidência de execução

O protótipo associado foi validado por 48 testes no PostgreSQL, abrangendo domínio, aplicação, persistência e contrato HTTP. Essa evidência técnica complementa, sem substituir, as atividades e os critérios pedagógicos da aula.

### Correções pedagógicas incorporadas

- aviso destacado sobre a mudança incompatível de array para `PaginaResponse`;
- rubrica de quatro níveis;
- orientações para o professor com divisão sugerida em dois encontros;
- demonstrações, perguntas, falhas controladas e extensões opcionais.

### Situação perante o padrão pedagógico

A aula passa a atender o padrão editorial. Permanecem como evoluções curriculares futuras, e não como lacunas desta aula, o tratamento de concorrência com locking e o registro persistente das movimentações.

---

## Aula 09 — OpenAPI: contrato executável e documentação da API

### Finalidade pedagógica observada

A aula transforma a API implementada nas aulas anteriores em um contrato explícito, legível por pessoas e processável por ferramentas. O estudante diferencia especificação, documento, interface visual e biblioteca geradora, analisa o resultado automático e acrescenta o significado de negócio que não pode ser deduzido apenas das assinaturas Java.

### Pontos fortes

- parte do problema concreto de uma equipe consumidora sem acesso ao código-fonte;
- distingue OpenAPI, documento OpenAPI, Swagger UI e `springdoc-openapi`;
- compara as estratégias design-first e code-first sem apresentar uma delas como universal;
- relaciona a geração automática do contrato à revisão humana estudada na Aula 06;
- usa uma dependência compatível com Spring Boot 4;
- configura título, descrição, versão, contato e documentação externa;
- organiza controllers por recurso com `@Tag`;
- documenta intenção, parâmetros e respostas com `@Operation`, `@Parameter` e `@ApiResponse`;
- representa sucessos e erros, incluindo `ApiError`;
- acrescenta exemplos e significado aos DTOs com `@Schema`;
- explica filtros, ordenação e `PaginaResponse`;
- disponibiliza JSON, YAML e Swagger UI nos ambientes de estudo;
- desabilita conscientemente a documentação no profile de produção;
- cria teste MockMvc específico para metadados, caminhos, respostas e schemas;
- fornece laboratório exploratório e coleção Postman importável;
- diferencia exploração manual de verificação automatizada;
- inclui diagnóstico, segurança, transferência, rubrica e orientação docente.

### Alinhamento entre resultado, atividade e evidência

| Resultado pretendido | Atividade | Evidência |
|---|---|---|
| diferenciar os componentes | comparar especificação, documento, UI e biblioteca | explicação conceitual e mapa do fluxo |
| ler um contrato | localizar `info`, `paths`, respostas e schemas | inspeção do JSON ou YAML |
| gerar documentação | integrar o springdoc | `/v3/api-docs` e Swagger UI disponíveis |
| acrescentar semântica | documentar operações, DTOs e erros | contrato enriquecido e navegável |
| verificar regressões | testar rotas e schemas essenciais | `OpenApiDocumentationTest` aprovado |
| atuar como consumidor | executar cenários sem consultar controllers | laboratório Swagger UI e coleção Postman |
| transferir conhecimento | documentar a API temática | contrato e justificativas do estudante |

### Evidência de execução

O protótipo foi validado com 49 testes no PostgreSQL. O teste adicional consulta `/v3/api-docs` dentro do contexto Spring e confirma versão, metadados, operações essenciais, conteúdo de respostas e schemas. A coleção Postman contém todas as operações atuais, variáveis internas, captura automática de IDs e cenários de sucesso e erro.

### Segurança e qualidade

- exemplos usam dados fictícios;
- a coleção não contém senha, token ou chave de API;
- a documentação permanece ativa em desenvolvimento e teste;
- o profile `prod` desabilita documento e interface até que sua publicação seja deliberada;
- Swagger UI é tratada como cliente exploratório, não como substituta de MockMvc;
- o documento gerado é tratado como evidência revisável, não como verdade automática.

### Situação perante o padrão pedagógico

A aula atende ao padrão definido. Há contextualização, resultados observáveis, fundamentos, mapeamento teoria–prática, desenvolvimento incremental, diagnóstico, segurança, transferência, atividades, rubrica, ponto de quebra e orientação docente. A implementação e o material estão alinhados, e a coleção Postman oferece um artefato executável adicional.

---

## Cobertura acumulada das previsões

A revisão inicial registrava conteúdos que deveriam aparecer nas aulas futuras. O quadro abaixo mostra onde foram efetivamente desenvolvidos.

| Conteúdo previsto | Fundamentação esperada | Situação em 29/09/2026 |
|---|---|---|
| modelo de domínio | entidade, estado, comportamento e invariantes | Atendido na Aula 03 |
| entidades JPA | identidade, persistência e ORM | Atendido na Aula 04 |
| relacionamentos | cardinalidade, consistência e integridade referencial | Atendido nas Aulas 03, 04 e 06 |
| repositories | abstração de acesso e consultas | Atendido na Aula 05 |
| services | casos de uso, coesão e transações | Atendido na Aula 05 |
| Liquibase | migração incremental, checksum e rollback | Atendido nas Aulas 04 e 06; ampliado na 08 |
| configuração externa | profiles, precedência e segredos | Atendido na Aula 04 |
| evolução segura do esquema | diff, revisão humana e preservação de dados | Atendido na Aula 06 |
| DTOs | separação entre domínio e contrato externo | Atendido na Aula 07 |
| controllers REST | contrato HTTP, status e representações | Atendido na Aula 07; CRUD ampliado na 08 |
| tratamento de erros | taxonomia de falhas e não vazamento | Atendido nas Aulas 07 e 08 |
| testes HTTP | MockMvc e contrato da API | Atendido nas Aulas 07 e 08 |
| testes manuais | cliente HTTP, variáveis e cenários | Atendido na Aula 07; coleção consolidada na 09 |
| atualização com `PUT` | idempotência e campos controlados | Atendido na Aula 08 |
| exclusão | integridade e conflito de recurso em uso | Atendido na Aula 08 |
| paginação e filtros | consulta no banco, limites e ordenação | Atendido na Aula 08 |
| segurança | segredos, validação e menor privilégio | Parcialmente atendido de forma transversal |
| autenticação e autorização | identidade do usuário e controle de acesso | Ainda não abordado |
| OpenAPI | contrato executável e documentação da API | Atendido na Aula 09 |
| Docker como conteúdo | imagem, contêiner, rede e volume | Usado como infraestrutura; aula própria ainda pendente |
| implantação | artefato, configuração, observabilidade e entrega | Ainda não abordado |

### Superações importantes

As limitações registradas na Aula 02 foram retomadas da seguinte forma:

- testes HTTP com MockMvc: Aulas 07 e 08;
- DTOs e validação de entrada: Aula 07;
- erros padronizados: Aulas 07 e 08;
- consultas por ID para telas de alteração: Aulas 07 e 08;
- atualização idempotente com `PUT`: Aula 08;
- exclusão e conflitos de integridade: Aula 08;
- pesquisa, filtros, ordenação e paginação: Aula 08;
- contrato OpenAPI, schemas e documentação interativa: Aula 09;
- coleção Postman completa e versionada: Aula 09.

---

## Coerência da progressão até a Aula 09

| Aula | Incremento principal | Dependência conceitual anterior |
|---:|---|---|
| 00 | repositório e fluxo Git | nenhuma |
| 01 | ambiente validado | repositório preparado |
| 02 | aplicação e health check | ambiente e Git |
| 03 | domínio Java puro | tema definido e projeto executável |
| 04 | JPA, PostgreSQL e Liquibase | domínio com invariantes |
| 05 | repositories, services e transações | persistência validada |
| 06 | evolução do modelo e migração assistida | histórico Liquibase e serviços |
| 07 | API REST, DTOs, erros e MockMvc | casos de uso transacionais |
| 08 | atualização, exclusão, estoque e consultas paginadas | contrato REST básico |
| 09 | OpenAPI, Swagger UI e teste do contrato | API com operações e representações estáveis |

A progressão preserva uma decisão pedagógica consistente: cada aula introduz uma camada ou problema novo sobre um estado executável já compreendido.

---

## Débitos pedagógicos e editoriais atuais

### Prioridade alta

1. **Segurança concorrente do estoque:** abordar perda de atualização e locking antes de uso multiusuário real.
2. **Evolução do domínio comercial:** introduzir clientes, colaboradores, vendas e itens de venda preservando coesão, histórico de preços e atomicidade.

### Prioridade média

1. **Atividades autônomas:** algumas aulas combinam transferência e atividade orientada, mas nem sempre separam claramente o trabalho com apoio do trabalho individual.
2. **Acessibilidade de diagramas:** manter explicações textuais próximas aos Mermaid.
3. **Atualização visual da Aula 01:** substituir capturas históricas na trilha principal.
4. **Manutenção do contrato:** incorporar a revisão do OpenAPI ao checklist de toda nova operação.

### Prioridade futura

1. Docker como conteúdo, não apenas pré-requisito operacional.
2. Observabilidade, logs estruturados e correlação de requisições.
3. Empacotamento, implantação e configuração por ambiente.
4. Concorrência em movimentações de estoque e controle de versão otimista.
5. Estratégia de autenticação, autorização e auditoria, mantida fora do escopo imediato do curso.

---

## Decisões para continuidade

1. Usar o [padrão pedagógico](PADRAO-PEDAGOGICO.md) como checklist obrigatório de autoria e revisão.
2. Manter resultados, atividades, evidências e avaliação alinhados.
3. Introduzir conceitos quando houver uma prática que os torne observáveis, salvo organizadores prévios indispensáveis.
4. Não apresentar código novo sem explicar responsabilidade, contrato, forma de verificação e erros prováveis.
5. Manter o projeto executável ao final de cada aula.
6. Criar tags somente depois de testes, revisão do diff e verificação de segredos.
7. Registrar simplificações didáticas e indicar onde serão superadas.
8. Preservar PostgreSQL nos testes de integração quando o comportamento do SGBD fizer parte do objetivo.
9. Manter testes unitários rápidos para regras que não exigem infraestrutura.
10. Atualizar versões, telas e recomendações de segurança antes de cada oferta anual.
11. Não alterar silenciosamente contratos usados por materiais anteriores; mudanças como a paginação da Aula 08 devem ser anunciadas.
12. Revisar esta análise sempre que uma aula, contrato HTTP ou versão estrutural do projeto mudar.
13. Atualizar OpenAPI e coleção Postman sempre que uma operação HTTP for criada ou alterada.

## Parecer final

As Aulas 00 a 02 atendem ao papel de fundação do curso. Elas não se limitam a procedimentos: apresentam modelos mentais, exigem evidências, incluem diagnóstico e preparam a transferência para um domínio escolhido pelo estudante.

As Aulas 03 a 09 concretizam a progressão anunciada: domínio, persistência, transações, evolução de esquema, API REST, operações completas, consultas paginadas e documentação executável. A implementação acompanha a documentação e foi validada no PostgreSQL por testes de domínio, aplicação, persistência, HTTP e contrato OpenAPI.

O material pode ser utilizado como base da oferta de 2026. As Aulas 00 a 09 atendem de forma consistente ao padrão definido, preservadas as melhorias futuras registradas neste documento. Os próximos passos coerentes são a evolução do domínio comercial, a segurança concorrente do estoque, Docker como objeto de estudo, observabilidade e implantação. Autenticação e autorização permanecem deliberadamente fora do escopo imediato.

---

[⬅ Voltar para o índice do curso](../README.md)
