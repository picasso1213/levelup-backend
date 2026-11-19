package com.levelup.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ActualizarPerfilRequest(
        @Size(max = 20, message = "Máximo 20 caracteres.")
        String telefono,

        @NotBlank(message = "La dirección es obligatoria.")
        @Size(max = 300, message = "Máximo 300 caracteres.")
        String direccion,

        @NotBlank(message = "La región es obligatoria.")
        @Size(max = 100, message = "Máximo 100 caracteres.")
        String region,

        @NotBlank(message = "La comuna es obligatoria.")
        @Size(max = 100, message = "Máximo 100 caracteres.")
        String comuna,

        List<String> preferencias
) {
}
