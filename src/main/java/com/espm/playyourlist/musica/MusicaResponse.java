package com.espm.playyourlist.musica;

public record MusicaResponse(
        Long id,
        String titulo,
        String artista,
        String album,
        Integer duracao,
        String genero
) {
    public static MusicaResponse fromEntity(Musica musica) {
        return new MusicaResponse(
                musica.getId(),
                musica.getTitulo(),
                musica.getArtista(),
                musica.getAlbum(),
                musica.getDuracao(),
                musica.getGenero()
        );
    }
}
