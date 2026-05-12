package com.jost.invernadero.taigasync.cli;

import picocli.CommandLine;

public final class TaigaSyncCli {

    private TaigaSyncCli() {
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new SyncCommand()).execute(args);
        System.exit(exitCode);
    }
}
