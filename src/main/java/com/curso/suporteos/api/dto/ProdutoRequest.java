package com.curso.suporteos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Dados para cadastrar um produto")
public record ProdutoRequest(
        @Schema(description = "Código único do produto", example = "7890000000001")
        @NotBlank(message = "Código de barras é obrigatório")
        @Size(max = 50, message = "Código de barras deve possuir no máximo 50 caracteres")
        String codigoBarras,

        @Schema(description = "Descrição comercial", example = "Caderno universitário")
        @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 150, message = "Descrição deve possuir no máximo 150 caracteres")
        String descricao,

        @Schema(description = "Saldo inicial, que não pode ser negativo", example = "10.000")
        @NotNull(message = "Saldo de estoque é obrigatório")
        @PositiveOrZero(message = "Saldo de estoque não pode ser negativo")
        BigDecimal saldoEstoque,

        @Schema(description = "Preço atual por unidade", example = "18.90")
        @NotNull(message = "Valor unitário é obrigatório")
        @PositiveOrZero(message = "Valor unitário não pode ser negativo")
        BigDecimal valorUnitario,

        @Schema(description = "Limite usado para indicar reposição", example = "3.000")
        @NotNull(message = "Estoque mínimo é obrigatório")
        @PositiveOrZero(message = "Estoque mínimo não pode ser negativo")
        BigDecimal estoqueMinimo,

        @Schema(description = "Identificador de um grupo existente", example = "1")
        @NotNull(message = "Grupo é obrigatório")
        @Positive(message = "Identificador do grupo deve ser positivo")
        Long grupoId,

        @Schema(description = "Identificador opcional de um fornecedor existente", example = "1",
                nullable = true)
        @Positive(message = "Identificador do fornecedor deve ser positivo")
        Long fornecedorId) {
}
