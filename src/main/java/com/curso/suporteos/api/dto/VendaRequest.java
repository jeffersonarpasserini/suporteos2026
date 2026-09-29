package com.curso.suporteos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record VendaRequest(
        @NotNull @Schema(example = "1") Long clienteId,
        @NotNull @Schema(example = "1") Long vendedorId) { }
