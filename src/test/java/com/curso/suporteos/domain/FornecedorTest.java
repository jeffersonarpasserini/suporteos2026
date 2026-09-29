package com.curso.suporteos.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FornecedorTest {

    @Test
    void deveCriarFornecedorAtivo() {
        Fornecedor fornecedor = new Fornecedor(
                "Distribuidora Acadêmica",
                "12345678000195");

        assertEquals("Distribuidora Acadêmica", fornecedor.getRazaoSocial());
        assertEquals("12345678000195", fornecedor.getCnpj());
        assertEquals(Status.ATIVO, fornecedor.getStatus());
    }

    @Test
    void deveRejeitarCnpjComFormatoInvalido() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Fornecedor("Fornecedor", "12.345"));
    }

    @Test
    void deveRejeitarRazaoSocialVazia() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Fornecedor(" ", "12345678000195"));
    }

    @Test
    void deveInativarFornecedor() {
        Fornecedor fornecedor = new Fornecedor("Fornecedor", "12345678000195");

        fornecedor.inativar();

        assertEquals(Status.INATIVO, fornecedor.getStatus());
    }
}
