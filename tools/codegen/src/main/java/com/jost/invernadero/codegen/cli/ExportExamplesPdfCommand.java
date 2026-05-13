package com.jost.invernadero.codegen.cli;

import com.jost.invernadero.codegen.pdf.PdfExporter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(
        name = "export-examples-pdf",
        description = "Exporta todas las definiciones de ejemplo a un PDF consolidado")
public class ExportExamplesPdfCommand implements Callable<Integer> {

    @Option(
            names = "--examples-dir",
            defaultValue = "tools/codegen/examples",
            description = "Directorio de ejemplos JSON. Default: ${DEFAULT-VALUE}")
    private Path examplesDir;

    @Option(
            names = "--output",
            defaultValue = "tools/codegen/examples-export.pdf",
            description = "Ruta del PDF generado. Default: ${DEFAULT-VALUE}")
    private Path output;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() {
        PrintWriter out = spec.commandLine().getOut();
        PrintWriter err = spec.commandLine().getErr();
        try {
            List<Path> jsonFiles = discoverJsonFiles(examplesDir);
            if (jsonFiles.isEmpty()) {
                out.println("No se encontraron archivos JSON en: " + examplesDir);
                return 0;
            }

            List<PdfExporter.EntityWithSource> validated = new ArrayList<>();
            boolean hasErrors = false;
            for (Path file : jsonFiles) {
                CliSupport.ValidationOutcome outcome = CliSupport.validate(file);
                if (outcome.report().hasErrors()) {
                    out.println("Archivo invalido: " + file.getFileName());
                    out.println(outcome.report().format());
                    hasErrors = true;
                } else {
                    validated.add(new PdfExporter.EntityWithSource(
                            file.getFileName().toString(), outcome.definition()));
                }
            }
            if (hasErrors) {
                return 1;
            }

            new PdfExporter().export(validated, output);
            out.println("PDF exportado: " + output);
            return 0;

        } catch (IOException ex) {
            err.println(ex.getMessage());
            return 2;
        }
    }

    static List<Path> discoverJsonFiles(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) {
            throw new IOException("El directorio no existe: " + dir);
        }
        try (var stream = Files.list(dir)) {
            return stream
                    .filter(p -> !Files.isDirectory(p) && p.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .toList();
        }
    }
}
