package br.ueg.controleobras.repository;

import br.ueg.controleobras.model.AutorizacaoPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AutorizacaoPagamentoRepository extends JpaRepository<AutorizacaoPagamento, Long> {

    List<AutorizacaoPagamento> findAllByOrderByIdAsc();

    boolean existsByPedidoId(Long pedidoId);

    @Query(value = "select nextval('autorizacao_numero_seq')", nativeQuery = true)
    Long proximoNumero();
}
