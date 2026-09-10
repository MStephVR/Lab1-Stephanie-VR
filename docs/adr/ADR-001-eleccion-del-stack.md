# ADR-001 · Elección del stack tecnológico

**Estado:** Aceptada · **Fecha:** 30/07/2026 · **Responsables:** María Stephanie Vargas Ramírez

## Contexto
Necesito elegir el stack para construir una aplicación web empresarial
durante el curso EIF509. El curso recomienda Java 21 + Spring Boot 3 + Gradle,
y ya tengo experiencia básica en Java. El tiempo es limitado
(un ciclo) y la aplicación debe crecer de forma incremental laboratorio a
laboratorio, trabajando en solitario.

## Decisión
Usaré Java 21 + Spring Boot 3 + Gradle (Groovy) como stack principal,
siguiendo la recomendación del curso.

## Alternativas consideradas
1. **Node.js + Express:** más liviano y rápido de arrancar, pero tengo
   menos experiencia y el curso está diseñado alrededor del ecosistema
   Spring (JPA, inyección de dependencias). Se descartó por alineación con el
   curso y curva de aprendizaje.
2. **Java + Maven (en vez de Gradle):** Maven es igual de válido, pero Gradle
   tiene una sintaxis más concisa y es lo que usan los ejemplos del curso.
   Se descartó Maven por consistencia con el material.

## Consecuencias
- **Positivas:** aprovecho el ecosistema maduro de Spring (seguridad, datos,
  pruebas) y la alineación total con el material del curso.
- **Negativas:** Spring Boot tiene una curva inicial más pronunciada que un
  framework minimalista; el primer arranque puede sentirse pesado.
- **Neutras:** trabajo con un único JDK (21) para evitar diferencias entre
  máquinas a lo largo de los laboratorios.

## Referencias
- Documentación oficial de Spring Boot 3.
- Sesión 2 del curso EIF509 (arquitectura de software).
