package io.github.edymihai.petowner.pet;

import io.github.edymihai.petowner.CannotRemoveLastOwnerException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PetTest {

    private static final LocalDate DAY1 = LocalDate.of(2026, 1, 1);
    private static final LocalDate DAY2 = LocalDate.of(2026, 2, 2);

    private Pet newPet() {
        return new Pet("Rex", Species.DOG, null, null, 1L, DAY1);
    }

    @Test
    void constructorKeepsValuesAndMakesTheFirstOwner() {
        Pet pet = new Pet("Rex", Species.DOG, LocalDate.of(2020, 5, 5), "friendly", 1L, DAY1);

        assertEquals("Rex", pet.getName());
        assertEquals(Species.DOG, pet.getSpecies());
        assertEquals(LocalDate.of(2020, 5, 5), pet.getBirthDate());
        assertEquals("friendly", pet.getNotes());
        assertEquals(0, pet.getId());
        assertEquals(1, pet.getOwners().size());
        assertEquals(DAY1, pet.getOwners().get(1L));
    }

    @Test
    void birthDateAndNotesAreOptional() {
        Pet pet = newPet();

        assertNull(pet.getBirthDate());
        assertNull(pet.getNotes());
    }

    @Test
    void birthDateAfterTheOwnerSinceDateIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Pet("Rex", Species.DOG, DAY1.plusDays(1), null, 1L, DAY1));
    }

    @Test
    void birthDateOnTheSinceDayIsAllowed() {
        Pet pet = new Pet("Rex", Species.DOG, DAY1, null, 1L, DAY1);

        assertEquals(DAY1, pet.getBirthDate());
    }

    @Test
    void nullBirthDateIsAllowed() {
        assertNull(new Pet("Rex", Species.DOG, null, null, 1L, DAY1).getBirthDate());
    }

    @Test
    void invalidConstructorArgumentsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Pet(" ", Species.DOG, null, null, 1L, DAY1));
        assertThrows(IllegalArgumentException.class, () -> new Pet(null, Species.DOG, null, null, 1L, DAY1));
        assertThrows(IllegalArgumentException.class, () -> new Pet("Rex", null, null, null, 1L, DAY1));
        assertThrows(IllegalArgumentException.class, () -> new Pet("Rex", Species.DOG, null, null, 1L, null));
    }

    @Test
    void addOwnerAddsASecondOwner() {
        Pet pet = newPet();

        assertTrue(pet.addOwner(2L, DAY2));

        assertEquals(2, pet.getOwners().size());
        assertEquals(DAY2, pet.getOwners().get(2L));
    }

    @Test
    void addingTheSameOwnerTwiceKeepsOneEntryAndTheOriginalDate() {
        Pet pet = newPet();

        assertFalse(pet.addOwner(1L, DAY2));

        assertEquals(1, pet.getOwners().size());
        assertEquals(DAY1, pet.getOwners().get(1L));
    }

    @Test
    void removeOwnerRemovesOneOfSeveralOwners() {
        Pet pet = newPet();
        pet.addOwner(2L, DAY2);

        assertTrue(pet.removeOwner(1L));

        assertFalse(pet.isOwnedBy(1L));
        assertTrue(pet.isOwnedBy(2L));
    }

    @Test
    void removingTheLastOwnerIsNotAllowed() {
        Pet pet = newPet();

        assertThrows(CannotRemoveLastOwnerException.class, () -> pet.removeOwner(1L));
        assertTrue(pet.isOwnedBy(1L));
    }

    @Test
    void removingANonOwnerFromASingleOwnerPetReturnsFalseAndDoesNotThrow() {
        Pet pet = newPet();

        assertFalse(pet.removeOwner(99L));

        assertEquals(1, pet.getOwners().size());
        assertTrue(pet.isOwnedBy(1L));
    }

    @Test
    void ownersViewCannotBeModifiedFromOutside() {
        Pet pet = newPet();

        assertThrows(UnsupportedOperationException.class, () -> pet.getOwners().put(2L, DAY2));
    }
}
