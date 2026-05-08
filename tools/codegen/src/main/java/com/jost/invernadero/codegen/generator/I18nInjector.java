package com.jost.invernadero.codegen.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jost.invernadero.codegen.model.EntityDefinition;
import com.jost.invernadero.codegen.model.FieldDef;
import com.jost.invernadero.codegen.model.RelationDef;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class I18nInjector {

    private final ObjectMapper objectMapper;

    public I18nInjector() {
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public InjectionResult inject(Path jsonPath, EntityDefinition definition) {
        if (!Files.exists(jsonPath)) {
            return InjectionResult.error("No existe el archivo i18n `" + jsonPath
                    + "`. Crealo con `{}` y vuelve a intentar.");
        }

        try {
            ObjectNode root = (ObjectNode) objectMapper.readTree(Files.readString(jsonPath, StandardCharsets.UTF_8));
            String entityKey = toCamelCase(definition.name());
            ObjectNode defaults = defaults(definition, entityKey);
            ObjectNode current = root.withObject("/" + entityKey);
            mergeMissing(current, defaults);
            String content = objectMapper.writeValueAsString(root) + System.lineSeparator();
            return InjectionResult.success(content);
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo actualizar i18n `" + jsonPath + "`", ex);
        }
    }

    private ObjectNode defaults(EntityDefinition definition, String entityKey) {
        ObjectNode entity = objectMapper.createObjectNode();
        ObjectNode list = entity.putObject("list");
        list.put("title", definition.name());
        list.put("create", "Crear");
        list.put("edit", "Editar");
        list.put("delete", "Eliminar");
        list.put("actions", "Acciones");
        list.put("empty", "Sin registros");
        list.put("loadError", "No se pudieron cargar los registros");
        ObjectNode listFields = list.putObject("fields");

        ObjectNode form = entity.putObject("form");
        form.put("createTitle", "Crear " + definition.name());
        form.put("editTitle", "Editar " + definition.name());
        form.put("save", "Guardar");
        form.put("cancel", "Cancelar");
        form.put("loadError", "No se pudo cargar el registro");
        form.put("saveError", "No se pudo guardar el registro");
        ObjectNode formFields = form.putObject("fields");

        for (FieldDef field : definition.fields()) {
            String label = label(field.name());
            listFields.put(field.name(), label);
            formFields.put(field.name(), label);
        }

        addRelationFieldLabels(entity, definition);

        ObjectNode wrapper = objectMapper.createObjectNode();
        wrapper.set(entityKey, entity);
        return (ObjectNode) wrapper.get(entityKey);
    }

    private void mergeMissing(ObjectNode target, ObjectNode defaults) {
        defaults.fields().forEachRemaining(entry -> {
            if (!target.has(entry.getKey())) {
                target.set(entry.getKey(), entry.getValue());
            } else if (target.get(entry.getKey()).isObject() && entry.getValue().isObject()) {
                mergeMissing((ObjectNode) target.get(entry.getKey()), (ObjectNode) entry.getValue());
            }
        });
    }

    private String label(String name) {
        String spaced = name.replaceAll("([a-z])([A-Z])", "$1 $2");
        return spaced.substring(0, 1).toUpperCase(Locale.ROOT) + spaced.substring(1);
    }

    private void addRelationFieldLabels(ObjectNode entity, EntityDefinition definition) {
        boolean hasRelationFields = definition.relations().stream()
                .anyMatch(r -> !"ManyToMany".equals(r.type()));
        if (!hasRelationFields) {
            return;
        }
        ObjectNode fieldSection = entity.putObject("field");
        for (RelationDef relation : definition.relations()) {
            if (!"ManyToMany".equals(relation.type())) {
                fieldSection.put(relation.name(), label(relation.name()));
            }
        }
    }

    private String toCamelCase(String value) {
        return value.substring(0, 1).toLowerCase(Locale.ROOT) + value.substring(1);
    }
}
