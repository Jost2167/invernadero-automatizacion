package com.jost.invernadero.taigasync.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaigaClientTest {

    private HttpServer server;
    private String baseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<String> seenAuthorizationHeaders = new ArrayList<>();
    private final List<String> postedComments = new ArrayList<>();
    private final List<String> patchedStories = new ArrayList<>();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void callsTaigaEndpointsWithBearerTokenAndParsesResponses() throws Exception {
        server.createContext("/api/v1/projects/by_slug", exchange -> {
            assertRequest(exchange, "GET", "slug=greenhouse");
            respondJson(exchange, 200, "{\"id\":42}");
        });
        server.createContext("/api/v1/userstory-custom-attributes", exchange -> {
            assertRequest(exchange, "GET", "project=42");
            respondJson(exchange, 200, "[{\"id\":77,\"name\":\"codegen_json\"}]");
        });
        server.createContext("/api/v1/userstories", exchange -> {
            assertRequest(exchange, "GET", "project=42&tags=codegen-schema");
            respondJson(exchange, 200, "[{\"id\":10,\"ref\":101,\"subject\":\"Sensor schema\"}]");
        });
        server.createContext("/api/v1/userstories/custom-attributes-values/10", exchange -> {
            assertRequest(exchange, "GET", null);
            respondJson(exchange, 200, "{\"attributes_values\":{\"77\":\"{\\\"name\\\":\\\"Sensor\\\"}\"}}");
        });
        server.createContext("/api/v1/userstories/10", exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                assertRequest(exchange, "GET", null);
                respondJson(exchange, 200, "{\"version\":5}");
                return;
            }

            assertRequest(exchange, "PATCH", null);
            postedComments.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respondJson(exchange, 200, "{}");
        });

        TaigaClient client = new TaigaClient(baseUrl, "secret-token");

        assertThat(client.getProjectId("greenhouse")).isEqualTo(42L);
        assertThat(client.getCodegenFieldId(42L)).isEqualTo(77L);
        assertThat(client.getTaggedStories(42L, "codegen-schema"))
                .containsExactly(new StoryRef(10L, 101L, "Sensor schema"));
        Map<String, JsonNode> attributes = client.getStoryAttributes(10L);
        assertThat(attributes.get("77").asText()).isEqualTo("{\"name\":\"Sensor\"}");

        client.postComment(10L, "validation failed");

        assertThat(seenAuthorizationHeaders).allMatch("Bearer secret-token"::equals);
        assertThat(postedComments).singleElement()
                .satisfies(body -> assertThat(body)
                        .contains("\"comment\":\"validation failed\"")
                        .contains("\"version\":5"));
    }

    @Test
    void getDoneStatusIdReturnsStatusNamedDone() throws Exception {
        server.createContext("/api/v1/userstory-statuses", exchange -> {
            assertRequest(exchange, "GET", "project=42");
            respondJson(exchange, 200, "["
                    + "{\"id\":1,\"name\":\"Ready\",\"is_closed\":false},"
                    + "{\"id\":2,\"name\":\"Closed\",\"is_closed\":true},"
                    + "{\"id\":3,\"name\":\"Done\",\"is_closed\":false}"
                    + "]");
        });

        TaigaClient client = new TaigaClient(baseUrl, "secret-token");

        assertThat(client.getDoneStatusId(42L)).isEqualTo(3L);
    }

    @Test
    void getDoneStatusIdFallsBackToFirstClosedStatus() throws Exception {
        server.createContext("/api/v1/userstory-statuses", exchange -> {
            assertRequest(exchange, "GET", "project=42");
            respondJson(exchange, 200, "["
                    + "{\"id\":1,\"name\":\"Ready\",\"is_closed\":false},"
                    + "{\"id\":2,\"name\":\"Released\",\"is_closed\":true},"
                    + "{\"id\":3,\"name\":\"Archived\",\"is_closed\":true}"
                    + "]");
        });

        TaigaClient client = new TaigaClient(baseUrl, "secret-token");

        assertThat(client.getDoneStatusId(42L)).isEqualTo(2L);
    }

    @Test
    void getDoneStatusIdThrowsWhenNoDoneOrClosedStatusExists() {
        server.createContext("/api/v1/userstory-statuses", exchange -> {
            assertRequest(exchange, "GET", "project=42");
            respondJson(exchange, 200, "[{\"id\":1,\"name\":\"Ready\",\"is_closed\":false}]");
        });

        TaigaClient client = new TaigaClient(baseUrl, "secret-token");

        assertThatThrownBy(() -> client.getDoneStatusId(42L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("project 42");
    }

    @Test
    void markAsDonePatchesStoryStatusWithCurrentVersion() throws Exception {
        server.createContext("/api/v1/userstories/10", exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                assertRequest(exchange, "GET", null);
                respondJson(exchange, 200, "{\"version\":5}");
                return;
            }

            assertRequest(exchange, "PATCH", null);
            patchedStories.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respondJson(exchange, 200, "{}");
        });

        TaigaClient client = new TaigaClient(baseUrl, "secret-token");

        client.markAsDone(10L, 99L);

        assertThat(patchedStories).singleElement()
                .satisfies(body -> {
                    JsonNode json = objectMapper.readTree(body);
                    assertThat(json.get("status").asLong()).isEqualTo(99L);
                    assertThat(json.get("version").asLong()).isEqualTo(5L);
                });
    }

    @Test
    void markAsDoneThrowsIOExceptionWhenPatchFails() {
        server.createContext("/api/v1/userstories/10", exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                assertRequest(exchange, "GET", null);
                respondJson(exchange, 200, "{\"version\":5}");
                return;
            }

            assertRequest(exchange, "PATCH", null);
            respondJson(exchange, 500, "{\"detail\":\"boom\"}");
        });

        TaigaClient client = new TaigaClient(baseUrl, "secret-token");

        assertThatThrownBy(() -> client.markAsDone(10L, 99L))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("PATCH")
                .hasMessageContaining("HTTP 500");
    }

    private void assertRequest(HttpExchange exchange, String method, String query) {
        assertThat(exchange.getRequestMethod()).isEqualTo(method);
        assertThat(exchange.getRequestURI().getRawQuery()).isEqualTo(query);
        seenAuthorizationHeaders.add(exchange.getRequestHeaders().getFirst("Authorization"));
    }

    private static void respondJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
