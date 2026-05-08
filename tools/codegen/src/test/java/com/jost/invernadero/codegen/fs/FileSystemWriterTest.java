package com.jost.invernadero.codegen.fs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileSystemWriterTest {

    @TempDir
    Path tempDir;

    private final FileSystemWriter writer = new FileSystemWriter();

    @Test
    void writesUtf8AndNormalizesLineEndings() throws IOException {
        Path file = tempDir.resolve("nested/file.txt");

        writer.writeIfAbsent(file, "uno\r\ndos\rtres\n");

        assertThat(writer.exists(file)).isTrue();
        assertThat(Files.readString(file, StandardCharsets.UTF_8)).isEqualTo("uno\ndos\ntres\n");
    }

    @Test
    void writeIfAbsentFailsWhenFileExistsAndOverwriteReplacesIt() throws IOException {
        Path file = tempDir.resolve("file.txt");
        writer.writeIfAbsent(file, "first");

        assertThatThrownBy(() -> writer.writeIfAbsent(file, "second"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("archivo ya existe");

        writer.writeOverwriting(file, "second");

        assertThat(Files.readString(file, StandardCharsets.UTF_8)).isEqualTo("second");
    }
}
