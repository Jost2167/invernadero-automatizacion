package com.jost.invernadero.codegen.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExamplesDiscoveryTest {

    @TempDir
    Path tempDir;

    @Test
    void discoversJsonFilesInAlphabeticalOrder() throws IOException {
        Files.writeString(tempDir.resolve("Zebra.json"), validJson("Zebra", "zebras"));
        Files.writeString(tempDir.resolve("Alpha.json"), validJson("Alpha", "alphas"));
        Files.writeString(tempDir.resolve("Mango.json"), validJson("Mango", "mangos"));

        List<Path> found = ExportExamplesPdfCommand.discoverJsonFiles(tempDir);

        assertThat(found)
                .extracting(p -> p.getFileName().toString())
                .containsExactly("Alpha.json", "Mango.json", "Zebra.json");
    }

    @Test
    void excludesNonJsonFiles() throws IOException {
        Files.writeString(tempDir.resolve("Entity.json"), validJson("Entity", "entities"));
        Files.writeString(tempDir.resolve("notes.txt"), "ignore");
        Files.writeString(tempDir.resolve("README.md"), "# readme");

        List<Path> found = ExportExamplesPdfCommand.discoverJsonFiles(tempDir);

        assertThat(found)
                .extracting(p -> p.getFileName().toString())
                .containsExactly("Entity.json");
    }

    @Test
    void excludesNestedDirectories() throws IOException {
        Files.writeString(tempDir.resolve("Root.json"), validJson("Root", "roots"));
        Path subdir = Files.createDirectory(tempDir.resolve("subdir"));
        Files.writeString(subdir.resolve("Nested.json"), validJson("Nested", "nesteds"));

        List<Path> found = ExportExamplesPdfCommand.discoverJsonFiles(tempDir);

        assertThat(found)
                .extracting(p -> p.getFileName().toString())
                .containsExactly("Root.json");
    }

    @Test
    void returnsEmptyListForEmptyDirectory() throws IOException {
        List<Path> found = ExportExamplesPdfCommand.discoverJsonFiles(tempDir);
        assertThat(found).isEmpty();
    }

    @Test
    void throwsWhenDirectoryDoesNotExist() {
        Path missing = tempDir.resolve("nonexistent");

        assertThatThrownBy(() -> ExportExamplesPdfCommand.discoverJsonFiles(missing))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("no existe");
    }

    private String validJson(String name, String tableName) {
        return """
                {
                  "version": "1",
                  "name": "%s",
                  "tableName": "%s",
                  "fields": [{"name": "id", "type": "Long", "nullable": false}]
                }
                """.formatted(name, tableName);
    }
}
