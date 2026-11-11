package br.ueg.controleobras.repositorio;

import br.ueg.controleobras.dominio.Ferramenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FerramentaRepository extends JpaRepository<Ferramenta, Long> {

    List<Ferramenta> findAllByOrderByNomeAsc();
}
