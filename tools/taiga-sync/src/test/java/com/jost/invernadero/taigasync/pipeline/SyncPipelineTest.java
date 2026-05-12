package com.jost.invernadero.taigasync.pipeline;

import static org.assertj.core.api.Assertions.assertThat;

import com.jost.invernadero.taigasync.client.TaigaClient;
import com.jost.invernadero.taigasync.config.SyncConfig;
import com.jost.invernadero.taigasync.validation.SchemaValidator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SyncPipelineTest {

    @TempDir
    private Path examplesDirectory;

    private HttpServer server;
    private String baseUrl;
    private final List<String> postedComments = new ArrayList<>();

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
    void reportsNameCollisionsBeforeWritingAnySchema() throws Exception {
        server.createContext("/api/v1/projects/by_slug", exchange -> respondJson(exchange, "{\"id\":42}"));
        server.createContext("/api/v1/userstory-statuses", exchange -> respondJson(exchange, doneStatusesBody()));
        server.createContext("/api/v1/userstories", exchange -> respondJson(exchange, """
                [
                  {"id":10,"ref":101,"subject":"Sensor one"},
                  {"id":11,"ref":102,"subject":"Sensor two"}
                ]
                """));
        server.createContext("/api/v1/userstories/custom-attributes-values/10",
                exchange -> respondJson(exchange, attributesBody("Sensor")));
        server.createContext("/api/v1/userstories/custom-attributes-values/11",
                exchange -> respondJson(exchange, attributesBody("Sensor")));
        server.createContext("/api/v1/userstories/10", exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                respondJson(exchange, "{\"version\":3}");
                return;
            }
            captureComment(exchange);
        });
        server.createContext("/api/v1/userstories/11", exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                respondJson(exchange, "{\"version\":4}");
                return;
            }
            captureComment(exchange);
        });

        SyncConfig config = SyncConfig.from(Map.of(
                "TAIGA_BASE_URL", baseUrl,
                "TAIGA_AUTH_TOKEN", "token",
                "TAIGA_PROJECT_SLUG", "greenhouse",
                "TAIGA_CODEGEN_FIELD_ID", "77"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        SyncPipeline pipeline = new SyncPipeline(
                config,
                new TaigaClient(baseUrl, "token"),
                new SchemaValidator(),
                new com.fasterxml.jackson.databind.ObjectMapper(),
                examplesDirectory,
                new PrintStream(output, true, StandardCharsets.UTF_8));

        SyncResult result = pipeline.run(false);

        assertThat(result.processedStories()).isEqualTo(2);
        assertThat(result.collisionFailures()).isEqualTo(2);
        assertThat(result.schemasWritten()).isZero();
        assertThat(Files.exists(examplesDirectory.resolve("Sensor.json"))).isFalse();
        assertThat(postedComments).hasSize(2)
                .allSatisfy(body -> assertThat(body)
                        .contains("Entity name collision")
                        .contains("#101")
                        .contains("#102"));
    }

    @Test
    void dryRunPrintsActionsWithoutWritingOrPostingComments() throws Exception {
        server.createContext("/api/v1/projects/by_slug", exchange -> respondJson(exchange, "{\"id\":42}"));
        server.createContext("/api/v1/userstory-statuses", exchange -> respondJson(exchange, doneStatusesBody()));
        server.createContext("/api/v1/userstories", exchange -> respondJson(exchange, """
                [{"id":10,"ref":101,"subject":"Sensor schema"}]
                """));
        server.createContext("/api/v1/userstories/custom-attributes-values/10",
                exchange -> respondJson(exchange, attributesBody("Sensor")));

        SyncConfig config = SyncConfig.from(Map.of(
                "TAIGA_BASE_URL", baseUrl,
                "TAIGA_AUTH_TOKEN", "token",
                "TAIGA_PROJECT_SLUG", "greenhouse",
                "TAIGA_CODEGEN_FIELD_ID", "77"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        SyncPipeline pipeline = new SyncPipeline(
                config,
                new TaigaClient(baseUrl, "token"),
                new SchemaValidator(),
                new com.fasterxml.jackson.databind.ObjectMapper(),
                examplesDirectory,
                new PrintStream(output, true, StandardCharsets.UTF_8));

        SyncResult result = pipeline.run(true);

        assertThat(result.schemasWritten()).isZero();
        assertThat(Files.exists(examplesDirectory.resolve("Sensor.json"))).isFalse();
        assertThat(output.toString(StandardCharsets.UTF_8))
                .contains("[dry-run] Se escribiria el schema")
                .contains("[dry-run] Se marcaría la historia #101 como Done en Taiga.");
        assertThat(postedComments).isEmpty();
    }

    private void captureComment(HttpExchange exchange) throws IOException {
        postedComments.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        respondJson(exchange, "{}");
    }

    private static String attributesBody(String name) {
        String schema = """
                {
                  "version": "1",
                  "name": "%s",
                  "tableName": "sensors",
                  "fields": [
                    {"name": "id", "type": "Long", "nullable": false}
                  ],
                  "relations": []
                }
                """.formatted(name).replace("\"", "\\\"").replace("\r", "").replace("\n", "\\n");
        return "{\"attributes_values\":{\"77\":\"" + schema + "\"}}";
    }

    private static String doneStatusesBody() {
        return "[{\"id\":9,\"name\":\"Done\",\"is_closed\":true}]";
    }

    private static void respondJson(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
