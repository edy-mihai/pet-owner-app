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
        String text = run("1\nRoot\nroot@example.com\nadmin\n3\n1\nRex\ndog\n\n\n4\n0\n");

        assertTrue(text.contains("is an ADMIN and cannot own a pet"));
        assertTrue(text.contains("No pets yet."));
        assertTrue(petCare.listPets().isEmpty());
    }

    @Test
    void addPetForUnknownAccountPrintsMessageAndContinues() {
        String text = run("3\n99\nRex\ndog\n\n\n4\n0\n");

        assertTrue(text.contains("No account with id 99"));
        assertTrue(text.contains("No pets yet."));
    }

    @Test
    void addPetForAnOwnerWorks() {
        String text = run("1\nAna\nana@example.com\nowner\n3\n1\nRex\ndog\n2020-01-02\nfriendly\n4\n0\n");

        assertTrue(text.contains("Added pet 1."));
        assertTrue(text.contains("Rex (DOG), born 2020-01-02, friendly"));
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
