package com.curso.suporteos.application;

import com.curso.suporteos.domain.Colaborador;
import com.curso.suporteos.domain.FuncaoColaborador;
import com.curso.suporteos.domain.Pessoa;
import com.curso.suporteos.domain.Status;
import com.curso.suporteos.repository.ColaboradorRepository;
import com.curso.suporteos.repository.PessoaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;

@Service
public class ColaboradorService {
    private final ColaboradorRepository repository;
    private final PessoaRepository pessoaRepository;

    public ColaboradorService(ColaboradorRepository repository, PessoaRepository pessoaRepository) {
        this.repository = repository;
        this.pessoaRepository = pessoaRepository;
    }

    @Transactional
    public Colaborador cadastrar(Long pessoaId, String matricula, FuncaoColaborador funcao, LocalDate dataAdmissao) {
        Pessoa pessoa = pessoaRepository.findById(pessoaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa não encontrada"));
        if (pessoa.getStatus() != Status.ATIVO) throw new IllegalArgumentException("Pessoa deve estar ativa");
        if (repository.existsByPessoaId(pessoaId)) throw new RecursoDuplicadoException("Pessoa já possui cadastro de colaborador");
        if (repository.existsByMatricula(matricula)) throw new RecursoDuplicadoException("Matrícula já cadastrada");
        return repository.save(new Colaborador(pessoa, matricula, funcao, dataAdmissao));
    }

    @Transactional(readOnly = true)
    public Colaborador buscarPorId(Long id) { return buscarEntidade(id); }

    @Transactional
    public Colaborador alterar(Long id, String matricula, FuncaoColaborador funcao) {
        Colaborador colaborador = buscarEntidade(id);
        if (repository.existsByMatriculaAndIdNot(matricula, id)) throw new RecursoDuplicadoException("Matrícula já cadastrada");
        colaborador.alterarDados(matricula, funcao);
        return colaborador;
    }

    @Transactional(readOnly = true)
    public Page<Colaborador> pesquisar(String nome, FuncaoColaborador funcao, Status status, Pageable pageable) {
        validarOrdenacao(pageable, Set.of("id", "matricula", "funcao", "dataAdmissao", "pessoa.nome", "pessoa.status"));
        Specification<Colaborador> filtros = (root, query, cb) -> cb.conjunction();
        if (nome != null && !nome.isBlank()) {
            String trecho = "%" + nome.trim().toLowerCase(Locale.ROOT) + "%";
            filtros = filtros.and((root, query, cb) -> cb.like(cb.lower(root.get("pessoa").get("nome")), trecho));
        }
        if (funcao != null) filtros = filtros.and((root, query, cb) -> cb.equal(root.get("funcao"), funcao));
        if (status != null) filtros = filtros.and((root, query, cb) -> cb.equal(root.get("pessoa").get("status"), status));
        return repository.findAll(filtros, pageable);
    }

    private Colaborador buscarEntidade(Long id) {
        return repository.findOneById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador não encontrado"));
    }

    private void validarOrdenacao(Pageable pageable, Set<String> permitidos) {
        pageable.getSort().forEach(o -> {
            if (!permitidos.contains(o.getProperty())) throw new IllegalArgumentException("Campo de ordenação inválido: " + o.getProperty());
        });
    }
}
