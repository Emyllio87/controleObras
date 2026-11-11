package br.ueg.controleobras.util;

import br.ueg.controleobras.erro.ErroValidacaoException;

public final class Telefones {

    private Telefones() {
    }

    public static String normalizar(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            return null;
        }
        String digitos = Documentos.apenasDigitos(telefone);
        if (digitos.length() < 10 || digitos.length() > 11) {
            throw new ErroValidacaoException("telefone", "Telefone deve conter 10 ou 11 dígitos");
        }
        int ddd = Integer.parseInt(digitos.substring(0, 2));
        if (ddd < 11 || ddd % 10 == 0) {
            throw new ErroValidacaoException("telefone", "DDD inválido");
        }
        return digitos;
    }
}
