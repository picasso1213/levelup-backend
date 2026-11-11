package com.levelup.backend.exception;

import java.util.Map;

public class BusinessException extends RuntimeException {

    private final Map<String, String> campos;

    public BusinessException(String mensaje) {
        this(mensaje, null);
    }

    public BusinessException(String mensaje, Map<String, String> campos) {
        super(mensaje);
        this.campos = campos;
    }

    public Map<String, String> getCampos() {
        return campos;
    }
}
