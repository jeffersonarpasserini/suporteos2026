package com.curso.suporteos.api.controller;

import com.curso.suporteos.api.dto.GrupoProdutoRequest;
import com.curso.suporteos.api.dto.GrupoProdutoAtualizacaoRequest;
import com.curso.suporteos.api.dto.GrupoProdutoResponse;
import com.curso.suporteos.api.dto.PaginaResponse;
import com.curso.suporteos.api.dto.StatusRequest;
import com.curso.suporteos.api.exception.ApiError;
import com.curso.suporteos.api.mapper.GrupoProdutoMapper;
import com.curso.suporteos.application.GrupoProdutoService;
import com.curso.suporteos.domain.GrupoProduto;
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

@Tag(name = "Grupos de produtos", description = "Classificação, consulta e situação dos produtos")
@RestController
@RequestMapping("/api/grupos-produtos")
public class GrupoProdutoController {

    private final GrupoProdutoService service;
    private final GrupoProdutoMapper mapper;

    public GrupoProdutoController(GrupoProdutoService service, GrupoProdutoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(summary = "Cadastrar grupo", description = "Cria um grupo ativo e devolve sua localização.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Grupo cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Já existe grupo com o mesmo nome",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<GrupoProdutoResponse> cadastrar(
            @Valid @RequestBody GrupoProdutoRequest request) {
        GrupoProduto grupo = service.cadastrar(request.nome());
        URI location = URI.create("/api/grupos-produtos/" + grupo.getId());
        return ResponseEntity.created(location).body(mapper.toResponse(grupo));
    }

    @Operation(summary = "Consultar grupo por ID",
            description = "Retorna os dados usados na visualização e na tela de alteração.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Grupo encontrado"),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public GrupoProdutoResponse buscarPorId(
            @Parameter(description = "Identificador do grupo", example = "1")
            @PathVariable Long id) {
        return mapper.toResponse(service.buscarPorId(id));
    }

    @Operation(summary = "Pesquisar grupos",
            description = "Combina filtros opcionais e devolve uma página ordenável.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pesquisa executada"),
            @ApiResponse(responseCode = "400", description = "Paginação ou ordenação inválida",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public PaginaResponse<GrupoProdutoResponse> pesquisar(
            @Parameter(description = "Trecho do nome, sem diferenciar maiúsculas de minúsculas",
                    example = "papel")
            @RequestParam(required = false) String nome,
            @Parameter(description = "Status exato do grupo", example = "ATIVO")
            @RequestParam(required = false) Status status,
            @Parameter(description = "Parâmetros page, size e sort. A página começa em zero.")
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return PaginaResponse.de(
                service.pesquisar(nome, status, pageable),
                mapper::toResponse);
    }

    @Operation(summary = "Alterar grupo",
            description = "Substitui os campos editáveis do grupo. O status possui operação própria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Grupo alterado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Nome já utilizado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public GrupoProdutoResponse alterar(
            @Parameter(description = "Identificador do grupo", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody GrupoProdutoAtualizacaoRequest request) {
        return mapper.toResponse(service.alterar(id, request.nome()));
    }

    @Operation(summary = "Ativar ou inativar grupo",
            description = "Executa uma transição explícita de status sem alterar os demais campos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status alterado"),
            @ApiResponse(responseCode = "400", description = "Status inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}/status")
    public GrupoProdutoResponse alterarStatus(
            @Parameter(description = "Identificador do grupo", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request) {
        return mapper.toResponse(service.alterarStatus(id, request.status()));
    }

    @Operation(summary = "Excluir grupo",
            description = "Exclui fisicamente somente quando nenhum produto utiliza o grupo.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Grupo excluído"),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Grupo em uso por produtos",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @Parameter(description = "Identificador do grupo", example = "1")
            @PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
