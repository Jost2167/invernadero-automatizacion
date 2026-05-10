package com.jost.invernadero.codegen.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record EntityDefinition(
        String version,
        String name,
        String tableName,
        List<FieldDef> fields,
        List<RelationDef> relations,
        I18nBlock i18n,
        Options options) {

    @JsonCreator
    public EntityDefinition(
            @JsonProperty(value = "version", required = true) String version,
            @JsonProperty(value = "name", required = true) String name,
            @JsonProperty(value = "tableName", required = true) String tableName,
            @JsonProperty(value = "fields", required = true) List<FieldDef> fields,
            @JsonProperty("relations") List<RelationDef> relations,
            @JsonProperty("i18n") I18nBlock i18n,
            @JsonProperty("options") Options options) {
        this.version = version;
        this.name = name;
        this.tableName = tableName;
        this.fields = fields == null ? List.of() : List.copyOf(fields);
        this.relations = relations == null ? List.of() : List.copyOf(relations);
        this.i18n = i18n == null ? I18nBlock.empty() : i18n;
        this.options = options == null ? Options.defaults() : options;
    }
}
