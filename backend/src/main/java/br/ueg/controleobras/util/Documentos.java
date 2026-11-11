package br.ueg.controleobras.util;

public final class Documentos {

    private Documentos() {
    }

    public static String apenasDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    public static boolean cpfValido(String cpf) {
        String digitos = apenasDigitos(cpf);
        if (digitos.length() != 11 || digitos.chars().distinct().count() == 1) {
            return false;
        }
        for (int posicao = 9; posicao < 11; posicao++) {
            int soma = 0;
            for (int i = 0; i < posicao; i++) {
                soma += (digitos.charAt(i) - '0') * (posicao + 1 - i);
            }
            int digito = 11 - (soma % 11);
            if (digito >= 10) {
                digito = 0;
            }
            if (digito != digitos.charAt(posicao) - '0') {
                return false;
            }
        }
        return true;
    }

    public static boolean cnpjValido(String cnpj) {
        String digitos = apenasDigitos(cnpj);
        if (digitos.length() != 14 || digitos.chars().distinct().count() == 1) {
            return false;
        }
        int[] pesosPrimeiro = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesosSegundo = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        if (digitoVerificador(digitos, pesosPrimeiro) != digitos.charAt(12) - '0') {
            return false;
        }
        return digitoVerificador(digitos, pesosSegundo) == digitos.charAt(13) - '0';
    }

    private static int digitoVerificador(String digitos, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (digitos.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
