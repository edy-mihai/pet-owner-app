package io.github.edymihai.petowner;

import io.github.edymihai.petowner.account.Account;
import io.github.edymihai.petowner.account.AccountDirectory;
import io.github.edymihai.petowner.account.Role;
import io.github.edymihai.petowner.pet.Pet;
import io.github.edymihai.petowner.pet.PetRegister;
import io.github.edymihai.petowner.pet.Species;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

/** Operations that span several objects. Depends only on the storage interfaces. */
public class PetCare {

    private final AccountDirectory accounts;
    private final PetRegister pets;

    public PetCare(AccountDirectory accounts, PetRegister pets) {
        this.accounts = accounts;
        this.pets = pets;
    }

    public Account addAccount(String name, String email, Role role) {
        return accounts.save(new Account(name, email, role));
    }

    /** Adds a pet whose first owner is the given account, owning it since {@code today}. */
    public Pet addPet(long ownerAccountId, String name, Species species,
                      LocalDate birthDate, String notes, LocalDate today) {
        requireCanOwnPets(ownerAccountId);
        return pets.save(new Pet(name, species, birthDate, notes, ownerAccountId, today));
    }

    /**
     * Rule R2 in one place: only non-admin accounts can own pets.
     * Throws NoSuchElementException for an unknown id and AdminCannotOwnPetException for an ADMIN.
     */
    public void requireCanOwnPets(long accountId) {
        Account account = accounts.findById(accountId)
                .orElseThrow(() -> new NoSuchElementException("No account with id " + accountId));
        if (account.getRole() == Role.ADMIN) {
            throw new AdminCannotOwnPetException(accountId);
        }
    }

    /** Accounts whose name equals the given name, ignoring case, after trimming the input. */
    public List<Account> findAccountsByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        String wanted = name.strip();
        return listAccounts().stream()
                .filter(a -> a.getName().equalsIgnoreCase(wanted))
                .toList();
    }

    public List<Account> listAccounts() {
        return accounts.list();
    }

    public List<Pet> listPets() {
        return pets.list();
    }
}
