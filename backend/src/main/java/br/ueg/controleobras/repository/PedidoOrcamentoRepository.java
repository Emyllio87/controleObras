package br.ueg.controleobras.repository;

import br.ueg.controleobras.model.PedidoOrcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PedidoOrcamentoRepository extends JpaRepository<PedidoOrcamento, Long> {

    List<PedidoOrcamento> findAllByOrderByIdAsc();

    boolean existsByFornecedorId(Long fornecedorId);

    @Query(value = "select nextval('pedido_numero_seq')", nativeQuery = true)
    Long proximoNumero();
}
