package com.jost.invernadero.codegen.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

class ExportExamplesPdfCommandTest {

    @TempDir
    Path tempDir;

    @Test
    void successfulExportCreatesPdfAndReturnsExitZero() throws IOException {
        Path examplesDir = prepareExamplesDir("Alpha.json", validJson("Alpha", "alphas"));
        Path output = tempDir.resolve("output").resolve("entities.pdf");

        StringWriter stdout = new StringWriter();
        int exitCode = commandLine(stdout, new StringWriter()).execute(
                "export-examples-pdf",
                "--examples-dir", examplesDir.toString(),
                "--output", output.toString());

        assertThat(exitCode).isEqualTo(0);
        assertThat(stdout.toString()).contains("PDF exportado");
        assertThat(Files.exists(output)).isTrue();
    }

    @Test
    void invalidJsonPreventsExportAndReturnsExitOne() throws IOException {
        Path examplesDir = tempDir.resolve("examples");
        Files.createDirectories(examplesDir);
        Files.writeString(examplesDir.resolve("Good.json"), validJson("Good", "goods"));
        // "Double" is not a valid field type — fails JSON schema validation
        Files.writeString(examplesDir.resolve("Bad.json"), """
                {"version":"1","name":"Bad","tableName":"bad_table","fields":[{"name":"x","type":"Double"}]}
                """);
        Path output = tempDir.resolve("entities.pdf");

        StringWriter stdout = new StringWriter();
        int exitCode = commandLine(stdout, new StringWriter()).execute(
                "export-examples-pdf",
                "--examples-dir", examplesDir.toString(),
                "--output", output.toString());

        assertThat(exitCode).isEqualTo(1);
        assertThat(stdout.toString()).contains("Bad.json");
        assertThat(Files.exists(output)).isFalse();
    }

    @Test
    void pdfContainsEntityNameTableNameAndSourceFileName() throws IOException {
        Path examplesDir = prepareExamplesDir("Greenhouse.json", validJson("Greenhouse", "greenhouses"));
        Path output = tempDir.resolve("entities.pdf");

        commandLine(new StringWriter(), new StringWriter()).execute(
                "export-examples-pdf",
                "--examples-dir", examplesDir.toString(),
                "--output", output.toString());

        assertThat(Files.exists(output)).isTrue();
        try (PDDocument doc = PDDocument.load(output.toFile())) {
            String text = new PDFTextStripper().getText(doc);
            assertThat(text).contains("Greenhouse");
            assertThat(text).contains("greenhouses");
            assertThat(text).contains("Greenhouse.json");
        }
    }

    @Test
    void pdfContainsAllEntitiesWhenMultipleFilesArePresent() throws IOException {
        Path examplesDir = tempDir.resolve("examples");
        Files.createDirectories(examplesDir);
        Files.writeString(examplesDir.resolve("Alpha.json"), validJson("Alpha", "alphas"));
        Files.writeString(examplesDir.resolve("Beta.json"), validJson("Beta", "betas"));
        Path output = tempDir.resolve("entities.pdf");

        int exitCode = commandLine(new StringWriter(), new StringWriter()).execute(
                "export-examples-pdf",
                "--examples-dir", examplesDir.toString(),
                "--output", output.toString());

        assertThat(exitCode).isEqualTo(0);
        try (PDDocument doc = PDDocument.load(output.toFile())) {
            String text = new PDFTextStripper().getText(doc);
            assertThat(text).contains("Alpha").contains("Beta");
        }
    }

    private Path prepareExamplesDir(String fileName, String content) throws IOException {
        Path dir = tempDir.resolve("examples");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve(fileName), content);
        return dir;
    }

    private CommandLine commandLine(StringWriter stdout, StringWriter stderr) {
        CommandLine cl = new CommandLine(new CodegenCli());
        cl.setOut(new PrintWriter(stdout, true));
        cl.setErr(new PrintWriter(stderr, true));
        return cl;
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
