package io.github.edymihai.petowner.report;

import io.github.edymihai.petowner.careplan.CarePlan;
import io.github.edymihai.petowner.pet.Pet;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Turns a PetHistory into text lines. Pure formatting: no file access, never reads the clock. */
public class PetHistoryReport {

    /** One section per private method, so a VET VISITS section can be appended later. */
    public List<String> format(PetHistory history) {
        List<String> lines = new ArrayList<>();
        header(history, lines);
        lines.add("");
        carePlans(history, lines);
        return lines;
    }

    private void header(PetHistory history, List<String> lines) {
        Pet pet = history.pet();
        lines.add("Pet history: " + pet.getName());
        lines.add("Species: " + pet.getSpecies());
        lines.add("Born: " + (pet.getBirthDate() == null ? "unknown" : pet.getBirthDate()));
        lines.add("Owners: " + String.join(", ", history.ownerNames()));
        lines.add("Exported on " + history.exportedOn());
    }

    private void carePlans(PetHistory history, List<String> lines) {
        lines.add("CARE PLANS");
        if (history.plans().isEmpty()) {
            lines.add("No care plans.");
            return;
        }
        for (CarePlan plan : history.plans()) {
            lines.add(plan.getTitle() + " (" + plan.getKind() + ")");
            lines.add("  Schedule: " + schedule(plan));
            lines.add("  Last given: " + (plan.getLastGivenDate() == null ? "never" : plan.getLastGivenDate()));
            lines.add("  Next due: " + plan.nextDue().map(LocalDate::toString).orElse("-"));
            lines.add("  Status: " + plan.statusOn(history.exportedOn()));
            lines.add("");
        }
        lines.remove(lines.size() - 1);
    }

    private String schedule(CarePlan plan) {
        if (plan.getIntervalDays() == null) {
            return "one-off on " + plan.getStartDate();
        }
        return "repeats every " + plan.getIntervalDays() + " days from " + plan.getStartDate()
                + (plan.getEndDate() == null ? "" : " until " + plan.getEndDate());
    }
}
