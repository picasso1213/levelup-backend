package com.levelup.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductoRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 150, message = "Máximo 150 caracteres.")
        String nombre,

        @Size(max = 500, message = "Máximo 500 caracteres.")
        String descripcion,

        @NotNull(message = "El precio es obligatorio.")
        @Min(value = 0, message = "El precio no puede ser negativo.")
        Integer precio,

        @NotNull(message = "El stock es obligatorio.")
        @Min(value = 0, message = "El stock no puede ser negativo.")
        Integer stock,

        @Size(max = 255, message = "Máximo 255 caracteres.")
        String imagen,

        @NotNull(message = "La categoría es obligatoria.")
        Long categoriaId
) {
}
