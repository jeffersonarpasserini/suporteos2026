package com.curso.suporteos.repository;

import com.curso.suporteos.domain.Produto;
import com.curso.suporteos.domain.Status;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    Optional<Produto> findByCodigoBarras(String codigoBarras);

    boolean existsByCodigoBarras(String codigoBarras);

    List<Produto> findByGrupoId(Long grupoId);

    List<Produto> findByStatus(Status status);

    @EntityGraph(attributePaths = {"grupo", "fornecedor"})
    @Query("select p from Produto p order by p.id")
    List<Produto> buscarTodosComRelacionamentos();

    @EntityGraph(attributePaths = {"grupo", "fornecedor"})
    @Query("select p from Produto p where p.id = :id")
    Optional<Produto> buscarPorIdComRelacionamentos(@Param("id") Long id);
}
