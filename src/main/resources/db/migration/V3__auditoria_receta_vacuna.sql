-- Agrega columnas de auditoría a receta y vacuna, para que Receta y Vacuna
-- puedan extender la clase Auditable igual que el resto de entidades relevantes
-- (creado_en, creado_por, actualizado_en, actualizado_por).

ALTER TABLE receta
    ADD COLUMN creado_en      TIMESTAMPTZ,
    ADD COLUMN creado_por     BIGINT REFERENCES personal(id),
    ADD COLUMN actualizado_en TIMESTAMPTZ,
    ADD COLUMN actualizado_por BIGINT REFERENCES personal(id);

ALTER TABLE vacuna
    ADD COLUMN creado_en      TIMESTAMPTZ,
    ADD COLUMN creado_por     BIGINT REFERENCES personal(id),
    ADD COLUMN actualizado_en TIMESTAMPTZ,
    ADD COLUMN actualizado_por BIGINT REFERENCES personal(id);

-- Backfill de registros existentes: usan la fecha propia de la receta/vacuna
-- como fecha de creación, para no dejar el campo NOT NULL sin valor.
UPDATE receta SET creado_en = fecha WHERE creado_en IS NULL;
UPDATE vacuna SET creado_en = fecha_aplicacion WHERE creado_en IS NULL;

ALTER TABLE receta ALTER COLUMN creado_en SET NOT NULL;
ALTER TABLE vacuna ALTER COLUMN creado_en SET NOT NULL;
