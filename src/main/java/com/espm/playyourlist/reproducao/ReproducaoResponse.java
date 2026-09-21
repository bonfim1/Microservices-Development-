package com.espm.playyourlist.reproducao;

import java.time.LocalDateTime;

public record ReproducaoResponse(
        Long id,
        Long playlistId,
        LocalDateTime dataHora
) {
    public static ReproducaoResponse fromEntity(Reproducao reproducao) {
        return new ReproducaoResponse(reproducao.getId(), reproducao.getPlaylistId(), reproducao.getDataHora());
    }
}
