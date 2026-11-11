package br.ueg.controleobras.web;

import br.ueg.controleobras.dto.FerramentaRequest;
import br.ueg.controleobras.dto.FerramentaResponse;
import br.ueg.controleobras.servico.FerramentaService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ferramentas")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('TELA_ALMOXARIFADO')")
public class FerramentaController {

    private final FerramentaService ferramentas;

    @GetMapping
    public List<FerramentaResponse> listar() {
        return ferramentas.listar();
    }

    @GetMapping("/{id}")
    public FerramentaResponse buscar(@PathVariable Long id) {
        return ferramentas.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FerramentaResponse criar(@Valid @RequestBody FerramentaRequest dados) {
        return ferramentas.criar(dados);
    }

    @PutMapping("/{id}")
    public FerramentaResponse atualizar(@PathVariable Long id, @Valid @RequestBody FerramentaRequest dados) {
        return ferramentas.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        ferramentas.excluir(id);
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf() {
        PdfGerado pdf = ferramentas.gerarPdf();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(pdf.nomeArquivo()).build().toString())
                .body(pdf.conteudo());
    }
}
