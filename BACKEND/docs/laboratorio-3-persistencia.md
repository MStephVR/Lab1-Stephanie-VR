# Laboratorio 3: persistencia con ORM y repositorios

## Mapeo JPA

Las diez entidades relacionales se mapean con clases `@Entity` separadas de los
records usados como DTOs. Las asociaciones se cargan con `FetchType.LAZY` para
no traer grafos completos por defecto. `LoteEntity` y `AnimalEntity` modelan la
relacion uno a muchos; las entidades sanitarias modelan las claves foraneas de
productos, animales, jornadas y usuarios; `PlanSanitarioEntity` y
`ProductoVeterinarioEntity` usan la tabla intermedia `plan_producto`.

Flyway crea el esquema con `V1__crear_esquema_base.sql` y
`V2__crear_dominio_sanitario.sql`. En los perfiles `local` y `docker`,
`spring.jpa.hibernate.ddl-auto=validate`, por lo que Hibernate valida el
esquema y no lo modifica.

## Repositorios generalizados

`BaseRepository<T, ID>` extiende `JpaRepository` y concentra las operaciones
CRUD comunes. Cada entidad tiene un repositorio especifico que lo extiende y
agrega consultas de negocio. `BitacoraRepository` mantiene su propia
especializacion `MongoRepository<BitacoraEvento, String>` porque pertenece al
subdominio documental.

## Cuatro consultas de negocio

### JPQL 1: animales con lote

`AnimalRepository.findAllWithLote`:

```sql
select a from AnimalEntity a join fetch a.lote order by a.id
```

SQL representativo generado por Hibernate:

```sql
select a1_0.id, a1_0.codigo_arete, a1_0.estado, a1_0.fecha_nacimiento,
       a1_0.lote_id, a1_0.peso_kg, a1_0.raza, a1_0.sexo,
       l1_0.id, l1_0.nombre, l1_0.proposito, l1_0.ubicacion
from animales a1_0
join lotes l1_0 on l1_0.id = a1_0.lote_id
order by a1_0.id
```

### JPQL 2: inventario disponible

`InventarioRepository.findDisponibleNoVencido` filtra lotes no vencidos y con
existencias positivas, y hace `join fetch` del producto:

```sql
select i from InventarioEntity i join fetch i.producto
where i.fechaVencimiento >= :hoy and i.cantidadDisponible > 0
order by i.fechaVencimiento
```

El SQL resultante combina `inventario` con `productos_veterinarios` en una sola
consulta y aprovecha el indice `(producto_id, fecha_vencimiento)`.

### Criteria 1: animales activos por raza

`BusinessSpecifications.animalesActivosDeRaza` construye predicados dinamicos
para `estado = 'Activo'` y, si se proporciona, `lower(raza) = lower(:raza)`.
Para `raza = 'brahman'`, el SQL generado es equivalente a:

```sql
select a1_0.id, a1_0.codigo_arete, a1_0.estado, a1_0.fecha_nacimiento,
       a1_0.lote_id, a1_0.peso_kg, a1_0.raza, a1_0.sexo
from animales a1_0
where a1_0.estado = ? and lower(a1_0.raza) = lower(?)
```

Los valores se envian como parametros, sin concatenar entrada del usuario.

### Criteria 2: productos por rango de precio

`BusinessSpecifications.productosDentroDelRangoDePrecio` agrega de forma
condicional `precio_unitario >= :minimo` y `precio_unitario <= :maximo`.
Para el rango `5.0` a `20.0`, Hibernate genera un SQL equivalente a:

```sql
select p1_0.id, p1_0.dosis_maxima, p1_0.dosis_minima, p1_0.nombre,
       p1_0.precio_unitario, p1_0.tipo, p1_0.unidad_medida
from productos_veterinarios p1_0
where p1_0.precio_unitario >= ? and p1_0.precio_unitario <= ?
```

La Specification permite usar solo uno de los limites y Hibernate genera el
`where` resultante.

## Evidencia del problema N+1

El caso real es listar animales y luego acceder a `animal.getLote().getNombre()`.
Con `animalRepository.findAll()` y la relacion `ManyToOne` LAZY, Hibernate
produce una consulta para los animales y una consulta adicional por cada lote
no inicializado: `1 + N` consultas.

Con tres animales y lotes distintos, la evidencia SQL es:

```sql
select ... from animales;
select ... from lotes where id = ?; -- animal 1
select ... from lotes where id = ?; -- animal 2
select ... from lotes where id = ?; -- animal 3
```

El total es una consulta principal mas tres consultas de relacion.

La correccion es `AnimalRepository.findAllWithLote`, que usa `JOIN FETCH`. La
consulta SQL contiene un unico `join` entre `animales` y `lotes`, de modo que
el mismo recorrido del servicio usa una consulta en lugar de `1 + N`.
La prueba `consultaAnimalesConLoteEnUnaConsultaJoinFetch` verifica el recorrido
que antes disparaba las consultas adicionales.

Con la correccion, el mismo caso queda en una sola consulta:

```sql
select a..., l...
from animales a
join lotes l on l.id = a.lote_id
order by a.id;
```

## Pruebas de integracion

`PersistenceIntegrationTest` contiene seis pruebas y levanta PostgreSQL 16 con
Testcontainers. Flyway crea `V1` y `V2`; Hibernate usa `ddl-auto=validate` para
comprobar que el mapeo coincide con el esquema real. Las pruebas cubren:

1. Persistencia y consulta de animales por lote.
2. Consulta optimizada de animales con lote mediante `JOIN FETCH`.
3. Inventario disponible y no vencido.
4. Compras en un rango de fechas con proveedor cargado.
5. Filtro dinamico de animales activos por raza con Criteria.
6. Filtro dinamico de productos por rango de precio con Criteria.

La CI ejecuta `./gradlew build` en Ubuntu, donde Docker esta disponible para
Testcontainers.
