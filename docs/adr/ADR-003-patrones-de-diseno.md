# ADR-003 · Patrones de diseño aplicados al flujo sanitario

**Estado:** Aceptada · **Fecha:** 19/11/2026 · **Responsable:** María Stephanie Vargas Ramírez

## Contexto

Las reglas de una jornada incluyen creación consistente de entidades y más de
una forma de calcular costos (por unidad o proporcional por mililitro). Si
estas reglas se mezclan en controladores o se duplican en cada caso de uso,
cambios de negocio elevan el riesgo de regresiones.

## Decisión

Se organiza la aplicación por capas (presentación, negocio, datos y dominio).
Los repositorios encapsulan persistencia; `CostoProductoStrategy` permite
seleccionar el cálculo por unidad o por mililitro, y `JornadaSanitariaFactory`
centraliza la creación de jornadas y aplicaciones con fechas derivadas. Los
flujos que escriben el dominio usan límites transaccionales en los servicios.

## Alternativas consideradas

1. **Controladores con lógica de negocio y consultas directas:** se descarta
   porque mezcla HTTP, reglas y persistencia y dificulta probar el dominio.
2. **Condicionales duplicados para cada fórmula de costo:** se descarta porque
   cada nueva modalidad obliga a editar múltiples servicios y puede producir
   cálculos inconsistentes.
3. **Microservicios y arquitectura distribuida desde el inicio:** se descarta
   por el costo de despliegue, comunicación y consistencia, no justificado por
   la escala actual.
4. **Capas, Strategy, Factory y Repository, opción elegida:** reutiliza
   componentes pequeños, comprobables y alineados con el código existente.

## Consecuencias

- **Beneficio:** cada regla tiene un punto de cambio y puede probarse sin
  ejecutar toda la interfaz.
- **Costo:** aparecen clases e interfaces adicionales y hay que mantener sus
  contratos coherentes.
- **Limitación:** la arquitectura por capas no impide por sí sola que alguien
  agregue lógica al lugar incorrecto; las revisiones y pruebas siguen siendo
  necesarias.

## Referencias

- `BACKEND/src/main/java/cr/ac/una/eif509/demo/business/CostoProductoStrategy.java`
- `BACKEND/src/main/java/cr/ac/una/eif509/demo/business/JornadaSanitariaFactory.java`
- [Pruebas y ejecución transaccional](../../BACKEND/docs/modelo-datos.md#capa-de-negocio)
