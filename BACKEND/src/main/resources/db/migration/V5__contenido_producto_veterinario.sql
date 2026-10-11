ALTER TABLE productos_veterinarios
    ADD COLUMN contenido_ml_por_unidad DOUBLE PRECISION NOT NULL DEFAULT 1.0;

ALTER TABLE productos_veterinarios
    ADD CONSTRAINT ck_productos_contenido_unidad
        CHECK (contenido_ml_por_unidad > 0);
