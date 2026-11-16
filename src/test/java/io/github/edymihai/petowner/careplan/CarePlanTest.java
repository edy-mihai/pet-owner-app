package io.github.edymihai.petowner.careplan;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CarePlanTest {

    private static final LocalDate START = LocalDate.of(2026, 3, 1);

    private static CarePlan recurring(Integer interval, LocalDate end, LocalDate last) {
        return new CarePlan(1, "Pills", PlanKind.MEDICATION, START, interval, end, last);
    }

    private static CarePlan oneOff(LocalDate last) {
        return new CarePlan(1, "Rabies shot", PlanKind.VACCINE, START, null, null, last);
    }

    // ---- R6 validation

    @Test
    void constructorKeepsAllValuesAndStartsUnsaved() {
        LocalDate end = START.plusDays(30);
        LocalDate last = START.plusDays(7);
        CarePlan plan = new CarePlan(5, "Pills", PlanKind.MEDICATION, START, 7, end, last);

        assertEquals(0, plan.getId());
        assertEquals(5, plan.getPetId());
        assertEquals("Pills", plan.getTitle());
        assertEquals(PlanKind.MEDICATION, plan.getKind());
        assertEquals(START, plan.getStartDate());
        assertEquals(7, plan.getIntervalDays());
        assertEquals(end, plan.getEndDate());
        assertEquals(last, plan.getLastGivenDate());
    }

    @Test
    void petIdMustBePositive() {
        assertThrows(IllegalArgumentException.class,
                () -> new CarePlan(0, "Pills", PlanKind.OTHER, START, 7, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new CarePlan(-1, "Pills", PlanKind.OTHER, START, 7, null, null));
    }

    @Test
    void titleMustNotBeBlankOrNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new CarePlan(1, "  ", PlanKind.OTHER, START, 7, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new CarePlan(1, null, PlanKind.OTHER, START, 7, null, null));
    }

    @Test
    void kindAndStartDateMustNotBeNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new CarePlan(1, "Pills", null, START, 7, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new CarePlan(1, "Pills", PlanKind.OTHER, null, 7, null, null));
    }

    @Test
    void intervalMustBePositiveWhenPresent() {
        assertThrows(IllegalArgumentException.class, () -> recurring(0, null, null));
        assertThrows(IllegalArgumentException.class, () -> recurring(-3, null, null));
        assertDoesNotThrow(() -> recurring(1, null, null));
    }

    @Test
    void oneOffCannotHaveAnEndDate() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new CarePlan(1, "Shot", PlanKind.VACCINE, START, null, START.plusDays(5), null));
        assertTrue(e.getMessage().contains("one-off"));
    }

    @Test
    void endDateCannotBeBeforeStartButSameDayIsAllowed() {
        assertThrows(IllegalArgumentException.class, () -> recurring(7, START.minusDays(1), null));
        assertDoesNotThrow(() -> recurring(7, START, null));
    }

    @Test
    void lastGivenMayBeEarlierThanStart() {
        assertDoesNotThrow(() -> recurring(7, null, START.minusDays(10)));
    }

    // ---- nextDue

    @Test
    void nextDueIsTheStartDateWhenNeverGiven() {
        assertEquals(Optional.of(START), recurring(7, null, null).nextDue());
        assertEquals(Optional.of(START), oneOff(null).nextDue());
    }

    @Test
    void nextDueIsEmptyForAOneOffThatWasGiven() {
        assertTrue(oneOff(START).nextDue().isEmpty());
    }

    @Test
    void nextDueIsLastGivenPlusInterval() {
        LocalDate last = START.plusDays(10);
        assertEquals(Optional.of(last.plusDays(7)), recurring(7, null, last).nextDue());
    }

    @Test
    void nextDueIsTheStartDateWhenGivenBeforeStart() {
        assertEquals(Optional.of(START), recurring(7, null, START.minusDays(20)).nextDue());
    }

    @Test
    void nextDueIsTheStartDateWhenTheIntervalEndsBeforeOrOnTheStart() {
        // last + interval = START - 1: the start date wins
        assertEquals(Optional.of(START), recurring(7, null, START.minusDays(8)).nextDue());
        // last + interval = START exactly
        assertEquals(Optional.of(START), recurring(7, null, START.minusDays(7)).nextDue());
    }

    // ---- statusOn

    @Test
    void oneOffThatWasGivenIsDone() {
        assertEquals(DueStatus.DONE, oneOff(START).statusOn(START.plusDays(100)));
    }

    @Test
    void doneBeatsNotStarted() {
        assertEquals(DueStatus.DONE, oneOff(START.minusDays(5)).statusOn(START.minusDays(1)));
    }

    @Test
    void beforeTheStartDateIsNotStarted() {
        assertEquals(DueStatus.NOT_STARTED, recurring(7, null, null).statusOn(START.minusDays(1)));
        assertEquals(DueStatus.NOT_STARTED, oneOff(null).statusOn(START.minusDays(1)));
    }

    @Test
    void notStartedBeatsGivenToday() {
        LocalDate today = START.minusDays(3);
        assertEquals(DueStatus.NOT_STARTED, recurring(7, null, today).statusOn(today));
    }

    @Test
    void afterTheEndDateIsEnded() {
        LocalDate end = START.plusDays(30);
        assertEquals(DueStatus.ENDED, recurring(7, end, null).statusOn(end.plusDays(1)));
        assertEquals(DueStatus.OVERDUE, recurring(7, end, null).statusOn(end));
    }

    @Test
    void endedBeatsGivenToday() {
        LocalDate end = START.plusDays(30);
        LocalDate today = end.plusDays(2);
        assertEquals(DueStatus.ENDED, recurring(7, end, today).statusOn(today));
    }

    @Test
    void lastGivenTodayIsGivenToday() {
        LocalDate today = START.plusDays(10);
        assertEquals(DueStatus.GIVEN_TODAY, recurring(7, null, today).statusOn(today));
    }

    @Test
    void nextDueInTheFutureIsNotDue() {
        LocalDate last = START.plusDays(10);
        assertEquals(DueStatus.NOT_DUE, recurring(7, null, last).statusOn(last.plusDays(6)));
    }

    @Test
    void nextDueTodayIsDueToday() {
        LocalDate last = START.plusDays(10);
        assertEquals(DueStatus.DUE_TODAY, recurring(7, null, last).statusOn(last.plusDays(7)));
        assertEquals(DueStatus.DUE_TODAY, recurring(7, null, null).statusOn(START));
    }

    @Test
    void nextDueInThePastIsOverdue() {
        LocalDate last = START.plusDays(10);
        assertEquals(DueStatus.OVERDUE, recurring(7, null, last).statusOn(last.plusDays(8)));
        assertEquals(DueStatus.OVERDUE, oneOff(null).statusOn(START.plusDays(1)));
    }
}
