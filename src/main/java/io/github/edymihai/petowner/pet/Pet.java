package io.github.edymihai.petowner.pet;

import io.github.edymihai.petowner.CannotRemoveLastOwnerException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Pet {

    private long id;
    private final String name;
    private final Species species;
    private final LocalDate birthDate;
    private final String notes;
    /** Owner account id to the date they became an owner. */
    private final Map<Long, LocalDate> owners = new HashMap<>();

    /**
     * Creates a new pet with its first owner, so a pet never exists without one.
     * The id stays 0 until the store assigns one. birthDate and notes may be null.
     */
    public Pet(String name, Species species, LocalDate birthDate, String notes,
               long firstOwnerId, LocalDate since) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Pet name must not be blank");
        }
        if (species == null) {
            throw new IllegalArgumentException("Pet species must not be null");
        }
        if (since == null) {
            throw new IllegalArgumentException("Owner since date must not be null");
        }
        if (birthDate != null && birthDate.isAfter(since)) {
            throw new IllegalArgumentException("Birth date " + birthDate + " is after the owner since date "
                    + since + ": a pet cannot be owned before it is born");
        }
        this.name = name;
        this.species = species;
        this.birthDate = birthDate;
        this.notes = notes;
        this.owners.put(firstOwnerId, since);
    }

    /** Adds an owner. Returns false (and changes nothing) if the account already owns this pet. */
    public boolean addOwner(long accountId, LocalDate since) {
        if (since == null) {
            throw new IllegalArgumentException("Owner since date must not be null");
        }
        return owners.putIfAbsent(accountId, since) == null;
    }

    /**
     * Removes an owner. Returns false if the account is not an owner.
     * Throws if the account is the last owner: deleting the pet is PetCare's job.
     */
    public boolean removeOwner(long accountId) {
        if (!owners.containsKey(accountId)) {
            return false;
        }
        if (owners.size() == 1) {
            throw new CannotRemoveLastOwnerException(accountId);
        }
        owners.remove(accountId);
        return true;
    }

    public boolean isOwnedBy(long accountId) {
        return owners.containsKey(accountId);
    }

    public Map<Long, LocalDate> getOwners() {
        return Collections.unmodifiableMap(owners);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Species getSpecies() {
        return species;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getNotes() {
        return notes;
    }

    /** Called only by the store that saves this pet. */
    void assignId(long id) {
        this.id = id;
    }
}
