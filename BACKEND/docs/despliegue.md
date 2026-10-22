# Despliegue del Laboratorio 5

## Servicios

El archivo `../render.yaml` define:

- API Spring Boot empaquetada en Docker, plan **Free**, verificada por
  `/actuator/health`.
- Sitio estático React/Vite desde `spa/`.
- No crea una base PostgreSQL en Render: el plan gratuito de PostgreSQL de
  Render vence a los 30 días. Se conecta a PostgreSQL externo en Neon.

PostgreSQL debe crearse en Neon y MongoDB en MongoDB Atlas, escogiendo sus
tiers gratuitos y revisando sus límites y políticas de inactividad. En Atlas,
habilite conectividad de red desde Render y cree un usuario con privilegios
mínimos para la base de auditoría. Guarde la URI como secreto
`SPRING_DATA_MONGODB_URI`; nunca la agregue al repositorio.

El Web Service Free de Render puede suspenderse tras 15 minutos sin tráfico y
tardar alrededor de un minuto en responder cuando vuelve a activarse. Tiene
límites de horas, ancho de banda y recursos, por lo que es adecuado para la
demostración académica, no para producción. La SPA estática de Render es
gratuita.

## Configuración del servicio API

Render inicia la aplicación con el perfil `prod`. En Neon, cree un proyecto y
copie los campos de conexión PostgreSQL (host, puerto, base y usuario) y la
contraseña a las variables privadas de la API en Render:

- `PGHOST`
- `PGPORT`
- `PGDATABASE`
- `PGUSER`
- `PGPASSWORD`
- `SPRING_DATA_MONGODB_URI`
- `SISGANADO_ADMIN_PASSWORD`
- `SISGANADO_VETERINARIAN_PASSWORD`
- `SISGANADO_CORS_ALLOWED_ORIGINS`

La conexión PostgreSQL se fuerza a TLS (`sslmode=require`).

Las dos contraseñas de prueba deben ser distintas, aleatorias y tener al menos
12 caracteres. La aplicación crea los usuarios `admin` (rol `ADMIN`) y
`veterinario` (rol `VETERINARIO`) solo si aún no existen. Cambiar estos secretos
después del primer arranque no cambia contraseñas ya persistidas; rótelas
mediante una operación administrativa antes de volver a anunciar credenciales.
`SISGANADO_JWT_SECRET` se genera con el blueprint y no se debe rotar sin
considerar la expiración de los tokens emitidos.

Antes de mostrar la SPA, cargue los datos maestros con el usuario `ADMIN`:
cree un lote, animales activos, un producto (también puede usar el formulario
MVC), un plan que incluya ese producto y una existencia de inventario vigente.
La aplicación no inserta datos de demostración en producción; sin esos
registros no habrá opciones para planificar ni existencias para ejecutar.
Puede cargar los recursos desde Swagger UI o con la
[colección REST](./sisganado-api.http).

`SISGANADO_CORS_ALLOWED_ORIGINS` debe contener únicamente el origen HTTPS
público de la SPA, sin ruta final. `VITE_API_BASE_URL` es la URL HTTPS del
servicio API y se configura antes de compilar el sitio estático, ya que Vite
incorpora este valor al bundle.

## Perfiles

- `dev`: H2 en memoria por defecto y MongoDB deshabilitado; permite ejecutar la
  aplicación local sin contenedores. Se pueden definir `SPRING_DATASOURCE_URL`,
  `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`,
  `SPRING_DATASOURCE_DRIVER_CLASS_NAME`, `SPRING_JPA_HIBERNATE_DDL_AUTO` y
  `SPRING_JPA_DIALECT` para cambiar la base local.
- `prod`: PostgreSQL externo con Flyway, MongoDB Atlas y configuración de secretos
  obligatoria. No inicia con credenciales, orígenes CORS o conexiones faltantes.

Para trabajar localmente:

```powershell
Set-Location BACKEND
.\gradlew.bat bootRun --args="--spring.profiles.active=dev"
Set-Location spa
npm ci
npm run dev
```

Vite sirve la SPA en `http://localhost:5173` y reenvía `/api` y `/auth` a
`http://localhost:8080`. Para React en producción, establezca
`VITE_API_BASE_URL` como variable de entorno de compilación del sitio.
Configure ambas variables con los dominios HTTPS que Render asigna a los
servicios definidos en el blueprint. Si la SPA obtiene otro dominio, actualice
el origen CORS y vuelva a desplegar la API.

## Credenciales y URL de entrega

Después de publicar ambos servicios y configurar Neon y Atlas, verifique que las tres
URL usan HTTPS:

- API: URL pública del servicio `sisganado-api`.
- SPA: URL pública del sitio `sisganado-spa`.
- Swagger UI: `https://<dominio-api>/swagger-ui/index.html`.

En el aula virtual entregue esas URL y los nombres de usuario junto con sus
contraseñas de prueba por rol. No registre las contraseñas en Git ni en el
README. Este repositorio no contiene URL ni credenciales reales; se completan
al crear los servicios y secretos en las cuentas institucionales.
