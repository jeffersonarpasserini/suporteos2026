# Plano de continuidade — Aulas 14 a 22

[⬅ Voltar para o índice do curso](../README.md)

---

## Finalidade

Este documento registra a continuidade do curso após o marco `aula-13-jwt`. Ele funciona como planejamento curricular e evita que decisões sobre sequência, pré-requisitos e limites de escopo se percam entre a produção de uma aula e outra.

> **Situação atual:** as Aulas 14 e 15 foram implementadas. As Aulas 16 a 22 permanecem como proposta de continuidade.

A proposta ainda pode ser refinada durante a autoria. Uma aula somente passa a integrar definitivamente o curso depois que material, implementação, testes, commit e tag forem concluídos.

## Princípio de progressão

As próximas aulas devem fechar quatro blocos:

1. **Segurança completa:** autorização, testes, ciclo de tokens e auditoria.
2. **Infraestrutura:** conteinerização e observabilidade.
3. **Entrega:** integração contínua e implantação.
4. **Consolidação:** revisão arquitetural e projeto final.

```text
Aula 13 — autenticação representada por JWT
    ↓
Aula 14 — autorização por perfil
    ↓
Aula 15 — estratégia de testes e cobertura
    ↓
Aula 16 — renovação, revogação e inatividade
    ↓
Aula 17 — auditoria e proteção da autenticação
    ↓
Aula 18 — aplicação e banco em contêineres
    ↓
Aula 19 — observabilidade da aplicação em execução
    ↓
Aula 20 — verificação automatizada no GitHub
    ↓
Aula 21 — implantação e operação em produção
    ↓
Aula 22 — consolidação e apresentação do projeto
```

## Visão geral

| Aula | Tema | Entrega principal | Tag sugerida |
|---:|---|---|---|
| 14 | Autorização e controle de acesso | operações protegidas por perfil | Implementada; tag `aula-14-autorizacao` |
| 15 | Testes de software e cobertura | estratégia de testes e relatório JaCoCo | Implementada; tag prevista `aula-15-testes-cobertura` |
| 16 | Ciclo de vida dos tokens | refresh token, rotação, revogação e logout | `aula-16-refresh-token` |
| 17 | Auditoria e proteção da autenticação | tentativas, bloqueio, auditoria e limitação de requisições | `aula-17-auditoria-seguranca` |
| 18 | Docker e conteinerização | aplicação e PostgreSQL executados com Docker Compose | `aula-18-docker` |
| 19 | Observabilidade | health checks, logs, correlação e métricas | `aula-19-observabilidade` |
| 20 | Integração contínua | pipeline de build, testes, cobertura e imagem | `aula-20-integracao-continua` |
| 21 | Implantação | aplicação configurada e operada em ambiente de produção | `aula-21-implantacao` |
| 22 | Consolidação e projeto final | projeto temático documentado, validado e apresentado | `aula-22-projeto-final` |

---

## Aula 14 — Autorização e controle de acesso

### Pergunta orientadora

> Depois que a aplicação confirma a identidade, como decidir quais operações essa pessoa pode executar?

### Fundamentação prevista

- diferença entre autenticação e autorização;
- papéis, authorities, permissões e políticas de acesso;
- princípio do menor privilégio;
- diferença entre `401 Unauthorized` e `403 Forbidden`;
- segurança na camada HTTP e na camada de aplicação;
- riscos de confiar em perfis enviados pelo cliente.

### Implementação prevista

- converter o claim `perfis` do JWT em authorities do Spring Security;
- habilitar segurança de métodos;
- aplicar `@PreAuthorize` em casos de uso selecionados;
- definir permissões de operador e administrador;
- restringir criação e administração de contas;
- substituir o bootstrap público provisório por um fluxo controlado;
- atualizar OpenAPI e coleção Postman;
- testar acessos permitidos, não autenticados e proibidos.

### Limite de escopo

Não implementar ainda refresh token, revogação, auditoria persistente ou limitação de tentativas. Esses assuntos dependem de uma política de autorização já compreendida.

---

## Aula 15 — Testes de software e cobertura

> **Situação:** implementada no código e no material; aguardando commit e tag.

### Pergunta orientadora

> Como obter evidências de que regras, integrações e controles de segurança continuam corretos depois de uma mudança?

### Fundamentação prevista

- qualidade, verificação e validação;
- pirâmide de testes;
- teste unitário, de integração, de componente e de sistema;
- Arrange–Act–Assert;
- doubles, stubs, mocks e spies;
- cobertura de linhas e branches;
- relação entre cobertura, qualidade e risco;
- limites de uma meta percentual isolada.

### Implementação prevista

- organizar a suíte existente por responsabilidade;
- aprofundar JUnit e Mockito;
- testar domínio, services, persistência, API e segurança;
- configurar JaCoCo no Maven;
- gerar e interpretar o relatório de cobertura;
- definir uma regra inicial de cobertura para o build;
- identificar código não testado e criar testes orientados por risco.

### Limite de escopo

Cobertura não será tratada como prova de ausência de defeitos. Testes de carga, segurança ofensiva e mutação poderão aparecer como extensões futuras.

---

## Aula 16 — Ciclo de vida dos tokens

### Pergunta orientadora

> Como renovar uma autenticação sem manter um access token válido indefinidamente e como encerrar o acesso antes do vencimento absoluto?

### Fundamentação prevista

- access token e refresh token;
- validade absoluta e validade por inatividade;
- sessão deslizante;
- rotação de refresh token;
- reutilização indevida e revogação;
- logout local e logout global;
- estado mínimo no servidor;
- relógio e segurança temporal.

### Implementação prevista

- persistir refresh tokens de forma protegida;
- emitir access e refresh token no login;
- criar endpoint de renovação;
- rotacionar o refresh token a cada uso;
- revogar a cadeia no logout ou na detecção de reutilização;
- bloquear renovação de conta inativa;
- testar vencimento com relógio controlado.

### Limite de escopo

Não transformar a aplicação em um Authorization Server completo nem introduzir login social. O objetivo é observar o ciclo de uma sessão renovável dentro do recorte didático atual.

---

## Aula 17 — Auditoria e proteção da autenticação

### Pergunta orientadora

> Como detectar abuso, investigar eventos de segurança e reduzir tentativas automatizadas contra a autenticação?

### Fundamentação prevista

- evento de segurança e trilha de auditoria;
- autenticação versus não repúdio;
- brute force, enumeração de contas e credential stuffing;
- limitação de taxa e bloqueio temporário;
- minimização de dados sensíveis em logs;
- respostas de erro que não revelam a existência da conta.

### Implementação prevista

- registrar sucessos e falhas relevantes sem armazenar senhas ou tokens;
- limitar tentativas de login;
- bloquear temporariamente quando a política for excedida;
- auditar alteração de senha, perfis, bloqueio e revogação;
- criar consultas administrativas protegidas;
- testar os limites e a liberação após o período configurado.

---

## Aula 18 — Docker e conteinerização

### Pergunta orientadora

> Como executar a aplicação e suas dependências de maneira reproduzível sem depender da configuração particular de cada computador?

### Fundamentação prevista

- imagem, contêiner, registry e runtime;
- diferença entre contêiner e máquina virtual;
- camadas e cache de imagem;
- rede, porta, volume e variável de ambiente;
- processo principal e ciclo de vida do contêiner;
- persistência de dados fora do contêiner.

### Implementação prevista

- criar `.dockerignore`;
- criar `Dockerfile` com build em múltiplos estágios;
- executar a aplicação como usuário não privilegiado;
- criar `compose.yaml` para aplicação e PostgreSQL;
- configurar rede, volume e health checks;
- fornecer variáveis sem gravar segredos na imagem;
- aplicar Liquibase durante a inicialização;
- validar reinício e preservação dos dados.

---

## Aula 19 — Observabilidade

### Pergunta orientadora

> Como compreender o estado da aplicação depois que ela está executando fora da IDE?

### Fundamentação prevista

- diferença entre monitoramento e observabilidade;
- logs, métricas e traces;
- disponibilidade e prontidão;
- correlação de requisições;
- sinais técnicos e sinais de negócio;
- riscos de exposição de endpoints operacionais.

### Implementação prevista

- adicionar Spring Boot Actuator;
- configurar liveness e readiness;
- produzir logs estruturados e identificador de correlação;
- expor somente métricas necessárias;
- proteger endpoints administrativos;
- observar pool de conexões, JVM e requisições HTTP;
- integrar health checks ao Docker Compose.

---

## Aula 20 — Integração contínua

### Pergunta orientadora

> Como garantir que todo commit publicado seja compilado e verificado da mesma maneira?

### Fundamentação prevista

- integração contínua e feedback rápido;
- pipeline, job, step, runner e artefato;
- build reproduzível;
- qualidade como gate;
- separação entre integração contínua e implantação contínua;
- segurança de secrets no pipeline.

### Implementação prevista

- criar workflow do GitHub Actions;
- selecionar Java 21 e usar Maven Wrapper;
- disponibilizar PostgreSQL de teste;
- executar Liquibase e a suíte completa;
- verificar cobertura;
- empacotar o JAR;
- construir a imagem Docker;
- manter credenciais fora do workflow versionado.

---

## Aula 21 — Implantação e ambiente de produção

### Pergunta orientadora

> O que precisa mudar quando a aplicação deixa o ambiente de desenvolvimento e passa a atender usuários reais?

### Fundamentação prevista

- artefato, release e ambiente;
- configuração externa e secrets;
- TLS e terminação HTTPS;
- migração compatível e estratégia de rollback;
- backup e recuperação;
- disponibilidade, capacidade e operação;
- diferenças entre rollback de aplicação e rollback de dados.

### Implementação prevista

- revisar o profile `prod`;
- fornecer configuração por variáveis de ambiente;
- executar a imagem produzida pelo pipeline;
- proteger documentação e endpoints operacionais;
- planejar aplicação das migrações;
- executar verificação pós-implantação;
- documentar backup, restauração e rollback.

---

## Aula 22 — Consolidação e projeto final

### Pergunta orientadora

> Como demonstrar que o projeto temático resolve seu problema, possui coerência arquitetural e pode ser reconstruído por outra pessoa?

### Entregas previstas

- revisão do problema e das regras do domínio;
- diagrama atualizado da arquitetura e do modelo;
- histórico Liquibase completo;
- contrato OpenAPI coerente com a implementação;
- coleção Postman executável;
- suíte e cobertura aprovadas;
- imagem Docker reproduzível;
- pipeline aprovado;
- instruções de execução em ambiente limpo;
- análise das simplificações e riscos restantes;
- apresentação e demonstração do projeto temático.

### Avaliação sugerida

A avaliação deve considerar domínio, persistência, contrato HTTP, segurança, testes, operação, documentação, versionamento e capacidade do estudante de justificar decisões — não apenas a quantidade de funcionalidades.

---

## Critérios para confirmar cada aula

Antes de transformar uma proposta em marco oficial:

- fundamentar o problema e os conceitos;
- declarar pré-requisitos e limites de escopo;
- implementar o incremento no projeto de referência;
- transferir a atividade para o tema do estudante;
- atualizar Liquibase, OpenAPI e Postman quando aplicável;
- criar testes proporcionais ao risco;
- registrar diagnóstico de erros comuns;
- revisar segurança e segredos;
- executar a suíte completa;
- revisar o diff;
- criar commit e tag somente sobre um estado reproduzível.

---

[⬅ Voltar para o índice do curso](../README.md)
