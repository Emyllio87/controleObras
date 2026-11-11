package br.ueg.controleobras.config;

import br.ueg.controleobras.dominio.StatusUsuario;
import br.ueg.controleobras.dominio.Tela;
import br.ueg.controleobras.dominio.Usuario;
import br.ueg.controleobras.repositorio.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    @Value("${app.admin.login}")
    private String login;

    @Value("${app.admin.senha}")
    private String senha;

    @Override
    public void run(String... args) {
        if (usuarios.count() > 0) {
            return;
        }
        Usuario admin = new Usuario();
        admin.setNome("Administrador");
        admin.setLogin(login.trim().toLowerCase(Locale.ROOT));
        admin.setSenha(encoder.encode(senha));
        admin.setStatus(StatusUsuario.ATIVO);
        admin.setTelas(EnumSet.allOf(Tela.class));
        usuarios.save(admin);
        log.info("Usuário administrador criado com o login '{}'. Altere a senha após o primeiro acesso.", admin.getLogin());
    }
}
