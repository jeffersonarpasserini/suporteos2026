package com.curso.suporteos.api;

import com.jayway.jsonpath.JsonPath;
import com.curso.suporteos.domain.ContaAcesso;
import com.curso.suporteos.domain.PerfilAcesso;
import com.curso.suporteos.domain.Pessoa;
import com.curso.suporteos.domain.StatusContaAcesso;
import com.curso.suporteos.repository.ContaAcessoRepository;
import com.curso.suporteos.repository.PessoaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ContaAcessoSecurityTest {

    private static final String SENHA = "uma-senha-didatica-2026";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private ContaAcessoRepository contaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Test
    void devePermitirHealthSemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    void deveNegarRotaProtegidaSemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/produtos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveCadastrarContaArmazenandoHashSemExporSenha() throws Exception {
        ContaAcesso administrador = salvarConta(
                "administrador.cadastro",
                SENHA,
                PerfilAcesso.ADMINISTRADOR);
        String tokenAdministrador = autenticar(administrador.getLogin(), SENHA);
        Pessoa pessoa = novaPessoa("cadastro");

        mockMvc.perform(post("/api/contas-acesso")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + tokenAdministrador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pessoaId": %d,
                                  "login": "ALUNA.2026",
                                  "senha": "%s"
                                }
                                """.formatted(pessoa.getId(), SENHA)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login").value("aluna.2026"))
                .andExpect(jsonPath("$.status").value("ATIVA"))
                .andExpect(jsonPath("$.perfis[0]").value("OPERADOR"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());

        ContaAcesso conta = contaRepository.findByLoginIgnoreCase("aluna.2026").orElseThrow();
        assertNotEquals(SENHA, conta.getSenhaHash());
        assertTrue(conta.getSenhaHash().startsWith("{bcrypt}"));
        assertTrue(passwordEncoder.matches(SENHA, conta.getSenhaHash()));
    }

    @Test
    void deveDiferenciarNaoAutenticadoDeOperadorSemPermissao() throws Exception {
        Pessoa pessoa = novaPessoa("sem-permissao");
        String corpo = """
                {
                  "pessoaId": %d,
                  "login": "nova.conta",
                  "senha": "%s"
                }
                """.formatted(pessoa.getId(), SENHA);

        mockMvc.perform(post("/api/contas-acesso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isUnauthorized());

        salvarConta("operador.limitado", SENHA);
        String tokenOperador = autenticar("operador.limitado", SENHA);
        mockMvc.perform(post("/api/contas-acesso")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + tokenOperador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorDeveAlterarPerfisEBloquearOutraConta() throws Exception {
        ContaAcesso administrador = salvarConta(
                "administrador.perfis",
                SENHA,
                PerfilAcesso.ADMINISTRADOR);
        ContaAcesso operador = salvarConta("operador.gerenciado", SENHA);
        String tokenAdministrador = autenticar(administrador.getLogin(), SENHA);

        mockMvc.perform(put("/api/contas-acesso/{id}/perfis", operador.getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + tokenAdministrador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"perfis":["OPERADOR","ADMINISTRADOR"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfis").isArray())
                .andExpect(jsonPath("$.perfis.length()").value(2));

        mockMvc.perform(put("/api/contas-acesso/{id}/status", operador.getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + tokenAdministrador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"BLOQUEADA"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOQUEADA"));

        ContaAcesso contaAtualizada = contaRepository.findById(operador.getId())
                .orElseThrow();
        assertEquals(StatusContaAcesso.BLOQUEADA, contaAtualizada.getStatus());
    }

    @Test
    void deveImpedirAdministradorDeBloquearAPropriaConta() throws Exception {
        ContaAcesso administrador = salvarConta(
                "administrador.protegido",
                SENHA,
                PerfilAcesso.ADMINISTRADOR);
        String tokenAdministrador = autenticar(administrador.getLogin(), SENHA);

        mockMvc.perform(put("/api/contas-acesso/{id}/status", administrador.getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + tokenAdministrador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"BLOQUEADA"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "O administrador não pode bloquear a própria conta"));
    }

    @Test
    void deveEmitirJwtEConsultarAPropriaContaComBearerToken() throws Exception {
        salvarConta("operadora.2026", SENHA);

        mockMvc.perform(get("/api/contas-acesso/me")
                        .with(httpBasic("operadora.2026", SENHA)))
                .andExpect(status().isUnauthorized());

        String token = autenticar("operadora.2026", SENHA);
        Jwt jwt = jwtDecoder.decode(token);

        assertEquals("https://suporteos2026.local", jwt.getIssuer().toString());
        assertEquals("operadora.2026", jwt.getSubject());
        assertTrue(jwt.getId() != null && !jwt.getId().isBlank());
        assertTrue(jwt.getClaimAsStringList("perfis").contains("OPERADOR"));
        assertTrue(jwt.getExpiresAt().isAfter(jwt.getIssuedAt()));

        mockMvc.perform(get("/api/contas-acesso/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("operadora.2026"))
                .andExpect(jsonPath("$.nomePessoa", containsString("Pessoa")));

        mockMvc.perform(post("/api/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"operadora.2026","senha":"senha-incorreta"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Login ou senha inválidos"));
    }

    @Test
    void deveImpedirAutenticacaoDeContaBloqueada() throws Exception {
        ContaAcesso conta = salvarConta("conta.bloqueada", SENHA);
        conta.bloquear();
        contaRepository.saveAndFlush(conta);

        mockMvc.perform(post("/api/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"conta.bloqueada","senha":"%s"}
                                """.formatted(SENHA)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Login ou senha inválidos"));
    }

    @Test
    void deveRejeitarBearerTokenInvalido() throws Exception {
        mockMvc.perform(get("/api/produtos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token.invalido.aqui"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRejeitarJwtExpiradoMesmoComAssinaturaValida() throws Exception {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("https://suporteos2026.local")
                .subject("operadora.expirada")
                .issuedAt(agora.minusSeconds(3600))
                .expiresAt(agora.minusSeconds(60))
                .claim("perfis", List.of("OPERADOR"))
                .build();
        String tokenExpirado = jwtEncoder
                .encode(JwtEncoderParameters.from(claims))
                .getTokenValue();

        mockMvc.perform(get("/api/produtos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenExpirado))
                .andExpect(status().isUnauthorized());
    }

    private String autenticar(String login, String senha) throws Exception {
        String resposta = mockMvc.perform(post("/api/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"%s","senha":"%s"}
                                """.formatted(login, senha)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(1800))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(resposta, "$.accessToken");
    }

    private ContaAcesso salvarConta(String login, String senha) {
        return salvarConta(login, senha, PerfilAcesso.OPERADOR);
    }

    private ContaAcesso salvarConta(
            String login,
            String senha,
            PerfilAcesso... perfis) {
        Pessoa pessoa = novaPessoa(login.replace('.', '-'));
        ContaAcesso conta = new ContaAcesso(
                pessoa,
                login,
                passwordEncoder.encode(senha));
        for (PerfilAcesso perfil : perfis) {
            conta.concederPerfil(perfil);
        }
        return contaRepository.saveAndFlush(conta);
    }

    private Pessoa novaPessoa(String identificador) {
        long semente = Math.abs(System.nanoTime());
        Pessoa pessoa = new Pessoa(
                "Pessoa " + identificador,
                identificador + "." + semente + "@example.com",
                gerarCpf(semente),
                LocalDate.of(2026, 10, 29));
        return pessoaRepository.saveAndFlush(pessoa);
    }

    private String gerarCpf(long semente) {
        String base = "%09d".formatted(semente % 1_000_000_000L);
        if (base.chars().distinct().count() == 1) {
            base = "123456789";
        }
        int primeiro = digitoCpf(base, 10);
        int segundo = digitoCpf(base + primeiro, 11);
        return base + primeiro + segundo;
    }

    private int digitoCpf(String numeros, int pesoInicial) {
        int soma = 0;
        for (int indice = 0; indice < numeros.length(); indice++) {
            soma += Character.getNumericValue(numeros.charAt(indice)) * (pesoInicial - indice);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
