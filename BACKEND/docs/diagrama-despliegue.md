# Diagrama de despliegue

```mermaid
flowchart LR
    Browser["Navegador del usuario"]
    SPA["React SPA\nRender Static Site\nHTTPS"]
    API["SisGanado API\nSpring Boot + Thymeleaf\nRender Web Service / Docker\nHTTPS"]
    PG[("PostgreSQL gestionado\nRender PostgreSQL")]
    Mongo[("MongoDB gestionado\nMongoDB Atlas")]

    Browser -->|HTTPS| SPA
    Browser -->|HTTPS · CORS con origen SPA| API
    SPA -->|JWT · HTTPS| API
    API -->|JDBC · red privada| PG
    API -->|MongoDB URI/TLS| Mongo
```

El blueprint `render.yaml` aprovisiona la API, la SPA y PostgreSQL. MongoDB se
crea en MongoDB Atlas y se conecta mediante `SPRING_DATA_MONGODB_URI`. Se debe
configurar `SISGANADO_CORS_ALLOWED_ORIGINS` con el origen HTTPS exacto publicado
por el sitio estático y `VITE_API_BASE_URL` con la URL HTTPS de la API antes de
compilar la SPA.
