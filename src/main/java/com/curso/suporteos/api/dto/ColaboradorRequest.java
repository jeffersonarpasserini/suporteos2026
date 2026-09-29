package com.curso.suporteos.api.dto;

import com.curso.suporteos.domain.FuncaoColaborador;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ColaboradorRequest(
        @NotNull @Schema(example = "2") Long pessoaId,
        @NotBlank @Size(max = 30) @Schema(example = "VEN-001") String matricula,
        @NotNull @Schema(example = "VENDEDOR") FuncaoColaborador funcao,
        @NotNull @Schema(example = "2026-09-29") LocalDate dataAdmissao) { }
