package com.curso.suporteos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.security.refresh-token")
public record RefreshTokenProperties(
        Duration idleTimeout,
        Duration absoluteExpiration) {

    public RefreshTokenProperties {
        if (idleTimeout == null || idleTimeout.isNegative() || idleTimeout.isZero()) {
            throw new IllegalArgumentException("O limite de inatividade deve ser positivo");
        }
        if (absoluteExpiration == null
                || absoluteExpiration.isNegative()
                || absoluteExpiration.isZero()) {
            throw new IllegalArgumentException("A validade absoluta deve ser positiva");
        }
        if (absoluteExpiration.compareTo(idleTimeout) <= 0) {
            throw new IllegalArgumentException(
                    "A validade absoluta deve ser maior que o limite de inatividade");
        }
    }
}
