package io.github.edymihai.petowner;

import java.time.LocalDate;

public class AlreadyGivenException extends RuntimeException {

    private AlreadyGivenException(String message) {
        super(message);
    }

    /** A one-off plan can be given only once. */
    public static AlreadyGivenException oneOff(String title, LocalDate givenOn) {
        return new AlreadyGivenException("\"" + title + "\" is a one-off and was already given on " + givenOn);
    }

    /** A new date must be after the last given date. */
    public static AlreadyGivenException notAfterLast(String title, LocalDate lastGiven, LocalDate attempted) {
        return new AlreadyGivenException("\"" + title + "\" was last given on " + lastGiven
                + "; " + attempted + " is not after it");
    }
}
