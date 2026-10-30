package com.curso.suporteos.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContaAcessoTest {

    @Test
    void deveCriarContaAtivaComLoginNormalizadoEPerfilOperador() {
        Pessoa pessoa = novaPessoa();

        ContaAcesso conta = new ContaAcesso(
                pessoa,
                "  ALUNA.2026  ",
                "  {bcrypt}hash-seguro  ");

        assertAll(
                () -> assertEquals("aluna.2026", conta.getLogin()),
                () -> assertEquals("{bcrypt}hash-seguro", conta.getSenhaHash()),
                () -> assertEquals(StatusContaAcesso.ATIVA, conta.getStatus()),
                () -> assertEquals(Set.of(PerfilAcesso.OPERADOR), conta.getPerfis()),
                () -> assertTrue(conta.podeAutenticar()));
    }

    @ParameterizedTest(name = "login inválido: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void naoDeveAceitarLoginNuloOuEmBranco(String loginInvalido) {
        Pessoa pessoa = novaPessoa();

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> new ContaAcesso(pessoa, loginInvalido, "{bcrypt}hash"));

        assertEquals("Login é obrigatório", erro.getMessage());
    }

    @Test
    void deveAlterarSenhaSomenteQuandoOHashForValido() {
        ContaAcesso conta = novaConta();

        conta.alterarSenha("  {bcrypt}novo-hash  ");

        assertEquals("{bcrypt}novo-hash", conta.getSenhaHash());
        assertThrows(
                IllegalArgumentException.class,
                () -> conta.alterarSenha(" "));
    }

    @Test
    void deveAdministrarPerfisSemPermitirUmaContaSemPerfil() {
        ContaAcesso conta = novaConta();

        conta.concederPerfil(PerfilAcesso.ADMINISTRADOR);
        conta.substituirPerfis(Set.of(PerfilAcesso.ADMINISTRADOR));

        assertTrue(conta.possuiPerfil(PerfilAcesso.ADMINISTRADOR));
        assertFalse(conta.possuiPerfil(PerfilAcesso.OPERADOR));
        assertThrows(
                IllegalArgumentException.class,
                () -> conta.substituirPerfis(Set.of()));
    }

    @Test
    void deveConsiderarOStatusDaContaEDaPessoaParaAutenticar() {
        Pessoa pessoa = novaPessoa();
        ContaAcesso conta = new ContaAcesso(pessoa, "aluna.2026", "{bcrypt}hash");

        conta.bloquear();
        assertFalse(conta.podeAutenticar());

        conta.ativar();
        pessoa.inativar();
        assertFalse(conta.podeAutenticar());

        pessoa.ativar();
        assertTrue(conta.podeAutenticar());
    }

    private ContaAcesso novaConta() {
        return new ContaAcesso(novaPessoa(), "aluna.2026", "{bcrypt}hash");
    }

    private Pessoa novaPessoa() {
        return new Pessoa(
                "Aluna de Teste",
                "aluna.teste@example.com",
                "52998224725",
                LocalDate.of(2026, 10, 29));
    }
}
