package io.github.edymihai.petowner.pet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryPetRegisterTest {

    private static final LocalDate DAY = LocalDate.of(2026, 1, 1);

    private InMemoryPetRegister register;

    @BeforeEach
    void setUp() {
        register = new InMemoryPetRegister();
    }

    private Pet pet(String name, long ownerId) {
        return new Pet(name, Species.CAT, null, null, ownerId, DAY);
    }

    @Test
    void saveAssignsIncreasingIds() {
        Pet first = register.save(pet("Tom", 1L));
        Pet second = register.save(pet("Kit", 1L));

        assertEquals(1, first.getId());
        assertEquals(2, second.getId());
    }

    @Test
    void findByIdReturnsSavedPetOrEmpty() {
        Pet saved = register.save(pet("Tom", 1L));

        assertSame(saved, register.findById(saved.getId()).orElseThrow());
        assertTrue(register.findById(99).isEmpty());
    }

    @Test
    void listReturnsAllPets() {
        register.save(pet("Tom", 1L));
        register.save(pet("Kit", 2L));

        assertEquals(2, register.list().size());
    }

    @Test
    void findByOwnerReturnsOnlyThatOwnersPets() {
        Pet tom = register.save(pet("Tom", 1L));
        Pet kit = register.save(pet("Kit", 2L));
        kit.addOwner(1L, DAY);
        register.save(kit);
        register.save(pet("Zed", 3L));

        assertEquals(2, register.findByOwner(1L).size());
        assertTrue(register.findByOwner(1L).contains(tom));
        assertTrue(register.findByOwner(1L).contains(kit));
        assertTrue(register.findByOwner(99L).isEmpty());
    }

    @Test
    void deleteRemovesThePet() {
        Pet saved = register.save(pet("Tom", 1L));

        register.delete(saved.getId());

        assertTrue(register.findById(saved.getId()).isEmpty());
    }

    @Test
    void savingAnExistingPetKeepsItsId() {
        Pet saved = register.save(pet("Tom", 1L));

        register.save(saved);

        assertEquals(1, saved.getId());
        assertEquals(1, register.list().size());
    }
}
