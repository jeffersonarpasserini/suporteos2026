package com.curso.suporteos.api;

import com.curso.suporteos.domain.Fornecedor;
import com.curso.suporteos.domain.GrupoProduto;
import com.curso.suporteos.domain.Produto;
import com.curso.suporteos.repository.FornecedorRepository;
import com.curso.suporteos.repository.GrupoProdutoRepository;
import com.curso.suporteos.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProdutoApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GrupoProdutoRepository grupoRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Test
    void deveCadastrarProdutoERetornar201() throws Exception {
        GrupoProduto grupo = grupoRepository.save(new GrupoProduto("Grupo API"));
        Fornecedor fornecedor = fornecedorRepository.save(new Fornecedor(
                "Fornecedor API",
                "11222333000181"));

        String json = """
                {
                  "codigoBarras": "API-001",
                  "descricao": "Produto criado pela API",
                  "saldoEstoque": 10.000,
                  "valorUnitario": 49.90,
                  "estoqueMinimo": 2.000,
                  "grupoId": %d,
                  "fornecedorId": %d
                }
                """.formatted(grupo.getId(), fornecedor.getId());

        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.codigoBarras").value("API-001"))
                .andExpect(jsonPath("$.grupoNome").value("Grupo API"))
                .andExpect(jsonPath("$.fornecedorRazaoSocial").value("Fornecedor API"));
    }

    @Test
    void deveRetornar400ComErrosPorCampo() throws Exception {
        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Um ou mais campos são inválidos"))
                .andExpect(jsonPath("$.fields.codigoBarras").exists())
                .andExpect(jsonPath("$.fields.grupoId").exists());
    }

    @Test
    void deveRetornar404ParaProdutoInexistente() throws Exception {
        mockMvc.perform(get("/api/produtos/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Produto não encontrado"))
                .andExpect(jsonPath("$.path").value("/api/produtos/" + Long.MAX_VALUE));
    }

    @Test
    void deveBuscarProdutoPorIdParaPreencherTelaDeAlteracao() throws Exception {
        GrupoProduto grupo = grupoRepository.save(new GrupoProduto("Grupo formulário"));
        Fornecedor fornecedor = fornecedorRepository.save(new Fornecedor(
                "Fornecedor formulário",
                "11444777000161"));
        Produto produto = new Produto(
                "API-FORMULARIO-001",
                "Produto para alteração",
                new BigDecimal("10.000"),
                new BigDecimal("49.90"),
                new BigDecimal("2.000"),
                LocalDate.of(2026, 9, 29));
        grupo.adicionarProduto(produto);
        produto.associarFornecedor(fornecedor);
        produtoRepository.save(produto);

        mockMvc.perform(get("/api/produtos/{id}", produto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(produto.getId()))
                .andExpect(jsonPath("$.codigoBarras").value("API-FORMULARIO-001"))
                .andExpect(jsonPath("$.descricao").value("Produto para alteração"))
                .andExpect(jsonPath("$.saldoEstoque").value(10.0))
                .andExpect(jsonPath("$.valorUnitario").value(49.9))
                .andExpect(jsonPath("$.estoqueMinimo").value(2.0))
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andExpect(jsonPath("$.grupoId").value(grupo.getId()))
                .andExpect(jsonPath("$.grupoNome").value("Grupo formulário"))
                .andExpect(jsonPath("$.fornecedorId").value(fornecedor.getId()))
                .andExpect(jsonPath("$.fornecedorRazaoSocial")
                        .value("Fornecedor formulário"));
    }

    @Test
    void deveAlterarStatusEMovimentarEstoque() throws Exception {
        GrupoProduto grupo = grupoRepository.save(new GrupoProduto("Grupo operações"));
        Produto produto = salvarProduto(grupo, "API-OPERACOES-001", "Produto original");

        String alteracao = """
                {
                  "descricao": "Produto alterado",
                  "valorUnitario": 59.90,
                  "estoqueMinimo": 4.000,
                  "grupoId": %d,
                  "fornecedorId": null
                }
                """.formatted(grupo.getId());

        mockMvc.perform(put("/api/produtos/{id}", produto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alteracao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Produto alterado"));

        mockMvc.perform(put("/api/produtos/{id}/status", produto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INATIVO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INATIVO"));

        mockMvc.perform(post("/api/produtos/{id}/estoque/entradas", produto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\":3.000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoEstoque").value(13.0));

        mockMvc.perform(post("/api/produtos/{id}/estoque/saidas", produto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\":2.000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoEstoque").value(11.0));
    }

    @Test
    void devePesquisarProdutosComFiltrosEPaginacao() throws Exception {
        GrupoProduto grupo = grupoRepository.save(new GrupoProduto("Grupo pesquisa"));
        salvarProduto(grupo, "API-PESQUISA-001", "Mouse ergonômico");
        salvarProduto(grupo, "API-PESQUISA-002", "Teclado mecânico");

        mockMvc.perform(get("/api/produtos")
                        .param("descricao", "mouse")
                        .param("grupoId", grupo.getId().toString())
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "descricao,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo.length()").value(1))
                .andExpect(jsonPath("$.conteudo[0].descricao").value("Mouse ergonômico"))
                .andExpect(jsonPath("$.totalElementos").value(1));
    }

    @Test
    void deveExcluirProduto() throws Exception {
        GrupoProduto grupo = grupoRepository.save(new GrupoProduto("Grupo exclusão"));
        Produto produto = salvarProduto(grupo, "API-EXCLUSAO-001", "Produto descartável");

        mockMvc.perform(delete("/api/produtos/{id}", produto.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void naoDeveRetirarQuantidadeMaiorQueOSaldo() throws Exception {
        GrupoProduto grupo = grupoRepository.save(new GrupoProduto("Grupo saldo insuficiente"));
        Produto produto = salvarProduto(
                grupo,
                "API-SALDO-INSUFICIENTE-001",
                "Produto com saldo limitado");

        mockMvc.perform(post("/api/produtos/{id}/estoque/saidas", produto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\":10.001}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Saldo de estoque insuficiente"));
    }

    @Test
    void deveRejeitarCampoDeOrdenacaoNaoPermitido() throws Exception {
        mockMvc.perform(get("/api/produtos")
                        .param("sort", "fornecedor.razaoSocial,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Campo de ordenação inválido: fornecedor.razaoSocial"));
    }

    private Produto salvarProduto(GrupoProduto grupo, String codigo, String descricao) {
        Produto produto = new Produto(
                codigo,
                descricao,
                new BigDecimal("10.000"),
                new BigDecimal("49.90"),
                new BigDecimal("2.000"),
                LocalDate.of(2026, 9, 29));
        grupo.adicionarProduto(produto);
        return produtoRepository.save(produto);
    }
}
