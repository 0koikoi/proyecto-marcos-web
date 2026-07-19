-- BD truncate
-- Huellitas v3 — Esquema inicial consolidado (Flyway V1)
-- ya fue ejecutada en el Neon
-- A partir de aquí, todo cambio de esquema va en V2, V3... nunca editar este archivo.

-- EXTENSIONES
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- MODULO 1: roles, personal, auditoria
CREATE TABLE rol (
    id     BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE  -- ADMINISTRADOR, RECEPCION, VETERINARIO
);

CREATE TABLE personal (
    id                  BIGSERIAL PRIMARY KEY,
    codigo_institucional VARCHAR(10) NOT NULL UNIQUE,
    username            VARCHAR(50) NOT NULL UNIQUE,
    password            VARCHAR(255) NOT NULL,
    nombre              VARCHAR(100) NOT NULL,
    apellido            VARCHAR(100) NOT NULL,
    email               VARCHAR(150),
    telefono            VARCHAR(20),
    rol_id              BIGINT NOT NULL REFERENCES rol(id),
    activo              BOOLEAN NOT NULL DEFAULT TRUE,

    -- control de intentos de acceso
    intentos_fallidos   INTEGER NOT NULL DEFAULT 0,
    bloqueado_hasta     TIMESTAMPTZ,

    -- auditoría
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por          BIGINT REFERENCES personal(id),
    actualizado_en      TIMESTAMPTZ,
    actualizado_por     BIGINT REFERENCES personal(id)
);

CREATE INDEX idx_personal_username ON personal(username);
CREATE INDEX idx_personal_rol ON personal(rol_id);

-- MÓDULO 2: clientes y paciente
CREATE TABLE propietario (
    id              BIGSERIAL PRIMARY KEY,
    dni             VARCHAR(8) NOT NULL UNIQUE,
    nombres         VARCHAR(100) NOT NULL,
    apellidos       VARCHAR(100) NOT NULL,
    telefono        VARCHAR(20) NOT NULL,
    email           VARCHAR(150),
    direccion       VARCHAR(200),

    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por      BIGINT REFERENCES personal(id),
    actualizado_en  TIMESTAMPTZ,
    actualizado_por BIGINT REFERENCES personal(id)
);

CREATE INDEX idx_propietario_dni ON propietario(dni);
CREATE INDEX idx_propietario_telefono ON propietario(telefono);

CREATE TABLE paciente (
    id                          BIGSERIAL PRIMARY KEY,
    propietario_id              BIGINT NOT NULL REFERENCES propietario(id),
    nombre                      VARCHAR(100) NOT NULL,
    especie                     VARCHAR(30) NOT NULL,   -- PERRO, GATO, AVE, ROEDOR, REPTIL, OTRO
    raza                        VARCHAR(100),
    genero                      VARCHAR(10),             -- MACHO, HEMBRA
    fecha_nacimiento            DATE,
    fecha_nacimiento_estimada   BOOLEAN NOT NULL DEFAULT FALSE,
    peso_referencia             NUMERIC(5,2),             -- se actualiza automáticamente desde historia clínica
    esterilizado                BOOLEAN NOT NULL DEFAULT FALSE,
    alergias                    TEXT,
    estado                      VARCHAR(20) NOT NULL DEFAULT 'ACTIVO'
                                CHECK (estado IN ('ACTIVO','FALLECIDO','INACTIVO')),

    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por      BIGINT REFERENCES personal(id),
    actualizado_en  TIMESTAMPTZ,
    actualizado_por BIGINT REFERENCES personal(id)
);

CREATE INDEX idx_paciente_propietario ON paciente(propietario_id);
CREATE INDEX idx_paciente_estado ON paciente(estado);

-- MÓDULO 3: inventario

CREATE TABLE servicio (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    descripcion TEXT,
    precio      NUMERIC(10,2) NOT NULL CHECK (precio >= 0),
    activo      BOOLEAN NOT NULL DEFAULT TRUE,

    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por      BIGINT REFERENCES personal(id),
    actualizado_en  TIMESTAMPTZ,
    actualizado_por BIGINT REFERENCES personal(id)
);

CREATE TABLE producto (
    id              BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(150) NOT NULL,
    descripcion     TEXT,
    categoria       VARCHAR(50),
    precio_venta    NUMERIC(10,2) NOT NULL CHECK (precio_venta >= 0),
    stock_actual    INTEGER NOT NULL DEFAULT 0 CHECK (stock_actual >= 0),
    stock_minimo    INTEGER NOT NULL DEFAULT 0 CHECK (stock_minimo >= 0),
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    version         BIGINT NOT NULL DEFAULT 0,  -- optimistic locking (@Version en la entidad JPA)

    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por      BIGINT REFERENCES personal(id),
    actualizado_en  TIMESTAMPTZ,
    actualizado_por BIGINT REFERENCES personal(id)
);

CREATE INDEX idx_producto_nombre ON producto(nombre);
CREATE INDEX idx_producto_stock_bajo ON producto(stock_actual, stock_minimo);

CREATE TABLE proveedor (
    id              BIGSERIAL PRIMARY KEY,
    ruc             VARCHAR(11) NOT NULL UNIQUE,
    razon_social    VARCHAR(150) NOT NULL,
    contacto        VARCHAR(100),
    telefono        VARCHAR(20),
    email           VARCHAR(150),
    activo          BOOLEAN NOT NULL DEFAULT TRUE,

    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por      BIGINT REFERENCES personal(id),
    actualizado_en  TIMESTAMPTZ,
    actualizado_por BIGINT REFERENCES personal(id)
);

CREATE TABLE orden_compra (
    id              BIGSERIAL PRIMARY KEY,
    proveedor_id    BIGINT NOT NULL REFERENCES proveedor(id),
    personal_id     BIGINT NOT NULL REFERENCES personal(id),
    estado          VARCHAR(20) NOT NULL DEFAULT 'BORRADOR'
                    CHECK (estado IN ('BORRADOR','ENVIADA','RECIBIDA','PARCIAL','CANCELADA')),
    observaciones   TEXT,
    fecha_recepcion TIMESTAMPTZ,

    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por      BIGINT REFERENCES personal(id),
    actualizado_en  TIMESTAMPTZ,
    actualizado_por BIGINT REFERENCES personal(id)
);

CREATE TABLE detalle_orden_compra (
    id                  BIGSERIAL PRIMARY KEY,
    orden_compra_id     BIGINT NOT NULL REFERENCES orden_compra(id),
    producto_id         BIGINT NOT NULL REFERENCES producto(id),
    cantidad_solicitada INTEGER NOT NULL CHECK (cantidad_solicitada > 0),
    cantidad_recibida   INTEGER,
    precio_unitario     NUMERIC(10,2) NOT NULL CHECK (precio_unitario >= 0)
);

CREATE TABLE movimiento_stock (
    id            BIGSERIAL PRIMARY KEY,
    producto_id   BIGINT NOT NULL REFERENCES producto(id),
    tipo          VARCHAR(10) NOT NULL CHECK (tipo IN ('ENTRADA','SALIDA')),
    cantidad      INTEGER NOT NULL CHECK (cantidad > 0),
    origen        VARCHAR(50) NOT NULL,   -- VENTA, SOLICITUD, COMPRA, AJUSTE
    referencia_id BIGINT,                 -- id de venta/solicitud/orden que originó el movimiento
    creado_en     TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por    BIGINT REFERENCES personal(id)
);

CREATE INDEX idx_movimiento_producto ON movimiento_stock(producto_id);

CREATE TABLE solicitud_material (
    id                      BIGSERIAL PRIMARY KEY,
    personal_id_solicitante BIGINT NOT NULL REFERENCES personal(id),
    producto_id             BIGINT NOT NULL REFERENCES producto(id),
    cantidad                INTEGER NOT NULL CHECK (cantidad > 0),
    motivo                  TEXT,
    estado                  VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
                            CHECK (estado IN ('PENDIENTE','APROBADA','RECHAZADA','ENTREGADA')),
    personal_id_respuesta   BIGINT REFERENCES personal(id),
    fecha_solicitud         TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_respuesta         TIMESTAMPTZ
);

-- MÓDULO 4: Agenda
CREATE TABLE horario_atencion (
    id          BIGSERIAL PRIMARY KEY,
    personal_id BIGINT NOT NULL REFERENCES personal(id),
    dia_semana  VARCHAR(10) NOT NULL
                CHECK (dia_semana IN ('LUNES','MARTES','MIERCOLES','JUEVES','VIERNES','SABADO','DOMINGO')),
    hora_inicio TIME NOT NULL,
    hora_fin    TIME NOT NULL
);

CREATE TABLE excepcion_horario (
    id          BIGSERIAL PRIMARY KEY,
    personal_id BIGINT NOT NULL REFERENCES personal(id),
    fecha       DATE NOT NULL,
    tipo        VARCHAR(20) NOT NULL CHECK (tipo IN ('VACACIONES','LICENCIA','DIA_LIBRE')),
    motivo      TEXT
);

CREATE TABLE cita (
    id                  BIGSERIAL PRIMARY KEY,
    paciente_id         BIGINT NOT NULL REFERENCES paciente(id),
    personal_id         BIGINT NOT NULL REFERENCES personal(id),  -- veterinario asignado
    fecha_hora          TIMESTAMPTZ NOT NULL,
    duracion_minutos    INTEGER NOT NULL DEFAULT 30 CHECK (duracion_minutos > 0),
    motivo              VARCHAR(200),
    estado              VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
                        CHECK (estado IN ('PENDIENTE','EN_PROCESO','COMPLETADA','CANCELADA')),
    observaciones       TEXT,
 
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por      BIGINT REFERENCES personal(id),
    actualizado_en  TIMESTAMPTZ,
    actualizado_por BIGINT REFERENCES personal(id)
);
 
CREATE INDEX idx_cita_personal_fecha ON cita(personal_id, fecha_hora);
CREATE INDEX idx_cita_paciente ON cita(paciente_id);
 
-- =====================================================================
-- MÓDULO 5: ATENCIÓN CLÍNICA
-- =====================================================================
 
CREATE TABLE historia_clinica (
    id              BIGSERIAL PRIMARY KEY,
    paciente_id     BIGINT NOT NULL REFERENCES paciente(id),
    personal_id     BIGINT NOT NULL REFERENCES personal(id),  -- veterinario tratante
    cita_id         BIGINT REFERENCES cita(id),
    fecha           TIMESTAMPTZ NOT NULL DEFAULT now(),
    peso_kg         NUMERIC(5,2),
    temperatura     NUMERIC(4,1),
    diagnostico     TEXT,
    tratamiento     TEXT,
    observaciones   TEXT,
 
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por      BIGINT REFERENCES personal(id)
);
 
CREATE INDEX idx_historia_paciente ON historia_clinica(paciente_id);
CREATE INDEX idx_historia_personal ON historia_clinica(personal_id);
 
CREATE TABLE vacuna (
    id                  BIGSERIAL PRIMARY KEY,
    paciente_id         BIGINT NOT NULL REFERENCES paciente(id),
    historia_clinica_id BIGINT REFERENCES historia_clinica(id),
    personal_id         BIGINT NOT NULL REFERENCES personal(id),
    nombre              VARCHAR(100) NOT NULL,
    lote                VARCHAR(50),
    fecha_aplicacion    DATE NOT NULL,
    fecha_proxima_dosis DATE
);
 
CREATE TABLE receta (
    id                  BIGSERIAL PRIMARY KEY,
    historia_clinica_id BIGINT NOT NULL REFERENCES historia_clinica(id),
    personal_id         BIGINT NOT NULL REFERENCES personal(id),
    fecha               TIMESTAMPTZ NOT NULL DEFAULT now(),
    indicaciones        TEXT
);
 
CREATE TABLE detalle_receta (
    id          BIGSERIAL PRIMARY KEY,
    receta_id   BIGINT NOT NULL REFERENCES receta(id),
    medicamento VARCHAR(150) NOT NULL,
    dosis       VARCHAR(100),
    frecuencia  VARCHAR(100),
    duracion    VARCHAR(100),
    producto_id BIGINT REFERENCES producto(id)  -- opcional: enlaza con inventario para poder venderlo
);
 
-- =====================================================================
-- MÓDULO 6: VENTAS Y FACTURACIÓN
-- =====================================================================
 
CREATE TABLE venta (
    id                  BIGSERIAL PRIMARY KEY,
    propietario_id      BIGINT REFERENCES propietario(id),
    cita_id             BIGINT REFERENCES cita(id),
    personal_id         BIGINT NOT NULL REFERENCES personal(id),  -- quien vendió
    fecha               TIMESTAMPTZ NOT NULL DEFAULT now(),
    total               NUMERIC(10,2) NOT NULL CHECK (total >= 0),
    tipo_pago           VARCHAR(20) NOT NULL,   -- EFECTIVO, TARJETA, YAPE, PLIN, TRANSFERENCIA
    estado              VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
                        CHECK (estado IN ('PENDIENTE','PAGADA','ANULADA')),
    motivo_anulacion    TEXT,
    anulado_por         BIGINT REFERENCES personal(id),
    anulado_en          TIMESTAMPTZ,
 
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_por      BIGINT REFERENCES personal(id)
);
 
CREATE INDEX idx_venta_fecha ON venta(fecha);
CREATE INDEX idx_venta_propietario ON venta(propietario_id);
 
CREATE TABLE detalle_venta (
    id              BIGSERIAL PRIMARY KEY,
    venta_id        BIGINT NOT NULL REFERENCES venta(id),
    producto_id     BIGINT REFERENCES producto(id),
    servicio_id     BIGINT REFERENCES servicio(id),
    cantidad        INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC(10,2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal        NUMERIC(10,2) NOT NULL CHECK (subtotal >= 0),
 
    CONSTRAINT chk_detalle_venta_una_linea CHECK (
        (producto_id IS NOT NULL AND servicio_id IS NULL) OR
        (producto_id IS NULL AND servicio_id IS NOT NULL)
    )
);
 
-- =====================================================================
-- DATOS SEMILLA
-- =====================================================================
 
INSERT INTO rol (nombre) VALUES ('ADMINISTRADOR'), ('RECEPCION'), ('VETERINARIO');
 
-- Usuario administrador inicial — CAMBIAR la contraseña apenas se levante el ambiente
INSERT INTO personal (codigo_institucional, username, password, nombre, apellido, rol_id, activo)
VALUES (
    'C000001',
    'admin',
    crypt('Huellitas2025!', gen_salt('bf', 12)),
    'Admin',
    'Sistema',
    (SELECT id FROM rol WHERE nombre = 'ADMINISTRADOR'),
    TRUE
);