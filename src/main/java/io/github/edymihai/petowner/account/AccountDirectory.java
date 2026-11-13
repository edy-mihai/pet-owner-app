package io.github.edymihai.petowner.account;

import java.util.List;
import java.util.Optional;

public interface AccountDirectory {

    /** Stores a new account (assigning its id) or replaces an existing one. Returns the saved account. */
    Account save(Account account);

    Optional<Account> findById(long id);

    List<Account> list();

    void delete(long id);
}
