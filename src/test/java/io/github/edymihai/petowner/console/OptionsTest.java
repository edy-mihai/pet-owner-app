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

    @Test
    void colorFlagsAreOffByDefault() {
        Options options = Options.parse(new String[0]);

        assertFalse(options.noColor());
        assertFalse(options.color());
    }

    @Test
    void noColorFlagSetsItAndKeepsTheOtherDefaults() {
        Options options = Options.parse(new String[]{"--no-color"});

        assertTrue(options.noColor());
        assertFalse(options.color());
        assertFalse(options.demo());
        assertEquals(Path.of("exports"), options.exportDir());
    }

    @Test
    void colorFlagSetsIt() {
        assertTrue(Options.parse(new String[]{"--color"}).color());
    }

    @Test
    void noColorWorksInEitherOrderWithDemoAndExportDir() {
        Options first = Options.parse(new String[]{"--no-color", "--demo", "--export-dir=out"});
        Options second = Options.parse(new String[]{"--export-dir=out", "--demo", "--no-color"});

        assertTrue(first.noColor());
        assertTrue(first.demo());
        assertEquals(Path.of("out"), first.exportDir());
        assertEquals(first, second);
    }

    @Test
    void colorWorksInEitherOrderWithDemoAndExportDir() {
        Options first = Options.parse(new String[]{"--color", "--demo", "--export-dir=out"});
        Options second = Options.parse(new String[]{"--export-dir=out", "--demo", "--color"});

        assertTrue(first.color());
        assertEquals(first, second);
    }

    @Test
    void unknownArgumentsAreStillRejectedNextToTheColorFlags() {
        for (String flag : new String[]{"--no-color", "--color"}) {
            assertThrows(IllegalArgumentException.class, () -> Options.parse(new String[]{flag, "--verbose"}));
            assertThrows(IllegalArgumentException.class, () -> Options.parse(new String[]{"--verbose", flag}));
        }
        assertThrows(IllegalArgumentException.class, () -> Options.parse(new String[]{"--no-color=yes"}));
        assertThrows(IllegalArgumentException.class, () -> Options.parse(new String[]{"--colour"}));
        assertThrows(IllegalArgumentException.class, () -> Options.parse(new String[]{"--NO-COLOR"}));
    }

    @Test
    void colorsNeedATerminalAndNoNoColorVariableByDefault() {
        Options options = Options.parse(new String[0]);

        assertTrue(options.colorsEnabled(true, false));
        assertFalse(options.colorsEnabled(false, false));
        assertFalse(options.colorsEnabled(true, true));
        assertFalse(options.colorsEnabled(false, true));
    }

    @Test
    void colorForcesColorsOnEvenWithoutATerminalOrWithNoColorSet() {
        Options options = Options.parse(new String[]{"--color"});

        assertTrue(options.colorsEnabled(false, false));
        assertTrue(options.colorsEnabled(false, true));
        assertTrue(options.colorsEnabled(true, true));
    }

    @Test
    void noColorTurnsColorsOffInEveryCase() {
        Options options = Options.parse(new String[]{"--no-color"});

        assertFalse(options.colorsEnabled(true, false));
        assertFalse(options.colorsEnabled(false, false));
    }

    @Test
    void noColorWinsOverColorInEitherOrder() {
        for (String[] args : new String[][]{{"--color", "--no-color"}, {"--no-color", "--color"}}) {
            Options options = Options.parse(args);

            assertFalse(options.colorsEnabled(true, false));
            assertFalse(options.colorsEnabled(false, false));
        }
    }
}
