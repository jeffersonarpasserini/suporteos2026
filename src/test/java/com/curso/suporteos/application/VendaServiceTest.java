package com.curso.suporteos.application;

import com.curso.suporteos.domain.*;
import com.curso.suporteos.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class VendaServiceTest {
    @Autowired VendaService vendaService;
    @Autowired PessoaRepository pessoaRepository;
    @Autowired ClienteRepository clienteRepository;
    @Autowired ColaboradorRepository colaboradorRepository;
    @Autowired GrupoProdutoRepository grupoRepository;
    @Autowired ProdutoRepository produtoRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    @AfterEach
    void limparDadosDoCenario() {
        jdbcTemplate.update("DELETE FROM item_venda");
        jdbcTemplate.update("DELETE FROM venda");
        jdbcTemplate.update("DELETE FROM cliente");
        jdbcTemplate.update("DELETE FROM colaborador");
        jdbcTemplate.update("DELETE FROM pessoa");
        jdbcTemplate.update("DELETE FROM produto WHERE codigo_barras LIKE 'VENDA-%'");
        jdbcTemplate.update("DELETE FROM grupo_produto WHERE nome LIKE 'Grupo VENDA-%'");
    }

    @Test
    void deveFinalizarVendaEBaixarEstoque() {
        Participantes participantes = criarParticipantes();
        Produto produto = criarProduto("VENDA-OK-" + System.nanoTime(), "5.000");
        Venda venda = vendaService.cadastrar(participantes.cliente().getId(), participantes.vendedor().getId());
        vendaService.adicionarItem(venda.getId(), produto.getId(), new BigDecimal("2.000"));

        Venda finalizada = vendaService.finalizar(venda.getId());

        assertEquals(StatusVenda.FINALIZADA, finalizada.getStatus());
        assertEquals(new BigDecimal("3.000"), produtoRepository.findById(produto.getId()).orElseThrow().getSaldoEstoque());
    }

    @Test
    void deveDesfazerTodasAsBaixasQuandoUmItemNaoTemEstoque() {
        Participantes participantes = criarParticipantes();
        Produto primeiro = criarProduto("VENDA-ROLLBACK-A-" + System.nanoTime(), "5.000");
        Produto segundo = criarProduto("VENDA-ROLLBACK-B-" + System.nanoTime(), "1.000");
        Venda venda = vendaService.cadastrar(participantes.cliente().getId(), participantes.vendedor().getId());
        vendaService.adicionarItem(venda.getId(), primeiro.getId(), new BigDecimal("2.000"));
        vendaService.adicionarItem(venda.getId(), segundo.getId(), new BigDecimal("2.000"));

        assertThrows(IllegalArgumentException.class, () -> vendaService.finalizar(venda.getId()));

        assertEquals(new BigDecimal("5.000"), produtoRepository.findById(primeiro.getId()).orElseThrow().getSaldoEstoque());
        assertEquals(StatusVenda.ABERTA, vendaService.buscarPorId(venda.getId()).getStatus());
    }

    @Test
    void deveImpedirVendaAcimaDoSaldoEmFinalizacoesConcorrentes() throws Exception {
        Participantes participantes = criarParticipantes();
        Produto produto = criarProduto("VENDA-CONCORRENTE-" + System.nanoTime(), "5.000");
        Venda primeira = criarVendaComItem(participantes, produto, "4.000");
        Venda segunda = criarVendaComItem(participantes, produto, "4.000");

        CountDownLatch inicioSimultaneo = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<ResultadoFinalizacao> primeiroResultado = executor.submit(
                    () -> finalizarAposSinal(primeira.getId(), inicioSimultaneo));
            Future<ResultadoFinalizacao> segundoResultado = executor.submit(
                    () -> finalizarAposSinal(segunda.getId(), inicioSimultaneo));

            inicioSimultaneo.countDown();

            List<ResultadoFinalizacao> resultados = List.of(
                    primeiroResultado.get(10, TimeUnit.SECONDS),
                    segundoResultado.get(10, TimeUnit.SECONDS));

            assertEquals(1, resultados.stream().filter(ResultadoFinalizacao::sucesso).count());
            assertEquals(1, resultados.stream().filter(resultado -> !resultado.sucesso()).count());
            assertTrue(resultados.stream()
                    .filter(resultado -> !resultado.sucesso())
                    .allMatch(resultado -> "Saldo de estoque insuficiente".equals(resultado.mensagem())));
        }

        assertEquals(new BigDecimal("1.000"),
                produtoRepository.findById(produto.getId()).orElseThrow().getSaldoEstoque());

        List<StatusVenda> estados = List.of(
                vendaService.buscarPorId(primeira.getId()).getStatus(),
                vendaService.buscarPorId(segunda.getId()).getStatus());
        assertEquals(1, estados.stream().filter(StatusVenda.FINALIZADA::equals).count());
        assertEquals(1, estados.stream().filter(StatusVenda.ABERTA::equals).count());
    }

    private Venda criarVendaComItem(
            Participantes participantes,
            Produto produto,
            String quantidade) {
        Venda venda = vendaService.cadastrar(
                participantes.cliente().getId(),
                participantes.vendedor().getId());
        vendaService.adicionarItem(
                venda.getId(),
                produto.getId(),
                new BigDecimal(quantidade));
        return venda;
    }

    private ResultadoFinalizacao finalizarAposSinal(
            Long vendaId,
            CountDownLatch inicioSimultaneo) throws InterruptedException {
        inicioSimultaneo.await();
        try {
            vendaService.finalizar(vendaId);
            return new ResultadoFinalizacao(true, null);
        } catch (IllegalArgumentException exception) {
            return new ResultadoFinalizacao(false, exception.getMessage());
        }
    }

    private Participantes criarParticipantes() {
        long semente = System.nanoTime();
        String sufixo = String.valueOf(semente);
        String cpfCliente = gerarCpf(semente);
        String cpfVendedor = gerarCpf(semente + 7919);
        Pessoa pc = pessoaRepository.save(new Pessoa("Cliente", "cliente" + sufixo + "@example.com", cpfCliente, LocalDate.now()));
        Pessoa pv = pessoaRepository.save(new Pessoa("Vendedor", "vendedor" + sufixo + "@example.com", cpfVendedor, LocalDate.now()));
        Cliente cliente = clienteRepository.save(new Cliente(pc, null));
        Colaborador vendedor = colaboradorRepository.save(new Colaborador(pv, "M-" + sufixo,
                FuncaoColaborador.VENDEDOR, LocalDate.now()));
        return new Participantes(cliente, vendedor);
    }

    private String gerarCpf(long semente) {
        String base = "%09d".formatted(Math.floorMod(semente, 1_000_000_000L));
        if (digitosRepetidos(base)) base = "123456789";
        int primeiro = digitoCpf(base, 10);
        return base + primeiro + digitoCpf(base + primeiro, 11);
    }

    private int digitoCpf(String base, int peso) {
        int soma = 0;
        for (char c : base.toCharArray()) soma += Character.getNumericValue(c) * peso--;
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private boolean digitosRepetidos(String valor) {
        for (int i = 1; i < valor.length(); i++)
            if (valor.charAt(i) != valor.charAt(0)) return false;
        return true;
    }

    private Produto criarProduto(String codigo, String saldo) {
        GrupoProduto grupo = grupoRepository.save(new GrupoProduto("Grupo " + codigo));
        Produto produto = new Produto(codigo, "Produto de teste", new BigDecimal(saldo),
                new BigDecimal("10.00"), LocalDate.now());
        grupo.adicionarProduto(produto);
        return produtoRepository.save(produto);
    }

    private record Participantes(Cliente cliente, Colaborador vendedor) { }

    private record ResultadoFinalizacao(boolean sucesso, String mensagem) { }
}
