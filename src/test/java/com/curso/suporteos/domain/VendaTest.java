package com.curso.suporteos.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class VendaTest {
    @Test
    void deveCalcularTotalComPrecoCapturadoNoMomentoDaInclusao() {
        Venda venda = novaVenda();
        Produto produto = novoProduto();
        venda.adicionarItem(produto, new BigDecimal("2.000"));

        produto.alterarValorUnitario(new BigDecimal("15.00"));

        assertEquals(new BigDecimal("20.00"), venda.calcularTotal());
        venda.finalizar();
        assertEquals(StatusVenda.FINALIZADA, venda.getStatus());
    }

    @Test
    void naoDeveAlterarVendaFinalizada() {
        Venda venda = novaVenda();
        venda.adicionarItem(novoProduto(), BigDecimal.ONE);
        venda.finalizar();
        assertThrows(IllegalStateException.class, venda::cancelar);
    }

    private Venda novaVenda() {
        Pessoa pessoaCliente = new Pessoa("Cliente", "cliente@example.com", "52998224725", LocalDate.now());
        Pessoa pessoaVendedor = new Pessoa("Vendedor", "vendedor@example.com", "11144477735", LocalDate.now());
        return new Venda(new Cliente(pessoaCliente, null),
                new Colaborador(pessoaVendedor, "VEN-1", FuncaoColaborador.VENDEDOR, LocalDate.now()),
                LocalDateTime.of(2026, 9, 29, 10, 0));
    }

    private Produto novoProduto() {
        return new Produto("P-1", "Produto", new BigDecimal("10.000"), new BigDecimal("10.00"), LocalDate.now());
    }
}
