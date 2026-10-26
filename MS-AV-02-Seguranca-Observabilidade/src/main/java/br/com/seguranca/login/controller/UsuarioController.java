package br.com.seguranca.login.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.seguranca.login.config.OpenApiConfig;
import br.com.seguranca.login.dto.LoginRequest;
import br.com.seguranca.login.dto.MensagemResponse;
import br.com.seguranca.login.dto.NovoUsuarioRequest;
import br.com.seguranca.login.dto.TrocaSenhaRequest;
import br.com.seguranca.login.dto.UsuarioResponse;
import br.com.seguranca.login.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Usuarios", description = "Login, troca de senha, cadastro, desbloqueio e listagem de usuarios")
public class UsuarioController {

    private final LoginService service;

    public UsuarioController(LoginService service) {
        this.service = service;
    }

    // --------------------------------------------------------------------- publicos

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Efetua login",
            description = "Endpoint publico. Valida usuario e senha na tabela usuarios. "
                    + "Na segunda falha consecutiva o usuario e bloqueado (tabela login_attempts).")
    @ApiResponse(responseCode = "200", description = "Usuario e senha validos",
            content = @Content(schema = @Schema(implementation = MensagemResponse.class)))
    @ApiResponse(responseCode = "401", description = "Usuario e senha invalidos ou usuario bloqueado",
            content = @Content(schema = @Schema(implementation = MensagemResponse.class)))
    @ApiResponse(responseCode = "400", description = "JSON invalido", content = @Content)
    public ResponseEntity<MensagemResponse> login(@Valid @RequestBody LoginRequest req) {
        return switch (service.login(req.username(), req.senha())) {
            case SUCESSO -> ResponseEntity.ok(MensagemResponse.of(200, "Login efetuado com sucesso"));
            case BLOQUEADO -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MensagemResponse.of(401, "Usuario bloqueado"));
            case CREDENCIAIS_INVALIDAS -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MensagemResponse.of(401, "Usuario ou senha invalidos"));
        };
    }

    @PutMapping(value = "/trocarsenha/{userid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Troca a senha do usuario",
            description = "Endpoint publico. Confere a senha atual com a base de dados antes de gravar a nova. "
                    + "Senha atual incorreta conta como tentativa falha (pode bloquear o usuario).")
    @ApiResponse(responseCode = "200", description = "Senha alterada",
            content = @Content(schema = @Schema(implementation = MensagemResponse.class)))
    @ApiResponse(responseCode = "400", description = "Nova senha invalida (vazia, > 10 caracteres ou igual a atual)",
            content = @Content(schema = @Schema(implementation = MensagemResponse.class)))
    @ApiResponse(responseCode = "401", description = "Senha atual invalida",
            content = @Content(schema = @Schema(implementation = MensagemResponse.class)))
    @ApiResponse(responseCode = "404", description = "Usuario nao encontrado",
            content = @Content(schema = @Schema(implementation = MensagemResponse.class)))
    @ApiResponse(responseCode = "423", description = "Usuario bloqueado",
            content = @Content(schema = @Schema(implementation = MensagemResponse.class)))
    public ResponseEntity<MensagemResponse> trocarSenha(
            @Parameter(description = "username do usuario", example = "maria") @PathVariable String userid,
            @Valid @RequestBody TrocaSenhaRequest req) {
        service.trocarSenha(userid, req.atual(), req.nova());
        return ResponseEntity.ok(MensagemResponse.of(200, "Senha alterada com sucesso"));
    }

    // ------------------------------------------------------------- ROLE_MANUTENCAO

    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cadastra um novo usuario",
            description = "Requer HTTP Basic de um usuario com ROLE_MANUTENCAO.",
            security = @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH))
    @ApiResponse(responseCode = "201", description = "Usuario criado",
            content = @Content(schema = @Schema(implementation = MensagemResponse.class)))
    @ApiResponse(responseCode = "400", description = "Dados invalidos", content = @Content)
    @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sem a role ROLE_MANUTENCAO", content = @Content)
    @ApiResponse(responseCode = "409", description = "Usuario ja existe",
            content = @Content(schema = @Schema(implementation = MensagemResponse.class)))
    public ResponseEntity<MensagemResponse> cadastrar(@Valid @RequestBody NovoUsuarioRequest req) {
        service.cadastrar(req.username(), req.senha());
        return ResponseEntity.created(URI.create("/"))
                .body(MensagemResponse.of(201, "Usuario cadastrado com sucesso"));
    }

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lista todos os usuarios",
            description = "Requer HTTP Basic de um usuario com ROLE_MANUTENCAO. A senha nao e retornada.",
            security = @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH))
    @ApiResponse(responseCode = "200", description = "Lista de usuarios",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UsuarioResponse.class))))
    @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sem a role ROLE_MANUTENCAO", content = @Content)
    public List<UsuarioResponse> listar() {
        return service.listar();
    }

    // ------------------------------------------------------------------ ROLE_ADMIN

    @PutMapping(value = "/unlock/{userid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Desbloqueia um usuario",
            description = "Requer HTTP Basic de um usuario com ROLE_ADMIN. Zera as tentativas falhas.",
            security = @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH))
    @ApiResponse(responseCode = "200", description = "Usuario desbloqueado",
            content = @Content(schema = @Schema(implementation = MensagemResponse.class)))
    @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content)
    @ApiResponse(responseCode = "403", description = "Sem a role ROLE_ADMIN", content = @Content)
    @ApiResponse(responseCode = "404", description = "Usuario nao encontrado",
            content = @Content(schema = @Schema(implementation = MensagemResponse.class)))
    public ResponseEntity<MensagemResponse> desbloquear(
            @Parameter(description = "username do usuario", example = "maria") @PathVariable String userid) {
        service.desbloquear(userid);
        return ResponseEntity.ok(MensagemResponse.of(200, "Usuario desbloqueado com sucesso"));
    }
}
