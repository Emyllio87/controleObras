package br.ueg.controleobras.repository;

import br.ueg.controleobras.model.Pagamento;
import br.ueg.controleobras.model.StatusPagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    List<Pagamento> findAllByOrderByIdAsc();

    List<Pagamento> findAllByStatusOrderByIdAsc(StatusPagamento status);
}
