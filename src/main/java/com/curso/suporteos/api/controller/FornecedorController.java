package com.curso.suporteos.api.controller;

import com.curso.suporteos.api.dto.FornecedorRequest;
import com.curso.suporteos.api.dto.FornecedorResponse;
import com.curso.suporteos.api.mapper.FornecedorMapper;
import com.curso.suporteos.application.FornecedorService;
import com.curso.suporteos.domain.Fornecedor;
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
@RequestMapping("/api/fornecedores")
public class FornecedorController {

    private final FornecedorService service;
    private final FornecedorMapper mapper;

    public FornecedorController(FornecedorService service, FornecedorMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<FornecedorResponse> cadastrar(
            @Valid @RequestBody FornecedorRequest request) {
        Fornecedor fornecedor = service.cadastrar(mapper.toEntity(request));
        URI location = URI.create("/api/fornecedores/" + fornecedor.getId());
        return ResponseEntity.created(location).body(mapper.toResponse(fornecedor));
    }

    @GetMapping("/{id}")
    public FornecedorResponse buscarPorId(@PathVariable Long id) {
        return mapper.toResponse(service.buscarPorId(id));
    }

    @GetMapping
    public List<FornecedorResponse> listar() {
        return service.listar().stream().map(mapper::toResponse).toList();
    }
}
