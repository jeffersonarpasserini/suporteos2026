package com.curso.suporteos.api.dto;

import com.curso.suporteos.domain.Status;

public record GrupoProdutoResponse(
        Long id,
        String nome,
        Status status) {
}
