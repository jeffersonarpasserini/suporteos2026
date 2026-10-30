package com.curso.suporteos.application;

import com.curso.suporteos.domain.EventoSeguranca;
import com.curso.suporteos.domain.TipoEventoSeguranca;
import com.curso.suporteos.repository.EventoSegurancaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class AuditoriaSegurancaService {

    private final EventoSegurancaRepository repository;
    private final Clock clock;

    public AuditoriaSegurancaService(
            EventoSegurancaRepository repository,
            Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public void registrar(
            TipoEventoSeguranca tipo,
            Long atorContaId,
            Long alvoContaId,
            String identificadorHash,
            String enderecoOrigem) {
        repository.save(new EventoSeguranca(
                tipo,
                clock.instant(),
                atorContaId,
                alvoContaId,
                identificadorHash,
                enderecoOrigem));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Page<EventoSeguranca> consultar(
            TipoEventoSeguranca tipo,
            Pageable pageable) {
        if (tipo == null) {
            return repository.findAllByOrderByOcorridoEmDescIdDesc(pageable);
        }
        return repository.findByTipoOrderByOcorridoEmDescIdDesc(tipo, pageable);
    }
}
