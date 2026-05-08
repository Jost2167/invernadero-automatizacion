package com.jost.invernadero.codegen.template;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.jost.invernadero.codegen.model.EntityDefinition;
import com.jost.invernadero.codegen.model.EntityDefinitionObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TemplateModelBuilderTest {

    private final TemplateModelBuilder builder = new TemplateModelBuilder();

    @Test
    void buildsNamesTypesImportsAndRelationFlags() throws IOException {
        Map<String, Object> model = builder.build(readFixture("fixtures/valid/with-relations.json"));

        assertThat(model)
                .containsEntry("entityName", "Measurement")
                .containsEntry("entityCamel", "measurement")
                .containsEntry("entityKebab", "measurement")
                .containsEntry("tableName", "measurements")
                .containsEntry("hasRelations", true);

        List<Map<String, Object>> fields = fields(model);
        assertThat(fields).extracting(field -> field.get("name"))
                .containsExactly("id", "recordedAt", "temperature");
        assertThat(fields.get(0).get("annotations").toString())
                .contains("@Id")
                .contains("GenerationType.IDENTITY");
        assertThat(fields.get(2))
                .containsEntry("javaType", "BigDecimal")
                .containsEntry("sqlType", "NUMERIC(8,2)");

        List<Map<String, Object>> relations = relations(model);
        assertThat(relations).hasSize(1);
        assertThat(relations.get(0))
                .containsEntry("name", "sensor")
                .containsEntry("javaType", "Sensor")
                .containsEntry("dtoComponent", true);
        assertThat(relations.get(0).get("annotations").toString())
                .contains("@ManyToOne(fetch = FetchType.LAZY)")
                .contains("@JoinColumn(name = \"sensor_id\")");

        assertThat(imports(model, "entityImports"))
                .contains("jakarta.persistence.ManyToOne", "java.math.BigDecimal");
    }

    @Test
    void buildsEnumFieldModelAndDtoImport() throws IOException {
        Map<String, Object> model = builder.build(readFixture("fixtures/valid/with-enum.json"));

        List<Map<String, Object>> enumFields = fields(model, "enumFields");
        assertThat(enumFields).hasSize(1);
        assertThat(enumFields.get(0))
                .containsEntry("name", "severity")
                .containsEntry("javaType", "AlertSeverity")
                .containsEntry("sqlType", "VARCHAR(50)");
        assertThat(imports(model, "dtoImports"))
                .contains("com.jost.invernadero.automatizacion.entity.AlertSeverity");
    }

    @Test
    void backendTemplatesRenderWithTheGeneratedModel() throws IOException {
        Map<String, Object> model = builder.build(readFixture("fixtures/valid/with-enum.json"));
        Handlebars handlebars = new Handlebars(new ClassPathTemplateLoader("/templates/backend", ".hbs"));

        assertThat(handlebars.compile("entity").apply(model))
                .contains("public class Alert")
                .contains("@Enumerated(EnumType.STRING)")
                .contains("private AlertSeverity severity;");
        assertThat(handlebars.compile("repository").apply(model))
                .contains("extends JpaRepository<Alert, Long>");
        assertThat(handlebars.compile("service").apply(model))
                .contains("List<Alert> findAll();");
        assertThat(handlebars.compile("service-impl").apply(model))
                .contains("class AlertServiceImpl implements AlertService");
        assertThat(handlebars.compile("dto").apply(model))
                .contains("public record AlertDto(")
                .contains("AlertSeverity severity")
                .contains("Long sensorId");
        assertThat(handlebars.compile("controller").apply(model))
                .contains("@RequestMapping(\"/api/alert\")")
                .contains("private AlertDto toDto(Alert entity)")
                .contains("private Alert toEntity(AlertDto dto)");
        assertThat(handlebars.compile("enum").apply(fields(model, "enumFields").get(0)))
                .contains("public enum AlertSeverity")
                .contains("CRITICAL");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fields(Map<String, Object> model) {
        return (List<Map<String, Object>>) model.get("fields");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fields(Map<String, Object> model, String key) {
        return (List<Map<String, Object>>) model.get(key);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> relations(Map<String, Object> model) {
        return (List<Map<String, Object>>) model.get("relations");
    }

    @SuppressWarnings("unchecked")
    private List<String> imports(Map<String, Object> model, String key) {
        return (List<String>) model.get(key);
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
