package com.jost.invernadero.codegen.generator;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.jost.invernadero.codegen.model.EntityDefinition;
import com.jost.invernadero.codegen.model.EntityDefinitionObjectMapper;
import com.jost.invernadero.codegen.model.FieldDef;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MigrationModelBuilderTest {

    @Test
    void mapsSqlTypesConstraintsAndDefaults() {
        SqlTypeMapper mapper = new SqlTypeMapper();

        assertThat(mapper.typeSql(field("String", 80, null, null, null)))
                .isEqualTo("VARCHAR(80)");
        assertThat(mapper.typeSql(field("String", null, null, null, null)))
                .isEqualTo("VARCHAR(255)");
        assertThat(mapper.typeSql(field("BigDecimal", null, 10, 2, null)))
                .isEqualTo("NUMERIC(10,2)");
        assertThat(mapper.constraints(field("Boolean", null, null, null, true)))
                .isEqualTo(" NOT NULL DEFAULT TRUE");
        assertThat(mapper.constraints(new FieldDef(
                "name", "String", false, true, 80, null, null, "sensor", List.of())))
                .isEqualTo(" NOT NULL UNIQUE DEFAULT 'SENSOR'");
    }

    @Test
    void buildsMigrationModelWithDeterministicFileNameForeignKeysAndChecks() throws IOException {
        Clock clock = Clock.fixed(Instant.parse("2026-05-06T13:14:15.123Z"), ZoneOffset.UTC);
        MigrationModelBuilder builder = new MigrationModelBuilder(clock);

        Map<String, Object> model = builder.build(readFixture("fixtures/valid/with-enum.json"));

        assertThat(model)
                .containsEntry("tableName", "alerts")
                .containsEntry("fileName", "V20260506131415123__create_alerts.sql");
        assertThat(lines(model))
                .contains(
                        "id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY",
                        "message VARCHAR(255) NOT NULL",
                        "severity VARCHAR(50) NOT NULL",
                        "sensor_id BIGINT",
                        "CONSTRAINT fk_alerts_sensor FOREIGN KEY (sensor_id) REFERENCES sensors(id)",
                        "CONSTRAINT chk_alerts_severity CHECK (severity IN ('LOW','MEDIUM','HIGH','CRITICAL'))");
    }

    @Test
    void createTableTemplateRendersDefinitionsInOrder() throws IOException {
        Clock clock = Clock.fixed(Instant.parse("2026-05-06T13:14:15.123Z"), ZoneOffset.UTC);
        Map<String, Object> model = new MigrationModelBuilder(clock)
                .build(readFixture("fixtures/valid/with-enum.json"));

        Handlebars handlebars = new Handlebars(new ClassPathTemplateLoader("/templates/migration", ".hbs"));
        String sql = handlebars.compile("create-table").apply(model).replace("\r\n", "\n");

        assertThat(sql).isEqualTo("""
                CREATE TABLE alerts (
                    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    message VARCHAR(255) NOT NULL,
                    severity VARCHAR(50) NOT NULL,
                    sensor_id BIGINT,
                    CONSTRAINT fk_alerts_sensor FOREIGN KEY (sensor_id) REFERENCES sensors(id),
                    CONSTRAINT chk_alerts_severity CHECK (severity IN ('LOW','MEDIUM','HIGH','CRITICAL'))
                );
                """.stripIndent());
    }

    @SuppressWarnings("unchecked")
    private List<String> lines(Map<String, Object> model) {
        return (List<String>) model.get("definitions");
    }

    private FieldDef field(String type, Integer length, Integer precision, Integer scale, Object defaultValue) {
        return new FieldDef("value", type, false, false, length, precision, scale, defaultValue, List.of());
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
