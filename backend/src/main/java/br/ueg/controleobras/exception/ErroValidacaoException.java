package br.ueg.controleobras.exception;

import java.util.Collections;
import java.util.Map;

public class ErroValidacaoException extends RuntimeException {

    private final Map<String, String> campos;

    public ErroValidacaoException(String campo, String mensagem) {
        super(mensagem);
        this.campos = Collections.singletonMap(campo, mensagem);
    }

    public ErroValidacaoException(Map<String, String> campos) {
        super("Verifique os campos informados");
        this.campos = campos;
    }

    public Map<String, String> getCampos() {
        return campos;
    }
}
