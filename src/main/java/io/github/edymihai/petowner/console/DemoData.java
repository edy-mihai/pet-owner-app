package io.github.edymihai.petowner.console;

import io.github.edymihai.petowner.PetCare;
import io.github.edymihai.petowner.account.Account;
import io.github.edymihai.petowner.account.Role;
import io.github.edymihai.petowner.careplan.CarePlan;
import io.github.edymihai.petowner.careplan.PlanKind;
import io.github.edymihai.petowner.pet.Pet;
import io.github.edymihai.petowner.pet.Species;

import java.time.LocalDate;

/**
 * Sample data for presentations. Uses only PetCare's public methods and computes every date from
 * the {@code today} parameter, so the Today list always has overdue, due-today and given-today items.
 */
public final class DemoData {

    /** How much was loaded. */
    public record Summary(int accounts, int pets, int plans) {
    }

    private DemoData() {
    }

    public static Summary load(PetCare petCare, LocalDate today) {
        petCare.addAccount("Demo Admin", "admin@example.com", Role.ADMIN);
        Account ana = petCare.addAccount("Ana Pop", "ana@example.com", Role.OWNER);
        Account bogdan = petCare.addAccount("Bogdan Ionescu", "bogdan@example.com", Role.OWNER);
        Account carmen = petCare.addAccount("Carmen Dima", "carmen@example.com", Role.OWNER);

        LocalDate since = today.minusYears(1);
        Pet rex = petCare.addPet(ana.getId(), "Rex", Species.DOG, today.minusYears(4), "Loves water", since);
        petCare.addOwner(rex.getId(), bogdan.getId(), today.minusMonths(6));
        Pet misu = petCare.addPet(ana.getId(), "Misu", Species.CAT, today.minusYears(2), null, since);
        Pet coco = petCare.addPet(bogdan.getId(), "Coco", Species.RABBIT, today.minusYears(1), null, since);
        Pet tweety = petCare.addPet(carmen.getId(), "Tweety", Species.BIRD, today.minusYears(3), null, since);
        Pet nemo = petCare.addPet(carmen.getId(), "Nemo", Species.OTHER, null, "Goldfish", since);

        // OVERDUE: recurring, started in the past, never given
        petCare.addCarePlan(rex.getId(), "Flea and tick tablet", PlanKind.PARASITE_TREATMENT,
                today.minusDays(60), 30, null);
        // OVERDUE: one-off
        petCare.addCarePlan(misu.getId(), "Rabies vaccine", PlanKind.VACCINE, today.minusDays(14), null, null);
        // DUE_TODAY
        petCare.addCarePlan(coco.getId(), "Dental checkup", PlanKind.CHECKUP, today, null, null);
        // GIVEN_TODAY
        CarePlan joint = petCare.addCarePlan(rex.getId(), "Joint supplement", PlanKind.MEDICATION,
                today.minusDays(30), 7, null);
        petCare.markGiven(joint.getId(), today, today);
        // NOT_DUE: given 5 days ago, every 30 days
        CarePlan trim = petCare.addCarePlan(tweety.getId(), "Beak and nail trim", PlanKind.CHECKUP,
                today.minusDays(10), 30, null);
        petCare.markGiven(trim.getId(), today.minusDays(5), today);
        // NOT_STARTED
        petCare.addCarePlan(nemo.getId(), "First checkup", PlanKind.CHECKUP, today.plusDays(30), null, null);
        // ENDED
        petCare.addCarePlan(misu.getId(), "Antibiotic course", PlanKind.MEDICATION,
                today.minusDays(30), 1, today.minusDays(20));
        // DONE: one-off, given
        CarePlan deworming = petCare.addCarePlan(coco.getId(), "Deworming", PlanKind.PARASITE_TREATMENT,
                today.minusDays(40), null, null);
        petCare.markGiven(deworming.getId(), today.minusDays(38), today);

        int plans = petCare.listPets().stream().mapToInt(p -> petCare.plansOf(p.getId()).size()).sum();
        return new Summary(petCare.listAccounts().size(), petCare.listPets().size(), plans);
    }
}
