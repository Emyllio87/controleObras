package br.ueg.controleobras.service;

import br.ueg.controleobras.model.Diaria;
import br.ueg.controleobras.model.FechamentoDiaria;
import br.ueg.controleobras.model.Funcionario;
import br.ueg.controleobras.model.Pagamento;
import br.ueg.controleobras.model.StatusPagamento;
import br.ueg.controleobras.model.CardDiariaResponse;
import br.ueg.controleobras.model.DiariaAtualizacaoRequest;
import br.ueg.controleobras.model.DiariaRequest;
import br.ueg.controleobras.model.DiariaResponse;
import br.ueg.controleobras.model.FechamentoResponse;
import br.ueg.controleobras.exception.ErroValidacaoException;
import br.ueg.controleobras.exception.RecursoNaoEncontradoException;
import br.ueg.controleobras.exception.RegraNegocioException;
import br.ueg.controleobras.service.RelatorioPdfService;
import br.ueg.controleobras.repository.DiariaRepository;
import br.ueg.controleobras.repository.FechamentoDiariaRepository;
import br.ueg.controleobras.repository.FuncionarioRepository;
import br.ueg.controleobras.repository.PagamentoRepository;
import br.ueg.controleobras.service.PdfGerado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiariaService {

    private final DiariaRepository diarias;
    private final FuncionarioRepository funcionarios;
    private final FechamentoDiariaRepository fechamentos;
    private final PagamentoRepository pagamentos;
    private final RelatorioPdfService relatorios;

    @Transactional(readOnly = true)
    public List<CardDiariaResponse> cards() {
        return funcionarios.findAllByOrderByNomeAsc().stream().map(this::montarCard).toList();
    }

    @Transactional
    public DiariaResponse lancar(DiariaRequest dados) {
        Funcionario funcionario = funcionarios.findById(dados.funcionarioId())
                .orElseThrow(() -> new ErroValidacaoException("funcionarioId", "Funcionário não encontrado"));
        validarData(dados.data());
        BigDecimal valor = dados.valor() == null ? funcionario.getValorDiaria() : dados.valor();

        Diaria diaria = new Diaria();
        diaria.setFuncionario(funcionario);
        diaria.setData(dados.data());
        diaria.setValor(validarValor(valor));
        return DiariaResponse.de(diarias.save(diaria));
    }

    @Transactional
    public DiariaResponse alterar(Long id, DiariaAtualizacaoRequest dados) {
        Diaria diaria = buscarEntidade(id);
        if (diaria.getFechamento() != null) {
            throw new RegraNegocioException("Diária já incluída em um fechamento e não pode ser alterada");
        }
        validarData(dados.data());
        diaria.setData(dados.data());
        if (dados.valor() != null) {
            diaria.setValor(validarValor(dados.valor()));
        }
        return DiariaResponse.de(diarias.save(diaria));
    }

    @Transactional
    public void excluir(Long id) {
        Diaria diaria = buscarEntidade(id);
        if (diaria.getFechamento() != null) {
            throw new RegraNegocioException("Diária já incluída em um fechamento e não pode ser excluída");
        }
        diarias.delete(diaria);
    }

    @Transactional
    public FechamentoResponse fechar(Long funcionarioId) {
        Funcionario funcionario = funcionarios.findById(funcionarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Funcionário não encontrado"));
        List<Diaria> abertas = diarias.findByFuncionarioIdAndFechamentoIsNullOrderByIdAsc(funcionarioId);
        if (abertas.isEmpty()) {
            throw new RegraNegocioException("Nenhuma diária lançada para este funcionário");
        }

        FechamentoDiaria fechamento = new FechamentoDiaria();
        fechamento.setFuncionario(funcionario);
        fechamento.setPeriodoInicio(abertas.get(0).getData());
        fechamento.setPeriodoFim(abertas.get(abertas.size() - 1).getData());
        fechamento.setValorTotal(somar(abertas));
        fechamento.setDataFechamento(LocalDate.now());
        fechamentos.save(fechamento);

        for (Diaria diaria : abertas) {
            diaria.setFechamento(fechamento);
            fechamento.getDiarias().add(diaria);
        }
        diarias.saveAll(abertas);

        Pagamento pagamento = new Pagamento();
        pagamento.setFechamento(fechamento);
        pagamento.setFuncionario(funcionario);
        pagamento.setPeriodoInicio(fechamento.getPeriodoInicio());
        pagamento.setPeriodoFim(fechamento.getPeriodoFim());
        pagamento.setValorTotal(fechamento.getValorTotal());
        pagamento.setTipoChavePix(funcionario.getTipoChavePix());
        pagamento.setChavePix(funcionario.getChavePix());
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamentos.save(pagamento);

        return FechamentoResponse.de(fechamento);
    }

    @Transactional(readOnly = true)
    public List<FechamentoResponse> historico() {
        return fechamentos.findAllByOrderByIdAsc().stream()
                .map(FechamentoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public PdfGerado gerarPdfFechamento(Long id) {
        FechamentoDiaria fechamento = fechamentos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fechamento não encontrado"));
        String nome = "fechamento-diarias-" + id + ".pdf";
        return new PdfGerado(nome, relatorios.fechamento(fechamento));
    }

    private CardDiariaResponse montarCard(Funcionario funcionario) {
        List<Diaria> abertas = diarias.findByFuncionarioIdAndFechamentoIsNullOrderByIdAsc(funcionario.getId());
        return new CardDiariaResponse(
                funcionario.getId(),
                funcionario.getNome(),
                funcionario.getFuncao(),
                funcionario.getValorDiaria(),
                somar(abertas),
                abertas.stream().map(DiariaResponse::de).toList());
    }

    private Diaria buscarEntidade(Long id) {
        return diarias.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Diária não encontrada"));
    }

    private static BigDecimal somar(List<Diaria> lista) {
        return lista.stream().map(Diaria::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal validarValor(BigDecimal valor) {
        if (valor == null || valor.compareTo(new BigDecimal("0.01")) < 0) {
            throw new ErroValidacaoException("valor", "Valor deve ser maior que zero");
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private static void validarData(LocalDate data) {
        if (data == null) {
            throw new ErroValidacaoException("data", "Data é obrigatória");
        }
        if (data.isAfter(LocalDate.now())) {
            throw new ErroValidacaoException("data", "A diária não pode ser lançada para data futura");
        }
    }
}
