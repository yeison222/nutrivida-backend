-- V7: corrige el dígito verificador de los RUT de los pacientes iniciales (V6 los traía mal)
-- y hace que el RUT no se pueda repetir.
-- No se modifica V6: una migración ya aplicada nunca se cambia, se agrega una nueva.
UPDATE pacientes SET rut = '18.765.432-7' WHERE correo = 'v.morales@gmail.com';
UPDATE pacientes SET rut = '15.432.987-0' WHERE correo = 'g.arriagada@gmail.com';
UPDATE pacientes SET rut = '19.876.543-0' WHERE correo = 'c.silva@outlook.com';

ALTER TABLE pacientes ADD CONSTRAINT uk_pacientes_rut UNIQUE (rut);
