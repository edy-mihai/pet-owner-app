package io.github.edymihai.petowner.console;

import io.github.edymihai.petowner.PetCare;
import io.github.edymihai.petowner.account.InMemoryAccountDirectory;
import io.github.edymihai.petowner.account.Role;
import io.github.edymihai.petowner.careplan.InMemoryCarePlanBook;
import io.github.edymihai.petowner.pet.InMemoryPetRegister;
import io.github.edymihai.petowner.pet.Species;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Scripts read like a path through the menus: 1 = Accounts, 2 = Pets, 3 = Care plans,
 * and 0 inside a submenu goes back to the main menu.
 */
class MenuTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 13);

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final PetCare petCare = new PetCare(new InMemoryAccountDirectory(), new InMemoryPetRegister(),
            new InMemoryCarePlanBook());

    private String run(String script) {
        PrintStream out = new PrintStream(output);
        Input input = new Input(new Scanner(new ByteArrayInputStream(script.getBytes())), out);
        new Menu(input, petCare, out, TODAY).run();
        return output.toString();
    }

    @Test
    void addAccountThenListIt() {
        String text = run("1\n1\nAna\nana@example.com\nowner\n2\n0\n0\n");

        assertTrue(text.contains("Added account 1."));
        assertTrue(text.contains("1: Ana <ana@example.com> OWNER"));
        assertEquals(1, petCare.listAccounts().size());
    }

    @Test
    void adminCannotAddAPetAndTheSubmenuKeepsRunning() {
        String text = run("1\n1\nRoot\nroot@example.com\nadmin\n0\n2\n1\nRoot\n2\n0\n0\n");

        assertTrue(text.contains("is an ADMIN and cannot own a pet"));
        assertFalse(text.contains("Pet name"));
        assertTrue(text.contains("No pets yet."));
        assertTrue(petCare.listPets().isEmpty());
    }

    @Test
    void addPetForUnknownNamePrintsMessageAndReturnsToTheSubmenu() {
        String text = run("2\n1\nNobody\n2\n0\n0\n");

        assertTrue(text.contains("No account named Nobody."));
        assertFalse(text.contains("Pet name"));
        assertTrue(text.contains("No pets yet."));
    }

    @Test
    void addPetForAnOwnerWorks() {
        String text = run("1\n1\nAna\nana@example.com\nowner\n0\n"
                + "2\n1\nana\nRex\ndog\n2020-01-02\nfriendly\n2\n0\n0\n");

        assertTrue(text.contains("Added pet 1."));
        assertTrue(text.contains("Rex (DOG), born 2020-01-02, friendly"));
    }

    @Test
    void addPetWithAFutureBirthDatePrintsTheMessage() {
        String text = run("1\n1\nAna\nana@example.com\nowner\n0\n"
                + "2\n1\nAna\nRex\ndog\n2030-01-01\n\n2\n0\n0\n");

        assertTrue(text.contains("a pet cannot be owned before it is born"));
        assertTrue(text.contains("No pets yet."));
    }

    @Test
    void twoAccountsWithTheSameNameAreListedAndPickedByNumber() {
        String text = run("1\n1\nAna\na@x.com\nowner\n1\nAna\nb@x.com\nowner\n0\n"
                + "2\n1\nAna\n2\nRex\ndog\n\n\n2\n0\n0\n");

        assertTrue(text.contains("1) Ana <a@x.com>"));
        assertTrue(text.contains("2) Ana <b@x.com>"));
        assertTrue(text.contains("Added pet 1."));
        assertTrue(petCare.listPets().get(0).isOwnedBy(2L));
    }

    @Test
    void aBadPickIsAskedAgain() {
        String text = run("1\n1\nAna\na@x.com\nowner\n1\nAna\nb@x.com\nowner\n0\n"
                + "2\n1\nAna\n0\nabc\n1\nRex\ndog\n\n\n0\n0\n");

        assertTrue(text.contains("Please type a number from 1 to 2."));
        assertTrue(text.contains("That is not a number."));
        assertTrue(petCare.listPets().get(0).isOwnedBy(1L));
    }

    @Test
    void duplicateEmailMessageIsPrinted() {
        String text = run("1\n1\nAna\na@x.com\nowner\n1\nAna2\na@x.com\nowner\n0\n0\n");

        assertTrue(text.contains("already exists"));
    }

    private void anaAndBobWithRex() {
        petCare.addAccount("Ana", "ana@x.com", Role.OWNER);
        petCare.addAccount("Bob", "bob@x.com", Role.OWNER);
        petCare.addPet(1L, "Rex", Species.DOG, null, null, TODAY);
    }

    @Test
    void deleteAccountWithYesDeletesTheAccountAndItsSolePets() {
        anaAndBobWithRex();

        String text = run("1\n3\nAna\ny\n0\n0\n");

        assertTrue(text.contains("These pets will be deleted with the account:"));
        assertTrue(text.contains("- Rex (DOG)"));
        assertTrue(text.contains("Deleted account Ana and 1 pet(s)."));
        assertEquals(1, petCare.listAccounts().size());
        assertTrue(petCare.listPets().isEmpty());
    }

    @Test
    void deleteAccountWithNoDeletesNothing() {
        anaAndBobWithRex();

        String text = run("1\n3\nAna\nn\n0\n0\n");

        assertTrue(text.contains("Nothing deleted."));
        assertEquals(2, petCare.listAccounts().size());
        assertEquals(1, petCare.listPets().size());
    }

    @Test
    void deleteAccountWithABlankAnswerDeletesNothing() {
        anaAndBobWithRex();

        run("1\n3\nAna\n\n0\n0\n");

        assertEquals(2, petCare.listAccounts().size());
        assertEquals(1, petCare.listPets().size());
    }

    @Test
    void deletePetAfterConfirmation() {
        anaAndBobWithRex();

        String text = run("2\n3\nAna\n1\ny\n0\n0\n");

        assertTrue(text.contains("1) Rex (DOG)"));
        assertTrue(text.contains("Deleted pet Rex."));
        assertTrue(petCare.listPets().isEmpty());
    }

    @Test
    void addAndRemoveACoOwner() {
        anaAndBobWithRex();

        String text = run("2\n4\nAna\n1\nBob\n2\n5\nBob\n1\n2\n0\n0\n");

        assertTrue(text.contains("Added Bob as co-owner of Rex."));
        assertTrue(text.contains("owners Ana, Bob") || text.contains("owners Bob, Ana"));
        assertTrue(text.contains("Removed Bob from Rex."));
        assertFalse(petCare.listPets().get(0).isOwnedBy(2L));
        assertTrue(petCare.listPets().get(0).isOwnedBy(1L));
    }

    @Test
    void addingAnExistingOwnerPrintsAMessage() {
        anaAndBobWithRex();

        String text = run("2\n4\nAna\n1\nAna\n0\n0\n");

        assertTrue(text.contains("Ana already owns Rex."));
    }

    @Test
    void anAdminCannotBeACoOwner() {
        anaAndBobWithRex();
        petCare.addAccount("Root", "root@x.com", Role.ADMIN);

        String text = run("2\n4\nAna\n1\nRoot\n0\n0\n");

        assertTrue(text.contains("is an ADMIN and cannot own a pet"));
        assertEquals(1, petCare.listPets().get(0).getOwners().size());
    }

    @Test
    void removingTheLastOwnerPrintsTheMessage() {
        anaAndBobWithRex();

        String text = run("2\n5\nAna\n1\n0\n0\n");

        assertTrue(text.contains("is the last owner of the pet and cannot be removed"));
        assertTrue(petCare.listPets().get(0).isOwnedBy(1L));
    }

    @Test
    void zeroExits() {
        assertTrue(run("0\n").contains("Goodbye."));
    }

    @Test
    void mainMenuListsTheThreeAreas() {
        String text = run("0\n");

        assertTrue(text.contains("1) Accounts"));
        assertTrue(text.contains("2) Pets"));
        assertTrue(text.contains("3) Care plans"));
        assertTrue(text.contains("0) Exit"));
    }

    @Test
    void eachSubmenuCanBeEnteredAndLeftWithBack() {
        String text = run("1\n0\n2\n0\n3\n0\n0\n");

        String nl = System.lineSeparator();
        assertTrue(text.contains("3) Delete" + nl + "0) Back"));
        assertTrue(text.contains("5) Remove co-owner" + nl + "0) Back"));
        assertTrue(text.contains("2) List" + nl + "0) Back"));
        // main menu is shown at start and after each of the three submenus
        assertEquals(4, text.split("1\\) Accounts", -1).length - 1);
        assertTrue(text.contains("Goodbye."));
    }

    @Test
    void backReturnsToTheMainMenuNotToExit() {
        String text = run("1\n0\n2\n2\n0\n0\n");

        assertTrue(text.contains("No pets yet."));
        assertTrue(text.contains("Goodbye."));
    }

    @Test
    void anInvalidChoiceInTheMainMenuIsAskedAgain() {
        String text = run("4\nabc\n0\n");

        assertTrue(text.contains("Please type a number from 0 to 3."));
        assertTrue(text.contains("That is not a number."));
        assertTrue(text.contains("Goodbye."));
    }

    @Test
    void anInvalidChoiceInASubmenuIsAskedAgainAndStaysThere() {
        String text = run("3\n3\nabc\n2\nNobody\n0\n0\n");

        assertTrue(text.contains("Please type a number from 0 to 2."));
        assertTrue(text.contains("That is not a number."));
        // after the bad choices the submenu still handled "2" (List care plans)
        assertTrue(text.contains("No account named Nobody."));
    }

    @Test
    void endOfInputExitsCleanly() {
        assertTrue(run("2\n").contains("Goodbye."));
    }

    @Test
    void endOfInputInsideASubmenuExitsCleanly() {
        for (String script : new String[] {"1\n", "1\n2\n0\n2\n", "3\n"}) {
            assertTrue(new MenuRun(script).text().contains("Goodbye."), script);
        }
    }

    @Test
    void endOfInputInTheMiddleOfAnActionExitsCleanly() {
        String text = run("1\n1\nAna\n");

        assertTrue(text.contains("Goodbye."));
        assertTrue(petCare.listAccounts().isEmpty());
    }

    @Test
    void aRejectedValueStaysInTheSubmenu() {
        String text = run("1\n1\nRoot\nroot@x.com\nadmin\n0\n2\n1\nRoot\n0\n0\n");

        assertTrue(text.contains("is an ADMIN and cannot own a pet"));
        // "0" after the rejection went back from the Pets submenu, so only the final "0" exits
        assertEquals(1, text.split("Goodbye\\.", -1).length - 1);
        assertTrue(petCare.listPets().isEmpty());
    }

    private static final String ANA_WITH_REX =
            "1\n1\nAna\nana@example.com\nowner\n0\n2\n1\nAna\nRex\ndog\n\n\n0\n";

    @Test
    void addARecurringCarePlanThenListIt() {
        String text = run(ANA_WITH_REX
                + "3\n1\nAna\n1\nPills\nmedication\n2026-11-01\n7\n2026-12-01\n"
                + "2\nAna\n1\n0\n0\n");

        assertTrue(text.contains("Added care plan 1."));
        assertTrue(text.contains("Pills (MEDICATION), repeats every 7 days from 2026-11-01 until 2026-12-01, "
                + "last given never, next due 2026-11-01, OVERDUE"));
    }

    @Test
    void theRecurrencePromptSaysRepeatEveryNDays() {
        String text = run(ANA_WITH_REX + "3\n1\nAna\n1\nPills\nmedication\n2026-11-01\n\n0\n0\n");

        assertTrue(text.contains("Repeat every N days (blank for one-off): "));
        assertFalse(text.contains("Interval in days"));
    }

    @Test
    void aOneOffCarePlanDoesNotAskForAnEndDate() {
        String text = run(ANA_WITH_REX
                + "3\n1\nAna\n1\nShot\nvaccine\n2026-11-20\n\n"
                + "2\nAna\n1\n0\n0\n");

        assertFalse(text.contains("End date"));
        assertTrue(text.contains("Shot (VACCINE), one-off on 2026-11-20, "
                + "last given never, next due 2026-11-20, NOT_STARTED"));
        assertFalse(text.contains("repeats every"));
    }

    @Test
    void anInvalidIntervalPrintsTheMessageAndSavesNothing() {
        String text = run(ANA_WITH_REX
                + "3\n1\nAna\n1\nPills\nmedication\n2026-11-01\n0\n\n"
                + "2\nAna\n1\n0\n0\n");

        assertTrue(text.contains("Interval must be at least 1 day"));
        assertTrue(text.contains("No care plans yet."));
    }

    @Test
    void listingCarePlansOfAPetWithNoneSaysSo() {
        String text = run(ANA_WITH_REX + "3\n2\nAna\n1\n0\n0\n");

        assertTrue(text.contains("No care plans yet."));
    }

    @Test
    void carePlanOptionsForAnOwnerWithoutPetsReturnToTheSubmenu() {
        String text = run("1\n1\nAna\nana@example.com\nowner\n0\n3\n1\nAna\n2\nAna\n0\n0\n");

        assertTrue(text.contains("Ana has no pets."));
        assertTrue(text.contains("Goodbye."));
    }

    /** Runs a script on a fresh PetCare, so one test can try several scripts. */
    private static final class MenuRun {
        private final String text;

        MenuRun(String script) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(bytes);
            Input input = new Input(new Scanner(new ByteArrayInputStream(script.getBytes())), out);
            PetCare care = new PetCare(new InMemoryAccountDirectory(), new InMemoryPetRegister(),
                    new InMemoryCarePlanBook());
            new Menu(input, care, out, TODAY).run();
            text = bytes.toString();
        }

        String text() {
            return text;
        }
    }
}
