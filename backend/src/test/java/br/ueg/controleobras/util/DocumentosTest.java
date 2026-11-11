package br.ueg.controleobras.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentosTest {

    @Test
    void aceitaCpfValidoComMascara() {
        assertThat(Documentos.cpfValido("529.982.247-25")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "111.111.111-11",
            "529.982.247-24",
            "123",
            "",
            "5299822472a"
    })
    void rejeitaCpfInvalido(String cpf) {
        assertThat(Documentos.cpfValido(cpf)).isFalse();
    }

    @Test
    void aceitaCnpjValidoComMascara() {
        assertThat(Documentos.cnpjValido("11.222.333/0001-81")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "11.222.333/0001-82",
            "00.000.000/0000-00",
            "11.222.333/0001",
            "",
            "1122233300018a"
    })
    void rejeitaCnpjInvalido(String cnpj) {
        assertThat(Documentos.cnpjValido(cnpj)).isFalse();
    }

    @Test
    void removeCaracteresNaoNumericos() {
        assertThat(Documentos.apenasDigitos("+55 (11) 91234-5678")).isEqualTo("5511912345678");
        assertThat(Documentos.apenasDigitos(null)).isEmpty();
    }
}
