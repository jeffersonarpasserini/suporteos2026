package com.curso.suporteos.repository;

import com.curso.suporteos.domain.Venda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface VendaRepository extends JpaRepository<Venda, Long>, JpaSpecificationExecutor<Venda> {
    @EntityGraph(attributePaths = {"cliente.pessoa", "vendedor.pessoa", "itens.produto"})
    Optional<Venda> findOneById(Long id);

    @Override
    @EntityGraph(attributePaths = {"cliente.pessoa", "vendedor.pessoa"})
    Page<Venda> findAll(Specification<Venda> specification, Pageable pageable);
}
