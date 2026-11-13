package br.ueg.controleobras.controller;

import br.ueg.controleobras.service.AnexoArquivo;
import br.ueg.controleobras.model.AutorizacaoResponse;
import br.ueg.controleobras.service.AutorizacaoService;
import br.ueg.controleobras.service.PdfGerado;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/autorizacoes")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('TELA_AUTORIZACAO_PAGAMENTO')")
public class AutorizacaoController {

    private final AutorizacaoService autorizacoes;

    @GetMapping
    public List<AutorizacaoResponse> listar() {
        return autorizacoes.listar();
    }

    @GetMapping("/{id}")
    public AutorizacaoResponse buscar(@PathVariable Long id) {
        return autorizacoes.buscar(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AutorizacaoResponse criar(
            @RequestParam Long pedidoId,
            @RequestParam BigDecimal valor,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate dataEmissao,
            @RequestParam(required = false) MultipartFile arquivo) {
        return autorizacoes.criar(pedidoId, valor, dataEmissao, arquivo);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AutorizacaoResponse atualizar(
            @PathVariable Long id,
            @RequestParam BigDecimal valor,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate dataEmissao,
            @RequestParam(required = false) MultipartFile arquivo) {
        return autorizacoes.atualizar(id, valor, dataEmissao, arquivo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        autorizacoes.excluir(id);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        PdfGerado pdf = autorizacoes.gerarPdf(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(pdf.nomeArquivo()).build().toString())
                .body(pdf.conteudo());
    }

    @GetMapping("/{id}/anexo")
    public ResponseEntity<org.springframework.core.io.Resource> anexo(@PathVariable Long id) {
        AnexoArquivo anexo = autorizacoes.baixarAnexo(id);
        return ResponseEntity.ok()
                .contentType(tipoConteudo(anexo.tipo()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(anexo.nome()).build().toString())
                .body(anexo.recurso());
    }

    private static MediaType tipoConteudo(String tipo) {
        try {
            return MediaType.parseMediaType(tipo);
        } catch (Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
