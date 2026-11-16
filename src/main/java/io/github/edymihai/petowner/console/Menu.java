package io.github.edymihai.petowner.console;

import io.github.edymihai.petowner.AdminCannotOwnPetException;
import io.github.edymihai.petowner.CannotRemoveLastOwnerException;
import io.github.edymihai.petowner.DuplicateEmailException;
import io.github.edymihai.petowner.EndOfInputException;
import io.github.edymihai.petowner.PetCare;
import io.github.edymihai.petowner.account.Account;
import io.github.edymihai.petowner.account.Role;
import io.github.edymihai.petowner.careplan.CarePlan;
import io.github.edymihai.petowner.careplan.PlanKind;
import io.github.edymihai.petowner.pet.Pet;
import io.github.edymihai.petowner.pet.Species;

import java.io.PrintStream;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

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
                switch (input.readInt("Choose: ", 0, 10)) {
                    case 1 -> addAccount();
                    case 2 -> listAccounts();
                    case 3 -> addPet();
                    case 4 -> listPets();
                    case 5 -> deleteAccount();
                    case 6 -> deletePet();
                    case 7 -> addCoOwner();
                    case 8 -> removeCoOwner();
                    case 9 -> addCarePlan();
                    case 10 -> listCarePlans();
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
        out.println("5) Delete account");
        out.println("6) Delete pet");
        out.println("7) Add co-owner");
        out.println("8) Remove co-owner");
        out.println("9) Add care plan");
        out.println("10) List care plans");
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
        Optional<Account> owner = chooseAccountByName("Owner name: ");
        if (owner.isEmpty()) {
            return;
        }
        long ownerId = owner.get().getId();
        try {
            petCare.requireCanOwnPets(ownerId);
            String name = input.readNonBlank("Pet name: ");
            Species species = input.readChoice("Species", Species.class);
            LocalDate birthDate = input.readOptionalDate("Birth date (yyyy-MM-dd, blank for none): ").orElse(null);
            String notes = input.readOptionalText("Notes (blank for none): ").orElse(null);
            Pet pet = petCare.addPet(ownerId, name, species, birthDate, notes, today);
            out.println("Added pet " + pet.getId() + ".");
        } catch (AdminCannotOwnPetException | IllegalArgumentException | NoSuchElementException e) {
            out.println(e.getMessage());
        }
    }

    /**
     * Asks for a name and finds the account. With several matches the user picks by number.
     * Prints a message and returns empty when no account has that name.
     */
    private Optional<Account> chooseAccountByName(String prompt) {
        String name = input.readNonBlank(prompt);
        List<Account> matches = petCare.findAccountsByName(name);
        if (matches.isEmpty()) {
            out.println("No account named " + name + ".");
            return Optional.empty();
        }
        if (matches.size() == 1) {
            return Optional.of(matches.get(0));
        }
        for (int i = 0; i < matches.size(); i++) {
            Account a = matches.get(i);
            out.println((i + 1) + ") " + a.getName() + " <" + a.getEmail() + ">");
        }
        int pick = input.readInt("Which one? ", 1, matches.size());
        return Optional.of(matches.get(pick - 1));
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
                    + ", owners " + petCare.ownersOf(p.getId()).stream()
                            .map(Account::getName).collect(Collectors.joining(", ")));
        }
    }

    /** Lists the account's pets as "1) name (SPECIES)" and picks by number. */
    private Optional<Pet> choosePetOf(Account account) {
        List<Pet> pets = petCare.petsOf(account.getId());
        if (pets.isEmpty()) {
            out.println(account.getName() + " has no pets.");
            return Optional.empty();
        }
        for (int i = 0; i < pets.size(); i++) {
            Pet p = pets.get(i);
            out.println((i + 1) + ") " + p.getName() + " (" + p.getSpecies() + ")");
        }
        int pick = input.readInt("Which pet? ", 1, pets.size());
        return Optional.of(pets.get(pick - 1));
    }

    private void deleteAccount() {
        try {
            Optional<Account> account = chooseAccountByName("Account name: ");
            if (account.isEmpty()) {
                return;
            }
            Account a = account.get();
            List<Pet> toDelete = petCare.petsOnlyOwnedBy(a.getId());
            if (toDelete.isEmpty()) {
                out.println("No pets will be deleted.");
            } else {
                out.println("These pets will be deleted with the account:");
                for (Pet p : toDelete) {
                    out.println("- " + p.getName() + " (" + p.getSpecies() + ")");
                }
            }
            if (!input.readYesNo("Delete account " + a.getName() + "? (y/N): ")) {
                out.println("Nothing deleted.");
                return;
            }
            petCare.removeAccount(a.getId());
            out.println("Deleted account " + a.getName() + " and " + toDelete.size() + " pet(s).");
        } catch (NoSuchElementException | CannotRemoveLastOwnerException | IllegalArgumentException e) {
            out.println(e.getMessage());
        }
    }

    private void deletePet() {
        try {
            Optional<Account> owner = chooseAccountByName("Owner name: ");
            if (owner.isEmpty()) {
                return;
            }
            Optional<Pet> pet = choosePetOf(owner.get());
            if (pet.isEmpty()) {
                return;
            }
            Pet p = pet.get();
            if (!input.readYesNo("Delete pet " + p.getName() + "? (y/N): ")) {
                out.println("Nothing deleted.");
                return;
            }
            petCare.deletePet(p.getId());
            out.println("Deleted pet " + p.getName() + ".");
        } catch (NoSuchElementException | IllegalArgumentException e) {
            out.println(e.getMessage());
        }
    }

    private void addCoOwner() {
        try {
            Optional<Account> owner = chooseAccountByName("Existing owner name: ");
            if (owner.isEmpty()) {
                return;
            }
            Optional<Pet> pet = choosePetOf(owner.get());
            if (pet.isEmpty()) {
                return;
            }
            Optional<Account> coOwner = chooseAccountByName("New co-owner name: ");
            if (coOwner.isEmpty()) {
                return;
            }
            Pet p = pet.get();
            Account c = coOwner.get();
            if (petCare.addOwner(p.getId(), c.getId(), today)) {
                out.println("Added " + c.getName() + " as co-owner of " + p.getName() + ".");
            } else {
                out.println(c.getName() + " already owns " + p.getName() + ".");
            }
        } catch (AdminCannotOwnPetException | NoSuchElementException | IllegalArgumentException e) {
            out.println(e.getMessage());
        }
    }

    private void removeCoOwner() {
        try {
            Optional<Account> account = chooseAccountByName("Name of the owner to remove: ");
            if (account.isEmpty()) {
                return;
            }
            Optional<Pet> pet = choosePetOf(account.get());
            if (pet.isEmpty()) {
                return;
            }
            Pet p = pet.get();
            if (petCare.removeOwner(p.getId(), account.get().getId())) {
                out.println("Removed " + account.get().getName() + " from " + p.getName() + ".");
            } else {
                out.println(account.get().getName() + " does not own " + p.getName() + ".");
            }
        } catch (CannotRemoveLastOwnerException | NoSuchElementException | IllegalArgumentException e) {
            out.println(e.getMessage());
        }
    }

    private void addCarePlan() {
        try {
            Optional<Account> owner = chooseAccountByName("Owner name: ");
            if (owner.isEmpty()) {
                return;
            }
            Optional<Pet> pet = choosePetOf(owner.get());
            if (pet.isEmpty()) {
                return;
            }
            String title = input.readNonBlank("Title: ");
            PlanKind kind = input.readChoice("Kind", PlanKind.class);
            LocalDate start = input.readDate("Start date (yyyy-MM-dd): ");
            Integer interval = input.readOptionalInt("Interval in days (blank for one-off): ").orElse(null);
            LocalDate end = interval == null
                    ? null
                    : input.readOptionalDate("End date (yyyy-MM-dd, blank for none): ").orElse(null);
            CarePlan plan = petCare.addCarePlan(pet.get().getId(), title, kind, start, interval, end);
            out.println("Added care plan " + plan.getId() + ".");
        } catch (NoSuchElementException | IllegalArgumentException e) {
            out.println(e.getMessage());
        }
    }

    private void listCarePlans() {
        try {
            Optional<Account> owner = chooseAccountByName("Owner name: ");
            if (owner.isEmpty()) {
                return;
            }
            Optional<Pet> pet = choosePetOf(owner.get());
            if (pet.isEmpty()) {
                return;
            }
            List<CarePlan> plans = petCare.plansOf(pet.get().getId());
            if (plans.isEmpty()) {
                out.println("No care plans yet.");
            }
            for (CarePlan p : plans) {
                String schedule = p.getIntervalDays() == null
                        ? "one-off on " + p.getStartDate()
                        : "every " + p.getIntervalDays() + " days from " + p.getStartDate()
                                + (p.getEndDate() == null ? "" : " until " + p.getEndDate());
                out.println(p.getTitle() + " (" + p.getKind() + "), " + schedule
                        + ", last given " + (p.getLastGivenDate() == null ? "never" : p.getLastGivenDate())
                        + ", next due " + p.nextDue().map(LocalDate::toString).orElse("-")
                        + ", " + p.statusOn(today));
            }
        } catch (NoSuchElementException | IllegalArgumentException e) {
            out.println(e.getMessage());
        }
    }
}
