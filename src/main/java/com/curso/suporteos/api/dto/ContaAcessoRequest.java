package com.curso.suporteos.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ContaAcessoRequest(
        @NotNull(message = "Pessoa é obrigatória")
        Long pessoaId,

        @NotBlank(message = "Login é obrigatório")
        @Size(min = 3, max = 80, message = "Login deve possuir entre 3 e 80 caracteres")
        @Pattern(
                regexp = "^[A-Za-z0-9._-]+$",
                message = "Login aceita somente letras, números, ponto, hífen e sublinhado")
        String login,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 12, max = 72, message = "Senha deve possuir entre 12 e 72 caracteres")
        String senha) {
}
