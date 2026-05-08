package com.jost.invernadero.codegen.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.List;

public class JsonSchemaValidator {

    private static final String SCHEMA_PATH = "/schema/entity-definition.schema.json";

    private final ObjectMapper objectMapper;
    private final JsonSchema schema;

    public JsonSchemaValidator() {
        this(new ObjectMapper());
    }

    public JsonSchemaValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.schema = loadSchema();
    }

    public List<ValidationMessage> validate(InputStream input) {
        try {
            return validate(objectMapper.readTree(input));
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not read entity definition JSON", ex);
        }
    }

    public List<ValidationMessage> validate(JsonNode json) {
        return schema.validate(json).stream()
                .sorted(Comparator
                        .comparing(ValidationMessage::getPath)
                        .thenComparing(ValidationMessage::getMessage))
                .toList();
    }

    private JsonSchema loadSchema() {
        InputStream schemaInput = JsonSchemaValidator.class.getResourceAsStream(SCHEMA_PATH);
        if (schemaInput == null) {
            throw new IllegalStateException("Schema not found on classpath: " + SCHEMA_PATH);
        }
        try (schemaInput) {
            JsonNode schemaNode = objectMapper.readTree(schemaInput);
            if (schemaNode instanceof ObjectNode objectNode) {
                objectNode.remove("$schema");
            }
            return JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909).getSchema(schemaNode);
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not load schema " + SCHEMA_PATH, ex);
        }
    }
}
