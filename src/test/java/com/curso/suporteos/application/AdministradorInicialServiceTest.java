package com.curso.suporteos.application;

import com.curso.suporteos.config.AdministradorInicialProperties;
import com.curso.suporteos.domain.ContaAcesso;
import com.curso.suporteos.domain.PerfilAcesso;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdministradorInicialServiceTest {

    @Autowired
    private AdministradorInicialService service;

    @Test
    void deveCriarAdministradorInicialUmaUnicaVez() {
        AdministradorInicialProperties properties =
                new AdministradorInicialProperties(
                        true,
                        "Administradora Inicial",
                        "administradora.inicial@example.com",
                        "52998224725",
                        "administradora.inicial",
                        "senha-inicial-segura-2026");

        Optional<ContaAcesso> criada = service.criarSeNecessario(properties);

        assertTrue(criada.isPresent());
        assertTrue(criada.orElseThrow().possuiPerfil(PerfilAcesso.OPERADOR));
        assertTrue(criada.orElseThrow().possuiPerfil(PerfilAcesso.ADMINISTRADOR));
        assertTrue(service.criarSeNecessario(properties).isEmpty());
    }
}
