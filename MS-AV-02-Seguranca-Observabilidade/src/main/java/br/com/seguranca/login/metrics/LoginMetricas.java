package br.com.seguranca.login.metrics;

import org.springframework.stereotype.Component;

import br.com.seguranca.login.repository.UsuarioRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Indicadores expostos em /actuator/metrics e /actuator/prometheus:
 * <ul>
 *   <li>login_validos_total             - logins com usuario e senha validos</li>
 *   <li>login_invalidos_total{motivo}   - logins recusados (senha_incorreta, usuario_inexistente, usuario_bloqueado)</li>
 *   <li>usuarios_bloqueados             - total atual de usuarios bloqueados (lido da tabela login_attempts)</li>
 *   <li>usuarios_cadastrados            - total de usuarios da tabela usuarios</li>
 * </ul>
 */
@Component
public class LoginMetricas {

    public static final String SENHA_INCORRETA = "senha_incorreta";
    public static final String USUARIO_INEXISTENTE = "usuario_inexistente";
    public static final String USUARIO_BLOQUEADO = "usuario_bloqueado";

    private final Counter loginsValidos;
    private final Counter invalidoSenha;
    private final Counter invalidoInexistente;
    private final Counter invalidoBloqueado;
    private final Counter bloqueiosRealizados;

    public LoginMetricas(MeterRegistry registry, UsuarioRepository repo) {
        loginsValidos = Counter.builder("login.validos")
                .description("Quantidade de logins validos")
                .register(registry);

        invalidoSenha = invalido(registry, SENHA_INCORRETA);
        invalidoInexistente = invalido(registry, USUARIO_INEXISTENTE);
        invalidoBloqueado = invalido(registry, USUARIO_BLOQUEADO);

        bloqueiosRealizados = Counter.builder("usuarios.bloqueios")
                .description("Quantidade de eventos de bloqueio de usuario")
                .register(registry);

        Gauge.builder("usuarios.bloqueados", repo, UsuarioRepository::contarBloqueados)
                .description("Total de usuarios bloqueados no momento")
                .register(registry);

        Gauge.builder("usuarios.cadastrados", repo, UsuarioRepository::contarUsuarios)
                .description("Total de usuarios cadastrados")
                .register(registry);
    }

    private static Counter invalido(MeterRegistry registry, String motivo) {
        return Counter.builder("login.invalidos")
                .description("Quantidade de logins invalidos")
                .tag("motivo", motivo)
                .register(registry);
    }

    public void loginValido() {
        loginsValidos.increment();
    }

    public void loginInvalido(String motivo) {
        switch (motivo) {
            case USUARIO_INEXISTENTE -> invalidoInexistente.increment();
            case USUARIO_BLOQUEADO -> invalidoBloqueado.increment();
            default -> invalidoSenha.increment();
        }
    }

    public void usuarioBloqueado() {
        bloqueiosRealizados.increment();
    }
}
