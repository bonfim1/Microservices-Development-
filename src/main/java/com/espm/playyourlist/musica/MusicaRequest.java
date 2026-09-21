package com.espm.playyourlist.musica;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para criação/atualização de uma música, com as regras de
 * validação definidas na especificação da atividade.
 */
public record MusicaRequest(

        @NotBlank(message = "título é obrigatório e não pode ser vazio ou conter apenas espaços")
        String titulo,

        @NotBlank(message = "artista é obrigatório e não pode ser vazio ou conter apenas espaços")
        String artista,

        @Size(max = 150, message = "álbum deve ter no máximo 150 caracteres")
        String album,

        @NotNull(message = "duração é obrigatória")
        @Positive(message = "duração deve ser maior que zero")
        Integer duracao,

        @Size(max = 50, message = "gênero deve ter no máximo 50 caracteres")
        String genero
) {
}
