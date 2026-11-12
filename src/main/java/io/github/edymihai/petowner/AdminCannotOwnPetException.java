package io.github.edymihai.petowner;

public class AdminCannotOwnPetException extends RuntimeException {

    public AdminCannotOwnPetException(long accountId) {
        super("Account " + accountId + " is an ADMIN and cannot own a pet");
    }
}
