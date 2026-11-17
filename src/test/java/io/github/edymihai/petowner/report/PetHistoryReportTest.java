package io.github.edymihai.petowner.report;

import io.github.edymihai.petowner.careplan.CarePlan;
import io.github.edymihai.petowner.careplan.PlanKind;
import io.github.edymihai.petowner.pet.Pet;
import io.github.edymihai.petowner.pet.Species;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PetHistoryReportTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 13);

    private final PetHistoryReport report = new PetHistoryReport();

    private static Pet rex(LocalDate birthDate) {
        return new Pet("Rex", Species.DOG, birthDate, null, 1L, TODAY);
    }

    private List<String> format(Pet pet, List<String> owners, List<CarePlan> plans) {
        return report.format(new PetHistory(pet, owners, plans, TODAY));
    }

    @Test
    void headerHasNameSpeciesBirthDateOwnersAndExportDate() {
        List<String> lines = format(rex(LocalDate.of(2020, 5, 1)), List.of("Ana", "Bob"), List.of());

        assertEquals(List.of("Pet history: Rex", "Species: DOG", "Born: 2020-05-01", "Owners: Ana, Bob",
                "Exported on 2026-11-13", "", "CARE PLANS", "No care plans."), lines);
    }

    @Test
    void unknownBirthDateIsWrittenAsUnknown() {
        assertTrue(format(rex(null), List.of("Ana"), List.of()).contains("Born: unknown"));
    }

    @Test
    void ownersAreNamesOnlyNeverEmails() {
        String text = String.join("\n", format(rex(null), List.of("Ana"), List.of()));

        assertFalse(text.contains("@"));
    }

    @Test
    void oneOffPlanNeverGiven() {
        CarePlan shot = new CarePlan(1, "Rabies shot", PlanKind.VACCINE, LocalDate.of(2026, 11, 20), null, null, null);

        List<String> lines = format(rex(null), List.of("Ana"), List.of(shot));

        assertEquals(List.of("Rabies shot (VACCINE)", "  Schedule: one-off on 2026-11-20",
                "  Last given: never", "  Next due: 2026-11-20", "  Status: NOT_STARTED"),
                lines.subList(lines.indexOf("CARE PLANS") + 1, lines.size()));
    }

    @Test
    void recurringPlanWithEndDateAndLastGiven() {
        CarePlan pills = new CarePlan(1, "Pills", PlanKind.MEDICATION, LocalDate.of(2026, 11, 1), 7,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 11, 6));

        List<String> lines = format(rex(null), List.of("Ana"), List.of(pills));

        assertTrue(lines.contains("  Schedule: repeats every 7 days from 2026-11-01 until 2026-12-01"));
        assertTrue(lines.contains("  Last given: 2026-11-06"));
        assertTrue(lines.contains("  Next due: 2026-11-13"));
        assertTrue(lines.contains("  Status: DUE_TODAY"));
    }

    @Test
    void recurringPlanWithoutEndDateHasNoUntil() {
        CarePlan bath = new CarePlan(1, "Bath", PlanKind.OTHER, LocalDate.of(2026, 11, 1), 30, null, null);

        List<String> lines = format(rex(null), List.of("Ana"), List.of(bath));

        assertTrue(lines.contains("  Schedule: repeats every 30 days from 2026-11-01"));
        assertTrue(lines.contains("  Status: OVERDUE"));
    }

    @Test
    void aGivenOneOffHasNoNextDue() {
        CarePlan shot = new CarePlan(1, "Shot", PlanKind.VACCINE, LocalDate.of(2026, 11, 1), null, null,
                LocalDate.of(2026, 11, 2));

        List<String> lines = format(rex(null), List.of("Ana"), List.of(shot));

        assertTrue(lines.contains("  Next due: -"));
        assertTrue(lines.contains("  Status: DONE"));
    }

    @Test
    void severalPlansAreSeparatedByABlankLineWithoutATrailingOne() {
        CarePlan a = new CarePlan(1, "A", PlanKind.OTHER, TODAY, null, null, null);
        CarePlan b = new CarePlan(1, "B", PlanKind.OTHER, TODAY, null, null, null);

        List<String> lines = format(rex(null), List.of("Ana"), List.of(a, b));

        assertEquals("", lines.get(lines.indexOf("B (OTHER)") - 1));
        assertEquals("  Status: DUE_TODAY", lines.get(lines.size() - 1));
    }

    @Test
    void historyCopiesTheListsItIsGiven() {
        List<String> owners = new java.util.ArrayList<>(List.of("Ana"));
        PetHistory history = new PetHistory(rex(null), owners, new java.util.ArrayList<>(), TODAY);

        owners.add("Intruder");

        assertEquals(List.of("Ana"), history.ownerNames());
    }
}
