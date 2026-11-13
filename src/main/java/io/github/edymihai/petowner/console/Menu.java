package io.github.edymihai.petowner.console;

import io.github.edymihai.petowner.AdminCannotOwnPetException;
import io.github.edymihai.petowner.DuplicateEmailException;
import io.github.edymihai.petowner.EndOfInputException;
import io.github.edymihai.petowner.PetCare;
import io.github.edymihai.petowner.account.Account;
import io.github.edymihai.petowner.account.Role;
import io.github.edymihai.petowner.pet.Pet;
import io.github.edymihai.petowner.pet.Species;

import java.io.PrintStream;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

/** Reads through Input, calls PetCare and prints. Contains no rules. */
public class Menu {

    private final Input input;
    private final PetCare petCare;
    private final PrintStream out;
    private final LocalDate today;

    public Menu(Input input, PetCare petCare, PrintStream out, LocalDate today) {
        this.input = input;
        this.petCare = petCare;
        this.out = out;
        this.today = today;
    }

    /** Runs until the user picks 0 or the input ends. */
    public void run() {
        try {
            boolean running = true;
            while (running) {
                printMenu();
                switch (input.readInt("Choose: ", 0, 4)) {
                    case 1 -> addAccount();
                    case 2 -> listAccounts();
                    case 3 -> addPet();
                    case 4 -> listPets();
                    default -> running = false;
                }
            }
        } catch (EndOfInputException e) {
            out.println();
        }
        out.println("Goodbye.");
    }

    private void printMenu() {
        out.println();
        out.println("1) Add account");
        out.println("2) List accounts");
        out.println("3) Add pet");
        out.println("4) List pets");
        out.println("0) Exit");
    }

    private void addAccount() {
        String name = input.readNonBlank("Name: ");
        String email = input.readEmail("Email: ");
        Role role = input.readChoice("Role", Role.class);
        try {
            Account account = petCare.addAccount(name, email, role);
            out.println("Added account " + account.getId() + ".");
        } catch (DuplicateEmailException | IllegalArgumentException e) {
            out.println(e.getMessage());
        }
    }

    private void listAccounts() {
        List<Account> accounts = petCare.listAccounts();
        if (accounts.isEmpty()) {
            out.println("No accounts yet.");
        }
        for (Account a : accounts) {
            out.println(a.getId() + ": " + a.getName() + " <" + a.getEmail() + "> " + a.getRole());
        }
    }

    private void addPet() {
        long ownerId = input.readInt("Owner account id: ", 1, Integer.MAX_VALUE);
        String name = input.readNonBlank("Pet name: ");
        Species species = input.readChoice("Species", Species.class);
        LocalDate birthDate = input.readOptionalDate("Birth date (yyyy-MM-dd, blank for none): ").orElse(null);
        String notes = input.readOptionalText("Notes (blank for none): ").orElse(null);
        try {
            Pet pet = petCare.addPet(ownerId, name, species, birthDate, notes, today);
            out.println("Added pet " + pet.getId() + ".");
        } catch (AdminCannotOwnPetException | IllegalArgumentException | NoSuchElementException e) {
            out.println(e.getMessage());
        }
    }

    private void listPets() {
        List<Pet> pets = petCare.listPets();
        if (pets.isEmpty()) {
            out.println("No pets yet.");
        }
        for (Pet p : pets) {
            out.println(p.getId() + ": " + p.getName() + " (" + p.getSpecies() + ")"
                    + (p.getBirthDate() == null ? "" : ", born " + p.getBirthDate())
                    + (p.getNotes() == null ? "" : ", " + p.getNotes())
                    + ", owners " + p.getOwners().keySet());
        }
    }
}
