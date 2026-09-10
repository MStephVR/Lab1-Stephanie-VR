# ADR-003 · Almacenamiento de bitácoras y auditoría con MongoDB

**Estado:** Propuesta · **Fecha:** 12/08/2026 · **Responsables:** María Stephanie Vargas Ramírez

## Contexto
El sistema registra operaciones críticas: aplicación de medicamentos, cambios de precios, autorización de compras. Para cumplimiento, trazabilidad y debugging, necesitamos guardar un historial auditable (quién, cuándo, qué, cambios). 

En una base de datos relacional tradicional (PostgreSQL/MySQL), esto requeriría:
- Tablas de auditoría normalizadas para cada entidad.
- Triggers o listeners complejos.
- Esquema rígido que cambia si los requisitos evolucionen.

Sin embargo, el dominio de ganadería es flexible: notas veterinarias pueden ser texto libre, formato variable, adjuntos ocasionales. Esto encaja mejor en un modelo documental.

El curso (Laboratorio 2) recomienda MongoDB para subdominios documentales. Hemos identificado bitácoras y observaciones como candidatos perfectos.

## Decisión
Usaremos **MongoDB como base de datos documental complementaria** para almacenar:
1. **Bitácora de operaciones:** logs auditables (usuario, timestamp, acción, datos antes/después).
2. **Historial de precios:** snapshot de precios en cada cambio (producto, precio anterior, precio nuevo, fecha, responsable).
3. **Observaciones veterinarias:** notas libres del veterinario (formato JSON flexible, puede incluir links a imágenes).
4. **Reportes generados:** PDF/JSON guardados como documentos (no requieren normalización).

La base de datos relacional (JPA/Hibernate, Lab 3) seguirá siendo la fuente de verdad para datos operacionales (animales, lotes, inventario); MongoDB es complementaria y nunca reemplaza la lógica transaccional.

Acceso: Spring Data MongoDB con repositorios análogos a JPA.

## Alternativas consideradas

1. **Todo en PostgreSQL relacional (una sola base de datos):**
   - Usar tablas de auditoría normalizadas con triggers o Hibernate Envers.
   - **Descartado:** los triggers en SQL son frágiles y difíciles de testear; Envers agrega complejidad; si el esquema de observaciones cambia, hay migración costosa en SQL. No es natural para datos semiestructurados.

2. **Elasticsearch para logs (ELK stack):**
   - Excelente para búsqueda full-text y análisis.
   - **Descartado:** overkill para este proyecto; requiere JVM adicional, curva de aprendizaje, no está en el alcance del curso. Elasticsearch es para búsqueda y análisis masivo, no para almacenamiento canónico de auditoría.

3. **Archivo de log plano (JSON lines o CSV):**
   - Simplicidad máxima: append-only, sin servidor de base de datos.
   - **Descartado:** no hay forma de hacer queries eficientes; no hay concurrencia segura si múltiples instancias escriben; escalabilidad limitada. No es professional para un sistema real.

4. **MongoDB (opción elegida):**
   - Documental: flexible, esquema dinámico.
   - Transacciones ACID en versiones recientes (4.0+).
   - Spring Data MongoDB está maduro.
   - Patrón recomendado por el curso.
   - **Elegido por:** es el sweet spot entre flexibilidad y profesionalismo; alineado con Laboratorio 2.

## Consecuencias

### Positivas
- **Flexibilidad:** si una observación vet necesita incluir un diagnóstico nuevo, no hay migración de esquema.
- **Separación de preocupaciones:** datos operacionales (relacional) vs. datos documentales (documental).
- **Trazabilidad completa:** cada operación queda registrada de forma inmutable.
- **Consultas naturales:** logs de quién modificó un precio en rango de fechas es una query simple.
- **Aprendizaje:** el equipo aprende dos paradigmas de datos en un mismo curso.

### Negativas
- **Duplicación de dependencias:** el proyecto tiene dos bases de datos. Más servicios que levantear localmente y en producción.
- **Consistencia eventual:** MongoDB y PostgreSQL pueden estar fuera de sincronía por un tiempo. Hay que programar cuidadosamente.
- **Overhead operacional:** el admin debe respaldar/restaurar dos bases de datos, no una.
- **Complejidad en transacciones cross-database:** si una operación debe ser atómica en ambas BDs, es más difícil que en una sola.

### Neutras
- Futura evolución: si el sistema crece, MongoDB puede separarse en servicio independiente (microservicios).
- El desarrollador debe entender dos paradigmas de querying (SQL vs. MongoDB aggregation pipeline).

## Referencias
- Laboratorio 2 del curso EIF509: "MongoDB para subdominios documentales".
- MongoDB documentation: Multi-document ACID transactions (desde v4.0).
- Patrón Event Sourcing y CQRS (futuro).
- Spring Data MongoDB reference documentation.
