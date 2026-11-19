# ADR-004 · Autenticación y autorización de la API

**Estado:** Aceptada · **Fecha:** 19/11/2026 · **Responsable:** María Stephanie Vargas Ramírez

## Contexto

La SPA y la API se despliegan separadas y deben autenticar cada solicitud sin
depender de una sesión compartida entre servidores. Además, ADMIN, VETERINARIO
y USUARIO tienen permisos distintos; las cuentas veterinarias registradas por
el público necesitan aprobación antes de autenticarse.

## Decisión

El backend autentica con JWT Bearer firmado con HS256. Las contraseñas se
almacenan con BCrypt y el secreto de firma se obtiene de
`SISGANADO_JWT_SECRET` en producción. Los JWT incluyen sujeto, rol, emisión y
expiración. Las reglas de autorización se aplican en Spring Security y en los
servicios/controladores: ADMIN administra catálogos, VETERINARIO puede operar
jornadas y USUARIO tiene acceso de consulta. El registro público solo acepta
USUARIO o VETERINARIO; el veterinario queda inactivo hasta aprobación
administrativa.

## Alternativas consideradas

1. **Sesión HTTP con cookie:** válida para una aplicación web acoplada, pero
   introduce estado de sesión en el backend y no sirve directamente a una SPA
   independiente sin configurar afinidad o almacenamiento compartido.
2. **Proveedor externo OAuth 2.0/OIDC:** ofrece gestión de identidad
   profesional, pero añade una cuenta/servicio y configuración que no se
   requieren para la escala académica actual.
3. **JWT propio con Spring Security, opción elegida:** evita estado de sesión
   para la API y permite verificar roles en cada solicitud. La interfaz MVC
   heredada conserva su cadena de seguridad web separada.

## Consecuencias

- **Beneficio:** autenticación stateless y autorización por rol en cada
  operación protegida.
- **Costo:** el equipo debe custodiar y rotar el secreto; las contraseñas de
  producción deben configurarse fuera del repositorio.
- **Limitación:** no hay revocación individual del JWT antes de su expiración;
  cerrar sesión elimina el token del navegador, y el backend rechaza tokens
  inválidos o expirados.
- Los errores de autenticación y permisos se distinguen como 401 y 403; los
  eventos de autenticación se registran estructuradamente.

## Referencias

- `BACKEND/src/main/java/cr/ac/una/eif509/demo/security/SecurityConfiguration.java`
- `BACKEND/src/main/java/cr/ac/una/eif509/demo/security/AuthService.java`
- `BACKEND/src/main/java/cr/ac/una/eif509/demo/security/SecurityEventLogger.java`
