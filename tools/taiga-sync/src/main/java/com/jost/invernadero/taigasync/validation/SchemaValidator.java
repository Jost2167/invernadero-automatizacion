package com.jost.invernadero.taigasync.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jost.invernadero.codegen.model.EntityDefinition;
import com.jost.invernadero.codegen.model.EntityDefinitionObjectMapper;
import com.jost.invernadero.codegen.validator.EntityDefinitionValidator;
import com.jost.invernadero.codegen.validator.JsonSchemaValidator;
import com.jost.invernadero.codegen.validator.ValidationReport;
import com.networknt.schema.ValidationMessage;
import java.util.List;

public final class SchemaValidator {

    private final ObjectMapper objectMapper;
    private final JsonSchemaValidator jsonSchemaValidator;
    private final EntityDefinitionValidator entityDefinitionValidator;

    public SchemaValidator() {
        this(EntityDefinitionObjectMapper.create(), new JsonSchemaValidator(), new EntityDefinitionValidator());
    }

    public SchemaValidator(
            ObjectMapper objectMapper,
            JsonSchemaValidator jsonSchemaValidator,
            EntityDefinitionValidator entityDefinitionValidator) {
        this.objectMapper = objectMapper;
        this.jsonSchemaValidator = jsonSchemaValidator;
        this.entityDefinitionValidator = entityDefinitionValidator;
    }

    public ValidationReport validate(String json) {
        JsonNode node;
        try {
            node = objectMapper.readTree(json);
        } catch (JsonProcessingException exception) {
            return invalidJsonReport(exception);
        }

        List<ValidationMessage> schemaMessages = jsonSchemaValidator.validate(node);
        if (!schemaMessages.isEmpty()) {
            return ValidationReport.fromSchemaMessages(schemaMessages);
        }

        try {
            EntityDefinition definition = objectMapper.treeToValue(node, EntityDefinition.class);
            return entityDefinitionValidator.validate(definition);
        } catch (JsonProcessingException exception) {
            return mappingReport(exception);
        }
    }

    private static ValidationReport invalidJsonReport(JsonProcessingException exception) {
        return errorReport("codegen_json must contain valid JSON: " + exception.getOriginalMessage());
    }

    private static ValidationReport mappingReport(JsonProcessingException exception) {
        return errorReport("codegen_json could not be converted to an EntityDefinition: "
                + exception.getOriginalMessage());
    }

    private static ValidationReport errorReport(String message) {
        return new ValidationReport(List.of(new ValidationReport.Error("$", message)), List.of());
    }
}
