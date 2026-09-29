package com.curso.suporteos.application;

import com.curso.suporteos.domain.GrupoProduto;
import com.curso.suporteos.domain.Status;
import com.curso.suporteos.repository.GrupoProdutoRepository;
import com.curso.suporteos.repository.ProdutoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class GrupoProdutoService {

    private final GrupoProdutoRepository repository;
    private final ProdutoRepository produtoRepository;

    public GrupoProdutoService(
            GrupoProdutoRepository repository,
            ProdutoRepository produtoRepository) {
        this.repository = repository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public GrupoProduto cadastrar(String nome) {
        String nomeNormalizado = normalizarNome(nome);
        if (repository.existsByNomeIgnoreCase(nomeNormalizado)) {
            throw new RecursoDuplicadoException("Nome do grupo já cadastrado");
        }
        return repository.save(new GrupoProduto(nomeNormalizado));
    }

    @Transactional
    public GrupoProduto alterar(Long id, String nome) {
        String nomeNormalizado = normalizarNome(nome);
        if (repository.existsByNomeIgnoreCaseAndIdNot(nomeNormalizado, id)) {
            throw new RecursoDuplicadoException("Nome do grupo já cadastrado");
        }

        GrupoProduto grupo = buscarEntidade(id);
        grupo.alterarNome(nomeNormalizado);
        return grupo;
    }

    @Transactional
    public GrupoProduto alterarStatus(Long id, Status status) {
        if (status == null) {
            throw new IllegalArgumentException("Status é obrigatório");
        }
        GrupoProduto grupo = buscarEntidade(id);
        if (status == Status.ATIVO) {
            grupo.ativar();
        } else {
            grupo.inativar();
        }
        return grupo;
    }

    @Transactional
    public void excluir(Long id) {
        GrupoProduto grupo = buscarEntidade(id);
        if (produtoRepository.existsByGrupoId(id)) {
            throw new RecursoEmUsoException(
                    "Grupo de produto não pode ser excluído porque possui produtos");
        }
        repository.delete(grupo);
        repository.flush();
    }

    @Transactional(readOnly = true)
    public GrupoProduto buscarPorId(Long id) {
        return buscarEntidade(id);
    }

    @Transactional(readOnly = true)
    public List<GrupoProduto> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<GrupoProduto> pesquisar(
            String nome,
            Status status,
            Pageable pageable) {
        validarOrdenacao(pageable, Set.of("id", "nome", "status"));
        Specification<GrupoProduto> filtros = (root, query, cb) -> cb.conjunction();

        if (nome != null && !nome.isBlank()) {
            String trecho = "%" + nome.trim().toLowerCase(Locale.ROOT) + "%";
            filtros = filtros.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("nome")), trecho));
        }
        if (status != null) {
            filtros = filtros.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        return repository.findAll(filtros, pageable);
    }

    private GrupoProduto buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Grupo de produto não encontrado"));
    }

    private String normalizarNome(String nome) {
        return nome == null ? null : nome.trim();
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
