-- Datos de prueba mínimos para probar el módulo de Atención Clínica (Persona 5).
-- Ejecutar directamente contra la base de Neon (por ejemplo desde la pestaña Database de IntelliJ,
-- o con psql). No es parte del esquema Flyway — es solo para tener algo que seleccionar en los formularios.

-- 1 veterinario adicional (además del admin que ya viene sembrado en V1)
INSERT INTO personal (codigo_institucional, username, password, nombre, apellido, email, rol_id, activo)
VALUES (
    'C000010',
    'vet1',
    crypt('Huellitas2025!', gen_salt('bf', 12)),
    'Laura',
    'Vega',
    'laura.vega@huellitas.pe',
    (SELECT id FROM rol WHERE nombre = 'VETERINARIO'),
    true
);

-- 1 propietario
INSERT INTO propietario (dni, nombres, apellidos, telefono, email, direccion)
VALUES ('12345678', 'Carlos', 'Ramírez', '987654321', 'carlos.ramirez@mail.com', 'Av. Siempre Viva 123');

-- 1 paciente para ese propietario
INSERT INTO paciente (propietario_id, nombre, especie, raza, genero, fecha_nacimiento, peso_referencia)
VALUES (
    (SELECT id FROM propietario WHERE dni = '12345678'),
    'Firulais',
    'PERRO',
    'Labrador',
    'MACHO',
    '2020-05-10',
    28.5
);

-- 1 producto en inventario, para probar el enlace Receta -> Producto
INSERT INTO producto (nombre, descripcion, categoria, precio_venta, stock_actual, stock_minimo)
VALUES ('Amoxicilina 250mg', 'Antibiótico de uso general', 'Medicamento', 15.90, 50, 10);

-- Verificación rápida
SELECT 'personal' AS tabla, count(*) FROM personal
UNION ALL SELECT 'propietario', count(*) FROM propietario
UNION ALL SELECT 'paciente', count(*) FROM paciente
UNION ALL SELECT 'producto', count(*) FROM producto;
