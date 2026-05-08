package com.jost.invernadero.codegen.generator;

import static org.assertj.core.api.Assertions.assertThat;

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
}
