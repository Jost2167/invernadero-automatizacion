# Taiga sync

`tools/taiga-sync` sincroniza schemas JSON de entidades desde user stories de Taiga hacia
`tools/codegen/examples/`. Procesa solo stories con el tag `codegen-schema`, lee el campo
personalizado `codegen_json`, valida el contenido con `tools/codegen` y:

- si el JSON es valido, escribe `tools/codegen/examples/<name>.json`;
- si el JSON es invalido, publica un comentario de validacion en la story;
- si dos stories definen el mismo `name`, no escribe ninguna y comenta el conflicto en ambas.

## Build

Desde la raiz del repo, instala primero `codegen` y luego empaqueta `taiga-sync`:

```powershell
.\mvnw.cmd -pl tools/codegen -DskipTests install
.\mvnw.cmd -pl tools/taiga-sync package
```

Tambien puedes construirlo como parte del reactor:

```powershell
.\mvnw.cmd -pl tools/taiga-sync -am package
```

## Variables de entorno

Variables requeridas:

- `TAIGA_BASE_URL`: URL base de Taiga, por ejemplo `https://taiga.example.com`.
- `TAIGA_AUTH_TOKEN`: token de API. Se envia como `Authorization: Bearer <token>`.
- `TAIGA_PROJECT_SLUG`: slug del proyecto Taiga.

Variable opcional:

- `TAIGA_CODEGEN_FIELD_ID`: ID numerico del campo personalizado `codegen_json`.

Si `TAIGA_CODEGEN_FIELD_ID` no esta definida, el sync consulta
`/api/v1/userstory-custom-attributes?project=<id>` y busca el atributo con `name == "codegen_json"`.

Ejemplo en PowerShell:

```powershell
$env:TAIGA_BASE_URL = "https://taiga.example.com"
$env:TAIGA_AUTH_TOKEN = "replace-me"
$env:TAIGA_PROJECT_SLUG = "greenhouse"
$env:TAIGA_CODEGEN_FIELD_ID = "77"
```

## Ejecucion manual

Empaqueta el modulo y ejecuta el JAR desde la raiz del repo:

```powershell
java -jar tools/taiga-sync/target/taiga-sync-0.0.1-SNAPSHOT.jar
```

Al finalizar imprime un resumen con:

- stories procesadas;
- schemas escritos;
- stories con errores de validacion;
- stories omitidas porque no tenian `codegen_json`.

El comando retorna exit code `0` si el proceso completo, aunque algunas stories tengan errores
de validacion. Retorna exit code `1` para errores de configuracion, autenticacion, red o API.

## Dry-run

Usa `--dry-run` para validar y listar acciones sin escribir archivos ni publicar comentarios:

```powershell
java -jar tools/taiga-sync/target/taiga-sync-0.0.1-SNAPSHOT.jar --dry-run
```

En este modo el proceso sigue consultando Taiga y validando los JSON, pero solo imprime que
archivo escribiria o que comentario publicaria.
