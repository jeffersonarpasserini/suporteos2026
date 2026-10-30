package com.curso.suporteos.application;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService {

    private final AuthenticationManager authenticationManager;
    private final SessaoAutenticacaoService sessaoService;
    private final ProtecaoAutenticacaoService protecaoService;

    public AutenticacaoService(
            AuthenticationManager authenticationManager,
            SessaoAutenticacaoService sessaoService,
            ProtecaoAutenticacaoService protecaoService) {
        this.authenticationManager = authenticationManager;
        this.sessaoService = sessaoService;
        this.protecaoService = protecaoService;
    }

    public TokensAutenticacao autenticar(
            String login,
            String senha,
            String enderecoOrigem) {
        if (!protecaoService.permitirTentativa(login, enderecoOrigem)) {
            throw new CredenciaisInvalidasException("Login ou senha inválidos");
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(login, senha));
            TokensAutenticacao tokens = sessaoService.iniciar(authentication);
            protecaoService.registrarSucesso(login, enderecoOrigem);
            return tokens;
        } catch (AuthenticationException exception) {
            protecaoService.registrarFalha(login, enderecoOrigem);
            throw new CredenciaisInvalidasException("Login ou senha inválidos");
        }
    }

    public TokensAutenticacao renovar(String refreshToken) {
        return sessaoService.renovar(refreshToken);
    }

    public void encerrar(String refreshToken) {
        sessaoService.encerrar(refreshToken);
    }

    public void encerrarTodas(String login) {
        sessaoService.encerrarTodas(login);
    }
}
