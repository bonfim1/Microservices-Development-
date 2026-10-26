package br.com.seguranca.login.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Usuario cadastrado (a senha nunca e retornada)")
public record UsuarioResponse(
        String username,
        boolean bloqueado,
        int tentativasFalhas,
        LocalDateTime ultimaFalhaEm,
        LocalDateTime bloqueadoEm) {
}
