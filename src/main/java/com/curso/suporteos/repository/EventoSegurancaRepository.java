package com.curso.suporteos.repository;

import com.curso.suporteos.domain.EventoSeguranca;
import com.curso.suporteos.domain.TipoEventoSeguranca;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoSegurancaRepository extends JpaRepository<EventoSeguranca, Long> {

    long countByTipo(TipoEventoSeguranca tipo);

    Page<EventoSeguranca> findAllByOrderByOcorridoEmDescIdDesc(Pageable pageable);

    Page<EventoSeguranca> findByTipoOrderByOcorridoEmDescIdDesc(
            TipoEventoSeguranca tipo,
            Pageable pageable);
}
