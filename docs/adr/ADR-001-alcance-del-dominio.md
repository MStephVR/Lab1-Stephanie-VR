# ADR-001 · Dominio y alcance de SisGanado

**Estado:** Aceptada · **Fecha:** 19/11/2026 · **Responsable:** María Stephanie Vargas Ramírez

## Contexto

La finca necesita organizar el manejo sanitario del ganado y relacionar las
decisiones veterinarias con el inventario y los costos. El proyecto es
académico, lo desarrolla una persona y debe poder demostrarse de extremo a
extremo dentro del tiempo y la infraestructura de un ciclo lectivo.

## Decisión

SisGanado cubre el registro de lotes y animales, catálogo de productos y planes
sanitarios, proveedores, inventario y compras, planificación y ejecución de
jornadas, aplicaciones sanitarias, presupuesto/costeo y auditoría. Los roles
definen el uso: ADMIN mantiene catálogos y cuentas, VETERINARIO opera jornadas
sanitarias y USUARIO consulta información sin permisos de escritura.

Quedan fuera del alcance actual la venta de ganado, facturación electrónica,
pagos, integración con laboratorios o proveedores externos y recomendaciones
con aprendizaje automático.

## Alternativas consideradas

1. **Sistema integral de administración ganadera:** incluir ventas, pagos,
   contabilidad y facturación. Se descarta porque excede la necesidad sanitaria
   y el tiempo del proyecto.
2. **Aplicación limitada al cálculo de dosis:** se descarta porque no vincula
   las dosis con lotes de inventario, fechas de vencimiento, aplicaciones y
   costos reales.
3. **Alcance sanitario trazable, opción elegida:** integra catálogo, animales,
   inventario, jornadas y costos con reglas explícitas, sin incorporar módulos
   comerciales ajenos al flujo principal.

## Consecuencias

- **Beneficio:** se puede recorrer el proceso desde la planificación hasta la
  ejecución y el registro de costos.
- **Costo/limitación:** ventas, facturación e integraciones externas deben
  resolverse fuera del sistema; la carga inicial de catálogos es manual.
- Los permisos diferenciados permiten que las cuentas de consulta no alteren
  datos operativos.

## Referencias

- [Modelo de datos](../../BACKEND/docs/modelo-datos.md)
- [API REST v1](../../BACKEND/README.md#api-rest-v1-laboratorio-5)
