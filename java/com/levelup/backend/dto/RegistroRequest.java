package com.levelup.backend.dto;

import com.levelup.backend.dto.validacion.MayorDeEdad;
import com.levelup.backend.dto.validacion.RunChileno;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegistroRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 100, message = "Máximo 100 caracteres.")
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios.")
        @Size(max = 100, message = "Máximo 100 caracteres.")
        String apellidos,

        @NotBlank(message = "Ingresa un RUN válido (ej: 123456785).")
        @RunChileno
        String run,

        @NotBlank(message = "El correo es obligatorio.")
        @Size(max = 120, message = "Máximo 120 caracteres.")
        @Pattern(regexp = "(?i)^[^@\\s]+@(duoc\\.cl|profesor\\.duoc\\.cl|gmail\\.com)$",
                message = "Usa un correo @duoc.cl, @profesor.duoc.cl o @gmail.com.")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 4, message = "Mínimo 4 caracteres.")
        @Pattern(regexp = "^.{0,72}$", message = "Máximo 72 caracteres.")
        String password,

        @NotNull(message = "La fecha de nacimiento es obligatoria.")
        @MayorDeEdad
        LocalDate fechaNacimiento,

        @Size(max = 20, message = "Máximo 20 caracteres.")
        String telefono,

        @NotBlank(message = "La región es obligatoria.")
        @Size(max = 100, message = "Máximo 100 caracteres.")
        String region,

        @NotBlank(message = "La comuna es obligatoria.")
        @Size(max = 100, message = "Máximo 100 caracteres.")
        String comuna,

        @NotBlank(message = "La dirección es obligatoria.")
        @Size(max = 300, message = "Máximo 300 caracteres.")
        String direccion,

        @Size(max = 20, message = "Máximo 20 caracteres.")
        String codigoReferido
) {
}
