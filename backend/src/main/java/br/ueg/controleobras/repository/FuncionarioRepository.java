package br.ueg.controleobras.repository;

import br.ueg.controleobras.model.Funcionario;
import br.ueg.controleobras.model.TipoChavePix;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    List<Funcionario> findAllByOrderByIdAsc();

    List<Funcionario> findAllByOrderByNomeAsc();

    List<Funcionario> findByTipoChavePixOrderByIdAsc(TipoChavePix tipoChavePix);

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);
}
