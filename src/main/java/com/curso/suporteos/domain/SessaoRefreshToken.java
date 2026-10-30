package com.curso.suporteos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "sessao_refresh_token")
public class SessaoRefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "conta_acesso_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_sessao_refresh_token_conta"))
    private ContaAcesso conta;

    @Column(name = "familia_id", nullable = false, length = 36)
    private String familiaId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    @Column(name = "ultima_atividade_em", nullable = false)
    private Instant ultimaAtividadeEm;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    @Column(name = "utilizada_em")
    private Instant utilizadaEm;

    @Column(name = "revogada_em")
    private Instant revogadaEm;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo_revogacao", length = 30)
    private MotivoRevogacaoRefreshToken motivoRevogacao;

    protected SessaoRefreshToken() {
    }

    public SessaoRefreshToken(
            ContaAcesso conta,
            String familiaId,
            String tokenHash,
            Instant criadaEm,
            Instant ultimaAtividadeEm,
            Instant expiraEm) {
        this.conta = Objects.requireNonNull(conta, "Conta é obrigatória");
        this.familiaId = validarTexto(familiaId, "Família é obrigatória");
        this.tokenHash = validarTexto(tokenHash, "Hash do token é obrigatório");
        this.criadaEm = Objects.requireNonNull(criadaEm, "Criação é obrigatória");
        this.ultimaAtividadeEm = Objects.requireNonNull(
                ultimaAtividadeEm,
                "Última atividade é obrigatória");
        this.expiraEm = Objects.requireNonNull(expiraEm, "Expiração é obrigatória");
        if (!expiraEm.isAfter(criadaEm)) {
            throw new IllegalArgumentException("A expiração deve ocorrer após a criação");
        }
    }

    public boolean foiUtilizada() {
        return utilizadaEm != null;
    }

    public boolean foiRevogada() {
        return revogadaEm != null;
    }

    public boolean expirouPorInatividade(Instant agora, Duration limiteInatividade) {
        return !agora.isBefore(ultimaAtividadeEm.plus(limiteInatividade));
    }

    public boolean expirouAbsolutamente(Instant agora) {
        return !agora.isBefore(expiraEm);
    }

    public void marcarUtilizada(Instant agora) {
        if (foiUtilizada()) {
            throw new IllegalStateException("Refresh token já foi utilizado");
        }
        this.utilizadaEm = Objects.requireNonNull(agora, "Instante de uso é obrigatório");
    }

    public void revogar(Instant agora, MotivoRevogacaoRefreshToken motivo) {
        if (!foiRevogada()) {
            this.revogadaEm = Objects.requireNonNull(agora, "Instante de revogação é obrigatório");
            this.motivoRevogacao = Objects.requireNonNull(motivo, "Motivo é obrigatório");
        }
    }

    public Long getId() {
        return id;
    }

    public ContaAcesso getConta() {
        return conta;
    }

    public String getFamiliaId() {
        return familiaId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }

    public Instant getUltimaAtividadeEm() {
        return ultimaAtividadeEm;
    }

    public Instant getExpiraEm() {
        return expiraEm;
    }

    public Instant getUtilizadaEm() {
        return utilizadaEm;
    }

    public Instant getRevogadaEm() {
        return revogadaEm;
    }

    public MotivoRevogacaoRefreshToken getMotivoRevogacao() {
        return motivoRevogacao;
    }

    private static String validarTexto(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
        return valor.trim();
    }
}
