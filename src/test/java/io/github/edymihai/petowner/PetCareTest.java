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
}
