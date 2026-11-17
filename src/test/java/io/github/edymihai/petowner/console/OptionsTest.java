package io.github.edymihai.petowner.console;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptionsTest {

    @Test
    void defaultExportDirIsExports() {
        assertEquals(Path.of("exports"), Options.parse(new String[0]).exportDir());
    }

    @Test
    void exportDirFlagSetsTheFolder() {
        assertEquals(Path.of("out", "history"), Options.parse(new String[]{"--export-dir=out/history"}).exportDir());
    }

    @Test
    void unknownArgumentIsRejectedWithItsName() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> Options.parse(new String[]{"--verbose"}));

        assertTrue(e.getMessage().contains("--verbose"));
    }

    @Test
    void emptyPathIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Options.parse(new String[]{"--export-dir="}));
        assertThrows(IllegalArgumentException.class, () -> Options.parse(new String[]{"--export-dir=  "}));
    }

    @Test
    void demoIsOffByDefaultAndOnWithTheFlag() {
        assertFalse(Options.parse(new String[0]).demo());
        assertTrue(Options.parse(new String[]{"--demo"}).demo());
        assertEquals(Path.of("exports"), Options.parse(new String[]{"--demo"}).exportDir());
    }

    @Test
    void demoAndExportDirWorkInEitherOrder() {
        Options first = Options.parse(new String[]{"--demo", "--export-dir=out"});
        Options second = Options.parse(new String[]{"--export-dir=out", "--demo"});

        assertTrue(first.demo());
        assertEquals(Path.of("out"), first.exportDir());
        assertEquals(first, second);
    }

    @Test
    void unknownArgumentsAreStillRejectedNextToDemo() {
        assertThrows(IllegalArgumentException.class, () -> Options.parse(new String[]{"--demo", "--verbose"}));
        assertThrows(IllegalArgumentException.class, () -> Options.parse(new String[]{"--verbose", "--demo"}));
        assertThrows(IllegalArgumentException.class, () -> Options.parse(new String[]{"--demo=yes"}));
    }

    @Test
    void theLastExportDirFlagWins() {
        assertEquals(Path.of("b"), Options.parse(new String[]{"--export-dir=a", "--export-dir=b"}).exportDir());
    }
}
