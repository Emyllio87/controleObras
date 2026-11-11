package br.ueg.controleobras.web;

import br.ueg.controleobras.dominio.Tela;
import br.ueg.controleobras.dto.LoginResponse;
import br.ueg.controleobras.dto.SessaoResponse;
import br.ueg.controleobras.repositorio.UsuarioRepository;
import br.ueg.controleobras.seguranca.JwtService;
import br.ueg.controleobras.servico.AutenticacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc
@Import(AuthControllerTest.TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AutenticacaoService autenticacao;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UsuarioRepository usuarios;

    @Test
    void realizaLogin() throws Exception {
        when(autenticacao.login(any())).thenReturn(new LoginResponse(
                "jwt-token", "Administrador", "admin", Set.of(Tela.ADMINISTRADOR)));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"admin\",\"senha\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.nome").value("Administrador"))
                .andExpect(jsonPath("$.login").value("admin"))
                .andExpect(jsonPath("$.telas[0]").value("ADMINISTRADOR"));
    }

    @Test
    void rejeitaLoginSemCredenciais() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.login").value("Informe o login"))
                .andExpect(jsonPath("$.campos.senha").value("Informe a senha"));

        verify(autenticacao, never()).login(any());
    }

    @Test
    void retornaSessaoDoUsuarioAutenticado() throws Exception {
        when(autenticacao.sessao("admin")).thenReturn(
                new SessaoResponse("Administrador", "admin", Set.of(Tela.ADMINISTRADOR)));

        mvc.perform(get("/api/auth/sessao")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                "admin", "senha", List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Administrador"))
                .andExpect(jsonPath("$.login").value("admin"));
    }

    @TestConfiguration
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(autorizacao -> autorizacao.anyRequest().permitAll());
            return http.build();
        }
    }
}
