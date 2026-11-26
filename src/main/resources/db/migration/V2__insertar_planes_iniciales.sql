-- V2: carga los 4 planes del sitio NutriVida (quedan con id 1 a 4)
INSERT INTO planes (nombre, categoria, descripcion, precio, imagen) VALUES
('Plan Nutrición Deportiva', 'deporte',
 'Optimización del rendimiento, recomposición corporal y pauta alimenticia adaptada a tu disciplina deportiva.',
 45000, 'img/deporte.png'),
('Programa Pérdida de Peso', 'peso',
 'Evaluación antropométrica completa y pauta alimenticia hipocalórica personalizada para lograr una reducción sostenida sin efecto rebote.',
 35000, 'img/perdida_peso.png'),
('Plan Control Metabólico', 'metabolico',
 'Abordaje clínico especializado para el manejo de diabetes tipo 2, hipertensión arterial y resistencia a la insulina.',
 40000, 'img/metabolico.png'),
('Plan Vegetariano / Vegano', 'vegetariano',
 'Transición nutricional segura, pautas basadas en plantas para asegurar proteínas completas y control analítico de micronutrientes.',
 35000, 'img/vegetariano.png');
