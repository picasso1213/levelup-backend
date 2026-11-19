package com.levelup.backend.dto;

public record ProductoDto(
        Long id,
        String nombre,
        String categoria,
        Long categoriaId,
        Integer precio,
        Integer stock,
        String imagen,
        String descripcion
) {
}
