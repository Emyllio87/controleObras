package br.ueg.controleobras.service;

import br.ueg.controleobras.model.Ferramenta;
import br.ueg.controleobras.model.FerramentaRequest;
import br.ueg.controleobras.model.FerramentaResponse;
import br.ueg.controleobras.exception.ErroValidacaoException;
import br.ueg.controleobras.exception.RecursoNaoEncontradoException;
import br.ueg.controleobras.service.RelatorioPdfService;
import br.ueg.controleobras.repository.FerramentaRepository;
import br.ueg.controleobras.service.PdfGerado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class FerramentaService {

    private final FerramentaRepository ferramentas;
    private final RelatorioPdfService relatorios;

    @Transactional(readOnly = true)
    public List<FerramentaResponse> listar() {
        return ferramentas.findAllByOrderByIdAsc().stream().map(FerramentaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public FerramentaResponse buscar(Long id) {
        return FerramentaResponse.de(buscarEntidade(id));
    }

    @Transactional
    public FerramentaResponse criar(FerramentaRequest dados) {
        Ferramenta ferramenta = new Ferramenta();
        preencher(ferramenta, dados);
        return FerramentaResponse.de(ferramentas.save(ferramenta));
    }

    @Transactional
    public FerramentaResponse atualizar(Long id, FerramentaRequest dados) {
        Ferramenta ferramenta = buscarEntidade(id);
        preencher(ferramenta, dados);
        return FerramentaResponse.de(ferramentas.save(ferramenta));
    }

    @Transactional
    public void excluir(Long id) {
        ferramentas.delete(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public PdfGerado gerarPdf() {
        return new PdfGerado("almoxarifado.pdf", relatorios.almoxarifado(ferramentas.findAllByOrderByIdAsc()));
    }

    public Ferramenta buscarEntidade(Long id) {
        return ferramentas.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ferramenta não encontrada"));
    }

    private void preencher(Ferramenta ferramenta, FerramentaRequest dados) {
        if (dados.quantidade() == null || dados.quantidade().compareTo(new BigDecimal("0.001")) < 0) {
            throw new ErroValidacaoException("quantidade", "Quantidade deve ser maior que zero");
        }
        ferramenta.setNome(dados.nome().trim());
        ferramenta.setQuantidade(dados.quantidade().setScale(3, RoundingMode.HALF_UP));
        ferramenta.setUnidade(dados.unidade().trim().toUpperCase(Locale.ROOT));
    }
}
