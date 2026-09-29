package com.curso.suporteos.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Infraestrutura", description = "Verificação mínima de disponibilidade da aplicação")
@RestController
public class HealthController {

    @Operation(summary = "Verificar disponibilidade",
            description = "Confirma que a aplicação recebeu e processou uma requisição HTTP.")
    @ApiResponse(responseCode = "200", description = "Aplicação disponível")
    @GetMapping("/api/health")
    public String health() {
        return "OK";
    }
}
