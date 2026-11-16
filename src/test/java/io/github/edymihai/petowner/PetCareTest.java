package io.github.edymihai.petowner;

import static org.junit.jupiter.api.Assertions.assertNull;
import io.github.edymihai.petowner.careplan.PlanKind;
import io.github.edymihai.petowner.careplan.CarePlan;
import io.github.edymihai.petowner.careplan.DueStatus;
import io.github.edymihai.petowner.careplan.TodayItem;
import io.github.edymihai.petowner.account.Account;
import io.github.edymihai.petowner.account.InMemoryAccountDirectory;
import io.github.edymihai.petowner.account.Role;
import io.github.edymihai.petowner.careplan.InMemoryCarePlanBook;
import io.github.edymihai.petowner.pet.InMemoryPetRegister;
import io.github.edymihai.petowner.pet.Pet;
import io.github.edymihai.petowner.pet.Species;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PetCareTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 12);

    private PetCare petCare;

    @BeforeEach
    void setUp() {
        petCare = new PetCare(new InMemoryAccountDirectory(), new InMemoryPetRegister(),
            new InMemoryCarePlanBook());
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

    @Test
    void addCarePlanSavesAPlanForThePet() {
        Pet rex = pet(owner("Ana"), "Rex");

        CarePlan plan = petCare.addCarePlan(rex.getId(), "Pills", PlanKind.MEDICATION,
                TODAY, 7, null);

        assertEquals(1, plan.getId());
        assertEquals(rex.getId(), plan.getPetId());
        assertNull(plan.getLastGivenDate());
        assertEquals(List.of(plan), petCare.plansOf(rex.getId()));
    }

    @Test
    void addCarePlanForAnUnknownPetFails() {
        assertThrows(NoSuchElementException.class,
                () -> petCare.addCarePlan(99L, "Pills", PlanKind.MEDICATION, TODAY, 7, null));
    }

    @Test
    void addCarePlanWithBrokenRuleIsNotSaved() {
        Pet rex = pet(owner("Ana"), "Rex");

        assertThrows(IllegalArgumentException.class,
                () -> petCare.addCarePlan(rex.getId(), "Shot", PlanKind.VACCINE, TODAY, null, TODAY));
        assertTrue(petCare.plansOf(rex.getId()).isEmpty());
    }

    @Test
    void plansOfListsOnlyThatPetsPlans() {
        Account ana = owner("Ana");
        Pet rex = pet(ana, "Rex");
        Pet tom = pet(ana, "Tom");
        petCare.addCarePlan(rex.getId(), "A", PlanKind.OTHER, TODAY, 7, null);
        petCare.addCarePlan(tom.getId(), "B", PlanKind.OTHER, TODAY, 7, null);

        assertEquals(1, petCare.plansOf(rex.getId()).size());
        assertEquals("B", petCare.plansOf(tom.getId()).get(0).getTitle());
        assertThrows(NoSuchElementException.class, () -> petCare.plansOf(99L));
    }

    @Test
    void deletePetDeletesItsCarePlansButNotOthers() {
        Account ana = owner("Ana");
        Pet rex = pet(ana, "Rex");
        Pet tom = pet(ana, "Tom");
        petCare.addCarePlan(rex.getId(), "A", PlanKind.OTHER, TODAY, 7, null);
        petCare.addCarePlan(tom.getId(), "B", PlanKind.OTHER, TODAY, 7, null);

        petCare.deletePet(rex.getId());

        assertThrows(NoSuchElementException.class, () -> petCare.plansOf(rex.getId()));
        assertEquals(1, petCare.plansOf(tom.getId()).size());
    }

    @Test
    void removeAccountDeletesCarePlansOfSoleOwnedPetsOnly() {
        Account ana = owner("Ana");
        Account bob = owner("Bob");
        Pet solo = pet(ana, "Solo");
        Pet shared = pet(ana, "Shared");
        petCare.addOwner(shared.getId(), bob.getId(), TODAY);
        petCare.addCarePlan(solo.getId(), "A", PlanKind.OTHER, TODAY, 7, null);
        petCare.addCarePlan(shared.getId(), "B", PlanKind.OTHER, TODAY, 7, null);

        petCare.removeAccount(ana.getId());

        assertEquals(1, petCare.plansOf(shared.getId()).size());
        assertEquals(1, petCare.listPets().size());
    }

    // ---- today list and mark as given

    @Test
    void todayForKeepsOnlyOverdueDueTodayAndGivenToday() {
        Account ana = owner("Ana");
        Pet rex = pet(ana, "Rex");
        petCare.addCarePlan(rex.getId(), "Overdue", PlanKind.MEDICATION, TODAY.minusDays(3), 7, null);
        petCare.addCarePlan(rex.getId(), "DueToday", PlanKind.MEDICATION, TODAY, 7, null);
        CarePlan given = petCare.addCarePlan(rex.getId(), "Given", PlanKind.MEDICATION, TODAY.minusDays(5), 7, null);
        petCare.markGiven(given.getId(), TODAY, TODAY);
        petCare.addCarePlan(rex.getId(), "Future", PlanKind.MEDICATION, TODAY.plusDays(2), 7, null);
        petCare.addCarePlan(rex.getId(), "Ended", PlanKind.MEDICATION, TODAY.minusDays(20), 7, TODAY.minusDays(10));
        CarePlan done = petCare.addCarePlan(rex.getId(), "Done", PlanKind.VACCINE, TODAY.minusDays(9), null, null);
        petCare.markGiven(done.getId(), TODAY.minusDays(1), TODAY);

        List<TodayItem> items = petCare.todayFor(ana.getId(), TODAY);

        assertEquals(List.of("Overdue", "DueToday", "Given"),
                items.stream().map(TodayItem::planTitle).toList());
    }

    @Test
    void todayForSortsByStatusThenNextDueThenPetName() {
        Account ana = owner("Ana");
        Pet zed = pet(ana, "Zed");
        Pet abe = pet(ana, "Abe");
        CarePlan given = petCare.addCarePlan(abe.getId(), "G", PlanKind.OTHER, TODAY.minusDays(5), 7, null);
        petCare.markGiven(given.getId(), TODAY, TODAY);
        petCare.addCarePlan(abe.getId(), "D-abe", PlanKind.OTHER, TODAY, 7, null);
        petCare.addCarePlan(zed.getId(), "D-zed", PlanKind.OTHER, TODAY, 7, null);
        petCare.addCarePlan(zed.getId(), "O-late", PlanKind.OTHER, TODAY.minusDays(1), 7, null);
        petCare.addCarePlan(abe.getId(), "O-early", PlanKind.OTHER, TODAY.minusDays(4), 7, null);

        List<String> titles = petCare.todayFor(ana.getId(), TODAY).stream().map(TodayItem::planTitle).toList();

        assertEquals(List.of("O-early", "O-late", "D-abe", "D-zed", "G"), titles);
    }

    @Test
    void todayForFillsInTheItemFields() {
        Account ana = owner("Ana");
        Pet rex = pet(ana, "Rex");
        CarePlan plan = petCare.addCarePlan(rex.getId(), "Pills", PlanKind.MEDICATION, TODAY.minusDays(2), 7, null);

        TodayItem item = petCare.todayFor(ana.getId(), TODAY).get(0);

        assertEquals("Rex", item.petName());
        assertEquals(plan.getId(), item.planId());
        assertEquals(DueStatus.OVERDUE, item.status());
        assertEquals(Optional.of(TODAY.minusDays(2)), item.nextDue());
        assertNull(item.lastGiven());
    }

    @Test
    void todayForListsOnlyThePetsOfThatAccount() {
        Account ana = owner("Ana");
        Account bob = owner("Bob");
        petCare.addCarePlan(pet(ana, "Rex").getId(), "Mine", PlanKind.OTHER, TODAY, 7, null);
        petCare.addCarePlan(pet(bob, "Tom").getId(), "Theirs", PlanKind.OTHER, TODAY, 7, null);

        assertEquals(1, petCare.todayFor(ana.getId(), TODAY).size());
        assertEquals("Theirs", petCare.todayFor(bob.getId(), TODAY).get(0).planTitle());
    }

    @Test
    void aCoOwnerSeesTheSameList() {
        Account ana = owner("Ana");
        Account bob = owner("Bob");
        Pet rex = pet(ana, "Rex");
        petCare.addOwner(rex.getId(), bob.getId(), TODAY);
        petCare.addCarePlan(rex.getId(), "Pills", PlanKind.OTHER, TODAY, 7, null);

        assertEquals(petCare.todayFor(ana.getId(), TODAY), petCare.todayFor(bob.getId(), TODAY));
        assertEquals(1, petCare.todayFor(bob.getId(), TODAY).size());
    }

    @Test
    void todayForAnAccountWithoutPetsIsEmpty() {
        assertTrue(petCare.todayFor(owner("Ana").getId(), TODAY).isEmpty());
    }

    @Test
    void todayForAnUnknownAccountFails() {
        assertThrows(NoSuchElementException.class, () -> petCare.todayFor(99, TODAY));
    }

    @Test
    void markGivenSavesTheNewLastGivenDate() {
        Account ana = owner("Ana");
        Pet rex = pet(ana, "Rex");
        CarePlan plan = petCare.addCarePlan(rex.getId(), "Pills", PlanKind.OTHER, TODAY.minusDays(2), 7, null);

        petCare.markGiven(plan.getId(), TODAY, TODAY);

        assertEquals(TODAY, petCare.plansOf(rex.getId()).get(0).getLastGivenDate());
        assertTrue(petCare.todayFor(ana.getId(), TODAY).stream()
                .allMatch(i -> i.status() == DueStatus.GIVEN_TODAY));
    }

    @Test
    void markGivenForAnUnknownPlanFails() {
        assertThrows(NoSuchElementException.class, () -> petCare.markGiven(99, TODAY, TODAY));
    }

    @Test
    void markGivenPassesOnTheRulesOfThePlan() {
        Account ana = owner("Ana");
        CarePlan plan = petCare.addCarePlan(pet(ana, "Rex").getId(), "Shot", PlanKind.VACCINE, TODAY, null, null);

        assertThrows(IllegalArgumentException.class, () -> petCare.markGiven(plan.getId(), TODAY.plusDays(1), TODAY));
        petCare.markGiven(plan.getId(), TODAY, TODAY);
        assertThrows(AlreadyGivenException.class, () -> petCare.markGiven(plan.getId(), TODAY, TODAY));
    }
}
