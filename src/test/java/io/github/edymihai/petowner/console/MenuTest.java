package io.github.edymihai.petowner.console;

import io.github.edymihai.petowner.PetCare;
import io.github.edymihai.petowner.account.InMemoryAccountDirectory;
import io.github.edymihai.petowner.pet.InMemoryPetRegister;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 13);

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final PetCare petCare = new PetCare(new InMemoryAccountDirectory(), new InMemoryPetRegister());

    private String run(String script) {
        PrintStream out = new PrintStream(output);
        Input input = new Input(new Scanner(new ByteArrayInputStream(script.getBytes())), out);
        new Menu(input, petCare, out, TODAY).run();
        return output.toString();
    }

    @Test
    void addAccountThenListIt() {
        String text = run("1\nAna\nana@example.com\nowner\n2\n0\n");

        assertTrue(text.contains("Added account 1."));
        assertTrue(text.contains("1: Ana <ana@example.com> OWNER"));
        assertEquals(1, petCare.listAccounts().size());
    }

    @Test
    void adminCannotAddAPetAndMenuKeepsRunning() {
        String text = run("1\nRoot\nroot@example.com\nadmin\n3\nRoot\n4\n0\n");

        assertTrue(text.contains("is an ADMIN and cannot own a pet"));
        assertFalse(text.contains("Pet name"));
        assertTrue(text.contains("No pets yet."));
        assertTrue(petCare.listPets().isEmpty());
    }

    @Test
    void addPetForUnknownNamePrintsMessageAndReturnsToTheMenu() {
        String text = run("3\nNobody\n4\n0\n");

        assertTrue(text.contains("No account named Nobody."));
        assertFalse(text.contains("Pet name"));
        assertTrue(text.contains("No pets yet."));
    }

    @Test
    void addPetForAnOwnerWorks() {
        String text = run("1\nAna\nana@example.com\nowner\n3\nana\nRex\ndog\n2020-01-02\nfriendly\n4\n0\n");

        assertTrue(text.contains("Added pet 1."));
        assertTrue(text.contains("Rex (DOG), born 2020-01-02, friendly"));
    }

    @Test
    void addPetWithAFutureBirthDatePrintsTheMessage() {
        String text = run("1\nAna\nana@example.com\nowner\n3\nAna\nRex\ndog\n2030-01-01\n\n4\n0\n");

        assertTrue(text.contains("a pet cannot be owned before it is born"));
        assertTrue(text.contains("No pets yet."));
    }

    @Test
    void twoAccountsWithTheSameNameAreListedAndPickedByNumber() {
        String text = run("1\nAna\na@x.com\nowner\n1\nAna\nb@x.com\nowner\n"
                + "3\nAna\n2\nRex\ndog\n\n\n4\n0\n");

        assertTrue(text.contains("1) Ana <a@x.com>"));
        assertTrue(text.contains("2) Ana <b@x.com>"));
        assertTrue(text.contains("Added pet 1."));
        assertTrue(petCare.listPets().get(0).isOwnedBy(2L));
    }

    @Test
    void aBadPickIsAskedAgain() {
        String text = run("1\nAna\na@x.com\nowner\n1\nAna\nb@x.com\nowner\n"
                + "3\nAna\n0\nabc\n1\nRex\ndog\n\n\n0\n");

        assertTrue(text.contains("Please type a number from 1 to 2."));
        assertTrue(text.contains("That is not a number."));
        assertTrue(petCare.listPets().get(0).isOwnedBy(1L));
    }

    @Test
    void duplicateEmailMessageIsPrinted() {
        String text = run("1\nAna\na@x.com\nowner\n1\nAna2\na@x.com\nowner\n0\n");

        assertTrue(text.contains("already exists"));
    }

    @Test
    void zeroExits() {
        assertTrue(run("0\n").contains("Goodbye."));
    }

    @Test
    void endOfInputExitsCleanly() {
        assertTrue(run("2\n").contains("Goodbye."));
    }

    @Test
    void endOfInputInTheMiddleOfAnActionExitsCleanly() {
        String text = run("1\nAna\n");

        assertTrue(text.contains("Goodbye."));
        assertTrue(petCare.listAccounts().isEmpty());
    }
}
