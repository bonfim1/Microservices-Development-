package com.espm.playyourlist.api;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final ApiOrquestradorService apiOrquestradorService;

    public ApiController(ApiOrquestradorService apiOrquestradorService) {
        this.apiOrquestradorService = apiOrquestradorService;
    }

    @PostMapping("/adicionar/{playlistId}/musicas/{musicaId}")
    public MensagemResponse adicionar(@PathVariable Long playlistId, @PathVariable Long musicaId) {
        return apiOrquestradorService.adicionarMusicaAPlaylist(playlistId, musicaId);
    }

    @PutMapping("/executar/{playlistId}")
    public MensagemResponse executar(@PathVariable Long playlistId) {
        return apiOrquestradorService.executarPlaylist(playlistId);
    }
}
