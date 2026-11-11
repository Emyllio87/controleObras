package br.ueg.controleobras.repositorio;

import br.ueg.controleobras.dominio.AutorizacaoPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AutorizacaoPagamentoRepository extends JpaRepository<AutorizacaoPagamento, Long> {

    List<AutorizacaoPagamento> findAllByOrderByNumeroDesc();

    boolean existsByPedidoId(Long pedidoId);

    @Query(value = "select nextval('autorizacao_numero_seq')", nativeQuery = true)
    Long proximoNumero();
}
