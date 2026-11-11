package br.ueg.controleobras.servico;

import br.ueg.controleobras.dominio.Fornecedor;
import br.ueg.controleobras.dominio.ItemPedido;
import br.ueg.controleobras.dominio.PedidoOrcamento;
import br.ueg.controleobras.dto.ItemRequest;
import br.ueg.controleobras.dto.PedidoRequest;
import br.ueg.controleobras.dto.PedidoResponse;
import br.ueg.controleobras.erro.ErroValidacaoException;
import br.ueg.controleobras.erro.RecursoNaoEncontradoException;
import br.ueg.controleobras.erro.RegraNegocioException;
import br.ueg.controleobras.pdf.RelatorioPdfService;
import br.ueg.controleobras.repositorio.AutorizacaoPagamentoRepository;
import br.ueg.controleobras.repositorio.FornecedorRepository;
import br.ueg.controleobras.repositorio.PedidoOrcamentoRepository;
import br.ueg.controleobras.util.PdfGerado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoOrcamentoRepository pedidos;
    private final FornecedorRepository fornecedores;
    private final AutorizacaoPagamentoRepository autorizacoes;
    private final RelatorioPdfService relatorios;

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar() {
        return pedidos.findAllByOrderByNumeroDesc().stream().map(PedidoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscar(Long id) {
        return PedidoResponse.de(buscarEntidade(id));
    }

    @Transactional
    public PedidoResponse criar(PedidoRequest dados) {
        PedidoOrcamento pedido = new PedidoOrcamento();
        pedido.setNumero(pedidos.proximoNumero());
        preencherCabecalho(pedido, dados);
        adicionarItens(pedido, dados.itens());
        return PedidoResponse.de(pedidos.save(pedido));
    }

    @Transactional
    public PedidoResponse atualizar(Long id, PedidoRequest dados) {
        PedidoOrcamento pedido = buscarEntidade(id);
        preencherCabecalho(pedido, dados);
        pedido.getItens().clear();
        pedidos.flush();
        adicionarItens(pedido, dados.itens());
        return PedidoResponse.de(pedidos.save(pedido));
    }

    @Transactional
    public void excluir(Long id) {
        PedidoOrcamento pedido = buscarEntidade(id);
        if (autorizacoes.existsByPedidoId(id)) {
            throw new RegraNegocioException("Pedido possui autorização de pagamento e não pode ser excluído");
        }
        pedidos.delete(pedido);
    }

    @Transactional(readOnly = true)
    public PdfGerado gerarPdf(Long id) {
        PedidoOrcamento pedido = buscarEntidade(id);
        String nome = "pedido-" + String.format("%06d", pedido.getNumero()) + ".pdf";
        return new PdfGerado(nome, relatorios.pedido(pedido));
    }

    public PedidoOrcamento buscarEntidade(Long id) {
        return pedidos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido de orçamento não encontrado"));
    }

    private void preencherCabecalho(PedidoOrcamento pedido, PedidoRequest dados) {
        Fornecedor fornecedor = fornecedores.findById(dados.fornecedorId())
                .orElseThrow(() -> new ErroValidacaoException("fornecedorId", "Selecione um fornecedor cadastrado"));
        pedido.setFornecedor(fornecedor);
        pedido.setDataEmissao(dados.dataEmissao() == null ? LocalDate.now() : dados.dataEmissao());
    }

    private void adicionarItens(PedidoOrcamento pedido, List<ItemRequest> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new ErroValidacaoException("itens", "Inclua ao menos um item no pedido");
        }
        int numero = 1;
        for (ItemRequest dados : itens) {
            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setNumero(numero++);
            item.setDescricao(dados.descricao().trim());
            item.setQuantidade(quantidade(dados.quantidade()));
            item.setUnidade(dados.unidade().trim().toUpperCase(Locale.ROOT));
            pedido.getItens().add(item);
        }
    }

    private static BigDecimal quantidade(BigDecimal valor) {
        return valor.setScale(3, RoundingMode.HALF_UP);
    }
}
