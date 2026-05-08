package com.jost.invernadero.codegen.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.jost.invernadero.codegen.model.EntityDefinition;
import com.jost.invernadero.codegen.model.EntityDefinitionObjectMapper;
import com.jost.invernadero.codegen.validator.EntityDefinitionValidator;
import com.jost.invernadero.codegen.validator.JsonSchemaValidator;
import com.jost.invernadero.codegen.validator.ValidationReport;
import com.networknt.schema.ValidationMessage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class CliSupport {

    private CliSupport() {
    }

    static ValidationOutcome validate(Path input) throws IOException {
        JsonNode node = readJson(input);
        List<ValidationMessage> schemaMessages = new JsonSchemaValidator().validate(node);
        if (!schemaMessages.isEmpty()) {
            return new ValidationOutcome(null, ValidationReport.fromSchemaMessages(schemaMessages));
        }

        EntityDefinition definition = EntityDefinitionObjectMapper.create().treeToValue(node, EntityDefinition.class);
        ValidationReport report = new EntityDefinitionValidator().validate(definition);
        return new ValidationOutcome(definition, report);
    }

    private static JsonNode readJson(Path input) throws IOException {
        if (Files.exists(input)) {
            return EntityDefinitionObjectMapper.create().readTree(input.toFile());
        }

        String resourcePath = input.toString().replace('\\', '/');
        try (InputStream resource = CliSupport.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (resource == null) {
                throw new IOException("no se puede leer el archivo: " + input);
            }
            return EntityDefinitionObjectMapper.create().readTree(resource);
        }
    }

    record ValidationOutcome(EntityDefinition definition, ValidationReport report) {
    }
}
