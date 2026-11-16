package io.github.edymihai.petowner.careplan;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryCarePlanBookTest {

    private static final LocalDate START = LocalDate.of(2026, 3, 1);

    private final InMemoryCarePlanBook book = new InMemoryCarePlanBook();

    private CarePlan plan(long petId, String title) {
        return new CarePlan(petId, title, PlanKind.OTHER, START, 7, null, null);
    }

    @Test
    void saveAssignsIncreasingIds() {
        assertEquals(1, book.save(plan(1, "A")).getId());
        assertEquals(2, book.save(plan(1, "B")).getId());
    }

    @Test
    void savingAgainKeepsTheIdAndDoesNotDuplicate() {
        CarePlan saved = book.save(plan(1, "A"));
        book.save(saved);

        assertEquals(1, saved.getId());
        assertEquals(1, book.findByPetId(1).size());
    }

    @Test
    void findByIdReturnsThePlanOrEmpty() {
        CarePlan saved = book.save(plan(1, "A"));

        assertSame(saved, book.findById(saved.getId()).orElseThrow());
        assertTrue(book.findById(99).isEmpty());
    }

    @Test
    void findByPetIdReturnsOnlyThatPetsPlans() {
        book.save(plan(1, "A"));
        book.save(plan(2, "B"));
        book.save(plan(1, "C"));

        assertEquals(2, book.findByPetId(1).size());
        assertEquals(1, book.findByPetId(2).size());
        assertTrue(book.findByPetId(3).isEmpty());
    }

    @Test
    void deleteByPetIdRemovesOnlyThatPetsPlans() {
        book.save(plan(1, "A"));
        CarePlan other = book.save(plan(2, "B"));
        book.save(plan(1, "C"));

        book.deleteByPetId(1);

        assertTrue(book.findByPetId(1).isEmpty());
        assertSame(other, book.findById(other.getId()).orElseThrow());
    }
}
