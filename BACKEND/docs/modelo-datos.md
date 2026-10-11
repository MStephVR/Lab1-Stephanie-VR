# Modelo de datos

## Modelo relacional

PostgreSQL contiene el dominio transaccional: lotes y animales, catálogo de
productos, inventario por lote de fabricación, proveedores y compras, usuarios,
planes sanitarios, jornadas, aplicaciones, presupuestos previos y alternativas
de producto. Las relaciones y reglas que deben ser consistentes se expresan con
llaves foráneas, restricciones `CHECK`, unicidad e índices.

Las migraciones se ejecutan en orden con Flyway. `V1` crea la base de animales
y lotes; `V2` agrega el subdominio sanitario, inventario y compras sin modificar
la migración ya aplicada; `V3` incorpora presupuestos y alternativas; `V4`
agrega el estado de jornada, `V5` el contenido por presentación del producto y
`V6` asocia las jornadas con un lote y un plan sanitario y agrega el estado
activo del lote.

## Normalización relacional

Las tablas separan entidades con identidad y ciclo de vida propios (animales,
lotes, productos, proveedores, usuarios y jornadas), y las relaciones se
representan con claves foráneas en vez de repetir sus datos descriptivos. Por
ejemplo, `animales.lote_id` y `compras.proveedor_id` guardan la identidad de la
relación, no copias del nombre del lote o proveedor; así, un cambio de nombre se
actualiza en un solo lugar. `aplicaciones_sanitarias` registra cada aplicación
y referencia al animal, producto y, cuando corresponde, jornada, evitando
listas repetidas o columnas de aplicaciones numeradas en esas entidades.

Las relaciones muchos a muchos se descomponen en tablas de unión: `plan_producto`
usa la clave compuesta `(plan_id, producto_id)`. `inventario` es una entidad
separada por lote de fabricación, porque vencimiento, cantidad y costo
corresponden a cada lote, no al producto en general; `(producto_id,
lote_fabricacion)` impide duplicar el mismo lote para un producto. Las
alternativas y los presupuestos también son registros propios: conservan los
valores de precio/costo y condiciones evaluados en ese momento, mientras
referencian las entidades con las que se relacionan. Esto evita anomalías de
inserción, actualización y eliminación por duplicación, y las claves foráneas,
restricciones `UNIQUE`, `CHECK` y `NOT NULL` hacen cumplir la integridad.

La normalización se aplica a los datos operacionales; los valores históricos de
presupuestos y alternativas son snapshots deliberados, no duplicación accidental.

## Decisiones de diseño

Inventario es una entidad propia porque un producto puede existir en varios
lotes de fabricación, con vencimientos y costos unitarios diferentes. La
aplicación sanitaria referencia al animal, producto y jornada para conservar el
historial y calcular consumos y costos. La tabla `plan_producto` resuelve la
relación muchos a muchos entre planes y productos. Un presupuesto conserva el
producto actual, sus costos estimados y, opcionalmente, la jornada que se
ejecutará; una alternativa se asocia al producto que puede reemplazar y al
proveedor que la ofrece. Esto permite filtrar alternativas por eficacia y plazo
de entrega antes de calcular el ahorro.

## Índices relacionales y consultas

Las migraciones declaran ocho índices explícitos para filtros usados por las
consultas del backend o para los listados previstos. Los índices creados por
claves primarias y restricciones `UNIQUE` no están incluidos en este conteo.

| Índice | Consulta que lo motiva | Justificación |
| --- | --- | --- |
| `idx_animales_lote_id (lote_id)` | `SELECT ... FROM animales WHERE lote_id = :loteId` (`findByLoteId`) | Agrupa y localiza los animales de un lote sin recorrer toda la tabla. |
| `idx_animales_estado (estado)` | `SELECT ... FROM animales WHERE estado = 'Activo'` | Permite filtrar listados por estado sin revisar todos los animales. |
| `idx_inventario_producto_vencimiento (producto_id, fecha_vencimiento)` | `SELECT ... FROM inventario WHERE producto_id = :productoId AND fecha_vencimiento > :fecha AND cantidad_disponible > :cantidad` (`findByProductoIdAndFechaVencimientoAfterAndCantidadDisponibleGreaterThan`) | La igualdad por producto y el rango de vencimiento usan las columnas iniciales del índice; la cantidad se comprueba como filtro adicional. |
| `idx_aplicaciones_animal_fecha (animal_id, fecha_aplicacion)` | `SELECT ... FROM aplicaciones_sanitarias WHERE animal_id = :animalId ORDER BY fecha_aplicacion DESC` (`findByAnimalIdOrderByFechaAplicacionDesc`) | Busca el historial de un animal y facilita devolverlo ordenado por fecha. |
| `idx_aplicaciones_jornada (jornada_id)` | `SELECT ... FROM aplicaciones_sanitarias WHERE jornada_id = :jornadaId ORDER BY fecha_aplicacion DESC` (`findByJornadaWithDetails`) | Reduce las aplicaciones a las de la jornada; el índice no cubre el orden por fecha. |
| `idx_compras_proveedor_fecha (proveedor_id, fecha_compra)` | `SELECT ... FROM compras WHERE proveedor_id = :proveedorId AND fecha_compra BETWEEN :desde AND :hasta` (`findByProveedorIdAndFechaCompraBetween`) | La igualdad por proveedor seguida del rango de fechas coincide con el orden del índice. |
| `idx_alternativas_producto_reglas (producto_actual_id, activo, eficacia, plazo_entrega_dias)` | `SELECT ... FROM alternativas_producto WHERE producto_actual_id = :id AND activo = TRUE AND eficacia >= :minima AND plazo_entrega_dias <= :maximo` (`findByProductoActualIdAndActivoTrueAndEficaciaGreaterThanEqualAndPlazoEntregaDiasLessThanEqual`) | Filtra primero por producto y estado; el resto de las condiciones acota las alternativas candidatas. |
| `idx_presupuestos_fecha (fecha_presupuesto)` | `SELECT ... FROM presupuestos_jornada WHERE fecha_presupuesto BETWEEN :desde AND :hasta` (`findByFechaPresupuestoBetween`) | Permite consultar presupuestos de un intervalo de fechas mediante un rango sobre la columna indexada. |

## Subdominio documental en MongoDB

La colección `bitacora_eventos` guarda auditoría: entidad afectada, acción,
usuario, detalle y fecha. Se eligió MongoDB porque el detalle puede cambiar
según el tipo de evento y no participa en las transacciones del inventario.

1. **¿Qué crece sin límite?** La cantidad de eventos aumenta con cada operación
   auditada; cada evento se guarda como un documento independiente, no en un
   arreglo creciente dentro del documento de un animal o producto.
2. **¿Qué se consulta junto?** Un evento completo se consulta como una unidad;
   por eso sus datos propios, incluido el `detalle` variable, se incrustan en
   un único documento.
3. **¿Qué se incrusta y qué se referencia?** Se incrustan los datos del evento;
   no se duplican animales ni productos y se conserva `entidad` como nombre
   lógico y `usuario` como texto histórico, sin referenciar documentos externos.

En los perfiles `local` y `docker`, `MongoSchemaInitializer` crea la colección
con un validador JSON Schema si aún no existe y asegura índices por fecha y por
entidad/acción; `DataSeeder` agrega un documento de ejemplo.

## Capa de negocio

`JornadaSanitariaService` aplica el flujo de dos fases del caso de uso:
`planificar` crea la jornada en estado `PLANIFICADA`, asociándola a un lote
activo y a un plan sanitario con productos; `ejecutar` es la confirmación
transaccional. La ejecución exige que se informe exactamente una vez cada
animal activo del lote, verifica que los productos coincidan con el plan,
calcula la dosis individual como peso por dosis por kilogramo, comprueba stock
vigente y lo consume por vencimiento próximo (FEFO), registra las aplicaciones
y cambia el estado a `EJECUTADA`. Todas esas escrituras se confirman o revierten
juntas. La prueba de integración
`revierteEjecucionCompletaSiFallaElRegistroDeAplicacion` usa PostgreSQL con
Testcontainers para inyectar un fallo en el registro de la aplicación después
de actualizar inventario; comprueba que se observó ese fallo y que el rollback
restauró existencias, aplicaciones y estado. La migración `V6` asocia las
jornadas con lote y plan y agrega el estado activo del lote.
Los fallos de stock y de transición de estado usan excepciones de negocio
expresivas (`InventarioInsuficienteException` y
`JornadaNoEjecutableException`), siguiendo el enfoque del taller.
`CostoJornadaService` solo procesa jornadas ejecutadas, verifica que todas las
aplicaciones correspondan a un producto y que el inventario vigente alcance
para cubrir las unidades redondeadas hacia arriba; consume primero los lotes
con vencimiento más próximo, guarda el costo y estado de la jornada y descuenta
existencias en la misma transacción. La migración `V5` persiste el contenido de
cada presentación para que ambos procesos apliquen el mismo cálculo. Las reglas
fallidas lanzan `ReglaNegocioException`, que provoca rollback. La prueba
`revierteCostoYEstadoSiFallaElDecrementoDeInventario` usa PostgreSQL con
Testcontainers y fuerza el error al actualizar inventario después de guardar
la jornada; verifica que la excepción viene del fallo inyectado y que ni el
costo ni el estado de la jornada quedaron persistidos.

Las solicitudes y respuestas de ambos procesos son records validados con Bean
Validation y no exponen entidades JPA; el servicio convierte explícitamente las
entidades a DTOs de salida.

### Patrones de diseño aplicados

- **Strategy (`CostoProductoStrategy` / `CostoPorUnidadStrategy`).** Señal: el
  cálculo de unidades y costo depende de la presentación del producto y puede
  variar sin que cambie la orquestación; por eso el servicio delega ese
  cálculo a una estrategia intercambiable.
- **Factory (`JornadaSanitariaFactory`).** Señal: planificar y ejecutar
  construyen entidades relacionadas con valores derivados y fechas; la fábrica
  concentra esa creación para mantener consistentes jornadas y aplicaciones.

Los repositorios de Spring Data aplican además el patrón Repository para
mantener el acceso a datos separado de las reglas.

La transacción cubre PostgreSQL; no se escribe en MongoDB durante el cálculo,
porque un commit PostgreSQL no puede hacer atómica una escritura MongoDB sin una
solución distribuida adicional. La bitácora MongoDB permanece como registro
complementario fuera de esta garantía.

## API REST (Laboratorio 5)

La capa de presentación expone animales, lotes, productos veterinarios,
inventarios, planes, proveedores, compras y jornadas bajo `/api/v1`; cada controlador delega en
servicios y serializa records DTO, nunca entidades JPA. Las colecciones usan
`Pageable` con orden configurable y respuesta `PagedModel` con metadatos
estables; las jornadas se listan limitadas al propietario autenticado. Los
filtros de animal (`estado`, `raza`) y
producto (rango de precio) se implementan con `BusinessSpecifications`.
`POST /auth/login` autentica usuarios persistidos con contraseñas BCrypt y
emite JWT HMAC; Spring Security opera sin sesión, con roles `ADMIN` y
`VETERINARIO`. La planificación guarda el usuario propietario de la jornada;
el servicio verifica propiedad antes de exponer, ejecutar o costear ese recurso.

`ApiExceptionHandler` traduce errores de formato a 400, recursos ausentes a
404, reglas de negocio a 422 y conflictos de estado/datos a 409 con RFC 9457
Problem Details; los rechazos del filtro de seguridad también tienen cuerpo
Problem Details y no exponen trazas. Swagger UI se sirve en
`/swagger-ui/index.html`; las peticiones reproducibles están en
`docs/sisganado-api.http`. `PersistenceIntegrationTest` prueba sobre PostgreSQL
con Testcontainers las respuestas 201 (incluyendo `Location`), 400, 401, 403,
404 y 422, además de verificar que la jornada solo es visible para su dueño.