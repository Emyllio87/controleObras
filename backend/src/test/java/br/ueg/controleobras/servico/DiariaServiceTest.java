package br.ueg.controleobras.servico;

import br.ueg.controleobras.dominio.Diaria;
import br.ueg.controleobras.dominio.FechamentoDiaria;
import br.ueg.controleobras.dominio.Funcionario;
import br.ueg.controleobras.dominio.Pagamento;
import br.ueg.controleobras.dominio.StatusPagamento;
import br.ueg.controleobras.dominio.TipoChavePix;
import br.ueg.controleobras.dto.DiariaAtualizacaoRequest;
import br.ueg.controleobras.dto.DiariaRequest;
import br.ueg.controleobras.dto.DiariaResponse;
import br.ueg.controleobras.dto.FechamentoResponse;
import br.ueg.controleobras.erro.ErroValidacaoException;
import br.ueg.controleobras.erro.RegraNegocioException;
import br.ueg.controleobras.pdf.RelatorioPdfService;
import br.ueg.controleobras.repositorio.DiariaRepository;
import br.ueg.controleobras.repositorio.FechamentoDiariaRepository;
import br.ueg.controleobras.repositorio.FuncionarioRepository;
import br.ueg.controleobras.repositorio.PagamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class DiariaServiceTest {

    @Mock
    private DiariaRepository diarias;

    @Mock
    private FuncionarioRepository funcionarios;

    @Mock
    private FechamentoDiariaRepository fechamentos;

    @Mock
    private PagamentoRepository pagamentos;

    @Mock
    private RelatorioPdfService relatorios;

    private DiariaService service;

    @BeforeEach
    void setUp() {
        service = new DiariaService(diarias, funcionarios, fechamentos, pagamentos, relatorios);
    }

    @Test
    void lancarUsaValorDoCadastroQuandoValorNaoInformado() {
        Funcionario funcionario = funcionario();
        when(funcionarios.findById(1L)).thenReturn(Optional.of(funcionario));
        when(diarias.save(any(Diaria.class))).thenAnswer(inv -> inv.getArgument(0));

        DiariaResponse resposta = service.lancar(
                new DiariaRequest(1L, LocalDate.now().minusDays(1), null));

        assertThat(resposta.valor()).isEqualByComparingTo("150.00");
        assertThat(resposta.data()).isEqualTo(LocalDate.now().minusDays(1));
    }

    @Test
    void lancarUsaValorInformado() {
        when(funcionarios.findById(1L)).thenReturn(Optional.of(funcionario()));
        when(diarias.save(any(Diaria.class))).thenAnswer(inv -> inv.getArgument(0));

        DiariaResponse resposta = service.lancar(
                new DiariaRequest(1L, LocalDate.now(), new BigDecimal("180.00")));

        assertThat(resposta.valor()).isEqualByComparingTo("180.00");
    }

    @Test
    void rejeitaDataFutura() {
        when(funcionarios.findById(1L)).thenReturn(Optional.of(funcionario()));

        assertThatThrownBy(() -> service.lancar(
                new DiariaRequest(1L, LocalDate.now().plusDays(1), new BigDecimal("100.00"))))
                .isInstanceOf(ErroValidacaoException.class)
                .hasMessageContaining("data futura");

        verify(diarias, never()).save(any());
    }

    @Test
    void alterarAtualizaDataEValor() {
        Diaria diaria = diaria(5L, LocalDate.now().minusDays(3), "100.00");
        when(diarias.findById(5L)).thenReturn(Optional.of(diaria));
        when(diarias.save(any(Diaria.class))).thenAnswer(inv -> inv.getArgument(0));

        DiariaResponse resposta = service.alterar(5L, new DiariaAtualizacaoRequest(
                LocalDate.now().minusDays(2), new BigDecimal("120.00")));

        assertThat(resposta.data()).isEqualTo(LocalDate.now().minusDays(2));
        assertThat(resposta.valor()).isEqualByComparingTo("120.00");
    }

    @Test
    void naoAlteraDiariaJaFechada() {
        Diaria diaria = diaria(5L, LocalDate.now().minusDays(3), "100.00");
        diaria.setFechamento(new FechamentoDiaria());
        when(diarias.findById(5L)).thenReturn(Optional.of(diaria));

        assertThatThrownBy(() -> service.alterar(5L, new DiariaAtualizacaoRequest(
                LocalDate.now(), new BigDecimal("120.00"))))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não pode ser alterada");

        verify(diarias, never()).save(any());
    }

    @Test
    void naoExcluiDiariaJaFechada() {
        Diaria diaria = diaria(5L, LocalDate.now().minusDays(3), "100.00");
        diaria.setFechamento(new FechamentoDiaria());
        when(diarias.findById(5L)).thenReturn(Optional.of(diaria));

        assertThatThrownBy(() -> service.excluir(5L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não pode ser excluída");

        verify(diarias, never()).delete(any());
    }

    @Test
    void rejeitaFechamentoSemDiarias() {
        when(funcionarios.findById(1L)).thenReturn(Optional.of(funcionario()));
        when(diarias.findByFuncionarioIdAndFechamentoIsNullOrderByDataAsc(1L))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.fechar(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Nenhuma diária lançada");

        verify(fechamentos, never()).save(any());
        verify(pagamentos, never()).save(any());
    }

    @Test
    void fecharGeraFechamentoEPagamentoPendente() {
        Funcionario funcionario = funcionario();
        Diaria primeira = diaria(1L, LocalDate.of(2026, 1, 5), "100.00");
        Diaria segunda = diaria(2L, LocalDate.of(2026, 1, 7), "150.00");

        when(funcionarios.findById(1L)).thenReturn(Optional.of(funcionario));
        when(diarias.findByFuncionarioIdAndFechamentoIsNullOrderByDataAsc(1L))
                .thenReturn(List.of(primeira, segunda));
        when(fechamentos.save(any(FechamentoDiaria.class))).thenAnswer(inv -> {
            FechamentoDiaria fechamento = inv.getArgument(0);
            fechamento.setId(9L);
            return fechamento;
        });
        when(diarias.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        FechamentoResponse resposta = service.fechar(1L);

        assertThat(resposta.periodoInicio()).isEqualTo(LocalDate.of(2026, 1, 5));
        assertThat(resposta.periodoFim()).isEqualTo(LocalDate.of(2026, 1, 7));
        assertThat(resposta.valorTotal()).isEqualByComparingTo("250.00");
        assertThat(resposta.diarias()).hasSize(2);
        assertThat(primeira.getFechamento()).isNotNull();
        assertThat(segunda.getFechamento()).isNotNull();

        ArgumentCaptor<Pagamento> captor = ArgumentCaptor.forClass(Pagamento.class);
        verify(pagamentos).save(captor.capture());
        Pagamento pagamento = captor.getValue();
        assertThat(pagamento.getStatus()).isEqualTo(StatusPagamento.PENDENTE);
        assertThat(pagamento.getValorTotal()).isEqualByComparingTo("250.00");
        assertThat(pagamento.getTipoChavePix()).isEqualTo(TipoChavePix.EMAIL);
        assertThat(pagamento.getChavePix()).isEqualTo("ana@obra.com");
    }

    private static Funcionario funcionario() {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(1L);
        funcionario.setNome("Ana");
        funcionario.setFuncao("Eletricista");
        funcionario.setValorDiaria(new BigDecimal("150.00"));
        funcionario.setTipoChavePix(TipoChavePix.EMAIL);
        funcionario.setChavePix("ana@obra.com");
        return funcionario;
    }

    private static Diaria diaria(Long id, LocalDate data, String valor) {
        Diaria diaria = new Diaria();
        diaria.setId(id);
        diaria.setFuncionario(funcionario());
        diaria.setData(data);
        diaria.setValor(new BigDecimal(valor));
        return diaria;
    }
}
