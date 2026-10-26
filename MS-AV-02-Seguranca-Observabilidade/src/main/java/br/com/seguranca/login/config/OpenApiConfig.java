package br.com.seguranca.login.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    public static final String BASIC_AUTH = "basicAuth";

    @Bean
    public OpenAPI loginServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Login Service API")
                        .version("1.0.0")
                        .description("""
                                MS-AV-02 - Seguranca e Observabilidade.

                                * Endpoints publicos: `POST /login`, `PUT /trocarsenha/{userid}`
                                * `POST /` e `GET /`: HTTP Basic com **ROLE_MANUTENCAO** (ex.: manutencao / manut123)
                                * `PUT /unlock/{userid}`: HTTP Basic com **ROLE_ADMIN** (ex.: admin / admin123)

                                Use o botao **Authorize** para informar as credenciais.""")
                        .contact(new Contact().name("Aluno - MS-AV-02")))
                .components(new Components().addSecuritySchemes(BASIC_AUTH,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")
                                .description("Usuarios do JdbcUserDetailsManager (tabelas users/authorities)")));
    }
}
