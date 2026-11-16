package io.github.edymihai.petowner.careplan;

import io.github.edymihai.petowner.AlreadyGivenException;

import java.time.LocalDate;
import java.util.Optional;

public class CarePlan {

    private long id;
    private final long petId;
    private final String title;
    private final PlanKind kind;
    private final LocalDate startDate;
    /** Days between doses; null means a one-off plan. */
    private final Integer intervalDays;
    private final LocalDate endDate;
    private LocalDate lastGivenDate;

    /**
     * Creates a care plan. The id stays 0 until the store assigns one.
     * intervalDays, endDate and lastGivenDate may be null; a one-off (no interval) cannot have an end date.
     */
    public CarePlan(long petId, String title, PlanKind kind, LocalDate startDate,
                    Integer intervalDays, LocalDate endDate, LocalDate lastGivenDate) {
        if (petId <= 0) {
            throw new IllegalArgumentException("Care plan pet id must be positive");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Care plan title must not be blank");
        }
        if (kind == null) {
            throw new IllegalArgumentException("Care plan kind must not be null");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Care plan start date must not be null");
        }
        if (intervalDays != null && intervalDays <= 0) {
            throw new IllegalArgumentException("Interval must be at least 1 day, got " + intervalDays);
        }
        if (intervalDays == null && endDate != null) {
            throw new IllegalArgumentException("A one-off care plan cannot have an end date");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date " + endDate + " is before the start date " + startDate);
        }
        this.petId = petId;
        this.title = title;
        this.kind = kind;
        this.startDate = startDate;
        this.intervalDays = intervalDays;
        this.endDate = endDate;
        this.lastGivenDate = lastGivenDate;
    }

    /** The next date the plan is due; empty when a one-off has already been given. */
    public Optional<LocalDate> nextDue() {
        if (lastGivenDate == null) {
            return Optional.of(startDate);
        }
        if (intervalDays == null) {
            return Optional.empty();
        }
        LocalDate afterLast = lastGivenDate.plusDays(intervalDays);
        return Optional.of(afterLast.isAfter(startDate) ? afterLast : startDate);
    }

    public DueStatus statusOn(LocalDate today) {
        if (intervalDays == null && lastGivenDate != null) {
            return DueStatus.DONE;
        }
        if (today.isBefore(startDate)) {
            return DueStatus.NOT_STARTED;
        }
        if (endDate != null && today.isAfter(endDate)) {
            return DueStatus.ENDED;
        }
        if (today.equals(lastGivenDate)) {
            return DueStatus.GIVEN_TODAY;
        }
        LocalDate due = nextDue().orElseThrow();
        if (due.isAfter(today)) {
            return DueStatus.NOT_DUE;
        }
        return due.isBefore(today) ? DueStatus.OVERDUE : DueStatus.DUE_TODAY;
    }

    /**
     * Records that the plan was given on {@code givenOn}, whatever its status. Missed days are not caught up.
     * Throws IllegalArgumentException for a date after today, AlreadyGivenException for a one-off that
     * was already given or a date not after the last given date. The caller saves the plan afterwards.
     */
    public void markGiven(LocalDate givenOn, LocalDate today) {
        if (givenOn == null) {
            throw new IllegalArgumentException("Given date must not be null");
        }
        if (givenOn.isAfter(today)) {
            throw new IllegalArgumentException("Given date " + givenOn + " is after today (" + today + ")");
        }
        if (intervalDays == null && lastGivenDate != null) {
            throw AlreadyGivenException.oneOff(title, lastGivenDate);
        }
        if (lastGivenDate != null && !givenOn.isAfter(lastGivenDate)) {
            throw AlreadyGivenException.notAfterLast(title, lastGivenDate, givenOn);
        }
        lastGivenDate = givenOn;
    }

    public long getId() {
        return id;
    }

    public long getPetId() {
        return petId;
    }

    public String getTitle() {
        return title;
    }

    public PlanKind getKind() {
        return kind;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public Integer getIntervalDays() {
        return intervalDays;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDate getLastGivenDate() {
        return lastGivenDate;
    }

    /** Called only by the store that saves this plan. */
    void assignId(long id) {
        this.id = id;
    }
}
