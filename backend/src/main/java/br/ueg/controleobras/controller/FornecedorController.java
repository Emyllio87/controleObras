package br.ueg.controleobras.controller;

import br.ueg.controleobras.model.FornecedorRequest;
import br.ueg.controleobras.model.FornecedorResponse;
import br.ueg.controleobras.service.FornecedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/fornecedores")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('TELA_FORNECEDORES')")
public class FornecedorController {

    private final FornecedorService fornecedores;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('TELA_FORNECEDORES', 'TELA_PEDIDOS_ORCAMENTO')")
    public List<FornecedorResponse> listar() {
        return fornecedores.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TELA_FORNECEDORES', 'TELA_PEDIDOS_ORCAMENTO')")
    public FornecedorResponse buscar(@PathVariable Long id) {
        return fornecedores.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FornecedorResponse criar(@Valid @RequestBody FornecedorRequest dados) {
        return fornecedores.criar(dados);
    }

    @PutMapping("/{id}")
    public FornecedorResponse atualizar(@PathVariable Long id, @Valid @RequestBody FornecedorRequest dados) {
        return fornecedores.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        fornecedores.excluir(id);
    }
}
