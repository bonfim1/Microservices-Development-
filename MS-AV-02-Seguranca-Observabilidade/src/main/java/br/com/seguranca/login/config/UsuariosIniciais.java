package br.com.seguranca.login.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Component;

/** Cria, via JdbcUserDetailsManager, os usuarios com ROLE_ADMIN e ROLE_MANUTENCAO. */
@Component
public class UsuariosIniciais implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UsuariosIniciais.class);

    private final JdbcUserDetailsManager manager;
    private final PasswordEncoder encoder;

    @Value("${app.seguranca.admin.username}")
    private String adminUser;
    @Value("${app.seguranca.admin.password}")
    private String adminPass;
    @Value("${app.seguranca.manutencao.username}")
    private String manutUser;
    @Value("${app.seguranca.manutencao.password}")
    private String manutPass;

    public UsuariosIniciais(JdbcUserDetailsManager manager, PasswordEncoder encoder) {
        this.manager = manager;
        this.encoder = encoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        criar(adminUser, adminPass, "ADMIN");
        criar(manutUser, manutPass, "MANUTENCAO");
    }

    private void criar(String username, String senha, String role) {
        if (manager.userExists(username)) {
            return;
        }
        manager.createUser(User.withUsername(username)
                .password(encoder.encode(senha))
                .roles(role) // grava ROLE_<role> na tabela authorities
                .build());
        log.info("Usuario operacional criado: {} (ROLE_{})", username, role);
    }
}
