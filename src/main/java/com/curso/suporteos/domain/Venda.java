package com.curso.suporteos.domain;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "venda")
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cliente_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_venda_cliente"))
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "vendedor_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_venda_colaborador"))
    private Colaborador vendedor;

    @Column(name = "data_venda", nullable = false)
    private LocalDateTime dataVenda;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusVenda status;

    @OneToMany(
            mappedBy = "venda",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ItemVenda> itens = new ArrayList<>();

    protected Venda() {
    }

    public Venda(Cliente cliente, Colaborador vendedor, LocalDateTime dataVenda) {
        this.cliente = Objects.requireNonNull(cliente, "Cliente é obrigatório");
        this.vendedor = Objects.requireNonNull(vendedor, "Vendedor é obrigatório");
        this.dataVenda = Objects.requireNonNull(dataVenda, "Data da venda é obrigatória");
        this.status = StatusVenda.ABERTA;
    }

    public ItemVenda adicionarItem(Produto produto, BigDecimal quantidade) {
        validarAberta();
        Objects.requireNonNull(produto, "Produto é obrigatório");
        boolean produtoRepetido = itens.stream()
                .anyMatch(item -> Objects.equals(
                        item.getProduto().getId(),
                        produto.getId()));
        if (produtoRepetido) {
            throw new IllegalArgumentException("Produto já incluído na venda");
        }

        ItemVenda item = new ItemVenda(
                this,
                produto,
                quantidade,
                produto.getValorUnitario());
        itens.add(item);
        return item;
    }

    public void removerItem(Long itemId) {
        validarAberta();
        boolean removido = itens.removeIf(item -> Objects.equals(item.getId(), itemId));
        if (!removido) {
            throw new IllegalArgumentException("Item não pertence à venda");
        }
    }

    public void finalizar() {
        validarAberta();
        if (itens.isEmpty()) {
            throw new IllegalArgumentException("Venda deve possuir ao menos um item");
        }
        this.status = StatusVenda.FINALIZADA;
    }

    public void cancelar() {
        validarAberta();
        this.status = StatusVenda.CANCELADA;
    }

    public BigDecimal calcularTotal() {
        return itens.stream()
                .map(ItemVenda::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Colaborador getVendedor() {
        return vendedor;
    }

    public LocalDateTime getDataVenda() {
        return dataVenda;
    }

    public StatusVenda getStatus() {
        return status;
    }

    public List<ItemVenda> getItens() {
        return List.copyOf(itens);
    }

    private void validarAberta() {
        if (status != StatusVenda.ABERTA) {
            throw new IllegalStateException("Somente vendas abertas podem ser alteradas");
        }
    }
}
