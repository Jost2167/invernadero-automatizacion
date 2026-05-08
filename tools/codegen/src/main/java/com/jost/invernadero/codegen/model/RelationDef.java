package com.jost.invernadero.codegen.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RelationDef(
        String name,
        String type,
        String target,
        String mappedBy,
        String joinColumn,
        List<String> cascade,
        String fetch) {

    @JsonCreator
    public RelationDef(
            @JsonProperty(value = "name", required = true) String name,
            @JsonProperty(value = "type", required = true) String type,
            @JsonProperty(value = "target", required = true) String target,
            @JsonProperty("mappedBy") String mappedBy,
            @JsonProperty("joinColumn") String joinColumn,
            @JsonProperty("cascade") List<String> cascade,
            @JsonProperty("fetch") String fetch) {
        this.name = name;
        this.type = type;
        this.target = target;
        this.mappedBy = mappedBy;
        this.joinColumn = joinColumn;
        this.cascade = cascade == null ? List.of() : List.copyOf(cascade);
        this.fetch = fetch == null ? "LAZY" : fetch;
    }
}
