package com.jost.invernadero.automatizacion.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jost.invernadero.automatizacion.dto.EntityDto;
import com.jost.invernadero.automatizacion.dto.ErSchemaDto;
import com.jost.invernadero.automatizacion.dto.FieldDto;
import com.jost.invernadero.automatizacion.dto.RelationshipDto;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/docs")
public class DocController {

    private static final Set<String> OWNING_SIDE = Set.of("ManyToOne", "OneToOne");
    private static final Set<String> SUPPORTED_LOCALES = Set.of("es", "en");
    private static final String DEFAULT_LOCALE = "es";

    private final ObjectMapper objectMapper;
    private final ResourcePatternResolver resourceResolver;

    public DocController(ObjectMapper objectMapper, ResourcePatternResolver resourceResolver) {
        this.objectMapper = objectMapper;
        this.resourceResolver = resourceResolver;
    }

    @GetMapping("/er-schema")
    public ResponseEntity<ErSchemaDto> erSchema(
            @RequestHeader(value = "Accept-Language", defaultValue = DEFAULT_LOCALE) String acceptLanguage
    ) throws IOException {
        String locale = resolveLocale(acceptLanguage);

        List<EntityDto> entities = new ArrayList<>();
        List<RelationshipDto> relationships = new ArrayList<>();

        Resource[] resources = resourceResolver.getResources("classpath*:codegen/examples/*.json");
        List<Resource> sorted = Arrays.stream(resources)
                .sorted(Comparator.comparing(Resource::getFilename))
                .toList();

        for (Resource resource : sorted) {
            JsonNode root = objectMapper.readTree(resource.getInputStream());
            String entityName = root.get("name").asText();
            JsonNode i18n = root.path("i18n").path(locale);

            String entityDisplayName = i18n.path("singular").isMissingNode()
                    ? entityName
                    : i18n.path("singular").asText();

            List<FieldDto> fields = new ArrayList<>();
            fields.add(new FieldDto("id", "ID", "Long", true, false));

            root.get("fields").forEach(f -> {
                String fname = f.get("name").asText();
                String fdisplay = i18n.path("fields").path(fname).isMissingNode()
                        ? fname
                        : i18n.path("fields").path(fname).asText();
                fields.add(new FieldDto(fname, fdisplay, f.get("type").asText(), false, false));
            });

            JsonNode relations = root.get("relations");
            if (relations != null) {
                relations.forEach(rel -> {
                    String relType = rel.get("type").asText();
                    String relName = rel.get("name").asText();

                    if (OWNING_SIDE.contains(relType)) {
                        String joinCol = rel.has("joinColumn")
                                ? rel.get("joinColumn").asText()
                                : relName + "_id";
                        String fkName = toCamelCase(joinCol);
                        String relLabel = i18n.path("relations").path(relName).isMissingNode()
                                ? fkName
                                : i18n.path("relations").path(relName).asText() + " ID";
                        fields.add(new FieldDto(fkName, relLabel, "Long", false, true));
                    }

                    relationships.add(new RelationshipDto(
                        entityName,
                        rel.get("target").asText(),
                        toConstantCase(relType),
                        relName
                    ));
                });
            }

            entities.add(new EntityDto(entityName, entityDisplayName, fields));
        }

        return ResponseEntity.ok(new ErSchemaDto(entities, relationships));
    }

    /** "es-CO,es;q=0.9,en;q=0.8" → "es" */
    private String resolveLocale(String acceptLanguage) {
        String primary = acceptLanguage.split("[,;]")[0].trim().substring(0, 2).toLowerCase();
        return SUPPORTED_LOCALES.contains(primary) ? primary : DEFAULT_LOCALE;
    }

    /** "location_id" → "locationId" */
    private String toCamelCase(String snake) {
        String[] parts = snake.split("_");
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            sb.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
        }
        return sb.toString();
    }

    /** "ManyToOne" → "MANY_TO_ONE" */
    private String toConstantCase(String camelCase) {
        return camelCase.replaceAll("(?<=[a-z])(?=[A-Z])", "_").toUpperCase();
    }
}
