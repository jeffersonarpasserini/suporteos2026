package com.curso.suporteos.application;

import com.curso.suporteos.domain.Cliente;
import com.curso.suporteos.domain.Pessoa;
import com.curso.suporteos.domain.Status;
import com.curso.suporteos.repository.ClienteRepository;
import com.curso.suporteos.repository.PessoaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
public class ClienteService {
    private final ClienteRepository repository;
    private final PessoaRepository pessoaRepository;

    public ClienteService(ClienteRepository repository, PessoaRepository pessoaRepository) {
        this.repository = repository;
        this.pessoaRepository = pessoaRepository;
    }

    @Transactional
    public Cliente cadastrar(Long pessoaId, String telefone) {
        Pessoa pessoa = pessoaRepository.findById(pessoaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa não encontrada"));
        if (pessoa.getStatus() != Status.ATIVO) throw new IllegalArgumentException("Pessoa deve estar ativa");
        if (repository.existsByPessoaId(pessoaId)) throw new RecursoDuplicadoException("Pessoa já possui cadastro de cliente");
        return repository.save(new Cliente(pessoa, telefone));
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) { return buscarEntidade(id); }

    @Transactional
    public Cliente alterar(Long id, String telefone) {
        Cliente cliente = buscarEntidade(id);
        cliente.alterarDados(telefone);
        return cliente;
    }

    @Transactional(readOnly = true)
    public Page<Cliente> pesquisar(String nome, String cpf, Status status, Pageable pageable) {
        validarOrdenacao(pageable, Set.of("id", "telefone", "pessoa.nome", "pessoa.cpf", "pessoa.status"));
        Specification<Cliente> filtros = (root, query, cb) -> cb.conjunction();
        if (nome != null && !nome.isBlank()) {
            String trecho = "%" + nome.trim().toLowerCase(Locale.ROOT) + "%";
            filtros = filtros.and((root, query, cb) -> cb.like(cb.lower(root.get("pessoa").get("nome")), trecho));
        }
        if (cpf != null && !cpf.isBlank()) filtros = filtros.and((root, query, cb) -> cb.equal(root.get("pessoa").get("cpf"), cpf.trim()));
        if (status != null) filtros = filtros.and((root, query, cb) -> cb.equal(root.get("pessoa").get("status"), status));
        return repository.findAll(filtros, pageable);
    }

    private Cliente buscarEntidade(Long id) {
        return repository.findOneById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
    }

    private void validarOrdenacao(Pageable pageable, Set<String> permitidos) {
        pageable.getSort().forEach(o -> {
            if (!permitidos.contains(o.getProperty())) throw new IllegalArgumentException("Campo de ordenação inválido: " + o.getProperty());
        });
    }
}
