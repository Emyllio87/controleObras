package br.ueg.controleobras.service;

import br.ueg.controleobras.model.StatusUsuario;
import br.ueg.controleobras.model.Usuario;
import br.ueg.controleobras.model.LoginRequest;
import br.ueg.controleobras.model.LoginResponse;
import br.ueg.controleobras.model.SessaoResponse;
import br.ueg.controleobras.exception.RecursoNaoEncontradoException;
import br.ueg.controleobras.exception.RegraNegocioException;
import br.ueg.controleobras.repository.UsuarioRepository;
import br.ueg.controleobras.config.JwtService;
import br.ueg.controleobras.service.Emails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutenticacaoService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest dados) {
        Usuario usuario = usuarios.findByLogin(Emails.normalizar(dados.login()))
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
