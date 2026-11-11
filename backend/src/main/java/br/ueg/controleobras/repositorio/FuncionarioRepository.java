package br.ueg.controleobras.repositorio;

import br.ueg.controleobras.dominio.Funcionario;
import br.ueg.controleobras.dominio.TipoChavePix;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    List<Funcionario> findAllByOrderByNomeAsc();

    List<Funcionario> findByTipoChavePixOrderByNomeAsc(TipoChavePix tipoChavePix);

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);
}
