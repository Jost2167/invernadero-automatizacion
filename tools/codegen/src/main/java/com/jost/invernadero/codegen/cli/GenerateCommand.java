package com.jost.invernadero.codegen.cli;

import com.jost.invernadero.codegen.generator.GenerationResult;
import com.jost.invernadero.codegen.generator.Generator;
import com.jost.invernadero.codegen.generator.Mode;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "generate", description = "Genera codigo desde una definicion JSON de entidad")
public class GenerateCommand implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "<input>")
    private Path input;

    @Option(names = "--overwrite", description = "Sobrescribe archivos existentes")
    private boolean overwrite;

    @Option(names = "--dry-run", description = "Lista archivos sin escribir")
    private boolean dryRun;

    @Option(names = "--yes", description = "Confirma overwrite en modo no interactivo")
    private boolean yes;

    @Option(names = "--output-backend", defaultValue = "backend", description = "Directorio backend de salida")
    private String outputBackend;

    @Option(names = "--output-frontend", defaultValue = "frontend", description = "Directorio frontend de salida")
    private String outputFrontend;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() {
        PrintWriter out = spec.commandLine().getOut();
        PrintWriter err = spec.commandLine().getErr();
        try {
            CliSupport.ValidationOutcome outcome = CliSupport.validate(input);
            if (outcome.report().hasErrors()) {
                out.println(outcome.report().format());
                return 1;
            }
            if (!outcome.report().warnings().isEmpty()) {
                out.println(outcome.report().format());
            }

            Mode mode = dryRun ? Mode.dryRun() : overwrite ? Mode.overwrite(yes) : Mode.write();
            GenerationResult result = new Generator(
                    Path.of("").toAbsolutePath(),
                    Clock.systemDefaultZone(),
                    outputBackend,
                    outputFrontend)
                    .generate(outcome.definition(), outcome.definition().options(), mode);
            result.messages().forEach(out::println);
            return result.success() ? 0 : 1;
        } catch (IOException | UncheckedIOException ex) {
            err.println(ex.getMessage());
            return 2;
        } catch (IllegalStateException ex) {
            err.println(ex.getMessage());
            return 1;
        }
    }
}
