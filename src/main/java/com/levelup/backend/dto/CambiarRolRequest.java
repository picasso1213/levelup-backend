package com.levelup.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record CambiarRolRequest(
        @NotBlank(message = "El rol es obligatorio.")
        String rol
) {
}
