CREATE TABLE proveedores (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(160) NOT NULL UNIQUE,
    telefono VARCHAR(40),
    correo VARCHAR(160),
    CONSTRAINT ck_proveedores_nombre_no_vacio CHECK (length(trim(nombre)) > 0)
);

CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre_completo VARCHAR(180) NOT NULL,
    rol VARCHAR(40) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_usuarios_nombre_no_vacio CHECK (length(trim(nombre_completo)) > 0)
);

CREATE TABLE productos_veterinarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(160) NOT NULL UNIQUE,
    tipo VARCHAR(80) NOT NULL,
    unidad_medida VARCHAR(30) NOT NULL,
    dosis_minima DOUBLE PRECISION,
    dosis_maxima DOUBLE PRECISION,
    precio_unitario NUMERIC(12, 2),
    CONSTRAINT ck_productos_dosis CHECK (dosis_minima IS NULL OR dosis_minima >= 0),
    CONSTRAINT ck_productos_dosis_rango CHECK (dosis_maxima IS NULL OR dosis_minima IS NULL OR dosis_maxima >= dosis_minima),
    CONSTRAINT ck_productos_precio CHECK (precio_unitario IS NULL OR precio_unitario >= 0)
);

CREATE TABLE planes_sanitarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(160) NOT NULL UNIQUE,
    descripcion VARCHAR(500),
    edad_objetivo_meses INTEGER,
    CONSTRAINT ck_planes_edad CHECK (edad_objetivo_meses IS NULL OR edad_objetivo_meses >= 0)
);

CREATE TABLE jornadas_sanitarias (
    id BIGSERIAL PRIMARY KEY,
    fecha DATE NOT NULL,
    descripcion VARCHAR(500),
    cantidad_animales INTEGER NOT NULL,
    costo_total NUMERIC(14, 2) NOT NULL DEFAULT 0,
    usuario_id BIGINT,
    CONSTRAINT fk_jornadas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT ck_jornadas_cantidad CHECK (cantidad_animales > 0),
    CONSTRAINT ck_jornadas_costo CHECK (costo_total >= 0)
);

CREATE TABLE inventario (
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    lote_fabricacion VARCHAR(100) NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    cantidad_disponible INTEGER NOT NULL,
    costo_unitario NUMERIC(12, 2) NOT NULL,
    CONSTRAINT fk_inventario_producto FOREIGN KEY (producto_id) REFERENCES productos_veterinarios (id),
    CONSTRAINT uq_inventario_lote UNIQUE (producto_id, lote_fabricacion),
    CONSTRAINT ck_inventario_cantidad CHECK (cantidad_disponible >= 0),
    CONSTRAINT ck_inventario_costo CHECK (costo_unitario >= 0)
);

CREATE TABLE compras (
    id BIGSERIAL PRIMARY KEY,
    proveedor_id BIGINT NOT NULL,
    fecha_compra DATE NOT NULL,
    total NUMERIC(14, 2) NOT NULL,
    CONSTRAINT fk_compras_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedores (id),
    CONSTRAINT ck_compras_total CHECK (total >= 0)
);

CREATE TABLE aplicaciones_sanitarias (
    id BIGSERIAL PRIMARY KEY,
    animal_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    jornada_id BIGINT,
    fecha_aplicacion DATE NOT NULL,
    dosis_aplicada DOUBLE PRECISION NOT NULL,
    proxima_fecha_aplicacion DATE,
    observacion VARCHAR(500),
    CONSTRAINT fk_aplicaciones_animal FOREIGN KEY (animal_id) REFERENCES animales (id),
    CONSTRAINT fk_aplicaciones_producto FOREIGN KEY (producto_id) REFERENCES productos_veterinarios (id),
    CONSTRAINT fk_aplicaciones_jornada FOREIGN KEY (jornada_id) REFERENCES jornadas_sanitarias (id),
    CONSTRAINT ck_aplicaciones_dosis CHECK (dosis_aplicada > 0)
);

CREATE TABLE plan_producto (
    plan_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    PRIMARY KEY (plan_id, producto_id),
    CONSTRAINT fk_plan_producto_plan FOREIGN KEY (plan_id) REFERENCES planes_sanitarios (id),
    CONSTRAINT fk_plan_producto_producto FOREIGN KEY (producto_id) REFERENCES productos_veterinarios (id)
);

CREATE INDEX idx_inventario_producto_vencimiento ON inventario (producto_id, fecha_vencimiento);
CREATE INDEX idx_aplicaciones_animal_fecha ON aplicaciones_sanitarias (animal_id, fecha_aplicacion);
CREATE INDEX idx_aplicaciones_jornada ON aplicaciones_sanitarias (jornada_id);
CREATE INDEX idx_compras_proveedor_fecha ON compras (proveedor_id, fecha_compra);