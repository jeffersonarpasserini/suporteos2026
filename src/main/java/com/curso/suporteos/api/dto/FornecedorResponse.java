package com.curso.suporteos.api.dto;

import com.curso.suporteos.domain.Status;

public record FornecedorResponse(
        Long id,
        String razaoSocial,
        String cnpj,
        Status status) {
}
