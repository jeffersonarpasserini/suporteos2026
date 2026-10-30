package com.curso.suporteos.api.controller;

import com.curso.suporteos.api.dto.AlterarSenhaContaRequest;
import com.curso.suporteos.api.dto.ContaAcessoRequest;
import com.curso.suporteos.api.dto.ContaAcessoResponse;
import com.curso.suporteos.api.mapper.ContaAcessoMapper;
import com.curso.suporteos.application.ContaAcessoService;
import com.curso.suporteos.domain.ContaAcesso;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Tag(name = "Contas de acesso", description = "Cadastro didático e dados da conta autenticada")
@RestController
@RequestMapping("/api/contas-acesso")
public class ContaAcessoController {

    private final ContaAcessoService service;
    private final ContaAcessoMapper mapper;

    public ContaAcessoController(ContaAcessoService service, ContaAcessoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(summary = "Cadastrar conta", description = "Cria uma conta OPERADOR para uma pessoa ativa. Rota pública apenas para fins didáticos.")
    @SecurityRequirements
    @PostMapping
    public ResponseEntity<ContaAcessoResponse> cadastrar(
            @Valid @RequestBody ContaAcessoRequest request) {
        ContaAcesso conta = service.cadastrar(
                request.pessoaId(),
                request.login(),
                request.senha());
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
}
