# ADR-004 · Arquitectura por capas (Layered Architecture)

**Estado:** Aceptada · **Fecha:** 12/08/2026 · **Responsables:** María Stephanie Vargas Ramírez

## Contexto
El sistema debe ser mantenible a largo plazo (Lab 1 → Lab 5, luego posibles mejoras). El equipo es pequeño (1 desarrollador) pero el código debe ser comprensible para otros o para re-lectura futura.

Sin una estructura clara, la capa de presentación (controladores) terminaría mezclada con lógica de negocio, validaciones, acceso a datos, etc. Eso hace que cambiar un cálculo de dosis o mover la persistencia sea riesgoso.

El curso (clase 2, EIF509) enfatiza arquitectura por capas como base para proyectos web empresariales.

## Decisión
Organizaremos el código en **cuatro capas verticales:**

```
presentation (Controllers) → business (Services) → data (Repositories) → domain (Entities)
```

**Regla de oro:** las dependencias solo fluyen hacia abajo. La presentación NO depende de datos directamente; data NO conoce de presentación.

```
src/main/java/cr/ac/una/eif509/demo/
├── presentation/           (REST Controllers)
├── business/               (Services, validaciones, cálculos)
├── data/                   (Repositories, mappers)
├── domain/                 (Entidades, Value Objects)
└── config/                 (Configuración de Spring)
```

Cada capa tiene responsabilidad única:
- **Presentation:** recibir HTTP, validar entrada, devolver respuesta.
- **Business:** lógica de negocio pura (cálculo de dosis, presupuesto, reglas sanitarias).
- **Data:** persistencia y recuperación (futura JPA/Hibernate).
- **Domain:** modelos de datos (Animal, Lote, ProductoVeterinario, etc.).

## Alternativas consideradas

1. **Arquitectura Hexagonal (Ports & Adapters):**
   - Aisla la lógica de negocio en el centro; todo lo demás son adaptadores (BD, UI, APIs externas).
   - Muy testeable, inversión de dependencias elegante.
   - **Descartado:** es más compleja para un proyecto pequeño. Requiere interfaces inversas en todas partes. Para un equipo de 1, el overhead arquitectónico no compensa; la curva de aprendizaje es más pronunciada.

2. **MVC simplista (sin capa de servicios):**
   - Controllers hablan directamente con Repositories.
   - Rápido para prototipado.
   - **Descartado:** la lógica de negocio termina en Controllers o Entities. Cuando hay reglas complejas (cálculo de dosis, presupuesto con alternativas), el código se enreda. Cambiar un cálculo requiere cambiar Controller y Entidad. No es escalable ni mantenible.

3. **Microservicios desde el inicio:**
   - Servicio para ganado, servicio para inventario, etc.
   - **Descartado:** overkill y complejo operacionalmente. No hay justificación de escalabilidad horizontal en este momento. El overhead de comunicación inter-servicios (latencia, serialización) no tiene beneficio real en Lab 1-5.

4. **Arquitectura por capas (opción elegida):**
   - Balance perfecto entre simplicidad y estructura.
   - Escalable dentro de una sola aplicación.
   - Alineado con el curso.
   - Patrón bien conocido por la industria.
   - **Elegido por:** pragmatismo, claridad, educativo.

## Consecuencias

### Positivas
- **Claridad:** cualquiera que lea el código sabe dónde buscar la lógica de dosis, dónde están las queries, dónde se parsea HTTP.
- **Testabilidad:** puedo testear servicios sin Controller ni DB (inyecto Repositorios mock).
- **Reutilización:** un servicio puede ser usado por múltiples Controllers o incluso APIs futuras (SOAP, gRPC).
- **Cambios confinados:** si cambio la BD de relacional a documental, toco solo la capa data; el business no cambia.
- **Escalabilidad vertical:** crecimiento dentro de una JVM es más manejable. La capa business puede alojar en caché, cálculos complejos, sin afectar presentación.

### Negativas
- **Overhead de código:** hay más clases y carpetas que en un monolito sin estructura. Nuevos desarrolladores necesitan entender la convención.
- **Indirección:** una request HTTP toca 4-5 capas antes de retornar. Más indirección = más tiempo en debugging si algo falla.
- **Falsa separación:** si no hay disciplina, la lógica de negocio se filtra a presentación de todas formas. La arquitectura no previene malas decisiones.

### Neutras
- El equipo debe acordar cómo se llaman y dónde viven las clases (paquetes claros).
- Patrón consistente en Spring Boot (es el estándar de facto).

## Referencias
- Clase 2, EIF509: Arquitectura de Software (presentación del profesor Elberth Garro).
- "Patterns of Enterprise Application Architecture" - Martin Fowler (Layered Architecture).
- Spring Boot documentation: Layered application structure.
- Java/Spring best practices.
