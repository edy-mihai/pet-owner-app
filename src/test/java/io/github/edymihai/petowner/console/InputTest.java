package io.github.edymihai.petowner.console;

import io.github.edymihai.petowner.EndOfInputException;
import io.github.edymihai.petowner.account.Role;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputTest {

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    private Input inputWith(String script) {
        Scanner scanner = new Scanner(new ByteArrayInputStream(script.getBytes()));
        return new Input(scanner, new PrintStream(output));
    }

    @Test
    void readYesNoAcceptsYesAndNoInAnyCase() {
        Input input = inputWith("y\nYES\nn\nNo\n");

        assertTrue(input.readYesNo("? "));
        assertTrue(input.readYesNo("? "));
        assertFalse(input.readYesNo("? "));
        assertFalse(input.readYesNo("? "));
    }

    @Test
    void readYesNoBlankMeansNo() {
        assertFalse(inputWith("\n").readYesNo("? "));
    }

    @Test
    void readYesNoReAsksOnAnythingElse() {
        Input input = inputWith("maybe\n1\ny\n");

        assertTrue(input.readYesNo("? "));
        assertTrue(output.toString().contains("Please answer y or n."));
    }

    @Test
    void readIntReAsksAfterBadNumberAndOutOfRange() {
        Input input = inputWith("abc\n9\n5\n");

        assertEquals(5, input.readInt("n: ", 0, 6));
        assertTrue(output.toString().contains("not a number"));
        assertTrue(output.toString().contains("from 0 to 6"));
    }

    @Test
    void readDateReAsksAfterBadDate() {
        Input input = inputWith("13/45/2026\n2026-05-01\n");

        assertEquals(LocalDate.of(2026, 5, 1), input.readDate("d: "));
        assertTrue(output.toString().contains("yyyy-MM-dd"));
    }

    @Test
    void readOptionalDateBlankMeansNone() {
        assertTrue(inputWith("\n").readOptionalDate("d: ").isEmpty());
    }

    @Test
    void readOptionalDateReAsksAfterBadDate() {
        assertEquals(LocalDate.of(2024, 1, 2), inputWith("x\n2024-01-02\n").readOptionalDate("d: ").orElseThrow());
    }

    @Test
    void readChoiceReAsksAfterInvalidChoice() {
        Input input = inputWith("FISH\nadmin\n");

        assertEquals(Role.ADMIN, input.readChoice("Role", Role.class));
        assertTrue(output.toString().contains("Not a valid choice"));
    }

    @Test
    void readChoiceAcceptsANumber() {
        assertEquals(Role.OWNER, inputWith("1\n").readChoice("Role", Role.class));
    }

    @Test
    void readEmailReAsksWithoutAt() {
        assertEquals("a@b.c", inputWith("nope\na@b.c\n").readEmail("e: "));
    }

    @Test
    void endOfInputThrowsEndOfInputException() {
        assertThrows(EndOfInputException.class, () -> inputWith("").readNonBlank("x: "));
    }
}
