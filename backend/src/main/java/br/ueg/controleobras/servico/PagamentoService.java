package br.ueg.controleobras.servico;

import br.ueg.controleobras.dominio.Pagamento;
import br.ueg.controleobras.dominio.StatusPagamento;
import br.ueg.controleobras.dto.PagamentoResponse;
import br.ueg.controleobras.erro.RecursoNaoEncontradoException;
import br.ueg.controleobras.repositorio.PagamentoRepository;
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
                ? pagamentos.findAllByOrderByPeriodoFimDescIdDesc()
                : pagamentos.findAllByStatusOrderByPeriodoFimDescIdDesc(status);
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
