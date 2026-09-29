package com.curso.suporteos.api.dto;

import java.math.BigDecimal;

public record ItemVendaResponse(Long id, Long produtoId, String produtoDescricao,
                                BigDecimal quantidade, BigDecimal valorUnitario,
                                BigDecimal subtotal) { }
