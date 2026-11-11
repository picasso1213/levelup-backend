package com.levelup.backend.dto.validacion;

import com.levelup.backend.util.RunUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RunChilenoValidator implements ConstraintValidator<RunChileno, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext contexto) {
        return valor == null || RunUtil.esValido(valor);
    }
}
