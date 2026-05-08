package com.jost.invernadero.codegen.generator;

import com.jost.invernadero.codegen.model.FieldDef;
import java.math.BigDecimal;
import java.util.Locale;

public class SqlTypeMapper {

    public String typeSql(FieldDef field) {
        return switch (field.type()) {
            case "String" -> "VARCHAR(" + (field.length() == null ? 255 : field.length()) + ")";
            case "Integer" -> "INTEGER";
            case "Long" -> "BIGINT";
            case "BigDecimal" -> bigDecimalType(field);
            case "Boolean" -> "BOOLEAN";
            case "LocalDate" -> "DATE";
            case "LocalDateTime" -> "TIMESTAMP";
            case "UUID" -> "UUID";
            case "Enum" -> "VARCHAR(50)";
            default -> throw new IllegalArgumentException("Unsupported field type: " + field.type());
        };
    }

    public String constraints(FieldDef field) {
        StringBuilder constraints = new StringBuilder();
        if (!field.nullable()) {
            constraints.append(" NOT NULL");
        }
        if (field.unique()) {
            constraints.append(" UNIQUE");
        }
        String defaultSql = defaultSql(field);
        if (defaultSql != null) {
            constraints.append(" DEFAULT ").append(defaultSql);
        }
        return constraints.toString();
    }

    private String bigDecimalType(FieldDef field) {
        if (field.precision() != null && field.scale() != null) {
            return "NUMERIC(" + field.precision() + "," + field.scale() + ")";
        }
        if (field.precision() != null) {
            return "NUMERIC(" + field.precision() + ")";
        }
        return "NUMERIC";
    }

    private String defaultSql(FieldDef field) {
        Object value = field.defaultValue();
        if (value == null) {
            return null;
        }
        return switch (field.type()) {
            case "String", "Enum", "LocalDate", "LocalDateTime", "UUID" -> quote(value.toString());
            case "Boolean" -> Boolean.parseBoolean(value.toString()) ? "TRUE" : "FALSE";
            case "Integer", "Long" -> value.toString();
            case "BigDecimal" -> new BigDecimal(value.toString()).toPlainString();
            default -> throw new IllegalArgumentException("Unsupported field type: " + field.type());
        };
    }

    private String quote(String value) {
        return "'" + value.replace("'", "''").toUpperCase(Locale.ROOT) + "'";
    }
}
