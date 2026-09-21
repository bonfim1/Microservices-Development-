package com.espm.playyourlist.reproducao;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReproducaoService {

    private final ReproducaoRepository reproducaoRepository;

    public ReproducaoService(ReproducaoRepository reproducaoRepository) {
        this.reproducaoRepository = reproducaoRepository;
    }

    public ReproducaoResponse registrar(ReproducaoRequest request) {
        LocalDateTime dataHora = request.dataHora() != null ? request.dataHora() : LocalDateTime.now();
        Reproducao reproducao = new Reproducao(request.playlistId(), dataHora);
        return ReproducaoResponse.fromEntity(reproducaoRepository.save(reproducao));
    }

    public List<ReproducaoResponse> listarPorPlaylist(Long playlistId) {
        return reproducaoRepository.findByPlaylistId(playlistId).stream()
                .map(ReproducaoResponse::fromEntity)
                .toList();
    }

    public long totalPorPlaylist(Long playlistId) {
        return reproducaoRepository.countByPlaylistId(playlistId);
    }
}
