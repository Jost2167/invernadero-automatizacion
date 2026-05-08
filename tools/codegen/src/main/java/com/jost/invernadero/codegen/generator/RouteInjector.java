package com.jost.invernadero.codegen.generator;

public class RouteInjector {

    private static final String MARKER = "// codegen:routes";

    public InjectionResult inject(String appContent, String entityName, String entityKebab) {
        if (!appContent.contains(MARKER)) {
            return InjectionResult.error("App.jsx no contiene el marcador `// codegen:routes`. "
                    + "Anadir el comentario donde se desean insertar rutas y reintentar.");
        }

        String newline = appContent.contains("\r\n") ? "\r\n" : "\n";
        String updated = ensureImport(appContent,
                "import " + entityName + "ListPage from './pages/" + entityKebab + "/" + entityName + "ListPage.jsx'",
                newline);
        updated = ensureImport(updated,
                "import " + entityName + "FormPage from './pages/" + entityKebab + "/" + entityName + "FormPage.jsx'",
                newline);

        if (updated.contains("path=\"/" + entityKebab + "\"")
                && updated.contains("path=\"/" + entityKebab + "/:id\"")) {
            return InjectionResult.success(updated);
        }

        String markerLine = updated.lines()
                .filter(line -> line.contains(MARKER))
                .findFirst()
                .orElse("        " + MARKER);
        String indent = markerLine.substring(0, markerLine.indexOf(MARKER));
        String routeBlock = ""
                + indent + "<Route path=\"/" + entityKebab + "\" element={<" + entityName + "ListPage />} />"
                + newline
                + indent + "<Route path=\"/" + entityKebab + "/:id\" element={<" + entityName + "FormPage />} />"
                + newline
                + markerLine;

        return InjectionResult.success(updated.replace(markerLine, routeBlock));
    }

    private String ensureImport(String content, String importLine, String newline) {
        if (content.contains(importLine)) {
            return content;
        }

        // Track actual byte positions using real \n offsets — avoids drift
        // when detected newline length doesn't match actual line endings.
        int insertAt = 0;
        int pos = 0;
        while (pos < content.length()) {
            int nlIdx = content.indexOf('\n', pos);
            if (nlIdx < 0) break;
            int lineEnd = nlIdx > 0 && content.charAt(nlIdx - 1) == '\r' ? nlIdx - 1 : nlIdx;
            String line = content.substring(pos, lineEnd);
            if (line.startsWith("import ")) {
                insertAt = nlIdx + 1;
            }
            pos = nlIdx + 1;
        }
        return content.substring(0, insertAt) + importLine + newline + content.substring(insertAt);
    }
}
