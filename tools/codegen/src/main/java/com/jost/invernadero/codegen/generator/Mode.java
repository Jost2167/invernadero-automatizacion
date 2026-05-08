package com.jost.invernadero.codegen.generator;

public final class Mode {

    private final boolean dryRun;
    private final boolean overwrite;
    private final boolean yes;

    private Mode(boolean dryRun, boolean overwrite, boolean yes) {
        this.dryRun = dryRun;
        this.overwrite = overwrite;
        this.yes = yes;
    }

    public static Mode dryRun() {
        return new Mode(true, false, false);
    }

    public static Mode write() {
        return new Mode(false, false, false);
    }

    public static Mode overwrite(boolean yes) {
        return new Mode(false, true, yes);
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public boolean isYes() {
        return yes;
    }
}
