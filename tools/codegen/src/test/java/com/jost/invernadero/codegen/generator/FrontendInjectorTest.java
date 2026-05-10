package com.jost.invernadero.codegen.generator;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jost.invernadero.codegen.model.EntityDefinition;
import com.jost.invernadero.codegen.model.EntityDefinitionObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FrontendInjectorTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @TempDir
    Path tempDir;

    @Test
    void routeInjectorAddsImportsAndRoutesBeforeMarker() {
        String app = """
                import { Route, Routes } from 'react-router-dom'
                import HomePage from './pages/HomePage.jsx'

                export default function App() {
                  return (
                    <Routes>
                      <Route path="/" element={<HomePage />} />
                      // codegen:routes
                    </Routes>
                  )
                }
                """.stripIndent();

        InjectionResult result = new RouteInjector().inject(app, "Sensor", "sensor");

        assertThat(result.success()).isTrue();
        assertThat(result.content())
                .contains("import SensorListPage from './pages/sensor/SensorListPage.jsx'")
                .contains("import SensorFormPage from './pages/sensor/SensorFormPage.jsx'")
                .contains("<Route path=\"/sensor\" element={<SensorListPage />} />")
                .contains("<Route path=\"/sensor/:id\" element={<SensorFormPage />} />")
                .contains("// codegen:routes");
    }

    @Test
    void routeInjectorReturnsSpecificErrorWhenMarkerIsMissing() {
        InjectionResult result = new RouteInjector().inject("<Routes />", "Sensor", "sensor");

        assertThat(result.success()).isFalse();
        assertThat(result.error()).contains("App.jsx no contiene el marcador `// codegen:routes`");
    }

    @Test
    void i18nInjectorCreatesSectionWhenMissing() throws IOException {
        Path i18n = tempDir.resolve("es.json");
        Files.writeString(i18n, "{}" + System.lineSeparator(), StandardCharsets.UTF_8);

        InjectionResult result = new I18nInjector().inject(i18n, readFixture("fixtures/valid/simple.json"));

        assertThat(result.success()).isTrue();
        assertThat(result.content())
                .contains("\"sensor\"")
                .contains("\"list\"")
                .contains("\"fields\"")
                .contains("\"name\" : \"Name\"");
    }

    @Test
    void i18nInjectorMergesExistingSectionWithoutOverwriting() throws IOException {
        Path i18n = tempDir.resolve("es.json");
        Files.writeString(i18n, """
                {
                  "sensor": {
                    "list": {
                      "title": "Sensores"
                    }
                  }
                }
                """.stripIndent(), StandardCharsets.UTF_8);

        InjectionResult result = new I18nInjector().inject(i18n, readFixture("fixtures/valid/simple.json"));

        assertThat(result.success()).isTrue();
        assertThat(result.content())
                .contains("\"title\" : \"Sensores\"")
                .contains("\"create\" : \"Crear\"")
                .contains("\"form\"")
                .contains("\"active\" : \"Active\"");
    }

    @Test
    void i18nInjectorOverwritesExistingEntityLabelsWhenRequested() throws IOException {
        Path i18n = tempDir.resolve("es.json");
        Files.writeString(i18n, """
                {
                  "app": {
                    "title": "Invernadero"
                  },
                  "measurement": {
                    "list": {
                      "title": "Viejo"
                    }
                  }
                }
                """.stripIndent(), StandardCharsets.UTF_8);

        InjectionResult result = new I18nInjector().inject(i18n, localizedMeasurementDefinition(), true);

        assertThat(result.success()).isTrue();
        assertThat(valueAt(result.content(), "/app/title")).isEqualTo("Invernadero");
        assertThat(valueAt(result.content(), "/measurement/list/title")).isEqualTo("Mediciones");
    }

    @Test
    void i18nInjectorUsesCustomFieldLabelForEsLocale() throws IOException {
        String content = injectContent("es.json", localizedMeasurementDefinition());

        assertThat(valueAt(content, "/measurement/list/fields/recordedAt"))
                .isEqualTo("Fecha de registro");
        assertThat(valueAt(content, "/measurement/form/fields/recordedAt"))
                .isEqualTo("Fecha de registro");
    }

    @Test
    void i18nInjectorUsesGeneratedLabelForFieldWithoutCustomLabel() throws IOException {
        String content = injectContent("es.json", localizedMeasurementDefinition());

        assertThat(valueAt(content, "/measurement/list/fields/temperature")).isEqualTo("Temperature");
        assertThat(valueAt(content, "/measurement/form/fields/temperature")).isEqualTo("Temperature");
    }

    @Test
    void i18nInjectorUsesEntitySingularAndPluralLabels() throws IOException {
        String content = injectContent("es.json", localizedMeasurementDefinition());

        assertThat(valueAt(content, "/measurement/list/title")).isEqualTo("Mediciones");
        assertThat(valueAt(content, "/measurement/form/createTitle")).isEqualTo("Crear Medición");
        assertThat(valueAt(content, "/measurement/form/editTitle")).isEqualTo("Editar Medición");
    }

    @Test
    void i18nInjectorAddsSidebarLabelUsingEntityPlural() throws IOException {
        String content = injectContent("es.json", localizedMeasurementDefinition());

        assertThat(valueAt(content, "/sidebar/measurement")).isEqualTo("Mediciones");
    }

    @Test
    void i18nInjectorUsesEnglishUiLabelsForEnFile() throws IOException {
        String content = injectContent("en.json", localizedMeasurementDefinition());

        assertThat(valueAt(content, "/measurement/list/create")).isEqualTo("Create");
        assertThat(valueAt(content, "/measurement/list/delete")).isEqualTo("Delete");
        assertThat(valueAt(content, "/measurement/form/save")).isEqualTo("Save");
        assertThat(valueAt(content, "/measurement/form/createTitle")).isEqualTo("Create Measurement");
        assertThat(valueAt(content, "/sidebar/measurement")).isEqualTo("Measurements");
    }

    @Test
    void i18nInjectorUsesCustomRelationLabelInFieldSection() throws IOException {
        String content = injectContent("es.json", localizedMeasurementDefinition());

        assertThat(valueAt(content, "/measurement/field/sensor")).isEqualTo("Sensor asociado");
    }

    @Test
    void i18nInjectorDetectsLocaleFromFileName() throws IOException {
        EntityDefinition definition = readDefinition("""
                {
                  "version": "1",
                  "name": "Measurement",
                  "tableName": "measurements",
                  "fields": [
                    { "name": "recordedAt", "type": "LocalDateTime" }
                  ],
                  "i18n": {
                    "es": {
                      "singular": "Medición",
                      "plural": "Mediciones",
                      "fields": {
                        "recordedAt": "Fecha de registro"
                      }
                    },
                    "en": {
                      "singular": "Measurement",
                      "plural": "Measurements",
                      "fields": {
                        "recordedAt": "Recorded at"
                      }
                    }
                  }
                }
                """);
        String esContent = injectContent("es.json", definition);
        String enContent = injectContent("en.json", definition);

        assertThat(valueAt(esContent, "/measurement/list/fields/recordedAt")).isEqualTo("Fecha de registro");
        assertThat(valueAt(enContent, "/measurement/list/fields/recordedAt")).isEqualTo("Recorded at");
    }

    @Test
    void i18nInjectorKeepsPreviousDefaultsWithoutI18nBlock() throws IOException {
        String content = injectContent("es.json", readFixture("fixtures/valid/simple.json"));

        assertThat(valueAt(content, "/sensor/list/title")).isEqualTo("Sensor");
        assertThat(valueAt(content, "/sensor/form/createTitle")).isEqualTo("Crear Sensor");
        assertThat(valueAt(content, "/sensor/form/editTitle")).isEqualTo("Editar Sensor");
        assertThat(valueAt(content, "/sensor/list/fields/id")).isEqualTo("ID");
        assertThat(valueAt(content, "/sensor/list/fields/name")).isEqualTo("Name");
        assertThat(valueAt(content, "/sensor/form/fields/active")).isEqualTo("Active");
        assertThat(JSON.readTree(content).at("/sensor/form/fields/id").isMissingNode()).isTrue();
    }

    @Test
    void i18nInjectorUsesFallbackForPartialI18nBlock() throws IOException {
        String content = injectContent("es.json", localizedMeasurementDefinition());

        assertThat(valueAt(content, "/measurement/list/fields/temperature")).isEqualTo("Temperature");
        assertThat(valueAt(content, "/measurement/form/fields/temperature")).isEqualTo("Temperature");
    }

    @Test
    void i18nInjectorFailsWhenFileDoesNotExist() throws IOException {
        Path missing = tempDir.resolve("missing.json");

        InjectionResult result = new I18nInjector().inject(missing, readFixture("fixtures/valid/simple.json"));

        assertThat(result.success()).isFalse();
        assertThat(result.error()).contains("No existe el archivo i18n");
    }

    private EntityDefinition readFixture(String resourcePath) throws IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            assertThat(input)
                    .as("fixture %s exists", resourcePath)
                    .isNotNull();
            return EntityDefinitionObjectMapper.create().readValue(input, EntityDefinition.class);
        }
    }

    private EntityDefinition readDefinition(String content) throws IOException {
        return EntityDefinitionObjectMapper.create().readValue(content, EntityDefinition.class);
    }

    private String injectContent(String fileName, EntityDefinition definition) throws IOException {
        Path i18n = tempDir.resolve(fileName);
        Files.writeString(i18n, "{}" + System.lineSeparator(), StandardCharsets.UTF_8);

        InjectionResult result = new I18nInjector().inject(i18n, definition);

        assertThat(result.success()).isTrue();
        return result.content();
    }

    private String valueAt(String content, String pointer) throws IOException {
        JsonNode value = JSON.readTree(content).at(pointer);
        assertThat(value.isMissingNode())
                .as("JSON pointer %s exists", pointer)
                .isFalse();
        return value.asText();
    }

    private EntityDefinition localizedMeasurementDefinition() throws IOException {
        return readDefinition("""
                {
                  "version": "1",
                  "name": "Measurement",
                  "tableName": "measurements",
                  "fields": [
                    { "name": "recordedAt", "type": "LocalDateTime" },
                    { "name": "temperature", "type": "BigDecimal" }
                  ],
                  "relations": [
                    {
                      "name": "sensor",
                      "type": "ManyToOne",
                      "target": "Sensor",
                      "joinColumn": "sensor_id"
                    }
                  ],
                  "i18n": {
                    "es": {
                      "singular": "Medición",
                      "plural": "Mediciones",
                      "fields": {
                        "recordedAt": "Fecha de registro"
                      },
                      "relations": {
                        "sensor": "Sensor asociado"
                      }
                    },
                    "en": {
                      "singular": "Measurement",
                      "plural": "Measurements",
                      "fields": {
                        "recordedAt": "Recorded at"
                      },
                      "relations": {
                        "sensor": "Sensor"
                      }
                    }
                  }
                }
                """);
    }
}
