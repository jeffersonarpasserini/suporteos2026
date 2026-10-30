package com.curso.suporteos.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "conta_acesso")
public class ContaAcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "pessoa_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_conta_acesso_pessoa"))
    private Pessoa pessoa;

    @Column(nullable = false, length = 80)
    private String login;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusContaAcesso status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "conta_acesso_perfil",
            joinColumns = @JoinColumn(
                    name = "conta_acesso_id",
                    foreignKey = @ForeignKey(name = "fk_conta_acesso_perfil_conta")))
    @Enumerated(EnumType.STRING)
    @Column(name = "perfil", nullable = false, length = 30)
    private Set<PerfilAcesso> perfis = new LinkedHashSet<>();

    protected ContaAcesso() {
    }

    public ContaAcesso(Pessoa pessoa, String login, String senhaHash) {
        this.pessoa = Objects.requireNonNull(pessoa, "Pessoa é obrigatória");
        this.login = normalizarLogin(login);
        this.senhaHash = validarTexto(senhaHash, "Hash da senha é obrigatório");
        this.status = StatusContaAcesso.ATIVA;
        this.perfis.add(PerfilAcesso.OPERADOR);
    }

    public void alterarSenha(String novoHash) {
        this.senhaHash = validarTexto(novoHash, "Hash da senha é obrigatório");
    }

    public void bloquear() {
        this.status = StatusContaAcesso.BLOQUEADA;
    }

    public void ativar() {
        this.status = StatusContaAcesso.ATIVA;
    }

    public void substituirPerfis(Set<PerfilAcesso> novosPerfis) {
        if (novosPerfis == null || novosPerfis.isEmpty()) {
            throw new IllegalArgumentException("A conta deve possuir ao menos um perfil");
        }
        this.perfis.clear();
        this.perfis.addAll(novosPerfis);
    }

    public void concederPerfil(PerfilAcesso perfil) {
        this.perfis.add(Objects.requireNonNull(perfil, "Perfil é obrigatório"));
    }

    public boolean possuiPerfil(PerfilAcesso perfil) {
        return perfis.contains(perfil);
    }

    public boolean podeAutenticar() {
        return status == StatusContaAcesso.ATIVA
                && pessoa.getStatus() == Status.ATIVO;
    }

    public Long getId() {
        return id;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public String getLogin() {
        return login;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public StatusContaAcesso getStatus() {
        return status;
    }

    public Set<PerfilAcesso> getPerfis() {
        return Set.copyOf(perfis);
    }

    private static String normalizarLogin(String login) {
        return validarTexto(login, "Login é obrigatório")
                .toLowerCase(Locale.ROOT);
    }

    private static String validarTexto(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
        return valor.trim();
    }
}
