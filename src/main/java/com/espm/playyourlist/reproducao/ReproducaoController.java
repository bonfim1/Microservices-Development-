package com.espm.playyourlist.reproducao;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reproducao")
public class ReproducaoController {

    private final ReproducaoService reproducaoService;

    public ReproducaoController(ReproducaoService reproducaoService) {
        this.reproducaoService = reproducaoService;
    }

    @PostMapping
    public ResponseEntity<ReproducaoResponse> registrar(@Valid @RequestBody ReproducaoRequest request) {
        ReproducaoResponse response = reproducaoService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{playlistid}")
    public List<ReproducaoResponse> listarPorPlaylist(@PathVariable("playlistid") Long playlistId) {
        return reproducaoService.listarPorPlaylist(playlistId);
    }

    @GetMapping("/total/{playlistid}")
    public long totalPorPlaylist(@PathVariable("playlistid") Long playlistId) {
        return reproducaoService.totalPorPlaylist(playlistId);
    }
}
