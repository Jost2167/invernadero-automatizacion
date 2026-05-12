package com.jost.invernadero.taigasync.cli;

import com.jost.invernadero.taigasync.config.SyncConfig;
import com.jost.invernadero.taigasync.pipeline.SyncPipeline;
import com.jost.invernadero.taigasync.pipeline.SyncResult;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "sync", mixinStandardHelpOptions = true, description = "Sincroniza schemas codegen desde Taiga")
public class SyncCommand implements Callable<Integer> {

    @Option(names = "--dry-run", description = "Valida e informa acciones sin escribir archivos ni publicar comentarios")
    private boolean dryRun;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() {
        PrintWriter out = spec.commandLine().getOut();
        PrintWriter err = spec.commandLine().getErr();

        try {
            SyncConfig config = SyncConfig.fromEnvironment();
            SyncResult result = new SyncPipeline(config).run(dryRun);
            printSummary(out, result);
            return 0;
        } catch (IllegalStateException ex) {
            err.println(ex.getMessage());
            return 1;
        } catch (IOException ex) {
            err.println(ex.getMessage());
            return 1;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            err.println("Sync interrupted while communicating with Taiga.");
            return 1;
        }
    }

    private void printSummary(PrintWriter out, SyncResult result) {
        out.println("Sincronizacion completada.");
        out.println("Historias procesadas: " + result.processedStories());
        out.println("Schemas escritos: " + result.schemasWritten());
        out.println("Historias marcadas como Done: " + result.storiesMarkedDone());
        out.println("Historias con errores de validacion: " + result.validationFailures());
        out.println("Historias omitidas sin codegen_json: " + result.skippedStories());
    }
}
