package br.ueg.controleobras.repository;

import br.ueg.controleobras.model.Diaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiariaRepository extends JpaRepository<Diaria, Long> {

    List<Diaria> findByFuncionarioIdAndFechamentoIsNullOrderByIdAsc(Long funcionarioId);

    List<Diaria> findByFechamentoIdOrderByIdAsc(Long fechamentoId);

    boolean existsByFuncionarioId(Long funcionarioId);
}
