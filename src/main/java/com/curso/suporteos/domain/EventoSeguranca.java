package com.curso.suporteos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "evento_seguranca")
public class EventoSeguranca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoEventoSeguranca tipo;

    @Column(name = "ocorrido_em", nullable = false)
    private Instant ocorridoEm;

    @Column(name = "ator_conta_id")
    private Long atorContaId;

    @Column(name = "alvo_conta_id")
    private Long alvoContaId;

    @Column(name = "identificador_hash", length = 64)
    private String identificadorHash;

    @Column(name = "endereco_origem", length = 45)
    private String enderecoOrigem;

    protected EventoSeguranca() {
    }

    public EventoSeguranca(
            TipoEventoSeguranca tipo,
            Instant ocorridoEm,
            Long atorContaId,
            Long alvoContaId,
            String identificadorHash,
            String enderecoOrigem) {
        this.tipo = Objects.requireNonNull(tipo, "Tipo do evento é obrigatório");
        this.ocorridoEm = Objects.requireNonNull(
                ocorridoEm,
                "Instante do evento é obrigatório");
        this.atorContaId = atorContaId;
        this.alvoContaId = alvoContaId;
        this.identificadorHash = validarHashOpcional(identificadorHash);
        this.enderecoOrigem = validarOrigemOpcional(enderecoOrigem);
    }

    public Long getId() {
        return id;
    }

    public TipoEventoSeguranca getTipo() {
        return tipo;
    }

    public Instant getOcorridoEm() {
        return ocorridoEm;
    }

    public Long getAtorContaId() {
        return atorContaId;
    }

    public Long getAlvoContaId() {
        return alvoContaId;
    }

    public String getIdentificadorHash() {
        return identificadorHash;
    }

    public String getEnderecoOrigem() {
        return enderecoOrigem;
    }

    private String validarHashOpcional(String hash) {
        if (hash == null) {
            return null;
        }
        String valor = hash.trim();
        if (!valor.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("Hash do identificador deve usar SHA-256 hexadecimal");
        }
        return valor;
    }

    private String validarOrigemOpcional(String origem) {
        if (origem == null || origem.isBlank()) {
            return null;
        }
        String valor = origem.trim();
        if (valor.length() > 45 || valor.contains("\r") || valor.contains("\n")) {
            throw new IllegalArgumentException("Endereço de origem inválido");
        }
        return valor;
    }
}
