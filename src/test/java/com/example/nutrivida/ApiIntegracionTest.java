package com.example.nutrivida;

import com.example.nutrivida.model.Plan;
import com.example.nutrivida.model.Usuario;
import com.example.nutrivida.repository.PacienteRepository;
import com.example.nutrivida.repository.PlanRepository;
import com.example.nutrivida.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración: levantan la aplicación completa (Flyway + H2 + Spring Security + JWT)
 * y llaman a los endpoints con MockMvc.
 * @Transactional deshace los cambios al terminar cada prueba.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    private String tokenCon(String rol) {
        Usuario usuario = DatosDePrueba.usuarioAleatorio(rol);
        return "Bearer " + jwtService.generarToken(usuario);
    }

    // Token de un usuario con correo y rol específicos
    private String tokenDe(String correo, String rol) {
        return "Bearer " + jwtService.generarToken(new Usuario("Usuario Prueba", correo, "x", rol));
    }

    private String planAleatorioJson() throws Exception {
        Plan plan = DatosDePrueba.planAleatorio();
        return objectMapper.writeValueAsString(plan);
    }

    @Test
    @DisplayName("GET /api/planes es público y devuelve los planes cargados por Flyway")
    void testListarPlanesEsPublico() throws Exception {
        mockMvc.perform(get("/api/planes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].categoria").value("deporte"))
                .andExpect(jsonPath("$[0].precio").value(45000.0));
    }

    @Test
    @DisplayName("GET de un plan inexistente responde 404")
    void testPlanInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/planes/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST sin token responde 401")
    void testCrearPlanSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/api/planes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planAleatorioJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST con un token inválido responde 401")
    void testCrearPlanConTokenInvalidoDevuelve401() throws Exception {
        mockMvc.perform(post("/api/planes")
                        .header("Authorization", "Bearer token.falso.123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planAleatorioJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST con token de paciente responde 403")
    void testCrearPlanConTokenPacienteDevuelve403() throws Exception {
        mockMvc.perform(post("/api/planes")
                        .header("Authorization", tokenCon("paciente"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planAleatorioJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST con token de nutricionista crea el plan")
    void testCrearPlanConTokenNutricionista() throws Exception {
        long antes = planRepository.count();

        mockMvc.perform(post("/api/planes")
                        .header("Authorization", tokenCon("nutricionista"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planAleatorioJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber());

        assertEquals(antes + 1, planRepository.count());
    }

    @Test
    @DisplayName("DELETE con token de nutricionista elimina el plan")
    void testEliminarPlanConTokenNutricionista() throws Exception {
        mockMvc.perform(delete("/api/planes/4").header("Authorization", tokenCon("nutricionista")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/planes/4"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Login con un usuario de la migración V4 devuelve un token")
    void testLoginCorrecto() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"m.fernandez@nutrivida.cl\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.rol").value("nutricionista"));
    }

    @Test
    @DisplayName("Login con contraseña incorrecta responde 401")
    void testLoginIncorrecto() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"m.fernandez@nutrivida.cl\",\"password\":\"clave-mala\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Registrar un paciente con RUT (DataFaker) lo guarda en pacientes; repetir el correo responde 409")
    void testRegistroYCorreoDuplicado() throws Exception {
        String correo = DatosDePrueba.FAKER.internet().emailAddress();
        String rut = DatosDePrueba.rutAleatorio();
        String cuerpo = "{\"nombre\":\"Paciente Prueba\",\"rut\":\"" + rut + "\",\"correo\":\"" + correo + "\",\"password\":\"123456\",\"telefono\":\"+56 9 1234 5678\",\"fechaNacimiento\":\"1998-05-20\",\"objetivo\":\"Pérdida de Peso\"}";

        mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("paciente"));

        assertEquals(true, pacienteRepository.existsByRut(rut));
        // Los datos del paciente no quedan nulos
        var paciente = pacienteRepository.findByCorreoIgnoreCase(correo).orElseThrow();
        assertEquals("+56 9 1234 5678", paciente.getTelefono());
        assertEquals("1998-05-20", paciente.getFechaNacimiento().toString());
        assertEquals("Pérdida de Peso", paciente.getObjetivo());

        mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Registrar con un RUT inválido responde 400")
    void testRegistroRutInvalido() throws Exception {
        String cuerpo = "{\"nombre\":\"Paciente Prueba\",\"rut\":\"12.345.678-9\",\"correo\":\"rut.malo@correo.cl\",\"password\":\"123456\",\"telefono\":\"+56 9 1234 5678\",\"fechaNacimiento\":\"1998-05-20\",\"objetivo\":\"Pérdida de Peso\"}";

        mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Registrar con el RUT de un paciente existente (V7) responde 409")
    void testRegistroRutDuplicado() throws Exception {
        String cuerpo = "{\"nombre\":\"Otra Persona\",\"rut\":\"187654327\",\"correo\":\"otra.persona@correo.cl\",\"password\":\"123456\",\"telefono\":\"+56 9 1234 5678\",\"fechaNacimiento\":\"1998-05-20\",\"objetivo\":\"Pérdida de Peso\"}";

        mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/pacientes/{id} con token de nutricionista devuelve el paciente cargado por Flyway")
    void testBuscarPacientePorId() throws Exception {
        mockMvc.perform(get("/api/pacientes/1").header("Authorization", tokenCon("nutricionista")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("v.morales@gmail.com"))
                .andExpect(jsonPath("$.fechaNacimiento").value("1995-04-12"));
    }

    @Test
    @DisplayName("GET /api/pacientes/{id} inexistente responde 404")
    void testBuscarPacienteInexistente() throws Exception {
        mockMvc.perform(get("/api/pacientes/999").header("Authorization", tokenCon("nutricionista")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/pacientes sin token responde 401")
    void testPacientesSinToken() throws Exception {
        mockMvc.perform(get("/api/pacientes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/pacientes crea un paciente generado con DataFaker")
    void testCrearPaciente() throws Exception {
        String cuerpo = objectMapper.writeValueAsString(DatosDePrueba.pacienteAleatorio());

        mockMvc.perform(post("/api/pacientes")
                        .header("Authorization", tokenCon("nutricionista"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("GET /api/ventas devuelve las 3 ventas cargadas por Flyway")
    void testListarVentas() throws Exception {
        mockMvc.perform(get("/api/ventas").header("Authorization", tokenCon("nutricionista")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @DisplayName("POST /api/ventas calcula el total con el precio del plan")
    void testCrearVentaCalculaTotal() throws Exception {
        // Plan 1 = $45.000 por sesión; 2 sesiones = $90.000 (sin descuento)
        String cuerpo = "{\"pacienteId\":1,\"planId\":1,\"sesiones\":2,\"estado\":\"Pagada\"}";

        mockMvc.perform(post("/api/ventas")
                        .header("Authorization", tokenCon("nutricionista"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(90000.0));
    }

    @Test
    @DisplayName("POST /api/ventas con un plan inexistente responde 400")
    void testCrearVentaPlanInexistente() throws Exception {
        mockMvc.perform(post("/api/ventas")
                        .header("Authorization", tokenCon("nutricionista"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pacienteId\":1,\"planId\":999,\"sesiones\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Las ventas con token de paciente responden 403")
    void testVentasConTokenPaciente() throws Exception {
        mockMvc.perform(get("/api/ventas").header("Authorization", tokenCon("paciente")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Un paciente con sesión contrata un plan y la venta queda a su nombre")
    void testPacienteContrataPlan() throws Exception {
        // v.morales@gmail.com es el paciente 1; plan 3 = $40.000 x 4 sesiones con 10% = $144.000
        mockMvc.perform(post("/api/ventas/contratar")
                        .header("Authorization", tokenDe("v.morales@gmail.com", "paciente"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planId\":3,\"sesiones\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pacienteId").value(1))
                .andExpect(jsonPath("$.total").value(144000.0));
    }

    @Test
    @DisplayName("Mis ventas devuelve solo las ventas del paciente con sesión")
    void testMisVentas() throws Exception {
        mockMvc.perform(get("/api/ventas/mis-ventas").header("Authorization", tokenDe("v.morales@gmail.com", "paciente")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].pacienteId").value(1));
    }

    @Test
    @DisplayName("Un nutricionista no puede usar /contratar (403)")
    void testNutricionistaNoContrata() throws Exception {
        mockMvc.perform(post("/api/ventas/contratar")
                        .header("Authorization", tokenCon("nutricionista"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planId\":1,\"sesiones\":1}"))
                .andExpect(status().isForbidden());
    }
}
