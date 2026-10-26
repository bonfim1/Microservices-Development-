package br.com.seguranca.login.config;

import static org.springframework.security.config.Customizer.withDefaults;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String JSON_401 = "{\"status\":401,\"mensagem\":\"Autenticacao necessaria\"}";
    private static final String JSON_403 = "{\"status\":403,\"mensagem\":\"Acesso negado: role insuficiente\"}";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // API REST stateless: sem sessao e sem CSRF (nao ha cookie de sessao)
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // publicos
                .requestMatchers(HttpMethod.POST, "/login").permitAll()
                .requestMatchers(HttpMethod.PUT, "/trocarsenha/**").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/health/**", "/actuator/info", "/actuator/prometheus").permitAll()
                .requestMatchers("/error").permitAll()
                // protegidos por role
                .requestMatchers(HttpMethod.POST, "/").hasRole("MANUTENCAO")
                .requestMatchers(HttpMethod.GET, "/").hasRole("MANUTENCAO")
                .requestMatchers(HttpMethod.PUT, "/unlock/**").hasRole("ADMIN")
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .httpBasic(withDefaults())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(401);
                    res.setHeader("WWW-Authenticate", "Basic realm=\"login-service\"");
                    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    res.getWriter().write(JSON_401);
                })
                .accessDeniedHandler((req, res, e) -> {
                    res.setStatus(403);
                    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    res.getWriter().write(JSON_403);
                }));
        return http.build();
    }

    /** Usuarios operacionais e roles armazenados nas tabelas users/authorities. */
    @Bean
    public JdbcUserDetailsManager userDetailsService(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    /** {bcrypt} por padrao; aceita outros prefixos ({noop}, ...) */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
