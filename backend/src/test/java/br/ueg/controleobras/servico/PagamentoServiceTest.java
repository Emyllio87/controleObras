package br.ueg.controleobras.servico;

import br.ueg.controleobras.dominio.Funcionario;
import br.ueg.controleobras.dominio.Pagamento;
import br.ueg.controleobras.dominio.StatusPagamento;
import br.ueg.controleobras.dominio.TipoChavePix;
import br.ueg.controleobras.dto.PagamentoResponse;
import br.ueg.controleobras.erro.RecursoNaoEncontradoException;
import br.ueg.controleobras.repositorio.PagamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock
    private PagamentoRepository pagamentos;

    private PagamentoService service;

    @BeforeEach
    void setUp() {
        service = new PagamentoService(pagamentos);
    }

    @Test
    void listaTodosOsPagamentosSemFiltro() {
        when(pagamentos.findAllByOrderByPeriodoFimDescIdDesc())
                .thenReturn(List.of(pagamento(StatusPagamento.PENDENTE)));

        List<PagamentoResponse> resposta = service.listar(null);

        assertThat(resposta).hasSize(1);
        assertThat(resposta.get(0).status()).isEqualTo(StatusPagamento.PENDENTE);
    }

    @Test
    void listaPagamentosPorStatus() {
        when(pagamentos.findAllByStatusOrderByPeriodoFimDescIdDesc(StatusPagamento.PAGO))
                .thenReturn(List.of(pagamento(StatusPagamento.PAGO)));

        List<PagamentoResponse> resposta = service.listar(StatusPagamento.PAGO);

        assertThat(resposta).hasSize(1);
        assertThat(resposta.get(0).status()).isEqualTo(StatusPagamento.PAGO);
    }

    @Test
    void alteraStatusDoPagamento() {
        Pagamento pagamento = pagamento(StatusPagamento.PENDENTE);
        when(pagamentos.findById(1L)).thenReturn(Optional.of(pagamento));
        when(pagamentos.save(any(Pagamento.class))).thenAnswer(inv -> inv.getArgument(0));

        PagamentoResponse resposta = service.alterarStatus(1L, StatusPagamento.PAGO);

        assertThat(resposta.status()).isEqualTo(StatusPagamento.PAGO);
        assertThat(pagamento.getStatus()).isEqualTo(StatusPagamento.PAGO);
        verify(pagamentos).save(pagamento);
    }

    @Test
    void rejeitaAlteracaoDePagamentoInexistente() {
        when(pagamentos.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.alterarStatus(99L, StatusPagamento.PAGO))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Pagamento não encontrado");

        verify(pagamentos, never()).save(any());
    }

    private static Pagamento pagamento(StatusPagamento status) {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(1L);
        funcionario.setNome("Ana");

        Pagamento pagamento = new Pagamento();
        pagamento.setId(1L);
        pagamento.setFuncionario(funcionario);
        pagamento.setPeriodoInicio(LocalDate.of(2026, 1, 5));
        pagamento.setPeriodoFim(LocalDate.of(2026, 1, 7));
        pagamento.setValorTotal(new BigDecimal("250.00"));
        pagamento.setTipoChavePix(TipoChavePix.EMAIL);
        pagamento.setChavePix("ana@obra.com");
        pagamento.setStatus(status);
        return pagamento;
    }
}
