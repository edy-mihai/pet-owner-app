package io.github.edymihai.petowner;

public class CannotRemoveLastOwnerException extends RuntimeException {

    public CannotRemoveLastOwnerException(long accountId) {
        super("Account " + accountId + " is the last owner of the pet and cannot be removed");
    }
}
