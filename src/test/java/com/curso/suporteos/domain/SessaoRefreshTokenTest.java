package com.curso.suporteos.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessaoRefreshTokenTest {

    private static final Instant CRIACAO = Instant.parse("2026-10-29T12:00:00Z");

    @Test
    void deveExpirarAoAtingirOLimiteDeInatividade() {
        SessaoRefreshToken sessao = novaSessao(CRIACAO.plus(Duration.ofHours(8)));

        assertFalse(sessao.expirouPorInatividade(
                CRIACAO.plus(Duration.ofMinutes(29)),
                Duration.ofMinutes(30)));
        assertTrue(sessao.expirouPorInatividade(
                CRIACAO.plus(Duration.ofMinutes(30)),
                Duration.ofMinutes(30)));
    }

    @Test
    void deveExpirarMesmoComAtividadeAoAtingirOLimiteAbsoluto() {
        Instant limiteAbsoluto = CRIACAO.plus(Duration.ofHours(8));
        SessaoRefreshToken sessao = novaSessao(limiteAbsoluto);

        assertFalse(sessao.expirouAbsolutamente(limiteAbsoluto.minusSeconds(1)));
        assertTrue(sessao.expirouAbsolutamente(limiteAbsoluto));
    }

    @Test
    void naoDevePermitirSegundoUsoDoMesmoRefreshToken() {
        SessaoRefreshToken sessao = novaSessao(CRIACAO.plus(Duration.ofHours(8)));

        sessao.marcarUtilizada(CRIACAO.plusSeconds(10));

        assertTrue(sessao.foiUtilizada());
        assertThrows(
                IllegalStateException.class,
                () -> sessao.marcarUtilizada(CRIACAO.plusSeconds(20)));
    }

    @Test
    void devePreservarOPrimeiroMotivoDeRevogacao() {
        SessaoRefreshToken sessao = novaSessao(CRIACAO.plus(Duration.ofHours(8)));

        sessao.revogar(CRIACAO.plusSeconds(10), MotivoRevogacaoRefreshToken.LOGOUT);
        sessao.revogar(
                CRIACAO.plusSeconds(20),
                MotivoRevogacaoRefreshToken.REUTILIZACAO);

        assertTrue(sessao.foiRevogada());
        assertEquals(MotivoRevogacaoRefreshToken.LOGOUT, sessao.getMotivoRevogacao());
        assertEquals(CRIACAO.plusSeconds(10), sessao.getRevogadaEm());
    }

    private SessaoRefreshToken novaSessao(Instant expiraEm) {
        Pessoa pessoa = new Pessoa(
                "Pessoa de Teste",
                "sessao@example.com",
                "52998224725",
                LocalDate.of(2026, 10, 29));
        ContaAcesso conta = new ContaAcesso(pessoa, "sessao.teste", "{bcrypt}hash");
        return new SessaoRefreshToken(
                conta,
                "00000000-0000-0000-0000-000000000001",
                "a".repeat(64),
                CRIACAO,
                CRIACAO,
                expiraEm);
    }
}
