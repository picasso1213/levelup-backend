package com.levelup.backend.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunUtilTest {

    @Test
    void runsValidosSinFormato() {
        assertTrue(RunUtil.esValido("123456785"));
        assertTrue(RunUtil.esValido("111111111"));
        assertTrue(RunUtil.esValido("222222222"));
    }

    @Test
    void runInvalidoPorDigitoVerificador() {
        assertFalse(RunUtil.esValido("123456780"));
    }

    @Test
    void runConPuntosYGuion() {
        assertTrue(RunUtil.esValido("12.345.678-5"));
        assertTrue(RunUtil.esValido("12345678-5"));
        assertFalse(RunUtil.esValido("12.345.678-0"));
    }

    @Test
    void runConDigitoVerificadorK() {
        assertTrue(RunUtil.esValido("10000013K"));
        assertTrue(RunUtil.esValido("10.000.013-k"));
    }

    @Test
    void runVacioONuloEsInvalido() {
        assertFalse(RunUtil.esValido(null));
        assertFalse(RunUtil.esValido(""));
        assertFalse(RunUtil.esValido("abc"));
    }

    @Test
    void normalizarQuitaPuntosGuionYPasaAMayuscula() {
        assertEquals("123456785", RunUtil.normalizar("12.345.678-5"));
        assertEquals("10000013K", RunUtil.normalizar("10.000.013-k"));
        assertNull(RunUtil.normalizar(null));
    }
}
