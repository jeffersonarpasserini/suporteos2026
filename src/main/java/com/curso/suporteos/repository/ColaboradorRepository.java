package com.curso.suporteos.repository;

import com.curso.suporteos.domain.Colaborador;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ColaboradorRepository extends JpaRepository<Colaborador, Long>, JpaSpecificationExecutor<Colaborador> {
    boolean existsByPessoaId(Long pessoaId);
    boolean existsByMatricula(String matricula);
    boolean existsByMatriculaAndIdNot(String matricula, Long id);

    @EntityGraph(attributePaths = "pessoa")
    Optional<Colaborador> findOneById(Long id);

    @Override
    @EntityGraph(attributePaths = "pessoa")
    Page<Colaborador> findAll(Specification<Colaborador> specification, Pageable pageable);
}
