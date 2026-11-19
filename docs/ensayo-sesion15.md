# Ensayo de defensa · Sesión 15

## Datos del ensayo

| Campo | Completar durante el ensayo |
| --- | --- |
| Proyecto evaluado | SisGanado · gestión sanitaria de finca |
| Integrante: | María Stephanie Vargas Ramírez |



## Preguntas del ensayo y puntos para responder

Dirigir al menos una pregunta a cada integrante. El proyecto registra una
integrante; si se presenta en pareja, agregue el nombre y la asignación de la
segunda persona antes del ensayo.

| # | Pregunta | Dirigida a | Puntos que debe cubrir la respuesta |
| ---: | --- | --- | --- |
| 1 | ¿Qué pasa si falla el registro de una aplicación después de consumir inventario? ¿Cómo se comprueba? | María Stephanie Vargas Ramírez | `@Transactional` revierte escrituras relacionales; prueba de integración inyecta el fallo y verifica inventario, aplicaciones y estado restaurados. |
| 2 | ¿Qué pasa si dos operaciones actualizan simultáneamente un inventario? | María Stephanie Vargas Ramírez | `@Version` detecta versión obsoleta; el manejador traduce el conflicto a HTTP 409; señalar migración y prueba. |
| 3 | ¿Cómo se evita que una cuenta de consulta o un usuario consulte jornadas ajenas? | María Stephanie Vargas Ramírez | Roles, reglas `@PreAuthorize`, autorización por propietario y respuestas 403/404 según el caso. |



### Persistencia

**¿Dónde aparecía un problema N+1 y cómo lo resolvieron?**

Al consultar entidades con relaciones diferidas y luego recorrerlas una por
una, ORM puede emitir una consulta adicional por cada relación. En los caminos
que necesitan los datos relacionados, los repositorios usan consultas JPQL con
`join fetch`, por ejemplo `AplicacionSanitariaRepository.findByJornadaWithDetails`
carga animal, producto y jornada en una consulta, y
`InventarioRepository.findDisponibleNoVencido` trae inventario y producto juntos.
Los listados paginados no deberían agregar un `join fetch` de colecciones sin
evaluar su efecto en la paginación. Las asociaciones se mantienen `LAZY` para
no cargar datos que ese caso de uso no necesita.

**¿Por qué esta relación se carga de forma diferida?**

Una jornada tiene asociaciones a usuario, lote, plan y aplicaciones; un lote
puede tener muchos animales y un producto muchos inventarios. Cargarlas siempre
puede traer colecciones grandes, aumentar memoria y multiplicar filas. `LAZY`
deja que cada caso de uso solicite explícitamente lo que necesita; cuando se
requieren detalles relacionados, el repositorio usa `join fetch` y los mapea a
DTO dentro de la transacción.

### Transacciones

**¿Qué ocurre si falla el último paso del proceso principal? ¿Cómo lo comprueba
una prueba?**

La ejecución de jornada se realiza en una transacción relacional: consumo de
inventario, aplicaciones y cambio de estado se confirman juntos o se revierten.
La prueba `revierteEjecucionCompletaSiFallaElRegistroDeAplicacion` instala un
trigger de prueba que falla al registrar la aplicación; comprueba que la
excepción ocurrió y que la cantidad de inventario, las aplicaciones y el
estado de la jornada quedaron como antes. MongoDB no comparte una transacción
ACID con PostgreSQL, por lo que la atomicidad descrita corresponde a las
escrituras relacionales.

### Negocio y diseño

**¿Dónde vive esta regla y por qué no en el controlador?**

Las reglas de planificar y ejecutar jornadas viven en `JornadaSanitariaService`;
el controlador traduce HTTP a una llamada del servicio. Así la regla no depende
de HTTP, puede reutilizarse y se prueba directamente o mediante integración.
La construcción consistente de entidades está en `JornadaSanitariaFactory`.

**¿Qué patrón aplicaron y qué problema resolvía?**

`CostoProductoStrategy` representa cálculos intercambiables. Sus
implementaciones calculan costo por unidad (`CostoPorUnidadStrategy`) o costo
proporcional por mililitro (`CostoPorMililitroStrategy`), evitando mezclar las
fórmulas en controladores o repetir condicionales. `JornadaSanitariaFactory`
centraliza la creación de jornadas y aplicaciones con fechas de próxima
aplicación y retiro.

### API

**¿Por qué este endpoint responde 422 y no 400?**

Cuando la solicitud tiene estructura válida, pero viola una regla de negocio,
por ejemplo una transición no permitida, el manejador responde
`422 Unprocessable Entity` con Problem Details. `400 Bad Request` queda para
una solicitud sintácticamente o formalmente inválida, como un campo de
ordenamiento inexistente o errores de validación de entrada según el handler.
`409 Conflict` se usa para conflictos de estado, integridad o concurrencia.

**¿Cómo se versiona la API?**

La versión forma parte de la ruta: los recursos REST están bajo `/api/v1`.
Un cambio incompatible puede introducir `/api/v2` y mantener v1 durante una
transición, en vez de cambiar silenciosamente el contrato existente.

### Seguridad

**¿Cómo evitan que un usuario consulte recursos ajenos?**

El JWT contiene el usuario (`sub`) y los roles. Spring Security verifica
autenticación y rol; además, los servicios de jornada consultan y ejecutan
recursos en el contexto del usuario autenticado. Un recurso de otro propietario
se responde como 404 para no revelar que existe. Los catálogos de escritura y
las cuentas requieren ADMIN; planificar/ejecutar jornadas requiere ADMIN o
VETERINARIO. USUARIO tiene acceso de consulta.

**¿Dónde está el secreto JWT en producción?**

En la variable de entorno `SISGANADO_JWT_SECRET` del servicio API en Render.
No se guarda en Git ni en el frontend. El archivo de propiedades de producción
lo lee de esa variable y Spring valida que tenga al menos 32 bytes.

### Concurrencia

**¿Qué pasa si dos usuarios modifican este dato al mismo tiempo?**

El inventario tiene `@Version`. Si dos transacciones leen la misma versión y
una confirma primero, la actualización de la otra detecta que la versión ya
cambió y no sobrescribe silenciosamente la cantidad nueva. El manejador global
traduce el conflicto optimista a HTTP 409 y pide volver a cargar el registro.

**¿Cómo lo muestran en una prueba?**

`PersistenceIntegrationTest` carga dos copias de una fila de inventario con la
misma versión, actualiza primero una y luego intenta guardar la segunda; la
prueba espera `OptimisticLockingFailureException`. Otra prueba del handler
comprueba la respuesta 409 con Problem Details. La columna de versión se agrega
con la migración `V9__versionar_inventario.sql`.

### Pruebas y despliegue

**¿Qué verifica esta prueba y qué no?**

La prueba de rollback verifica una propiedad concreta: si falla el último paso
relacional de la ejecución, se revierten las escrituras anteriores de esa
transacción en PostgreSQL. No verifica fallos distribuidos de MongoDB, carga
real de producción, disponibilidad futura ni todos los escenarios posibles;
eso requiere pruebas y monitoreo adicionales.

**¿Qué impide que un cambio con errores llegue a producción?**

En pushes a `master`, el job `verificar` compila y prueba el proyecto raíz y el
backend, aplica Checkstyle y cobertura, exige al menos 40 pruebas exitosas y
construye la SPA. El job `desplegar` depende de `verificar`: si falla, no se
invoca el deploy hook de Render. GitHub también permite exigir el check en la
protección de rama; el workflow por sí solo no activa esa regla remota.

### Arquitectura

**¿Por qué eligieron MongoDB para esta parte?**

La bitácora guarda documentos de eventos con campos comunes y un detalle que
puede variar según la operación. MongoDB facilita almacenar ese documento
completo sin agregar una tabla rígida por cada tipo de detalle. PostgreSQL se
mantiene como fuente de verdad para inventario y operaciones que necesitan
relaciones y transacciones. El costo es operar dos bases y no tener una
transacción ACID distribuida entre ellas.

**¿Qué cambiarían si el sistema tuviera cien veces más usuarios?**

Primero mediría latencia, consultas, conexiones y errores; no separaría
servicios sin evidencia. Revisaría índices con planes de consulta, paginación,
pool de conexiones y límites de recursos, agregaría métricas, alertas y
respaldos verificados, y ampliaría PostgreSQL/Render según la carga. Si hubiera
un cuello de botella aislado y medido, evaluaría caché o separar ese componente
con contratos claros y consistencia explícita.
