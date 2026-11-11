package br.ueg.controleobras.erro;

import java.util.Map;

public record ErroResponse(String mensagem, Map<String, String> campos) {

    public ErroResponse(String mensagem) {
        this(mensagem, null);
    }
}
