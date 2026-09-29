package com.curso.suporteos.application;

import com.curso.suporteos.domain.GrupoProduto;
import com.curso.suporteos.domain.Produto;
import com.curso.suporteos.repository.FornecedorRepository;
import com.curso.suporteos.repository.GrupoProdutoRepository;
import com.curso.suporteos.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceUnitTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private GrupoProdutoRepository grupoRepository;

    @Mock
    private FornecedorRepository fornecedorRepository;

    @InjectMocks
    private ProdutoService service;

    @Test
    void deveAlterarDadosEGrupoDoProduto() {
        GrupoProduto grupoOriginal = new GrupoProduto("Grupo original");
        GrupoProduto novoGrupo = new GrupoProduto("Novo grupo");
        Produto produto = novoProduto();
        grupoOriginal.adicionarProduto(produto);
        when(produtoRepository.buscarPorIdComRelacionamentos(1L))
                .thenReturn(Optional.of(produto));
        when(grupoRepository.findById(2L)).thenReturn(Optional.of(novoGrupo));

        Produto alterado = service.alterar(
                1L,
                "Descrição alterada",
                new BigDecimal("75.90"),
                new BigDecimal("3.000"),
                2L,
                null);

        assertEquals("Descrição alterada", alterado.getDescricao());
        assertEquals(novoGrupo, alterado.getGrupo());
        assertEquals(0, grupoOriginal.getProdutos().size());
        assertEquals(1, novoGrupo.getProdutos().size());
    }

    @Test
    void naoDeveRetirarQuantidadeMaiorQueOSaldo() {
        Produto produto = novoProduto();
        when(produtoRepository.buscarPorIdComRelacionamentos(1L))
                .thenReturn(Optional.of(produto));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.retirarEstoque(1L, new BigDecimal("10.001")));
        assertEquals(0, new BigDecimal("10.000").compareTo(produto.getSaldoEstoque()));
    }

    @Test
    void deveRejeitarOrdenacaoNaoPermitida() {
        PageRequest pagina = PageRequest.of(
                0,
                20,
                Sort.by("fornecedor.razaoSocial"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.pesquisar(null, null, null, null, null, pagina));
    }

    private Produto novoProduto() {
        return new Produto(
                "UNIT-001",
                "Produto unitário",
                new BigDecimal("10.000"),
                new BigDecimal("49.90"),
                new BigDecimal("2.000"),
                LocalDate.of(2026, 9, 29));
    }
}
