package io.github.edymihai.petowner;

import io.github.edymihai.petowner.account.Account;
import io.github.edymihai.petowner.account.AccountDirectory;
import io.github.edymihai.petowner.account.Role;
import io.github.edymihai.petowner.careplan.CarePlan;
import io.github.edymihai.petowner.careplan.CarePlanBook;
import io.github.edymihai.petowner.careplan.PlanKind;
import io.github.edymihai.petowner.pet.Pet;
import io.github.edymihai.petowner.pet.PetRegister;
import io.github.edymihai.petowner.pet.Species;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/** Operations that span several objects. Depends only on the storage interfaces. */
public class PetCare {

    private final AccountDirectory accounts;
    private final PetRegister pets;
    private final CarePlanBook plans;

    public PetCare(AccountDirectory accounts, PetRegister pets, CarePlanBook plans) {
        this.accounts = accounts;
        this.pets = pets;
        this.plans = plans;
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

    /** Pets whose only owner is the given account. Read-only. */
    public List<Pet> petsOnlyOwnedBy(long accountId) {
        return pets.findByOwner(accountId).stream()
                .filter(p -> p.getOwners().size() == 1)
                .toList();
    }

    /**
     * Rule R5: a pet the account owns alone is deleted, a shared pet just loses this owner.
     * The account is deleted last. Throws NoSuchElementException for an unknown id.
     * The care plans of every deleted pet are deleted too.
     * TODO (task 7): also delete the visits of every deleted pet.
     */
    public void removeAccount(long accountId) {
        if (accounts.findById(accountId).isEmpty()) {
            throw new NoSuchElementException("No account with id " + accountId);
        }
        for (Pet pet : pets.findByOwner(accountId)) {
            if (pet.getOwners().size() == 1) {
                plans.deleteByPetId(pet.getId());
                pets.delete(pet.getId());
            } else {
                pet.removeOwner(accountId);
                pets.save(pet);
            }
        }
        accounts.delete(accountId);
    }

    /**
     * Deletes a pet. Co-owners are equal, so there is no owner check.
     * Its care plans are deleted too.
     * TODO (task 7): also delete the pet's visits.
     */
    public void deletePet(long petId) {
        findPet(petId);
        plans.deleteByPetId(petId);
        pets.delete(petId);
    }

    /** Adds a co-owner since {@code today}. Returns false if the account already owns the pet. */
    public boolean addOwner(long petId, long accountId, LocalDate today) {
        requireCanOwnPets(accountId);
        Pet pet = findPet(petId);
        boolean added = pet.addOwner(accountId, today);
        pets.save(pet);
        return added;
    }

    /**
     * Removes an owner. Returns false if the account is not an owner;
     * throws CannotRemoveLastOwnerException if it is the last one.
     */
    public boolean removeOwner(long petId, long accountId) {
        Pet pet = findPet(petId);
        boolean removed = pet.removeOwner(accountId);
        pets.save(pet);
        return removed;
    }

    /** Adds a care plan that has never been given. Throws NoSuchElementException for an unknown pet. */
    public CarePlan addCarePlan(long petId, String title, PlanKind kind, LocalDate startDate,
                                Integer intervalDays, LocalDate endDate) {
        findPet(petId);
        return plans.save(new CarePlan(petId, title, kind, startDate, intervalDays, endDate, null));
    }

    /** The care plans of a pet. Throws NoSuchElementException for an unknown pet. */
    public List<CarePlan> plansOf(long petId) {
        findPet(petId);
        return plans.findByPetId(petId);
    }

    public List<Pet> petsOf(long accountId) {
        return pets.findByOwner(accountId);
    }

    /** The owner accounts of a pet. */
    public List<Account> ownersOf(long petId) {
        return findPet(petId).getOwners().keySet().stream()
                .map(accounts::findById)
                .flatMap(Optional::stream)
                .toList();
    }

    private Pet findPet(long petId) {
        return pets.findById(petId)
                .orElseThrow(() -> new NoSuchElementException("No pet with id " + petId));
    }

    public List<Account> listAccounts() {
        return accounts.list();
    }

    public List<Pet> listPets() {
        return pets.list();
    }
}
