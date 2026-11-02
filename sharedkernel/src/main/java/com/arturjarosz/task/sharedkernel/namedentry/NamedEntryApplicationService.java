package com.arturjarosz.task.sharedkernel.namedentry;

import java.util.List;

/**
 * Create, rename, delete and list entries of one list of named entries; each dto carries the number of items using
 * the entry.
 */
public interface NamedEntryApplicationService<D> {

    /**
     * Create new entry with name from dto. If the name is missing, too long or already used by another entry, an
     * exception is thrown.
     */
    D create(D dto);

    /**
     * Rename entry of given entryId. If the entry does not exist or the name is not correct, an exception is thrown.
     */
    D update(Long entryId, D dto);

    /**
     * Remove entry with given entryId. If the entry does not exist or any item uses it, an exception is thrown.
     */
    void delete(Long entryId);

    /**
     * Get entry of given entryId. If the entry does not exist, an exception is thrown.
     */
    D get(Long entryId);

    /**
     * Get all entries sorted by name.
     */
    List<D> getAll();
}
