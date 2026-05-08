package com.jost.invernadero.codegen.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record Options(
        Boolean generateController,
        Boolean generateFrontend,
        Boolean auditable) {

    @JsonCreator
    public Options(
            @JsonProperty("generateController") Boolean generateController,
            @JsonProperty("generateFrontend") Boolean generateFrontend,
            @JsonProperty("auditable") Boolean auditable) {
        this.generateController = generateController == null ? Boolean.TRUE : generateController;
        this.generateFrontend = generateFrontend == null ? Boolean.FALSE : generateFrontend;
        this.auditable = auditable == null ? Boolean.FALSE : auditable;
    }

    public static Options defaults() {
        return new Options(true, false, false);
    }
}
