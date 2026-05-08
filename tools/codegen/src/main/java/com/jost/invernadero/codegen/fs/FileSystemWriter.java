package com.jost.invernadero.codegen.fs;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSystemWriter {

    public boolean exists(Path path) {
        return Files.exists(path);
    }

    public void writeIfAbsent(Path path, String content) {
        if (exists(path)) {
            throw new IllegalStateException("archivo ya existe: " + path);
        }
        write(path, content);
    }

    public void writeOverwriting(Path path, String content) {
        write(path, content);
    }

    private void write(Path path, String content) {
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, normalizeLf(content), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo escribir " + path, ex);
        }
    }

    private String normalizeLf(String content) {
        return content.replace("\r\n", "\n").replace("\r", "\n");
    }
}
