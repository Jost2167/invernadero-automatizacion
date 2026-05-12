package com.jost.invernadero.taigasync.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Configuration for the Taiga synchronization process.
 *
 * <p>{@code TAIGA_CODEGEN_FIELD_ID} is optional. When it is absent, the sync
 * pipeline must resolve the {@code codegen_json} custom field by name through
 * the Taiga API.
 */
public final class SyncConfig {

    private static final String TAIGA_BASE_URL = "TAIGA_BASE_URL";
    private static final String TAIGA_AUTH_TOKEN = "TAIGA_AUTH_TOKEN";
    private static final String TAIGA_PROJECT_SLUG = "TAIGA_PROJECT_SLUG";
    private static final String TAIGA_CODEGEN_FIELD_ID = "TAIGA_CODEGEN_FIELD_ID";

    private final String baseUrl;
    private final String authToken;
    private final String projectSlug;
    private final Long codegenFieldId;

    private SyncConfig(String baseUrl, String authToken, String projectSlug, Long codegenFieldId) {
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
        this.authToken = Objects.requireNonNull(authToken, "authToken");
        this.projectSlug = Objects.requireNonNull(projectSlug, "projectSlug");
        this.codegenFieldId = codegenFieldId;
    }

    public static SyncConfig fromEnvironment() {
        return from(System.getenv());
    }

    public static SyncConfig from(Map<String, String> environment) {
        Objects.requireNonNull(environment, "environment");

        List<String> missingVariables = new ArrayList<>();
        String baseUrl = required(environment, TAIGA_BASE_URL, missingVariables);
        String authToken = required(environment, TAIGA_AUTH_TOKEN, missingVariables);
        String projectSlug = required(environment, TAIGA_PROJECT_SLUG, missingVariables);

        if (!missingVariables.isEmpty()) {
            throw new IllegalStateException("Missing required environment variables: "
                    + String.join(", ", missingVariables));
        }

        Long codegenFieldId = optionalLong(environment, TAIGA_CODEGEN_FIELD_ID);
        return new SyncConfig(baseUrl, authToken, projectSlug, codegenFieldId);
    }

    public String baseUrl() {
        return baseUrl;
    }

    public String authToken() {
        return authToken;
    }

    public String projectSlug() {
        return projectSlug;
    }

    public Optional<Long> codegenFieldId() {
        return Optional.ofNullable(codegenFieldId);
    }

    private static String required(Map<String, String> environment, String variableName, List<String> missingVariables) {
        String value = trimToNull(environment.get(variableName));
        if (value == null) {
            missingVariables.add(variableName);
        }
        return value;
    }

    private static Long optionalLong(Map<String, String> environment, String variableName) {
        String value = trimToNull(environment.get(variableName));
        if (value == null) {
            return null;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Environment variable " + variableName
                    + " must be a numeric Taiga custom field ID.", exception);
        }
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
