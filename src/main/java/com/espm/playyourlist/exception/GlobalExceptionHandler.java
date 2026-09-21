package com.espm.playyourlist.exception;

import feign.FeignException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Tratamento centralizado de exceções para todos os controllers da aplicação
 * (musicas, playlists, reproducao e api).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiErro> handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        ApiErro erro = new ApiErro(HttpStatus.NOT_FOUND.value(), "Recurso não encontrado", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErro> handleValidacao(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        ApiErro erro = new ApiErro(HttpStatus.BAD_REQUEST.value(), "Erro de validação",
                "Um ou mais campos são inválidos", detalhes);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErro> handleIntegridade(DataIntegrityViolationException ex) {
        ApiErro erro = new ApiErro(HttpStatus.BAD_REQUEST.value(), "Violação de integridade",
                "A operação viola uma restrição do banco de dados (ex.: música ou playlist inexistente).");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    // Erros vindos das chamadas OpenFeign feitas pelo microsserviço "api"
    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<ApiErro> handleFeignNotFound(FeignException.NotFound ex) {
        ApiErro erro = new ApiErro(HttpStatus.NOT_FOUND.value(), "Recurso não encontrado",
                "Recurso referenciado não foi encontrado no serviço correspondente.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiErro> handleFeign(FeignException ex) {
        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) {
            status = HttpStatus.BAD_GATEWAY;
        }
        ApiErro erro = new ApiErro(status.value(), "Erro na comunicação entre serviços",
                "Falha ao chamar um serviço dependente: " + ex.getMessage());
        return ResponseEntity.status(status).body(erro);
    }
}
