package io.github.edymihai.petowner.careplan;

import java.util.List;
import java.util.Optional;

public interface CarePlanBook {

    /** Stores a new care plan (assigning its id) or replaces an existing one. Returns the saved plan. */
    CarePlan save(CarePlan plan);

    Optional<CarePlan> findById(long id);

    List<CarePlan> findByPetId(long petId);

    void deleteByPetId(long petId);
}
