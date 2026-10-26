package br.com.seguranca.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro de um novo usuario")
public record NovoUsuarioRequest(
        @Schema(example = "carlos", maxLength = 100)
        @NotBlank @Size(max = 100) String username,
        @Schema(example = "carlos1", maxLength = 10)
        @NotBlank @Size(max = 10, message = "a senha deve ter no maximo 10 caracteres") String senha) {
}
