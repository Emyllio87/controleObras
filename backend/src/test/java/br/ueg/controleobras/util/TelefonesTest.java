package br.ueg.controleobras.util;

import br.ueg.controleobras.erro.ErroValidacaoException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelefonesTest {

    @Test
    void trataTelefoneOpcional() {
        assertThat(Telefones.normalizar(null)).isNull();
        assertThat(Telefones.normalizar("   ")).isNull();
    }

    @Test
    void normalizaFixoECelular() {
        assertThat(Telefones.normalizar("(11) 3333-4444")).isEqualTo("1133334444");
        assertThat(Telefones.normalizar("(11) 93333-4444")).isEqualTo("11933334444");
    }

    @Test
    void rejeitaTamanhoInvalido() {
        assertThatThrownBy(() -> Telefones.normalizar("(11) 333-4444"))
                .isInstanceOf(ErroValidacaoException.class)
                .hasMessageContaining("10 ou 11 dígitos");
        assertThatThrownBy(() -> Telefones.normalizar("(11) 93333-44445"))
                .isInstanceOf(ErroValidacaoException.class)
                .hasMessageContaining("10 ou 11 dígitos");
    }

    @Test
    void rejeitaDddInvalido() {
        assertThatThrownBy(() -> Telefones.normalizar("(10) 3333-4444"))
                .isInstanceOf(ErroValidacaoException.class)
                .hasMessageContaining("DDD inválido");
        assertThatThrownBy(() -> Telefones.normalizar("(20) 3333-4444"))
                .isInstanceOf(ErroValidacaoException.class)
                .hasMessageContaining("DDD inválido");
    }
}
