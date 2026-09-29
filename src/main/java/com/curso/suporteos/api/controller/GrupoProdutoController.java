package com.curso.suporteos.api.controller;

import com.curso.suporteos.api.dto.GrupoProdutoRequest;
import com.curso.suporteos.api.dto.GrupoProdutoAtualizacaoRequest;
import com.curso.suporteos.api.dto.GrupoProdutoResponse;
import com.curso.suporteos.api.dto.PaginaResponse;
import com.curso.suporteos.api.dto.StatusRequest;
import com.curso.suporteos.api.mapper.GrupoProdutoMapper;
import com.curso.suporteos.application.GrupoProdutoService;
import com.curso.suporteos.domain.GrupoProduto;
import com.curso.suporteos.domain.Status;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
@RestController
@RequestMapping("/api/grupos-produtos")
public class GrupoProdutoController {

    private final GrupoProdutoService service;
    private final GrupoProdutoMapper mapper;

    public GrupoProdutoController(GrupoProdutoService service, GrupoProdutoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<GrupoProdutoResponse> cadastrar(
            @Valid @RequestBody GrupoProdutoRequest request) {
        GrupoProduto grupo = service.cadastrar(request.nome());
        URI location = URI.create("/api/grupos-produtos/" + grupo.getId());
        return ResponseEntity.created(location).body(mapper.toResponse(grupo));
    }

    @GetMapping("/{id}")
    public GrupoProdutoResponse buscarPorId(@PathVariable Long id) {
        return mapper.toResponse(service.buscarPorId(id));
    }

    @GetMapping
    public PaginaResponse<GrupoProdutoResponse> pesquisar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Status status,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return PaginaResponse.de(
                service.pesquisar(nome, status, pageable),
                mapper::toResponse);
    }

    @PutMapping("/{id}")
    public GrupoProdutoResponse alterar(
            @PathVariable Long id,
            @Valid @RequestBody GrupoProdutoAtualizacaoRequest request) {
        return mapper.toResponse(service.alterar(id, request.nome()));
    }

    @PutMapping("/{id}/status")
    public GrupoProdutoResponse alterarStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request) {
        return mapper.toResponse(service.alterarStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
