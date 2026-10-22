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
  -d '{"username":"admin","password":"<SISGANADO_ADMIN_PASSWORD>"}'

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

Los nombres de variables utilizados por despliegue son
`SPRING_PROFILES_ACTIVE`, `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`,
`PGPASSWORD`, `SPRING_DATA_MONGODB_URI`, `SPRING_DATA_MONGODB_DATABASE`,
`SISGANADO_JWT_SECRET`, `SISGANADO_ADMIN_PASSWORD`,
`SISGANADO_VETERINARIAN_PASSWORD`, `SISGANADO_CORS_ALLOWED_ORIGINS` y
`VITE_API_BASE_URL`. Los nombres de variables para ajustes locales adicionales
se detallan en [docs/despliegue.md](docs/despliegue.md). No se publican sus
valores aquí.

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

En perfiles `local`, `docker` y `prod`, la bitácora documental está disponible
para ADMIN. El POST recibe un JSON validado y responde `201 Created` con `Location`;
la colección devuelve DTOs y el recurso individual puede consultarse por ID.
Los errores HTTP usan `application/problem+json`; los errores inesperados
responden 500 con un detalle genérico y las validaciones incluyen `errors` por
campo.

| Recurso | Rutas principales | Acceso de escritura |
| --- | --- | --- |
| Animales | `/api/v1/animales`, `/api/v1/animales/{id}` | ADMIN |
| Lotes | `/api/v1/lotes`, `/api/v1/lotes/{id}` | ADMIN |
| Productos | `/api/v1/productos`, `/api/v1/productos/{id}` | ADMIN |
| Inventario | `/api/v1/inventarios`, `/api/v1/inventarios/{id}` | ADMIN |
| Planes sanitarios | `/api/v1/planes-sanitarios`, `/api/v1/planes-sanitarios/{id}` | ADMIN |
| Proveedores | `/api/v1/proveedores`, `/api/v1/proveedores/{id}` | ADMIN |
| Compras | `/api/v1/compras`, `/api/v1/compras/{id}` | ADMIN |
| Bitácora | `/api/v1/bitacora`, `/api/v1/bitacora/{id}` | ADMIN |
| Jornadas sanitarias | `/api/v1/jornadas`, `/api/v1/jornadas/{id}` | ADMIN/VETERINARIO |
| Ejecución | `/api/v1/jornadas/{id}/ejecucion` | ADMIN/VETERINARIO, propietario |
| Costeo | `/api/v1/jornadas/{id}/costo` | ADMIN/VETERINARIO, propietario |

Las colecciones de animales, lotes, productos, inventarios, planes, compras,
proveedores y jornadas propias aceptan `page`, `size` y `sort=propiedad,direccion`;
las respuestas usan la forma estable `content` + `page` de `PagedModel`.
Los animales aceptan `estado`, `raza` y `loteId`; los productos `precioMinimo`
y `precioMaximo`, implementados con
Spring Data Specifications. Los errores de validación y de negocio se devuelven
como `application/problem+json` (RFC 9457).

Al planificar un tratamiento, el plan sanitario define `intervaloMinimoDias`
(7 por defecto). La ejecución verifica el historial por animal y producto, y
comprueba que `dosisMlPorKg` esté dentro de `dosisMinima`/`dosisMaxima` del
producto cuando esos límites están configurados. El producto también define
`retiroCarneDias` y `retiroLecheDias` (0 por defecto); al aplicar, se guardan
las fechas de fin de retiro en la aplicación para que cambios posteriores del
catálogo no alteren la advertencia histórica.

La ejecución consume primero los lotes vigentes con vencimiento más próximo y
guarda el detalle de lote, unidades y costo unitario usado. El endpoint de
costeo solo valora ese consumo registrado: no vuelve a modificar existencias,
incluye todos los productos de la jornada y conserva el costo del lote usado.
Los presupuestos estimados usan Strategy por mililitro; las jornadas reales
usan Strategy por unidad del lote consumido. La `JornadaSanitariaFactory`
construye las jornadas y aplicaciones utilizadas por el servicio.
Las jornadas ejecutadas antes de la migración V8 no contienen este detalle y
no se costean automáticamente: se requiere reconciliar el consumo real para no
atribuirles un lote o costo incorrecto.

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

## Laboratorio 5: interfaces y despliegue

El módulo administrativo Thymeleaf cubre la gestión de productos veterinarios
en `/admin/productos` y de proveedores en `/admin/proveedores`. Ambos flujos
incluyen listado paginado, alta/edición con validación de Bean Validation y
Post/Redirect/Get. Se autentica por sesión y los formularios POST conservan la
protección CSRF de Spring Security.

La SPA React de [spa/](spa/) permite iniciar sesión con JWT, consultar jornadas
con paginación y planificar o ejecutar una jornada. El formulario de ejecución
permite elegir animales activos del lote y asignar dosis a los productos del
plan sanitario. En el frontend se mantienen estados visibles de carga, datos y
error. La API ofrece el filtro `loteId` para consultar los animales del lote.

### Configuración local y producción

Use el perfil `dev` para desarrollar con H2 en memoria y la SPA Vite en
`http://localhost:5173`; Vite reenvía `/api` y `/auth` a Spring Boot. La
configuración CORS predeterminada de desarrollo solo admite ese origen.
`prod` requiere PostgreSQL, MongoDB Atlas y los secretos externos configurados.
La lista de variables —solo sus nombres— se documenta en
[docs/despliegue.md](docs/despliegue.md).

El blueprint [../render.yaml](../render.yaml) prepara la API y la SPA estática
en Render Free. PostgreSQL se administra en el tier gratuito de Neon y MongoDB
en Atlas. El
[diagrama de despliegue](docs/diagrama-despliegue.md) documenta el tráfico
HTTPS, JWT y las conexiones a las bases de datos.

Para ejecutar la SPA localmente:

```powershell
Set-Location spa
npm ci
npm run dev
```

El despliegue público no se puede completar desde este repositorio: requiere
cuentas de Render y MongoDB Atlas, configuración de sus secretos y dominios. Al
terminarlo, registre en el aula virtual las URLs HTTPS de API, SPA y Swagger,
junto con las credenciales efímeras de prueba de cada rol. No incluya esos
valores en Git o en este README.
