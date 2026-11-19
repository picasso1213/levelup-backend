package com.levelup.backend.dto;

import java.util.List;

public record BoletaDto(
        Long id,
        String fecha,
        Long usuarioId,
        String usuarioNombre,
        String usuarioCorreo,
        int subtotal,
        int descuento,
        int total,
        int puntosGanados,
        List<DetalleBoletaDto> detalles
) {
}
