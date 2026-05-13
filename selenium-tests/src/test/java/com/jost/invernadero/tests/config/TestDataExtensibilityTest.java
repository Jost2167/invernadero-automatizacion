package com.jost.invernadero.tests.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestDataExtensibilityTest {

    @TempDir
    private Path tempDir;

    @Test
    void addingNewModelToJsonIsAvailableWithoutJavaChanges() throws IOException {
        Path testData = tempDir.resolve("test-data.json");
        Files.writeString(testData, """
                {
                  "models": [
                    {
                      "name": "Location",
                      "formUrl": "/location/new",
                      "fields": [{ "selector": "#name", "value": "Location QA" }],
                      "successIndicator": ".alert-success"
                    },
                    {
                      "name": "Greenhouse",
                      "formUrl": "/greenhouse/new",
                      "fields": [{ "selector": "#code", "value": "GH-QA" }],
                      "successIndicator": ".alert-success"
                    },
                    {
                      "name": "New JSON Only Model",
                      "formUrl": "/sensor/new",
                      "fields": [{ "selector": "#name", "value": "Sensor QA" }],
                      "successIndicator": ".alert-success"
                    }
                  ]
                }
                """, StandardCharsets.UTF_8);

        TestSuiteConfig config = new TestDataLoader(
                new ObjectMapper(),
                name -> null,
                new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8)
        ).load(testData);

        assertEquals(3, config.getModels().size());
        assertEquals("New JSON Only Model", config.getModels().get(2).getName());
    }
}
