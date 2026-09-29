package com.curso.suporteos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record ClienteAtualizacaoRequest(
        @Size(max = 30) @Schema(example = "(11) 98888-0000") String telefone) { }
