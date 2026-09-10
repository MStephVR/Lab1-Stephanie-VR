# Sistema Web de Control Sanitario y Costos para Ganado

![CI](https://github.com/MStephVR/Lab1-Stephanie-VR/actions/workflows/ci.yml/badge.svg)

Proyecto **Spring Boot 3 + Java 21 + Gradle** organizado por capas, como base
para administrar la información sanitaria del ganado, controlar inventario de
productos veterinarios y calcular costos de vacunación y desparasitación.

> ⚠️ **Este repo es un esqueleto de referencia del proyecto.** La idea es que
> la estructura, nombres y módulos reflejen el dominio: animales, lotes,
> productos veterinarios, inventario, jornadas sanitarias y costos.

- **Integrante:** Stephanie VR
- **Sistema:** SisGanado v1.0 - Control Sanitario y de Costos para Ganado, para
  Finca San Isidro.
- **Propuesta de dominio completa:** [docs/propuesta-dominio.md](docs/propuesta-dominio.md)
- **Decisiones de arquitectura (ADRs):** [docs/adr/](docs/adr/README.md)

---

## Arquitectura: Cómo se organiza

```
src/main/java/cr/ac/una/eif509/demo/
├── presentation/   → Controladores HTTP de animales, inventario, jornadas y costos.
├── business/       → Servicios con validaciones, cálculos y reglas sanitarias.
├── data/           → Repositorios para animales, productos, inventario y compras.
├── domain/         → Entidades base del sistema.
└── config/         → Configuración de Spring (se llena más adelante).
```

**Regla de oro de las dependencias:** presentación → negocio → datos. Nunca al revés.
Cada capa solo conoce a la que tiene debajo. Las entidades de `domain` se usan
como modelo común entre capas.

## Diagrama de arquitectura

```mermaid
flowchart TB
	subgraph UI["Presentación"]
		C1[GanadoController]
		C2[CostoJornadaController]
		C3[SaludController]
	end
	subgraph BL["Lógica de negocio"]
		S1[ControlGanadoService]
		S2[CostoJornadaService]
		S3[SaludService]
	end
	subgraph DOM["Modelo de dominio"]
		D1[Animal, Lote, ProductoVeterinario,
JornadaSanitaria, AplicacionSanitaria,
Inventario, PlanSanitario, Proveedor,
Compra, Usuario]
	end
	subgraph DL["Acceso a datos"]
		R1[ControlGanadoRepository]
		R2[SaludRepository]
	end

	C1 --> S1
	C2 --> S2
	C3 --> S3
	S1 --> R1
	S3 --> R2
	S1 --> DOM
	S2 --> DOM
	S3 --> DOM
	R1 --> EXT[(Base de datos / futuro ORM)]
	R2 --> EXT
```

La capa de presentación solo expone endpoints. La capa de negocio concentra
las reglas sanitarias, el cálculo de dosis, el costo total y el costo promedio
por animal. La capa de datos simula el acceso a precios e inventario y luego
podrá conectarse a una base de datos real.

## Cómo correrlo

```bash
# 1. Compilar y correr las pruebas
./gradlew build

# 2. Levantar la aplicación
./gradlew bootRun

# 3. Probar el resumen sanitario del ganado (en otra terminal, con la app corriendo)
curl "http://localhost:8080/api/ganado/control?animales=10"
# Respuesta esperada: mensaje, cantidad de animales y costo base por animal

# 4. Probar el cálculo de costo de una jornada sanitaria
curl "http://localhost:8080/api/jornadas/costo?animales=10&pesoPromedioKg=380&dosisMlPorKg=0.8&contenidoMlPorUnidad=10&precioProducto=12500&manoObra=25000&transporte=10000&veterinario=35000&otros=5000"
# Respuesta esperada: dosis total, costo de producto, costos adicionales y promedio por animal

# 5. Probar el health de Actuator
curl http://localhost:8080/actuator/health
# Respuesta esperada: {"status":"UP", ...}
```

## Entidades y procesos

El dominio completo tiene doce entidades (Animal, Lote, ProductoVeterinario,
Inventario, JornadaSanitaria, AplicacionSanitaria, PlanSanitario, Proveedor,
Compra, Usuario, AlternativaDeProducto, PresupuestoJornada) y tres procesos de
negocio (planificación y ejecución de jornada sanitaria, cálculo transaccional
del costo de jornada, y presupuesto con asistente de optimización de costos).
El detalle de reglas, cálculos y validaciones está en
[docs/propuesta-dominio.md](docs/propuesta-dominio.md).

## Checklist de entrega

- Propuesta de dominio documentada en `docs/propuesta-dominio.md`.
- Esqueleto Spring Boot 3 + Gradle por capas.
- `README` con instrucciones de ejecución y alcance funcional.
- `.gitignore` para Gradle, IDE y temporales.
- CI en GitHub Actions para compilar en cada push.
- ADR en `docs/adr/` con las decisiones tecnológicas.

## Resultado esperado

La base deja listo el sistema para luego conectar base de datos, formularios,
validaciones avanzadas, alertas y reportes por animal, lote o período.
