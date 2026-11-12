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
        Account owner = accounts.findById(ownerAccountId)
                .orElseThrow(() -> new NoSuchElementException("No account with id " + ownerAccountId));
        if (owner.getRole() == Role.ADMIN) {
            throw new AdminCannotOwnPetException(ownerAccountId);
        }
        return pets.save(new Pet(name, species, birthDate, notes, ownerAccountId, today));
    }

    public List<Account> listAccounts() {
        return accounts.list();
    }

    public List<Pet> listPets() {
        return pets.list();
    }
}
