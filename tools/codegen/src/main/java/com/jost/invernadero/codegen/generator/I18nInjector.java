package com.jost.invernadero.codegen.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jost.invernadero.codegen.model.EntityDefinition;
import com.jost.invernadero.codegen.model.FieldDef;
import com.jost.invernadero.codegen.model.LocaleLabels;
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
        return inject(jsonPath, definition, false);
    }

    public InjectionResult inject(Path jsonPath, EntityDefinition definition, boolean overwriteExisting) {
        if (!Files.exists(jsonPath)) {
            return InjectionResult.error("No existe el archivo i18n `" + jsonPath
                    + "`. Crealo con `{}` y vuelve a intentar.");
        }

        try {
            ObjectNode root = (ObjectNode) objectMapper.readTree(Files.readString(jsonPath, StandardCharsets.UTF_8));
            String entityKey = toCamelCase(definition.name());
            String entityKebab = toKebabCase(definition.name());
            String locale = localeFromFileName(jsonPath.getFileName().toString());
            ObjectNode defaults = defaults(definition, entityKey, locale);
            ObjectNode current = root.withObject("/" + entityKey);
            merge(current, defaults, overwriteExisting);
            ObjectNode sidebar = root.withObject("/sidebar");
            merge(sidebar, sidebarDefaults(definition, entityKebab, locale), overwriteExisting);
            String content = objectMapper.writeValueAsString(root) + System.lineSeparator();
            return InjectionResult.success(content);
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo actualizar i18n `" + jsonPath + "`", ex);
        }
    }

    private ObjectNode defaults(EntityDefinition definition, String entityKey, String locale) {
        LocaleLabels labels = definition.i18n().resolve(locale);
        String singular = presentOrDefault(labels.singular(), definition.name());
        String plural = presentOrDefault(labels.plural(), definition.name());
        UiLabels ui = uiLabels(locale);

        ObjectNode entity = objectMapper.createObjectNode();
        ObjectNode list = entity.putObject("list");
        list.put("title", plural);
        list.put("create", ui.create());
        list.put("edit", ui.edit());
        list.put("delete", ui.delete());
        list.put("actions", ui.actions());
        list.put("empty", ui.empty());
        list.put("loadError", ui.listLoadError());
        ObjectNode listFields = list.putObject("fields");

        ObjectNode form = entity.putObject("form");
        form.put("createTitle", ui.create() + " " + singular);
        form.put("editTitle", ui.edit() + " " + singular);
        form.put("save", ui.save());
        form.put("cancel", ui.cancel());
        form.put("loadError", ui.formLoadError());
        form.put("saveError", ui.saveError());
        ObjectNode formFields = form.putObject("fields");

        if (definition.fields().stream().noneMatch(field -> "id".equalsIgnoreCase(field.name()))) {
            listFields.put("id", labels.fields().getOrDefault("id", "ID"));
        }
        for (FieldDef field : definition.fields()) {
            String fieldName = field.name();
            String fieldLabel = "id".equalsIgnoreCase(fieldName)
                    ? labels.fields().getOrDefault(fieldName, "ID")
                    : labels.fields().getOrDefault(fieldName, label(fieldName));
            listFields.put(fieldName, fieldLabel);
            if (!"id".equalsIgnoreCase(fieldName)) {
                formFields.put(fieldName, fieldLabel);
            }
        }

        addRelationFieldLabels(entity, definition, locale);

        ObjectNode wrapper = objectMapper.createObjectNode();
        wrapper.set(entityKey, entity);
        return (ObjectNode) wrapper.get(entityKey);
    }

    private ObjectNode sidebarDefaults(EntityDefinition definition, String entityKebab, String locale) {
        LocaleLabels labels = definition.i18n().resolve(locale);
        ObjectNode sidebar = objectMapper.createObjectNode();
        sidebar.put(entityKebab, presentOrDefault(labels.plural(), definition.name()));
        return sidebar;
    }

    private void merge(ObjectNode target, ObjectNode defaults, boolean overwriteExisting) {
        defaults.fields().forEachRemaining(entry -> {
            if (!target.has(entry.getKey())) {
                target.set(entry.getKey(), entry.getValue());
            } else if (target.get(entry.getKey()).isObject() && entry.getValue().isObject()) {
                merge((ObjectNode) target.get(entry.getKey()), (ObjectNode) entry.getValue(), overwriteExisting);
            } else if (overwriteExisting) {
                target.set(entry.getKey(), entry.getValue());
            }
        });
    }

    private String label(String name) {
        String spaced = name.replaceAll("([a-z])([A-Z])", "$1 $2");
        return spaced.substring(0, 1).toUpperCase(Locale.ROOT) + spaced.substring(1);
    }

    private void addRelationFieldLabels(ObjectNode entity, EntityDefinition definition, String locale) {
        boolean hasRelationFields = definition.relations().stream()
                .anyMatch(r -> !"ManyToMany".equals(r.type()));
        if (!hasRelationFields) {
            return;
        }
        LocaleLabels labels = definition.i18n().resolve(locale);
        ObjectNode fieldSection = entity.putObject("field");
        for (RelationDef relation : definition.relations()) {
            if (!"ManyToMany".equals(relation.type())) {
                String relationName = relation.name();
                fieldSection.put(relationName, labels.relations().getOrDefault(relationName, label(relationName)));
            }
        }
    }

    private String localeFromFileName(String fileName) {
        int extensionStart = fileName.lastIndexOf('.');
        if (extensionStart <= 0) {
            return fileName;
        }
        return fileName.substring(0, extensionStart);
    }

    private String presentOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String toCamelCase(String value) {
        return value.substring(0, 1).toLowerCase(Locale.ROOT) + value.substring(1);
    }

    private String toKebabCase(String value) {
        return value.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase(Locale.ROOT);
    }

    private UiLabels uiLabels(String locale) {
        if ("en".equals(locale)) {
            return new UiLabels(
                    "Create",
                    "Edit",
                    "Delete",
                    "Actions",
                    "No records",
                    "Could not load records",
                    "Save",
                    "Cancel",
                    "Could not load the record",
                    "Could not save the record");
        }
        return new UiLabels(
                "Crear",
                "Editar",
                "Eliminar",
                "Acciones",
                "Sin registros",
                "No se pudieron cargar los registros",
                "Guardar",
                "Cancelar",
                "No se pudo cargar el registro",
                "No se pudo guardar el registro");
    }

    private record UiLabels(
            String create,
            String edit,
            String delete,
            String actions,
            String empty,
            String listLoadError,
            String save,
            String cancel,
            String formLoadError,
            String saveError) {
    }
}
