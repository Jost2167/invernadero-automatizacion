# Codegen de entidades

`tools/codegen` genera artefactos backend, migracion SQL y frontend opcional a partir de una definicion JSON de entidad. El objetivo es acelerar la creacion de CRUDs repetibles manteniendo validacion estructural, reglas semanticas y salidas deterministas.

## Formato JSON

El contrato publicado esta en `src/main/resources/schema/entity-definition.schema.json`.

Ejemplo completo:

```json
{
  "version": "1",
  "name": "Sensor",
  "tableName": "sensors",
  "fields": [
    {
      "name": "name",
      "type": "String",
      "length": 120,
      "nullable": false,
      "unique": true
    },
    {
      "name": "status",
      "type": "Enum",
      "enumValues": ["ACTIVE", "INACTIVE", "MAINTENANCE"],
      "nullable": false
    },
    {
      "name": "lastReadingAt",
      "type": "LocalDateTime",
      "nullable": true
    }
  ],
  "relations": [
    {
      "name": "owner",
      "type": "ManyToOne",
      "target": "User",
      "joinColumn": "owner_id",
      "fetch": "LAZY"
    }
  ],
  "options": {
    "generateController": true,
    "generateFrontend": false,
    "auditable": false
  }
}
```

Campos principales:

- `version`: string fijo `"1"`.
- `name`: nombre PascalCase de la entidad.
- `tableName`: nombre snake_case de la tabla.
- `fields`: minimo un campo. Tipos soportados: `String`, `Integer`, `Long`, `BigDecimal`, `Boolean`, `LocalDate`, `LocalDateTime`, `UUID`, `Enum`.
- `relations`: relaciones JPA opcionales: `OneToMany`, `ManyToOne`, `ManyToMany`, `OneToOne`.
- `options`: banderas de generacion.

## Comandos

Desde la raiz del repo:

```powershell
.\mvnw.cmd -pl tools/codegen package
java -jar tools/codegen/target/codegen.jar --help
```

Validar un JSON:

```powershell
java -jar tools/codegen/target/codegen.jar validate tools/codegen/examples/Sensor.json
```

Generar archivos:

```powershell
java -jar tools/codegen/target/codegen.jar generate tools/codegen/examples/Sensor.json
```

Flags de `generate`:

- `--dry-run`: lista archivos que se crearian o que colisionarian, sin escribir.
- `--overwrite`: permite sobrescribir archivos existentes.
- `--yes`: confirma `--overwrite` en modo no interactivo.
- `--output-backend <dir>`: cambia el directorio backend de salida. Default: `backend`.
- `--output-frontend <dir>`: cambia el directorio frontend de salida. Default: `frontend`.

Ejemplos:

```powershell
java -jar tools/codegen/target/codegen.jar generate tools/codegen/examples/Sensor.json --dry-run
java -jar tools/codegen/target/codegen.jar generate tools/codegen/examples/Sensor.json --overwrite --yes
```

## Troubleshooting

### Falta `// codegen:routes`

Si `options.generateFrontend=true`, `App.jsx` debe tener el marcador:

```jsx
// codegen:routes
```

Agregalo dentro de `<Routes>`, en el lugar donde quieras insertar las rutas generadas. Luego reintenta `generate`.

### Faltan archivos i18n

Si `frontend/src/i18n/es.json` o `frontend/src/i18n/en.json` no existen, crealos como JSON vacio:

```json
{}
```

Luego reintenta `generate`.

### Falta `// codegen:nav`

Si `options.generateFrontend=true`, `frontend/src/components/Sidebar.jsx` debe tener el marcador:

```jsx
// codegen:nav
```

Colócalo como última línea del array `NAV_MODULES`, antes del cierre `]`:

```jsx
export const NAV_MODULES = [
  { key: 'dashboard', path: '/', exact: true },
  // codegen:nav
]
```

Si el marcador está ausente, el generador lanzará un error y se detendrá. Para restaurarlo manualmente, agrega el comentario en la posición correcta y reintenta `generate`.

Para restaurar entradas de entidades ya generadas sin re-ejecutar el generador, agrégalas directamente al array:

```jsx
export const NAV_MODULES = [
  { key: 'dashboard', path: '/', exact: true },
  { key: 'location', path: '/location' },
  { key: 'sensor', path: '/sensor' },
  // codegen:nav
]
```

### Aplicar migracion con psql

El generador crea una migracion en:

```text
backend/src/main/resources/db/migration/V<timestamp>__create_<table_name>.sql
```

Para aplicarla manualmente con `psql`:

```powershell
psql "postgresql://usuario:password@host:5432/base_datos" -f backend/src/main/resources/db/migration/V20260506131415123__create_sensors.sql
```

Reemplaza el nombre del archivo por el generado en tu ejecucion.
