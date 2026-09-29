package com.curso.suporteos.api.controller;

import com.curso.suporteos.api.dto.*;
import com.curso.suporteos.api.exception.ApiError;
import com.curso.suporteos.api.mapper.VendaMapper;
import com.curso.suporteos.application.VendaService;
import com.curso.suporteos.domain.StatusVenda;
import com.curso.suporteos.domain.Venda;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;

@Tag(name = "Vendas", description = "Venda, itens, total histórico e baixa transacional de estoque")
@RestController @RequestMapping("/api/vendas")
public class VendaController {
    private final VendaService service; private final VendaMapper mapper;
    public VendaController(VendaService service, VendaMapper mapper) { this.service = service; this.mapper = mapper; }

    @Operation(summary = "Abrir venda", description = "Cria uma venda ABERTA para um cliente e um vendedor ativos.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Venda aberta"),
            @ApiResponse(responseCode = "400", description = "Participante inativo ou função incompatível", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Cliente ou colaborador não encontrado", content = @Content(schema = @Schema(implementation = ApiError.class)))})
    @PostMapping public ResponseEntity<VendaResponse> cadastrar(@Valid @RequestBody VendaRequest request) {
        Venda venda = service.cadastrar(request.clienteId(), request.vendedorId());
        return ResponseEntity.created(URI.create("/api/vendas/" + venda.getId())).body(mapper.toResponse(venda));
    }

    @Operation(summary = "Consultar venda por ID", description = "Retorna participantes, itens, preços históricos e total.")
    @GetMapping("/{id}") public VendaResponse buscar(@PathVariable Long id) { return mapper.toResponse(service.buscarPorId(id)); }

    @Operation(summary = "Pesquisar vendas", description = "Filtra por status, participantes e intervalo inclusivo de datas.")
    @GetMapping public PaginaResponse<VendaResumoResponse> pesquisar(@RequestParam(required = false) StatusVenda status,
            @RequestParam(required = false) Long clienteId, @RequestParam(required = false) Long vendedorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @PageableDefault(size = 20, sort = "dataVenda", direction = Sort.Direction.DESC) Pageable pageable) {
        return PaginaResponse.de(service.pesquisar(status, clienteId, vendedorId, dataInicial, dataFinal, pageable), mapper::toResumo);
    }

    @Operation(summary = "Adicionar item", description = "Captura o preço atual do produto; somente vendas abertas aceitam itens.")
    @PostMapping("/{id}/itens") public VendaResponse adicionarItem(@PathVariable Long id, @Valid @RequestBody ItemVendaRequest request) {
        return mapper.toResponse(service.adicionarItem(id, request.produtoId(), request.quantidade()));
    }

    @Operation(summary = "Remover item", description = "Remove um item enquanto a venda estiver aberta.")
    @DeleteMapping("/{vendaId}/itens/{itemId}") public VendaResponse removerItem(@PathVariable Long vendaId, @PathVariable Long itemId) {
        return mapper.toResponse(service.removerItem(vendaId, itemId));
    }

    @Operation(summary = "Finalizar venda", description = "Valida toda a venda e baixa o estoque de todos os itens na mesma transação.")
    @PostMapping("/{id}/finalizacao") public VendaResponse finalizar(@PathVariable Long id) {
        return mapper.toResponse(service.finalizar(id));
    }

    @Operation(summary = "Cancelar venda", description = "Cancela uma venda ainda aberta; como não houve baixa, não há estorno de estoque.")
    @PostMapping("/{id}/cancelamento") public VendaResponse cancelar(@PathVariable Long id) {
        return mapper.toResponse(service.cancelar(id));
    }
}
