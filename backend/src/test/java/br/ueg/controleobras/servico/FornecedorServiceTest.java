package br.ueg.controleobras.servico;

import br.ueg.controleobras.dominio.Fornecedor;
import br.ueg.controleobras.dominio.TipoChavePix;
import br.ueg.controleobras.dto.FornecedorRequest;
import br.ueg.controleobras.dto.FornecedorResponse;
import br.ueg.controleobras.erro.ErroValidacaoException;
import br.ueg.controleobras.erro.RegraNegocioException;
import br.ueg.controleobras.repositorio.FornecedorRepository;
import br.ueg.controleobras.repositorio.PedidoOrcamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FornecedorServiceTest {

    @Mock
    private FornecedorRepository fornecedores;

    @Mock
    private PedidoOrcamentoRepository pedidos;

    private FornecedorService service;

    @BeforeEach
    void setUp() {
        service = new FornecedorService(fornecedores, pedidos);
    }

    @Test
    void criaFornecedorNormalizandoCampos() {
        when(fornecedores.existsByCnpj("11222333000181")).thenReturn(false);
        when(fornecedores.save(any(Fornecedor.class))).thenAnswer(inv -> inv.getArgument(0));

        FornecedorResponse resposta = service.criar(new FornecedorRequest(
                "  Construtora Alfa Ltda  ",
                "11.222.333/0001-81",
                "(11) 3333-4444",
                "  Maria ",
                TipoChavePix.EMAIL,
                "  Contato@Alfa.com "));

        assertThat(resposta.razaoSocial()).isEqualTo("Construtora Alfa Ltda");
        assertThat(resposta.cnpj()).isEqualTo("11222333000181");
        assertThat(resposta.telefone()).isEqualTo("1133334444");
        assertThat(resposta.nomeContato()).isEqualTo("Maria");
        assertThat(resposta.chavePix()).isEqualTo("contato@alfa.com");
    }

    @Test
    void rejeitaCnpjInvalido() {
        FornecedorRequest dados = new FornecedorRequest(
                "Fornecedor", "11.222.333/0001-82", null, null, null, null);

        assertThatThrownBy(() -> service.criar(dados))
                .isInstanceOf(ErroValidacaoException.class)
                .hasMessageContaining("CNPJ inválido");

        verify(fornecedores, never()).save(any());
    }

    @Test
    void rejeitaCnpjJaCadastrado() {
        when(fornecedores.existsByCnpj("11222333000181")).thenReturn(true);
        FornecedorRequest dados = new FornecedorRequest(
                "Fornecedor", "11.222.333/0001-81", null, null, null, null);

        assertThatThrownBy(() -> service.criar(dados))
                .isInstanceOf(ErroValidacaoException.class)
                .hasMessageContaining("CNPJ já cadastrado");

        verify(fornecedores, never()).save(any());
    }

    @Test
    void bloqueiaExclusaoDeFornecedorComPedidos() {
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setId(1L);
        when(fornecedores.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(pedidos.existsByFornecedorId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("possui pedidos de orçamento");

        verify(fornecedores, never()).delete(any());
    }

    @Test
    void excluiFornecedorSemPedidos() {
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setId(1L);
        when(fornecedores.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(pedidos.existsByFornecedorId(1L)).thenReturn(false);

        service.excluir(1L);

        verify(fornecedores).delete(fornecedor);
    }
}
