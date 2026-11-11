package br.ueg.controleobras.web;

import br.ueg.controleobras.dto.TelaResponse;
import br.ueg.controleobras.dominio.Tela;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/telas")
@PreAuthorize("hasAuthority('TELA_ADMINISTRADOR')")
public class TelaController {

    @GetMapping
    public List<TelaResponse> listar() {
        return Arrays.stream(Tela.values()).map(TelaResponse::de).toList();
    }
}
