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

# Obtener un JWT local (cambie estas credenciales fuera de desarrollo)
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"AdminLocal2026!"}'

# Sustituya <JWT> por accessToken del resultado del login
curl http://localhost:8080/api/v1/ganado/control?animales=10 \
  -H "Authorization: Bearer <JWT>"

# Calcular el costo estimado de una jornada
curl "http://localhost:8080/api/v1/costos/jornada?cantidadAnimales=10&pesoPromedioKg=380&dosisMlPorKg=0.8&contenidoMlPorUnidad=10&precioProducto=12500&manoObra=25000&transporte=10000&veterinario=35000&otros=5000" \
  -H "Authorization: Bearer <JWT>"
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

## API REST v1 (Laboratorio 5)

La API versionada se publica bajo `/api/v1`; Swagger UI está disponible en
`/swagger-ui/index.html` y el esquema OpenAPI en `/v3/api-docs`. La colección
para VS Code REST Client está en [docs/sisganado-api.http](docs/sisganado-api.http).

Antes de usar recursos protegidos, autentíquese con `POST /auth/login` y envíe
el JWT recibido como `Authorization: Bearer <token>`. En perfiles `local`,
`docker` y `test` se provisionan usuarios de demostración si no existen:
`admin` (`SISGANADO_ADMIN_PASSWORD`) y `veterinario`
(`SISGANADO_VETERINARIAN_PASSWORD`). Los valores por defecto son únicamente
para desarrollo; cámbielos en cualquier entorno compartido.

La administración del catálogo también cuenta con interfaz HTML en
`/admin/productos`, disponible para `ADMIN` después de iniciar sesión en
`/login`. Esta interfaz usa sesión de servidor y protección CSRF para sus
formularios; se mantiene separada de la API REST, que continúa siendo stateless
y autenticada exclusivamente mediante JWT.

La firma JWT acepta `SISGANADO_JWT_SECRET` (mínimo 32 bytes). Los cambios de
catálogo y el acceso a `/api/v1/admin/**` requieren `ADMIN`; los procesos
sanitarios aceptan `ADMIN` o `VETERINARIO`. La API no crea sesiones y cada
jornada planificada queda asociada al usuario autenticado; leerla, ejecutarla
o costearla requiere ser su propietario. Un recurso ajeno se responde como
404 para evitar revelar su existencia.

| Recurso | Rutas principales | Acceso de escritura |
| --- | --- | --- |
| Animales | `/api/v1/animales`, `/api/v1/animales/{id}` | ADMIN |
| Lotes | `/api/v1/lotes`, `/api/v1/lotes/{id}` | ADMIN |
| Productos | `/api/v1/productos`, `/api/v1/productos/{id}` | ADMIN |
| Inventario | `/api/v1/inventarios`, `/api/v1/inventarios/{id}` | ADMIN |
| Planes sanitarios | `/api/v1/planes-sanitarios`, `/api/v1/planes-sanitarios/{id}` | ADMIN |
| Proveedores | `/api/v1/proveedores`, `/api/v1/proveedores/{id}` | ADMIN |
| Compras | `/api/v1/compras`, `/api/v1/compras/{id}` | ADMIN |
| Jornadas sanitarias | `/api/v1/jornadas`, `/api/v1/jornadas/{id}` | ADMIN/VETERINARIO |
| Ejecución | `/api/v1/jornadas/{id}/ejecucion` | ADMIN/VETERINARIO, propietario |
| Costeo | `/api/v1/jornadas/{id}/costo` | ADMIN/VETERINARIO, propietario |

Las colecciones de animales, lotes, productos, inventarios, planes, compras,
proveedores y jornadas propias aceptan `page`, `size` y `sort=propiedad,direccion`;
las respuestas usan la forma estable `content` + `page` de `PagedModel`.
Los animales aceptan `estado` y
`raza`; los productos `precioMinimo` y `precioMaximo`, implementados con
Spring Data Specifications. Los errores de validación y de negocio se devuelven
como `application/problem+json` (RFC 9457).

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
