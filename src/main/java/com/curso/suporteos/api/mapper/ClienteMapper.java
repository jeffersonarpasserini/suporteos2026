package com.curso.suporteos.api.mapper;

import com.curso.suporteos.api.dto.ClienteResponse;
import com.curso.suporteos.domain.Cliente;
import com.curso.suporteos.domain.Pessoa;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {
    public ClienteResponse toResponse(Cliente cliente) {
        Pessoa pessoa = cliente.getPessoa();
        return new ClienteResponse(cliente.getId(), pessoa.getId(), pessoa.getNome(), pessoa.getEmail(),
                pessoa.getCpf(), cliente.getTelefone(), pessoa.getStatus());
    }
}
