package br.com.seguranca.login.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.seguranca.login.dto.UsuarioResponse;
import br.com.seguranca.login.exception.ApiException;
import br.com.seguranca.login.metrics.LoginMetricas;
import br.com.seguranca.login.repository.UsuarioRepository;

@Service
public class LoginService {

    private static final Logger log = LoggerFactory.getLogger(LoginService.class);

    public enum Resultado { SUCESSO, CREDENCIAIS_INVALIDAS, BLOQUEADO }

    private final UsuarioRepository repo;
    private final LoginMetricas metricas;
    private final int maxFalhas;

    public LoginService(UsuarioRepository repo, LoginMetricas metricas,
                        @Value("${app.seguranca.max-falhas:1}") int maxFalhas) {
        this.repo = repo;
        this.metricas = metricas;
        this.maxFalhas = maxFalhas;
    }

    /** Valida usuario e senha contra a tabela usuarios e controla bloqueio em login_attempts. */
    @Transactional
    public Resultado login(String username, String senha) {
        Optional<String> senhaBanco = repo.buscarSenha(username);

        if (senhaBanco.isEmpty()) {
            metricas.loginInvalido(LoginMetricas.USUARIO_INEXISTENTE);
            log.info("Login invalido: usuario inexistente [{}]", username);
            return Resultado.CREDENCIAIS_INVALIDAS;
        }

        if (repo.estaBloqueado(username)) {
            metricas.loginInvalido(LoginMetricas.USUARIO_BLOQUEADO);
            log.info("Login invalido: usuario bloqueado [{}]", username);
            return Resultado.BLOQUEADO;
        }

        if (senhasIguais(senhaBanco.get(), senha)) {
            repo.zerarFalhas(username);
            metricas.loginValido();
            log.info("Login valido [{}]", username);
            return Resultado.SUCESSO;
        }

        metricas.loginInvalido(LoginMetricas.SENHA_INCORRETA);
        boolean bloqueou = repo.registrarFalha(username, maxFalhas);
        if (bloqueou) {
            metricas.usuarioBloqueado();
            log.warn("Usuario bloqueado por excesso de falhas [{}]", username);
            return Resultado.BLOQUEADO;
        }
        log.info("Login invalido: senha incorreta [{}]", username);
        return Resultado.CREDENCIAIS_INVALIDAS;
    }

    /** Troca a senha conferindo a senha atual com a base de dados. */
    @Transactional(noRollbackFor = ApiException.class) // mantem a falha registrada mesmo devolvendo 401
    public void trocarSenha(String username, String atual, String nova) {
        Optional<String> senhaBanco = repo.buscarSenha(username);
        if (senhaBanco.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Usuario nao encontrado");
        }
        if (repo.estaBloqueado(username)) {
            throw new ApiException(HttpStatus.LOCKED, "Usuario bloqueado. Solicite o desbloqueio a um administrador");
        }
        if (!senhasIguais(senhaBanco.get(), atual)) {
            // Senha atual errada conta como tentativa falha (evita forca bruta por este endpoint publico)
            if (repo.registrarFalha(username, maxFalhas)) {
                metricas.usuarioBloqueado();
            }
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Senha atual invalida");
        }
        if (atual.equals(nova)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A nova senha deve ser diferente da atual");
        }
        repo.atualizarSenha(username, nova);
        repo.zerarFalhas(username);
        log.info("Senha alterada [{}]", username);
    }

    @Transactional
    public void cadastrar(String username, String senha) {
        if (repo.existe(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "Usuario ja cadastrado");
        }
        repo.inserir(username, senha);
        log.info("Usuario cadastrado [{}]", username);
    }

    @Transactional
    public void desbloquear(String username) {
        if (!repo.existe(username)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Usuario nao encontrado");
        }
        repo.desbloquear(username);
        log.info("Usuario desbloqueado [{}]", username);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return repo.listar();
    }

    /** Comparacao em tempo constante para nao vazar informacao por timing. */
    private static boolean senhasIguais(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
