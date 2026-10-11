ALTER TABLE lotes
    ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE jornadas_sanitarias
    ADD COLUMN lote_id BIGINT;

ALTER TABLE jornadas_sanitarias
    ADD COLUMN plan_id BIGINT;

ALTER TABLE jornadas_sanitarias
    ADD CONSTRAINT fk_jornadas_lote FOREIGN KEY (lote_id) REFERENCES lotes (id);

ALTER TABLE jornadas_sanitarias
    ADD CONSTRAINT fk_jornadas_plan FOREIGN KEY (plan_id) REFERENCES planes_sanitarios (id);
