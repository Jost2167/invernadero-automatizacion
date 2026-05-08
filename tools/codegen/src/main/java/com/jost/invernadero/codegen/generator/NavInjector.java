package com.jost.invernadero.codegen.generator;

public class NavInjector {

    private static final String MARKER = "// codegen:nav";

    public InjectionResult inject(String sidebarContent, String entityKey, String entityKebab) {
        if (!sidebarContent.contains(MARKER)) {
            return InjectionResult.error("Sidebar.jsx no contiene el marcador `// codegen:nav`. "
                    + "Agregar el comentario al final del array NAV_MODULES y reintentar.");
        }

        if (sidebarContent.contains("path: '/" + entityKebab + "'")) {
            return InjectionResult.success(sidebarContent);
        }

        String newline = sidebarContent.contains("\r\n") ? "\r\n" : "\n";

        String markerLine = sidebarContent.lines()
                .filter(line -> line.contains(MARKER))
                .findFirst()
                .orElse("  " + MARKER);
        String indent = markerLine.substring(0, markerLine.indexOf(MARKER));

        String entry = indent + "{ key: '" + entityKey + "', path: '/" + entityKebab + "' },"
                + newline + markerLine;

        return InjectionResult.success(sidebarContent.replace(markerLine, entry));
    }
}
