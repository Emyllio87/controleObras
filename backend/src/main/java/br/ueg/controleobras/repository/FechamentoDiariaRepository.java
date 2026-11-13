package br.ueg.controleobras.repository;

import br.ueg.controleobras.model.FechamentoDiaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FechamentoDiariaRepository extends JpaRepository<FechamentoDiaria, Long> {

    List<FechamentoDiaria> findAllByOrderByIdAsc();

    boolean existsByFuncionarioId(Long funcionarioId);
}
