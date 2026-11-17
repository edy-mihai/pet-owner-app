package io.github.edymihai.petowner.console;

import java.nio.file.Path;

/** Command-line options of the console. */
public record Options(Path exportDir, boolean demo, boolean noColor, boolean color) {

    private static final String EXPORT_DIR = "--export-dir=";
    private static final String DEMO = "--demo";
    private static final String NO_COLOR = "--no-color";
    private static final String COLOR = "--color";
    private static final String DEFAULT_EXPORT_DIR = "exports";

    /** Throws IllegalArgumentException for an unknown argument or an empty path. */
    public static Options parse(String[] args) {
        Path exportDir = Path.of(DEFAULT_EXPORT_DIR);
        boolean demo = false;
        boolean noColor = false;
        boolean color = false;
        for (String arg : args) {
            if (arg.equals(DEMO)) {
                demo = true;
                continue;
            }
            if (arg.equals(NO_COLOR)) {
                noColor = true;
                continue;
            }
            if (arg.equals(COLOR)) {
                color = true;
                continue;
            }
            if (!arg.startsWith(EXPORT_DIR)) {
                throw new IllegalArgumentException("Unknown argument: " + arg);
            }
            String value = arg.substring(EXPORT_DIR.length());
            if (value.isBlank()) {
                throw new IllegalArgumentException("--export-dir needs a path, for example --export-dir=exports");
            }
            exportDir = Path.of(value);
        }
        return new Options(exportDir, demo, noColor, color);
    }

    /**
     * --no-color wins over everything; --color forces colors on (even without a terminal and
     * even with NO_COLOR set); otherwise colors need a real terminal and no NO_COLOR.
     */
    public boolean colorsEnabled(boolean terminal, boolean noColorEnvSet) {
        if (noColor) {
            return false;
        }
        if (color) {
            return true;
        }
        return terminal && !noColorEnvSet;
    }
}
