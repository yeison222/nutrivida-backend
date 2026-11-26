-- V1: crea la tabla de planes nutricionales
CREATE TABLE planes (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(255) NOT NULL,
    categoria   VARCHAR(50)  NOT NULL,
    descripcion VARCHAR(500),
    precio      DOUBLE PRECISION NOT NULL,
    imagen      VARCHAR(255)
);
