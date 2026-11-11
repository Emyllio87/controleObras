package br.ueg.controleobras.web;

import br.ueg.controleobras.dto.CardDiariaResponse;
import br.ueg.controleobras.dto.DiariaAtualizacaoRequest;
import br.ueg.controleobras.dto.DiariaRequest;
import br.ueg.controleobras.dto.DiariaResponse;
import br.ueg.controleobras.dto.FechamentoResponse;
import br.ueg.controleobras.servico.DiariaService;
import br.ueg.controleobras.util.PdfGerado;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/diarias")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('TELA_DIARIAS')")
public class DiariaController {

    private final DiariaService diarias;

    @GetMapping
    public List<CardDiariaResponse> cards() {
        return diarias.cards();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DiariaResponse lancar(@Valid @RequestBody DiariaRequest dados) {
        return diarias.lancar(dados);
    }

    @PutMapping("/{id}")
    public DiariaResponse alterar(@PathVariable Long id, @Valid @RequestBody DiariaAtualizacaoRequest dados) {
        return diarias.alterar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        diarias.excluir(id);
    }

    @PostMapping("/fechamentos")
    @ResponseStatus(HttpStatus.CREATED)
    public FechamentoResponse fechar(@RequestParam Long funcionarioId) {
        return diarias.fechar(funcionarioId);
    }
}
