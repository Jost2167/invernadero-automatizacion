package com.jost.invernadero.codegen.validator;

import com.networknt.schema.ValidationMessage;
import java.util.List;

public record ValidationReport(List<Error> errors, List<Warning> warnings) {

    public ValidationReport {
        errors = errors == null ? List.of() : List.copyOf(errors);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public static ValidationReport ok() {
        return new ValidationReport(List.of(), List.of());
    }

    public static ValidationReport fromSchemaMessages(List<ValidationMessage> messages) {
        return new ValidationReport(messages.stream()
                .map(message -> new Error(normalizePath(message.getPath()), message.getMessage()))
                .toList(), List.of());
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public int exitCode() {
        return hasErrors() ? 1 : 0;
    }

    public String format() {
        if (errors.isEmpty() && warnings.isEmpty()) {
            return "Validation passed.";
        }

        StringBuilder output = new StringBuilder();
        appendGroup(output, "Errors", errors);
        appendGroup(output, "Warnings", warnings);
        return output.toString().stripTrailing();
    }

    private static void appendGroup(StringBuilder output, String title, List<? extends Entry> entries) {
        if (entries.isEmpty()) {
            return;
        }
        if (!output.isEmpty()) {
            output.append(System.lineSeparator()).append(System.lineSeparator());
        }
        output.append(title).append(':');
        for (Entry entry : entries) {
            output.append(System.lineSeparator())
                    .append("- ")
                    .append(entry.path())
                    .append(": ")
                    .append(entry.message());
        }
    }

    private static String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "$";
        }
        return path.startsWith("$") ? path : "$." + path;
    }

    private sealed interface Entry permits Error, Warning {
        String path();

        String message();
    }

    public record Error(String path, String message) implements Entry {
    }

    public record Warning(String path, String message) implements Entry {
    }
}
