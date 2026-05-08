package com.jost.invernadero.codegen.cli;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "validate", description = "Valida una definicion JSON de entidad")
public class ValidateCommand implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "<input>")
    private Path input;

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
            out.println("JSON valido");
            if (!outcome.report().warnings().isEmpty()) {
                out.println(outcome.report().format());
            }
            return 0;
        } catch (IOException ex) {
            err.println(ex.getMessage());
            return 2;
        }
    }
}
