package com.jost.invernadero.codegen.generator;

public record InjectionResult(boolean success, String content, String error) {

    public static InjectionResult success(String content) {
        return new InjectionResult(true, content, null);
    }

    public static InjectionResult error(String error) {
        return new InjectionResult(false, null, error);
    }
}
