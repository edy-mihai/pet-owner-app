package io.github.edymihai.petowner.report;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistoryFileTest {

    private final HistoryFile historyFile = new HistoryFile();

    @TempDir
    Path temp;

    @Test
    void writeThenReadReturnsTheSameLines() throws IOException {
        List<String> lines = List.of("Pet history: Rex", "", "Owners: Ană, Bob");

        Path written = historyFile.write(temp, "rex.txt", lines);

        assertEquals(temp.resolve("rex.txt"), written);
        assertEquals(lines, historyFile.read(written));
    }

    @Test
    void writeCreatesAMissingDirectory() throws IOException {
        Path directory = temp.resolve("a").resolve("b");

        Path written = historyFile.write(directory, "x.txt", List.of("hi"));

        assertTrue(Files.isRegularFile(written));
    }

    @Test
    void writeOverwritesAnExistingFile() throws IOException {
        historyFile.write(temp, "x.txt", List.of("one", "two", "three"));
        Path written = historyFile.write(temp, "x.txt", List.of("only"));

        assertEquals(List.of("only"), historyFile.read(written));
    }

    @Test
    void listReturnsTheTxtFilesSortedByName() throws IOException {
        historyFile.write(temp, "b.txt", List.of("b"));
        historyFile.write(temp, "a.txt", List.of("a"));
        historyFile.write(temp, "notes.md", List.of("ignored"));
        Files.createDirectory(temp.resolve("folder.txt"));

        List<Path> files = historyFile.list(temp);

        assertEquals(List.of(temp.resolve("a.txt"), temp.resolve("b.txt")), files);
    }

    @Test
    void listOfAMissingDirectoryIsEmpty() throws IOException {
        assertTrue(historyFile.list(temp.resolve("missing")).isEmpty());
    }

    @Test
    void fileNameIsLowercasedAndSanitisedAndHasTheIdAndDate() {
        assertEquals("rex-the-3rd--1-history-2026-11-13.txt",
                HistoryFile.fileName("Rex the 3rd!", 1, LocalDate.of(2026, 11, 13)));
    }

    @Test
    void theIdKeepsPetsWithTheSameNameApart() {
        LocalDate date = LocalDate.of(2026, 11, 13);

        assertEquals("rex-1-history-2026-11-13.txt", HistoryFile.fileName("Rex", 1, date));
        assertEquals("rex-2-history-2026-11-13.txt", HistoryFile.fileName("Rex", 2, date));
    }

    @Test
    void pathSeparatorsInAPetNameCannotEscapeTheFolder() {
        String name = HistoryFile.fileName("../../evil\\x", 1, LocalDate.of(2026, 11, 13));

        assertTrue(name.chars().noneMatch(c -> c == '/' || c == '\\'));
        assertTrue(name.startsWith("------evil-x-1-history"));
    }
}
