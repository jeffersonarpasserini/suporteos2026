package com.curso.suporteos.repository;

import com.curso.suporteos.domain.GrupoProduto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface GrupoProdutoRepository extends
        JpaRepository<GrupoProduto, Long>,
        JpaSpecificationExecutor<GrupoProduto> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    Optional<GrupoProduto> findByNomeIgnoreCase(String nome);
}
