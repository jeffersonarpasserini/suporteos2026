package com.curso.suporteos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemVendaRequest(
        @NotNull @Schema(example = "1") Long produtoId,
        @NotNull @DecimalMin(value = "0.001") @Digits(integer = 15, fraction = 3)
        @Schema(example = "2.000") BigDecimal quantidade) { }
