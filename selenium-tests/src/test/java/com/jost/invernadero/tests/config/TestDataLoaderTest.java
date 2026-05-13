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
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestDataLoaderTest {

    @TempDir
    private Path tempDir;

    @Test
    void loadsValidJsonAndUsesDefaultTimeout() throws IOException {
        Path testData = writeTestData("""
                {
                  "models": [
                    {
                      "name": "Sensor",
                      "formUrl": "/sensors/new",
                      "fields": [
                        { "selector": "#name", "value": "Sensor QA" }
                      ],
                      "successIndicator": ".alert-success"
                    }
                  ]
                }
                """);

        TestSuiteConfig config = newLoader(name -> null).load(testData);

        assertAll(
                () -> assertEquals(1, config.getModels().size()),
                () -> assertEquals("Sensor", config.getModels().get(0).getName()),
                () -> assertEquals(TestDataLoader.DEFAULT_TIMEOUT_SECONDS, config.getTimeout())
        );
    }

    @Test
    void omitsInvalidModelsAndPrintsMissingFields() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path testData = writeTestData("""
                {
                  "models": [
                    {
                      "name": "Sensor",
                      "formUrl": "/sensors/new",
                      "fields": [
                        { "selector": "#name", "value": "Sensor QA" }
                      ],
                      "successIndicator": ".alert-success"
                    },
                    {
                      "name": "Missing Url",
                      "fields": [],
                      "successIndicator": " "
                    },
                    null,
                    {
                      "name": "No success",
                      "formUrl": "/x",
                      "fields": [
                        { "selector": "#x", "value": "y" }
                      ]
                    }
                  ]
                }
                """);

        TestSuiteConfig config = newLoader(name -> null, output).load(testData);
        String log = output.toString(StandardCharsets.UTF_8);

        assertAll(
                () -> assertEquals(1, config.getModels().size()),
                () -> assertTrue(log.contains("Modelo invalido omitido (Missing Url): falta formUrl, fields, successIndicator")),
                () -> assertTrue(log.contains("Modelo invalido omitido (indice 2): falta name, formUrl, fields, successIndicator")),
                () -> assertTrue(log.contains("Modelo invalido omitido (No success): falta successIndicator"))
        );
    }

    @Test
    void environmentTimeoutOverridesJsonTimeout() throws IOException {
        Path testData = writeTestData("""
                {
                  "timeout": 30,
                  "models": [
                    {
                      "name": "Sensor",
                      "formUrl": "/sensors/new",
                      "fields": [
                        { "selector": "#name", "value": "Sensor QA" }
                      ],
                      "successIndicator": ".alert-success"
                    }
                  ]
                }
                """);

        TestSuiteConfig config = newLoader(name -> TestDataLoader.TEST_TIMEOUT_SECONDS_ENV.equals(name) ? "20" : null)
                .load(testData);

        assertEquals(20, config.getTimeout());
    }

    @Test
    void usesJsonTimeoutWhenEnvironmentTimeoutIsNotDefined() throws IOException {
        Path testData = writeTestData("""
                {
                  "timeout": 15,
                  "models": []
                }
                """);

        TestSuiteConfig config = newLoader(name -> null).load(testData);

        assertEquals(15, config.getTimeout());
    }

    @Test
    void abortsWithClearMessageWhenFileDoesNotExist() {
        Path missingPath = tempDir.resolve("test-data.json");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> newLoader(name -> null).load(missingPath)
        );

        assertAll(
                () -> assertTrue(exception.getMessage().contains("No se encontro test-data.json en:")),
                () -> assertTrue(exception.getMessage().contains(missingPath.toAbsolutePath().normalize().toString()))
        );
    }

    @Test
    void abortsWithClearMessageWhenJsonSyntaxIsInvalid() throws IOException {
        Path testData = writeTestData("{ \"models\": [ }");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> newLoader(name -> null).load(testData)
        );

        assertAll(
                () -> assertTrue(exception.getMessage().contains("JSON invalido en")),
                () -> assertTrue(exception.getMessage().contains("linea")),
                () -> assertTrue(exception.getMessage().contains("columna"))
        );
    }

    private TestDataLoader newLoader(Function<String, String> environment) {
        return newLoader(environment, new ByteArrayOutputStream());
    }

    private TestDataLoader newLoader(Function<String, String> environment, ByteArrayOutputStream output) {
        return new TestDataLoader(new ObjectMapper(), environment, new PrintStream(output, true, StandardCharsets.UTF_8));
    }

    private Path writeTestData(String content) throws IOException {
        Path testData = tempDir.resolve("test-data.json");
        Files.writeString(testData, content, StandardCharsets.UTF_8);
        return testData;
    }
}
