package br.ueg.controleobras.service;

import br.ueg.controleobras.model.UsuarioRequest;
import br.ueg.controleobras.model.UsuarioResponse;
import br.ueg.controleobras.model.Usuario;
import br.ueg.controleobras.exception.ErroValidacaoException;
import br.ueg.controleobras.exception.RecursoNaoEncontradoException;
import br.ueg.controleobras.repository.UsuarioRepository;
import br.ueg.controleobras.service.Emails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarios.findAllByOrderByIdAsc().stream().map(UsuarioResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscar(Long id) {
        return UsuarioResponse.de(buscarEntidade(id));
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest dados) {
        validar(dados, null);
        Usuario usuario = new Usuario();
        usuario.setSenha(encoder.encode(dados.senha()));
        preencher(usuario, dados);
        return UsuarioResponse.de(usuarios.save(usuario));
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioRequest dados) {
        Usuario usuario = buscarEntidade(id);
        validar(dados, id);
        if (dados.senha() != null && !dados.senha().isBlank()) {
            usuario.setSenha(encoder.encode(dados.senha()));
        }
        preencher(usuario, dados);
        return UsuarioResponse.de(usuarios.save(usuario));
    }

    @Transactional
    public void excluir(Long id) {
        usuarios.delete(buscarEntidade(id));
    }

    public Usuario buscarEntidade(Long id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    private void validar(UsuarioRequest dados, Long idAtual) {
        if (!Emails.valido(dados.login())) {
            throw new ErroValidacaoException("login", "E-mail inválido");
        }
        String login = Emails.normalizar(dados.login());
        boolean duplicado = idAtual == null
                ? usuarios.existsByLogin(login)
                : usuarios.existsByLoginAndIdNot(login, idAtual);
        if (duplicado) {
            throw new ErroValidacaoException("login", "E-mail já cadastrado");
        }
        if (idAtual == null && (dados.senha() == null || dados.senha().isBlank())) {
            throw new ErroValidacaoException("senha", "Senha é obrigatória");
        }
        if (dados.senha() != null && !dados.senha().isBlank() && dados.senha().length() < 6) {
            throw new ErroValidacaoException("senha", "Senha deve ter ao menos 6 caracteres");
        }
    }

    private void preencher(Usuario usuario, UsuarioRequest dados) {
        usuario.setNome(dados.nome().trim());
        usuario.setLogin(Emails.normalizar(dados.login()));
        usuario.setStatus(dados.status());
        usuario.setTelas(new HashSet<>(dados.telas()));
    }
}
