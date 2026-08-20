package com.curso.suporteos.domain;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class PersistenciaJpaTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void devePersistirERelerGrupoEProduto() {
        GrupoProduto grupo = new GrupoProduto("Periféricos");
        Produto produto = new Produto(
                "7891000000019",
                "Mouse sem fio",
                new BigDecimal("10.000"),
                new BigDecimal("89.90"),
                LocalDate.of(2026, 3, 10));

        grupo.adicionarProduto(produto);

        entityManager.persist(grupo);
        entityManager.persist(produto);
        entityManager.flush();

        Long produtoId = produto.getId();
        entityManager.clear();

        Produto produtoRecuperado = entityManager.find(Produto.class, produtoId);

        assertNotNull(produtoRecuperado);
        assertEquals("Mouse sem fio", produtoRecuperado.getDescricao());
        assertEquals("Periféricos", produtoRecuperado.getGrupo().getNome());
        assertEquals(Status.ATIVO, produtoRecuperado.getStatus());
    }

    @Test
    void deveRegistrarTodosOsChangeSetsDoCurso() {
        Integer quantidade = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM databasechangelog",
                Integer.class);

        assertEquals(8, quantidade);
    }

    @Test
    @Transactional
    void bancoDeveImpedirCodigoDeBarrasDuplicado() {
        Long grupoId = inserirGrupoDiretamente("Grupo para unicidade");

        inserirProdutoDiretamente(grupoId, "CODIGO-REPETIDO", "Primeiro produto", "1.000", "10.00");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> inserirProdutoDiretamente(
                        grupoId,
                        "CODIGO-REPETIDO",
                        "Segundo produto",
                        "1.000",
                        "20.00"));
    }

    @Test
    @Transactional
    void bancoDeveImpedirSaldoNegativo() {
        Long grupoId = inserirGrupoDiretamente("Grupo para saldo");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> inserirProdutoDiretamente(
                        grupoId,
                        "CODIGO-SALDO-NEGATIVO",
                        "Produto inválido",
                        "-1.000",
                        "10.00"));
    }

    private Long inserirGrupoDiretamente(String nome) {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO grupo_produto (nome, status)
                VALUES (?, 'ATIVO')
                RETURNING id
                """,
                Long.class,
                nome);
    }

    private void inserirProdutoDiretamente(
            Long grupoId,
            String codigoBarras,
            String descricao,
            String saldo,
            String valor) {
        jdbcTemplate.update(
                """
                INSERT INTO produto (
                    codigo_barras,
                    descricao,
                    saldo_estoque,
                    valor_unitario,
                    data_cadastro,
                    status,
                    grupo_produto_id
                )
                VALUES (?, ?, CAST(? AS NUMERIC), CAST(? AS NUMERIC), DATE '2026-03-10', 'ATIVO', ?)
                """,
                codigoBarras,
                descricao,
                saldo,
                valor,
                grupoId);
    }
}
