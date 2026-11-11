package br.ueg.controleobras.servico;

import br.ueg.controleobras.arquivo.AnexoArquivo;
import br.ueg.controleobras.arquivo.ArquivoArmazenado;
import br.ueg.controleobras.arquivo.ArquivoService;
import br.ueg.controleobras.dominio.AutorizacaoPagamento;
import br.ueg.controleobras.dominio.PedidoOrcamento;
import br.ueg.controleobras.dto.AutorizacaoResponse;
import br.ueg.controleobras.erro.ErroValidacaoException;
import br.ueg.controleobras.erro.RecursoNaoEncontradoException;
import br.ueg.controleobras.erro.RegraNegocioException;
import br.ueg.controleobras.pdf.RelatorioPdfService;
import br.ueg.controleobras.repositorio.AutorizacaoPagamentoRepository;
import br.ueg.controleobras.util.PdfGerado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AutorizacaoService {

    private final AutorizacaoPagamentoRepository autorizacoes;
    private final PedidoService pedidoService;
    private final ArquivoService arquivos;
    private final RelatorioPdfService relatorios;

    @Transactional(readOnly = true)
    public List<AutorizacaoResponse> listar() {
        return autorizacoes.findAllByOrderByNumeroDesc().stream()
                .map(AutorizacaoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public AutorizacaoResponse buscar(Long id) {
        return AutorizacaoResponse.de(buscarEntidade(id));
    }

    @Transactional
    public AutorizacaoResponse criar(Long pedidoId, BigDecimal valor, LocalDate dataEmissao, MultipartFile arquivo) {
        PedidoOrcamento pedido = pedidoService.buscarEntidade(pedidoId);
        if (autorizacoes.existsByPedidoId(pedidoId)) {
            throw new RegraNegocioException("Este pedido já possui autorização de pagamento");
        }
        BigDecimal valorValidado = validarValor(valor);
        ArquivoArmazenado anexo = arquivos.salvar(arquivo);

        AutorizacaoPagamento autorizacao = new AutorizacaoPagamento();
        autorizacao.setNumero(autorizacoes.proximoNumero());
        autorizacao.setPedido(pedido);
        autorizacao.setValor(valorValidado);
        autorizacao.setDataEmissao(dataEmissao == null ? LocalDate.now() : dataEmissao);
        aplicarAnexo(autorizacao, anexo);
        try {
            return AutorizacaoResponse.de(autorizacoes.save(autorizacao));
        } catch (RuntimeException e) {
            arquivos.excluir(anexo == null ? null : anexo.caminho());
            throw e;
        }
    }

    @Transactional
    public AutorizacaoResponse atualizar(Long id, BigDecimal valor, LocalDate dataEmissao, MultipartFile arquivo) {
        AutorizacaoPagamento autorizacao = buscarEntidade(id);
        autorizacao.setValor(validarValor(valor));
        if (dataEmissao != null) {
            autorizacao.setDataEmissao(dataEmissao);
        }
        ArquivoArmazenado anexo = arquivos.salvar(arquivo);
        if (anexo != null) {
            arquivos.excluir(autorizacao.getArquivoCaminho());
            aplicarAnexo(autorizacao, anexo);
        }
        return AutorizacaoResponse.de(autorizacoes.save(autorizacao));
    }

    @Transactional
    public void excluir(Long id) {
        AutorizacaoPagamento autorizacao = buscarEntidade(id);
        arquivos.excluir(autorizacao.getArquivoCaminho());
        autorizacoes.delete(autorizacao);
    }

    @Transactional(readOnly = true)
    public PdfGerado gerarPdf(Long id) {
        AutorizacaoPagamento autorizacao = buscarEntidade(id);
        String nome = "autorizacao-" + String.format("%06d", autorizacao.getNumero()) + ".pdf";
        byte[] anexo = lerAnexo(autorizacao);
        return new PdfGerado(nome, relatorios.autorizacao(autorizacao, anexo, autorizacao.getArquivoTipo()));
    }

    private byte[] lerAnexo(AutorizacaoPagamento autorizacao) {
        if (autorizacao.getArquivoCaminho() == null) {
            return null;
        }
        try (InputStream entrada = arquivos.carregar(autorizacao.getArquivoCaminho()).getInputStream()) {
            return entrada.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler o anexo da autorização", e);
        }
    }

    @Transactional(readOnly = true)
    public AnexoArquivo baixarAnexo(Long id) {
        AutorizacaoPagamento autorizacao = buscarEntidade(id);
        if (autorizacao.getArquivoCaminho() == null) {
            throw new RecursoNaoEncontradoException("Autorização sem anexo");
        }
        return new AnexoArquivo(
                autorizacao.getArquivoNome(),
                autorizacao.getArquivoTipo(),
                arquivos.carregar(autorizacao.getArquivoCaminho()));
    }

    public AutorizacaoPagamento buscarEntidade(Long id) {
        return autorizacoes.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Autorização de pagamento não encontrada"));
    }

    private static BigDecimal validarValor(BigDecimal valor) {
        if (valor == null) {
            throw new ErroValidacaoException("valor", "Valor é obrigatório");
        }
        if (valor.compareTo(new BigDecimal("0.01")) < 0) {
            throw new ErroValidacaoException("valor", "Valor deve ser maior que zero");
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private static void aplicarAnexo(AutorizacaoPagamento autorizacao, ArquivoArmazenado anexo) {
        if (anexo == null) {
            return;
        }
        autorizacao.setArquivoNome(anexo.nomeOriginal());
        autorizacao.setArquivoCaminho(anexo.caminho());
        autorizacao.setArquivoTipo(anexo.tipo());
        autorizacao.setArquivoTamanho(anexo.tamanho());
    }
}
