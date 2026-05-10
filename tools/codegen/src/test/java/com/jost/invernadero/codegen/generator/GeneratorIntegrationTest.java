package com.jost.invernadero.codegen.generator;

import static org.assertj.core.api.Assertions.assertThat;

import com.jost.invernadero.codegen.model.EntityDefinition;
import com.jost.invernadero.codegen.model.EntityDefinitionObjectMapper;
import com.jost.invernadero.codegen.model.Options;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GeneratorIntegrationTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-05-06T13:14:15.123Z"), ZoneOffset.UTC);

    @TempDir
    Path tempDir;

    @Test
    void generatesSensorFilesAndSnapshots() throws IOException {
        EntityDefinition sensor = readFixture("fixtures/valid/simple.json");
        Generator generator = new Generator(tempDir, FIXED_CLOCK);

        GenerationResult result = generator.generate(sensor, sensor.options(), Mode.write());

        assertThat(result.success()).isTrue();
        assertThat(generatedFiles(tempDir)).containsExactly(
                "backend/src/main/java/com/jost/invernadero/automatizacion/controller/SensorController.java",
                "backend/src/main/java/com/jost/invernadero/automatizacion/dto/SensorDto.java",
                "backend/src/main/java/com/jost/invernadero/automatizacion/entity/Sensor.java",
                "backend/src/main/java/com/jost/invernadero/automatizacion/repository/SensorRepository.java",
                "backend/src/main/java/com/jost/invernadero/automatizacion/service/SensorService.java",
                "backend/src/main/java/com/jost/invernadero/automatizacion/service/SensorServiceImpl.java",
                "backend/src/main/resources/db/migration/V20260506131415123__create_sensors.sql");

        assertThat(readGenerated("backend/src/main/java/com/jost/invernadero/automatizacion/entity/Sensor.java"))
                .contains("package com.jost.invernadero.automatizacion.entity;")
                .contains("@Table(name = \"sensors\")")
                .contains("private Long id;")
                .contains("@Column(nullable = false, unique = true, length = 120)")
                .contains("private String name;")
                .contains("@Column(nullable = false)")
                .contains("private Boolean active;");
        assertThat(readGenerated("backend/src/main/java/com/jost/invernadero/automatizacion/dto/SensorDto.java"))
                .contains("public record SensorDto(")
                .contains("Long id")
                .contains("String name")
                .contains("Boolean active");
        assertThat(readGenerated("backend/src/main/resources/db/migration/V20260506131415123__create_sensors.sql"))
                .isEqualTo("""
                        CREATE TABLE sensors (
                            id BIGINT PRIMARY KEY,
                            name VARCHAR(120) NOT NULL UNIQUE,
                            active BOOLEAN NOT NULL DEFAULT TRUE
                        );
                        """.stripIndent());
    }

    @Test
    void dryRunDoesNotWriteAndDefaultWriteReportsCollisions() throws IOException {
        EntityDefinition sensor = readFixture("fixtures/valid/simple.json");
        Generator generator = new Generator(tempDir, FIXED_CLOCK);

        GenerationResult dryRun = generator.generate(sensor, sensor.options(), Mode.dryRun());

        assertThat(dryRun.success()).isTrue();
        assertThat(generatedFiles(tempDir)).isEmpty();
        assertThat(dryRun.messages()).allMatch(message -> message.startsWith("[CREATE]"));

        assertThat(generator.generate(sensor, sensor.options(), Mode.write()).success()).isTrue();
        GenerationResult collision = generator.generate(sensor, sensor.options(), Mode.write());

        assertThat(collision.success()).isFalse();
        assertThat(collision.messages()).anyMatch(message -> message.contains("archivo ya existe"));
    }

    @Test
    void overwriteInNonInteractiveModeRequiresYes() throws IOException {
        EntityDefinition sensor = readFixture("fixtures/valid/simple.json");
        Generator generator = new Generator(tempDir, FIXED_CLOCK);

        assertThat(generator.generate(sensor, sensor.options(), Mode.write()).success()).isTrue();
        GenerationResult result = generator.generate(sensor, sensor.options(), Mode.overwrite(false));

        assertThat(result.success()).isFalse();
        assertThat(result.messages()).containsExactly("modo no interactivo: anade --yes para confirmar overwrite");

        GenerationResult confirmed = generator.generate(sensor, sensor.options(), Mode.overwrite(true));
        assertThat(confirmed.success()).isTrue();
    }

    @Test
    void generatedSensorBackendCompilesInTemporaryWorkspace() throws Exception {
        Path workspace = tempDir.resolve("workspace");
        prepareBackendWorkspace(workspace);

        EntityDefinition sensor = readFixture("fixtures/valid/simple.json");
        GenerationResult result = new Generator(workspace, FIXED_CLOCK)
                .generate(sensor, sensor.options(), Mode.overwrite(true));
        assertThat(result.success()).isTrue();

        Process process = new ProcessBuilder(mavenCompileCommand())
                .directory(workspace.toFile())
                .redirectErrorStream(true)
                .start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();

        assertThat(exitCode)
                .as(output)
                .isEqualTo(0);
    }

    @Test
    void generateIncludesSidebarWhenItExists() throws IOException {
        Path sidebarPath = tempDir.resolve("frontend/src/components/Sidebar.jsx");
        Files.createDirectories(sidebarPath.getParent());
        Files.writeString(sidebarPath, """
                export const NAV_MODULES = [
                  { key: 'dashboard', path: '/', exact: true },
                  // codegen:nav
                ]
                """.stripIndent(), StandardCharsets.UTF_8);

        EntityDefinition sensor = readFixture("fixtures/valid/simple.json");
        Options withFrontend = new Options(true, true, false);
        GenerationResult result = new Generator(tempDir, FIXED_CLOCK).generate(sensor, withFrontend, Mode.write());

        // 5.1: Sidebar.jsx aparece en los archivos escritos
        assertThat(result.success()).isTrue();
        assertThat(result.messages()).anyMatch(msg -> msg.contains("Sidebar.jsx"));

        // 5.2: el contenido refleja la entrada de la entidad Sensor
        String content = Files.readString(sidebarPath, StandardCharsets.UTF_8);
        assertThat(content)
                .contains("{ key: 'sensor', path: '/sensor' }")
                .contains("// codegen:nav");
    }

    @Test
    void overwriteRefreshesGeneratedI18nEntityLabelsWithoutRemovingOtherSections() throws IOException {
        Path esPath = tempDir.resolve("frontend/src/i18n/es.json");
        Files.createDirectories(esPath.getParent());
        Files.writeString(esPath, """
                {
                  "app": {
                    "title": "Invernadero"
                  },
                  "location": {
                    "list": {
                      "title": "Viejo"
                    },
                    "form": {
                      "fields": {
                        "name": "Viejo nombre"
                      }
                    }
                  }
                }
                """.stripIndent(), StandardCharsets.UTF_8);
        EntityDefinition location = readDefinition("""
                {
                  "version": "1",
                  "name": "Location",
                  "tableName": "locations",
                  "fields": [
                    { "name": "name", "type": "String" }
                  ],
                  "relations": [],
                  "options": {
                    "generateController": true,
                    "generateFrontend": true,
                    "auditable": false
                  },
                  "i18n": {
                    "es": {
                      "singular": "Ubicación",
                      "plural": "Ubicaciones",
                      "fields": {
                        "name": "Nombre"
                      },
                      "relations": {}
                    }
                  }
                }
                """);

        GenerationResult result = new Generator(tempDir, FIXED_CLOCK)
                .generate(location, location.options(), Mode.overwrite(true));

        assertThat(result.success()).isTrue();
        String content = Files.readString(esPath, StandardCharsets.UTF_8);
        assertThat(content)
                .contains("\"title\" : \"Invernadero\"")
                .contains("\"title\" : \"Ubicaciones\"")
                .contains("\"name\" : \"Nombre\"");
    }

    private void prepareBackendWorkspace(Path workspace) throws IOException {
        Path repo = repoRoot();
        Files.createDirectories(workspace);
        copyFile(repo.resolve("pom.xml"), workspace.resolve("pom.xml"));
        copyFile(repo.resolve("mvnw"), workspace.resolve("mvnw"));
        copyFile(repo.resolve("mvnw.cmd"), workspace.resolve("mvnw.cmd"));
        copyDirectory(repo.resolve(".mvn"), workspace.resolve(".mvn"));
        copyFile(repo.resolve("backend/pom.xml"), workspace.resolve("backend/pom.xml"));
        copyDirectory(repo.resolve("backend/src/main"), workspace.resolve("backend/src/main"));
        copyFile(repo.resolve("tools/codegen/pom.xml"), workspace.resolve("tools/codegen/pom.xml"));
    }

    private List<String> mavenCompileCommand() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return List.of("cmd.exe", "/c", ".\\mvnw.cmd", "compile");
        }
        return List.of("./mvnw", "compile");
    }

    private Path repoRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null && !Files.exists(current.resolve("backend/pom.xml"))) {
            current = current.getParent();
        }
        assertThat(current).as("repo root").isNotNull();
        return current;
    }

    private List<String> generatedFiles(Path root) throws IOException {
        if (!Files.exists(root)) {
            return List.of();
        }
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(Files::isRegularFile)
                    .map(root::relativize)
                    .map(path -> path.toString().replace('\\', '/'))
                    .filter(path -> !path.startsWith("workspace/"))
                    .sorted()
                    .toList();
        }
    }

    private String readGenerated(String relativePath) throws IOException {
        return Files.readString(tempDir.resolve(relativePath), StandardCharsets.UTF_8);
    }

    private void copyFile(Path source, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        Files.copy(source, target);
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        try (Stream<Path> paths = Files.walk(source)) {
            for (Path path : paths.sorted(Comparator.naturalOrder()).toList()) {
                Path destination = target.resolve(source.relativize(path));
                if (Files.isDirectory(path)) {
                    Files.createDirectories(destination);
                } else {
                    copyFile(path, destination);
                }
            }
        }
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
}
