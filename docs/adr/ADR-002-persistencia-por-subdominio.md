# ADR-002 · Persistencia SQL y documental por subdominio

**Estado:** Aceptada · **Fecha:** 19/11/2026 · **Responsable:** María Stephanie Vargas Ramírez

## Contexto

El dominio operativo tiene relaciones y reglas de integridad —por ejemplo,
animal/lote, producto/inventario y jornada/plan— que deben confirmarse de forma
transaccional. En cambio, los eventos de bitácora tienen detalles que pueden
variar según la operación y se consultan como documentos individuales.

## Decisión

PostgreSQL es la fuente de verdad relacional para usuarios, lotes, animales,
productos, planes, proveedores, compras, inventario, jornadas, aplicaciones,
presupuestos y alternativas. MongoDB almacena documentos de auditoría en
`bitacora_eventos` (entidad, acción, usuario, detalle y fecha). Flyway versiona
el esquema relacional; Spring Data JPA y MongoDB implementan el acceso.

## Alternativas consideradas

1. **Solo PostgreSQL:** conservar también la bitácora en tablas relacionales.
   Se descarta para este proyecto porque el detalle del evento es variable y no
   requiere joins transaccionales con el dominio.
2. **Solo MongoDB:** representar también inventario y jornadas como documentos.
   Se descarta porque debilitaría la integridad referencial y las operaciones
   atómicas que requiere el consumo de existencias.
3. **PostgreSQL más MongoDB, opción elegida:** cada tecnología sirve a un
   subdominio; las escrituras operativas permanecen dentro de transacciones SQL.

## Consecuencias

- **Beneficio:** las relaciones, restricciones y transacciones del negocio se
  expresan en PostgreSQL; la bitácora admite detalles documentales.
- **Costo:** se operan y respaldan dos motores y se configuran dos conexiones.
- **Limitación:** no existe una transacción ACID distribuida entre PostgreSQL y
  MongoDB; los eventos documentales no deben tratarse como parte atómica de una
  escritura relacional.
- Para desarrollo local hay perfiles con configuración distinta; producción
  conecta servicios administrados mediante variables de entorno.

## Referencias

- [Modelo de datos y normalización](../../BACKEND/docs/modelo-datos.md)
- [Migraciones Flyway](../../BACKEND/src/main/resources/db/migration/)
