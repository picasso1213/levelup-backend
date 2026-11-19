package com.levelup.backend.dto;

import java.util.List;

public record UsuarioDto(
        Long id,
        String nombre,
        String apellidos,
        String run,
        String correo,
        String fechaNacimiento,
        String telefono,
        String region,
        String comuna,
        String direccion,
        String rol,
        boolean esDuoc,
        int descuento,
        List<String> preferencias,
        String codigoReferido,
        String referidoPor,
        int cantidadReferidos,
        int puntosLevelUp,
        String fechaRegistro
) {
}
