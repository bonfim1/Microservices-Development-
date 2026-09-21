package com.espm.playyourlist.musica;

import com.espm.playyourlist.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MusicaService {

    private final MusicaRepository musicaRepository;

    public MusicaService(MusicaRepository musicaRepository) {
        this.musicaRepository = musicaRepository;
    }

    public MusicaResponse criar(MusicaRequest request) {
        Musica musica = new Musica(request.titulo(), request.artista(), request.album(),
                request.duracao(), request.genero());
        return MusicaResponse.fromEntity(musicaRepository.save(musica));
    }

    public List<MusicaResponse> listarTodas() {
        return musicaRepository.findAll().stream()
                .map(MusicaResponse::fromEntity)
                .toList();
    }

    public MusicaResponse buscarPorId(Long id) {
        return MusicaResponse.fromEntity(buscarEntidadePorId(id));
    }

    public Musica buscarEntidadePorId(Long id) {
        return musicaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Música com id " + id + " não encontrada"));
    }

    public MusicaResponse atualizar(Long id, MusicaRequest request) {
        Musica musica = buscarEntidadePorId(id);
        musica.setTitulo(request.titulo());
        musica.setArtista(request.artista());
        musica.setAlbum(request.album());
        musica.setDuracao(request.duracao());
        musica.setGenero(request.genero());
        return MusicaResponse.fromEntity(musicaRepository.save(musica));
    }

    public void excluir(Long id) {
        Musica musica = buscarEntidadePorId(id);
        musicaRepository.delete(musica);
    }
}
