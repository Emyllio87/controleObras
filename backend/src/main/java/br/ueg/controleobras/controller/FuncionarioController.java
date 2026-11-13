package br.ueg.controleobras.controller;

import br.ueg.controleobras.model.TipoChavePix;
import br.ueg.controleobras.model.FuncionarioRequest;
import br.ueg.controleobras.model.FuncionarioResponse;
import br.ueg.controleobras.service.FuncionarioService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/funcionarios")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('TELA_FUNCIONARIOS')")
public class FuncionarioController {

    private final FuncionarioService funcionarios;

    @GetMapping
    public List<FuncionarioResponse> listar(@RequestParam(required = false) TipoChavePix tipoChavePix) {
        return funcionarios.listar(tipoChavePix);
    }

    @GetMapping("/{id}")
    public FuncionarioResponse buscar(@PathVariable Long id) {
        return funcionarios.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FuncionarioResponse criar(@Valid @RequestBody FuncionarioRequest dados) {
        return funcionarios.criar(dados);
    }

    @PutMapping("/{id}")
    public FuncionarioResponse atualizar(@PathVariable Long id, @Valid @RequestBody FuncionarioRequest dados) {
        return funcionarios.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        funcionarios.excluir(id);
    }
}
