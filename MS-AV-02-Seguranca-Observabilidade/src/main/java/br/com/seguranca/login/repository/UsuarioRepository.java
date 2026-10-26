package br.com.seguranca.login.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import br.com.seguranca.login.dto.UsuarioResponse;

/** Acesso as tabelas usuarios e login_attempts. */
@Repository
public class UsuarioRepository {

    private final JdbcTemplate jdbc;

    public UsuarioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ------------------------- usuarios -------------------------

    public boolean existe(String username) {
        Integer qtd = jdbc.queryForObject(
                "SELECT COUNT(*) FROM usuarios WHERE username = ?", Integer.class, username);
        return qtd != null && qtd > 0;
    }

    public Optional<String> buscarSenha(String username) {
        return jdbc.query("SELECT senha FROM usuarios WHERE username = ?",
                (rs, i) -> rs.getString("senha"), username).stream().findFirst();
    }

    public void inserir(String username, String senha) {
        jdbc.update("INSERT INTO usuarios (username, senha) VALUES (?, ?)", username, senha);
        jdbc.update("INSERT INTO login_attempts (username) VALUES (?)", username);
    }

    public void atualizarSenha(String username, String novaSenha) {
        jdbc.update("UPDATE usuarios SET senha = ? WHERE username = ?", novaSenha, username);
    }

    public List<UsuarioResponse> listar() {
        return jdbc.query("""
                SELECT u.username,
                       COALESCE(a.locked, FALSE)        AS locked,
                       COALESCE(a.failed_attempts, 0)  AS failed_attempts,
                       a.last_failed_at,
                       a.locked_at
                  FROM usuarios u
                  LEFT JOIN login_attempts a ON a.username = u.username
                 ORDER BY u.username
                """,
                (rs, i) -> new UsuarioResponse(
                        rs.getString("username"),
                        rs.getBoolean("locked"),
                        rs.getInt("failed_attempts"),
                        toLocal(rs.getTimestamp("last_failed_at")),
                        toLocal(rs.getTimestamp("locked_at"))));
    }

    // ---------------------- login_attempts ----------------------

    /** Garante que exista a linha de controle (usuarios legados sem registro). */
    public void garantirControle(String username) {
        jdbc.update("MERGE INTO login_attempts (username) KEY (username) VALUES (?)", username);
    }

    public boolean estaBloqueado(String username) {
        return jdbc.query("SELECT locked FROM login_attempts WHERE username = ?",
                (rs, i) -> rs.getBoolean("locked"), username).stream().findFirst().orElse(false);
    }

    /** Incrementa a falha e bloqueia quando failed_attempts > maxFalhas. Retorna true se ESTA chamada bloqueou o usuario. */
    public boolean registrarFalha(String username, int maxFalhas) {
        garantirControle(username);
        jdbc.update("""
                UPDATE login_attempts
                   SET failed_attempts = failed_attempts + 1,
                       last_failed_at  = CURRENT_TIMESTAMP,
                       updated_at      = CURRENT_TIMESTAMP
                 WHERE username = ?
                """, username);
        int bloqueou = jdbc.update("""
                UPDATE login_attempts
                   SET locked = TRUE, locked_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
                 WHERE username = ? AND locked = FALSE AND failed_attempts > ?
                """, username, maxFalhas);
        return bloqueou > 0;
    }

    public void zerarFalhas(String username) {
        garantirControle(username);
        jdbc.update("""
                UPDATE login_attempts
                   SET failed_attempts = 0, updated_at = CURRENT_TIMESTAMP
                 WHERE username = ?
                """, username);
    }

    public void desbloquear(String username) {
        garantirControle(username);
        jdbc.update("""
                UPDATE login_attempts
                   SET locked = FALSE, failed_attempts = 0, locked_at = NULL, updated_at = CURRENT_TIMESTAMP
                 WHERE username = ?
                """, username);
    }

    public long contarBloqueados() {
        Long qtd = jdbc.queryForObject("SELECT COUNT(*) FROM login_attempts WHERE locked = TRUE", Long.class);
        return qtd == null ? 0 : qtd;
    }

    public long contarUsuarios() {
        Long qtd = jdbc.queryForObject("SELECT COUNT(*) FROM usuarios", Long.class);
        return qtd == null ? 0 : qtd;
    }

    private static LocalDateTime toLocal(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
