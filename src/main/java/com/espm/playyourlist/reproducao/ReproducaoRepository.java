package com.espm.playyourlist.reproducao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReproducaoRepository extends JpaRepository<Reproducao, Long> {

    List<Reproducao> findByPlaylistId(Long playlistId);

    long countByPlaylistId(Long playlistId);
}
