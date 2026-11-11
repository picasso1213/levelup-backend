package com.levelup.backend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Formato único de error de la API: { "status": 404, "error": "mensaje", "campos": {...} }.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(int status, String error, Map<String, String> campos) {

    public static ErrorResponse de(int status, String error) {
        return new ErrorResponse(status, error, null);
    }

    public static ErrorResponse de(int status, String error, Map<String, String> campos) {
        if (campos == null || campos.isEmpty()) {
            return new ErrorResponse(status, error, null);
        }
        return new ErrorResponse(status, error, campos);
    }
}
