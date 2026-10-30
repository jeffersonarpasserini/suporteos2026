package com.curso.suporteos.api.dto;

import com.curso.suporteos.domain.StatusContaAcesso;
import jakarta.validation.constraints.NotNull;

public record StatusContaAcessoRequest(
        @NotNull(message = "Status é obrigatório")
        StatusContaAcesso status) {
}
