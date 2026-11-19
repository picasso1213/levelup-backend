package com.levelup.backend.util;

import java.security.SecureRandom;
import java.text.Normalizer;

/**
 * Genera códigos de referido: 2 iniciales (nombre y primer apellido) + 4 caracteres alfanuméricos.
 */
public final class CodigoReferidoUtil {

    private static final String ALFANUMERICOS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private CodigoReferidoUtil() {
    }

    public static String generar(String nombre, String apellidos) {
        StringBuilder sb = new StringBuilder();
        sb.append(inicial(nombre));
        sb.append(inicial(primeraPalabra(apellidos)));
        for (int i = 0; i < 4; i++) {
            sb.append(ALFANUMERICOS.charAt(ALEATORIO.nextInt(ALFANUMERICOS.length())));
        }
        return sb.toString();
    }

    private static String primeraPalabra(String texto) {
        if (texto == null) {
            return "";
        }
        String limpio = texto.trim();
        int espacio = limpio.indexOf(' ');
        return espacio < 0 ? limpio : limpio.substring(0, espacio);
    }

    private static char inicial(String texto) {
        if (texto != null) {
            String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "");
            for (int i = 0; i < sinTildes.length(); i++) {
                char c = Character.toUpperCase(sinTildes.charAt(i));
                if (c >= 'A' && c <= 'Z') {
                    return c;
                }
            }
        }
        return 'X';
    }
}
