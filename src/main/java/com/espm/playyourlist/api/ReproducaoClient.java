package com.espm.playyourlist.api;

import com.espm.playyourlist.reproducao.ReproducaoRequest;
import com.espm.playyourlist.reproducao.ReproducaoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Cliente OpenFeign para o microsserviço "reproducao", usado pelo
 * microsserviço "api" para registrar a execução de uma playlist
 * (equivalente ao endpoint "/statistic" citado no enunciado).
 */
@FeignClient(name = "reproducao-client", url = "${app.base-url}")
public interface ReproducaoClient {

    @PostMapping("/reproducao")
    ReproducaoResponse registrar(@RequestBody ReproducaoRequest request);
}
