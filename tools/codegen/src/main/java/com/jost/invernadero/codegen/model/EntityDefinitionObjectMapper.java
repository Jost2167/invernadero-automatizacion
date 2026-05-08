package com.jost.invernadero.codegen.model;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class EntityDefinitionObjectMapper {

    private EntityDefinitionObjectMapper() {
    }

    public static ObjectMapper create() {
        return new ObjectMapper()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
}
