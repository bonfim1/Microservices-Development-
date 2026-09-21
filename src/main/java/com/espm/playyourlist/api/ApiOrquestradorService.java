package com.espm.playyourlist.api;

import com.espm.playyourlist.musica.MusicaResponse;
import com.espm.playyourlist.playlist.PlaylistResponse;
import com.espm.playyourlist.reproducao.ReproducaoRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Microsserviço "api": orquestra chamadas aos demais serviços (musicas,
 * playlists e reproducao) via OpenFeign.
 */
@Service
public class ApiOrquestradorService {

    private final MusicaClient musicaClient;
    private final PlaylistClient playlistClient;
    private final ReproducaoClient reproducaoClient;

    public ApiOrquestradorService(MusicaClient musicaClient, PlaylistClient playlistClient,
                                   ReproducaoClient reproducaoClient) {
        this.musicaClient = musicaClient;
        this.playlistClient = playlistClient;
        this.reproducaoClient = reproducaoClient;
    }

    public MensagemResponse adicionarMusicaAPlaylist(Long playlistId, Long musicaId) {
        // valida os dois recursos via Open Feign antes de prosseguir
        MusicaResponse musica = musicaClient.buscarPorId(musicaId);
        PlaylistResponse playlist = playlistClient.buscarPorId(playlistId);

        playlistClient.adicionarMusica(playlistId, musicaId);

        String mensagem = "Música " + musica.titulo() + " adicionada com sucesso à playlist " + playlist.nome();
        return new MensagemResponse(mensagem);
    }

    public MensagemResponse executarPlaylist(Long playlistId) {
        PlaylistResponse playlist = playlistClient.buscarPorId(playlistId);

        reproducaoClient.registrar(new ReproducaoRequest(playlistId, LocalDateTime.now()));

        String mensagem = "Playlist " + playlist.nome() + " executada com sucesso";
        return new MensagemResponse(mensagem);
    }
}
