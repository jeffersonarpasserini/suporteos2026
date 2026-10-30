package com.curso.suporteos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.security.initial-admin")
public record AdministradorInicialProperties(
        boolean enabled,
        String nome,
        String email,
        String cpf,
        String login,
        String senha) {
}
