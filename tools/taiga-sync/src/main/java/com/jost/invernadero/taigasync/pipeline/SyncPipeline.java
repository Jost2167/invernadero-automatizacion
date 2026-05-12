package com.jost.invernadero.taigasync.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jost.invernadero.codegen.model.EntityDefinition;
import com.jost.invernadero.codegen.model.EntityDefinitionObjectMapper;
import com.jost.invernadero.codegen.validator.ValidationReport;
import com.jost.invernadero.taigasync.client.StoryRef;
import com.jost.invernadero.taigasync.client.TaigaClient;
import com.jost.invernadero.taigasync.config.SyncConfig;
import com.jost.invernadero.taigasync.validation.SchemaValidator;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SyncPipeline {

    private static final String CODEGEN_SCHEMA_TAG = "codegen-schema";

    private final SyncConfig config;
    private final TaigaClient taigaClient;
    private final SchemaValidator schemaValidator;
    private final ObjectMapper objectMapper;
    private final Path examplesDirectory;
    private final PrintStream out;

    public SyncPipeline(SyncConfig config) {
        this(
                config,
                new TaigaClient(config.baseUrl(), config.authToken()),
                new SchemaValidator(),
                EntityDefinitionObjectMapper.create(),
                Path.of("tools", "codegen", "examples"),
                System.out);
    }

    public SyncPipeline(
            SyncConfig config,
            TaigaClient taigaClient,
            SchemaValidator schemaValidator,
            ObjectMapper objectMapper,
            Path examplesDirectory,
            PrintStream out) {
        this.config = Objects.requireNonNull(config, "config");
        this.taigaClient = Objects.requireNonNull(taigaClient, "taigaClient");
        this.schemaValidator = Objects.requireNonNull(schemaValidator, "schemaValidator");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.examplesDirectory = Objects.requireNonNull(examplesDirectory, "examplesDirectory");
        this.out = Objects.requireNonNull(out, "out");
    }

    public SyncResult run(boolean dryRun) throws IOException, InterruptedException {
        long projectId = taigaClient.getProjectId(config.projectSlug());
        long codegenFieldId = resolveCodegenFieldId(projectId);
        long doneStatusId = taigaClient.getDoneStatusId(projectId);
        List<StoryRef> stories = taigaClient.getTaggedStories(projectId, CODEGEN_SCHEMA_TAG);

        if (stories.isEmpty()) {
            out.println("No se encontraron historias con la etiqueta '" + CODEGEN_SCHEMA_TAG + "'.");
            return new SyncResult(0, 0, 0, 0, 0, 0, 0);
        }

        List<ValidSchema> validSchemas = new ArrayList<>();
        int validationFailures = 0;
        int skippedStories = 0;
        int commentsPosted = 0;

        for (StoryRef story : stories) {
            Map<String, JsonNode> attributes = taigaClient.getStoryAttributes(story.id());
            String json = extractJson(attributes, codegenFieldId);
            if (json == null) {
                skippedStories++;
                out.println("Omitiendo historia #" + story.ref() + ": codegen_json esta vacio o no existe.");
                continue;
            }

            ValidationReport report = schemaValidator.validate(json);
            if (report.hasErrors()) {
                validationFailures++;
                if (publishComment(story, validationComment(story, report), dryRun)) {
                    commentsPosted++;
                }
                continue;
            }

            validSchemas.add(toValidSchema(story, json));
        }

        Map<String, List<ValidSchema>> schemasByName = groupByName(validSchemas);
        int schemasWritten = 0;
        int collisionFailures = 0;
        int storiesMarkedDone = 0;

        for (ValidSchema schema : validSchemas) {
            List<ValidSchema> collisions = schemasByName.get(schema.name());
            if (collisions.size() > 1) {
                collisionFailures++;
                if (publishComment(schema.story(), collisionComment(schema, collisions), dryRun)) {
                    commentsPosted++;
                }
                continue;
            }

            writeSchema(schema, dryRun);
            if (!dryRun) {
                schemasWritten++;
            }
            if (markSchemaAsDone(schema, doneStatusId, dryRun)) {
                storiesMarkedDone++;
            }
        }

        return new SyncResult(
                stories.size(),
                schemasWritten,
                validationFailures,
                collisionFailures,
                skippedStories,
                commentsPosted,
                storiesMarkedDone);
    }

    private long resolveCodegenFieldId(long projectId) throws IOException, InterruptedException {
        if (config.codegenFieldId().isPresent()) {
            return config.codegenFieldId().get();
        }
        return taigaClient.getCodegenFieldId(projectId);
    }

    private String extractJson(Map<String, JsonNode> attributes, long codegenFieldId) {
        JsonNode value = attributes.get(Long.toString(codegenFieldId));
        if (value == null || value.isNull()) {
            return null;
        }

        String json = value.isTextual() ? value.asText() : value.toString();
        if (json.isBlank()) {
            return null;
        }
        return json;
    }

    private ValidSchema toValidSchema(StoryRef story, String json) throws IOException {
        JsonNode node = objectMapper.readTree(json);
        EntityDefinition definition = objectMapper.treeToValue(node, EntityDefinition.class);
        return new ValidSchema(story, definition.name(), node);
    }

    private Map<String, List<ValidSchema>> groupByName(List<ValidSchema> validSchemas) {
        Map<String, List<ValidSchema>> schemasByName = new LinkedHashMap<>();
        for (ValidSchema schema : validSchemas) {
            schemasByName.computeIfAbsent(schema.name(), ignored -> new ArrayList<>()).add(schema);
        }
        return schemasByName;
    }

    private void writeSchema(ValidSchema schema, boolean dryRun) throws IOException {
        Path target = examplesDirectory.resolve(schema.name() + ".json").normalize();
        if (dryRun) {
            out.println("[dry-run] Se escribiria el schema de la historia #" + schema.story().ref() + " en " + target);
            return;
        }

        Files.createDirectories(examplesDirectory);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(target.toFile(), schema.json());
    }

    private boolean markSchemaAsDone(ValidSchema schema, long doneStatusId, boolean dryRun) {
        StoryRef story = schema.story();
        if (dryRun) {
            out.println("[dry-run] Se marcaría la historia #" + story.ref() + " como Done en Taiga.");
            return false;
        }

        try {
            taigaClient.markAsDone(story.id(), doneStatusId);
            return true;
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            out.println("Advertencia: no se pudo marcar la historia #" + story.ref()
                    + " como Done en Taiga: " + exception.getMessage());
            return false;
        }
    }

    private boolean publishComment(StoryRef story, String comment, boolean dryRun) throws IOException, InterruptedException {
        if (dryRun) {
            out.println("[dry-run] Se publicaria un comentario en la historia #" + story.ref() + ":");
            out.println(comment);
            return false;
        }

        taigaClient.postComment(story.id(), comment);
        return true;
    }

    private String validationComment(StoryRef story, ValidationReport report) {
        return "Automatic taiga-sync validation report for story #" + story.ref() + "."
                + System.lineSeparator()
                + System.lineSeparator()
                + report.format();
    }

    private String collisionComment(ValidSchema schema, List<ValidSchema> collisions) {
        String refs = collisions.stream()
                .map(collision -> "#" + collision.story().ref())
                .reduce((left, right) -> left + ", " + right)
                .orElse("#" + schema.story().ref());

        return "Automatic taiga-sync validation report for story #" + schema.story().ref() + "."
                + System.lineSeparator()
                + System.lineSeparator()
                + "Entity name collision: '" + schema.name() + "' is defined by stories " + refs
                + ". No file was written for this entity.";
    }

    private record ValidSchema(StoryRef story, String name, JsonNode json) {
    }
}
