package com.jost.invernadero.codegen.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record LocaleLabels(
        String singular,
        String plural,
        Map<String, String> fields,
        Map<String, String> relations) {

    @JsonCreator
    public LocaleLabels(
            @JsonProperty("singular") String singular,
            @JsonProperty("plural") String plural,
            @JsonProperty("fields") Map<String, String> fields,
            @JsonProperty("relations") Map<String, String> relations) {
        this.singular = singular;
        this.plural = plural;
        this.fields = fields == null ? Map.of() : Map.copyOf(fields);
        this.relations = relations == null ? Map.of() : Map.copyOf(relations);
    }

    public static LocaleLabels empty() {
        return new LocaleLabels(null, null, Map.of(), Map.of());
    }
}
