package com.curso.suporteos.api.dto;

import com.curso.suporteos.domain.Status;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação de um fornecedor")
public record FornecedorResponse(
        @Schema(description = "Identificador do fornecedor", example = "1")
        Long id,
        @Schema(description = "Razão social", example = "Papelaria Central Ltda.")
        String razaoSocial,
        @Schema(description = "CNPJ com 14 dígitos", example = "12345678000195")
        String cnpj,
        @Schema(description = "Situação do fornecedor", example = "ATIVO")
        Status status) {
}
