package io.github.edymihai.petowner.pet;

import java.util.List;
import java.util.Optional;

public interface PetRegister {

    /** Stores a new pet (assigning its id) or replaces an existing one. Returns the saved pet. */
    Pet save(Pet pet);

    Optional<Pet> findById(long id);

    List<Pet> list();

    List<Pet> findByOwner(long accountId);

    void delete(long id);
}
