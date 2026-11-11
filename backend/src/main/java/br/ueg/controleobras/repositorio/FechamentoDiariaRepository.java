package br.ueg.controleobras.repositorio;

import br.ueg.controleobras.dominio.FechamentoDiaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FechamentoDiariaRepository extends JpaRepository<FechamentoDiaria, Long> {

    List<FechamentoDiaria> findAllByOrderByDataFechamentoDescIdDesc();

    boolean existsByFuncionarioId(Long funcionarioId);
}
