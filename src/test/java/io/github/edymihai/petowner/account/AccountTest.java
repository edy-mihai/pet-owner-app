package io.github.edymihai.petowner.account;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountTest {

    @Test
    void validAccountKeepsItsValuesAndHasNoIdYet() {
        Account account = new Account("Ana", "ana@example.com", Role.OWNER);

        assertEquals("Ana", account.getName());
        assertEquals("ana@example.com", account.getEmail());
        assertEquals(Role.OWNER, account.getRole());
        assertEquals(0, account.getId());
    }

    @Test
    void blankOrNullNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Account(" ", "a@b.c", Role.OWNER));
        assertThrows(IllegalArgumentException.class, () -> new Account(null, "a@b.c", Role.OWNER));
    }

    @Test
    void blankOrNullEmailIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Account("Ana", "", Role.OWNER));
        assertThrows(IllegalArgumentException.class, () -> new Account("Ana", null, Role.OWNER));
    }

    @Test
    void emailWithoutAtIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Account("Ana", "ana.example.com", Role.OWNER));
    }

    @Test
    void nullRoleIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Account("Ana", "a@b.c", null));
    }
}
