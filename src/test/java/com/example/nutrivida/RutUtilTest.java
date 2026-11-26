package com.example.nutrivida;

import com.example.nutrivida.util.RutUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de la validación del RUT chileno (módulo 11).
 */
class RutUtilTest {

    @Test
    @DisplayName("Calcula correctamente el dígito verificador, incluyendo K y 0")
    void testCalcularDv() {
        assertEquals('5', RutUtil.calcularDv("12345678"));
        assertEquals('7', RutUtil.calcularDv("18765432"));
        assertEquals('0', RutUtil.calcularDv("15432987"));
        assertEquals('K', RutUtil.calcularDv("10000013"));
    }

    @Test
    @DisplayName("Acepta RUT válidos con o sin puntos y guion")
    void testRutValidos() {
        assertTrue(RutUtil.esValido("12.345.678-5"));
        assertTrue(RutUtil.esValido("123456785"));
        assertTrue(RutUtil.esValido("10.000.013-k"));
    }

    @Test
    @DisplayName("Rechaza RUT con dígito verificador incorrecto, vacíos o con letras")
    void testRutInvalidos() {
        assertFalse(RutUtil.esValido("12.345.678-9"));
        assertFalse(RutUtil.esValido(""));
        assertFalse(RutUtil.esValido(null));
        assertFalse(RutUtil.esValido("abc"));
    }

    @Test
    @DisplayName("Da formato con puntos y guion")
    void testFormatear() {
        assertEquals("12.345.678-5", RutUtil.formatear("123456785"));
        assertEquals("9.876.543-3", RutUtil.formatear("9876543-3"));
    }

    @Test
    @DisplayName("Los RUT generados con DataFaker siempre son válidos")
    void testRutAleatorioEsValido() {
        for (int i = 0; i < 20; i++) {
            assertTrue(RutUtil.esValido(DatosDePrueba.rutAleatorio()));
        }
    }
}
