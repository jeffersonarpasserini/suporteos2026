package com.curso.suporteos.application;

import java.time.Instant;

public record TokenAcesso(
        String valor,
        Instant expiraEm,
        long duracaoSegundos) {
}
