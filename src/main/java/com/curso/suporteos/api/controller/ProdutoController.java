package com.curso.suporteos.api.controller;

import com.curso.suporteos.api.dto.ProdutoRequest;
import com.curso.suporteos.api.dto.ProdutoAtualizacaoRequest;
import com.curso.suporteos.api.dto.ProdutoResponse;
import com.curso.suporteos.api.dto.MovimentacaoEstoqueRequest;
import com.curso.suporteos.api.dto.PaginaResponse;
import com.curso.suporteos.api.dto.StatusRequest;
import com.curso.suporteos.api.exception.ApiError;
import com.curso.suporteos.api.mapper.ProdutoMapper;
import com.curso.suporteos.application.ProdutoService;
import com.curso.suporteos.domain.Produto;
import com.curso.suporteos.domain.Status;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

@Tag(name = "Produtos", description = "Cadastro, consulta e movimentação de estoque")
@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService service;
    private final ProdutoMapper mapper;

    public ProdutoController(ProdutoService service, ProdutoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(summary = "Cadastrar produto",
            description = "Cria um produto ativo associado a um grupo e, opcionalmente, a um fornecedor.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produto cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Grupo ou fornecedor não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Código de barras já utilizado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<ProdutoResponse> cadastrar(
            @Valid @RequestBody ProdutoRequest request) {
        Produto produto = mapper.toEntity(request);
        Produto cadastrado = service.cadastrar(
                produto,
                request.grupoId(),
                request.fornecedorId());
        URI location = URI.create("/api/produtos/" + cadastrado.getId());
        return ResponseEntity.created(location).body(mapper.toResponse(cadastrado));
    }

    @Operation(summary = "Consultar produto por ID",
            description = "Retorna a representação completa usada na visualização e na tela de alteração.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto encontrado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public ProdutoResponse buscarPorId(
            @Parameter(description = "Identificador do produto", example = "1")
            @PathVariable Long id) {
        return mapper.toResponse(service.buscarPorId(id));
    }

    @Operation(summary = "Pesquisar produtos",
            description = "Combina filtros opcionais e executa paginação e ordenação no banco de dados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pesquisa executada"),
            @ApiResponse(responseCode = "400", description = "Paginação ou ordenação inválida",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public PaginaResponse<ProdutoResponse> pesquisar(
            @Parameter(description = "Trecho da descrição, sem diferenciar maiúsculas de minúsculas",
                    example = "caderno")
            @RequestParam(required = false) String descricao,
            @Parameter(description = "Status exato do produto", example = "ATIVO")
            @RequestParam(required = false) Status status,
            @Parameter(description = "Identificador do grupo", example = "1")
            @RequestParam(required = false) Long grupoId,
            @Parameter(description = "Identificador do fornecedor", example = "1")
            @RequestParam(required = false) Long fornecedorId,
            @Parameter(description = "Quando verdadeiro, retorna produtos com saldo abaixo do mínimo",
                    example = "true")
            @RequestParam(required = false) Boolean abaixoEstoqueMinimo,
            @Parameter(description = "Parâmetros page, size e sort. A página começa em zero.")
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return PaginaResponse.de(
                service.pesquisar(
                        descricao,
                        status,
                        grupoId,
                        fornecedorId,
                        abaixoEstoqueMinimo,
                        pageable),
                mapper::toResponse);
    }

    @Operation(summary = "Alterar produto",
            description = "Substitui descrição, preço, estoque mínimo, grupo e fornecedor. "
                    + "Código, saldo e status possuem fluxos próprios.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto alterado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Produto, grupo ou fornecedor não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Operação incompatível com a integridade dos dados",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public ProdutoResponse alterar(
            @Parameter(description = "Identificador do produto", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ProdutoAtualizacaoRequest request) {
        return mapper.toResponse(service.alterar(
                id,
                request.descricao(),
                request.valorUnitario(),
                request.estoqueMinimo(),
                request.grupoId(),
                request.fornecedorId()));
    }

    @Operation(summary = "Ativar ou inativar produto",
            description = "Executa uma transição explícita de status sem alterar os demais campos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status alterado"),
            @ApiResponse(responseCode = "400", description = "Status inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}/status")
    public ProdutoResponse alterarStatus(
            @Parameter(description = "Identificador do produto", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request) {
        return mapper.toResponse(service.alterarStatus(id, request.status()));
    }

    @Operation(summary = "Receber estoque",
            description = "Soma uma quantidade positiva ao saldo atual. Repetir a chamada repete o efeito.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entrada registrada"),
            @ApiResponse(responseCode = "400", description = "Quantidade inválida",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/{id}/estoque/entradas")
    public ProdutoResponse receberEstoque(
            @Parameter(description = "Identificador do produto", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody MovimentacaoEstoqueRequest request) {
        return mapper.toResponse(service.receberEstoque(id, request.quantidade()));
    }

    @Operation(summary = "Retirar estoque",
            description = "Subtrai uma quantidade positiva sem permitir saldo negativo. Repetir a chamada repete o efeito.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saída registrada"),
            @ApiResponse(responseCode = "400", description = "Quantidade inválida ou saldo insuficiente",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/{id}/estoque/saidas")
    public ProdutoResponse retirarEstoque(
            @Parameter(description = "Identificador do produto", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody MovimentacaoEstoqueRequest request) {
        return mapper.toResponse(service.retirarEstoque(id, request.quantidade()));
    }

    @Operation(summary = "Excluir produto", description = "Exclui fisicamente o produto informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Produto excluído"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Produto referenciado por outro registro",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @Parameter(description = "Identificador do produto", example = "1")
            @PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
