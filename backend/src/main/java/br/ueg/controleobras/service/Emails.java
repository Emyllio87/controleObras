package br.ueg.controleobras.service;

import java.util.Locale;
import java.util.regex.Pattern;

public final class Emails {

    private static final Pattern FORMATO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private Emails() {
    }

    public static String normalizar(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean valido(String email) {
        String valor = normalizar(email);
        return valor != null && FORMATO.matcher(valor).matches();
    }
}
