package br.ueg.controleobras.seguranca;

import br.ueg.controleobras.dominio.StatusUsuario;
import br.ueg.controleobras.dominio.Usuario;
import br.ueg.controleobras.repositorio.UsuarioRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarios;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecalho = request.getHeader("Authorization");
        if (cabecalho != null && cabecalho.startsWith(PREFIXO)) {
            autenticar(cabecalho.substring(PREFIXO.length()));
        }
        chain.doFilter(request, response);
    }

    private void autenticar(String token) {
        try {
            String login = jwtService.extrairLogin(token);
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                return;
            }
            Usuario usuario = usuarios.findByLogin(login).orElse(null);
            if (usuario == null || usuario.getStatus() != StatusUsuario.ATIVO) {
                return;
            }
            List<SimpleGrantedAuthority> autoridades = usuario.getTelas().stream()
                    .map(tela -> new SimpleGrantedAuthority("TELA_" + tela.name()))
                    .toList();
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(usuario.getLogin(), null, autoridades));
        } catch (JwtException | IllegalArgumentException ignored) {
            SecurityContextHolder.clearContext();
        }
    }
}
