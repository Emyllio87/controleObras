package br.ueg.controleobras.web;

import br.ueg.controleobras.dto.PedidoRequest;
import br.ueg.controleobras.dto.PedidoResponse;
import br.ueg.controleobras.servico.PedidoService;
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
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('TELA_PEDIDOS_ORCAMENTO')")
public class PedidoController {

    private final PedidoService pedidos;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('TELA_PEDIDOS_ORCAMENTO', 'TELA_AUTORIZACAO_PAGAMENTO')")
    public List<PedidoResponse> listar() {
        return pedidos.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TELA_PEDIDOS_ORCAMENTO', 'TELA_AUTORIZACAO_PAGAMENTO')")
    public PedidoResponse buscar(@PathVariable Long id) {
        return pedidos.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse criar(@Valid @RequestBody PedidoRequest dados) {
        return pedidos.criar(dados);
    }

    @PutMapping("/{id}")
    public PedidoResponse atualizar(@PathVariable Long id, @Valid @RequestBody PedidoRequest dados) {
        return pedidos.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        pedidos.excluir(id);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        PdfGerado pdf = pedidos.gerarPdf(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(pdf.nomeArquivo()).build().toString())
                .body(pdf.conteudo());
    }
}
