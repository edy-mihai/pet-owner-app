package io.github.edymihai.petowner.careplan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryCarePlanBook implements CarePlanBook {

    private final Map<Long, CarePlan> plans = new HashMap<>();
    private long nextId = 1;

    @Override
    public CarePlan save(CarePlan plan) {
        if (plan.getId() == 0) {
            plan.assignId(nextId++);
        }
        plans.put(plan.getId(), plan);
        return plan;
    }

    @Override
    public Optional<CarePlan> findById(long id) {
        return Optional.ofNullable(plans.get(id));
    }

    @Override
    public List<CarePlan> findByPetId(long petId) {
        List<CarePlan> result = new ArrayList<>();
        for (CarePlan plan : plans.values()) {
            if (plan.getPetId() == petId) {
                result.add(plan);
            }
        }
        return result;
    }

    @Override
    public void deleteByPetId(long petId) {
        plans.values().removeIf(plan -> plan.getPetId() == petId);
    }
}
