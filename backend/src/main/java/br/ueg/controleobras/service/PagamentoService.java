package br.ueg.controleobras.service;

import br.ueg.controleobras.model.Pagamento;
import br.ueg.controleobras.model.StatusPagamento;
import br.ueg.controleobras.model.PagamentoResponse;
import br.ueg.controleobras.exception.RecursoNaoEncontradoException;
import br.ueg.controleobras.repository.PagamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PagamentoRepository pagamentos;

    @Transactional(readOnly = true)
    public List<PagamentoResponse> listar(StatusPagamento status) {
        List<Pagamento> lista = status == null
                ? pagamentos.findAllByOrderByIdAsc()
                : pagamentos.findAllByStatusOrderByIdAsc(status);
        return lista.stream().map(PagamentoResponse::de).toList();
    }

    @Transactional
    public PagamentoResponse alterarStatus(Long id, StatusPagamento status) {
        Pagamento pagamento = pagamentos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pagamento não encontrado"));
        pagamento.setStatus(status);
        return PagamentoResponse.de(pagamentos.save(pagamento));
    }
}
