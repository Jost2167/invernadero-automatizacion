package com.jost.invernadero.codegen.generator;

import com.jost.invernadero.codegen.model.EntityDefinition;
import com.jost.invernadero.codegen.model.FieldDef;
import com.jost.invernadero.codegen.model.RelationDef;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MigrationModelBuilder {

    private static final DateTimeFormatter FILE_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final Clock clock;
    private final SqlTypeMapper sqlTypeMapper;

    public MigrationModelBuilder(Clock clock) {
        this(clock, new SqlTypeMapper());
    }

    public MigrationModelBuilder(Clock clock, SqlTypeMapper sqlTypeMapper) {
        this.clock = clock;
        this.sqlTypeMapper = sqlTypeMapper;
    }

    public Map<String, Object> build(EntityDefinition definition) {
        Map<String, Object> model = new LinkedHashMap<>();
        List<Map<String, Object>> columns = columns(definition);
        List<Map<String, Object>> foreignKeys = foreignKeys(definition);
        List<Map<String, Object>> checks = enumChecks(definition);

        model.put("tableName", definition.tableName());
        model.put("fileName", fileName(definition.tableName()));
        model.put("columns", columns);
        model.put("foreignKeys", foreignKeys);
        model.put("checks", checks);
        List<String> definitions = definitions(columns, foreignKeys, checks);
        model.put("definitions", definitions);
        model.put("body", body(definitions));
        return model;
    }

    public String fileName(String tableName) {
        String timestamp = LocalDateTime.now(clock).format(FILE_TIMESTAMP);
        return "V" + timestamp + "__create_" + tableName + ".sql";
    }

    private List<Map<String, Object>> columns(EntityDefinition definition) {
        List<Map<String, Object>> columns = new ArrayList<>();
        if (definition.fields().stream().noneMatch(field -> "id".equalsIgnoreCase(field.name()))) {
            columns.add(column("id", "BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY"));
        }
        for (FieldDef field : definition.fields()) {
            if ("id".equalsIgnoreCase(field.name())) {
                columns.add(column(toSnakeCase(field.name()), sqlTypeMapper.typeSql(field) + " PRIMARY KEY"));
            } else {
                columns.add(column(toSnakeCase(field.name()), sqlTypeMapper.typeSql(field) + sqlTypeMapper.constraints(field)));
            }
        }
        for (RelationDef relation : definition.relations()) {
            if (hasText(relation.joinColumn())) {
                columns.add(column(relation.joinColumn(), "BIGINT"));
            }
        }
        return columns;
    }

    private Map<String, Object> column(String name, String definition) {
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("name", name);
        column.put("definition", definition);
        column.put("line", name + " " + definition);
        return column;
    }

    private List<Map<String, Object>> foreignKeys(EntityDefinition definition) {
        List<Map<String, Object>> foreignKeys = new ArrayList<>();
        for (RelationDef relation : definition.relations()) {
            if (hasText(relation.joinColumn())) {
                Map<String, Object> foreignKey = new LinkedHashMap<>();
                foreignKey.put("name", "fk_" + definition.tableName() + "_" + toSnakeCase(relation.name()));
                foreignKey.put("column", relation.joinColumn());
                foreignKey.put("targetTable", toSnakeCasePlural(relation.target()));
                foreignKey.put("line", "CONSTRAINT fk_" + definition.tableName() + "_" + toSnakeCase(relation.name())
                        + " FOREIGN KEY (" + relation.joinColumn() + ") REFERENCES "
                        + toSnakeCasePlural(relation.target()) + "(id)");
                foreignKeys.add(foreignKey);
            }
        }
        return foreignKeys;
    }

    private List<Map<String, Object>> enumChecks(EntityDefinition definition) {
        List<Map<String, Object>> checks = new ArrayList<>();
        for (FieldDef field : definition.fields()) {
            if ("Enum".equals(field.type())) {
                String columnName = toSnakeCase(field.name());
                String values = field.enumValues().stream()
                        .map(value -> "'" + value.replace("'", "''") + "'")
                        .reduce((left, right) -> left + "," + right)
                        .orElse("");
                Map<String, Object> check = new LinkedHashMap<>();
                check.put("name", "chk_" + definition.tableName() + "_" + columnName);
                check.put("column", columnName);
                check.put("line", "CONSTRAINT chk_" + definition.tableName() + "_" + columnName
                        + " CHECK (" + columnName + " IN (" + values + "))");
                checks.add(check);
            }
        }
        return checks;
    }

    private List<String> definitions(
            List<Map<String, Object>> columns,
            List<Map<String, Object>> foreignKeys,
            List<Map<String, Object>> checks) {
        List<String> definitions = new ArrayList<>();
        columns.forEach(column -> definitions.add((String) column.get("line")));
        foreignKeys.forEach(foreignKey -> definitions.add((String) foreignKey.get("line")));
        checks.forEach(check -> definitions.add((String) check.get("line")));
        return definitions;
    }

    private String body(List<String> definitions) {
        return "    " + String.join("," + System.lineSeparator() + "    ", definitions);
    }

    private String toSnakeCasePlural(String value) {
        String snake = toSnakeCase(value);
        return snake.endsWith("s") ? snake : snake + "s";
    }

    private String toSnakeCase(String value) {
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (Character.isUpperCase(current) && index > 0) {
                result.append('_');
            }
            result.append(Character.toLowerCase(current));
        }
        return result.toString().toLowerCase(Locale.ROOT);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
