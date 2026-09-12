# ADR-001 · Elección del stack tecnológico

**Estado:** Aceptada · **Fecha:** 30/07/2026 · **Responsable: María Stephanie Vargas Ramírez**

## Contexto
Elegir el stack para construir una aplicación web empresarial
durante el curso EIF509. El sistema SisGanado necesita una API REST para
administrar animales, inventario, jornadas sanitarias y sus costos, con
persistencia relacional y registros de auditoría documentales. María Stephanie
Vargas Ramírez cuenta con experiencia básica en Java, y el tiempo disponible es
un ciclo académico; por ello, el proyecto debe poder crecer de forma
incremental de un laboratorio a otro sin introducir herramientas ajenas al
material del curso.

## Decisión
Java 21 + Spring Boot 3 + Gradle (Groovy) como stack principal. Spring Boot
permite concentrar las reglas sanitarias y de cálculo de costos en servicios
probables, exponerlos mediante controladores REST y conectar PostgreSQL y
MongoDB con el ecosistema que se estudia en el curso.

## Alternativas consideradas
1. **Node.js + Express:** más liviano y rápido de arrancar, pero requería
  adoptar un lenguaje y un ecosistema nuevos mientras se implementan las
  reglas de dosificación, inventario y costos.
2. **Java + Maven (en vez de Gradle):** Maven es igual de válido, pero Gradle
  tiene una sintaxis más concisa y coincide con los ejemplos del curso. Se
  descartó Maven para reutilizar la configuración y comandos ya conocidos.

## Consecuencias
- **Positivas:** se aprovecha el ecosistema maduro de Spring (seguridad, datos,
  pruebas) y la alineación total con el material del curso.
- **Negativas:** Spring Boot tiene una curva inicial más pronunciada que un
  framework minimalista; el primer arranque puede sentirse pesado.
- **Neutras:** se trabajará con el mismo JDK (21) para evitar
  diferencias entre máquinas.

## Referencias
- Documentación oficial de Spring Boot 3.
- Sesión 2 del curso EIF509 (arquitectura de software).
