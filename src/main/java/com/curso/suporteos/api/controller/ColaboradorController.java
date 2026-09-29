package com.curso.suporteos.api.controller;

import com.curso.suporteos.api.dto.*;
import com.curso.suporteos.api.mapper.ColaboradorMapper;
import com.curso.suporteos.application.ColaboradorService;
import com.curso.suporteos.domain.Colaborador;
import com.curso.suporteos.domain.FuncaoColaborador;
import com.curso.suporteos.domain.Status;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "Colaboradores", description = "Vínculo profissional e função de negócio")
@RestController @RequestMapping("/api/colaboradores")
public class ColaboradorController {
    private final ColaboradorService service; private final ColaboradorMapper mapper;
    public ColaboradorController(ColaboradorService service, ColaboradorMapper mapper) { this.service = service; this.mapper = mapper; }

    @Operation(summary = "Cadastrar colaborador", description = "Associa um vínculo profissional a uma pessoa ativa.")
    @PostMapping public ResponseEntity<ColaboradorResponse> cadastrar(@Valid @RequestBody ColaboradorRequest request) {
        Colaborador c = service.cadastrar(request.pessoaId(), request.matricula(), request.funcao(), request.dataAdmissao());
        return ResponseEntity.created(URI.create("/api/colaboradores/" + c.getId())).body(mapper.toResponse(c));
    }
    @Operation(summary = "Consultar colaborador por ID")
    @GetMapping("/{id}") public ColaboradorResponse buscar(@PathVariable Long id) { return mapper.toResponse(service.buscarPorId(id)); }
    @Operation(summary = "Pesquisar colaboradores")
    @GetMapping public PaginaResponse<ColaboradorResponse> pesquisar(@RequestParam(required = false) String nome,
            @RequestParam(required = false) FuncaoColaborador funcao, @RequestParam(required = false) Status status,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return PaginaResponse.de(service.pesquisar(nome, funcao, status, pageable), mapper::toResponse);
    }
    @Operation(summary = "Alterar colaborador", description = "Altera matrícula e função de negócio; não concede permissões de acesso.")
    @PutMapping("/{id}") public ColaboradorResponse alterar(@PathVariable Long id, @Valid @RequestBody ColaboradorAtualizacaoRequest request) {
        return mapper.toResponse(service.alterar(id, request.matricula(), request.funcao()));
    }
}
