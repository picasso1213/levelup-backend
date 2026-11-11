package com.levelup.backend.dto.validacion;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class MayorDeEdadValidator implements ConstraintValidator<MayorDeEdad, LocalDate> {

    @Override
    public boolean isValid(LocalDate fecha, ConstraintValidatorContext contexto) {
        if (fecha == null) {
            return true;
        }
        return !fecha.plusYears(18).isAfter(LocalDate.now());
    }
}
