ALTER TABLE planes_sanitarios
    ADD COLUMN intervalo_minimo_dias INTEGER NOT NULL DEFAULT 7;

ALTER TABLE productos_veterinarios
    ADD COLUMN retiro_carne_dias INTEGER NOT NULL DEFAULT 0;

ALTER TABLE productos_veterinarios
    ADD COLUMN retiro_leche_dias INTEGER NOT NULL DEFAULT 0;

ALTER TABLE aplicaciones_sanitarias
    ADD COLUMN fecha_fin_retiro_carne DATE;

ALTER TABLE aplicaciones_sanitarias
    ADD COLUMN fecha_fin_retiro_leche DATE;

ALTER TABLE planes_sanitarios
    ADD CONSTRAINT ck_planes_intervalo_minimo CHECK (intervalo_minimo_dias >= 0);

ALTER TABLE productos_veterinarios
    ADD CONSTRAINT ck_productos_retiro_carne CHECK (retiro_carne_dias >= 0);

ALTER TABLE productos_veterinarios
    ADD CONSTRAINT ck_productos_retiro_leche CHECK (retiro_leche_dias >= 0);

CREATE TABLE jornadas_consumos_inventario (
    id BIGSERIAL PRIMARY KEY,
    jornada_id BIGINT NOT NULL,
    inventario_id BIGINT NOT NULL,
    cantidad_unidades INTEGER NOT NULL,
    costo_unitario NUMERIC(12, 2) NOT NULL,
    CONSTRAINT fk_consumos_jornada FOREIGN KEY (jornada_id) REFERENCES jornadas_sanitarias (id),
    CONSTRAINT fk_consumos_inventario FOREIGN KEY (inventario_id) REFERENCES inventario (id),
    CONSTRAINT uq_consumos_jornada_inventario UNIQUE (jornada_id, inventario_id),
    CONSTRAINT ck_consumos_cantidad CHECK (cantidad_unidades > 0),
    CONSTRAINT ck_consumos_costo CHECK (costo_unitario >= 0)
);

CREATE INDEX idx_consumos_jornada ON jornadas_consumos_inventario (jornada_id);
