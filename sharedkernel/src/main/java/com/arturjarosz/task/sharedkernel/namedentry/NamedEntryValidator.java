package com.arturjarosz.task.sharedkernel.namedentry;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertEntityPresent;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsFalse;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

/**
 * Validates dtos and entities of one list of named entries. Message codes start with entryCode (e.g. "stageType"),
 * and usagesCode (e.g. "stages") names the items that keep an entry from being removed.
 */
public abstract class NamedEntryValidator<T extends AbstractNamedEntry> {
    public static final String NAME = "name";
    public static final String TOO_LONG = "tooLong";
    public static final String UNIQUE = "unique";

    private final NamedEntryRepository<T> repository;
    private final String entryCode;
    private final Function<Collection<Long>, Map<Long, Long>> usageCounter;
    private final String usedEntryMessageCode;
    private final String nameUniqueMessageCode;

    /**
     * usageCounter returns the number of items using each of the given entry ids, 0 for unused entries.
     */
    protected NamedEntryValidator(NamedEntryRepository<T> repository, String entryCode, String usagesCode,
            Function<Collection<Long>, Map<Long, Long>> usageCounter) {
        this.repository = repository;
        this.entryCode = entryCode;
        this.usageCounter = usageCounter;
        this.usedEntryMessageCode = createMessageCode(ExceptionCodes.NOT_VALID, entryCode, usagesCode);
        this.nameUniqueMessageCode = createMessageCode(ExceptionCodes.NOT_VALID, entryCode, NAME, UNIQUE);
    }

    public String getUsedEntryMessageCode() {
        return this.usedEntryMessageCode;
    }

    public String getNameUniqueMessageCode() {
        return this.nameUniqueMessageCode;
    }

    public Map<Long, Long> countUsages(Collection<Long> entryIds) {
        return this.usageCounter.apply(entryIds);
    }

    public <D> void validateCreateDto(D dto, Function<D, String> nameOf) {
        this.validateDto(dto, nameOf, null);
    }

    public <D> void validateUpdateDto(D dto, Function<D, String> nameOf, Long entryId) {
        this.validateDto(dto, nameOf, entryId);
    }

    /**
     * Validates the name of a new entry (entryId null) or of the renamed one with entryId. Names are compared without
     * surrounding whitespace and regardless of letter case.
     */
    private <D> void validateDto(D dto, Function<D, String> nameOf, Long entryId) {
        assertNotNull(dto, createMessageCode(ExceptionCodes.NULL, this.entryCode));
        assertNotNull(nameOf.apply(dto), createMessageCode(ExceptionCodes.NULL, this.entryCode, NAME));
        var name = nameOf.apply(dto).strip();
        assertNotEmpty(name, createMessageCode(ExceptionCodes.EMPTY, this.entryCode, NAME));
        assertIsTrue(name.length() <= AbstractNamedEntry.NAME_MAX_LENGTH,
                createMessageCode(ExceptionCodes.NOT_VALID, this.entryCode, NAME, TOO_LONG),
                AbstractNamedEntry.NAME_MAX_LENGTH);
        var nameTaken = entryId == null
                ? this.repository.existsByNameIgnoreCase(name)
                : this.repository.existsByNameIgnoreCaseAndIdNot(name, entryId);
        assertIsFalse(nameTaken, this.nameUniqueMessageCode, name);
    }

    public void validateExistence(Optional<T> maybeEntry, Long entryId) {
        assertEntityPresent(maybeEntry.isPresent(), createMessageCode(ExceptionCodes.NOT_EXIST, this.entryCode),
                entryId);
    }

    /**
     * Validates that the entry exists and locks it until the transaction ends, so an item saved with it in the same
     * transaction cannot lose it to a delete made at the same time.
     */
    public void validateExistence(Long entryId) {
        this.validateExistence(this.repository.findLockedById(entryId), entryId);
    }

    public void validateHasNoUsages(Long entryId) {
        assertIsTrue(this.countUsages(List.of(entryId)).get(entryId) == 0, this.usedEntryMessageCode);
    }
}
