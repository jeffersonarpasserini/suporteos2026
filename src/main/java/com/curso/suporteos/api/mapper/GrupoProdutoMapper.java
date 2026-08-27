package com.curso.suporteos.api.mapper;

import com.curso.suporteos.api.dto.GrupoProdutoResponse;
import com.curso.suporteos.domain.GrupoProduto;
import org.springframework.stereotype.Component;

@Component
public class GrupoProdutoMapper {

    public GrupoProdutoResponse toResponse(GrupoProduto grupo) {
        return new GrupoProdutoResponse(
                grupo.getId(),
                grupo.getNome(),
                grupo.getStatus());
    }
}
