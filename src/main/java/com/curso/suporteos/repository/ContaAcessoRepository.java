package com.curso.suporteos.repository;

import com.curso.suporteos.domain.ContaAcesso;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContaAcessoRepository extends JpaRepository<ContaAcesso, Long> {

    boolean existsByLoginIgnoreCase(String login);

    boolean existsByPessoaId(Long pessoaId);

    @EntityGraph(attributePaths = {"pessoa", "perfis"})
    Optional<ContaAcesso> findByLoginIgnoreCase(String login);
}
