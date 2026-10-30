package com.curso.suporteos.application;

import com.curso.suporteos.config.RefreshTokenProperties;
import com.curso.suporteos.domain.ContaAcesso;
import com.curso.suporteos.domain.MotivoRevogacaoRefreshToken;
import com.curso.suporteos.domain.SessaoRefreshToken;
import com.curso.suporteos.domain.TipoEventoSeguranca;
import com.curso.suporteos.repository.ContaAcessoRepository;
import com.curso.suporteos.repository.SessaoRefreshTokenRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class SessaoAutenticacaoService {

    private static final int TAMANHO_REFRESH_TOKEN_BYTES = 32;

    private final SessaoRefreshTokenRepository sessaoRepository;
    private final ContaAcessoRepository contaRepository;
    private final TokenService tokenService;
    private final RefreshTokenProperties properties;
    private final AuditoriaSegurancaService auditoriaService;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public SessaoAutenticacaoService(
            SessaoRefreshTokenRepository sessaoRepository,
            ContaAcessoRepository contaRepository,
            TokenService tokenService,
            RefreshTokenProperties properties,
            AuditoriaSegurancaService auditoriaService,
            Clock clock) {
        this.sessaoRepository = sessaoRepository;
        this.contaRepository = contaRepository;
        this.tokenService = tokenService;
        this.properties = properties;
        this.auditoriaService = auditoriaService;
        this.clock = clock;
    }

    @Transactional
    public TokensAutenticacao iniciar(Authentication authentication) {
        ContaAcesso conta = contaRepository.findByLoginIgnoreCase(authentication.getName())
                .orElseThrow(RefreshTokenInvalidoException::new);
        Instant agora = clock.instant();
        String familiaId = UUID.randomUUID().toString();
        Instant expiraEm = agora.plus(properties.absoluteExpiration());
        return emitirPar(conta, authentication, familiaId, agora, expiraEm);
    }

    @Transactional(noRollbackFor = RefreshTokenInvalidoException.class)
    public TokensAutenticacao renovar(String refreshToken) {
        String hash = calcularHash(refreshToken);
        SessaoRefreshToken sessao = sessaoRepository
                .buscarPorHashParaAtualizacao(hash)
                .orElseThrow(() -> {
                    auditoriaService.registrar(
                            TipoEventoSeguranca.REFRESH_FALHA,
                            null,
                            null,
                            null,
                            null);
                    return new RefreshTokenInvalidoException();
                });
        Instant agora = clock.instant();

        validarSessaoParaRenovacao(sessao, agora);
        sessao.marcarUtilizada(agora);

        Authentication authentication = criarAuthentication(sessao.getConta());
        TokensAutenticacao tokens = emitirPar(
                sessao.getConta(),
                authentication,
                sessao.getFamiliaId(),
                agora,
                sessao.getExpiraEm());
        auditoriaService.registrar(
                TipoEventoSeguranca.REFRESH_SUCESSO,
                sessao.getConta().getId(),
                sessao.getConta().getId(),
                null,
                null);
        return tokens;
    }

    @Transactional
    public void encerrar(String refreshToken) {
        String hash = calcularHash(refreshToken);
        sessaoRepository.buscarPorHashParaAtualizacao(hash).ifPresent(sessao -> {
                sessaoRepository.revogarFamilia(
                        sessao.getFamiliaId(),
                        clock.instant(),
                        MotivoRevogacaoRefreshToken.LOGOUT);
                auditoriaService.registrar(
                        TipoEventoSeguranca.LOGOUT,
                        sessao.getConta().getId(),
                        sessao.getConta().getId(),
                        null,
                        null);
        });
    }

    @Transactional
    public void encerrarTodas(String login) {
        ContaAcesso conta = contaRepository.findByLoginIgnoreCase(login)
                .orElseThrow(RefreshTokenInvalidoException::new);
        sessaoRepository.revogarTodasDaConta(
                conta.getId(),
                clock.instant(),
                MotivoRevogacaoRefreshToken.LOGOUT_GLOBAL);
        auditoriaService.registrar(
                TipoEventoSeguranca.LOGOUT_GLOBAL,
                conta.getId(),
                conta.getId(),
                null,
                null);
    }

    private TokensAutenticacao emitirPar(
            ContaAcesso conta,
            Authentication authentication,
            String familiaId,
            Instant agora,
            Instant refreshTokenExpiraEm) {
        String refreshToken = gerarRefreshToken();
        SessaoRefreshToken sessao = new SessaoRefreshToken(
                conta,
                familiaId,
                calcularHash(refreshToken),
                agora,
                agora,
                refreshTokenExpiraEm);
        sessaoRepository.save(sessao);

        TokenAcesso acesso = tokenService.emitir(authentication, familiaId);
        return new TokensAutenticacao(acesso, refreshToken, refreshTokenExpiraEm);
    }

    private void validarSessaoParaRenovacao(
            SessaoRefreshToken sessao,
            Instant agora) {
        if (sessao.foiUtilizada()) {
            revogarERejeitar(sessao, agora, MotivoRevogacaoRefreshToken.REUTILIZACAO);
        }
        if (sessao.foiRevogada()) {
            registrarFalhaRefresh(sessao);
            throw new RefreshTokenInvalidoException();
        }
        if (sessao.expirouAbsolutamente(agora)) {
            revogarERejeitar(
                    sessao,
                    agora,
                    MotivoRevogacaoRefreshToken.EXPIRACAO_ABSOLUTA);
        }
        if (sessao.expirouPorInatividade(agora, properties.idleTimeout())) {
            revogarERejeitar(sessao, agora, MotivoRevogacaoRefreshToken.INATIVIDADE);
        }
        if (!sessao.getConta().podeAutenticar()) {
            revogarERejeitar(sessao, agora, MotivoRevogacaoRefreshToken.CONTA_INATIVA);
        }
    }

    private void revogarERejeitar(
            SessaoRefreshToken sessao,
            Instant agora,
            MotivoRevogacaoRefreshToken motivo) {
        sessaoRepository.revogarFamilia(sessao.getFamiliaId(), agora, motivo);
        registrarFalhaRefresh(sessao);
        throw new RefreshTokenInvalidoException();
    }

    private void registrarFalhaRefresh(SessaoRefreshToken sessao) {
        auditoriaService.registrar(
                TipoEventoSeguranca.REFRESH_FALHA,
                sessao.getConta().getId(),
                sessao.getConta().getId(),
                null,
                null);
    }

    private Authentication criarAuthentication(ContaAcesso conta) {
        var authorities = conta.getPerfis().stream()
                .map(perfil -> new SimpleGrantedAuthority("ROLE_" + perfil.name()))
                .toList();
        return UsernamePasswordAuthenticationToken.authenticated(
                conta.getLogin(),
                "[PROTECTED]",
                authorities);
    }

    private String gerarRefreshToken() {
        byte[] bytes = new byte[TAMANHO_REFRESH_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String calcularHash(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RefreshTokenInvalidoException();
        }
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 não está disponível", exception);
        }
    }
}
