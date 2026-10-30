package com.curso.suporteos.api.mapper;

import com.curso.suporteos.api.dto.ContaAcessoResponse;
import com.curso.suporteos.domain.ContaAcesso;
import org.springframework.stereotype.Component;

@Component
public class ContaAcessoMapper {

    public ContaAcessoResponse toResponse(ContaAcesso conta) {
        return new ContaAcessoResponse(
                conta.getId(),
                conta.getPessoa().getId(),
                conta.getPessoa().getNome(),
                conta.getLogin(),
                conta.getStatus(),
                conta.getPerfis());
    }
}
