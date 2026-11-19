# ADR-005 · Plataforma y estrategia de despliegue

**Estado:** Aceptada · **Fecha:** 19/11/2026 · **Responsable:** María Stephanie Vargas Ramírez

## Contexto

El proyecto debe poder demostrarse en Internet, separar la interfaz de la API y
ejecutar verificaciones automáticas antes de publicar cambios. El presupuesto
y la operación disponibles corresponden a un proyecto académico individual.

## Decisión

La SPA React/Vite se publica como sitio estático en Render y la API Spring Boot
se ejecuta como servicio Docker en Render. PostgreSQL y MongoDB se conectan
como servicios externos mediante variables de entorno. GitHub Actions ejecuta
compilación, pruebas, Checkstyle, cobertura, umbral mínimo de pruebas y build
de la SPA; tras el job `verificar`, el job `desplegar` invoca el deploy hook de
Render para pushes a `master`. Actuator expone `/actuator/health` para el
health check.

La carga de datos de demostración se habilita mediante
`SISGANADO_DEMO_DATA_ENABLED`; el seeder de producción busca registros por
claves conocidas y no los duplica. Debe desactivarse después de la demostración
si ya no se necesitan datos de ejemplo.

## Alternativas consideradas

1. **Desplegar todo en una sola máquina virtual:** se descarta porque mezcla
   el servidor de archivos estáticos y la API, y requiere configurar TLS,
   proxy y supervisión manualmente.
2. **Kubernetes o varios microservicios:** se descarta por el costo operacional
   y de configuración que no se justifica para una aplicación de este tamaño.
3. **Render con SPA estática y API Docker, opción elegida:** separa las
   responsabilidades, automatiza el deploy y ofrece servicios administrados
   dentro del alcance del proyecto.

## Consecuencias

- **Beneficio:** la interfaz puede distribuirse por CDN y el pipeline bloquea
  el despliegue cuando las verificaciones fallan.
- **Costo/limitación:** el servicio gratuito puede tener arranques en frío,
  límites de recursos y disponibilidad distinta a un servicio de producción
  pagado; el deploy hook es un secreto que debe custodiarse en GitHub.
- **Riesgo operativo:** la disponibilidad y retención de datos dependen de la
  configuración y respaldos de PostgreSQL/MongoDB administrados; H2 local no
  comparte datos con Render.
- Los ejemplos de demostración son datos artificiales y no deben confundirse
  con información real de una finca.

## Referencias

- [Blueprint de Render](../../render.yaml)
- [Pipeline de CI/CD](../../.github/workflows/ci-cd.yml)
- [Configuración de producción](../../BACKEND/src/main/resources/application-prod.properties)
