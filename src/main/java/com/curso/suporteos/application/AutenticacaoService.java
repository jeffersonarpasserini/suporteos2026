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

    public AutenticacaoService(
            AuthenticationManager authenticationManager,
            SessaoAutenticacaoService sessaoService) {
        this.authenticationManager = authenticationManager;
        this.sessaoService = sessaoService;
    }

    public TokensAutenticacao autenticar(String login, String senha) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(login, senha));
            return sessaoService.iniciar(authentication);
        } catch (AuthenticationException exception) {
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
