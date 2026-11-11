package br.ueg.controleobras.servico;

import br.ueg.controleobras.dominio.StatusUsuario;
import br.ueg.controleobras.dominio.Tela;
import br.ueg.controleobras.dominio.Usuario;
import br.ueg.controleobras.dto.UsuarioRequest;
import br.ueg.controleobras.dto.UsuarioResponse;
import br.ueg.controleobras.erro.ErroValidacaoException;
import br.ueg.controleobras.repositorio.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarios;

    @Mock
    private PasswordEncoder encoder;

    private UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(usuarios, encoder);
    }

    @Test
    void criaUsuarioNormalizandoLoginEProtegendoSenha() {
        when(usuarios.existsByLogin("admin")).thenReturn(false);
        when(encoder.encode("segredo123")).thenReturn("hash-bcrypt");
        when(usuarios.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario usuario = inv.getArgument(0);
            usuario.setId(1L);
            return usuario;
        });

        UsuarioResponse resposta = service.criar(new UsuarioRequest(
                "  Administrador  ",
                " ADMIN ",
                "segredo123",
                StatusUsuario.ATIVO,
                Set.of(Tela.ADMINISTRADOR, Tela.PAGAMENTOS)));

        assertThat(resposta.id()).isEqualTo(1L);
        assertThat(resposta.nome()).isEqualTo("Administrador");
        assertThat(resposta.login()).isEqualTo("admin");
        assertThat(resposta.status()).isEqualTo(StatusUsuario.ATIVO);
        assertThat(resposta.telas()).containsExactlyInAnyOrder(
                Tela.ADMINISTRADOR, Tela.PAGAMENTOS);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(captor.capture());
        assertThat(captor.getValue().getSenha()).isEqualTo("hash-bcrypt");
    }

    @Test
    void rejeitaLoginJaCadastrado() {
        when(usuarios.existsByLogin("admin")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(new UsuarioRequest(
                "Administrador", "admin", "segredo123",
                StatusUsuario.ATIVO, Set.of(Tela.ADMINISTRADOR))))
                .isInstanceOf(ErroValidacaoException.class)
                .hasMessageContaining("Login já cadastrado");

        verify(encoder, never()).encode(any());
        verify(usuarios, never()).save(any());
    }

    @Test
    void exigeSenhaNoCadastro() {
        when(usuarios.existsByLogin("admin")).thenReturn(false);

        assertThatThrownBy(() -> service.criar(new UsuarioRequest(
                "Administrador", "admin", " ",
                StatusUsuario.ATIVO, Set.of(Tela.ADMINISTRADOR))))
                .isInstanceOf(ErroValidacaoException.class)
                .hasMessageContaining("Senha é obrigatória");

        verify(encoder, never()).encode(any());
        verify(usuarios, never()).save(any());
    }

    @Test
    void rejeitaSenhaCurta() {
        when(usuarios.existsByLogin("admin")).thenReturn(false);

        assertThatThrownBy(() -> service.criar(new UsuarioRequest(
                "Administrador", "admin", "123",
                StatusUsuario.ATIVO, Set.of(Tela.ADMINISTRADOR))))
                .isInstanceOf(ErroValidacaoException.class)
                .hasMessageContaining("ao menos 6 caracteres");
    }

    @Test
    void atualizarSemSenhaMantemSenhaAtual() {
        Usuario usuario = usuario();
        when(usuarios.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarios.existsByLoginAndIdNot("admin", 1L)).thenReturn(false);
        when(usuarios.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse resposta = service.atualizar(1L, new UsuarioRequest(
                "Administrador", "ADMIN", null,
                StatusUsuario.INATIVO, Set.of(Tela.ADMINISTRADOR)));

        assertThat(resposta.status()).isEqualTo(StatusUsuario.INATIVO);
        assertThat(usuario.getSenha()).isEqualTo("hash-atual");
        verify(encoder, never()).encode(any());
    }

    @Test
    void atualizarComSenhaTrocaHash() {
        Usuario usuario = usuario();
        when(usuarios.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarios.existsByLoginAndIdNot("admin", 1L)).thenReturn(false);
        when(encoder.encode("novaSenha123")).thenReturn("hash-novo");
        when(usuarios.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        service.atualizar(1L, new UsuarioRequest(
                "Administrador", "admin", "novaSenha123",
                StatusUsuario.ATIVO, Set.of(Tela.ADMINISTRADOR)));

        assertThat(usuario.getSenha()).isEqualTo("hash-novo");
    }

    @Test
    void atualizarRejeitaLoginDeOutroUsuario() {
        when(usuarios.findById(1L)).thenReturn(Optional.of(usuario()));
        when(usuarios.existsByLoginAndIdNot("admin", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(1L, new UsuarioRequest(
                "Administrador", "admin", null,
                StatusUsuario.ATIVO, Set.of(Tela.ADMINISTRADOR))))
                .isInstanceOf(ErroValidacaoException.class)
                .hasMessageContaining("Login já cadastrado");

        verify(usuarios, never()).save(any());
    }

    private static Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Administrador");
        usuario.setLogin("admin");
        usuario.setSenha("hash-atual");
        usuario.setStatus(StatusUsuario.ATIVO);
        usuario.setTelas(Set.of(Tela.ADMINISTRADOR));
        return usuario;
    }
}
