-- Agregar columna para forzar cambio de contraseña temporal
ALTER TABLE personal ADD COLUMN debe_cambiar_password BOOLEAN NOT NULL DEFAULT false;
