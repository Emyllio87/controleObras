package br.ueg.controleobras.web;

import br.ueg.controleobras.dominio.StatusPagamento;
import br.ueg.controleobras.dto.AlterarStatusRequest;
import br.ueg.controleobras.dto.PagamentoResponse;
import br.ueg.controleobras.servico.PagamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pagamentos")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('TELA_PAGAMENTOS')")
public class PagamentoController {

    private final PagamentoService pagamentos;

    @GetMapping
    public List<PagamentoResponse> listar(@RequestParam(required = false) StatusPagamento status) {
        return pagamentos.listar(status);
    }

    @PatchMapping("/{id}/status")
    public PagamentoResponse alterarStatus(@PathVariable Long id, @Valid @RequestBody AlterarStatusRequest dados) {
        return pagamentos.alterarStatus(id, dados.status());
    }
}
