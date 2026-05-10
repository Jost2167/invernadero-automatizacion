package com.jost.invernadero.codegen.generator;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.jost.invernadero.codegen.fs.FileSystemWriter;
import com.jost.invernadero.codegen.model.EntityDefinition;
import com.jost.invernadero.codegen.model.Options;
import com.jost.invernadero.codegen.template.TemplateModelBuilder;
import java.io.Console;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Generator {

    private final Path root;
    private final FileSystemWriter writer;
    private final TemplateModelBuilder templateModelBuilder;
    private final MigrationModelBuilder migrationModelBuilder;
    private final String backendRoot;
    private final String frontendRoot;
    private final Handlebars backendHandlebars;
    private final Handlebars frontendHandlebars;
    private final Handlebars migrationHandlebars;
    private final RouteInjector routeInjector;
    private final NavInjector navInjector;
    private final I18nInjector i18nInjector;

    public Generator(Path root, Clock clock) {
        this(root, clock, "backend", "frontend");
    }

    public Generator(Path root, Clock clock, String backendRoot, String frontendRoot) {
        this(root, new FileSystemWriter(), new TemplateModelBuilder(), new MigrationModelBuilder(clock), backendRoot, frontendRoot);
    }

    public Generator(
            Path root,
            FileSystemWriter writer,
            TemplateModelBuilder templateModelBuilder,
            MigrationModelBuilder migrationModelBuilder) {
        this(root, writer, templateModelBuilder, migrationModelBuilder, "backend", "frontend");
    }

    public Generator(
            Path root,
            FileSystemWriter writer,
            TemplateModelBuilder templateModelBuilder,
            MigrationModelBuilder migrationModelBuilder,
            String backendRoot,
            String frontendRoot) {
        this.root = root;
        this.writer = writer;
        this.templateModelBuilder = templateModelBuilder;
        this.migrationModelBuilder = migrationModelBuilder;
        this.backendRoot = trimSlashes(backendRoot);
        this.frontendRoot = trimSlashes(frontendRoot);
        this.backendHandlebars = new Handlebars(new ClassPathTemplateLoader("/templates/backend", ".hbs"));
        this.frontendHandlebars = new Handlebars(new ClassPathTemplateLoader("/templates/frontend", ".hbs"));
        this.migrationHandlebars = new Handlebars(new ClassPathTemplateLoader("/templates/migration", ".hbs"));
        this.routeInjector = new RouteInjector();
        this.navInjector = new NavInjector();
        this.i18nInjector = new I18nInjector();
    }

    public GenerationResult generate(EntityDefinition definition, Options options, Mode mode) {
        List<FileToWrite> files = collectFiles(definition, options, mode);
        if (files.stream().anyMatch(file -> file.content() == null)) {
            return GenerationResult.error(files, files.stream()
                    .filter(file -> file.content() == null)
                    .map(file -> "No se pudo preparar " + file.path())
                    .toList());
        }

        if (mode.isDryRun()) {
            return GenerationResult.success(files, dryRunMessages(files));
        }

        List<String> collisions = collisions(files);
        if (!collisions.isEmpty() && !mode.isOverwrite()) {
            return GenerationResult.error(files, collisions.stream()
                    .map(path -> "archivo ya existe: " + path + ". Usa --overwrite para sobrescribir.")
                    .toList());
        }

        if (!collisions.isEmpty() && mode.isOverwrite() && !mode.isYes() && !confirmOverwrite(collisions)) {
            return GenerationResult.error(files, List.of(
                    "modo no interactivo: anade --yes para confirmar overwrite"));
        }

        for (FileToWrite file : files) {
            Path absolute = root.resolve(file.path());
            if (file.policy() == FileToWrite.WritePolicy.UPDATE || mode.isOverwrite()) {
                writer.writeOverwriting(absolute, file.content());
            } else {
                writer.writeIfAbsent(absolute, file.content());
            }
        }
        return GenerationResult.success(files, files.stream()
                .map(file -> "[WRITE] " + normalizePath(file.path()))
                .toList());
    }

    private List<FileToWrite> collectFiles(EntityDefinition definition, Options options, Mode mode) {
        try {
            Map<String, Object> model = templateModelBuilder.build(definition);
            List<FileToWrite> files = new ArrayList<>();
            String entityName = definition.name();
            String entityKebab = (String) model.get("entityKebab");
            String backendBase = backendRoot + "/src/main/java/com/jost/invernadero/automatizacion/";

            files.add(create(backendBase + "entity/" + entityName + ".java", render(backendHandlebars, "entity", model)));
            files.add(create(backendBase + "repository/" + entityName + "Repository.java", render(backendHandlebars, "repository", model)));
            files.add(create(backendBase + "service/" + entityName + "Service.java", render(backendHandlebars, "service", model)));
            files.add(create(backendBase + "service/" + entityName + "ServiceImpl.java", render(backendHandlebars, "service-impl", model)));
            files.add(create(backendBase + "dto/" + entityName + "Dto.java", render(backendHandlebars, "dto", model)));
            if (options.generateController()) {
                files.add(create(backendBase + "controller/" + entityName + "Controller.java", render(backendHandlebars, "controller", model)));
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> enumFields = (List<Map<String, Object>>) model.get("enumFields");
            for (Map<String, Object> enumField : enumFields) {
                files.add(create(backendBase + "entity/" + enumField.get("javaType") + ".java",
                        render(backendHandlebars, "enum", enumField)));
            }

            Map<String, Object> migrationModel = migrationModelBuilder.build(definition);
            files.add(create(backendRoot + "/src/main/resources/db/migration/" + migrationModel.get("fileName"),
                    render(migrationHandlebars, "create-table", migrationModel)));

            if (options.generateFrontend()) {
                collectFrontendFiles(definition, model, files, entityName, entityKebab, mode.isOverwrite());
            }

            return files;
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudieron renderizar las plantillas", ex);
        }
    }

    private void collectFrontendFiles(
            EntityDefinition definition,
            Map<String, Object> model,
            List<FileToWrite> files,
            String entityName,
            String entityKebab,
            boolean overwriteTranslations) throws IOException {
        files.add(create(frontendRoot + "/src/api/" + entityKebab + ".js", render(frontendHandlebars, "api-client", model)));
        files.add(create(frontendRoot + "/src/pages/" + entityKebab + "/" + entityName + "ListPage.jsx",
                render(frontendHandlebars, "list-page", model)));
        files.add(create(frontendRoot + "/src/pages/" + entityKebab + "/" + entityName + "FormPage.jsx",
                render(frontendHandlebars, "form-page", model)));

        Path appPath = root.resolve(frontendRoot + "/src/App.jsx");
        if (Files.exists(appPath)) {
            InjectionResult routeResult = routeInjector.inject(
                    Files.readString(appPath, StandardCharsets.UTF_8), entityName, entityKebab);
            if (!routeResult.success()) {
                throw new IllegalStateException(routeResult.error());
            }
            files.add(update(frontendRoot + "/src/App.jsx", routeResult.content()));
        }

        Path sidebarPath = root.resolve(frontendRoot + "/src/components/Sidebar.jsx");
        if (Files.exists(sidebarPath)) {
            InjectionResult navResult = navInjector.inject(
                    Files.readString(sidebarPath, StandardCharsets.UTF_8), entityKebab, entityKebab);
            if (!navResult.success()) {
                throw new IllegalStateException(navResult.error());
            }
            files.add(update(frontendRoot + "/src/components/Sidebar.jsx", navResult.content()));
        }

        addI18nUpdate(definition, files, frontendRoot + "/src/i18n/es.json", overwriteTranslations);
        addI18nUpdate(definition, files, frontendRoot + "/src/i18n/en.json", overwriteTranslations);
    }

    private void addI18nUpdate(
            EntityDefinition definition,
            List<FileToWrite> files,
            String relativePath,
            boolean overwriteTranslations) {
        Path path = root.resolve(relativePath);
        if (Files.exists(path)) {
            InjectionResult result = i18nInjector.inject(path, definition, overwriteTranslations);
            if (!result.success()) {
                throw new IllegalStateException(result.error());
            }
            files.add(update(relativePath, result.content()));
        }
    }

    private String render(Handlebars handlebars, String template, Object model) throws IOException {
        return handlebars.compile(template).apply(model);
    }

    private FileToWrite create(String path, String content) {
        return new FileToWrite(Path.of(path), content, FileToWrite.WritePolicy.CREATE);
    }

    private FileToWrite update(String path, String content) {
        return new FileToWrite(Path.of(path), content, FileToWrite.WritePolicy.UPDATE);
    }

    private List<String> dryRunMessages(List<FileToWrite> files) {
        return files.stream()
                .map(file -> {
                    String status = writer.exists(root.resolve(file.path())) ? "[CONFLICT]" : "[" + file.policy() + "]";
                    return status + " " + normalizePath(file.path());
                })
                .toList();
    }

    private List<String> collisions(List<FileToWrite> files) {
        return files.stream()
                .filter(file -> file.policy() == FileToWrite.WritePolicy.CREATE)
                .filter(file -> writer.exists(root.resolve(file.path())))
                .map(file -> normalizePath(file.path()))
                .toList();
    }

    private boolean confirmOverwrite(List<String> collisions) {
        Console console = System.console();
        if (console == null) {
            return false;
        }
        console.printf("Se sobrescribiran estos archivos:%n");
        collisions.forEach(path -> console.printf("- %s%n", path));
        String answer = console.readLine("Confirmar overwrite? y/N: ");
        return "y".equals(answer.toLowerCase(Locale.ROOT));
    }

    private String normalizePath(Path path) {
        return path.toString().replace('\\', '/');
    }

    private String trimSlashes(String value) {
        String normalized = value.replace('\\', '/');
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
