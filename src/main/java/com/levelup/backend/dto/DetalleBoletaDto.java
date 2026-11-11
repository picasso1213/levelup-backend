package com.levelup.backend.dto;

public record DetalleBoletaDto(
        Long productoId,
        String productoNombre,
        int cantidad,
        int precioUnitario,
        int subtotal
) {
}
