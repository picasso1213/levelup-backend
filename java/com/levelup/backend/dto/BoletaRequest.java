package com.levelup.backend.dto;

import jakarta.validation.Valid;

import java.util.List;

public record BoletaRequest(
        @Valid
        List<ItemBoletaRequest> items
) {
}
