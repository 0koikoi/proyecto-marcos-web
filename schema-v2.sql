-- ============================================================
--  HUELLITAS — Esquema de Base de Datos v2.0
--  Clínica Veterinaria
--  Fecha: 2025-07-09
-- ============================================================
--  INSTRUCCIONES:
--    1. Abre la consola SQL de Neon.tech
--    2. Pega TODO este script y ejecútalo
--    3. Verifica que aparezcan 15 tablas en el explorador
--    4. Los usuarios iniciales tienen contraseña: Huellitas2025!
-- ============================================================

-- Habilitar extensión para hasheo BCrypt (compatible con Spring Security)
CREATE EXTENSION IF NOT EXISTS pgcrypto;


-- ============================================================
-- 0. LIMPIAR ESQUEMA ANTERIOR (orden inverso a FK)
-- ============================================================
DROP TABLE IF EXISTS detalle_receta      CASCADE;
DROP TABLE IF EXISTS receta              CASCADE;
DROP TABLE IF EXISTS vacuna              CASCADE;
DROP TABLE IF EXISTS historia_clinica    CASCADE;
DROP TABLE IF EXISTS solicitud_material  CASCADE;
DROP TABLE IF EXISTS detalle_venta       CASCADE;
DROP TABLE IF EXISTS venta               CASCADE;
DROP TABLE IF EXISTS cita                CASCADE;
DROP TABLE IF EXISTS paciente            CASCADE;
DROP TABLE IF EXISTS propietario         CASCADE;
DROP TABLE IF EXISTS producto            CASCADE;
DROP TABLE IF EXISTS proveedor           CASCADE;
DROP TABLE IF EXISTS servicio            CASCADE;
DROP TABLE IF EXISTS personal            CASCADE;
DROP TABLE IF EXISTS rol                 CASCADE;


-- ============================================================
-- 1. CATÁLOGOS / LOOKUP TABLES
-- ============================================================

-- ── 1.1 ROLES ──────────────────────────────────────────────
CREATE TABLE rol (
    id          SERIAL      PRIMARY KEY,
    nombre      VARCHAR(20) NOT NULL UNIQUE,  -- ADMINISTRADOR | RECEPCION | VETERINARIO
    descripcion TEXT
);

INSERT INTO rol (nombre, descripcion) VALUES
    ('ADMINISTRADOR',
        'Acceso total al sistema. Gestión de personal, reportes, proveedores y configuración general.'),
    ('RECEPCION',
        'Gestión de citas, registro de propietarios y pacientes, ventas y cobros de servicios y medicamentos.'),
    ('VETERINARIO',
        'Atención clínica: historia clínica, recetas médicas, vacunas, solicitud de materiales y actualización de datos del paciente.');


-- ── 1.2 SERVICIOS VETERINARIOS ─────────────────────────────
-- Catálogo de servicios que se pueden vender / cobrar en ventas
CREATE TABLE servicio (
    id          BIGSERIAL       PRIMARY KEY,
    nombre      VARCHAR(100)    NOT NULL,
    descripcion TEXT,
    precio      DECIMAL(10,2)   NOT NULL CHECK (precio >= 0),
    activo      BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

INSERT INTO servicio (nombre, descripcion, precio) VALUES
    ('Consulta General',        'Revisión general del estado de salud del paciente',               40.00),
    ('Consulta de Urgencia',    'Atención fuera de horario o casos de emergencia',                 80.00),
    ('Vacunación',              'Aplicación de vacuna (no incluye el costo del biológico)',        15.00),
    ('Desparasitación',         'Tratamiento antiparasitario interno y/o externo',                 25.00),
    ('Baño y Corte Básico',     'Servicio de grooming: baño, secado y corte estándar',            35.00),
    ('Baño y Corte Premium',    'Grooming premium con accesorios y finalización estética',         55.00),
    ('Cirugía Menor',           'Procedimientos quirúrgicos menores con anestesia local',         150.00),
    ('Cirugía Mayor',           'Procedimientos quirúrgicos mayores con anestesia general',       400.00),
    ('Esterilización (hembra)', 'Ovariohisterectomía (espay)',                                    220.00),
    ('Esterilización (macho)',  'Orquiectomía (castración)',                                      180.00),
    ('Radiografía',             'Diagnóstico por imagen — placa simple',                           80.00),
    ('Ecografía',               'Diagnóstico por ultrasonido',                                    100.00),
    ('Análisis de Laboratorio', 'Hemograma, bioquímica sérica u otros perfiles',                   60.00),
    ('Hospitalización (día)',   'Cuidado intensivo con monitoreo continuo, por día',              120.00),
    ('Odontología Básica',      'Limpieza dental con ultrasonido',                                 90.00);


-- ============================================================
-- 2. PERSONAL (USUARIOS DEL SISTEMA)
-- ============================================================
CREATE TABLE personal (
    id                    BIGSERIAL     PRIMARY KEY,
    codigo_institucional  VARCHAR(7)    NOT NULL UNIQUE,   -- Formato: C000001
    nombre_completo       VARCHAR(100)  NOT NULL,
    rol_id                INT           NOT NULL REFERENCES rol(id),
    cargo                 VARCHAR(50)   NOT NULL,           -- Etiqueta visible (ej: "Veterinario")
    especialidad          VARCHAR(100),                     -- Solo relevante para veterinarios
    telefono              VARCHAR(15),
    username              VARCHAR(50)   NOT NULL UNIQUE,
    password_hash         VARCHAR(255)  NOT NULL,           -- BCrypt generado con pgcrypto
    activo                BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

-- ── USUARIOS INICIALES ──────────────────────────────────────
-- Contraseña para TODOS los usuarios iniciales: Huellitas2025!
-- El hash BCrypt ($2a$12$...) es generado por pgcrypto y es compatible
-- con Spring Security's BCryptPasswordEncoder.
-- El equipo deberá cambiar las contraseñas tras el primer acceso.

INSERT INTO personal
    (codigo_institucional, nombre_completo, rol_id, cargo, especialidad, telefono, username, password_hash)
VALUES
    -- ADMINISTRADORES (rol_id = 1)
    ('C000001', 'Administrador Principal',    1, 'Administrador', NULL,                NULL, 'admin1', crypt('Huellitas2025!', gen_salt('bf', 12))),
    ('C000002', 'Administrador Auxiliar',     1, 'Administrador', NULL,                NULL, 'admin2', crypt('Huellitas2025!', gen_salt('bf', 12))),

    -- RECEPCIÓN (rol_id = 2)
    ('C000003', 'Recepcionista Uno',          2, 'Recepcionista', NULL,                NULL, 'recep1', crypt('Huellitas2025!', gen_salt('bf', 12))),
    ('C000004', 'Recepcionista Dos',          2, 'Recepcionista', NULL,                NULL, 'recep2', crypt('Huellitas2025!', gen_salt('bf', 12))),

    -- VETERINARIOS (rol_id = 3)
    ('C000005', 'Dr. Veterinario Uno',        3, 'Veterinario', 'Medicina General',    NULL, 'vet1',   crypt('Huellitas2025!', gen_salt('bf', 12))),
    ('C000006', 'Dr. Veterinario Dos',        3, 'Veterinario', 'Medicina General',    NULL, 'vet2',   crypt('Huellitas2025!', gen_salt('bf', 12))),
    ('C000007', 'Dr. Veterinario Tres',       3, 'Veterinario', 'Cirugía',             NULL, 'vet3',   crypt('Huellitas2025!', gen_salt('bf', 12))),
    ('C000008', 'Dr. Veterinario Cuatro',     3, 'Veterinario', 'Dermatología',        NULL, 'vet4',   crypt('Huellitas2025!', gen_salt('bf', 12))),
    ('C000009', 'Dr. Veterinario Cinco',      3, 'Veterinario', 'Odontología',         NULL, 'vet5',   crypt('Huellitas2025!', gen_salt('bf', 12))),
    ('C000010', 'Dr. Veterinario Seis',       3, 'Veterinario', 'Nutrición Animal',    NULL, 'vet6',   crypt('Huellitas2025!', gen_salt('bf', 12))),
    ('C000011', 'Dr. Veterinario Siete',      3, 'Veterinario', 'Medicina General',    NULL, 'vet7',   crypt('Huellitas2025!', gen_salt('bf', 12))),
    ('C000012', 'Dr. Veterinario Ocho',       3, 'Veterinario', 'Medicina en Exóticos',NULL, 'vet8',   crypt('Huellitas2025!', gen_salt('bf', 12)));


-- ============================================================
-- 3. PROPIETARIOS Y PACIENTES
-- ============================================================

-- ── 3.1 PROPIETARIOS ───────────────────────────────────────
CREATE TABLE propietario (
    id              BIGSERIAL     PRIMARY KEY,
    dni             VARCHAR(8)    NOT NULL UNIQUE,
    nombre_completo VARCHAR(100)  NOT NULL,
    telefono        VARCHAR(15)   NOT NULL,
    correo          VARCHAR(150),
    direccion       TEXT          NOT NULL,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_propietario_dni_numerico CHECK (dni ~ '^\d{8}$')
);


-- ── 3.2 PACIENTES (mascotas) ────────────────────────────────
CREATE TABLE paciente (
    id               BIGSERIAL    PRIMARY KEY,
    nombre           VARCHAR(100) NOT NULL,
    especie          VARCHAR(50)  NOT NULL,             -- Perro, Gato, Ave, Reptil, etc.
    raza             VARCHAR(50),
    genero           VARCHAR(10)  CHECK (genero IN ('MACHO', 'HEMBRA', 'DESCONOCIDO')),
    fecha_nacimiento DATE,
    propietario_id   BIGINT       NOT NULL REFERENCES propietario(id),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by       BIGINT       REFERENCES personal(id)  -- Quién registró al paciente
);


-- ============================================================
-- 4. CITAS / AGENDA
-- ============================================================
CREATE TABLE cita (
    id          BIGSERIAL    PRIMARY KEY,
    paciente_id BIGINT       NOT NULL REFERENCES paciente(id),
    personal_id BIGINT       NOT NULL REFERENCES personal(id),  -- Veterinario asignado
    fecha_hora  TIMESTAMPTZ  NOT NULL,
    motivo      TEXT         NOT NULL,
    estado      VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE'
                    CHECK (estado IN ('PENDIENTE', 'EN_PROCESO', 'COMPLETADA', 'CANCELADA')),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by  BIGINT       REFERENCES personal(id)            -- Recepcionista que agendó
);


-- ============================================================
-- 5. HISTORIA CLÍNICA
-- ============================================================
-- Una entrada puede o no venir de una cita agendada (cita_id es nullable)
-- El peso y temperatura aquí son los del momento de la consulta (dato histórico)
CREATE TABLE historia_clinica (
    id              BIGSERIAL     PRIMARY KEY,
    paciente_id     BIGINT        NOT NULL REFERENCES paciente(id),
    personal_id     BIGINT        NOT NULL REFERENCES personal(id),  -- Veterinario tratante
    cita_id         BIGINT        REFERENCES cita(id),               -- NULL = consulta sin cita previa
    fecha_consulta  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    motivo_consulta TEXT          NOT NULL,
    diagnostico     TEXT,
    tratamiento     TEXT,
    observaciones   TEXT,
    peso_kg         DECIMAL(5,2)  CHECK (peso_kg > 0),
    temperatura_c   DECIMAL(4,1),
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);


-- ============================================================
-- 6. VACUNAS
-- ============================================================
CREATE TABLE vacuna (
    id                  BIGSERIAL    PRIMARY KEY,
    paciente_id         BIGINT       NOT NULL REFERENCES paciente(id),
    personal_id         BIGINT       NOT NULL REFERENCES personal(id),  -- Veterinario que aplica
    nombre_vacuna       VARCHAR(100) NOT NULL,
    laboratorio         VARCHAR(100),
    lote                VARCHAR(50),
    fecha_aplicacion    DATE         NOT NULL,
    fecha_proxima_dosis DATE,
    observaciones       TEXT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);


-- ============================================================
-- 7. RECETAS MÉDICAS
-- ============================================================
-- Una receta siempre está ligada a una entrada de historia clínica
CREATE TABLE receta (
    id                      BIGSERIAL    PRIMARY KEY,
    historia_clinica_id     BIGINT       NOT NULL REFERENCES historia_clinica(id),
    personal_id             BIGINT       NOT NULL REFERENCES personal(id),  -- Veterinario que firma
    fecha_emision           DATE         NOT NULL DEFAULT CURRENT_DATE,
    observaciones_generales TEXT,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Cada línea de la receta es un medicamento prescrito
CREATE TABLE detalle_receta (
    id            BIGSERIAL    PRIMARY KEY,
    receta_id     BIGINT       NOT NULL REFERENCES receta(id) ON DELETE CASCADE,
    medicamento   VARCHAR(150) NOT NULL,
    presentacion  VARCHAR(50),                     -- comprimido, jarabe, inyectable, tópico
    dosis         VARCHAR(100) NOT NULL,            -- Ej: "5 mg/kg"
    frecuencia    VARCHAR(100) NOT NULL,            -- Ej: "cada 8 horas"
    duracion_dias INT          CHECK (duracion_dias > 0),
    cantidad      INT          CHECK (cantidad > 0),
    observaciones TEXT
);


-- ============================================================
-- 8. INVENTARIO
-- ============================================================

-- ── 8.1 PROVEEDORES ────────────────────────────────────────
CREATE TABLE proveedor (
    id           BIGSERIAL    PRIMARY KEY,
    ruc          VARCHAR(11)  NOT NULL UNIQUE,
    razon_social VARCHAR(150) NOT NULL,
    contacto     VARCHAR(100),
    telefono     VARCHAR(15),
    email        VARCHAR(150),
    activo       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_proveedor_ruc_numerico CHECK (ruc ~ '^\d{11}$')
);


-- ── 8.2 PRODUCTOS / MEDICAMENTOS / INSUMOS ─────────────────
CREATE TABLE producto (
    id            BIGSERIAL     PRIMARY KEY,
    nombre        VARCHAR(100)  NOT NULL,
    descripcion   TEXT,
    precio_compra DECIMAL(10,2) NOT NULL CHECK (precio_compra >= 0),
    precio_venta  DECIMAL(10,2) NOT NULL CHECK (precio_venta >= 0),
    stock_actual  INT           NOT NULL DEFAULT 0 CHECK (stock_actual >= 0),
    stock_minimo  INT           NOT NULL DEFAULT 5  CHECK (stock_minimo >= 0),
    proveedor_id  BIGINT        REFERENCES proveedor(id),
    activo        BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);


-- ── 8.3 SOLICITUDES DE MATERIAL (por veterinarios) ─────────
-- Un veterinario solicita un producto; un administrador lo aprueba o rechaza
CREATE TABLE solicitud_material (
    id                    BIGSERIAL    PRIMARY KEY,
    solicitante_id        BIGINT       NOT NULL REFERENCES personal(id),  -- Veterinario solicitante
    producto_id           BIGINT       NOT NULL REFERENCES producto(id),
    cantidad_solicitada   INT          NOT NULL CHECK (cantidad_solicitada > 0),
    motivo                TEXT         NOT NULL,
    estado                VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE'
                              CHECK (estado IN ('PENDIENTE', 'APROBADA', 'RECHAZADA', 'ENTREGADA')),
    aprobado_por          BIGINT       REFERENCES personal(id),           -- Admin que responde
    observacion_respuesta TEXT,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    fecha_respuesta       TIMESTAMPTZ
);


-- ============================================================
-- 9. VENTAS Y FACTURACIÓN
-- ============================================================

-- ── 9.1 VENTA / BOLETA ─────────────────────────────────────
-- La recepción genera ventas que pueden incluir servicios y/o productos
-- Una venta puede estar asociada a una cita (ej: cobrar la consulta completada)
CREATE TABLE venta (
    id              BIGSERIAL     PRIMARY KEY,
    nro_boleta      VARCHAR(20)   NOT NULL UNIQUE,        -- BOL-XXXXXXXX
    propietario_id  BIGINT        REFERENCES propietario(id),   -- Null = cliente sin registro
    personal_id     BIGINT        NOT NULL REFERENCES personal(id),   -- Recepcionista
    cita_id         BIGINT        REFERENCES cita(id),          -- Si viene de una cita
    subtotal        DECIMAL(10,2) NOT NULL CHECK (subtotal >= 0),
    igv             DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total           DECIMAL(10,2) NOT NULL CHECK (total >= 0),
    tipo_pago       VARCHAR(20)   NOT NULL DEFAULT 'EFECTIVO'
                        CHECK (tipo_pago IN ('EFECTIVO', 'TARJETA', 'TRANSFERENCIA', 'MIXTO')),
    estado          VARCHAR(20)   NOT NULL DEFAULT 'PAGADA'
                        CHECK (estado IN ('PENDIENTE', 'PAGADA', 'ANULADA')),
    fecha_emision   TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);


-- ── 9.2 DETALLE DE VENTA ───────────────────────────────────
-- Cada línea puede ser un producto del inventario, un servicio del catálogo,
-- o una descripción libre (para casos especiales)
CREATE TABLE detalle_venta (
    id              BIGSERIAL     PRIMARY KEY,
    venta_id        BIGINT        NOT NULL REFERENCES venta(id) ON DELETE CASCADE,
    producto_id     BIGINT        REFERENCES producto(id),
    servicio_id     BIGINT        REFERENCES servicio(id),
    descripcion     VARCHAR(200),                            -- Descripción libre si no hay FK
    cantidad        INT           NOT NULL DEFAULT 1 CHECK (cantidad > 0),
    precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal        DECIMAL(10,2) NOT NULL CHECK (subtotal >= 0),

    -- Al menos uno de los tres identificadores debe estar presente
    CONSTRAINT ck_detalle_tiene_item
        CHECK (producto_id IS NOT NULL OR servicio_id IS NOT NULL OR descripcion IS NOT NULL)
);


-- ============================================================
-- 10. ÍNDICES DE RENDIMIENTO
-- ============================================================

-- Pacientes
CREATE INDEX idx_paciente_propietario   ON paciente(propietario_id);
CREATE INDEX idx_paciente_nombre        ON paciente(nombre);

-- Citas
CREATE INDEX idx_cita_paciente          ON cita(paciente_id);
CREATE INDEX idx_cita_veterinario       ON cita(personal_id);
CREATE INDEX idx_cita_fecha             ON cita(fecha_hora);
CREATE INDEX idx_cita_estado            ON cita(estado);

-- Historia clínica
CREATE INDEX idx_historia_paciente      ON historia_clinica(paciente_id);
CREATE INDEX idx_historia_veterinario   ON historia_clinica(personal_id);
CREATE INDEX idx_historia_fecha         ON historia_clinica(fecha_consulta);
CREATE INDEX idx_historia_cita          ON historia_clinica(cita_id);

-- Vacunas
CREATE INDEX idx_vacuna_paciente        ON vacuna(paciente_id);
CREATE INDEX idx_vacuna_proxima_dosis   ON vacuna(fecha_proxima_dosis);

-- Recetas
CREATE INDEX idx_receta_historia        ON receta(historia_clinica_id);
CREATE INDEX idx_detalle_receta_receta  ON detalle_receta(receta_id);

-- Inventario
CREATE INDEX idx_producto_proveedor     ON producto(proveedor_id);
CREATE INDEX idx_producto_activo        ON producto(activo);
CREATE INDEX idx_producto_stock_critico ON producto(stock_actual) WHERE stock_actual <= stock_minimo;

-- Solicitudes de material
CREATE INDEX idx_solicitud_solicitante  ON solicitud_material(solicitante_id);
CREATE INDEX idx_solicitud_estado       ON solicitud_material(estado);

-- Ventas
CREATE INDEX idx_venta_propietario      ON venta(propietario_id);
CREATE INDEX idx_venta_personal         ON venta(personal_id);
CREATE INDEX idx_venta_fecha            ON venta(fecha_emision);
CREATE INDEX idx_venta_cita             ON venta(cita_id);
CREATE INDEX idx_detalle_venta_venta    ON detalle_venta(venta_id);


-- ============================================================
-- 11. VERIFICACIÓN FINAL
-- ============================================================
-- Ejecuta esto al final para confirmar que todo fue creado correctamente.

-- Listar las 15 tablas con su cantidad de columnas:
SELECT
    table_name,
    (SELECT COUNT(*)
     FROM information_schema.columns c
     WHERE c.table_name = t.table_name
       AND c.table_schema = 'public') AS columnas
FROM information_schema.tables t
WHERE table_schema = 'public'
  AND table_type = 'BASE TABLE'
ORDER BY table_name;

-- Verificar los 12 usuarios iniciales:
SELECT
    p.id,
    p.codigo_institucional,
    p.nombre_completo,
    r.nombre  AS rol,
    p.cargo,
    p.username,
    p.activo
FROM personal p
JOIN rol r ON r.id = p.rol_id
ORDER BY r.id, p.id;
