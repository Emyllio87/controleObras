package br.ueg.controleobras.servico;

import br.ueg.controleobras.dominio.StatusUsuario;
import br.ueg.controleobras.dominio.Usuario;
import br.ueg.controleobras.dto.LoginRequest;
import br.ueg.controleobras.dto.LoginResponse;
import br.ueg.controleobras.dto.SessaoResponse;
import br.ueg.controleobras.erro.RecursoNaoEncontradoException;
import br.ueg.controleobras.erro.RegraNegocioException;
import br.ueg.controleobras.repositorio.UsuarioRepository;
import br.ueg.controleobras.seguranca.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AutenticacaoService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest dados) {
        Usuario usuario = usuarios.findByLogin(dados.login().trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new RegraNegocioException("Usuário ou senha inválidos"));
        if (usuario.getStatus() != StatusUsuario.ATIVO || !encoder.matches(dados.senha(), usuario.getSenha())) {
            throw new RegraNegocioException("Usuário ou senha inválidos");
        }
        return new LoginResponse(
                jwtService.gerarToken(usuario),
                usuario.getNome(),
                usuario.getLogin(),
                usuario.getTelas());
    }

    @Transactional(readOnly = true)
    public SessaoResponse sessao(String login) {
        Usuario usuario = usuarios.findByLogin(login)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
        return new SessaoResponse(usuario.getNome(), usuario.getLogin(), usuario.getTelas());
    }
}
