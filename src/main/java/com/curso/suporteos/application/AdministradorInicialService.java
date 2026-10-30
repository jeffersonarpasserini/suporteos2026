package com.curso.suporteos.application;

import com.curso.suporteos.config.AdministradorInicialProperties;
import com.curso.suporteos.domain.ContaAcesso;
import com.curso.suporteos.domain.PerfilAcesso;
import com.curso.suporteos.domain.Pessoa;
import com.curso.suporteos.domain.StatusContaAcesso;
import com.curso.suporteos.repository.ContaAcessoRepository;
import com.curso.suporteos.repository.PessoaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class AdministradorInicialService {

    private static final int LIMITE_BCRYPT_BYTES = 72;

    private final ContaAcessoRepository contaRepository;
    private final PessoaRepository pessoaRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public AdministradorInicialService(
            ContaAcessoRepository contaRepository,
            PessoaRepository pessoaRepository,
            PasswordEncoder passwordEncoder,
            Clock clock) {
        this.contaRepository = contaRepository;
        this.pessoaRepository = pessoaRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Transactional
    public Optional<ContaAcesso> criarSeNecessario(
            AdministradorInicialProperties properties) {
        long administradores = contaRepository.countByPerfilAndStatus(
                PerfilAcesso.ADMINISTRADOR,
                StatusContaAcesso.ATIVA);
        if (administradores > 0) {
            return Optional.empty();
        }

        validar(properties);
        if (contaRepository.existsByLoginIgnoreCase(properties.login())) {
            throw new IllegalStateException("Login do administrador inicial já está em uso");
        }
        if (pessoaRepository.existsByCpf(properties.cpf())) {
            throw new IllegalStateException("CPF do administrador inicial já está em uso");
        }
        if (pessoaRepository.existsByEmailIgnoreCase(properties.email())) {
            throw new IllegalStateException("E-mail do administrador inicial já está em uso");
        }

        Pessoa pessoa = pessoaRepository.save(new Pessoa(
                properties.nome(),
                properties.email(),
                properties.cpf(),
                LocalDate.now(clock)));
        ContaAcesso conta = new ContaAcesso(
                pessoa,
                properties.login(),
                passwordEncoder.encode(properties.senha()));
        conta.concederPerfil(PerfilAcesso.ADMINISTRADOR);
        return Optional.of(contaRepository.save(conta));
    }

    private void validar(AdministradorInicialProperties properties) {
        exigirTexto(properties.nome(), "INITIAL_ADMIN_NAME");
        exigirTexto(properties.email(), "INITIAL_ADMIN_EMAIL");
        exigirTexto(properties.cpf(), "INITIAL_ADMIN_CPF");
        exigirTexto(properties.login(), "INITIAL_ADMIN_LOGIN");
        exigirTexto(properties.senha(), "INITIAL_ADMIN_PASSWORD");
        if (properties.senha().length() < 12) {
            throw new IllegalStateException(
                    "INITIAL_ADMIN_PASSWORD deve possuir ao menos 12 caracteres");
        }
        if (properties.senha().getBytes(StandardCharsets.UTF_8).length
                > LIMITE_BCRYPT_BYTES) {
            throw new IllegalStateException(
                    "INITIAL_ADMIN_PASSWORD deve possuir no máximo 72 bytes em UTF-8");
        }
    }

    private void exigirTexto(String valor, String variavel) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException(
                    variavel + " é obrigatória quando INITIAL_ADMIN_ENABLED=true");
        }
    }
}
