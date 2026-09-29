package com.curso.suporteos.api;

import com.curso.suporteos.domain.GrupoProduto;
import com.curso.suporteos.domain.Produto;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GrupoProdutoApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GrupoProdutoRepository repository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Test
    void deveAlterarEInativarGrupo() throws Exception {
        GrupoProduto grupo = repository.save(new GrupoProduto("Grupo original"));

        mockMvc.perform(put("/api/grupos-produtos/{id}", grupo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Grupo alterado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Grupo alterado"));

        mockMvc.perform(put("/api/grupos-produtos/{id}/status", grupo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INATIVO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INATIVO"));
    }

    @Test
    void deveBuscarGrupoPorIdParaPreencherTelaDeAlteracao() throws Exception {
        GrupoProduto grupo = repository.save(new GrupoProduto("Grupo para alteração"));

        mockMvc.perform(get("/api/grupos-produtos/{id}", grupo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(grupo.getId()))
                .andExpect(jsonPath("$.nome").value("Grupo para alteração"))
                .andExpect(jsonPath("$.status").value("ATIVO"));
    }

    @Test
    void devePesquisarGruposComPaginacao() throws Exception {
        repository.save(new GrupoProduto("Material escolar"));
        repository.save(new GrupoProduto("Equipamentos"));

        mockMvc.perform(get("/api/grupos-produtos")
                        .param("nome", "material")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo.length()").value(1))
                .andExpect(jsonPath("$.conteudo[0].nome").value("Material escolar"))
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanho").value(5));
    }

    @Test
    void deveExcluirGrupoSemProdutos() throws Exception {
        GrupoProduto grupo = repository.save(new GrupoProduto("Grupo descartável"));

        mockMvc.perform(delete("/api/grupos-produtos/{id}", grupo.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void naoDeveExcluirGrupoQuePossuiProdutos() throws Exception {
        GrupoProduto grupo = repository.save(new GrupoProduto("Grupo em uso"));
        Produto produto = new Produto(
                "GRUPO-EM-USO-001",
                "Produto relacionado",
                BigDecimal.ONE,
                BigDecimal.TEN,
                LocalDate.of(2026, 9, 29));
        grupo.adicionarProduto(produto);
        produtoRepository.saveAndFlush(produto);

        mockMvc.perform(delete("/api/grupos-produtos/{id}", grupo.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Grupo de produto não pode ser excluído porque possui produtos"));
    }
}
