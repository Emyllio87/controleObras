package br.ueg.controleobras.repositorio;

import br.ueg.controleobras.dominio.Diaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiariaRepository extends JpaRepository<Diaria, Long> {

    List<Diaria> findByFuncionarioIdAndFechamentoIsNullOrderByDataAsc(Long funcionarioId);

    List<Diaria> findByFechamentoIdOrderByDataAsc(Long fechamentoId);

    boolean existsByFuncionarioId(Long funcionarioId);
}
