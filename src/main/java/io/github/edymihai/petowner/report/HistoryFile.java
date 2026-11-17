package io.github.edymihai.petowner.report;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/** Writes and reads history text files (UTF-8). The only class that touches the file system. */
public class HistoryFile {

    /** Creates the directory if missing, overwrites an existing file and returns the path written. */
    public Path write(Path directory, String fileName, List<String> lines) throws IOException {
        Files.createDirectories(directory);
        Path file = directory.resolve(fileName);
        return Files.write(file, lines, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    public List<String> read(Path file) throws IOException {
        return Files.readAllLines(file, StandardCharsets.UTF_8);
    }

    /** The .txt files in the directory sorted by name; empty if the directory does not exist. */
    public List<Path> list(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(directory)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(f -> f.getFileName().toString().endsWith(".txt"))
                    .sorted(Comparator.comparing(f -> f.getFileName().toString()))
                    .toList();
        }
    }

    /**
     * "{pet-name}-{pet-id}-history-{date}.txt". The name is lowercased and every character that is
     * not a letter or digit becomes '-'. The id keeps two pets with the same name apart.
     */
    public static String fileName(String petName, long petId, LocalDate date) {
        StringBuilder safe = new StringBuilder();
        for (char c : petName.toLowerCase(Locale.ROOT).toCharArray()) {
            safe.append(Character.isLetterOrDigit(c) ? c : '-');
        }
        return safe + "-" + petId + "-history-" + date + ".txt";
    }
}
