package com.jost.invernadero.taigasync.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class TaigaClient {

    private static final String CODEGEN_FIELD_NAME = "codegen_json";

    private final URI baseUri;
    private final String authToken;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TaigaClient(String baseUrl, String authToken) {
        this(baseUrl, authToken, HttpClient.newHttpClient(), new ObjectMapper());
    }

    public TaigaClient(String baseUrl, String authToken, HttpClient httpClient, ObjectMapper objectMapper) {
        this.baseUri = normalizeBaseUri(baseUrl);
        this.authToken = requireText(authToken, "authToken");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    public long getProjectId(String slug) throws IOException, InterruptedException {
        URI uri = uri("api/v1/projects/by_slug", query("slug", requireText(slug, "slug")));
        JsonNode project = getJson(uri);
        return requiredLong(project, "id", "Taiga project response did not include a numeric id.");
    }

    public long getCodegenFieldId(long projectId) throws IOException, InterruptedException {
        URI uri = uri("api/v1/userstory-custom-attributes", query("project", Long.toString(projectId)));
        JsonNode attributes = getJson(uri);

        if (!attributes.isArray()) {
            throw new IOException("Taiga userstory attributes response was not a JSON array.");
        }

        for (JsonNode attribute : attributes) {
            if (CODEGEN_FIELD_NAME.equals(attribute.path("name").asText())) {
                return requiredLong(attribute, "id", "Taiga codegen_json attribute did not include a numeric id.");
            }
        }

        throw new IllegalStateException("Taiga custom field '" + CODEGEN_FIELD_NAME
                + "' was not found for project " + projectId + ".");
    }

    public long getDoneStatusId(long projectId) throws IOException, InterruptedException {
        URI uri = uri("api/v1/userstory-statuses", query("project", Long.toString(projectId)));
        JsonNode statuses = getJson(uri);

        if (!statuses.isArray()) {
            throw new IOException("Taiga userstory statuses response was not a JSON array.");
        }

        Long firstClosedStatusId = null;
        for (JsonNode status : statuses) {
            long id = requiredLong(status, "id", "Taiga user story status did not include a numeric id.");
            if ("Done".equals(status.path("name").asText())) {
                return id;
            }
            if (firstClosedStatusId == null && status.path("is_closed").asBoolean(false)) {
                firstClosedStatusId = id;
            }
        }

        if (firstClosedStatusId != null) {
            return firstClosedStatusId;
        }

        throw new IllegalStateException("Taiga Done status was not found for project " + projectId + ".");
    }

    public List<StoryRef> getTaggedStories(long projectId, String tag) throws IOException, InterruptedException {
        URI uri = uri("api/v1/userstories", query(
                "project", Long.toString(projectId),
                "tags", requireText(tag, "tag")));
        JsonNode stories = getJson(uri);

        if (!stories.isArray()) {
            throw new IOException("Taiga userstories response was not a JSON array.");
        }

        List<StoryRef> result = new ArrayList<>();
        for (JsonNode story : stories) {
            long id = requiredLong(story, "id", "Taiga user story did not include a numeric id.");
            long ref = requiredLong(story, "ref", "Taiga user story did not include a numeric ref.");
            String subject = requiredText(story, "subject", "Taiga user story did not include a subject.");
            result.add(new StoryRef(id, ref, subject));
        }
        return List.copyOf(result);
    }

    public Map<String, JsonNode> getStoryAttributes(long storyId) throws IOException, InterruptedException {
        URI uri = uri("api/v1/userstories/custom-attributes-values/" + storyId);
        JsonNode response = getJson(uri);
        JsonNode attributes = response.has("attributes_values") ? response.get("attributes_values") : response;

        if (!attributes.isObject()) {
            throw new IOException("Taiga custom attributes response was not a JSON object.");
        }

        Map<String, JsonNode> result = new LinkedHashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = attributes.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            result.put(field.getKey(), field.getValue());
        }
        return Collections.unmodifiableMap(result);
    }

    public void postComment(long storyId, String text) throws IOException, InterruptedException {
        long version = getStoryVersion(storyId);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("comment", requireText(text, "text"));
        body.put("version", version);

        HttpRequest request = baseRequest(uri("api/v1/userstories/" + storyId))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();
        send(request);
    }

    public void markAsDone(long storyId, long doneStatusId) throws IOException, InterruptedException {
        long version = getStoryVersion(storyId);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("status", doneStatusId);
        body.put("version", version);

        HttpRequest request = baseRequest(uri("api/v1/userstories/" + storyId))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();
        send(request);
    }

    private long getStoryVersion(long storyId) throws IOException, InterruptedException {
        JsonNode story = getJson(uri("api/v1/userstories/" + storyId));
        return requiredLong(story, "version", "Taiga user story did not include a numeric version.");
    }

    private JsonNode getJson(URI uri) throws IOException, InterruptedException {
        HttpRequest request = baseRequest(uri).GET().build();
        String body = send(request);
        return objectMapper.readTree(body);
    }

    private String send(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            throw new IOException("Taiga API request failed: " + request.method() + " " + request.uri()
                    + " returned HTTP " + status + " with body: " + truncate(response.body()));
        }
        return response.body();
    }

    private HttpRequest.Builder baseRequest(URI uri) {
        return HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + authToken);
    }

    private URI uri(String path) {
        return uri(path, Map.of());
    }

    private URI uri(String path, Map<String, String> queryParameters) {
        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        URI endpoint = baseUri.resolve(cleanPath);
        if (queryParameters.isEmpty()) {
            return endpoint;
        }

        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : queryParameters.entrySet()) {
            if (query.length() > 0) {
                query.append('&');
            }
            query.append(encode(entry.getKey())).append('=').append(encode(entry.getValue()));
        }
        return URI.create(endpoint + "?" + query);
    }

    private static Map<String, String> query(String... pairs) {
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Query parameters must be provided as name/value pairs.");
        }

        Map<String, String> query = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            query.put(pairs[i], pairs[i + 1]);
        }
        return query;
    }

    private static URI normalizeBaseUri(String baseUrl) {
        String value = requireText(baseUrl, "baseUrl");
        if (!value.endsWith("/")) {
            value += "/";
        }
        return URI.create(value);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static long requiredLong(JsonNode node, String fieldName, String message) throws IOException {
        JsonNode value = node.get(fieldName);
        if (value == null || !value.isIntegralNumber()) {
            throw new IOException(message);
        }
        return value.asLong();
    }

    private static String requiredText(JsonNode node, String fieldName, String message) throws IOException {
        JsonNode value = node.get(fieldName);
        if (value == null || !value.isTextual() || value.asText().isBlank()) {
            throw new IOException(message);
        }
        return value.asText();
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
        return value.trim();
    }

    private static String truncate(String body) {
        if (body == null || body.length() <= 500) {
            return body;
        }
        return body.substring(0, 500) + "...";
    }
}
