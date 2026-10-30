package com.curso.suporteos.application;

import com.curso.suporteos.domain.ContaAcesso;
import com.curso.suporteos.domain.PerfilAcesso;
import com.curso.suporteos.domain.Pessoa;
import com.curso.suporteos.domain.Status;
import com.curso.suporteos.domain.StatusContaAcesso;
import com.curso.suporteos.repository.ContaAcessoRepository;
import com.curso.suporteos.repository.PessoaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@Service
public class ContaAcessoService {

    private static final int LIMITE_BCRYPT_BYTES = 72;

    private final ContaAcessoRepository repository;
    private final PessoaRepository pessoaRepository;
    private final PasswordEncoder passwordEncoder;

    public ContaAcessoService(
            ContaAcessoRepository repository,
            PessoaRepository pessoaRepository,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.pessoaRepository = pessoaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ContaAcesso cadastrar(Long pessoaId, String login, String senhaAberta) {
        validarSenha(senhaAberta);
        if (repository.existsByLoginIgnoreCase(login)) {
            throw new RecursoDuplicadoException("Login já cadastrado");
        }
        if (repository.existsByPessoaId(pessoaId)) {
            throw new RecursoDuplicadoException("Pessoa já possui conta de acesso");
        }

        Pessoa pessoa = pessoaRepository.findById(pessoaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa não encontrada"));
        if (pessoa.getStatus() != Status.ATIVO) {
            throw new IllegalArgumentException("Pessoa deve estar ativa");
        }

        String senhaHash = passwordEncoder.encode(senhaAberta);
        return repository.save(new ContaAcesso(pessoa, login, senhaHash));
    }

    @Transactional(readOnly = true)
    public ContaAcesso buscarPorLogin(String login) {
        return repository.findByLoginIgnoreCase(login)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta de acesso não encontrada"));
    }

    @Transactional
    public void alterarSenha(String login, String senhaAtual, String novaSenha) {
        validarSenha(novaSenha);
        ContaAcesso conta = buscarPorLogin(login);
        if (!passwordEncoder.matches(senhaAtual, conta.getSenhaHash())) {
            throw new IllegalArgumentException("Senha atual inválida");
        }
        if (passwordEncoder.matches(novaSenha, conta.getSenhaHash())) {
            throw new IllegalArgumentException("Nova senha deve ser diferente da senha atual");
        }
        conta.alterarSenha(passwordEncoder.encode(novaSenha));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ContaAcesso alterarPerfis(
            Long contaId,
            Set<PerfilAcesso> perfis,
            String loginAdministrador) {
        ContaAcesso conta = buscarDetalhada(contaId);
        boolean removeAdministrador = conta.possuiPerfil(PerfilAcesso.ADMINISTRADOR)
                && (perfis == null || !perfis.contains(PerfilAcesso.ADMINISTRADOR));

        if (conta.getLogin().equalsIgnoreCase(loginAdministrador) && removeAdministrador) {
            throw new IllegalArgumentException(
                    "O administrador não pode remover o próprio perfil administrativo");
        }
        if (removeAdministrador && quantidadeAdministradoresAtivos() <= 1) {
            throw new IllegalArgumentException(
                    "O sistema deve manter ao menos um administrador ativo");
        }

        conta.substituirPerfis(perfis);
        return conta;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ContaAcesso alterarStatus(
            Long contaId,
            StatusContaAcesso status,
            String loginAdministrador) {
        if (status == null) {
            throw new IllegalArgumentException("Status é obrigatório");
        }
        ContaAcesso conta = buscarDetalhada(contaId);
        if (conta.getLogin().equalsIgnoreCase(loginAdministrador)
                && status == StatusContaAcesso.BLOQUEADA) {
            throw new IllegalArgumentException(
                    "O administrador não pode bloquear a própria conta");
        }
        if (conta.possuiPerfil(PerfilAcesso.ADMINISTRADOR)
                && status == StatusContaAcesso.BLOQUEADA
                && quantidadeAdministradoresAtivos() <= 1) {
            throw new IllegalArgumentException(
                    "O sistema deve manter ao menos um administrador ativo");
        }

        if (status == StatusContaAcesso.ATIVA) {
            conta.ativar();
        } else {
            conta.bloquear();
        }
        return conta;
    }

    private ContaAcesso buscarDetalhada(Long id) {
        return repository.findDetalhadaById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Conta de acesso não encontrada"));
    }

    private long quantidadeAdministradoresAtivos() {
        return repository.countByPerfilAndStatus(
                PerfilAcesso.ADMINISTRADOR,
                StatusContaAcesso.ATIVA);
    }

    private void validarSenha(String senha) {
        if (senha == null || senha.length() < 12) {
            throw new IllegalArgumentException("Senha deve possuir ao menos 12 caracteres");
        }
        if (senha.getBytes(StandardCharsets.UTF_8).length > LIMITE_BCRYPT_BYTES) {
            throw new IllegalArgumentException("Senha deve possuir no máximo 72 bytes em UTF-8");
        }
    }
}
