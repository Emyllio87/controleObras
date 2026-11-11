package br.ueg.controleobras.servico;

import br.ueg.controleobras.dominio.Funcionario;
import br.ueg.controleobras.dominio.TipoChavePix;
import br.ueg.controleobras.dto.FuncionarioRequest;
import br.ueg.controleobras.dto.FuncionarioResponse;
import br.ueg.controleobras.erro.ErroValidacaoException;
import br.ueg.controleobras.erro.RecursoNaoEncontradoException;
import br.ueg.controleobras.erro.RegraNegocioException;
import br.ueg.controleobras.repositorio.DiariaRepository;
import br.ueg.controleobras.repositorio.FechamentoDiariaRepository;
import br.ueg.controleobras.repositorio.FuncionarioRepository;
import br.ueg.controleobras.util.ChavesPix;
import br.ueg.controleobras.util.Documentos;
import br.ueg.controleobras.util.Telefones;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FuncionarioService {

    private final FuncionarioRepository funcionarios;
    private final DiariaRepository diarias;
    private final FechamentoDiariaRepository fechamentos;

    @Transactional(readOnly = true)
    public List<FuncionarioResponse> listar(TipoChavePix tipoChavePix) {
        List<Funcionario> lista = tipoChavePix == null
                ? funcionarios.findAllByOrderByNomeAsc()
                : funcionarios.findByTipoChavePixOrderByNomeAsc(tipoChavePix);
        return lista.stream().map(FuncionarioResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public FuncionarioResponse buscar(Long id) {
        return FuncionarioResponse.de(buscarEntidade(id));
    }

    @Transactional
    public FuncionarioResponse criar(FuncionarioRequest dados) {
        Funcionario funcionario = new Funcionario();
        preencher(funcionario, dados, null);
        return FuncionarioResponse.de(funcionarios.save(funcionario));
    }

    @Transactional
    public FuncionarioResponse atualizar(Long id, FuncionarioRequest dados) {
        Funcionario funcionario = buscarEntidade(id);
        preencher(funcionario, dados, id);
        return FuncionarioResponse.de(funcionarios.save(funcionario));
    }

    @Transactional
    public void excluir(Long id) {
        Funcionario funcionario = buscarEntidade(id);
        if (diarias.existsByFuncionarioId(id) || fechamentos.existsByFuncionarioId(id)) {
            throw new RegraNegocioException(
                    "Funcionário possui diárias ou pagamentos e não pode ser excluído");
        }
        funcionarios.delete(funcionario);
    }

    public Funcionario buscarEntidade(Long id) {
        return funcionarios.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Funcionário não encontrado"));
    }

    private void preencher(Funcionario funcionario, FuncionarioRequest dados, Long idAtual) {
        String nome = dados.nome().trim();
        if (nome.isEmpty()) {
            throw new ErroValidacaoException("nome", "Nome é obrigatório");
        }

        String cpf = Documentos.apenasDigitos(dados.cpf());
        if (!Documentos.cpfValido(cpf)) {
            throw new ErroValidacaoException("cpf", "CPF inválido");
        }
        boolean cpfEmUso = idAtual == null
                ? funcionarios.existsByCpf(cpf)
                : funcionarios.existsByCpfAndIdNot(cpf, idAtual);
        if (cpfEmUso) {
            throw new ErroValidacaoException("cpf", "CPF já cadastrado");
        }

        funcionario.setNome(nome);
        funcionario.setTelefone(Telefones.normalizar(dados.telefone()));
        funcionario.setFuncao(textoOuNulo(dados.funcao()));
        funcionario.setValorDiaria(arredondar(dados.valorDiaria()));
        funcionario.setTipoChavePix(dados.tipoChavePix());
        funcionario.setChavePix(ChavesPix.normalizar(dados.tipoChavePix(), dados.chavePix()));
        funcionario.setCpf(cpf);
    }

    private static BigDecimal arredondar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private static String textoOuNulo(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
