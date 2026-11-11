package br.ueg.controleobras.servico;

import br.ueg.controleobras.dominio.Fornecedor;
import br.ueg.controleobras.dominio.PedidoOrcamento;
import br.ueg.controleobras.dto.ItemRequest;
import br.ueg.controleobras.dto.PedidoRequest;
import br.ueg.controleobras.dto.PedidoResponse;
import br.ueg.controleobras.erro.ErroValidacaoException;
import br.ueg.controleobras.erro.RegraNegocioException;
import br.ueg.controleobras.pdf.RelatorioPdfService;
import br.ueg.controleobras.repositorio.AutorizacaoPagamentoRepository;
import br.ueg.controleobras.repositorio.FornecedorRepository;
import br.ueg.controleobras.repositorio.PedidoOrcamentoRepository;
import br.ueg.controleobras.util.PdfGerado;
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
class PedidoServiceTest {

    @Mock
    private PedidoOrcamentoRepository pedidos;

    @Mock
    private FornecedorRepository fornecedores;

    @Mock
    private AutorizacaoPagamentoRepository autorizacoes;

    @Mock
    private RelatorioPdfService relatorios;

    private PedidoService service;

    @BeforeEach
    void setUp() {
        service = new PedidoService(pedidos, fornecedores, autorizacoes, relatorios);
    }

    @Test
    void criaPedidoComNumeracaoENormalizacaoDosItens() {
        when(pedidos.proximoNumero()).thenReturn(7L);
        when(fornecedores.findById(1L)).thenReturn(Optional.of(fornecedor()));
        when(pedidos.save(any(PedidoOrcamento.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponse resposta = service.criar(new PedidoRequest(
                1L,
                LocalDate.of(2026, 2, 10),
                List.of(
                        new ItemRequest("  Cimento CP II  ", new BigDecimal("2.5"), "saco"),
                        new ItemRequest("Areia média", new BigDecimal("3"), " m3 "))));

        assertThat(resposta.numero()).isEqualTo(7L);
        assertThat(resposta.dataEmissao()).isEqualTo(LocalDate.of(2026, 2, 10));
        assertThat(resposta.fornecedor().razaoSocial()).isEqualTo("Construtora Alfa Ltda");
        assertThat(resposta.itens()).hasSize(2);
        assertThat(resposta.itens().get(0).numero()).isEqualTo(1);
        assertThat(resposta.itens().get(0).descricao()).isEqualTo("Cimento CP II");
        assertThat(resposta.itens().get(0).quantidade()).isEqualByComparingTo("2.500");
        assertThat(resposta.itens().get(0).unidade()).isEqualTo("SACO");
        assertThat(resposta.itens().get(1).numero()).isEqualTo(2);
        assertThat(resposta.itens().get(1).unidade()).isEqualTo("M3");
    }

    @Test
    void usaDataAtualQuandoDataNaoInformada() {
        when(pedidos.proximoNumero()).thenReturn(1L);
        when(fornecedores.findById(1L)).thenReturn(Optional.of(fornecedor()));
        when(pedidos.save(any(PedidoOrcamento.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponse resposta = service.criar(new PedidoRequest(
                1L, null,
                List.of(new ItemRequest("Cimento", BigDecimal.ONE, "saco"))));

        assertThat(resposta.dataEmissao()).isEqualTo(LocalDate.now());
    }

    @Test
    void rejeitaFornecedorInexistente() {
        when(pedidos.proximoNumero()).thenReturn(1L);
        when(fornecedores.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(new PedidoRequest(
                99L, LocalDate.now(),
                List.of(new ItemRequest("Cimento", BigDecimal.ONE, "saco")))))
                .isInstanceOf(ErroValidacaoException.class)
                .hasMessageContaining("Selecione um fornecedor cadastrado");

        verify(pedidos, never()).save(any());
    }

    @Test
    void rejeitaPedidoSemItens() {
        when(pedidos.proximoNumero()).thenReturn(1L);
        when(fornecedores.findById(1L)).thenReturn(Optional.of(fornecedor()));

        assertThatThrownBy(() -> service.criar(new PedidoRequest(
                1L, LocalDate.now(), List.of())))
                .isInstanceOf(ErroValidacaoException.class)
                .hasMessageContaining("Inclua ao menos um item no pedido");

        verify(pedidos, never()).save(any());
    }

    @Test
    void bloqueiaExclusaoDePedidoComAutorizacao() {
        PedidoOrcamento pedido = pedido();
        when(pedidos.findById(1L)).thenReturn(Optional.of(pedido));
        when(autorizacoes.existsByPedidoId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("possui autorização de pagamento");

        verify(pedidos, never()).delete(any());
    }

    @Test
    void excluiPedidoSemAutorizacao() {
        PedidoOrcamento pedido = pedido();
        when(pedidos.findById(1L)).thenReturn(Optional.of(pedido));
        when(autorizacoes.existsByPedidoId(1L)).thenReturn(false);

        service.excluir(1L);

        verify(pedidos).delete(pedido);
    }

    @Test
    void geraPdfComNomePadronizado() {
        PedidoOrcamento pedido = pedido();
        when(pedidos.findById(1L)).thenReturn(Optional.of(pedido));
        when(relatorios.pedido(pedido)).thenReturn(new byte[]{1, 2, 3});

        PdfGerado pdf = service.gerarPdf(1L);

        assertThat(pdf.nomeArquivo()).isEqualTo("pedido-000007.pdf");
        assertThat(pdf.conteudo()).containsExactly(1, 2, 3);
    }

    private static Fornecedor fornecedor() {
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setId(1L);
        fornecedor.setRazaoSocial("Construtora Alfa Ltda");
        fornecedor.setCnpj("11222333000181");
        return fornecedor;
    }

    private static PedidoOrcamento pedido() {
        PedidoOrcamento pedido = new PedidoOrcamento();
        pedido.setId(1L);
        pedido.setNumero(7L);
        pedido.setDataEmissao(LocalDate.now());
        pedido.setFornecedor(fornecedor());
        return pedido;
    }
}
