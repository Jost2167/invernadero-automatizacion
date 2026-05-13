package com.jost.invernadero.tests.config;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class TestDataLoader {

    public static final Path DEFAULT_TEST_DATA_PATH = Path.of("test-data.json");
    public static final int DEFAULT_TIMEOUT_SECONDS = 10;
    public static final String TEST_TIMEOUT_SECONDS_ENV = "TEST_TIMEOUT_SECONDS";

    private final ObjectMapper objectMapper;
    private final Function<String, String> environment;
    private final PrintStream output;

    public TestDataLoader() {
        this(new ObjectMapper(), System::getenv, System.out);
    }

    TestDataLoader(ObjectMapper objectMapper, Function<String, String> environment, PrintStream output) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.environment = Objects.requireNonNull(environment, "environment");
        this.output = Objects.requireNonNull(output, "output");
    }

    public TestSuiteConfig load() {
        return load(DEFAULT_TEST_DATA_PATH);
    }

    public TestSuiteConfig load(Path testDataPath) {
        Path normalizedPath = testDataPath.toAbsolutePath().normalize();

        if (!Files.exists(normalizedPath)) {
            throw new IllegalStateException("No se encontro test-data.json en: " + normalizedPath);
        }

        TestSuiteConfig config = readConfig(normalizedPath);
        config.setModels(validModels(config.getModels()));
        config.setTimeout(resolveTimeout(config.getTimeout()));
        return config;
    }

    private TestSuiteConfig readConfig(Path testDataPath) {
        try {
            TestSuiteConfig config = objectMapper.readValue(testDataPath.toFile(), TestSuiteConfig.class);
            return config == null ? new TestSuiteConfig() : config;
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("JSON invalido en " + testDataPath + formatLocation(exception)
                    + ": " + exception.getOriginalMessage(), exception);
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo leer test-data.json en " + testDataPath + ": "
                    + exception.getMessage(), exception);
        }
    }

    private List<ModelTestData> validModels(List<ModelTestData> models) {
        List<ModelTestData> validModels = new ArrayList<>();

        for (int index = 0; index < models.size(); index++) {
            ModelTestData model = models.get(index);
            List<String> missingFields = missingFields(model);

            if (missingFields.isEmpty()) {
                validModels.add(model);
                continue;
            }

            output.println("Modelo invalido omitido (" + modelLabel(model, index) + "): falta "
                    + String.join(", ", missingFields));
        }

        return validModels;
    }

    private List<String> missingFields(ModelTestData model) {
        List<String> missingFields = new ArrayList<>();

        if (model == null) {
            missingFields.add("name");
            missingFields.add("formUrl");
            missingFields.add("fields");
            missingFields.add("successIndicator");
            return missingFields;
        }

        if (isBlank(model.getName())) {
            missingFields.add("name");
        }

        if (isBlank(model.getFormUrl())) {
            missingFields.add("formUrl");
        }

        if (model.getFields().isEmpty()) {
            missingFields.add("fields");
        }

        if (isBlank(model.getSuccessIndicator())) {
            missingFields.add("successIndicator");
        }

        return missingFields;
    }

    private String modelLabel(ModelTestData model, int index) {
        if (model != null && !isBlank(model.getName())) {
            return model.getName();
        }

        return "indice " + index;
    }

    private Integer resolveTimeout(Integer jsonTimeout) {
        String environmentTimeout = environment.apply(TEST_TIMEOUT_SECONDS_ENV);

        if (!isBlank(environmentTimeout)) {
            return parsePositiveTimeout(environmentTimeout, TEST_TIMEOUT_SECONDS_ENV);
        }

        if (jsonTimeout != null) {
            return parsePositiveTimeout(String.valueOf(jsonTimeout), "timeout");
        }

        return DEFAULT_TIMEOUT_SECONDS;
    }

    private Integer parsePositiveTimeout(String timeoutValue, String source) {
        try {
            int timeout = Integer.parseInt(timeoutValue.trim());
            if (timeout <= 0) {
                throw new IllegalStateException(source + " debe ser mayor que cero: " + timeoutValue);
            }
            return timeout;
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(source + " debe ser un numero entero valido: " + timeoutValue, exception);
        }
    }

    private String formatLocation(JsonProcessingException exception) {
        JsonLocation location = exception.getLocation();

        if (location == null) {
            return "";
        }

        return " (linea " + location.getLineNr() + ", columna " + location.getColumnNr() + ")";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
