-- V3: crea la tabla de usuarios para la autenticación con JWT
-- La contraseña se guarda encriptada con BCrypt (nunca en texto plano)
CREATE TABLE usuarios (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre   VARCHAR(255) NOT NULL,
    correo   VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol      VARCHAR(20)  NOT NULL
);
