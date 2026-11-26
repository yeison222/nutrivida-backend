package com.example.nutrivida;

import com.example.nutrivida.dto.RegistroRequest;
import com.example.nutrivida.model.Paciente;
import com.example.nutrivida.model.Plan;
import com.example.nutrivida.model.Usuario;
import com.example.nutrivida.model.Venta;
import com.example.nutrivida.util.RutUtil;
import net.datafaker.Faker;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Genera datos de prueba aleatorios con DataFaker.
 * Cada vez que se ejecutan las pruebas se usan nombres, precios y correos distintos.
 */
public final class DatosDePrueba {

    public static final Faker FAKER = new Faker(Locale.forLanguageTag("es"));

    public static final String[] CATEGORIAS = {"deporte", "peso", "metabolico", "vegetariano"};

    private DatosDePrueba() {}

    // Plan aleatorio (sin id, como si viniera del formulario)
    public static Plan planAleatorio() {
        return new Plan(
                "Plan " + FAKER.food().dish(),
                FAKER.options().option(CATEGORIAS),
                FAKER.lorem().sentence(12),
                FAKER.number().numberBetween(10, 90) * 1000.0,
                "img/logo.png");
    }

    // Plan aleatorio con id asignado (como si viniera de la base de datos)
    public static Plan planAleatorioConId(long id) {
        Plan plan = planAleatorio();
        plan.setId(id);
        return plan;
    }

    // Usuario aleatorio con el rol indicado ("nutricionista" o "paciente")
    public static Usuario usuarioAleatorio(String rol) {
        return new Usuario(
                FAKER.name().fullName(),
                FAKER.internet().emailAddress(),
                FAKER.internet().password(8, 12),
                rol);
    }

    // RUT aleatorio con dígito verificador correcto (formato 12.345.678-5)
    public static String rutAleatorio() {
        String cuerpo = String.valueOf(FAKER.number().numberBetween(10_000_000, 25_000_000));
        return RutUtil.formatear(cuerpo + RutUtil.calcularDv(cuerpo));
    }

    // Celular chileno aleatorio: +56 9 XXXX XXXX
    public static String telefonoAleatorio() {
        return "+56 9 " + FAKER.number().digits(4) + " " + FAKER.number().digits(4);
    }

    // Datos de registro completos y válidos (nombre, RUT, correo, contraseña, teléfono, fecha y objetivo)
    public static RegistroRequest registroAleatorio(String rut, String correo, String password) {
        return new RegistroRequest(
                FAKER.name().fullName(), rut, correo, password,
                telefonoAleatorio(),
                LocalDate.now().minusYears(FAKER.number().numberBetween(18, 70)),
                FAKER.options().option("Pérdida de Peso", "Nutrición Deportiva", "Control Metabólico", "Alimentación Vegetariana"));
    }

    // Paciente aleatorio (sin id)
    public static Paciente pacienteAleatorio() {
        return new Paciente(
                rutAleatorio(),
                FAKER.name().fullName(),
                FAKER.internet().emailAddress(),
                telefonoAleatorio(),
                LocalDate.now().minusYears(FAKER.number().numberBetween(18, 70)),
                FAKER.options().option("Pérdida de Peso", "Nutrición Deportiva", "Control Metabólico", "Alimentación Vegetariana"));
    }

    // Venta aleatoria para un paciente y un plan (sin id, sin total: lo calcula el servicio)
    public static Venta ventaAleatoria(long pacienteId, long planId) {
        return new Venta(
                pacienteId,
                planId,
                LocalDate.now().minusDays(FAKER.number().numberBetween(0, 60)),
                FAKER.options().option(1, 2, 4),
                null,
                FAKER.options().option("Pagada", "Pendiente"));
    }
}
