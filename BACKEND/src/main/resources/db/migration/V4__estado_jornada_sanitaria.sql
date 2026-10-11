ALTER TABLE jornadas_sanitarias
    ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'PLANIFICADA';

ALTER TABLE jornadas_sanitarias
    ADD CONSTRAINT ck_jornadas_estado
        CHECK (estado IN ('PLANIFICADA', 'EJECUTADA', 'COSTEADA'));
