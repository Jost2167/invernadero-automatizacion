package com.jost.invernadero.codegen.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record FieldDef(
        String name,
        String type,
        Boolean nullable,
        Boolean unique,
        Integer length,
        Integer precision,
        Integer scale,
        Object defaultValue,
        List<String> enumValues) {

    @JsonCreator
    public FieldDef(
            @JsonProperty(value = "name", required = true) String name,
            @JsonProperty(value = "type", required = true) String type,
            @JsonProperty("nullable") Boolean nullable,
            @JsonProperty("unique") Boolean unique,
            @JsonProperty("length") Integer length,
            @JsonProperty("precision") Integer precision,
            @JsonProperty("scale") Integer scale,
            @JsonProperty("defaultValue") Object defaultValue,
            @JsonProperty("enumValues") List<String> enumValues) {
        this.name = name;
        this.type = type;
        this.nullable = nullable == null ? Boolean.TRUE : nullable;
        this.unique = unique == null ? Boolean.FALSE : unique;
        this.length = length;
        this.precision = precision;
        this.scale = scale;
        this.defaultValue = defaultValue;
        this.enumValues = enumValues == null ? List.of() : List.copyOf(enumValues);
    }
}
