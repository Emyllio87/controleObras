package br.ueg.controleobras.web;

import br.ueg.controleobras.dto.FechamentoResponse;
import br.ueg.controleobras.servico.DiariaService;
import br.ueg.controleobras.util.PdfGerado;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/diarias/historico")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('TELA_HISTORICO_DIARIAS')")
public class HistoricoDiariaController {

    private final DiariaService diarias;

    @GetMapping
    public List<FechamentoResponse> listar() {
        return diarias.historico();
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        PdfGerado pdf = diarias.gerarPdfFechamento(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(pdf.nomeArquivo()).build().toString())
                .body(pdf.conteudo());
    }
}
