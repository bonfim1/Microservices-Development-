package com.espm.playyourlist.playlist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaylistMusicaRepository extends JpaRepository<PlaylistMusica, Long> {

    List<PlaylistMusica> findByPlaylistId(Long playlistId);

    Optional<PlaylistMusica> findByPlaylistIdAndMusicaId(Long playlistId, Long musicaId);

    void deleteByPlaylistId(Long playlistId);

    void deleteByPlaylistIdAndMusicaId(Long playlistId, Long musicaId);
}
