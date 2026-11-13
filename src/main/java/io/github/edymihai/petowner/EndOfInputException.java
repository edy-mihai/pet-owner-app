package io.github.edymihai.petowner;

/**
 * Thrown by the console input when there is nothing left to read.
 * Deliberately not a NoSuchElementException, which PetCare uses for "not found".
 */
public class EndOfInputException extends RuntimeException {

    public EndOfInputException() {
        super("End of input");
    }
}
