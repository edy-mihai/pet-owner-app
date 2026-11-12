package io.github.edymihai.petowner.account;

import io.github.edymihai.petowner.DuplicateEmailException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryAccountDirectoryTest {

    private InMemoryAccountDirectory directory;

    @BeforeEach
    void setUp() {
        directory = new InMemoryAccountDirectory();
    }

    @Test
    void saveAssignsIncreasingIds() {
        Account first = directory.save(new Account("Ana", "ana@example.com", Role.OWNER));
        Account second = directory.save(new Account("Bob", "bob@example.com", Role.OWNER));

        assertEquals(1, first.getId());
        assertEquals(2, second.getId());
    }

    @Test
    void findByIdReturnsSavedAccountOrEmpty() {
        Account saved = directory.save(new Account("Ana", "ana@example.com", Role.OWNER));

        assertSame(saved, directory.findById(saved.getId()).orElseThrow());
        assertTrue(directory.findById(99).isEmpty());
    }

    @Test
    void listReturnsAllAccounts() {
        directory.save(new Account("Ana", "ana@example.com", Role.OWNER));
        directory.save(new Account("Bob", "bob@example.com", Role.ADMIN));

        assertEquals(2, directory.list().size());
    }

    @Test
    void deleteRemovesTheAccount() {
        Account saved = directory.save(new Account("Ana", "ana@example.com", Role.OWNER));

        directory.delete(saved.getId());

        assertTrue(directory.findById(saved.getId()).isEmpty());
        assertTrue(directory.list().isEmpty());
    }

    @Test
    void duplicateEmailIsRejectedIgnoringCase() {
        directory.save(new Account("Ana", "ana@example.com", Role.OWNER));

        assertThrows(DuplicateEmailException.class,
                () -> directory.save(new Account("Other", "ANA@example.com", Role.OWNER)));
        assertEquals(1, directory.list().size());
    }

    @Test
    void savingTheSameAccountAgainIsNotADuplicate() {
        Account saved = directory.save(new Account("Ana", "ana@example.com", Role.OWNER));

        directory.save(saved);

        assertEquals(1, directory.list().size());
        assertEquals(1, saved.getId());
    }
}
