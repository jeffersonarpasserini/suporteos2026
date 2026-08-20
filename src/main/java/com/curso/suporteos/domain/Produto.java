package com.curso.suporteos.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

public class Produto {

    private final String codigoBarras;
    private String descricao;
    private BigDecimal saldoEstoque;
    private BigDecimal valorUnitario;
    private final LocalDate dataCadastro;
    private Status status;
    private GrupoProduto grupo;

    public Produto(
            String codigoBarras,
            String descricao,
            BigDecimal saldoEstoque,
            BigDecimal valorUnitario,
            LocalDate dataCadastro) {
        this.codigoBarras = validarTextoObrigatorio(
                codigoBarras,
                "Código de barras é obrigatório");
        this.descricao = validarTextoObrigatorio(
                descricao,
                "Descrição é obrigatória");
        this.saldoEstoque = validarNaoNegativo(
                saldoEstoque,
                "Saldo de estoque não pode ser negativo");
        this.valorUnitario = validarNaoNegativo(
                valorUnitario,
                "Valor unitário não pode ser negativo");
        this.dataCadastro = Objects.requireNonNull(
                dataCadastro,
                "Data de cadastro é obrigatória");
        this.status = Status.ATIVO;
    }

    public BigDecimal calcularValorEstoque() {
        return saldoEstoque
                .multiply(valorUnitario)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void receberEstoque(BigDecimal quantidade) {
        validarPositivo(quantidade, "Quantidade recebida deve ser maior que zero");
        this.saldoEstoque = saldoEstoque.add(quantidade);
    }

    public void retirarEstoque(BigDecimal quantidade) {
        validarPositivo(quantidade, "Quantidade retirada deve ser maior que zero");

        if (saldoEstoque.compareTo(quantidade) < 0) {
            throw new IllegalArgumentException("Saldo de estoque insuficiente");
        }

        this.saldoEstoque = saldoEstoque.subtract(quantidade);
    }

    public void alterarDescricao(String novaDescricao) {
        this.descricao = validarTextoObrigatorio(
                novaDescricao,
                "Descrição é obrigatória");
    }

    public void alterarValorUnitario(BigDecimal novoValor) {
        this.valorUnitario = validarNaoNegativo(
                novoValor,
                "Valor unitário não pode ser negativo");
    }

    public void ativar() {
        this.status = Status.ATIVO;
    }

    public void inativar() {
        this.status = Status.INATIVO;
    }

    void associarAo(GrupoProduto grupo) {
        Objects.requireNonNull(grupo, "Grupo de produto é obrigatório");

        if (this.grupo != null && this.grupo != grupo) {
            throw new IllegalStateException("Produto já pertence a outro grupo");
        }

        this.grupo = grupo;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getSaldoEstoque() {
        return saldoEstoque;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public Status getStatus() {
        return status;
    }

    public GrupoProduto getGrupo() {
        return grupo;
    }

    private static String validarTextoObrigatorio(String texto, String mensagem) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
        return texto.trim();
    }

    private static BigDecimal validarNaoNegativo(BigDecimal valor, String mensagem) {
        Objects.requireNonNull(valor, mensagem);
        if (valor.signum() < 0) {
            throw new IllegalArgumentException(mensagem);
        }
        return valor;
    }

    private static void validarPositivo(BigDecimal valor, String mensagem) {
        Objects.requireNonNull(valor, mensagem);
        if (valor.signum() <= 0) {
            throw new IllegalArgumentException(mensagem);
        }
    }
}
