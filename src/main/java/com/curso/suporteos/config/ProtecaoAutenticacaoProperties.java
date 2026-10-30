package com.curso.suporteos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.security.authentication-protection")
public record ProtecaoAutenticacaoProperties(
        int maxFailedAttempts,
        Duration failureWindow,
        Duration lockDuration) {

    public ProtecaoAutenticacaoProperties {
        if (maxFailedAttempts < 1) {
            throw new IllegalArgumentException("Máximo de falhas deve ser positivo");
        }
        if (failureWindow == null || failureWindow.isZero() || failureWindow.isNegative()) {
            throw new IllegalArgumentException("Janela de falhas deve ser positiva");
        }
        if (lockDuration == null || lockDuration.isZero() || lockDuration.isNegative()) {
            throw new IllegalArgumentException("Duração do bloqueio deve ser positiva");
        }
    }
}
