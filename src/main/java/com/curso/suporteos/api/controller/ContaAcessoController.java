package com.curso.suporteos.api.controller;

import com.curso.suporteos.api.dto.AlterarSenhaContaRequest;
import com.curso.suporteos.api.dto.ContaAcessoRequest;
import com.curso.suporteos.api.dto.ContaAcessoResponse;
import com.curso.suporteos.api.dto.PerfisContaAcessoRequest;
import com.curso.suporteos.api.dto.StatusContaAcessoRequest;
import com.curso.suporteos.api.mapper.ContaAcessoMapper;
import com.curso.suporteos.application.ContaAcessoService;
import com.curso.suporteos.domain.ContaAcesso;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Tag(name = "Contas de acesso", description = "Administração de contas e dados da conta autenticada")
@RestController
@RequestMapping("/api/contas-acesso")
public class ContaAcessoController {

    private final ContaAcessoService service;
    private final ContaAcessoMapper mapper;

    public ContaAcessoController(ContaAcessoService service, ContaAcessoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(summary = "Cadastrar conta", description = "Cria uma conta OPERADOR para uma pessoa ativa. Exige ADMINISTRADOR.")
    @PostMapping
    public ResponseEntity<ContaAcessoResponse> cadastrar(
            Authentication authentication,
            @Valid @RequestBody ContaAcessoRequest request) {
        ContaAcesso conta = service.cadastrar(
                request.pessoaId(),
                request.login(),
                request.senha(),
                authentication.getName());
        return ResponseEntity
                .created(URI.create("/api/contas-acesso/me"))
                .body(mapper.toResponse(conta));
    }

    @Operation(summary = "Consultar minha conta")
    @GetMapping("/me")
    public ContaAcessoResponse buscarAtual(Authentication authentication) {
        return mapper.toResponse(service.buscarPorLogin(authentication.getName()));
    }

    @Operation(summary = "Alterar minha senha")
    @PutMapping("/me/senha")
    public ResponseEntity<Void> alterarSenha(
            Authentication authentication,
            @Valid @RequestBody AlterarSenhaContaRequest request) {
        service.alterarSenha(
                authentication.getName(),
                request.senhaAtual(),
                request.novaSenha());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Alterar perfis", description = "Substitui os perfis da conta. Exige ADMINISTRADOR.")
    @PutMapping("/{id}/perfis")
    public ContaAcessoResponse alterarPerfis(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody PerfisContaAcessoRequest request) {
        return mapper.toResponse(service.alterarPerfis(
                id,
                request.perfis(),
                authentication.getName()));
    }

    @Operation(summary = "Alterar status", description = "Ativa ou bloqueia uma conta. Exige ADMINISTRADOR.")
    @PutMapping("/{id}/status")
    public ContaAcessoResponse alterarStatus(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody StatusContaAcessoRequest request) {
        return mapper.toResponse(service.alterarStatus(
                id,
                request.status(),
                authentication.getName()));
    }
}
