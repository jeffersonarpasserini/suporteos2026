package com.curso.suporteos.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GrupoProdutoTest {

    @Test
    void deveAdicionarProdutoEManejarOsDoisLadosDaAssociacao() {
        GrupoProduto grupo = new GrupoProduto("Papelaria");
        Produto produto = novoProduto("7890000000001");

        grupo.adicionarProduto(produto);

        assertEquals(1, grupo.getProdutos().size());
        assertSame(produto, grupo.getProdutos().getFirst());
        assertSame(grupo, produto.getGrupo());
    }

    @Test
    void naoDeveAdicionarProdutoNulo() {
        GrupoProduto grupo = new GrupoProduto("Papelaria");

        assertThrows(NullPointerException.class, () -> grupo.adicionarProduto(null));
    }

    @Test
    void naoDeveAdicionarDoisProdutosComOMesmoCodigo() {
        GrupoProduto grupo = new GrupoProduto("Papelaria");
        grupo.adicionarProduto(novoProduto("7890000000001"));

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> grupo.adicionarProduto(novoProduto("7890000000001")));

        assertEquals("Código de barras já utilizado no grupo", excecao.getMessage());
    }

    @Test
    void naoDevePermitirQueProdutoPertençaADoisGrupos() {
        GrupoProduto papelaria = new GrupoProduto("Papelaria");
        GrupoProduto materialEscolar = new GrupoProduto("Material escolar");
        Produto produto = novoProduto("7890000000001");
        papelaria.adicionarProduto(produto);

        IllegalStateException excecao = assertThrows(
                IllegalStateException.class,
                () -> materialEscolar.adicionarProduto(produto));

        assertEquals("Produto já pertence a outro grupo", excecao.getMessage());
    }

    @Test
    void naoDeveExporUmaListaInternaModificavel() {
        GrupoProduto grupo = new GrupoProduto("Papelaria");
        Produto produto = novoProduto("7890000000001");
        grupo.adicionarProduto(produto);

        assertThrows(
                UnsupportedOperationException.class,
                () -> grupo.getProdutos().add(novoProduto("7890000000002")));
    }

    private Produto novoProduto(String codigoBarras) {
        return new Produto(
                codigoBarras,
                "Caderno",
                new BigDecimal("3.000"),
                new BigDecimal("12.90"),
                LocalDate.of(2026, 8, 20));
    }
}
