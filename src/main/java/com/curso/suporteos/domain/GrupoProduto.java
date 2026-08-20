package com.curso.suporteos.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GrupoProduto {

    private final String nome;
    private Status status;
    private final List<Produto> produtos = new ArrayList<>();

    public GrupoProduto(String nome) {
        this.nome = validarTextoObrigatorio(nome, "Nome do grupo é obrigatório");
        this.status = Status.ATIVO;
    }

    public void adicionarProduto(Produto produto) {
        Objects.requireNonNull(produto, "Produto é obrigatório");

        boolean codigoJaUtilizado = produtos.stream()
                .anyMatch(item -> item != produto
                        && item.getCodigoBarras().equals(produto.getCodigoBarras()));

        if (codigoJaUtilizado) {
            throw new IllegalArgumentException("Código de barras já utilizado no grupo");
        }

        produto.associarAo(this);

        if (!produtos.contains(produto)) {
            produtos.add(produto);
        }
    }

    public void ativar() {
        this.status = Status.ATIVO;
    }

    public void inativar() {
        this.status = Status.INATIVO;
    }

    public String getNome() {
        return nome;
    }

    public Status getStatus() {
        return status;
    }

    public List<Produto> getProdutos() {
        return List.copyOf(produtos);
    }

    private static String validarTextoObrigatorio(String texto, String mensagem) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
        return texto.trim();
    }
}
