package com.jost.invernadero.taigasync.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SyncConfigTest {

    @Test
    void failsWithDescriptiveMessageWhenRequiredVariablesAreMissing() {
        assertThatThrownBy(() -> SyncConfig.from(Map.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Missing required environment variables")
                .hasMessageContaining("TAIGA_BASE_URL")
                .hasMessageContaining("TAIGA_AUTH_TOKEN")
                .hasMessageContaining("TAIGA_PROJECT_SLUG");
    }

    @Test
    void treatsBlankRequiredVariablesAsMissing() {
        Map<String, String> environment = new LinkedHashMap<>();
        environment.put("TAIGA_BASE_URL", "https://taiga.example.com");
        environment.put("TAIGA_AUTH_TOKEN", "token");
        environment.put("TAIGA_PROJECT_SLUG", " ");

        assertThatThrownBy(() -> SyncConfig.from(environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("TAIGA_PROJECT_SLUG");
    }

    @Test
    void keepsCodegenFieldIdOptional() {
        SyncConfig config = SyncConfig.from(Map.of(
                "TAIGA_BASE_URL", "https://taiga.example.com",
                "TAIGA_AUTH_TOKEN", "token",
                "TAIGA_PROJECT_SLUG", "greenhouse"));

        assertThat(config.codegenFieldId()).isEmpty();
    }

    @Test
    void parsesOptionalCodegenFieldIdWhenPresent() {
        SyncConfig config = SyncConfig.from(Map.of(
                "TAIGA_BASE_URL", "https://taiga.example.com",
                "TAIGA_AUTH_TOKEN", "token",
                "TAIGA_PROJECT_SLUG", "greenhouse",
                "TAIGA_CODEGEN_FIELD_ID", "77"));

        assertThat(config.codegenFieldId()).contains(77L);
    }
}
