package br.ueg.controleobras.repository;

import br.ueg.controleobras.model.Ferramenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FerramentaRepository extends JpaRepository<Ferramenta, Long> {

    List<Ferramenta> findAllByOrderByIdAsc();
}
