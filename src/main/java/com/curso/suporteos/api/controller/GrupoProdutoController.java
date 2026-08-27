package com.curso.suporteos.api.controller;

import com.curso.suporteos.api.dto.GrupoProdutoRequest;
import com.curso.suporteos.api.dto.GrupoProdutoResponse;
import com.curso.suporteos.api.mapper.GrupoProdutoMapper;
import com.curso.suporteos.application.GrupoProdutoService;
import com.curso.suporteos.domain.GrupoProduto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

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
    public List<GrupoProdutoResponse> listar() {
        return service.listar().stream().map(mapper::toResponse).toList();
    }
}
