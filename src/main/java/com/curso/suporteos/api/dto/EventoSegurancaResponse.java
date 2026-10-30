package com.curso.suporteos.api.dto;

import com.curso.suporteos.domain.TipoEventoSeguranca;

import java.time.Instant;

public record EventoSegurancaResponse(
        Long id,
        TipoEventoSeguranca tipo,
        Instant ocorridoEm,
        Long atorContaId,
        Long alvoContaId,
        String enderecoOrigem) {
}
