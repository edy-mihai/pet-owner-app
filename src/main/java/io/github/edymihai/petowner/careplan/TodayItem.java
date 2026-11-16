package io.github.edymihai.petowner.careplan;

import java.time.LocalDate;
import java.util.Optional;

/**
 * One line of the today list. {@code nextDue} is an Optional because CarePlan.nextDue() is
 * (empty for a finished one-off); {@code lastGiven} is null when the plan was never given,
 * like CarePlan.getLastGivenDate().
 */
public record TodayItem(String petName, long planId, String planTitle, PlanKind kind,
                        DueStatus status, Optional<LocalDate> nextDue, LocalDate lastGiven) {
}
