package com.curso.suporteos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastrar um fornecedor")
public record FornecedorRequest(
        @Schema(description = "Razão social do fornecedor", example = "Papelaria Central Ltda.")
        @NotBlank(message = "Razão social é obrigatória")
        @Size(max = 150, message = "Razão social deve possuir no máximo 150 caracteres")
        String razaoSocial,

        @Schema(description = "CNPJ contendo exatamente 14 dígitos", example = "12345678000195")
        @NotBlank(message = "CNPJ é obrigatório")
        @Pattern(regexp = "\\d{14}", message = "CNPJ deve possuir 14 dígitos")
        String cnpj) {
}
