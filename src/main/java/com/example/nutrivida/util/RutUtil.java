package com.example.nutrivida.util;

/**
 * Validación y formato del RUT chileno.
 * El dígito verificador se calcula con el algoritmo módulo 11.
 */
public final class RutUtil {

    private RutUtil() {}

    // Quita puntos, guion y espacios: "12.345.678-5" -> "123456785"
    public static String limpiar(String rut) {
        return rut == null ? "" : rut.replace(".", "").replace("-", "").replace(" ", "").trim().toUpperCase();
    }

    // Calcula el dígito verificador del cuerpo del RUT (sin el DV)
    public static char calcularDv(String cuerpo) {
        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
            multiplicador = (multiplicador == 7) ? 2 : multiplicador + 1;
        }
        int resto = 11 - (suma % 11);
        if (resto == 11) return '0';
        if (resto == 10) return 'K';
        return (char) ('0' + resto);
    }

    // true si el RUT tiene 7 u 8 dígitos y su dígito verificador es correcto
    public static boolean esValido(String rut) {
        String limpio = limpiar(rut);
        if (!limpio.matches("\\d{7,8}[0-9K]")) {
            return false;
        }
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char dv = limpio.charAt(limpio.length() - 1);
        return calcularDv(cuerpo) == dv;
    }

    // Da formato con puntos y guion: "123456785" -> "12.345.678-5"
    public static String formatear(String rut) {
        String limpio = limpiar(rut);
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char dv = limpio.charAt(limpio.length() - 1);
        StringBuilder conPuntos = new StringBuilder();
        int contador = 0;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            conPuntos.insert(0, cuerpo.charAt(i));
            contador++;
            if (contador % 3 == 0 && i > 0) {
                conPuntos.insert(0, '.');
            }
        }
        return conPuntos + "-" + dv;
    }
}
