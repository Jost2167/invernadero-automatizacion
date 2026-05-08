package com.jost.invernadero.codegen.validator;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.jost.invernadero.codegen.model.EntityDefinition;
import com.jost.invernadero.codegen.model.EntityDefinitionObjectMapper;
import com.networknt.schema.ValidationMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;

class EntityDefinitionValidationTest {

    private final JsonSchemaValidator schemaValidator = new JsonSchemaValidator();
    private final EntityDefinitionValidator semanticValidator = new EntityDefinitionValidator();

    @Test
    void validFixturesProduceNoErrors() throws IOException {
        assertNoErrors("fixtures/valid/simple.json");
        assertNoErrors("fixtures/valid/with-relations.json");
        assertNoErrors("fixtures/valid/with-enum.json");
        assertNoErrors("fixtures/valid/with-options.json");
    }

    @Test
    void invalidFixturesProduceExpectedErrorsAtExpectedPaths() throws IOException {
        assertErrorAt("fixtures/invalid/unknown-prop.json", "$");
        assertErrorAt("fixtures/invalid/bad-type.json", "$.fields[0].type");
        assertErrorAt("fixtures/invalid/enum-without-values.json", "$.fields[0]");
        assertErrorAt("fixtures/invalid/duplicate-fields.json", "$.fields[1].name");
        assertErrorAt("fixtures/invalid/missing-joincolumn.json", "$.relations[0]");
        assertErrorAt("fixtures/invalid/bad-pascal-case.json", "$.name");
    }

    @Test
    void stringWithoutLengthProducesOneWarningAndNoErrors() throws IOException {
        String json = """
                {
                  "version": "1",
                  "name": "Sensor",
                  "tableName": "sensors",
                  "fields": [
                    {
                      "name": "id",
                      "type": "Long",
                      "nullable": false
                    },
                    {
                      "name": "name",
                      "type": "String"
                    }
                  ]
                }
                """;

        JsonNode node = EntityDefinitionObjectMapper.create().readTree(json);
        assertThat(schemaValidator.validate(node)).isEmpty();

        EntityDefinition definition = EntityDefinitionObjectMapper.create()
                .readValue(json, EntityDefinition.class);
        ValidationReport report = semanticValidator.validate(definition);

        assertThat(report.errors()).isEmpty();
        assertThat(report.warnings())
                .containsExactly(new ValidationReport.Warning(
                        "$.fields[1].length",
                        "String field should define length"));
    }

    @Test
    void reportFormatsEntriesAndUsesExitCodeOneWhenErrorsExist() {
        ValidationReport report = new ValidationReport(
                List.of(new ValidationReport.Error("$.name", "name must be PascalCase")),
                List.of(new ValidationReport.Warning("$.fields", "No id field found")));

        assertThat(report.exitCode()).isEqualTo(1);
        assertThat(report.format().replace("\r\n", "\n")).isEqualTo("""
                Errors:
                - $.name: name must be PascalCase

                Warnings:
                - $.fields: No id field found""".stripIndent());
    }

    private void assertNoErrors(String resourcePath) throws IOException {
        ValidationReport report = validate(resourcePath);

        assertThat(report.errors())
                .as("%s errors", resourcePath)
                .isEmpty();
    }

    private void assertErrorAt(String resourcePath, String expectedPath) throws IOException {
        ValidationReport report = validate(resourcePath);

        assertThat(report.errors())
                .as("%s errors", resourcePath)
                .isNotEmpty();
        assertThat(report.errors())
                .extracting(ValidationReport.Error::path)
                .contains(expectedPath);
    }

    private ValidationReport validate(String resourcePath) throws IOException {
        JsonNode node = readJson(resourcePath);
        List<ValidationMessage> schemaMessages = schemaValidator.validate(node);
        if (!schemaMessages.isEmpty()) {
            return ValidationReport.fromSchemaMessages(schemaMessages);
        }

        EntityDefinition definition = EntityDefinitionObjectMapper.create()
                .treeToValue(node, EntityDefinition.class);
        return semanticValidator.validate(definition);
    }

    private JsonNode readJson(String resourcePath) throws IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            assertThat(input)
                    .as("fixture %s exists", resourcePath)
                    .isNotNull();
            return EntityDefinitionObjectMapper.create().readTree(input);
        }
    }
}
