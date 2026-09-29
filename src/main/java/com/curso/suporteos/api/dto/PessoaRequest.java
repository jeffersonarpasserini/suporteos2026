package com.curso.suporteos.api.dto;

import com.curso.suporteos.api.validation.CpfValido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Identidade civil compartilhada por cliente e colaborador")
public record PessoaRequest(
        @NotBlank @Size(max = 150) @Schema(example = "Ana Souza") String nome,
        @NotBlank @Email @Size(max = 180) @Schema(example = "ana.souza@example.com") String email,
        @NotBlank @CpfValido @Schema(example = "52998224725") String cpf) { }
