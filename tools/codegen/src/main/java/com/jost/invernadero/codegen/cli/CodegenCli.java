package com.jost.invernadero.codegen.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "codegen",
        mixinStandardHelpOptions = true,
        version = "1.0",
        subcommands = {ValidateCommand.class, GenerateCommand.class},
        description = "Generador de codigo para entidades del invernadero")
public class CodegenCli implements Runnable {

    @Spec
    private CommandSpec spec;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new CodegenCli()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        spec.commandLine().usage(spec.commandLine().getOut());
    }
}
