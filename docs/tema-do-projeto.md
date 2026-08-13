# Tema do projeto de referência

## Identificação

- Nome: `suporteos2026`
- Tema: controle simplificado de itens de estoque
- Objetivo: cadastrar produtos e organizá-los por grupos de produtos

## Entidade de classificação

- Nome no singular: `GrupoProduto`
- Nome no plural: grupos de produtos
- Descrição: classificação utilizada para organizar produtos
- Exemplos: Limpeza, Alimentos e Papelaria
- Status: ativo ou inativo

## Entidade principal

- Nome no singular: `Produto`
- Nome no plural: produtos
- Código único: código de barras
- Descrição: nome ou descrição comercial do produto
- Medida quantitativa: saldo em estoque
- Valor monetário: valor unitário
- Valor calculado: saldo em estoque multiplicado pelo valor unitário
- Data relevante: data de cadastro
- Status: ativo ou inativo

## Relacionamento

- Um grupo de produtos pode classificar vários produtos.
- Cada produto pertence a um grupo de produtos.

## Exemplos

| Grupo | Código | Produto | Saldo | Valor unitário |
|---|---|---|---:|---:|
| Limpeza | `7890000000001` | Detergente | 20 | 3,50 |
| Alimentos | `7890000000002` | Arroz | 15 | 24,90 |
| Papelaria | `7890000000003` | Caderno | 10 | 18,00 |

## Correspondência para os projetos dos estudantes

Cada estudante escolherá outro tema, mas deverá manter uma estrutura conceitualmente equivalente:

```text
GrupoProduto -> entidade de classificação do tema
Produto      -> entidade principal do tema
```

O tema individual deverá permitir código único, descrição, medida quantitativa, valor monetário, data, status e relacionamento com a classificação.
