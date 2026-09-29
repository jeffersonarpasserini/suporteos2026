package com.curso.suporteos.api.dto;

import com.curso.suporteos.domain.StatusVenda;

import java.time.LocalDateTime;

public record VendaResumoResponse(Long id, Long clienteId, String clienteNome,
                                  Long vendedorId, String vendedorNome,
                                  LocalDateTime dataVenda, StatusVenda status) { }
