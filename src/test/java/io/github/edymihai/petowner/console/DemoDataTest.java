package io.github.edymihai.petowner.console;

import io.github.edymihai.petowner.PetCare;
import io.github.edymihai.petowner.account.Account;
import io.github.edymihai.petowner.account.InMemoryAccountDirectory;
import io.github.edymihai.petowner.careplan.DueStatus;
import io.github.edymihai.petowner.careplan.InMemoryCarePlanBook;
import io.github.edymihai.petowner.careplan.TodayItem;
import io.github.edymihai.petowner.pet.InMemoryPetRegister;
import io.github.edymihai.petowner.pet.Pet;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DemoDataTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 19);

    private static PetCare newPetCare() {
        return new PetCare(new InMemoryAccountDirectory(), new InMemoryPetRegister(), new InMemoryCarePlanBook());
    }

    @Test
    void loadsTheExpectedCounts() {
        PetCare petCare = newPetCare();

        DemoData.Summary summary = DemoData.load(petCare, TODAY);

        assertEquals(new DemoData.Summary(4, 5, 8), summary);
        assertEquals(4, petCare.listAccounts().size());
        assertEquals(5, petCare.listPets().size());
        assertTrue(petCare.listPets().stream().anyMatch(p -> p.getOwners().size() == 2));
    }

    @Test
    void todayListShowsOverdueDueTodayAndGivenTodayOnly() {
        PetCare petCare = newPetCare();
        DemoData.load(petCare, TODAY);

        Map<DueStatus, Set<String>> titlesByStatus = new HashMap<>();
        for (Account account : petCare.listAccounts()) {
            for (TodayItem item : petCare.todayFor(account.getId(), TODAY)) {
                titlesByStatus.computeIfAbsent(item.status(), s -> new HashSet<>()).add(item.planTitle());
            }
        }

        assertEquals(Set.of("Flea and tick tablet", "Rabies vaccine"), titlesByStatus.get(DueStatus.OVERDUE));
        assertEquals(Set.of("Dental checkup"), titlesByStatus.get(DueStatus.DUE_TODAY));
        assertEquals(Set.of("Joint supplement"), titlesByStatus.get(DueStatus.GIVEN_TODAY));
        Set<String> shown = new HashSet<>();
        titlesByStatus.values().forEach(shown::addAll);
        for (String hidden : Set.of("First checkup", "Antibiotic course", "Deworming", "Beak and nail trim")) {
            assertFalse(shown.contains(hidden), hidden + " must not be on the Today list");
        }
    }

    @Test
    void everyPlanHasTheIntendedStatus() {
        PetCare petCare = newPetCare();
        DemoData.load(petCare, TODAY);

        Map<String, DueStatus> statuses = new HashMap<>();
        for (Pet pet : petCare.listPets()) {
            petCare.plansOf(pet.getId()).forEach(p -> statuses.put(p.getTitle(), p.statusOn(TODAY)));
        }

        assertEquals(DueStatus.NOT_DUE, statuses.get("Beak and nail trim"));
        assertEquals(DueStatus.NOT_STARTED, statuses.get("First checkup"));
        assertEquals(DueStatus.ENDED, statuses.get("Antibiotic course"));
        assertEquals(DueStatus.DONE, statuses.get("Deworming"));
    }

    @Test
    void loadingWithAnotherTodayDoesNotThrow() {
        LocalDate leapDay = LocalDate.of(2024, 2, 29);

        DemoData.Summary summary = assertDoesNotThrow(() -> DemoData.load(newPetCare(), leapDay));

        assertEquals(new DemoData.Summary(4, 5, 8), summary);
    }
}
