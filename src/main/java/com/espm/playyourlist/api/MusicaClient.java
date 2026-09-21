package com.espm.playyourlist.api;

import com.espm.playyourlist.musica.MusicaResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente OpenFeign para o microsserviço "musicas", usado pelo
 * microsserviço "api" para validar a existência de uma música.
 */
@FeignClient(name = "musica-client", url = "${app.base-url}")
public interface MusicaClient {

    @GetMapping("/musicas/{id}")
    MusicaResponse buscarPorId(@PathVariable("id") Long id);
}
