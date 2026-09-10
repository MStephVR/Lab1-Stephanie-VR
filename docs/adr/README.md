# Registros de Decisiones de Arquitectura (ADRs)

## Índice

Este directorio contiene los **Architecture Decision Records** del proyecto SisGanado. Cada ADR documenta una decisión arquitectónica importante: qué se eligió, por qué, qué alternativas se consideraron, y qué consecuencias tiene.

### Lista de ADRs

| # | Título | Estado | Fecha |
|---|--------|--------|-------|
| [ADR-001](ADR-001-eleccion-del-stack.md) | Elección del stack tecnológico | Aceptada | 30/07/2026 |
| [ADR-002](ADR-002-presupuesto-y-asistente-inteligente.md) | Presupuesto pre-campaña y Asistente Inteligente de Optimización | Aceptada | 12/08/2026 |
| [ADR-003](ADR-003-mongodb-bitacoras-auditoria.md) | Almacenamiento de bitácoras y auditoría con MongoDB | Propuesta | 12/08/2026 |
| [ADR-004](ADR-004-arquitectura-capas.md) | Arquitectura por capas (Layered Architecture) | Aceptada | 12/08/2026 |
| [ADR-005](ADR-005-transaccionalidad-costo-jornada.md) | Transaccionalidad en Proceso de Cálculo de Costo Total | Aceptada | 12/08/2026 |

## Estados posibles

- **Propuesta:** decisión sugerida, bajo consideración.
- **Aceptada:** decisión tomada y en vigor.
- **Reemplazada por ADR-XXX:** la decisión fue descartada posteriormente en favor de otra.
- **Deprecada:** ya no aplica, pero se mantiene para referencia histórica.

## Cómo leer un ADR

Cada archivo ADR contiene:
1. **Contexto:** el problema y las restricciones que llevaron a la decisión.
2. **Decisión:** qué se eligió, en forma clara y concisa.
3. **Alternativas consideradas:** al menos 2 opciones, con razones técnicas de descarte.
4. **Consecuencias:** positivas (ganancias), negativas (costos), neutras (cambios en el equipo).
5. **Referencias:** documentación o fuentes en que se apoyó la decisión.

## Principios para nuevos ADRs

- Documentar una decisión por ADR, no varias juntas.
- Incluir **al menos 2 alternativas**. Si no hubo alternativas, quizás no fue una decisión real.
- Ser honesto sobre **costos y limitaciones**, no solo beneficios.
- Usar criterios técnicos concretos (rendimiento, mantenibilidad, costo, seguridad), no opiniones.
- Fecha y responsables claros para trazabilidad.

## Evolución de decisiones

Este proyecto fue diseñado incrementalmente:
- **Lab 1:** Stack, arquitectura por capas, presupuesto + asistente.
- **Lab 2:** MongoDB para bitácoras y subdominios documentales.
- **Lab 3:** Base de datos relacional con JPA/Hibernate (a definir en ADR futuro).
- **Lab 4:** Transacciones ACID y manejo de errores (ADR-005 sentó base).
- **Lab 5+:** Posibles mejoras (caching, APIs externas, seguridad avanzada).

Cada decisión fue tomada considerando restricciones reales (tiempo, equipo, alcance del curso) y criterios técnicos solidos. Un ADR descartado no significa "error", sino parte natural de la evolución del proyecto.
