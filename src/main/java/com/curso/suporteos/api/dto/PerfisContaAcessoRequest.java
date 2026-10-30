package com.curso.suporteos.api.dto;

import com.curso.suporteos.domain.PerfilAcesso;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record PerfisContaAcessoRequest(
        @NotEmpty(message = "Informe ao menos um perfil")
        Set<PerfilAcesso> perfis) {
}
