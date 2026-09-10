# ADR-005 · Transaccionalidad en Proceso de Cálculo de Costo Total de Jornada

**Estado:** Aceptada · **Fecha:** 12/08/2026 · **Responsables:** María Stephanie Vargas Ramírez

## Contexto
El Proceso 2 (Cálculo de Costo Total de Jornada Sanitaria) tiene múltiples escrituras:
1. Actualizar `JornadaSanitaria` con costo total.
2. Decrementar `Inventario` (consumo de producto).
3. Crear registro en `Bitácora` (MongoDB).

Si la aplicación se cae o falla entre paso 1 y 2, quedamos en un estado inconsistente:
- Costo registrado pero inventario NO decrementado → falta la verdad sobre existencias.
- O inventario decrementado pero costo NO guardado → no hay trazabilidad de gasto.

Esto es inaceptable en un dominio ganadero donde cada medicamento tiene costo real y debe haber reconciliación con proveedores.

El curso (Laboratorio 4) introduce transacciones ACID como solución.

## Decisión
Implementaremos **transacciones ACID** en el método `calcularYRegistrarCostoJornada()` del servicio `CostoJornadaService`:

```java
@Transactional
public PresupuestoJornada calcularYRegistrarCostoJornada(JornadaSanitaria jornada, ...) {
    // Paso 1: Cálculos (en memoria, sin I/O)
    BigDecimal costoTotal = calcularCosto(...);
    
    // Paso 2-3: Escrituras en BD relacional (transacción única)
    jornada.setCostoTotal(costoTotal);
    jornadaRepository.save(jornada);          // ESCRITURA 1
    
    inventarioRepository.decrementarPorProducto(...);  // ESCRITURA 2
    
    // Paso 4: Bitácora en MongoDB (transacción separada, eventual consistency)
    bitacoraRepository.registrarOperacion(...);  // ESCRITURA 3
    
    return presupuesto;
}
```

**Alcance de la transacción:** solo datos relacionales (JPA/Hibernate). MongoDB se maneja aparte (eventual consistency).

**Rollback automático:** si hay excepción, Spring revierte ambas escrituras en la BD relacional.

## Alternativas consideradas

1. **Sin transacciones (manual rollback):**
   - En caso de error, escribir código que revierta cambios manualmente.
   - **Descartado:** propenso a errores lógicos. ¿Qué pasa si el rollback mismo falla? ¿Qué si hay concurrencia? Reinventar la rueda cuando las BDs ya lo resuelven.

2. **Transacciones implícitas por BD (AutoCommit):**
   - Cada sentencia SQL es una transacción de 1 paso. Sin agrupación.
   - **Descartado:** no hay atomicidad sobre múltiples escrituras. Si cae entre UPDATE y UPDATE, inconsistencia.

3. **Transacciones Saga (orquestación de servicios distribuidos):**
   - Cada paso es un servicio independiente que se llama en secuencia.
   - Si uno falla, compensaciones manuales (reversar paso anterior).
   - **Descartado:** es patrón para microservicios distribuidos. Nuestro sistema es monolito; usar Sagas sería over-engineering. Además, escribir compensaciones es complejo y requiere lógica de retry.

4. **Transacciones ACID en una base de datos (opción elegida):**
   - Spring `@Transactional` con Hibernateboard/JPA.
   - Atomicidad garantizada: o pasan todos los pasos o ninguno.
   - **Elegido por:** simplicidad, estándar de la industria, alineado con Laboratorio 4.

## Consecuencias

### Positivas
- **Integridad de datos:** garantizado: si hay error, no quedamos en estado intermedio.
- **Reconciliación:** el contador puede confiar en que cada jornada registrada tiene inventario decrementado exactamente en la cantidad esperada.
- **Auditabilidad:** bitácora solo se crea si las escrituras principales tuvieron éxito.
- **Simplicidad del código:** Spring maneja rollback automáticamente; no hay código de error complejo.
- **Performance:** transacción local en una sola BD es muy rápida (< 100ms típicamente).

### Negativas
- **Bloqueos de BD:** durante la transacción, las filas modificadas están lockeadas. Si dos usuarios intentan calcular costo de la misma jornada simultáneamente, uno se bloquea esperando al otro.
- **Rollback de todo:** si MongoDB falla (bitácora), la transacción relacional NO se revierte automáticamente. Quedamos con escritura en PostgreSQL pero SIN registro en MongoDB. Hay que tratar esto explícitamente (o acepar eventual consistency).
- **Escalabilidad limitada:** si hay muchas jornadas siendo calculadas a la vez, los locks pueden convertirse en cuello de botella (si el hardware/BDD es limitado).

### Neutras
- El equipo debe entender ACID y niveles de aislamiento de transacciones (READ_COMMITTED, REPEATABLE_READ, etc.).
- Cambio de BD (ej: NoSQL puro) requeriría revisar la estrategia de transacciones.

## Referencias
- Laboratorio 4 del curso EIF509: Transacciones ACID y manejo de excepciones.
- Spring `@Transactional` documentation.
- "Designing Data-Intensive Applications" - Martin Kleppmann (capítulo de transacciones).
- PostgreSQL/Hibernate transaction docs.
- Patrón Saga para microservicios (futuro si el proyecto evoluciona).
