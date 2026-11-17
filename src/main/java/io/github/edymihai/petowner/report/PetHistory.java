package io.github.edymihai.petowner.report;

import io.github.edymihai.petowner.careplan.CarePlan;
import io.github.edymihai.petowner.pet.Pet;

import java.time.LocalDate;
import java.util.List;

/** Everything the history report needs about one pet. Owners are names only. */
public record PetHistory(Pet pet, List<String> ownerNames, List<CarePlan> plans, LocalDate exportedOn) {

    public PetHistory {
        ownerNames = List.copyOf(ownerNames);
        plans = List.copyOf(plans);
    }
}
