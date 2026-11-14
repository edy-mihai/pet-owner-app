package io.github.edymihai.petowner;

import io.github.edymihai.petowner.account.Account;
import io.github.edymihai.petowner.account.InMemoryAccountDirectory;
import io.github.edymihai.petowner.account.Role;
import io.github.edymihai.petowner.pet.InMemoryPetRegister;
import io.github.edymihai.petowner.pet.Pet;
import io.github.edymihai.petowner.pet.Species;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PetCareTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 12);

    private PetCare petCare;

    @BeforeEach
    void setUp() {
        petCare = new PetCare(new InMemoryAccountDirectory(), new InMemoryPetRegister());
    }

    @Test
    void addAccountSavesAndListsIt() {
        Account account = petCare.addAccount("Ana", "ana@example.com", Role.OWNER);

        assertEquals(1, account.getId());
        assertEquals(1, petCare.listAccounts().size());
    }

    @Test
    void addAccountRejectsDuplicateEmail() {
        petCare.addAccount("Ana", "ana@example.com", Role.OWNER);

        assertThrows(DuplicateEmailException.class,
                () -> petCare.addAccount("Other", "ana@example.com", Role.OWNER));
    }

    @Test
    void addPetMakesTheAccountTheFirstOwnerSinceToday() {
        Account ana = petCare.addAccount("Ana", "ana@example.com", Role.OWNER);

        Pet pet = petCare.addPet(ana.getId(), "Rex", Species.DOG, null, null, TODAY);

        assertEquals(1, pet.getId());
        assertEquals(TODAY, pet.getOwners().get(ana.getId()));
        assertEquals(1, petCare.listPets().size());
    }

    @Test
    void adminCannotOwnAPet() {
        Account admin = petCare.addAccount("Root", "root@example.com", Role.ADMIN);

        assertThrows(AdminCannotOwnPetException.class,
                () -> petCare.addPet(admin.getId(), "Rex", Species.DOG, null, null, TODAY));
        assertTrue(petCare.listPets().isEmpty());
    }

    @Test
    void findAccountsByNameIgnoresCaseAndSurroundingSpaces() {
        Account ana = petCare.addAccount("Ana", "ana@example.com", Role.OWNER);

        List<Account> found = petCare.findAccountsByName("  aNA ");

        assertEquals(1, found.size());
        assertEquals(ana.getId(), found.get(0).getId());
    }

    @Test
    void findAccountsByNameMatchesTheWholeNameOnly() {
        petCare.addAccount("Ana", "ana@example.com", Role.OWNER);
        petCare.addAccount("Anabel", "anabel@example.com", Role.OWNER);

        assertEquals(1, petCare.findAccountsByName("Ana").size());
        assertTrue(petCare.findAccountsByName("An").isEmpty());
    }

    @Test
    void findAccountsByNameReturnsNothingWhenNoneMatch() {
        assertTrue(petCare.findAccountsByName("Nobody").isEmpty());
    }

    @Test
    void findAccountsByNameReturnsAllAccountsWithThatName() {
        petCare.addAccount("Ana", "a1@example.com", Role.OWNER);
        petCare.addAccount("ana", "a2@example.com", Role.OWNER);
        petCare.addAccount("Bob", "bob@example.com", Role.OWNER);

        assertEquals(2, petCare.findAccountsByName("ANA").size());
    }

    @Test
    void requireCanOwnPetsAcceptsAnOwner() {
        Account ana = petCare.addAccount("Ana", "ana@example.com", Role.OWNER);

        petCare.requireCanOwnPets(ana.getId());
    }

    @Test
    void requireCanOwnPetsRejectsAnAdmin() {
        Account admin = petCare.addAccount("Root", "root@example.com", Role.ADMIN);

        assertThrows(AdminCannotOwnPetException.class, () -> petCare.requireCanOwnPets(admin.getId()));
    }

    @Test
    void requireCanOwnPetsRejectsAnUnknownId() {
        assertThrows(NoSuchElementException.class, () -> petCare.requireCanOwnPets(99L));
    }

    @Test
    void addPetForAnUnknownAccountFails() {
        assertThrows(NoSuchElementException.class,
                () -> petCare.addPet(99L, "Rex", Species.DOG, null, null, TODAY));
    }

    private Account owner(String name) {
        return petCare.addAccount(name, name.toLowerCase() + "@example.com", Role.OWNER);
    }

    private Pet pet(Account owner, String name) {
        return petCare.addPet(owner.getId(), name, Species.DOG, null, null, TODAY);
    }

    @Test
    void removeAccountDeletesAPetItOwnsAlone() {
        Account ana = owner("Ana");
        pet(ana, "Rex");

        petCare.removeAccount(ana.getId());

        assertTrue(petCare.listPets().isEmpty());
        assertTrue(petCare.listAccounts().isEmpty());
    }

    @Test
    void removeAccountKeepsACoOwnedPetWithTheRemainingOwner() {
        Account ana = owner("Ana");
        Account bob = owner("Bob");
        Pet rex = pet(ana, "Rex");
        petCare.addOwner(rex.getId(), bob.getId(), TODAY);

        petCare.removeAccount(ana.getId());

        assertEquals(1, petCare.listPets().size());
        assertTrue(rex.isOwnedBy(bob.getId()));
        assertFalse(rex.isOwnedBy(ana.getId()));
        assertEquals(1, petCare.listAccounts().size());
    }

    @Test
    void removeAccountWithOneSharedAndOneSolePet() {
        Account ana = owner("Ana");
        Account bob = owner("Bob");
        Pet shared = pet(ana, "Shared");
        pet(ana, "Solo");
        petCare.addOwner(shared.getId(), bob.getId(), TODAY);

        petCare.removeAccount(ana.getId());

        assertEquals(List.of("Shared"), petCare.listPets().stream().map(Pet::getName).toList());
        assertTrue(shared.isOwnedBy(bob.getId()));
    }

    @Test
    void removeAccountWorksForAnAdmin() {
        Account admin = petCare.addAccount("Root", "root@example.com", Role.ADMIN);

        petCare.removeAccount(admin.getId());

        assertTrue(petCare.listAccounts().isEmpty());
    }

    @Test
    void removeAccountWithAnUnknownIdFails() {
        assertThrows(NoSuchElementException.class, () -> petCare.removeAccount(99L));
    }

    @Test
    void petsOnlyOwnedByIgnoresSharedPetsAndChangesNothing() {
        Account ana = owner("Ana");
        Account bob = owner("Bob");
        Pet shared = pet(ana, "Shared");
        pet(ana, "Solo");
        petCare.addOwner(shared.getId(), bob.getId(), TODAY);

        List<Pet> sole = petCare.petsOnlyOwnedBy(ana.getId());

        assertEquals(List.of("Solo"), sole.stream().map(Pet::getName).toList());
        assertEquals(2, petCare.listPets().size());
        assertTrue(petCare.petsOnlyOwnedBy(bob.getId()).isEmpty());
    }

    @Test
    void deletePetRemovesIt() {
        Pet rex = pet(owner("Ana"), "Rex");

        petCare.deletePet(rex.getId());

        assertTrue(petCare.listPets().isEmpty());
    }

    @Test
    void deletePetWithAnUnknownIdFails() {
        assertThrows(NoSuchElementException.class, () -> petCare.deletePet(99L));
    }

    @Test
    void addOwnerAddsACoOwnerSinceToday() {
        Account ana = owner("Ana");
        Account bob = owner("Bob");
        Pet rex = pet(ana, "Rex");
        LocalDate later = TODAY.plusDays(1);

        assertTrue(petCare.addOwner(rex.getId(), bob.getId(), later));

        assertEquals(later, rex.getOwners().get(bob.getId()));
    }

    @Test
    void addOwnerReturnsFalseWhenAlreadyAnOwner() {
        Account ana = owner("Ana");
        Pet rex = pet(ana, "Rex");

        assertFalse(petCare.addOwner(rex.getId(), ana.getId(), TODAY.plusDays(1)));

        assertEquals(TODAY, rex.getOwners().get(ana.getId()));
    }

    @Test
    void addOwnerRejectsAnAdmin() {
        Pet rex = pet(owner("Ana"), "Rex");
        Account admin = petCare.addAccount("Root", "root@example.com", Role.ADMIN);

        assertThrows(AdminCannotOwnPetException.class,
                () -> petCare.addOwner(rex.getId(), admin.getId(), TODAY));
        assertEquals(1, rex.getOwners().size());
    }

    @Test
    void addOwnerWithUnknownIdsFails() {
        Account ana = owner("Ana");
        Pet rex = pet(ana, "Rex");

        assertThrows(NoSuchElementException.class, () -> petCare.addOwner(99L, ana.getId(), TODAY));
        assertThrows(NoSuchElementException.class, () -> petCare.addOwner(rex.getId(), 99L, TODAY));
    }

    @Test
    void removeOwnerRemovesACoOwner() {
        Account ana = owner("Ana");
        Account bob = owner("Bob");
        Pet rex = pet(ana, "Rex");
        petCare.addOwner(rex.getId(), bob.getId(), TODAY);

        assertTrue(petCare.removeOwner(rex.getId(), bob.getId()));

        assertFalse(rex.isOwnedBy(bob.getId()));
        assertTrue(rex.isOwnedBy(ana.getId()));
    }

    @Test
    void removeOwnerRefusesTheLastOwner() {
        Account ana = owner("Ana");
        Pet rex = pet(ana, "Rex");

        assertThrows(CannotRemoveLastOwnerException.class,
                () -> petCare.removeOwner(rex.getId(), ana.getId()));
        assertTrue(rex.isOwnedBy(ana.getId()));
    }

    @Test
    void removeOwnerReturnsFalseForANonOwner() {
        Account bob = owner("Bob");
        Pet rex = pet(owner("Ana"), "Rex");

        assertFalse(petCare.removeOwner(rex.getId(), bob.getId()));
    }

    @Test
    void ownersOfListsTheOwnerAccountsAndPetsOfListsTheirPets() {
        Account ana = owner("Ana");
        Account bob = owner("Bob");
        Pet rex = pet(ana, "Rex");
        petCare.addOwner(rex.getId(), bob.getId(), TODAY);

        assertEquals(2, petCare.ownersOf(rex.getId()).size());
        assertTrue(petCare.ownersOf(rex.getId()).stream().anyMatch(a -> a.getName().equals("Bob")));
        assertEquals(1, petCare.petsOf(bob.getId()).size());
        assertThrows(NoSuchElementException.class, () -> petCare.ownersOf(99L));
    }
}
