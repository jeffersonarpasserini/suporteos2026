package com.curso.suporteos.api.dto;

import com.curso.suporteos.domain.StatusVenda;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VendaResponse(Long id, Long clienteId, String clienteNome,
                            Long vendedorId, String vendedorNome,
                            LocalDateTime dataVenda, StatusVenda status,
                            List<ItemVendaResponse> itens, BigDecimal total) { }
