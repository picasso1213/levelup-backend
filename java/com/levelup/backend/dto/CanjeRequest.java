package com.levelup.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record CanjeRequest(
        @NotBlank(message = "El canje es obligatorio.")
        String idCanje
) {
}
