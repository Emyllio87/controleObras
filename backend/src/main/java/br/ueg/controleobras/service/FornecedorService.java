package br.ueg.controleobras.service;

import br.ueg.controleobras.model.Fornecedor;
import br.ueg.controleobras.model.FornecedorRequest;
import br.ueg.controleobras.model.FornecedorResponse;
import br.ueg.controleobras.exception.ErroValidacaoException;
import br.ueg.controleobras.exception.RecursoNaoEncontradoException;
import br.ueg.controleobras.exception.RegraNegocioException;
import br.ueg.controleobras.repository.FornecedorRepository;
import br.ueg.controleobras.repository.PedidoOrcamentoRepository;
import br.ueg.controleobras.service.ChavesPix;
import br.ueg.controleobras.service.Documentos;
import br.ueg.controleobras.service.Telefones;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FornecedorService {

    private final FornecedorRepository fornecedores;
    private final PedidoOrcamentoRepository pedidos;

    @Transactional(readOnly = true)
    public List<FornecedorResponse> listar() {
        return fornecedores.findAllByOrderByIdAsc().stream()
                .map(FornecedorResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public FornecedorResponse buscar(Long id) {
        return FornecedorResponse.de(buscarEntidade(id));
    }

    @Transactional
    public FornecedorResponse criar(FornecedorRequest dados) {
        Fornecedor fornecedor = new Fornecedor();
        preencher(fornecedor, dados, null);
        return FornecedorResponse.de(fornecedores.save(fornecedor));
    }

    @Transactional
    public FornecedorResponse atualizar(Long id, FornecedorRequest dados) {
        Fornecedor fornecedor = buscarEntidade(id);
        preencher(fornecedor, dados, id);
        return FornecedorResponse.de(fornecedores.save(fornecedor));
    }

    @Transactional
    public void excluir(Long id) {
        Fornecedor fornecedor = buscarEntidade(id);
        if (pedidos.existsByFornecedorId(id)) {
            throw new RegraNegocioException("Fornecedor possui pedidos de orçamento e não pode ser excluído");
        }
        fornecedores.delete(fornecedor);
    }

    public Fornecedor buscarEntidade(Long id) {
        return fornecedores.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor não encontrado"));
    }

    private void preencher(Fornecedor fornecedor, FornecedorRequest dados, Long idAtual) {
        String razaoSocial = dados.razaoSocial().trim();
        if (razaoSocial.isEmpty()) {
            throw new ErroValidacaoException("razaoSocial", "Razão social é obrigatória");
        }

        String cnpj = Documentos.apenasDigitos(dados.cnpj());
        if (!Documentos.cnpjValido(cnpj)) {
            throw new ErroValidacaoException("cnpj", "CNPJ inválido");
        }
        boolean cnpjEmUso = idAtual == null
                ? fornecedores.existsByCnpj(cnpj)
                : fornecedores.existsByCnpjAndIdNot(cnpj, idAtual);
        if (cnpjEmUso) {
            throw new ErroValidacaoException("cnpj", "CNPJ já cadastrado");
        }

        fornecedor.setRazaoSocial(razaoSocial);
        fornecedor.setCnpj(cnpj);
        fornecedor.setTelefone(Telefones.normalizar(dados.telefone()));
        fornecedor.setNomeContato(textoOuNulo(dados.nomeContato()));
        fornecedor.setTipoChavePix(dados.tipoChavePix());
        fornecedor.setChavePix(ChavesPix.normalizar(dados.tipoChavePix(), dados.chavePix()));
    }

    private static String textoOuNulo(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
