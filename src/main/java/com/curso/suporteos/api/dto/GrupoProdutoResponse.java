package com.curso.suporteos.api.dto;

import com.curso.suporteos.domain.Status;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação de um grupo de produtos")
public record GrupoProdutoResponse(
        @Schema(description = "Identificador do grupo", example = "1")
        Long id,
        @Schema(description = "Nome do grupo", example = "Papelaria")
        String nome,
        @Schema(description = "Situação do grupo", example = "ATIVO")
        Status status) {
}
