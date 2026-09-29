package com.curso.suporteos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Campos substituíveis de um grupo de produtos")
public record GrupoProdutoAtualizacaoRequest(
        @Schema(description = "Novo nome único do grupo", example = "Material de escritório")
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 120, message = "Nome deve possuir no máximo 120 caracteres")
        String nome) {
}
