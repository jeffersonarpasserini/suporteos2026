package com.curso.suporteos.api.dto;

import com.curso.suporteos.domain.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Novo estado de ativação de um recurso")
public record StatusRequest(
        @Schema(description = "Status desejado", example = "INATIVO")
        @NotNull(message = "Status é obrigatório")
        Status status) {
}
