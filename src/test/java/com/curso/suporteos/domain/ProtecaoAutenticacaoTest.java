package com.curso.suporteos.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProtecaoAutenticacaoTest {

    private static final Instant INICIO = Instant.parse("2026-10-29T12:00:00Z");
    private static final Duration JANELA = Duration.ofMinutes(15);
    private static final Duration BLOQUEIO = Duration.ofMinutes(15);

    @Test
    void deveBloquearAoAtingirOLimiteDeFalhas() {
        ProtecaoAutenticacao protecao = novaProtecao();

        for (int tentativa = 1; tentativa < 5; tentativa++) {
            assertFalse(protecao.registrarFalha(
                    INICIO.plusSeconds(tentativa),
                    5,
                    JANELA,
                    BLOQUEIO));
        }
        assertTrue(protecao.registrarFalha(
                INICIO.plusSeconds(5),
                5,
                JANELA,
                BLOQUEIO));

        assertEquals(5, protecao.getFalhasConsecutivas());
        assertTrue(protecao.estaBloqueada(INICIO.plus(Duration.ofMinutes(14))));
    }

    @Test
    void deveLiberarAoAtingirOFimDoBloqueio() {
        ProtecaoAutenticacao protecao = novaProtecao();
        for (int tentativa = 1; tentativa <= 5; tentativa++) {
            protecao.registrarFalha(INICIO, 5, JANELA, BLOQUEIO);
        }

        assertTrue(protecao.estaBloqueada(INICIO.plus(BLOQUEIO).minusMillis(1)));
        assertFalse(protecao.estaBloqueada(INICIO.plus(BLOQUEIO)));
    }

    @Test
    void sucessoDeveLimparFalhasEBloqueio() {
        ProtecaoAutenticacao protecao = novaProtecao();
        protecao.registrarFalha(INICIO, 1, JANELA, BLOQUEIO);

        protecao.registrarSucesso();

        assertEquals(0, protecao.getFalhasConsecutivas());
        assertFalse(protecao.estaBloqueada(INICIO.plusSeconds(1)));
    }

    @Test
    void novaJanelaDeveReiniciarOContador() {
        ProtecaoAutenticacao protecao = novaProtecao();
        protecao.registrarFalha(INICIO, 5, JANELA, BLOQUEIO);
        protecao.registrarFalha(INICIO.plus(JANELA), 5, JANELA, BLOQUEIO);

        assertEquals(1, protecao.getFalhasConsecutivas());
        assertEquals(INICIO.plus(JANELA), protecao.getJanelaIniciadaEm());
    }

    @Test
    void eventoNaoDeveAceitarQuebraDeLinhaNaOrigem() {
        assertThrows(IllegalArgumentException.class, () -> new EventoSeguranca(
                TipoEventoSeguranca.LOGIN_FALHA,
                INICIO,
                null,
                null,
                "a".repeat(64),
                "127.0.0.1\nEVENTO_FORJADO"));
    }

    @Test
    void eventoNaoDeveAceitarIdentificadorForaDoFormatoHash() {
        assertThrows(IllegalArgumentException.class, () -> new EventoSeguranca(
                TipoEventoSeguranca.LOGIN_FALHA,
                INICIO,
                null,
                null,
                "login-em-texto-aberto",
                "127.0.0.1"));
    }

    private ProtecaoAutenticacao novaProtecao() {
        return new ProtecaoAutenticacao("a".repeat(64));
    }
}
