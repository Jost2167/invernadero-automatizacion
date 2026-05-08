package com.jost.invernadero.codegen.generator;

import java.util.List;

public record GenerationResult(boolean success, List<FileToWrite> files, List<String> messages) {

    public GenerationResult {
        files = files == null ? List.of() : List.copyOf(files);
        messages = messages == null ? List.of() : List.copyOf(messages);
    }

    public static GenerationResult success(List<FileToWrite> files, List<String> messages) {
        return new GenerationResult(true, files, messages);
    }

    public static GenerationResult error(List<FileToWrite> files, List<String> messages) {
        return new GenerationResult(false, files, messages);
    }
}
