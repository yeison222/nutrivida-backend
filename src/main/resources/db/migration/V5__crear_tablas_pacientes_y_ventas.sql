-- V5: crea las tablas de pacientes (clientes) y ventas de planes
-- Las tablas son independientes: ventas guarda paciente_id y plan_id como números, sin llaves foráneas
CREATE TABLE pacientes (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    rut              VARCHAR(20),
    nombre           VARCHAR(255) NOT NULL,
    correo           VARCHAR(255) NOT NULL UNIQUE,
    telefono         VARCHAR(30),
    fecha_nacimiento DATE,
    objetivo         VARCHAR(255)
);

CREATE TABLE ventas (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    paciente_id BIGINT NOT NULL,
    plan_id     BIGINT NOT NULL,
    fecha       DATE NOT NULL,
    sesiones    INT NOT NULL,
    total       DOUBLE PRECISION NOT NULL,
    estado      VARCHAR(20) NOT NULL
);
