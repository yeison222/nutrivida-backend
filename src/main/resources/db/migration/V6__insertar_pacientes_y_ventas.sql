-- V6: pacientes del sitio NutriVida y algunas ventas de ejemplo
INSERT INTO pacientes (rut, nombre, correo, telefono, fecha_nacimiento, objetivo) VALUES
('18.765.432-1', 'Valentina Morales', 'v.morales@gmail.com', '+56 9 8765 4321', '1995-04-12', 'Nutrición Deportiva y Recomposición Corporal'),
('15.432.987-K', 'Gonzalo Arriagada', 'g.arriagada@gmail.com', '+56 9 7654 3210', '1983-09-25', 'Control de Diabetes Tipo 2 y Resistencia a la Insulina'),
('19.876.543-2', 'Camila Silva', 'c.silva@outlook.com', '+56 9 6543 2109', '1998-11-03', 'Alimentación Vegetariana Equilibrada');

-- Ventas: paciente 1 compró 4 sesiones del plan 1 (con 10% de descuento), etc.
INSERT INTO ventas (paciente_id, plan_id, fecha, sesiones, total, estado) VALUES
(1, 1, '2026-09-02', 4, 162000, 'Pagada'),
(2, 3, '2026-09-04', 2, 80000, 'Pagada'),
(3, 4, '2026-09-03', 1, 35000, 'Pendiente');
