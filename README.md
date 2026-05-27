# Invernadero

Sistema web para la automatizacion y gestion operativa de un invernadero. El
proyecto integra un backend REST con Spring Boot, un frontend React/Vite,
pruebas automatizadas E2E con Selenium y herramientas internas para generar
CRUDs a partir de definiciones JSON.

## Contenido

- [Vision general](#vision-general)
- [Stack tecnico](#stack-tecnico)
- [Estructura del repositorio](#estructura-del-repositorio)
- [Modulos funcionales](#modulos-funcionales)
- [Requisitos](#requisitos)
- [Configuracion](#configuracion)
- [Ejecucion local](#ejecucion-local)
- [API y autenticacion](#api-y-autenticacion)
- [Pruebas](#pruebas)
- [Herramientas internas](#herramientas-internas)
- [Documentacion y diagrama ER](#documentacion-y-diagrama-er)
- [GitHub Actions](#github-actions)
- [Despliegue](#despliegue)

## Vision general

La aplicacion permite administrar entidades clave de un invernadero:
ubicaciones, invernaderos, sensores, lecturas climaticas, ciclos de cultivo,
eventos de fertilizacion, eventos de riego, alertas, tareas de mantenimiento e
inspecciones de plagas.

El acceso se protege con OAuth2 de Google y JWT. El backend emite el token al
terminar el login OAuth2 y el frontend lo guarda en `localStorage` para llamar a
los endpoints protegidos.

## Stack tecnico

| Capa | Tecnologias |
| --- | --- |
| Backend | Java 17, Spring Boot 3.5, Spring Web, Spring Security, OAuth2 Client, JWT, Spring Data JPA |
| Base de datos | PostgreSQL en desarrollo/produccion, H2 para pruebas y perfil E2E |
| API docs | springdoc-openapi, Swagger UI |
| Frontend | React 18, Vite, Material UI, React Router, Axios, i18next |
| Diagramas | React Flow, Dagre, schemas JSON de `tools/codegen/examples` |
| Tests frontend | Vitest, Testing Library, jsdom |
| Tests backend | JUnit 5, Spring Boot Test, Spring Security Test |
| Tests E2E | Selenium WebDriver, WebDriverManager, JUnit 5 |
| Herramientas | Maven multi-modulo, Picocli, Handlebars, JSON Schema Validator, PDFBox |
| Despliegue | Render, Dockerfile backend, GitHub Actions |

## Estructura del repositorio

```text
.
|-- backend/                 # API Spring Boot
|-- frontend/                # SPA React/Vite
|-- selenium-tests/          # Suite E2E con Selenium
|-- tools/
|   |-- codegen/             # Generador de CRUDs backend/frontend desde JSON
|   `-- taiga-sync/          # Sincronizacion de schemas JSON desde Taiga
|-- .github/workflows/       # Workflow de pruebas y deploy backend
|-- openspec/                # Cambios y especificaciones del flujo OpenSpec
|-- Dockerfile               # Imagen backend desde la raiz
|-- render.yaml              # Infraestructura Render
|-- pom.xml                  # Reactor Maven: backend, codegen, taiga-sync
`-- .env.example             # Variables de entorno de referencia
```

## Modulos funcionales

Cada modulo CRUD tiene backend, frontend y pruebas asociadas:

| Modulo | Ruta frontend | Endpoint base |
| --- | --- | --- |
| Dashboard | `/` | N/A |
| Ubicaciones | `/location` | `/api/location` |
| Invernaderos | `/greenhouse` | `/api/greenhouse` |
| Sensores | `/sensor` | `/api/sensor` |
| Lecturas climaticas | `/climate-reading` | `/api/climate-reading` |
| Ciclos de cultivo | `/crop-cycle` | `/api/crop-cycle` |
| Fertilizacion | `/fertilization-event` | `/api/fertilization-event` |
| Riego | `/irrigation-event` | `/api/irrigation-event` |
| Alertas | `/greenhouse-alert` | `/api/greenhouse-alert` |
| Mantenimiento | `/maintenance-task` | `/api/maintenance-task` |
| Inspecciones de plagas | `/pest-inspection` | `/api/pest-inspection` |
| Documentacion interna | `/docs` | N/A |
| Diagrama ER | `/er-diagram` | N/A |

Los endpoints CRUD exponen el patron:

- `GET /api/<modulo>`: listar registros.
- `GET /api/<modulo>/{id}`: consultar un registro.
- `POST /api/<modulo>`: crear un registro.
- `PUT /api/<modulo>/{id}`: actualizar un registro.
- `DELETE /api/<modulo>/{id}`: eliminar un registro.

## Requisitos

- Java 17.
- Node.js 18 o superior.
- npm.
- PostgreSQL 14 o superior para ejecucion local normal.
- Chrome o Edge para la suite Selenium.
- Cuenta OAuth2 de Google configurada para login real.

En Windows usa `.\mvnw.cmd`. En Linux/macOS usa `./mvnw`.

## Configuracion

El archivo `.env.example` documenta las variables esperadas. Es una referencia:
exporta sus valores en tu shell, en tu proveedor de despliegue o en archivos
locales de la herramienta que corresponda.

Variables principales para backend local:

```powershell
$env:DB_PASSWORD = "<password-postgres>"
$env:JWT_SECRET = "<secret-de-al-menos-32-caracteres>"
$env:JWT_EXPIRATION = "900000"
$env:GOOGLE_CLIENT_ID = "<google-client-id>"
$env:GOOGLE_CLIENT_SECRET = "<google-client-secret>"
$env:APP_OAUTH2_REDIRECT_URI = "http://localhost:5173/auth/callback"
$env:APP_OAUTH2_AUTO_REGISTER = "true"
```

La configuracion local por defecto espera PostgreSQL en:

```text
jdbc:postgresql://localhost:5432/invernadero
usuario: postgres
password: valor de DB_PASSWORD
```

Para Google OAuth2, configura en Google Cloud Console el redirect URI del
backend:

```text
http://localhost:8080/login/oauth2/code/google
```

`APP_OAUTH2_REDIRECT_URI` es el callback del frontend al que el backend redirige
despues de autenticar:

```text
http://localhost:5173/auth/callback
```

Nota: el auto registro OAuth2 asigna `ROLE_USER`. En el perfil `e2e` este rol se
crea automaticamente. En ambientes normales debe existir en la tabla `roles`.

## Ejecucion local

### 1. Instalar dependencias frontend

```powershell
npm --prefix frontend ci
```

### 2. Preparar base de datos

Crea una base PostgreSQL llamada `invernadero` y exporta las variables del
backend. El esquema se mantiene con JPA (`ddl-auto: update`) en los perfiles
local y prod. Los archivos SQL en `backend/src/main/resources/db/migration` son
salidas generadas por la herramienta de codegen y pueden aplicarse manualmente
si tu flujo lo requiere.

### 3. Ejecutar backend

```powershell
.\mvnw.cmd -pl backend spring-boot:run
```

El backend queda en:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

### 4. Ejecutar frontend

```powershell
npm --prefix frontend run dev
```

El frontend queda en:

```text
http://localhost:5173
```

Si la API no esta en `http://localhost:8080`, define `VITE_API_BASE_URL` antes
de iniciar Vite:

```powershell
$env:VITE_API_BASE_URL = "https://tu-api.example.com"
npm --prefix frontend run dev
```

### Perfil local E2E

Para pruebas sin PostgreSQL ni Google OAuth real, el backend incluye el perfil
`e2e`. Usa H2 en memoria, crea el usuario `e2e@example.com` y expone un endpoint
para emitir JWTs de prueba.

```powershell
.\mvnw.cmd -pl backend spring-boot:run -Dspring-boot.run.profiles=e2e
```

Token de prueba:

```text
http://localhost:8080/e2e/token?email=e2e@example.com
```

Para entrar al frontend manualmente con ese token, abre:

```text
http://localhost:5173/auth/callback?token=<jwt>
```

No uses el perfil `e2e` en produccion.

## API y autenticacion

Endpoints de autenticacion:

| Endpoint | Uso |
| --- | --- |
| `GET /oauth2/authorization/google` | Inicia login con Google desde Spring Security |
| `GET /auth/me` | Devuelve el usuario autenticado |
| `POST /auth/logout` | Respuesta de logout para el cliente |
| `GET /auth/swagger-token?email=<email>` | Genera JWT para Swagger fuera de `prod` |
| `GET /e2e/token?email=<email>` | Genera JWT solo con perfil `e2e` |

El frontend:

- Lee la API base desde `VITE_API_BASE_URL`.
- Usa `http://localhost:8080` como fallback.
- Guarda el JWT en `localStorage` con la llave `invernadero.jwt`.
- Envia `Authorization: Bearer <token>` en cada request.
- Envia `Accept-Language` segun el idioma activo.

El backend:

- Permite publicamente OAuth2, Swagger/OpenAPI, `/auth/swagger-token` fuera de
  `prod` y `/e2e/**` bajo perfil E2E.
- Protege el resto de endpoints con JWT.
- Soporta mensajes en espanol e ingles mediante `messages_es.properties` y
  `messages_en.properties`.

## Pruebas

### Backend

```powershell
.\mvnw.cmd -pl backend test
```

### Reactor Maven completo

Incluye `backend`, `tools/codegen` y `tools/taiga-sync`.

```powershell
.\mvnw.cmd test
```

### Frontend

```powershell
npm --prefix frontend test
```

Modo watch:

```powershell
npm --prefix frontend run test:watch
```

Build de produccion:

```powershell
npm --prefix frontend run build
```

### Selenium E2E

Desde `selenium-tests/`:

```powershell
..\mvnw.cmd test -Dtest=CrudFormTest
```

Modo visual:

```powershell
..\mvnw.cmd test -Dtest=CrudFormTest -Dheadless=false
```

Variables comunes:

```powershell
$env:BASE_URL = "http://localhost:5173"
$env:TEST_AUTH_TOKEN_URL = "http://localhost:8080/e2e/token?email=e2e@example.com"
$env:SESSION_INDICATOR_SELECTOR = ".MuiAvatar-root"
```

La suite lee `selenium-tests/test-data.json` y genera:

```text
selenium-tests/test-results.html
```

Mas detalle en `selenium-tests/README.md`.

## Herramientas internas

### Codegen

`tools/codegen` genera artefactos backend, migraciones SQL y frontend a partir
de definiciones JSON como las de `tools/codegen/examples`.

Compilar:

```powershell
.\mvnw.cmd -pl tools/codegen package
```

Validar un schema:

```powershell
java -jar tools/codegen/target/codegen.jar validate tools/codegen/examples/Sensor.json
```

Generar archivos:

```powershell
java -jar tools/codegen/target/codegen.jar generate tools/codegen/examples/Sensor.json --dry-run
```

Exportar PDF para el frontend:

```powershell
java -jar tools/codegen/target/codegen.jar export-examples-pdf --output frontend/public/docs/examples-export.pdf
```

El frontend depende de estos marcadores para inyectar rutas y navegacion:

```text
frontend/src/App.jsx                 // codegen:routes
frontend/src/components/Sidebar.jsx  // codegen:nav
```

Mas detalle en `tools/codegen/README.md`.

### Taiga sync

`tools/taiga-sync` sincroniza user stories de Taiga con tag `codegen-schema`,
lee el campo personalizado `codegen_json`, valida el JSON y escribe schemas en
`tools/codegen/examples`.

Compilar:

```powershell
.\mvnw.cmd -pl tools/taiga-sync -am package
```

Variables requeridas:

```powershell
$env:TAIGA_BASE_URL = "https://taiga.example.com"
$env:TAIGA_AUTH_TOKEN = "<token>"
$env:TAIGA_PROJECT_SLUG = "<slug>"
```

Ejecucion:

```powershell
java -jar tools/taiga-sync/target/taiga-sync-0.0.1-SNAPSHOT.jar --dry-run
```

Mas detalle en `tools/taiga-sync/README.md`.

## Documentacion y diagrama ER

El frontend expone:

- `/docs`: tarjetas para descargar PDF de ejemplos y abrir el diagrama ER.
- `/er-diagram`: diagrama entidad-relacion construido desde
  `tools/codegen/examples`.

El plugin Vite `erSchemasPlugin` carga los JSON de ejemplos en build/dev como un
modulo virtual. Si agregas o modificas schemas, reinicia Vite para refrescar el
diagrama.

El PDF que usa el frontend debe existir en:

```text
frontend/public/docs/examples-export.pdf
```

## GitHub Actions

El repositorio incluye el workflow `.github/workflows/deploy.yml` con el nombre
`Deploy Backend`.

Se ejecuta en dos casos:

- Cada push a la rama `main`.
- Manualmente desde la pestana `Actions` de GitHub usando `workflow_dispatch`.

Flujo del job `test-and-deploy`:

1. Descarga el codigo con `actions/checkout@v4`.
2. Configura Java 17 con `actions/setup-java@v4` y distribucion Temurin.
3. Ejecuta las pruebas del backend:

```bash
./mvnw -pl backend test
```

4. Si las pruebas pasan, llama el deploy hook de Render:

```bash
curl -X POST "${{ secrets.RENDER_DEPLOY_HOOK }}"
```

Secreto requerido en GitHub:

| Secret | Uso |
| --- | --- |
| `RENDER_DEPLOY_HOOK` | URL privada del deploy hook de Render para disparar el despliegue del backend |

Configuracion recomendada:

1. En Render, crea o copia el deploy hook del servicio backend.
2. En GitHub, ve a `Settings > Secrets and variables > Actions`.
3. Crea el secret `RENDER_DEPLOY_HOOK` con la URL del hook.
4. Verifica que el workflow tenga permisos para ejecutarse en la rama `main`.

Notas importantes:

- El workflow actual prueba y despliega el backend.
- El frontend se despliega con la configuracion declarada en `render.yaml`.
- Si `./mvnw -pl backend test` falla, el deploy no se dispara.
- El workflow no ejecuta `npm test`, `npm run build` ni la suite Selenium.

## Despliegue

El archivo `render.yaml` define:

- Base PostgreSQL `invernadero-db`.
- Servicio web Java `invernadero-backend`.
- Servicio estatico `invernadero-frontend`.

Build backend en Render:

```text
mvn -pl backend package -DskipTests
```

Start backend:

```text
java -jar backend/target/invernadero-automatizacion-0.0.1-SNAPSHOT.jar
```

Build frontend:

```text
npm --prefix frontend ci && npm --prefix frontend run build
```

Variables esperadas en Render:

| Variable | Uso |
| --- | --- |
| `SPRING_PROFILES_ACTIVE=prod` | Activa configuracion de produccion |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | Conexion PostgreSQL |
| `JWT_SECRET` | Firma JWT |
| `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` | OAuth2 Google |
| `APP_OAUTH2_REDIRECT_URI` | Callback del frontend desplegado |
| `ALLOWED_ORIGINS` | Origen permitido del frontend |
| `VITE_API_BASE_URL` | URL publica del backend para el build frontend |

El despliegue del backend tambien puede dispararse desde GitHub Actions mediante
el secreto:

```text
RENDER_DEPLOY_HOOK
```

## Comandos rapidos

```powershell
# Backend
.\mvnw.cmd -pl backend spring-boot:run
.\mvnw.cmd -pl backend test

# Frontend
npm --prefix frontend ci
npm --prefix frontend run dev
npm --prefix frontend test
npm --prefix frontend run build

# Maven reactor
.\mvnw.cmd test

# Codegen
.\mvnw.cmd -pl tools/codegen package
java -jar tools/codegen/target/codegen.jar --help

# Selenium
cd selenium-tests
..\mvnw.cmd test -Dtest=CrudFormTest
```
