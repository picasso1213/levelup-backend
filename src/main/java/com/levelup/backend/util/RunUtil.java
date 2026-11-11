package com.levelup.backend.util;

/**
 * Utilidades para el RUN chileno (módulo 11, pesos 2 a 7).
 */
public final class RunUtil {

    private RunUtil() {
    }

    /** Quita puntos, guiones y espacios y pasa a mayúscula. Devuelve null si la entrada es null. */
    public static String normalizar(String run) {
        if (run == null) {
            return null;
        }
        return run.replace(".", "").replace("-", "").replace(" ", "").toUpperCase();
    }

    /** Indica si el RUN (con o sin puntos/guión) tiene formato y dígito verificador correctos. */
    public static boolean esValido(String run) {
        String limpio = normalizar(run);
        if (limpio == null || !limpio.matches("\\d{7,8}[0-9K]")) {
            return false;
        }
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char dv = limpio.charAt(limpio.length() - 1);
        return calcularDv(cuerpo) == dv;
    }

    static char calcularDv(String cuerpo) {
        int suma = 0;
        int peso = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * peso;
            peso = (peso == 7) ? 2 : peso + 1;
        }
        int resto = 11 - (suma % 11);
        if (resto == 11) {
            return '0';
        }
        if (resto == 10) {
            return 'K';
        }
        return (char) ('0' + resto);
    }
}
