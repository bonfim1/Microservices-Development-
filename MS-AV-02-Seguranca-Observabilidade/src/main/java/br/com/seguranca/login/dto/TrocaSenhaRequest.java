package br.com.seguranca.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Senha atual e nova senha")
public record TrocaSenhaRequest(
        @Schema(example = "maria123") @NotBlank String atual,
        @Schema(example = "nova123", maxLength = 10)
        @NotBlank @Size(max = 10, message = "a nova senha deve ter no maximo 10 caracteres") String nova) {
}
