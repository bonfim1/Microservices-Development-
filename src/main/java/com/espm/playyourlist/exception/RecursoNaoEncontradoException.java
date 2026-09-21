package com.espm.playyourlist.exception;

/**
 * Lançada quando um recurso (música, playlist, associação, etc.) não é encontrado.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
