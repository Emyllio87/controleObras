package br.ueg.controleobras.servico;

import br.ueg.controleobras.dominio.Funcionario;
import br.ueg.controleobras.dominio.TipoChavePix;
import br.ueg.controleobras.dto.FuncionarioRequest;
import br.ueg.controleobras.dto.FuncionarioResponse;
import br.ueg.controleobras.erro.ErroValidacaoException;
import br.ueg.controleobras.erro.RegraNegocioException;
import br.ueg.controleobras.repositorio.DiariaRepository;
import br.ueg.controleobras.repositorio.FechamentoDiariaRepository;
import br.ueg.controleobras.repositorio.FuncionarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FuncionarioServiceTest {

    @Mock
    private FuncionarioRepository funcionarios;

    @Mock
    private DiariaRepository diarias;

    @Mock
    private FechamentoDiariaRepository fechamentos;

    private FuncionarioService service;

    @BeforeEach
    void setUp() {
        service = new FuncionarioService(funcionarios, diarias, fechamentos);
    }

    @Test
    void criaFuncionarioNormalizandoCampos() {
        when(funcionarios.existsByCpf("52998224725")).thenReturn(false);
        when(funcionarios.save(any(Funcionario.class))).thenAnswer(inv -> inv.getArgument(0));

        FuncionarioResponse resposta = service.criar(new FuncionarioRequest(
                "  João da Silva  ",
                "(11) 91234-5678",
                "  Pedreiro ",
                new BigDecimal("150.00"),
                TipoChavePix.CPF,
                "529.982.247-25",
                "529.982.247-25"));

        assertThat(resposta.nome()).isEqualTo("João da Silva");
        assertThat(resposta.telefone()).isEqualTo("11912345678");
        assertThat(resposta.funcao()).isEqualTo("Pedreiro");
        assertThat(resposta.valorDiaria()).isEqualByComparingTo("150.00");
        assertThat(resposta.cpf()).isEqualTo("52998224725");
        assertThat(resposta.chavePix()).isEqualTo("52998224725");
    }

    @Test
    void rejeitaCpfInvalido() {
        FuncionarioRequest dados = new FuncionarioRequest(
                "João", null, null, new BigDecimal("100.00"), null, null, "111.111.111-11");

        assertThatThrownBy(() -> service.criar(dados))
                .isInstanceOf(ErroValidacaoException.class)
                .hasMessageContaining("CPF inválido");

        verify(funcionarios, never()).save(any());
    }

    @Test
    void rejeitaCpfJaCadastrado() {
        when(funcionarios.existsByCpf("52998224725")).thenReturn(true);
        FuncionarioRequest dados = new FuncionarioRequest(
                "João", null, null, new BigDecimal("100.00"), null, null, "529.982.247-25");

        assertThatThrownBy(() -> service.criar(dados))
                .isInstanceOf(ErroValidacaoException.class)
                .hasMessageContaining("CPF já cadastrado");

        verify(funcionarios, never()).save(any());
    }

    @Test
    void bloqueiaExclusaoQuandoHaDiarias() {
        when(funcionarios.findById(1L)).thenReturn(Optional.of(funcionario()));
        when(diarias.existsByFuncionarioId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("possui diárias ou pagamentos");

        verify(funcionarios, never()).delete(any());
    }

    @Test
    void bloqueiaExclusaoQuandoHaFechamentos() {
        when(funcionarios.findById(1L)).thenReturn(Optional.of(funcionario()));
        when(diarias.existsByFuncionarioId(1L)).thenReturn(false);
        when(fechamentos.existsByFuncionarioId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOf(RegraNegocioException.class);

        verify(funcionarios, never()).delete(any());
    }

    @Test
    void excluiFuncionarioSemHistorico() {
        Funcionario funcionario = funcionario();
        when(funcionarios.findById(1L)).thenReturn(Optional.of(funcionario));
        when(diarias.existsByFuncionarioId(1L)).thenReturn(false);
        when(fechamentos.existsByFuncionarioId(1L)).thenReturn(false);

        service.excluir(1L);

        verify(funcionarios).delete(funcionario);
    }

    private static Funcionario funcionario() {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(1L);
        funcionario.setNome("João");
        funcionario.setCpf("52998224725");
        funcionario.setValorDiaria(new BigDecimal("100.00"));
        return funcionario;
    }
}
