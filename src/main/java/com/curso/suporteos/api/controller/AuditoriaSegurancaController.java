package com.curso.suporteos.api.controller;

import com.curso.suporteos.api.dto.EventoSegurancaResponse;
import com.curso.suporteos.application.AuditoriaSegurancaService;
import com.curso.suporteos.domain.EventoSeguranca;
import com.curso.suporteos.domain.TipoEventoSeguranca;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auditoria de segurança", description = "Consulta administrativa de eventos de segurança")
@RestController
@RequestMapping("/api/auditoria-seguranca")
public class AuditoriaSegurancaController {

    private final AuditoriaSegurancaService service;

    public AuditoriaSegurancaController(AuditoriaSegurancaService service) {
        this.service = service;
    }

    @Operation(summary = "Consultar eventos de segurança", description = "Exige ADMINISTRADOR")
    @GetMapping
    public Page<EventoSegurancaResponse> consultar(
            @RequestParam(required = false) TipoEventoSeguranca tipo,
            Pageable pageable) {
        return service.consultar(tipo, pageable).map(this::toResponse);
    }

    private EventoSegurancaResponse toResponse(EventoSeguranca evento) {
        return new EventoSegurancaResponse(
                evento.getId(),
                evento.getTipo(),
                evento.getOcorridoEm(),
                evento.getAtorContaId(),
                evento.getAlvoContaId(),
                evento.getEnderecoOrigem());
    }
}
