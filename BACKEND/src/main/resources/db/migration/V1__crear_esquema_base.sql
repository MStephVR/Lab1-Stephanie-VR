CREATE TABLE lotes (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL UNIQUE,
    ubicacion VARCHAR(180),
    proposito VARCHAR(180),
    CONSTRAINT ck_lotes_nombre_no_vacio CHECK (length(trim(nombre)) > 0)
);

CREATE TABLE animales (
    id BIGSERIAL PRIMARY KEY,
    codigo_arete VARCHAR(80) NOT NULL UNIQUE,
    raza VARCHAR(100),
    sexo VARCHAR(20),
    peso_kg DOUBLE PRECISION,
    fecha_nacimiento DATE,
    estado VARCHAR(30),
    lote_id BIGINT,
    CONSTRAINT fk_animales_lote FOREIGN KEY (lote_id) REFERENCES lotes (id),
    CONSTRAINT ck_animales_peso_positivo CHECK (peso_kg IS NULL OR peso_kg > 0),
    CONSTRAINT ck_animales_estado CHECK (estado IS NULL OR estado IN ('Activo', 'Inactivo'))
);

CREATE INDEX idx_animales_lote_id ON animales (lote_id);
CREATE INDEX idx_animales_estado ON animales (estado);
