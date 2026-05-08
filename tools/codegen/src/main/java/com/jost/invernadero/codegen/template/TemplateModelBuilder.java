package com.jost.invernadero.codegen.template;

import com.jost.invernadero.codegen.model.EntityDefinition;
import com.jost.invernadero.codegen.model.FieldDef;
import com.jost.invernadero.codegen.model.RelationDef;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

public class TemplateModelBuilder {

    private static final String BASE_PACKAGE = "com.jost.invernadero.automatizacion";

    public Map<String, Object> build(EntityDefinition definition) {
        Map<String, Object> model = new LinkedHashMap<>();
        String entityName = definition.name();
        String entityCamel = toCamelCase(entityName);
        String entityKebab = toKebabCase(entityName);

        List<Map<String, Object>> fields = buildFields(definition);
        List<Map<String, Object>> relations = buildRelations(definition);
        List<Map<String, Object>> dtoComponents = buildDtoComponents(fields, relations);
        List<Map<String, Object>> enumFields = fields.stream()
                .filter(field -> Boolean.TRUE.equals(field.get("enum")))
                .toList();
        List<Map<String, Object>> formFields = buildFormFields(fields);
        List<Map<String, Object>> singleRelations = buildSingleRelations(definition);
        List<Map<String, Object>> relatedTargets = buildRelatedTargets(singleRelations);

        model.put("basePackage", BASE_PACKAGE);
        model.put("entityPackage", BASE_PACKAGE + ".entity");
        model.put("repositoryPackage", BASE_PACKAGE + ".repository");
        model.put("servicePackage", BASE_PACKAGE + ".service");
        model.put("dtoPackage", BASE_PACKAGE + ".dto");
        model.put("controllerPackage", BASE_PACKAGE + ".controller");
        model.put("entityName", entityName);
        model.put("entityCamel", entityCamel);
        model.put("entityKebab", entityKebab);
        model.put("tableName", definition.tableName());
        model.put("fields", fields);
        model.put("relations", relations);
        model.put("dtoComponents", dtoComponents);
        model.put("formFields", formFields);
        model.put("listFields", fields);
        model.put("listColumnSpan", fields.size() + 1);
        model.put("enumFields", enumFields);
        model.put("hasRelations", !relations.isEmpty());
        model.put("hasEnums", !enumFields.isEmpty());
        model.put("singleRelations", singleRelations);
        model.put("relatedTargets", relatedTargets);
        model.put("hasSingleRelations", !singleRelations.isEmpty());
        model.put("hasRelatedTargets", !relatedTargets.isEmpty());
        model.put("generateController", definition.options().generateController());
        model.put("entityImports", entityImports(fields, relations));
        model.put("dtoImports", dtoImports(dtoComponents));
        model.put("serviceImports", List.of("java.util.List", "java.util.Optional"));
        model.put("controllerImports", List.of("java.util.List"));

        return model;
    }

    private List<Map<String, Object>> buildFormFields(List<Map<String, Object>> fields) {
        return fields.stream()
                .filter(field -> !Boolean.TRUE.equals(field.get("id")))
                .map(this::formField)
                .toList();
    }

    private Map<String, Object> formField(Map<String, Object> field) {
        Map<String, Object> model = new LinkedHashMap<>(field);
        String javaType = (String) field.get("javaType");
        model.put("inputType", inputType(javaType));
        model.put("numeric", isNumeric(javaType));
        model.put("boolean", "Boolean".equals(javaType));
        model.put("date", "LocalDate".equals(javaType) || "LocalDateTime".equals(javaType));
        model.put("textInput", !Boolean.TRUE.equals(model.get("enum")) && !"Boolean".equals(javaType));
        model.put("inputLabelProps", Boolean.TRUE.equals(model.get("date")) ? "InputLabelProps={shrinkInputLabelProps}" : "");
        return model;
    }

    private List<Map<String, Object>> buildFields(EntityDefinition definition) {
        List<Map<String, Object>> fields = new ArrayList<>();
        if (definition.fields().stream().noneMatch(field -> "id".equalsIgnoreCase(field.name()))) {
            fields.add(generatedIdField());
        }
        for (FieldDef field : definition.fields()) {
            fields.add(fieldModel(definition.name(), field, "id".equalsIgnoreCase(field.name())));
        }
        return fields;
    }

    private Map<String, Object> generatedIdField() {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("name", "id");
        field.put("pascalName", "Id");
        field.put("javaType", "Long");
        field.put("sqlType", "BIGINT");
        field.put("nullable", false);
        field.put("unique", false);
        field.put("id", true);
        field.put("enum", false);
        field.put("annotations", List.of(
                "@Id",
                "@GeneratedValue(strategy = GenerationType.IDENTITY)"));
        return field;
    }

    private Map<String, Object> fieldModel(String entityName, FieldDef field, boolean id) {
        Map<String, Object> model = new LinkedHashMap<>();
        String javaType = javaType(entityName, field);
        model.put("name", field.name());
        model.put("pascalName", toPascalCase(field.name()));
        model.put("entityPackage", BASE_PACKAGE + ".entity");
        model.put("javaType", javaType);
        model.put("sqlType", sqlType(field));
        model.put("nullable", field.nullable());
        model.put("unique", field.unique());
        model.put("length", field.length());
        model.put("precision", field.precision());
        model.put("scale", field.scale());
        model.put("defaultValue", field.defaultValue());
        model.put("enumValues", field.enumValues());
        model.put("id", id);
        model.put("enum", "Enum".equals(field.type()));
        model.put("annotations", fieldAnnotations(field, id));
        return model;
    }

    private List<String> fieldAnnotations(FieldDef field, boolean id) {
        if (id) {
            return List.of(
                    "@Id",
                    "@GeneratedValue(strategy = GenerationType.IDENTITY)");
        }

        List<String> annotations = new ArrayList<>();
        if ("Enum".equals(field.type())) {
            annotations.add("@Enumerated(EnumType.STRING)");
        }
        annotations.add(columnAnnotation(field));
        return annotations;
    }

    private String columnAnnotation(FieldDef field) {
        List<String> attributes = new ArrayList<>();
        attributes.add("nullable = " + field.nullable());
        if (field.unique()) {
            attributes.add("unique = true");
        }
        if ("String".equals(field.type())) {
            attributes.add("length = " + (field.length() == null ? 255 : field.length()));
        }
        if ("BigDecimal".equals(field.type()) && field.precision() != null) {
            attributes.add("precision = " + field.precision());
        }
        if ("BigDecimal".equals(field.type()) && field.scale() != null) {
            attributes.add("scale = " + field.scale());
        }
        return "@Column(" + String.join(", ", attributes) + ")";
    }

    private List<Map<String, Object>> buildRelations(EntityDefinition definition) {
        List<Map<String, Object>> relations = new ArrayList<>();
        for (RelationDef relation : definition.relations()) {
            relations.add(relationModel(relation));
        }
        return relations;
    }

    private Map<String, Object> relationModel(RelationDef relation) {
        Map<String, Object> model = new LinkedHashMap<>();
        boolean collection = "OneToMany".equals(relation.type()) || "ManyToMany".equals(relation.type());
        String javaType = collection ? "List<" + relation.target() + ">" : relation.target();
        model.put("name", relation.name());
        model.put("pascalName", toPascalCase(relation.name()));
        model.put("type", relation.type());
        model.put("target", relation.target());
        model.put("javaType", javaType);
        model.put("mappedBy", relation.mappedBy());
        model.put("joinColumn", relation.joinColumn());
        model.put("fetch", relation.fetch());
        model.put("collection", collection);
        model.put("dtoComponent", !collection);
        model.put("annotations", relationAnnotations(relation));
        return model;
    }

    private List<String> relationAnnotations(RelationDef relation) {
        List<String> annotations = new ArrayList<>();
        String fetch = "fetch = FetchType." + relation.fetch();
        switch (relation.type()) {
            case "ManyToOne" -> {
                annotations.add("@ManyToOne(" + fetch + ")");
                annotations.add("@JoinColumn(name = \"" + relation.joinColumn() + "\")");
            }
            case "OneToOne" -> {
                annotations.add("@OneToOne(" + oneToOneAttributes(relation, fetch) + ")");
                if (hasText(relation.joinColumn())) {
                    annotations.add("@JoinColumn(name = \"" + relation.joinColumn() + "\")");
                }
            }
            case "OneToMany" -> annotations.add("@OneToMany(mappedBy = \"" + relation.mappedBy() + "\", " + fetch + ")");
            case "ManyToMany" -> {
                if (hasText(relation.mappedBy())) {
                    annotations.add("@ManyToMany(mappedBy = \"" + relation.mappedBy() + "\", " + fetch + ")");
                } else {
                    annotations.add("@ManyToMany(" + fetch + ")");
                }
            }
            default -> throw new IllegalArgumentException("Unsupported relation type: " + relation.type());
        }
        return annotations;
    }

    private String oneToOneAttributes(RelationDef relation, String fetch) {
        if (hasText(relation.mappedBy())) {
            return "mappedBy = \"" + relation.mappedBy() + "\", " + fetch;
        }
        return fetch;
    }

    private List<Map<String, Object>> buildDtoComponents(
            List<Map<String, Object>> fields,
            List<Map<String, Object>> relations) {
        List<Map<String, Object>> components = new ArrayList<>();
        for (Map<String, Object> field : fields) {
            String name = (String) field.get("name");
            components.add(dtoComponent(
                    (String) field.get("javaType"),
                    name,
                    "entity.get" + field.get("pascalName") + "()"));
        }
        for (Map<String, Object> relation : relations) {
            if (Boolean.TRUE.equals(relation.get("dtoComponent"))) {
                String name = (String) relation.get("name");
                String pascalName = (String) relation.get("pascalName");
                components.add(dtoComponent(
                        "Long",
                        name + "Id",
                        "entity.get" + pascalName + "() == null ? null : entity.get" + pascalName + "().getId()"));
            }
        }
        return components;
    }

    private Map<String, Object> dtoComponent(String javaType, String name, String readExpression) {
        Map<String, Object> component = new LinkedHashMap<>();
        component.put("javaType", javaType);
        component.put("name", name);
        component.put("pascalName", toPascalCase(name));
        component.put("readExpression", readExpression);
        return component;
    }

    private List<String> entityImports(List<Map<String, Object>> fields, List<Map<String, Object>> relations) {
        TreeSet<String> imports = new TreeSet<>();
        imports.add("jakarta.persistence.Column");
        imports.add("jakarta.persistence.Entity");
        imports.add("jakarta.persistence.GeneratedValue");
        imports.add("jakarta.persistence.GenerationType");
        imports.add("jakarta.persistence.Id");
        imports.add("jakarta.persistence.Table");
        imports.add("lombok.AllArgsConstructor");
        imports.add("lombok.Builder");
        imports.add("lombok.Getter");
        imports.add("lombok.NoArgsConstructor");
        imports.add("lombok.Setter");

        for (Map<String, Object> field : fields) {
            addJavaTypeImport(imports, (String) field.get("javaType"), false);
            if (Boolean.TRUE.equals(field.get("enum"))) {
                imports.add("jakarta.persistence.EnumType");
                imports.add("jakarta.persistence.Enumerated");
            }
        }

        for (Map<String, Object> relation : relations) {
            imports.add("jakarta.persistence.FetchType");
            switch ((String) relation.get("type")) {
                case "ManyToOne" -> {
                    imports.add("jakarta.persistence.JoinColumn");
                    imports.add("jakarta.persistence.ManyToOne");
                }
                case "OneToOne" -> {
                    imports.add("jakarta.persistence.OneToOne");
                    if (hasText((String) relation.get("joinColumn"))) {
                        imports.add("jakarta.persistence.JoinColumn");
                    }
                }
                case "OneToMany" -> imports.add("jakarta.persistence.OneToMany");
                case "ManyToMany" -> imports.add("jakarta.persistence.ManyToMany");
                default -> {
                }
            }
            if (Boolean.TRUE.equals(relation.get("collection"))) {
                imports.add("java.util.ArrayList");
                imports.add("java.util.List");
            }
        }
        return List.copyOf(imports);
    }

    private List<String> dtoImports(List<Map<String, Object>> dtoComponents) {
        TreeSet<String> imports = new TreeSet<>();
        for (Map<String, Object> component : dtoComponents) {
            addJavaTypeImport(imports, (String) component.get("javaType"), true);
        }
        return List.copyOf(imports);
    }

    private void addJavaTypeImport(TreeSet<String> imports, String javaType, boolean includeEntityTypes) {
        switch (javaType) {
            case "BigDecimal" -> imports.add("java.math.BigDecimal");
            case "LocalDate" -> imports.add("java.time.LocalDate");
            case "LocalDateTime" -> imports.add("java.time.LocalDateTime");
            case "UUID" -> imports.add("java.util.UUID");
            default -> {
                if (includeEntityTypes && !isJavaLangType(javaType) && !javaType.startsWith("List<")) {
                    imports.add(BASE_PACKAGE + ".entity." + javaType);
                }
            }
        }
    }

    private boolean isJavaLangType(String javaType) {
        return switch (javaType) {
            case "String", "Integer", "Long", "Boolean" -> true;
            default -> false;
        };
    }

    private String javaType(String entityName, FieldDef field) {
        return switch (field.type()) {
            case "Enum" -> entityName + toPascalCase(field.name());
            default -> field.type();
        };
    }

    private String sqlType(FieldDef field) {
        return switch (field.type()) {
            case "String" -> "VARCHAR(" + (field.length() == null ? 255 : field.length()) + ")";
            case "Integer" -> "INTEGER";
            case "Long" -> "BIGINT";
            case "BigDecimal" -> {
                if (field.precision() != null && field.scale() != null) {
                    yield "NUMERIC(" + field.precision() + "," + field.scale() + ")";
                }
                yield "NUMERIC";
            }
            case "Boolean" -> "BOOLEAN";
            case "LocalDate" -> "DATE";
            case "LocalDateTime" -> "TIMESTAMP";
            case "UUID" -> "UUID";
            case "Enum" -> "VARCHAR(50)";
            default -> throw new IllegalArgumentException("Unsupported field type: " + field.type());
        };
    }

    private static String toCamelCase(String value) {
        return value.substring(0, 1).toLowerCase(Locale.ROOT) + value.substring(1);
    }

    private static String toPascalCase(String value) {
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }

    private static String toKebabCase(String value) {
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (Character.isUpperCase(current) && index > 0) {
                result.append('-');
            }
            result.append(Character.toLowerCase(current));
        }
        return result.toString();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String inputType(String javaType) {
        return switch (javaType) {
            case "Integer", "Long", "BigDecimal" -> "number";
            case "Boolean" -> "checkbox";
            case "LocalDate" -> "date";
            case "LocalDateTime" -> "datetime-local";
            default -> "text";
        };
    }

    private boolean isNumeric(String javaType) {
        return "Integer".equals(javaType) || "Long".equals(javaType) || "BigDecimal".equals(javaType);
    }

    private List<Map<String, Object>> buildSingleRelations(EntityDefinition definition) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (RelationDef rel : definition.relations()) {
            boolean isManyToOne = "ManyToOne".equals(rel.type());
            boolean isOwningOneToOne = "OneToOne".equals(rel.type()) && !hasText(rel.mappedBy());
            if (isManyToOne || isOwningOneToOne) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("name", rel.name());
                entry.put("pascalName", toPascalCase(rel.name()));
                entry.put("targetEntity", rel.target());
                entry.put("targetCamel", toCamelCase(rel.target()));
                entry.put("targetApiPath", toKebabCase(rel.target()));
                entry.put("idField", rel.name() + "Id");
                entry.put("optionsVar", rel.name() + "Options");
                entry.put("optionsSetter", "set" + toPascalCase(rel.name()) + "Options");
                result.add(entry);
            }
        }
        return result;
    }

    private List<Map<String, Object>> buildRelatedTargets(List<Map<String, Object>> singleRelations) {
        LinkedHashMap<String, Map<String, Object>> seen = new LinkedHashMap<>();
        for (Map<String, Object> rel : singleRelations) {
            String target = (String) rel.get("targetEntity");
            if (!seen.containsKey(target)) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("targetEntity", target);
                entry.put("targetCamel", rel.get("targetCamel"));
                entry.put("targetApiPath", rel.get("targetApiPath"));
                seen.put(target, entry);
            }
        }
        return new ArrayList<>(seen.values());
    }
}
