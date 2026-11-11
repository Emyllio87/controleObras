package br.ueg.controleobras.repositorio;

import br.ueg.controleobras.dominio.Pagamento;
import br.ueg.controleobras.dominio.StatusPagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    List<Pagamento> findAllByOrderByPeriodoFimDescIdDesc();

    List<Pagamento> findAllByStatusOrderByPeriodoFimDescIdDesc(StatusPagamento status);
}
