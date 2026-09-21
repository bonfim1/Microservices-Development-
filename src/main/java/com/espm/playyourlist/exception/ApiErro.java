package com.espm.playyourlist.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Estrutura padrão de erro devolvida pela API.
 */
public record ApiErro(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        List<String> detalhes
) {
    public ApiErro(int status, String erro, String mensagem) {
        this(LocalDateTime.now(), status, erro, mensagem, List.of());
    }

    public ApiErro(int status, String erro, String mensagem, List<String> detalhes) {
        this(LocalDateTime.now(), status, erro, mensagem, detalhes);
    }
}
