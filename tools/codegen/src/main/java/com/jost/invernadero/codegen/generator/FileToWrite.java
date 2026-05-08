package com.jost.invernadero.codegen.generator;

import java.nio.file.Path;

public record FileToWrite(Path path, String content, WritePolicy policy) {

    public enum WritePolicy {
        CREATE,
        UPDATE
    }
}
