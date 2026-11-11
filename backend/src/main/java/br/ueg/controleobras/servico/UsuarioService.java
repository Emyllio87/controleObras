package br.ueg.controleobras.servico;

import br.ueg.controleobras.dto.UsuarioRequest;
import br.ueg.controleobras.dto.UsuarioResponse;
import br.ueg.controleobras.dominio.Usuario;
import br.ueg.controleobras.erro.ErroValidacaoException;
import br.ueg.controleobras.erro.RecursoNaoEncontradoException;
import br.ueg.controleobras.repositorio.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarios.findAllByOrderByNomeAsc().stream().map(UsuarioResponse::de).toList();
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
        String login = dados.login().trim().toLowerCase(Locale.ROOT);
        boolean duplicado = idAtual == null
                ? usuarios.existsByLogin(login)
                : usuarios.existsByLoginAndIdNot(login, idAtual);
        if (duplicado) {
            throw new ErroValidacaoException("login", "Login já cadastrado");
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
        usuario.setLogin(dados.login().trim().toLowerCase(Locale.ROOT));
        usuario.setStatus(dados.status());
        usuario.setTelas(new HashSet<>(dados.telas()));
    }
}
