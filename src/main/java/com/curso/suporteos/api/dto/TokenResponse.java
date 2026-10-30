package com.curso.suporteos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record TokenResponse(
        @Schema(description = "JWT assinado usado no cabeçalho Authorization")
        String accessToken,
        @Schema(example = "Bearer")
        String tokenType,
        @Schema(description = "Duração do token em segundos", example = "1800")
        long expiresIn,
        Instant expiresAt) {
}
