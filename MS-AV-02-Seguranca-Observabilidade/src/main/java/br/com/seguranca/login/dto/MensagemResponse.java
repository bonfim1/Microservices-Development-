package br.com.seguranca.login.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta padrao com status e mensagem")
public record MensagemResponse(int status, String mensagem, LocalDateTime timestamp) {

    public static MensagemResponse of(int status, String mensagem) {
        return new MensagemResponse(status, mensagem, LocalDateTime.now());
    }
}
