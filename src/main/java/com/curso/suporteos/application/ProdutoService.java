package com.curso.suporteos.application;

import com.curso.suporteos.domain.Fornecedor;
import com.curso.suporteos.domain.GrupoProduto;
import com.curso.suporteos.domain.Produto;
import com.curso.suporteos.domain.Status;
import com.curso.suporteos.repository.FornecedorRepository;
import com.curso.suporteos.repository.GrupoProdutoRepository;
import com.curso.suporteos.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final GrupoProdutoRepository grupoRepository;
    private final FornecedorRepository fornecedorRepository;

    public ProdutoService(
            ProdutoRepository produtoRepository,
            GrupoProdutoRepository grupoRepository,
            FornecedorRepository fornecedorRepository) {
        this.produtoRepository = produtoRepository;
        this.grupoRepository = grupoRepository;
        this.fornecedorRepository = fornecedorRepository;
    }

    @Transactional
    public Produto cadastrar(Produto produto, Long grupoId, Long fornecedorId) {
        if (produtoRepository.existsByCodigoBarras(produto.getCodigoBarras())) {
            throw new RecursoDuplicadoException("Código de barras já cadastrado");
        }

        GrupoProduto grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Grupo de produto não encontrado"));
        grupo.adicionarProduto(produto);

        if (fornecedorId != null) {
            Fornecedor fornecedor = fornecedorRepository.findById(fornecedorId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Fornecedor não encontrado"));
            produto.associarFornecedor(fornecedor);
        }

        return produtoRepository.save(produto);
    }

    @Transactional(readOnly = true)
    public Produto buscarPorId(Long id) {
        return buscarEntidade(id);
    }

    @Transactional(readOnly = true)
    public List<Produto> listar() {
        return produtoRepository.buscarTodosComRelacionamentos();
    }

    @Transactional
    public Produto receberEstoque(Long id, BigDecimal quantidade) {
        Produto produto = buscarEntidade(id);
        produto.receberEstoque(quantidade);
        return produto;
    }

    @Transactional
    public Produto retirarEstoque(Long id, BigDecimal quantidade) {
        Produto produto = buscarEntidade(id);
        produto.retirarEstoque(quantidade);
        return produto;
    }

    @Transactional
    public Produto alterar(
            Long id,
            String descricao,
            BigDecimal valorUnitario,
            BigDecimal estoqueMinimo,
            Long grupoId,
            Long fornecedorId) {
        Produto produto = buscarEntidade(id);
        GrupoProduto grupo = buscarGrupo(grupoId);
        Fornecedor fornecedor = fornecedorId == null
                ? null
                : buscarFornecedor(fornecedorId);

        produto.alterarDescricao(descricao);
        produto.alterarValorUnitario(valorUnitario);
        produto.alterarEstoqueMinimo(estoqueMinimo);
        produto.alterarGrupo(grupo);
        produto.alterarFornecedor(fornecedor);
        return produto;
    }

    @Transactional
    public Produto alterarStatus(Long id, Status status) {
        if (status == null) {
            throw new IllegalArgumentException("Status é obrigatório");
        }
        Produto produto = buscarEntidade(id);
        if (status == Status.ATIVO) {
            produto.ativar();
        } else {
            produto.inativar();
        }
        return produto;
    }

    @Transactional
    public void excluir(Long id) {
        Produto produto = buscarEntidade(id);
        produtoRepository.delete(produto);
        produtoRepository.flush();
    }

    @Transactional(readOnly = true)
    public Page<Produto> pesquisar(
            String descricao,
            Status status,
            Long grupoId,
            Long fornecedorId,
            Boolean abaixoEstoqueMinimo,
            Pageable pageable) {
        validarOrdenacao(
                pageable,
                Set.of("id", "codigoBarras", "descricao", "saldoEstoque",
                        "valorUnitario", "estoqueMinimo", "dataCadastro", "status"));
        Specification<Produto> filtros = (root, query, cb) -> cb.conjunction();

        if (descricao != null && !descricao.isBlank()) {
            String trecho = "%" + descricao.trim().toLowerCase(Locale.ROOT) + "%";
            filtros = filtros.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("descricao")), trecho));
        }
        if (status != null) {
            filtros = filtros.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }
        if (grupoId != null) {
            filtros = filtros.and((root, query, cb) ->
                    cb.equal(root.get("grupo").get("id"), grupoId));
        }
        if (fornecedorId != null) {
            filtros = filtros.and((root, query, cb) ->
                    cb.equal(root.get("fornecedor").get("id"), fornecedorId));
        }
        if (Boolean.TRUE.equals(abaixoEstoqueMinimo)) {
            filtros = filtros.and((root, query, cb) ->
                    cb.lessThan(root.get("saldoEstoque"), root.get("estoqueMinimo")));
        }

        return produtoRepository.findAll(filtros, pageable);
    }

    private Produto buscarEntidade(Long id) {
        return produtoRepository.buscarPorIdComRelacionamentos(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Produto não encontrado"));
    }

    private GrupoProduto buscarGrupo(Long id) {
        return grupoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Grupo de produto não encontrado"));
    }

    private Fornecedor buscarFornecedor(Long id) {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Fornecedor não encontrado"));
    }

    private void validarOrdenacao(Pageable pageable, Set<String> camposPermitidos) {
        pageable.getSort().forEach(ordem -> {
            if (!camposPermitidos.contains(ordem.getProperty())) {
                throw new IllegalArgumentException(
                        "Campo de ordenação inválido: " + ordem.getProperty());
            }
        });
    }
}
