package com.curso.suporteos.api.dto;

import com.curso.suporteos.domain.Status;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Representação completa de um produto")
public record ProdutoResponse(
        @Schema(description = "Identificador do produto", example = "1")
        Long id,
        @Schema(description = "Código único do produto", example = "7890000000001")
        String codigoBarras,
        @Schema(description = "Descrição comercial", example = "Caderno universitário")
        String descricao,
        @Schema(description = "Saldo atual", example = "10.000")
        BigDecimal saldoEstoque,
        @Schema(description = "Preço atual por unidade", example = "18.90")
        BigDecimal valorUnitario,
        @Schema(description = "Limite usado para indicar reposição", example = "3.000")
        BigDecimal estoqueMinimo,
        @Schema(description = "Saldo multiplicado pelo valor unitário", example = "189.00")
        BigDecimal valorEstoque,
        @Schema(description = "Data de entrada do produto no cadastro", example = "2026-09-29")
        LocalDate dataCadastro,
        @Schema(description = "Situação do produto", example = "ATIVO")
        Status status,
        @Schema(description = "Identificador do grupo", example = "1")
        Long grupoId,
        @Schema(description = "Nome do grupo", example = "Papelaria")
        String grupoNome,
        @Schema(description = "Identificador do fornecedor, quando associado", example = "1",
                nullable = true)
        Long fornecedorId,
        @Schema(description = "Razão social do fornecedor, quando associado",
                example = "Papelaria Central Ltda.", nullable = true)
        String fornecedorRazaoSocial) {
}
