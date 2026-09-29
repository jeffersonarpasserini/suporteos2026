package com.curso.suporteos.application;

public class RecursoEmUsoException extends RuntimeException {

    public RecursoEmUsoException(String mensagem) {
        super(mensagem);
    }
}
