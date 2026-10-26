-- Usuarios de exemplo do "sistema generico" (validados pelo POST /login)
MERGE INTO usuarios (username, senha) KEY (username) VALUES ('maria', 'maria123');
MERGE INTO usuarios (username, senha) KEY (username) VALUES ('joao',  'joao123');
MERGE INTO usuarios (username, senha) KEY (username) VALUES ('ana',   'ana123');

MERGE INTO login_attempts (username) KEY (username) VALUES ('maria');
MERGE INTO login_attempts (username) KEY (username) VALUES ('joao');
MERGE INTO login_attempts (username) KEY (username) VALUES ('ana');

-- Os usuarios operacionais (admin / manutencao) sao criados pelo
-- JdbcUserDetailsManager na classe UsuariosIniciais, com senha BCrypt.
