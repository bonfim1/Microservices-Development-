package com.espm.playyourlist.playlist;

/**
 * Representa apenas o id de uma música dentro de uma playlist, sem trazer
 * nome, descrição etc. — conforme exigido pelo endpoint
 * GET /playlists/{playlistid}/musicas.
 */
public record MusicaIdResponse(Long musicaId) {
}
