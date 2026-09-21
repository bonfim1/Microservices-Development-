package com.espm.playyourlist.reproducao;

import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para registrar a execução de uma playlist. O campo
 * dataHora é opcional: quando omitido, o servidor usa o instante atual
 * (LocalDateTime.now()).
 */
public record ReproducaoRequest(

        @NotNull(message = "playlistId é obrigatório")
        Long playlistId,

        java.time.LocalDateTime dataHora
) {
}
