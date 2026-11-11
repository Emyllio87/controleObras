package br.ueg.controleobras.servico;

import br.ueg.controleobras.dominio.StatusUsuario;
import br.ueg.controleobras.dominio.Tela;
import br.ueg.controleobras.dominio.Usuario;
import br.ueg.controleobras.dto.LoginRequest;
import br.ueg.controleobras.dto.LoginResponse;
import br.ueg.controleobras.dto.SessaoResponse;
import br.ueg.controleobras.erro.RecursoNaoEncontradoException;
import br.ueg.controleobras.erro.RegraNegocioException;
import br.ueg.controleobras.repositorio.UsuarioRepository;
import br.ueg.controleobras.seguranca.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTest {

    @Mock
    private UsuarioRepository usuarios;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtService jwtService;

    private AutenticacaoService service;

    @BeforeEach
    void setUp() {
        service = new AutenticacaoService(usuarios, encoder, jwtService);
    }

    @Test
    void realizaLoginComCredenciaisValidas() {
        Usuario usuario = usuario(StatusUsuario.ATIVO);
        when(usuarios.findByLogin("admin")).thenReturn(Optional.of(usuario));
        when(encoder.matches("admin123", "hash")).thenReturn(true);
        when(jwtService.gerarToken(usuario)).thenReturn("jwt-token");

        LoginResponse resposta = service.login(new LoginRequest("  ADMIN ", "admin123"));

        assertThat(resposta.token()).isEqualTo("jwt-token");
        assertThat(resposta.nome()).isEqualTo("Administrador");
        assertThat(resposta.login()).isEqualTo("admin");
        assertThat(resposta.telas()).containsExactly(Tela.ADMINISTRADOR);
    }

    @Test
    void rejeitaSenhaIncorreta() {
        when(usuarios.findByLogin("admin")).thenReturn(Optional.of(usuario(StatusUsuario.ATIVO)));
        when(encoder.matches("errada", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginRequest("admin", "errada")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Usuário ou senha inválidos");

        verify(jwtService, never()).gerarToken(any());
    }

    @Test
    void rejeitaUsuarioInativoSemConsultarSenha() {
        when(usuarios.findByLogin("admin"))
                .thenReturn(Optional.of(usuario(StatusUsuario.INATIVO)));

        assertThatThrownBy(() -> service.login(new LoginRequest("admin", "admin123")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Usuário ou senha inválidos");

        verify(encoder, never()).matches(any(), any());
        verify(jwtService, never()).gerarToken(any());
    }

    @Test
    void rejeitaLoginInexistente() {
        when(usuarios.findByLogin("fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("fantasma", "admin123")))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void retornaSessaoDoUsuario() {
        when(usuarios.findByLogin("admin"))
                .thenReturn(Optional.of(usuario(StatusUsuario.ATIVO)));

        SessaoResponse resposta = service.sessao("admin");

        assertThat(resposta.nome()).isEqualTo("Administrador");
        assertThat(resposta.login()).isEqualTo("admin");
        assertThat(resposta.telas()).containsExactly(Tela.ADMINISTRADOR);
    }

    @Test
    void rejeitaSessaoDeUsuarioInexistente() {
        when(usuarios.findByLogin("fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.sessao("fantasma"))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    private static Usuario usuario(StatusUsuario status) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Administrador");
        usuario.setLogin("admin");
        usuario.setSenha("hash");
        usuario.setStatus(status);
        usuario.setTelas(Set.of(Tela.ADMINISTRADOR));
        return usuario;
    }
}
