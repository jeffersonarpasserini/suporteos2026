package com.curso.suporteos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Quantidade de uma entrada ou saída de estoque")
public record MovimentacaoEstoqueRequest(
        @Schema(description = "Quantidade positiva a movimentar", example = "5.000")
        @NotNull(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade deve ser maior que zero")
        BigDecimal quantidade) {
}
