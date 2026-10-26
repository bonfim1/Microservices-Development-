package br.com.seguranca.login;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureObservability // habilita /actuator/prometheus nos testes
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LoginServiceApplicationTests {

    @Autowired
    MockMvc mvc;

    private ResultActions login(String user, String senha) throws Exception {
        return mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + user + "\",\"senha\":\"" + senha + "\"}"));
    }

    @Test
    void loginValidoRetorna200() throws Exception {
        login("maria", "maria123").andExpect(status().isOk());
    }

    @Test
    void loginInvalidoRetorna401() throws Exception {
        login("maria", "errada").andExpect(status().isUnauthorized());
        login("naoexiste", "x").andExpect(status().isUnauthorized());
    }

    @Test
    void segundaFalhaBloqueiaEUnlockLibera() throws Exception {
        login("joao", "errada1").andExpect(status().isUnauthorized());
        login("joao", "errada2").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Usuario bloqueado"));
        // mesmo com a senha correta continua bloqueado
        login("joao", "joao123").andExpect(status().isUnauthorized());

        mvc.perform(get("/").with(httpBasic("manutencao", "manut123")))
                .andExpect(jsonPath("$[?(@.username=='joao')].bloqueado").value(hasItem(true)));

        // manutencao nao pode desbloquear
        mvc.perform(put("/unlock/joao").with(httpBasic("manutencao", "manut123")))
                .andExpect(status().isForbidden());
        mvc.perform(put("/unlock/joao").with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk());

        login("joao", "joao123").andExpect(status().isOk());
    }

    @Test
    void loginComSucessoZeraFalhas() throws Exception {
        login("ana", "errada").andExpect(status().isUnauthorized());
        login("ana", "ana123").andExpect(status().isOk());
        login("ana", "errada").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Usuario ou senha invalidos"));
    }

    @Test
    void trocarSenha() throws Exception {
        mvc.perform(put("/trocarsenha/maria").contentType(MediaType.APPLICATION_JSON)
                .content("{\"atual\":\"errada\",\"nova\":\"nova1\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/trocarsenha/maria").contentType(MediaType.APPLICATION_JSON)
                .content("{\"atual\":\"maria123\",\"nova\":\"senhamuitolonga\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/trocarsenha/maria").contentType(MediaType.APPLICATION_JSON)
                .content("{\"atual\":\"maria123\",\"nova\":\"nova1\"}"))
                .andExpect(status().isOk());
        mvc.perform(put("/trocarsenha/fulano").contentType(MediaType.APPLICATION_JSON)
                .content("{\"atual\":\"a\",\"nova\":\"b\"}"))
                .andExpect(status().isNotFound());
        login("maria", "nova1").andExpect(status().isOk());
    }

    @Test
    void cadastroEListagemExigemRoleManutencao() throws Exception {
        String body = "{\"username\":\"carlos\",\"senha\":\"carlos1\"}";
        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/").with(httpBasic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(post("/").with(httpBasic("manutencao", "manut123"))
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mvc.perform(post("/").with(httpBasic("manutencao", "manut123"))
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());

        mvc.perform(get("/")).andExpect(status().isUnauthorized());
        mvc.perform(get("/").with(httpBasic("admin", "admin123"))).andExpect(status().isForbidden());
        mvc.perform(get("/").with(httpBasic("manutencao", "manut123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].username").value(hasItem("carlos")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("senha"))));

        login("carlos", "carlos1").andExpect(status().isOk());
    }

    @Test
    void metricasESwaggerPublicos() throws Exception {
        login("maria", "maria123");
        login("maria", "x");
        login("maria", "y");
        mvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("login_validos_total")))
                .andExpect(content().string(containsString("login_invalidos_total")))
                .andExpect(content().string(containsString("usuarios_bloqueados")));
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(content().string(containsString("/trocarsenha/{userid}")));
    }
}
