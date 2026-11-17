package io.github.edymihai.petowner.console;

import io.github.edymihai.petowner.AdminCannotOwnPetException;
import io.github.edymihai.petowner.AlreadyGivenException;
import io.github.edymihai.petowner.CannotRemoveLastOwnerException;
import io.github.edymihai.petowner.DuplicateEmailException;
import io.github.edymihai.petowner.EndOfInputException;
import io.github.edymihai.petowner.PetCare;
import io.github.edymihai.petowner.account.Account;
import io.github.edymihai.petowner.account.Role;
import io.github.edymihai.petowner.careplan.CarePlan;
import io.github.edymihai.petowner.careplan.PlanKind;
import io.github.edymihai.petowner.careplan.TodayItem;
import io.github.edymihai.petowner.pet.Pet;
import io.github.edymihai.petowner.pet.Species;
import io.github.edymihai.petowner.report.HistoryFile;
import io.github.edymihai.petowner.report.PetHistoryReport;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
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
    private final Path exportDir;
    private final PetHistoryReport report = new PetHistoryReport();
    private final HistoryFile historyFile = new HistoryFile();
    private final Style style;

    public Menu(Input input, PetCare petCare, PrintStream out, LocalDate today, Path exportDir, Style style) {
        this.input = input;
        this.petCare = petCare;
        this.out = out;
        this.today = today;
        this.exportDir = exportDir;
        this.style = style;
    }

    /** Runs until the user picks 0 or the input ends. */
    public void run() {
        try {
            boolean running = true;
            while (running) {
                printTitle("MAIN MENU");
                out.println("1) Accounts");
                out.println("2) Pets");
                out.println("3) Care plans");
                out.println("0) Exit");
                switch (input.readInt("Choose: ", 0, 3)) {
                    case 1 -> accountsMenu();
                    case 2 -> petsMenu();
                    case 3 -> carePlansMenu();
                    default -> running = false;
                }
            }
        } catch (EndOfInputException e) {
            out.println();
        }
        out.println("Goodbye.");
    }

    /** A blank line, then the title in capitals. */
    private void printTitle(String title) {
        out.println();
        out.println(title);
    }

    /** Loops until the user picks 0 (back). */
    private void accountsMenu() {
        boolean inMenu = true;
        while (inMenu) {
            printTitle("ACCOUNTS");
            out.println("1) Add");
            out.println("2) List");
            out.println("3) Delete");
            out.println("0) Back");
            switch (input.readInt("Choose: ", 0, 3)) {
                case 1 -> addAccount();
                case 2 -> listAccounts();
                case 3 -> deleteAccount();
                default -> inMenu = false;
            }
        }
    }

    private void petsMenu() {
        boolean inMenu = true;
        while (inMenu) {
            printTitle("PETS");
            out.println("1) Add");
            out.println("2) List");
            out.println("3) Delete");
            out.println("4) Add co-owner");
            out.println("5) Remove co-owner");
            out.println("6) Export history");
            out.println("7) View an exported history");
            out.println("0) Back");
            switch (input.readInt("Choose: ", 0, 7)) {
                case 1 -> addPet();
                case 2 -> listPets();
                case 3 -> deletePet();
                case 4 -> addCoOwner();
                case 5 -> removeCoOwner();
                case 6 -> exportHistory();
                case 7 -> viewHistory();
                default -> inMenu = false;
            }
        }
    }

    private void carePlansMenu() {
        boolean inMenu = true;
        while (inMenu) {
            printTitle("CARE PLANS");
            out.println("1) Add");
            out.println("2) List");
            out.println("3) Today list");
            out.println("4) Mark as given");
            out.println("0) Back");
            switch (input.readInt("Choose: ", 0, 4)) {
                case 1 -> addCarePlan();
                case 2 -> listCarePlans();
                case 3 -> todayList();
                case 4 -> markPlanGiven();
                default -> inMenu = false;
            }
        }
    }

    private void addAccount() {
        String name = input.readNonBlank("Name: ");
        String email = input.readEmail("Email: ");
        Role role = input.readChoice("Role", Role.class);
        try {
            Account account = petCare.addAccount(name, email, role);
            out.println(style.success("Added account " + account.getId() + "."));
        } catch (DuplicateEmailException | IllegalArgumentException e) {
            out.println(style.error(e.getMessage()));
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
            out.println(style.success("Added pet " + pet.getId() + "."));
        } catch (AdminCannotOwnPetException | IllegalArgumentException | NoSuchElementException e) {
            out.println(style.error(e.getMessage()));
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
            out.println(style.error("No account named " + name + "."));
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
            out.println(style.error(account.getName() + " has no pets."));
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
            out.println(style.success("Deleted account " + a.getName() + " and " + toDelete.size() + " pet(s)."));
        } catch (NoSuchElementException | CannotRemoveLastOwnerException | IllegalArgumentException e) {
            out.println(style.error(e.getMessage()));
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
            out.println(style.success("Deleted pet " + p.getName() + "."));
        } catch (NoSuchElementException | IllegalArgumentException e) {
            out.println(style.error(e.getMessage()));
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
                out.println(style.success("Added " + c.getName() + " as co-owner of " + p.getName() + "."));
            } else {
                out.println(style.error(c.getName() + " already owns " + p.getName() + "."));
            }
        } catch (AdminCannotOwnPetException | NoSuchElementException | IllegalArgumentException e) {
            out.println(style.error(e.getMessage()));
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
                out.println(style.success("Removed " + account.get().getName() + " from " + p.getName() + "."));
            } else {
                out.println(style.error(account.get().getName() + " does not own " + p.getName() + "."));
            }
        } catch (CannotRemoveLastOwnerException | NoSuchElementException | IllegalArgumentException e) {
            out.println(style.error(e.getMessage()));
        }
    }

    private void exportHistory() {
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
            List<String> lines = report.format(petCare.historyOf(p.getId(), today));
            Path saved = historyFile.write(exportDir, HistoryFile.fileName(p.getName(), p.getId(), today), lines);
            out.println(style.success("Saved to " + saved));
        } catch (IOException e) {
            out.println(style.error("Could not write the file: " + e.getMessage()));
        } catch (NoSuchElementException | IllegalArgumentException e) {
            out.println(style.error(e.getMessage()));
        }
    }

    private void viewHistory() {
        try {
            List<Path> files = historyFile.list(exportDir);
            if (files.isEmpty()) {
                out.println("No exported files yet.");
                return;
            }
            for (int i = 0; i < files.size(); i++) {
                out.println((i + 1) + ") " + files.get(i).getFileName());
            }
            Path file = files.get(input.readInt("Which file? ", 1, files.size()) - 1);
            out.println();
            historyFile.read(file).forEach(out::println);
        } catch (IOException e) {
            out.println(style.error("Could not read the file: " + e.getMessage()));
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
            Integer interval = input.readOptionalInt("Repeat every N days (blank for one-off): ").orElse(null);
            LocalDate end = interval == null
                    ? null
                    : input.readOptionalDate("End date (yyyy-MM-dd, blank for none): ").orElse(null);
            CarePlan plan = petCare.addCarePlan(pet.get().getId(), title, kind, start, interval, end);
            out.println(style.success("Added care plan " + plan.getId() + "."));
        } catch (NoSuchElementException | IllegalArgumentException e) {
            out.println(style.error(e.getMessage()));
        }
    }

    private void todayList() {
        try {
            Optional<Account> owner = chooseAccountByName("Owner name: ");
            if (owner.isEmpty()) {
                return;
            }
            List<TodayItem> items = petCare.todayFor(owner.get().getId(), today);
            if (items.isEmpty()) {
                out.println("Nothing to do today.");
            }
            for (TodayItem i : items) {
                out.println(i.status() + ": " + i.petName() + " - " + i.planTitle() + " (" + i.kind() + ")"
                        + ", next due " + i.nextDue().map(LocalDate::toString).orElse("-")
                        + ", last given " + (i.lastGiven() == null ? "never" : i.lastGiven()));
            }
        } catch (NoSuchElementException | IllegalArgumentException e) {
            out.println(style.error(e.getMessage()));
        }
    }

    private void markPlanGiven() {
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
                out.println(style.error("No care plans yet."));
                return;
            }
            for (int i = 0; i < plans.size(); i++) {
                CarePlan p = plans.get(i);
                out.println((i + 1) + ") " + p.getTitle() + " (" + p.getKind() + "), " + p.statusOn(today));
            }
            CarePlan plan = plans.get(input.readInt("Which plan? ", 1, plans.size()) - 1);
            LocalDate givenOn = input.readOptionalDate("Date given (yyyy-MM-dd, blank for today): ").orElse(today);
            petCare.markGiven(plan.getId(), givenOn, today);
            out.println(style.success("Marked " + plan.getTitle() + " as given on " + givenOn + "."));
        } catch (AlreadyGivenException | NoSuchElementException | IllegalArgumentException e) {
            out.println(style.error(e.getMessage()));
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
                        : "repeats every " + p.getIntervalDays() + " days from " + p.getStartDate()
                                + (p.getEndDate() == null ? "" : " until " + p.getEndDate());
                out.println(p.getTitle() + " (" + p.getKind() + "), " + schedule
                        + ", last given " + (p.getLastGivenDate() == null ? "never" : p.getLastGivenDate())
                        + ", next due " + p.nextDue().map(LocalDate::toString).orElse("-")
                        + ", " + p.statusOn(today));
            }
        } catch (NoSuchElementException | IllegalArgumentException e) {
            out.println(style.error(e.getMessage()));
        }
    }
}
