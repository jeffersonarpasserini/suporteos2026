package com.curso.suporteos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(name = "pessoa")
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(nullable = false, length = 11, unique = true)
    private String cpf;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDate dataCadastro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    protected Pessoa() {
    }

    public Pessoa(String nome, String email, String cpf, LocalDate dataCadastro) {
        this.nome = validarTextoObrigatorio(nome, "Nome é obrigatório");
        this.email = normalizarEmail(email);
        this.cpf = validarCpf(cpf);
        this.dataCadastro = Objects.requireNonNull(
                dataCadastro,
                "Data de cadastro é obrigatória");
        this.status = Status.ATIVO;
    }

    public void alterarDados(String nome, String email, String cpf) {
        this.nome = validarTextoObrigatorio(nome, "Nome é obrigatório");
        this.email = normalizarEmail(email);
        this.cpf = validarCpf(cpf);
    }

    public void ativar() {
        this.status = Status.ATIVO;
    }

    public void inativar() {
        this.status = Status.INATIVO;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getCpf() {
        return cpf;
    }

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public Status getStatus() {
        return status;
    }

    private static String normalizarEmail(String email) {
        String valor = validarTextoObrigatorio(email, "E-mail é obrigatório")
                .toLowerCase(Locale.ROOT);
        if (!valor.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("E-mail inválido");
        }
        return valor;
    }

    private static String validarCpf(String cpf) {
        String valor = validarTextoObrigatorio(cpf, "CPF é obrigatório");
        if (!DocumentoFiscal.cpfValido(valor)) {
            throw new IllegalArgumentException("CPF inválido");
        }
        return valor;
    }

    private static String validarTextoObrigatorio(String texto, String mensagem) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
        return texto.trim();
    }
}
