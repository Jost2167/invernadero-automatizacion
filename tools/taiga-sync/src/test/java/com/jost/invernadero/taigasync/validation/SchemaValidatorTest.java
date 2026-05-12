package com.jost.invernadero.taigasync.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.jost.invernadero.codegen.validator.ValidationReport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class SchemaValidatorTest {

    private final SchemaValidator validator = new SchemaValidator();

    @Test
    void acceptsValidFixturesFromCodegen() throws IOException {
        try (Stream<Path> fixtures = Files.list(fixtureDirectory("valid"))) {
            for (Path fixture : fixtures.toList()) {
                ValidationReport report = validator.validate(Files.readString(fixture));

                assertThat(report.errors())
                        .as(fixture.getFileName().toString())
                        .isEmpty();
            }
        }
    }

    @Test
    void reportsErrorsForInvalidFixturesFromCodegen() throws IOException {
        try (Stream<Path> fixtures = Files.list(fixtureDirectory("invalid"))) {
            for (Path fixture : fixtures.toList()) {
                ValidationReport report = validator.validate(Files.readString(fixture));

                assertThat(report.errors())
                        .as(fixture.getFileName().toString())
                        .isNotEmpty();
            }
        }
    }

    @Test
    void reportsInvalidJsonWithoutThrowing() {
        ValidationReport report = validator.validate("{not-json");

        assertThat(report.errors()).singleElement()
                .satisfies(error -> {
                    assertThat(error.path()).isEqualTo("$");
                    assertThat(error.message()).contains("codegen_json must contain valid JSON");
                });
    }

    private static Path fixtureDirectory(String kind) {
        Path fromRoot = Path.of("tools", "codegen", "src", "test", "resources", "fixtures", kind);
        if (Files.exists(fromRoot)) {
            return fromRoot;
        }

        Path fromModule = Path.of("..", "codegen", "src", "test", "resources", "fixtures", kind);
        if (Files.exists(fromModule)) {
            return fromModule;
        }

        throw new IllegalStateException("Could not find codegen " + kind + " fixtures.");
    }
}
