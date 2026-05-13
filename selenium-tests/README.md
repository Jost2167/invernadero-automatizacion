# Selenium CRUD Tests

Suite E2E para crear registros CRUD desde datos declarados en `test-data.json`.

## Requisitos

- Frontend ejecutandose en `BASE_URL`.
- Backend/API accesible desde el frontend.
- Credenciales de prueba configuradas en el entorno.
- Chrome instalado; WebDriverManager descarga/configura el driver.

## Variables de entorno

Ejemplo minimo:

```bash
BASE_URL=http://localhost:5173
TEST_USER=qa@example.com
TEST_PASS=change-me
TEST_TIMEOUT_SECONDS=10
```

En PowerShell:

```powershell
$env:BASE_URL = "http://localhost:5173"
$env:TEST_USER = "qa@example.com"
$env:TEST_PASS = "change-me"
$env:TEST_TIMEOUT_SECONDS = "10"
```

Variables opcionales:

- `LOGIN_PATH`: ruta del login. Default: `/login`.
- `LOGIN_USER_SELECTOR`: selector del campo de usuario.
- `LOGIN_PASS_SELECTOR`: selector del campo de contrasena.
- `LOGIN_SUBMIT_SELECTOR`: selector del boton de login.
- `SESSION_INDICATOR_SELECTOR`: selector visible que confirma sesion activa.
- `FORM_SUBMIT_SELECTOR`: selector del boton de guardado en formularios CRUD.
- `TEST_AUTH_TOKEN`: JWT ya emitido para saltar el formulario de login.
- `TEST_AUTH_TOKEN_URL`: endpoint que devuelve `{ "token": "..." }`; util con el perfil backend `e2e`.

## Ejecucion

Desde `selenium-tests/`, con Maven instalado:

```bash
mvn test -Dtest=CrudFormTest
```

Desde `selenium-tests/`, usando el wrapper del repo:

```powershell
..\mvnw.cmd test -Dtest=CrudFormTest
```

Modo visual:

```powershell
..\mvnw.cmd test -Dtest=CrudFormTest -Dheadless=false
```

Modo visual pausado:

```powershell
..\mvnw.cmd test -Dtest=CrudFormTest -Dheadless=false -DstepDelayMs=700
```

Usando Microsoft Edge:

```powershell
..\mvnw.cmd test -Dtest=CrudFormTest -Dbrowser=edge
```

La ejecucion genera `test-results.html` en la raiz de `selenium-tests/`.

## Entorno local E2E

El backend incluye un perfil `e2e` que usa H2 en memoria, crea el usuario `e2e@example.com` y expone `/e2e/token` solo bajo ese perfil.

Arranque del backend:

```powershell
..\mvnw.cmd -pl backend spring-boot:run -Dspring-boot.run.profiles=e2e
```

Arranque del frontend:

```powershell
npm run dev -- --host 127.0.0.1
```

Variables para ejecutar Selenium contra ese entorno:

```powershell
$env:BASE_URL = "http://127.0.0.1:5173"
$env:TEST_USER = "e2e@example.com"
$env:TEST_PASS = "e2e"
$env:TEST_AUTH_TOKEN_URL = "http://localhost:8080/e2e/token?email=e2e@example.com"
$env:SESSION_INDICATOR_SELECTOR = ".MuiAvatar-root"
```

## Formato de `test-data.json`

```json
{
  "timeout": 10,
  "models": [
    {
      "name": "Location",
      "formUrl": "/location/new",
      "fields": [
        { "selector": "(//form//input[@type='text'])[1]", "value": "Ubicacion Selenium ${RUN_ID}" }
      ],
      "successIndicator": "//table//*[contains(normalize-space(), 'Ubicacion Selenium ${RUN_ID}')]"
    }
  ]
}
```

Campos:

- `timeout`: opcional, segundos de espera. `TEST_TIMEOUT_SECONDS` tiene prioridad.
- `models`: lista de modelos CRUD a ejecutar.
- `name`: nombre legible del modelo.
- `formUrl`: ruta relativa o URL absoluta del formulario.
- `fields`: lista de pares `selector` y `value`.
- `successIndicator`: selector CSS o XPath que debe aparecer tras guardar.

Los selectores que empiezan con `/` o `(` se tratan como XPath; los demas como CSS.

`value`, `selector`, `formUrl` y `successIndicator` soportan `${RUN_ID}` para evitar choques con campos unicos entre ejecuciones. Tambien puedes fijarlo con:

```powershell
..\mvnw.cmd test -Dtest=CrudFormTest -Dtest.run.id=202605110001
```
