package com.jost.invernadero.codegen.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class EntityDefinitionDeserializationTest {

    @Test
    void deserializesSimpleFixture() throws IOException {
        EntityDefinition definition = readFixture("fixtures/valid/simple.json");

        assertThat(definition.version()).isEqualTo("1");
        assertThat(definition.name()).isEqualTo("Sensor");
        assertThat(definition.tableName()).isEqualTo("sensors");
        assertThat(definition.relations()).isEmpty();
        assertThat(definition.options()).isEqualTo(Options.defaults());
        assertThat(definition.fields()).hasSize(3);

        FieldDef id = definition.fields().get(0);
        assertThat(id.name()).isEqualTo("id");
        assertThat(id.type()).isEqualTo("Long");
        assertThat(id.nullable()).isFalse();
        assertThat(id.unique()).isFalse();

        FieldDef name = definition.fields().get(1);
        assertThat(name.name()).isEqualTo("name");
        assertThat(name.type()).isEqualTo("String");
        assertThat(name.length()).isEqualTo(120);
        assertThat(name.nullable()).isFalse();
        assertThat(name.unique()).isTrue();

        FieldDef active = definition.fields().get(2);
        assertThat(active.defaultValue()).isEqualTo(true);
    }

    @Test
    void deserializesFixtureWithRelations() throws IOException {
        EntityDefinition definition = readFixture("fixtures/valid/with-relations.json");

        assertThat(definition.name()).isEqualTo("Measurement");
        assertThat(definition.tableName()).isEqualTo("measurements");
        assertThat(definition.fields()).extracting(FieldDef::name)
                .containsExactly("recordedAt", "temperature");

        FieldDef temperature = definition.fields().get(1);
        assertThat(temperature.type()).isEqualTo("BigDecimal");
        assertThat(temperature.precision()).isEqualTo(8);
        assertThat(temperature.scale()).isEqualTo(2);

        assertThat(definition.relations()).hasSize(1);
        RelationDef sensor = definition.relations().get(0);
        assertThat(sensor.name()).isEqualTo("sensor");
        assertThat(sensor.type()).isEqualTo("ManyToOne");
        assertThat(sensor.target()).isEqualTo("Sensor");
        assertThat(sensor.joinColumn()).isEqualTo("sensor_id");
        assertThat(sensor.mappedBy()).isNull();
        assertThat(sensor.cascade()).isEmpty();
        assertThat(sensor.fetch()).isEqualTo("LAZY");
    }

    @Test
    void deserializesFixtureWithEnum() throws IOException {
        EntityDefinition definition = readFixture("fixtures/valid/with-enum.json");

        assertThat(definition.name()).isEqualTo("Alert");
        assertThat(definition.fields()).hasSize(2);

        FieldDef severity = definition.fields().get(1);
        assertThat(severity.name()).isEqualTo("severity");
        assertThat(severity.type()).isEqualTo("Enum");
        assertThat(severity.enumValues()).containsExactly("LOW", "MEDIUM", "HIGH", "CRITICAL");
        assertThat(severity.nullable()).isFalse();

        RelationDef sensor = definition.relations().get(0);
        assertThat(sensor.fetch()).isEqualTo("LAZY");
    }

    @Test
    void deserializesFixtureWithOptions() throws IOException {
        EntityDefinition definition = readFixture("fixtures/valid/with-options.json");

        assertThat(definition.name()).isEqualTo("IrrigationSchedule");
        assertThat(definition.tableName()).isEqualTo("irrigation_schedules");
        assertThat(definition.fields()).extracting(FieldDef::type)
                .containsExactly("LocalDateTime", "Integer", "Boolean");
        assertThat(definition.relations()).isEmpty();
        assertThat(definition.options()).isEqualTo(new Options(true, true, true));
    }

    @Test
    void rejectsUnknownPropertiesDuringDeserialization() {
        assertThatThrownBy(() -> readFixture("fixtures/invalid/unknown-prop.json"))
                .isInstanceOf(UnrecognizedPropertyException.class)
                .hasMessageContaining("unknownProperty");
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
