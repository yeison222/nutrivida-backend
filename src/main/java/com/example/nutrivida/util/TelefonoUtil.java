package com.example.nutrivida.util;

/**
 * Validación y formato de teléfonos celulares chilenos (+56 9 XXXX XXXX).
 */
public final class TelefonoUtil {

    private TelefonoUtil() {}

    // Deja solo los dígitos y quita el 56 inicial: "+56 9 8765 4321" -> "987654321"
    private static String soloNumero(String telefono) {
        String digitos = telefono == null ? "" : telefono.replaceAll("\\D", "");
        return digitos.startsWith("56") && digitos.length() == 11 ? digitos.substring(2) : digitos;
    }

    // true si es un celular chileno: 9 dígitos que empiezan con 9
    public static boolean esValido(String telefono) {
        return soloNumero(telefono).matches("9\\d{8}");
    }

    // "987654321" -> "+56 9 8765 4321"
    public static String formatear(String telefono) {
        String numero = soloNumero(telefono);
        return "+56 9 " + numero.substring(1, 5) + " " + numero.substring(5);
    }
}
