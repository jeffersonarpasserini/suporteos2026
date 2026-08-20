package com.curso.suporteos.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProdutoTest {

    @Test
    void deveCriarProdutoAtivoComDadosValidos() {
        Produto produto = novoProduto("3.000", "12.90");

        assertEquals("7890000000001", produto.getCodigoBarras());
        assertEquals("Caderno", produto.getDescricao());
        assertEquals(Status.ATIVO, produto.getStatus());
        assertEquals(LocalDate.of(2026, 8, 20), produto.getDataCadastro());
    }

    @Test
    void deveCalcularValorDoEstoque() {
        Produto produto = novoProduto("3.000", "12.90");

        BigDecimal valorEstoque = produto.calcularValorEstoque();

        assertEquals(0, new BigDecimal("38.70").compareTo(valorEstoque));
    }

    @Test
    void deveReceberERetirarEstoque() {
        Produto produto = novoProduto("3.000", "12.90");

        produto.receberEstoque(new BigDecimal("2.500"));
        produto.retirarEstoque(new BigDecimal("1.000"));

        assertEquals(0, new BigDecimal("4.500").compareTo(produto.getSaldoEstoque()));
    }

    @Test
    void naoDeveRetirarQuantidadeMaiorQueOSaldo() {
        Produto produto = novoProduto("3.000", "12.90");

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> produto.retirarEstoque(new BigDecimal("3.001")));

        assertEquals("Saldo de estoque insuficiente", excecao.getMessage());
    }

    @Test
    void naoDeveCriarProdutoComCodigoEmBranco() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Produto(
                        "  ",
                        "Caderno",
                        BigDecimal.ZERO,
                        new BigDecimal("12.90"),
                        LocalDate.of(2026, 8, 20)));
    }

    @Test
    void naoDeveCriarProdutoComSaldoNegativo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> novoProduto("-0.001", "12.90"));
    }

    @Test
    void deveAlterarOStatusPorComportamentoExplicito() {
        Produto produto = novoProduto("3.000", "12.90");

        produto.inativar();
        assertEquals(Status.INATIVO, produto.getStatus());

        produto.ativar();
        assertEquals(Status.ATIVO, produto.getStatus());
    }

    private Produto novoProduto(String saldo, String valorUnitario) {
        return new Produto(
                "7890000000001",
                "Caderno",
                new BigDecimal(saldo),
                new BigDecimal(valorUnitario),
                LocalDate.of(2026, 8, 20));
    }
}
