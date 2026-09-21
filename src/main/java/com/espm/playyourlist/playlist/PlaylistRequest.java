package com.espm.playyourlist.playlist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para criação/atualização de uma playlist, com as regras de
 * validação definidas na especificação da atividade.
 */
public record PlaylistRequest(

        @NotBlank(message = "nome é obrigatório e não pode ser vazio ou conter apenas espaços")
        String nome,

        @Size(max = 255, message = "descrição deve ter no máximo 255 caracteres")
        String descricao
) {
}
