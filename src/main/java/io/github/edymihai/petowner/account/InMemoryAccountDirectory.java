package io.github.edymihai.petowner.account;

import io.github.edymihai.petowner.DuplicateEmailException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryAccountDirectory implements AccountDirectory {

    private final Map<Long, Account> accounts = new HashMap<>();
    private long nextId = 1;

    @Override
    public Account save(Account account) {
        for (Account other : accounts.values()) {
            if (other.getId() != account.getId()
                    && other.getEmail().equalsIgnoreCase(account.getEmail())) {
                throw new DuplicateEmailException(account.getEmail());
            }
        }
        if (account.getId() == 0) {
            account.assignId(nextId++);
        }
        accounts.put(account.getId(), account);
        return account;
    }

    @Override
    public Optional<Account> findById(long id) {
        return Optional.ofNullable(accounts.get(id));
    }

    @Override
    public List<Account> list() {
        return new ArrayList<>(accounts.values());
    }

    @Override
    public void delete(long id) {
        accounts.remove(id);
    }
}
