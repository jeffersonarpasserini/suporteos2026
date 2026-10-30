package com.curso.suporteos.application;

import com.curso.suporteos.config.ProtecaoAutenticacaoProperties;
import com.curso.suporteos.domain.ContaAcesso;
import com.curso.suporteos.domain.ProtecaoAutenticacao;
import com.curso.suporteos.domain.TipoEventoSeguranca;
import com.curso.suporteos.repository.ContaAcessoRepository;
import com.curso.suporteos.repository.ProtecaoAutenticacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class ProtecaoAutenticacaoService {

    private final ProtecaoAutenticacaoRepository repository;
    private final ContaAcessoRepository contaRepository;
    private final AuditoriaSegurancaService auditoriaService;
    private final ProtecaoAutenticacaoProperties properties;
    private final Clock clock;

    public ProtecaoAutenticacaoService(
            ProtecaoAutenticacaoRepository repository,
            ContaAcessoRepository contaRepository,
            AuditoriaSegurancaService auditoriaService,
            ProtecaoAutenticacaoProperties properties,
            Clock clock) {
        this.repository = repository;
        this.contaRepository = contaRepository;
        this.auditoriaService = auditoriaService;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public boolean permitirTentativa(String login, String enderecoOrigem) {
        String chaveLogin = calcularChave(login);
        ProtecaoAutenticacao protecao = repository.findByChaveLogin(chaveLogin)
                .orElse(null);
        if (protecao == null || !protecao.estaBloqueada(clock.instant())) {
            return true;
        }

        Long contaId = buscarContaId(login);
        auditoriaService.registrar(
                TipoEventoSeguranca.LOGIN_RECUSADO_BLOQUEIO,
                contaId,
                contaId,
                chaveLogin,
                enderecoOrigem);
        return false;
    }

    @Transactional
    public void registrarFalha(String login, String enderecoOrigem) {
        String chaveLogin = calcularChave(login);
        repository.garantirRegistro(chaveLogin);
        ProtecaoAutenticacao protecao = repository
                .buscarParaAtualizacao(chaveLogin)
                .orElseThrow(() -> new IllegalStateException(
                        "Não foi possível controlar as tentativas de autenticação"));
        Instant agora = clock.instant();
        boolean bloqueioAtivado = protecao.registrarFalha(
                agora,
                properties.maxFailedAttempts(),
                properties.failureWindow(),
                properties.lockDuration());
        Long contaId = buscarContaId(login);

        auditoriaService.registrar(
                TipoEventoSeguranca.LOGIN_FALHA,
                contaId,
                contaId,
                chaveLogin,
                enderecoOrigem);
        if (bloqueioAtivado) {
            auditoriaService.registrar(
                    TipoEventoSeguranca.BLOQUEIO_TEMPORARIO_ATIVADO,
                    contaId,
                    contaId,
                    chaveLogin,
                    enderecoOrigem);
        }
    }

    @Transactional
    public void registrarSucesso(String login, String enderecoOrigem) {
        String chaveLogin = calcularChave(login);
        repository.buscarParaAtualizacao(chaveLogin)
                .ifPresent(ProtecaoAutenticacao::registrarSucesso);
        Long contaId = buscarContaId(login);
        auditoriaService.registrar(
                TipoEventoSeguranca.LOGIN_SUCESSO,
                contaId,
                contaId,
                chaveLogin,
                enderecoOrigem);
    }

    private Long buscarContaId(String login) {
        return contaRepository.findByLoginIgnoreCase(login)
                .map(ContaAcesso::getId)
                .orElse(null);
    }

    private String calcularChave(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Login é obrigatório");
        }
        String normalizado = login.trim().toLowerCase(Locale.ROOT);
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(normalizado.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 não está disponível", exception);
        }
    }
}
