package com.espm.playyourlist.playlist;

public record PlaylistResponse(
        Long id,
        String nome,
        String descricao
) {
    public static PlaylistResponse fromEntity(Playlist playlist) {
        return new PlaylistResponse(playlist.getId(), playlist.getNome(), playlist.getDescricao());
    }
}
