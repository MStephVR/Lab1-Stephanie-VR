CREATE TABLE alternativas_producto (
    id BIGSERIAL PRIMARY KEY,
    producto_actual_id BIGINT NOT NULL,
    proveedor_id BIGINT NOT NULL,
    nombre VARCHAR(160) NOT NULL,
    precio_unitario NUMERIC(12, 2) NOT NULL,
    eficacia DOUBLE PRECISION NOT NULL,
    plazo_entrega_dias INTEGER NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_alternativas_producto_actual FOREIGN KEY (producto_actual_id) REFERENCES productos_veterinarios (id),
    CONSTRAINT fk_alternativas_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedores (id),
    CONSTRAINT ck_alternativas_precio CHECK (precio_unitario >= 0),
    CONSTRAINT ck_alternativas_eficacia CHECK (eficacia >= 0 AND eficacia <= 100),
    CONSTRAINT ck_alternativas_plazo CHECK (plazo_entrega_dias >= 0)
);

CREATE TABLE presupuestos_jornada (
    id BIGSERIAL PRIMARY KEY,
    fecha_presupuesto DATE NOT NULL,
    cantidad_animales INTEGER NOT NULL,
    producto_actual_id BIGINT NOT NULL,
    jornada_id BIGINT,
    costo_producto NUMERIC(14, 2) NOT NULL,
    costos_adicionales NUMERIC(14, 2) NOT NULL DEFAULT 0,
    costo_total NUMERIC(14, 2) NOT NULL,
    CONSTRAINT fk_presupuestos_producto_actual FOREIGN KEY (producto_actual_id) REFERENCES productos_veterinarios (id),
    CONSTRAINT fk_presupuestos_jornada FOREIGN KEY (jornada_id) REFERENCES jornadas_sanitarias (id),
    CONSTRAINT ck_presupuestos_cantidad CHECK (cantidad_animales > 0),
    CONSTRAINT ck_presupuestos_costo_producto CHECK (costo_producto >= 0),
    CONSTRAINT ck_presupuestos_costos_adicionales CHECK (costos_adicionales >= 0),
    CONSTRAINT ck_presupuestos_costo_total CHECK (costo_total >= 0)
);

CREATE INDEX idx_alternativas_producto_reglas
    ON alternativas_producto (producto_actual_id, activo, eficacia, plazo_entrega_dias);
CREATE INDEX idx_presupuestos_fecha ON presupuestos_jornada (fecha_presupuesto);