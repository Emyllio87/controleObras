package br.ueg.controleobras.servico;

import br.ueg.controleobras.dominio.Ferramenta;
import br.ueg.controleobras.dto.FerramentaRequest;
import br.ueg.controleobras.dto.FerramentaResponse;
import br.ueg.controleobras.erro.ErroValidacaoException;
import br.ueg.controleobras.erro.RecursoNaoEncontradoException;
import br.ueg.controleobras.pdf.RelatorioPdfService;
import br.ueg.controleobras.repositorio.FerramentaRepository;
import br.ueg.controleobras.util.PdfGerado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FerramentaServiceTest {

    @Mock
    private FerramentaRepository ferramentas;

    @Mock
    private RelatorioPdfService relatorios;

    private FerramentaService service;

    @BeforeEach
    void setUp() {
        service = new FerramentaService(ferramentas, relatorios);
    }

    @Test
    void criaFerramentaNormalizandoCampos() {
        when(ferramentas.save(any(Ferramenta.class))).thenAnswer(inv -> inv.getArgument(0));

        FerramentaResponse resposta = service.criar(new FerramentaRequest(
                "  Betoneira  ", new BigDecimal("2.5"), " un "));

        assertThat(resposta.nome()).isEqualTo("Betoneira");
        assertThat(resposta.quantidade()).isEqualByComparingTo("2.500");
        assertThat(resposta.unidade()).isEqualTo("UN");
    }

    @Test
    void rejeitaQuantidadeNula() {
        assertThatThrownBy(() -> service.criar(new FerramentaRequest("Betoneira", null, "UN")))
                .isInstanceOf(ErroValidacaoException.class)
                .hasMessageContaining("Quantidade deve ser maior que zero");

        verify(ferramentas, never()).save(any());
    }

    @Test
    void rejeitaQuantidadeMenorQueUmMilesimo() {
        assertThatThrownBy(() -> service.criar(new FerramentaRequest(
                "Betoneira", new BigDecimal("0.0001"), "UN")))
                .isInstanceOf(ErroValidacaoException.class)
                .hasMessageContaining("Quantidade deve ser maior que zero");

        verify(ferramentas, never()).save(any());
    }

    @Test
    void atualizaFerramentaExistente() {
        Ferramenta ferramenta = new Ferramenta();
        ferramenta.setId(1L);
        ferramenta.setNome("Betoneira");
        ferramenta.setQuantidade(new BigDecimal("2.000"));
        ferramenta.setUnidade("UN");
        when(ferramentas.findById(1L)).thenReturn(Optional.of(ferramenta));
        when(ferramentas.save(any(Ferramenta.class))).thenAnswer(inv -> inv.getArgument(0));

        FerramentaResponse resposta = service.atualizar(1L, new FerramentaRequest(
                "Betoneira 400L", new BigDecimal("3"), "un"));

        assertThat(resposta.nome()).isEqualTo("Betoneira 400L");
        assertThat(resposta.quantidade()).isEqualByComparingTo("3.000");
        assertThat(resposta.unidade()).isEqualTo("UN");
    }

    @Test
    void rejeitaFerramentaInexistente() {
        when(ferramentas.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Ferramenta não encontrada");
    }

    @Test
    void geraPdfDoAlmoxarifado() {
        Ferramenta ferramenta = new Ferramenta();
        ferramenta.setId(1L);
        ferramenta.setNome("Betoneira");
        ferramenta.setQuantidade(new BigDecimal("2.000"));
        ferramenta.setUnidade("UN");
        List<Ferramenta> lista = List.of(ferramenta);
        when(ferramentas.findAllByOrderByNomeAsc()).thenReturn(lista);
        when(relatorios.almoxarifado(lista)).thenReturn(new byte[]{9, 8});

        PdfGerado pdf = service.gerarPdf();

        assertThat(pdf.nomeArquivo()).isEqualTo("almoxarifado.pdf");
        assertThat(pdf.conteudo()).containsExactly(9, 8);
    }

    @Test
    void excluiFerramentaExistente() {
        Ferramenta ferramenta = new Ferramenta();
        ferramenta.setId(1L);
        when(ferramentas.findById(1L)).thenReturn(Optional.of(ferramenta));

        service.excluir(1L);

        verify(ferramentas).delete(ferramenta);
    }
}
