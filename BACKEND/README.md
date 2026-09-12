# SisGanado Backend

Backend independiente para el sistema de control sanitario y costos de ganado.

## Objetivo
Dejar el backend separado del repositorio principal, con una arquitectura preparada para:
- PostgreSQL como base relacional para transacciones y dominio principal.
- MongoDB como base documental para auditoría y registros flexibles.
- Spring Boot 3 + Java 21 como capa de servicio y API.

## Stack
- Java 21
- Spring Boot 3.3.x
- Gradle
- PostgreSQL 16
- MongoDB 7
- Docker Compose

## Estructura del backend
- Capa de dominio: entidades del negocio.
- Capa de datos: repositorios y acceso a persistencia.
- Capa de negocio: lógica y validaciones.
- Capa de presentación: controladores REST.

## Requisitos
- Java 21
- Docker Desktop
- Git

## Ejecutar infraestructura
```bash
docker compose up -d
```

Este comando levanta PostgreSQL y MongoDB juntos. Compose incluye healthchecks
para confirmar que ambos servicios están disponibles:

```bash
docker compose ps
```

## Ejecutar la aplicación
```bash
./gradlew bootRun
```

Para usar las dos bases de datos Docker y cargar datos de ejemplo en ambas,
ejecute la aplicación con el perfil `local` después de levantar Compose:

```bash
./gradlew bootRun --args="--spring.profiles.active=local"
```

El `DataSeeder` crea un lote y dos animales en PostgreSQL, y un evento en la
colección MongoDB `bitacora_eventos`. El seeder no duplica registros existentes.

❌ Facturación electrónica ante Hacienda.  
❌ Integración con sistemas de pago o banca.  
❌ Gestión de ventas de ganado (solo crianza y sanitario).  
# Compilar y ejecutar las pruebas
❌ Sincronización con laboratorios veterinarios externos.  
❌ Predicción avanzada con modelos externos.
# Levantar PostgreSQL y MongoDB
docker compose up -d

# Iniciar la aplicación con datos de ejemplo
./gradlew bootRun --args="--spring.profiles.active=local"
```

En Windows, use `gradlew.bat` en lugar de `./gradlew`.

## Cómo probarlo

```bash
# Verificar que la aplicación está disponible
curl http://localhost:8080/actuator/health

# Consultar el resumen sanitario
curl "http://localhost:8080/api/ganado/control?animales=10"

# Calcular el costo de una jornada
curl "http://localhost:8080/api/jornadas/costo?animales=10&pesoPromedioKg=380&dosisMlPorKg=0.8&contenidoMlPorUnidad=10&precioProducto=12500&manoObra=25000&transporte=10000&veterinario=35000&otros=5000"
```

## Documentación

- [Propuesta de dominio](docs/propuesta-dominio.md)
- [Modelo de datos](docs/modelo-datos.md)
- [Persistencia con ORM y repositorios](docs/laboratorio-3-persistencia.md)
- [ADR-001: elección del stack](docs/adr/ADR-001-eleccion-del-stack.md)
- [ADR-002: presupuesto y asistente de optimización](docs/adr/ADR-002-presupuesto-y-asistente-inteligente.md)
- Esqueleto Spring Boot 3 + Gradle por capas.
- `README` con instrucciones de ejecución y alcance funcional.
- `.gitignore` para Gradle, IDE y temporales.
- CI en GitHub Actions para compilar en cada push.
- ADR en `docs/adr/` con la decisión tecnológica.

## Entidades de negocio

El esqueleto incluye entidades base para:

- Animal
- Lote
- Producto veterinario
- Inventario
- Plan sanitario
- Aplicación sanitaria
- Jornada sanitaria
- Proveedor
- Compra
- Usuario

## Procesos cubiertos por el esqueleto

1. Programación y aplicación sanitaria.
2. Cálculo del costo de una jornada sanitaria.
3. Control de inventario veterinario.

## Resultado esperado

La base deja listo el sistema para luego conectar base de datos, formularios,
validaciones avanzadas, alertas y reportes por animal, lote o período.
