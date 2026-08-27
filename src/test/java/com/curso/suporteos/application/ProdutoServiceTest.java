package com.curso.suporteos.application;

import com.curso.suporteos.domain.Fornecedor;
import com.curso.suporteos.domain.GrupoProduto;
import com.curso.suporteos.domain.Produto;
import com.curso.suporteos.repository.FornecedorRepository;
import com.curso.suporteos.repository.GrupoProdutoRepository;
import com.curso.suporteos.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProdutoServiceTest {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private GrupoProdutoRepository grupoRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Test
    void deveCadastrarProdutoComGrupoEFornecedor() {
        GrupoProduto grupo = grupoRepository.save(new GrupoProduto("Periféricos de teste"));
        Fornecedor fornecedor = fornecedorRepository.save(new Fornecedor(
                "Fornecedor de teste",
                "11111111000191"));

        Produto cadastrado = produtoService.cadastrar(
                novoProduto("TESTE-SERVICE-001"),
                grupo.getId(),
                fornecedor.getId());

        assertNotNull(cadastrado.getId());
        assertEquals(grupo.getId(), cadastrado.getGrupo().getId());
        assertEquals(fornecedor.getId(), cadastrado.getFornecedor().getId());
    }

    @Test
    void deveImpedirCodigoDeBarrasDuplicado() {
        GrupoProduto grupo = grupoRepository.save(new GrupoProduto("Grupo duplicidade"));
        produtoService.cadastrar(novoProduto("TESTE-DUPLICADO"), grupo.getId(), null);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> produtoService.cadastrar(
                        novoProduto("TESTE-DUPLICADO"),
                        grupo.getId(),
                        null));
    }

    @Test
    void deveInformarGrupoInexistenteSemSalvarProduto() {
        Produto produto = novoProduto("TESTE-SEM-GRUPO");

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.cadastrar(produto, Long.MAX_VALUE, null));
        assertEquals(false, produtoRepository.existsByCodigoBarras("TESTE-SEM-GRUPO"));
    }

    @Test
    void deveAtualizarSaldoPorDirtyChecking() {
        GrupoProduto grupo = grupoRepository.save(new GrupoProduto("Grupo dirty checking"));
        Produto produto = produtoService.cadastrar(
                novoProduto("TESTE-DIRTY-CHECKING"),
                grupo.getId(),
                null);

        produtoService.receberEstoque(produto.getId(), new BigDecimal("5.000"));

        Produto atualizado = produtoRepository.findById(produto.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("15.000").compareTo(atualizado.getSaldoEstoque()));
    }

    private Produto novoProduto(String codigo) {
        return new Produto(
                codigo,
                "Produto de teste",
                new BigDecimal("10.000"),
                new BigDecimal("25.90"),
                new BigDecimal("2.000"),
                LocalDate.of(2026, 8, 27));
    }
}
