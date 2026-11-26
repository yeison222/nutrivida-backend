package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.PreparedStatement;

/**
 * V4: migración de Flyway escrita en Java.
 * Inserta los usuarios del sitio NutriVida encriptando sus contraseñas con BCrypt,
 * así en la base de datos nunca queda una contraseña en texto plano.
 */
public class V4__InsertarUsuariosIniciales extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // { nombre, correo, contraseña, rol }
        String[][] usuarios = {
                {"Dra. María Paz Fernández", "m.fernandez@nutrivida.cl", "123456", "nutricionista"},
                {"Nut. Carlos Muñoz", "c.munoz@nutrivida.cl", "123456", "nutricionista"},
                {"Nut. Andrea Riquelme", "a.riquelme@nutrivida.cl", "123456", "nutricionista"},
                {"Nut. Sofía Toledo", "s.toledo@nutrivida.cl", "123456", "nutricionista"},
                {"Valentina Morales", "v.morales@gmail.com", "123456", "paciente"},
                {"Gonzalo Arriagada", "g.arriagada@gmail.com", "123456", "paciente"},
                {"Camila Silva", "c.silva@outlook.com", "1234567", "paciente"}
        };

        String sql = "INSERT INTO usuarios (nombre, correo, password, rol) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = context.getConnection().prepareStatement(sql)) {
            for (String[] u : usuarios) {
                ps.setString(1, u[0]);
                ps.setString(2, u[1]);
                ps.setString(3, encoder.encode(u[2]));
                ps.setString(4, u[3]);
                ps.executeUpdate();
            }
        }
    }
}
