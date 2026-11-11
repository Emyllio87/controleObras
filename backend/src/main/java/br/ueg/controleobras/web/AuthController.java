package br.ueg.controleobras.web;

import br.ueg.controleobras.dto.LoginRequest;
import br.ueg.controleobras.dto.LoginResponse;
import br.ueg.controleobras.dto.SessaoResponse;
import br.ueg.controleobras.servico.AutenticacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AutenticacaoService autenticacao;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest dados) {
        return autenticacao.login(dados);
    }

    @GetMapping("/sessao")
    public SessaoResponse sessao(Authentication authentication) {
        return autenticacao.sessao(authentication.getName());
    }
}
