package com.espm.playyourlist.playlist;

import com.espm.playyourlist.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistMusicaRepository playlistMusicaRepository;

    public PlaylistService(PlaylistRepository playlistRepository,
                            PlaylistMusicaRepository playlistMusicaRepository) {
        this.playlistRepository = playlistRepository;
        this.playlistMusicaRepository = playlistMusicaRepository;
    }

    public PlaylistResponse criar(PlaylistRequest request) {
        Playlist playlist = new Playlist(request.nome(), request.descricao());
        return PlaylistResponse.fromEntity(playlistRepository.save(playlist));
    }

    public List<PlaylistResponse> listarTodas() {
        return playlistRepository.findAll().stream()
                .map(PlaylistResponse::fromEntity)
                .toList();
    }

    public PlaylistResponse buscarPorId(Long playlistId) {
        return PlaylistResponse.fromEntity(buscarEntidadePorId(playlistId));
    }

    public Playlist buscarEntidadePorId(Long playlistId) {
        return playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Playlist com id " + playlistId + " não encontrada"));
    }

    public PlaylistResponse atualizar(Long playlistId, PlaylistRequest request) {
        Playlist playlist = buscarEntidadePorId(playlistId);
        playlist.setNome(request.nome());
        playlist.setDescricao(request.descricao());
        return PlaylistResponse.fromEntity(playlistRepository.save(playlist));
    }

    @Transactional
    public void excluir(Long playlistId) {
        Playlist playlist = buscarEntidadePorId(playlistId);
        playlistMusicaRepository.deleteByPlaylistId(playlistId);
        playlistRepository.delete(playlist);
    }

    public void adicionarMusica(Long playlistId, Long musicaId) {
        // Garante que a playlist existe; a existência da música é garantida
        // pela FK da tabela playlist_musicas (violação vira 400, tratada no
        // GlobalExceptionHandler) e/ou validada previamente pelo microsserviço "api".
        buscarEntidadePorId(playlistId);
        playlistMusicaRepository.findByPlaylistIdAndMusicaId(playlistId, musicaId)
                .orElseGet(() -> playlistMusicaRepository.save(new PlaylistMusica(playlistId, musicaId)));
    }

    @Transactional
    public void removerMusica(Long playlistId, Long musicaId) {
        buscarEntidadePorId(playlistId);
        playlistMusicaRepository.deleteByPlaylistIdAndMusicaId(playlistId, musicaId);
    }

    public List<MusicaIdResponse> listarMusicasDaPlaylist(Long playlistId) {
        buscarEntidadePorId(playlistId);
        return playlistMusicaRepository.findByPlaylistId(playlistId).stream()
                .map(pm -> new MusicaIdResponse(pm.getMusicaId()))
                .toList();
    }
}
