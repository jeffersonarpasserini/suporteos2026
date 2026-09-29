package com.curso.suporteos.application;

import com.curso.suporteos.domain.GrupoProduto;
import com.curso.suporteos.repository.GrupoProdutoRepository;
import com.curso.suporteos.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrupoProdutoServiceUnitTest {

    @Mock
    private GrupoProdutoRepository repository;

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private GrupoProdutoService service;

    @Test
    void deveNormalizarNomeAoAlterar() {
        GrupoProduto grupo = new GrupoProduto("Nome original");
        when(repository.existsByNomeIgnoreCaseAndIdNot("Nome alterado", 1L))
                .thenReturn(false);
        when(repository.findById(1L)).thenReturn(Optional.of(grupo));

        GrupoProduto alterado = service.alterar(1L, "  Nome alterado  ");

        assertEquals("Nome alterado", alterado.getNome());
        verify(repository).existsByNomeIgnoreCaseAndIdNot("Nome alterado", 1L);
    }

    @Test
    void naoDeveExcluirGrupoQuePossuiProdutos() {
        GrupoProduto grupo = new GrupoProduto("Grupo em uso");
        when(repository.findById(1L)).thenReturn(Optional.of(grupo));
        when(produtoRepository.existsByGrupoId(1L)).thenReturn(true);

        assertThrows(RecursoEmUsoException.class, () -> service.excluir(1L));
    }

    @Test
    void deveRejeitarOrdenacaoNaoPermitida() {
        PageRequest pagina = PageRequest.of(0, 20).withSort(
                org.springframework.data.domain.Sort.by("produtos.descricao"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.pesquisar(null, null, pagina));
    }
}
