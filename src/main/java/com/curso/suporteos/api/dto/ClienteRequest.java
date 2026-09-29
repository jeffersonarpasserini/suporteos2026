package com.curso.suporteos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotNull @Schema(example = "1") Long pessoaId,
        @Size(max = 30) @Schema(example = "(11) 99999-0000") String telefone) { }
