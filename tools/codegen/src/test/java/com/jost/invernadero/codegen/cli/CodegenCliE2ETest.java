package com.jost.invernadero.codegen.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class CodegenCliE2ETest {

    @Test
    void validateValidFixtureReturnsExitZero() {
        StringWriter stdout = new StringWriter();
        StringWriter stderr = new StringWriter();
        CommandLine commandLine = commandLine(stdout, stderr);

        int exitCode = commandLine.execute("validate", "fixtures/valid/simple.json");

        assertThat(exitCode).isEqualTo(0);
        assertThat(stdout.toString()).contains("JSON valido");
        assertThat(stderr.toString()).isEmpty();
    }

    @Test
    void validateInvalidFixtureReturnsExitOneWithReportOnStdout() {
        StringWriter stdout = new StringWriter();
        StringWriter stderr = new StringWriter();
        CommandLine commandLine = commandLine(stdout, stderr);

        int exitCode = commandLine.execute("validate", "fixtures/invalid/bad-type.json");

        assertThat(exitCode).isEqualTo(1);
        assertThat(stdout.toString())
                .contains("Errors:")
                .contains("$.fields[0].type");
        assertThat(stderr.toString()).isEmpty();
    }

    private CommandLine commandLine(StringWriter stdout, StringWriter stderr) {
        CommandLine commandLine = new CommandLine(new CodegenCli());
        commandLine.setOut(new PrintWriter(stdout, true));
        commandLine.setErr(new PrintWriter(stderr, true));
        return commandLine;
    }
}
