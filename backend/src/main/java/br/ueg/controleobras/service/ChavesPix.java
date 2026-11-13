package br.ueg.controleobras.service;

import br.ueg.controleobras.model.TipoChavePix;
import br.ueg.controleobras.exception.ErroValidacaoException;

import java.util.regex.Pattern;

public final class ChavesPix {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern ALEATORIA =
            Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

    private ChavesPix() {
    }

    public static String normalizar(TipoChavePix tipo, String chave) {
        if (tipo == null) {
            if (chave != null && !chave.isBlank()) {
                throw new ErroValidacaoException("tipoChavePix", "Informe o tipo da chave PIX");
            }
            return null;
        }
        if (chave == null || chave.isBlank()) {
            throw new ErroValidacaoException("chavePix", "Chave PIX é obrigatória");
        }
        return switch (tipo) {
            case CPF -> normalizarCpf(chave);
            case CNPJ -> normalizarCnpj(chave);
            case TELEFONE -> normalizarTelefone(chave);
            case EMAIL -> normalizarEmail(chave);
            case ALEATORIA -> normalizarAleatoria(chave);
        };
    }

    private static String normalizarCpf(String chave) {
        String digitos = Documentos.apenasDigitos(chave);
        if (!Documentos.cpfValido(digitos)) {
            throw new ErroValidacaoException("chavePix", "CPF da chave PIX inválido");
        }
        return digitos;
    }

    private static String normalizarCnpj(String chave) {
        String digitos = Documentos.apenasDigitos(chave);
        if (!Documentos.cnpjValido(digitos)) {
            throw new ErroValidacaoException("chavePix", "CNPJ da chave PIX inválido");
        }
        return digitos;
    }

    private static String normalizarTelefone(String chave) {
        String digitos = Documentos.apenasDigitos(chave);
        if (digitos.length() == 11) {
            digitos = "55" + digitos;
        }
        if (digitos.length() != 13 || !digitos.startsWith("55")) {
            throw new ErroValidacaoException("chavePix", "Chave PIX de telefone deve ter 11 dígitos após o código do país");
        }
        return digitos;
    }

    private static String normalizarEmail(String chave) {
        String valor = chave.trim().toLowerCase();
        if (!EMAIL.matcher(valor).matches()) {
            throw new ErroValidacaoException("chavePix", "E-mail inválido");
        }
        return valor;
    }

    private static String normalizarAleatoria(String chave) {
        String valor = chave.trim().toLowerCase();
        if (!ALEATORIA.matcher(valor).matches()) {
            throw new ErroValidacaoException("chavePix", "Chave aleatória deve estar no formato UUID");
        }
        return valor;
    }
}
