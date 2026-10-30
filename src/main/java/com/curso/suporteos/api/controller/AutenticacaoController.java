package com.curso.suporteos.api.controller;

import com.curso.suporteos.api.dto.LoginRequest;
import com.curso.suporteos.api.dto.RefreshTokenRequest;
import com.curso.suporteos.api.dto.TokenResponse;
import com.curso.suporteos.application.AutenticacaoService;
import com.curso.suporteos.application.TokensAutenticacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticação", description = "Emissão de token para uma conta válida")
@RestController
@RequestMapping("/api/autenticacao")
public class AutenticacaoController {

    private final AutenticacaoService service;

    public AutenticacaoController(AutenticacaoService service) {
        this.service = service;
    }

    @Operation(summary = "Autenticar e emitir JWT")
    @SecurityRequirements
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return toResponse(service.autenticar(request.login(), request.senha()));
    }

    @Operation(summary = "Renovar tokens com rotação do refresh token")
    @SecurityRequirements
    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return toResponse(service.renovar(request.refreshToken()));
    }

    @Operation(summary = "Encerrar a sessão representada pelo refresh token")
    @SecurityRequirements
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/logout")
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        service.encerrar(request.refreshToken());
    }

    @Operation(summary = "Encerrar todas as sessões da conta autenticada")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/logout-todos")
    public void logoutTodos(Authentication authentication) {
        service.encerrarTodas(authentication.getName());
    }

    private TokenResponse toResponse(TokensAutenticacao tokens) {
        return new TokenResponse(
                tokens.acesso().valor(),
                "Bearer",
                tokens.acesso().duracaoSegundos(),
                tokens.acesso().expiraEm(),
                tokens.refreshToken(),
                tokens.refreshTokenExpiraEm());
    }
}
