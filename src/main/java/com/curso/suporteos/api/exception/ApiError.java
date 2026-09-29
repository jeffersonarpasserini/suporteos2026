package com.curso.suporteos.api.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Representação padronizada de um erro da API")
public record ApiError(
        @Schema(description = "Instante em que o erro foi produzido", example = "2026-09-29T18:30:00Z")
        Instant timestamp,
        @Schema(description = "Código HTTP numérico", example = "400")
        int status,
        @Schema(description = "Nome padronizado do status HTTP", example = "Bad Request")
        String error,
        @Schema(description = "Mensagem segura para o consumidor", example = "Um ou mais campos são inválidos")
        String message,
        @Schema(description = "Caminho que recebeu a requisição", example = "/api/produtos")
        String path,
        @Schema(description = "Erros separados por campo; vazio quando a falha não pertence a um campo")
        Map<String, String> fields) {
}
