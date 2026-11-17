package io.github.edymihai.petowner.console;

import java.nio.file.Path;

/** Command-line options of the console. */
public record Options(Path exportDir, boolean demo) {

    private static final String EXPORT_DIR = "--export-dir=";
    private static final String DEMO = "--demo";
    private static final String DEFAULT_EXPORT_DIR = "exports";

    /** Throws IllegalArgumentException for an unknown argument or an empty path. */
    public static Options parse(String[] args) {
        Path exportDir = Path.of(DEFAULT_EXPORT_DIR);
        boolean demo = false;
        for (String arg : args) {
            if (arg.equals(DEMO)) {
                demo = true;
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
        return new Options(exportDir, demo);
    }
}
