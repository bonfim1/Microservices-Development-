package br.com.seguranca.login.exception;

import org.springframework.http.HttpStatus;

/** Excecao de negocio com o status HTTP que deve ser devolvido ao cliente. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
