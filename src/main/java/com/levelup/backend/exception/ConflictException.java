package com.levelup.backend.exception;

import java.util.Map;

public class ConflictException extends RuntimeException {

    private final Map<String, String> campos;

    public ConflictException(String mensaje) {
        this(mensaje, null);
    }

    public ConflictException(String mensaje, Map<String, String> campos) {
        super(mensaje);
        this.campos = campos;
    }

    public Map<String, String> getCampos() {
        return campos;
    }
}
