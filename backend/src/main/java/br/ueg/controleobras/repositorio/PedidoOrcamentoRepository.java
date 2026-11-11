package br.ueg.controleobras.repositorio;

import br.ueg.controleobras.dominio.PedidoOrcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PedidoOrcamentoRepository extends JpaRepository<PedidoOrcamento, Long> {

    List<PedidoOrcamento> findAllByOrderByNumeroDesc();

    boolean existsByFornecedorId(Long fornecedorId);

    @Query(value = "select nextval('pedido_numero_seq')", nativeQuery = true)
    Long proximoNumero();
}
