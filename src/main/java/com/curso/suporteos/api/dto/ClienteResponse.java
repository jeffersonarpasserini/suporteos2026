package com.curso.suporteos.api.dto;

import com.curso.suporteos.domain.Status;

public record ClienteResponse(Long id, Long pessoaId, String nome, String email,
                              String cpf, String telefone, Status status) { }
