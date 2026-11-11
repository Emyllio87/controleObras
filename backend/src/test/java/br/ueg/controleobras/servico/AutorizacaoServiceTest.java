package br.ueg.controleobras.servico;

import br.ueg.controleobras.arquivo.AnexoArquivo;
import br.ueg.controleobras.arquivo.ArquivoArmazenado;
import br.ueg.controleobras.arquivo.ArquivoService;
import br.ueg.controleobras.dominio.AutorizacaoPagamento;
import br.ueg.controleobras.dominio.Fornecedor;
import br.ueg.controleobras.dominio.PedidoOrcamento;
import br.ueg.controleobras.dto.AutorizacaoResponse;
import br.ueg.controleobras.erro.ErroValidacaoException;
import br.ueg.controleobras.erro.RecursoNaoEncontradoException;
import br.ueg.controleobras.erro.RegraNegocioException;
import br.ueg.controleobras.pdf.RelatorioPdfService;
import br.ueg.controleobras.repositorio.AutorizacaoPagamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutorizacaoServiceTest {

    @Mock
    private AutorizacaoPagamentoRepository autorizacoes;

    @Mock
    private PedidoService pedidoService;

    @Mock
    private ArquivoService arquivos;

    @Mock
    private RelatorioPdfService relatorios;

    private AutorizacaoService service;

    @BeforeEach
    void setUp() {
        service = new AutorizacaoService(autorizacoes, pedidoService, arquivos, relatorios);
    }

    @Test
    void rejeitaPedidoQueJaPossuiAutorizacao() {
        when(pedidoService.buscarEntidade(1L)).thenReturn(pedido());
        when(autorizacoes.existsByPedidoId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.criar(
                1L, new BigDecimal("100.00"), LocalDate.now(), null))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("já possui autorização");

        verify(arquivos, never()).salvar(any());
        verify(autorizacoes, never()).save(any());
    }

    @Test
    void rejeitaValorMenorQueUmCentavo() {
        when(pedidoService.buscarEntidade(1L)).thenReturn(pedido());
        when(autorizacoes.existsByPedidoId(1L)).thenReturn(false);

        assertThatThrownBy(() -> service.criar(
                1L, new BigDecimal("0.00"), LocalDate.now(), null))
                .isInstanceOf(ErroValidacaoException.class)
                .hasMessageContaining("Valor deve ser maior que zero");

        verify(arquivos, never()).salvar(any());
        verify(autorizacoes, never()).save(any());
    }

    @Test
    void criaAutorizacaoComAnexo() {
        MultipartFile arquivo = mock(MultipartFile.class);
        when(pedidoService.buscarEntidade(1L)).thenReturn(pedido());
        when(autorizacoes.existsByPedidoId(1L)).thenReturn(false);
        when(autorizacoes.proximoNumero()).thenReturn(10L);
        when(arquivos.salvar(arquivo)).thenReturn(
                new ArquivoArmazenado("orcamento.pdf", "uuid.pdf", "application/pdf", 500L));
        when(autorizacoes.save(any(AutorizacaoPagamento.class))).thenAnswer(inv -> {
            AutorizacaoPagamento autorizacao = inv.getArgument(0);
            autorizacao.setId(3L);
            return autorizacao;
        });

        AutorizacaoResponse resposta = service.criar(
                1L, new BigDecimal("100.5"), LocalDate.of(2026, 3, 4), arquivo);

        assertThat(resposta.id()).isEqualTo(3L);
        assertThat(resposta.numero()).isEqualTo(10L);
        assertThat(resposta.pedidoId()).isEqualTo(1L);
        assertThat(resposta.pedidoNumero()).isEqualTo(7L);
        assertThat(resposta.fornecedor()).isEqualTo("Construtora Alfa Ltda");
        assertThat(resposta.valor()).isEqualByComparingTo("100.50");
        assertThat(resposta.dataEmissao()).isEqualTo(LocalDate.of(2026, 3, 4));
        assertThat(resposta.arquivoNome()).isEqualTo("orcamento.pdf");
        assertThat(resposta.arquivoTipo()).isEqualTo("application/pdf");
        assertThat(resposta.arquivoTamanho()).isEqualTo(500L);
        assertThat(resposta.possuiAnexo()).isTrue();
    }

    @Test
    void usaDataAtualQuandoNaoInformada() {
        when(pedidoService.buscarEntidade(1L)).thenReturn(pedido());
        when(autorizacoes.existsByPedidoId(1L)).thenReturn(false);
        when(autorizacoes.proximoNumero()).thenReturn(10L);
        when(autorizacoes.save(any(AutorizacaoPagamento.class))).thenAnswer(inv -> inv.getArgument(0));

        AutorizacaoResponse resposta = service.criar(
                1L, new BigDecimal("50.00"), null, null);

        assertThat(resposta.dataEmissao()).isEqualTo(LocalDate.now());
        assertThat(resposta.possuiAnexo()).isFalse();
    }

    @Test
    void atualizarSubstituiAnexoAnterior() {
        AutorizacaoPagamento autorizacao = autorizacao();
        MultipartFile arquivo = mock(MultipartFile.class);
        when(autorizacoes.findById(3L)).thenReturn(Optional.of(autorizacao));
        when(arquivos.salvar(arquivo)).thenReturn(
                new ArquivoArmazenado("novo.pdf", "novo-uuid.pdf", "application/pdf", 900L));
        when(autorizacoes.save(any(AutorizacaoPagamento.class))).thenAnswer(inv -> inv.getArgument(0));

        AutorizacaoResponse resposta = service.atualizar(
                3L, new BigDecimal("200.00"), null, arquivo);

        assertThat(resposta.valor()).isEqualByComparingTo("200.00");
        assertThat(resposta.arquivoNome()).isEqualTo("novo.pdf");
        assertThat(resposta.arquivoTamanho()).isEqualTo(900L);
        verify(arquivos).excluir("antigo.pdf");
    }

    @Test
    void atualizarSemArquivoMantemAnexo() {
        AutorizacaoPagamento autorizacao = autorizacao();
        when(autorizacoes.findById(3L)).thenReturn(Optional.of(autorizacao));
        when(autorizacoes.save(any(AutorizacaoPagamento.class))).thenAnswer(inv -> inv.getArgument(0));

        AutorizacaoResponse resposta = service.atualizar(
                3L, new BigDecimal("300.00"), LocalDate.of(2026, 4, 1), null);

        assertThat(resposta.arquivoNome()).isEqualTo("antigo.pdf");
        assertThat(resposta.dataEmissao()).isEqualTo(LocalDate.of(2026, 4, 1));
        verify(arquivos, never()).excluir(any());
    }

    @Test
    void baixarAnexoInexistenteRetornaErro() {
        AutorizacaoPagamento autorizacao = autorizacao();
        autorizacao.setArquivoCaminho(null);
        when(autorizacoes.findById(3L)).thenReturn(Optional.of(autorizacao));

        assertThatThrownBy(() -> service.baixarAnexo(3L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Autorização sem anexo");
    }

    @Test
    void baixarAnexoExistenteEntregaArquivo() {
        AutorizacaoPagamento autorizacao = autorizacao();
        Resource resource = mock(Resource.class);
        when(autorizacoes.findById(3L)).thenReturn(Optional.of(autorizacao));
        when(arquivos.carregar("antigo.pdf")).thenReturn(resource);

        AnexoArquivo anexo = service.baixarAnexo(3L);

        assertThat(anexo.nome()).isEqualTo("antigo.pdf");
        assertThat(anexo.tipo()).isEqualTo("application/pdf");
        assertThat(anexo.recurso()).isSameAs(resource);
    }

    @Test
    void excluirRemoveArquivoERegistro() {
        AutorizacaoPagamento autorizacao = autorizacao();
        when(autorizacoes.findById(3L)).thenReturn(Optional.of(autorizacao));

        service.excluir(3L);

        verify(arquivos).excluir("antigo.pdf");
        verify(autorizacoes).delete(autorizacao);
    }

    @Test
    void gerarPdfEnviaConteudoDoAnexoParaORelatorio() {
        AutorizacaoPagamento autorizacao = autorizacao();
        byte[] conteudo = {1, 2, 3};
        byte[] pdf = {4, 5, 6};
        when(autorizacoes.findById(3L)).thenReturn(Optional.of(autorizacao));
        when(arquivos.carregar("antigo.pdf")).thenReturn(new ByteArrayResource(conteudo));
        when(relatorios.autorizacao(eq(autorizacao), aryEq(conteudo), eq("application/pdf")))
                .thenReturn(pdf);

        var gerado = service.gerarPdf(3L);

        assertThat(gerado.nomeArquivo()).isEqualTo("autorizacao-000010.pdf");
        assertThat(gerado.conteudo()).isSameAs(pdf);
    }

    @Test
    void gerarPdfSemAnexoNaoCarregaArquivo() {
        AutorizacaoPagamento autorizacao = autorizacao();
        autorizacao.setArquivoCaminho(null);
        when(autorizacoes.findById(3L)).thenReturn(Optional.of(autorizacao));
        when(relatorios.autorizacao(eq(autorizacao), eq(null), eq("application/pdf"))).thenReturn(new byte[]{7});

        service.gerarPdf(3L);

        verify(arquivos, never()).carregar(any());
    }

    private static PedidoOrcamento pedido() {
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setId(1L);
        fornecedor.setRazaoSocial("Construtora Alfa Ltda");
        fornecedor.setCnpj("11222333000181");

        PedidoOrcamento pedido = new PedidoOrcamento();
        pedido.setId(1L);
        pedido.setNumero(7L);
        pedido.setDataEmissao(LocalDate.now());
        pedido.setFornecedor(fornecedor);
        return pedido;
    }

    private static AutorizacaoPagamento autorizacao() {
        AutorizacaoPagamento autorizacao = new AutorizacaoPagamento();
        autorizacao.setId(3L);
        autorizacao.setNumero(10L);
        autorizacao.setPedido(pedido());
        autorizacao.setValor(new BigDecimal("100.00"));
        autorizacao.setDataEmissao(LocalDate.of(2026, 3, 4));
        autorizacao.setArquivoNome("antigo.pdf");
        autorizacao.setArquivoCaminho("antigo.pdf");
        autorizacao.setArquivoTipo("application/pdf");
        autorizacao.setArquivoTamanho(400L);
        return autorizacao;
    }
}
