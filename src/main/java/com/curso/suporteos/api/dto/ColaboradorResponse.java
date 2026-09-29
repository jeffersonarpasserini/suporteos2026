package com.curso.suporteos.api.dto;

import com.curso.suporteos.domain.FuncaoColaborador;
import com.curso.suporteos.domain.Status;

import java.time.LocalDate;

public record ColaboradorResponse(Long id, Long pessoaId, String nome, String email,
                                  String cpf, Status status, String matricula,
                                  FuncaoColaborador funcao, LocalDate dataAdmissao) { }
