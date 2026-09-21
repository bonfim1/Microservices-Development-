package com.espm.playyourlist.musica;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/musicas")
public class MusicaController {

    private final MusicaService musicaService;

    public MusicaController(MusicaService musicaService) {
        this.musicaService = musicaService;
    }

    @PostMapping
    public ResponseEntity<MusicaResponse> cadastrar(@Valid @RequestBody MusicaRequest request) {
        MusicaResponse response = musicaService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<MusicaResponse> listarTodas() {
        return musicaService.listarTodas();
    }

    @GetMapping("/{id}")
    public MusicaResponse buscarPorId(@PathVariable Long id) {
        return musicaService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public MusicaResponse atualizar(@PathVariable Long id, @Valid @RequestBody MusicaRequest request) {
        return musicaService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        musicaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
