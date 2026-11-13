package io.github.edymihai.petowner.account;

public class Account {

    private long id;
    private final String name;
    private final String email;
    private final Role role;

    /** Creates a new account. The id stays 0 until the store assigns one. */
    public Account(String name, String email, Role role) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Account name must not be blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Account email must not be blank");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Account email must contain @: " + email);
        }
        if (role == null) {
            throw new IllegalArgumentException("Account role must not be null");
        }
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    /** Called only by the store that saves this account. */
    void assignId(long id) {
        this.id = id;
    }
}
