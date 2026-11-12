package io.github.edymihai.petowner;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("An account with this email already exists: " + email);
    }
}
