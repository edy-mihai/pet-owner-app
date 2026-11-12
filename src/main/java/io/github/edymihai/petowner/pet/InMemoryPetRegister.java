package io.github.edymihai.petowner.pet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryPetRegister implements PetRegister {

    private final Map<Long, Pet> pets = new HashMap<>();
    private long nextId = 1;

    @Override
    public Pet save(Pet pet) {
        if (pet.getId() == 0) {
            pet.assignId(nextId++);
        }
        pets.put(pet.getId(), pet);
        return pet;
    }

    @Override
    public Optional<Pet> findById(long id) {
        return Optional.ofNullable(pets.get(id));
    }

    @Override
    public List<Pet> list() {
        return new ArrayList<>(pets.values());
    }

    @Override
    public List<Pet> findByOwner(long accountId) {
        List<Pet> result = new ArrayList<>();
        for (Pet pet : pets.values()) {
            if (pet.isOwnedBy(accountId)) {
                result.add(pet);
            }
        }
        return result;
    }

    @Override
    public void delete(long id) {
        pets.remove(id);
    }
}
