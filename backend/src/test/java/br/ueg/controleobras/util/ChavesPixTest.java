package br.ueg.controleobras.util;

import br.ueg.controleobras.dominio.TipoChavePix;
import br.ueg.controleobras.erro.ErroValidacaoException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChavesPixTest {

    @Test
    void semTipoESemChaveRetornaNulo() {
        assertThat(ChavesPix.normalizar(null, null)).isNull();
        assertThat(ChavesPix.normalizar(null, "   ")).isNull();
    }

    @Test
    void rejeitaChaveSemTipo() {
        assertThatThrownBy(() -> ChavesPix.normalizar(null, "529.982.247-25"))
                .isInstanceOf(ErroValidacaoException.class)
                .hasMessageContaining("tipo da chave PIX");
    }

    @Test
    void rejeitaChaveVaziaQuandoTipoInformado() {
        assertThatThrownBy(() -> ChavesPix.normalizar(TipoChavePix.CPF, " "))
                .isInstanceOf(ErroValidacaoException.class)
                .hasMessageContaining("Chave PIX é obrigatória");
    }

    @Test
    void normalizaCpfECnpjSomenteComDigitos() {
        assertThat(ChavesPix.normalizar(TipoChavePix.CPF, "529.982.247-25"))
                .isEqualTo("52998224725");
        assertThat(ChavesPix.normalizar(TipoChavePix.CNPJ, "11.222.333/0001-81"))
                .isEqualTo("11222333000181");
    }

    @Test
    void normalizaTelefoneComCodigoDoPais() {
        assertThat(ChavesPix.normalizar(TipoChavePix.TELEFONE, "(11) 91234-5678"))
                .isEqualTo("5511912345678");
        assertThat(ChavesPix.normalizar(TipoChavePix.TELEFONE, "+55 (11) 91234-5678"))
                .isEqualTo("5511912345678");
        assertThat(ChavesPix.normalizar(TipoChavePix.TELEFONE, "5511912345678"))
                .isEqualTo("5511912345678");
    }

    @Test
    void normalizaEmailEAleatoriaParaMinusculas() {
        assertThat(ChavesPix.normalizar(TipoChavePix.EMAIL, "  Contato@Empresa.COM "))
                .isEqualTo("contato@empresa.com");
        assertThat(ChavesPix.normalizar(
                TipoChavePix.ALEATORIA, "123E4567-E89B-12D3-A456-426614174000"))
                .isEqualTo("123e4567-e89b-12d3-a456-426614174000");
    }

    @Test
    void rejeitaChavesInvalidasPorTipo() {
        assertThatThrownBy(() -> ChavesPix.normalizar(TipoChavePix.CPF, "111.111.111-11"))
                .isInstanceOf(ErroValidacaoException.class);
        assertThatThrownBy(() -> ChavesPix.normalizar(TipoChavePix.CNPJ, "11.222.333/0001-82"))
                .isInstanceOf(ErroValidacaoException.class);
        assertThatThrownBy(() -> ChavesPix.normalizar(TipoChavePix.TELEFONE, "(11) 9123-4567"))
                .isInstanceOf(ErroValidacaoException.class);
        assertThatThrownBy(() -> ChavesPix.normalizar(TipoChavePix.EMAIL, "sem-arroba"))
                .isInstanceOf(ErroValidacaoException.class);
        assertThatThrownBy(() -> ChavesPix.normalizar(TipoChavePix.ALEATORIA, "123"))
                .isInstanceOf(ErroValidacaoException.class);
    }
}
