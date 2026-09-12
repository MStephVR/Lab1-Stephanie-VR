# Modelo de datos

## Modelo relacional

PostgreSQL contiene el dominio transaccional: lotes y animales, catálogo de
productos, inventario por lote de fabricación, proveedores y compras, usuarios,
planes sanitarios, jornadas y aplicaciones. Las relaciones y reglas que deben
ser consistentes se expresan con llaves foráneas, restricciones `CHECK`,
unicidad e índices.

Las migraciones se ejecutan en orden con Flyway. `V1` crea la base de animales
y lotes; `V2` agrega el subdominio sanitario, inventario y compras sin modificar
la migración ya aplicada.

## Decisiones de diseño

Inventario es una entidad propia porque un producto puede existir en varios
lotes de fabricación, con vencimientos y costos unitarios diferentes. La
aplicación sanitaria referencia al animal, producto y jornada para conservar el
historial y calcular consumos y costos. La tabla `plan_producto` resuelve la
relación muchos a muchos entre planes y productos.

## Subdominio documental en MongoDB

La colección `bitacora_eventos` guarda auditoría: entidad afectada, acción,
usuario, detalle y fecha. Se eligió MongoDB porque el detalle puede cambiar
según el tipo de evento y no participa en las transacciones del inventario.

1. **¿Qué cambia con frecuencia?** El contenido de `detalle` y las acciones
   registradas evolucionan con los procesos del sistema.
2. **¿Qué se consulta junto?** Un evento completo se consulta como una unidad;
   por eso sus campos se mantienen incrustados en un único documento.
3. **¿Qué se referencia?** No se duplican animales, productos o usuarios en
   la bitácora; se conserva el nombre lógico en `entidad` y el usuario que
   ejecutó la acción como texto histórico.

La aplicación crea la colección con un validador JSON Schema, índices por fecha
y por entidad/acción, y un documento de ejemplo mediante `DataSeeder`.