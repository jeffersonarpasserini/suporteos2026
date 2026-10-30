package com.curso.suporteos.repository;

import com.curso.suporteos.domain.ContaAcesso;
import com.curso.suporteos.domain.PerfilAcesso;
import com.curso.suporteos.domain.StatusContaAcesso;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ContaAcessoRepository extends JpaRepository<ContaAcesso, Long> {

    boolean existsByLoginIgnoreCase(String login);

    boolean existsByPessoaId(Long pessoaId);

    @EntityGraph(attributePaths = {"pessoa", "perfis"})
    Optional<ContaAcesso> findByLoginIgnoreCase(String login);

    @EntityGraph(attributePaths = {"pessoa", "perfis"})
    @Query("select c from ContaAcesso c where c.id = :id")
    Optional<ContaAcesso> findDetalhadaById(@Param("id") Long id);

    @Query("""
            select count(distinct c.id)
            from ContaAcesso c join c.perfis perfil
            where perfil = :perfil and c.status = :status
            """)
    long countByPerfilAndStatus(
            @Param("perfil") PerfilAcesso perfil,
            @Param("status") StatusContaAcesso status);
}
