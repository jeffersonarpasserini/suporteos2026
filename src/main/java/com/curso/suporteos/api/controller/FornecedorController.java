package com.curso.suporteos.api.controller;

import com.curso.suporteos.api.dto.FornecedorRequest;
import com.curso.suporteos.api.dto.FornecedorResponse;
import com.curso.suporteos.api.exception.ApiError;
import com.curso.suporteos.api.mapper.FornecedorMapper;
import com.curso.suporteos.application.FornecedorService;
import com.curso.suporteos.domain.Fornecedor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Fornecedores", description = "Cadastro e consulta de fornecedores")
@RestController
@RequestMapping("/api/fornecedores")
public class FornecedorController {

    private final FornecedorService service;
    private final FornecedorMapper mapper;

    public FornecedorController(FornecedorService service, FornecedorMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(summary = "Cadastrar fornecedor")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Fornecedor cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "CNPJ já utilizado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<FornecedorResponse> cadastrar(
            @Valid @RequestBody FornecedorRequest request) {
        Fornecedor fornecedor = service.cadastrar(mapper.toEntity(request));
        URI location = URI.create("/api/fornecedores/" + fornecedor.getId());
        return ResponseEntity.created(location).body(mapper.toResponse(fornecedor));
    }

    @Operation(summary = "Consultar fornecedor por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fornecedor encontrado"),
            @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public FornecedorResponse buscarPorId(
            @Parameter(description = "Identificador do fornecedor", example = "1")
            @PathVariable Long id) {
        return mapper.toResponse(service.buscarPorId(id));
    }

    @Operation(summary = "Listar fornecedores",
            description = "Retorna todos os fornecedores; paginação será introduzida quando este cadastro evoluir.")
    @ApiResponse(responseCode = "200", description = "Fornecedores listados")
    @GetMapping
    public List<FornecedorResponse> listar() {
        return service.listar().stream().map(mapper::toResponse).toList();
    }
}
