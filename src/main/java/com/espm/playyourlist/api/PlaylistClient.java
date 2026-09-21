package com.espm.playyourlist.api;

import com.espm.playyourlist.playlist.PlaylistResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Cliente OpenFeign para o microsserviço "playlists", usado pelo
 * microsserviço "api" para validar a existência de uma playlist e para
 * adicionar músicas a ela.
 */
@FeignClient(name = "playlist-client", url = "${app.base-url}")
public interface PlaylistClient {

    @GetMapping("/playlists/{playlistid}")
    PlaylistResponse buscarPorId(@PathVariable("playlistid") Long playlistId);

    @PostMapping("/playlists/{playlistid}/musicas/{musicaId}")
    void adicionarMusica(@PathVariable("playlistid") Long playlistId, @PathVariable("musicaId") Long musicaId);
}
