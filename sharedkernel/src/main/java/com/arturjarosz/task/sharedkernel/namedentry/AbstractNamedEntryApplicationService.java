package com.arturjarosz.task.sharedkernel.namedentry;

import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application service of one list of named entries. entryLabel (e.g. "Stage type") is used in logs.
 */
@Slf4j
public abstract class AbstractNamedEntryApplicationService<T extends AbstractNamedEntry, D>
        implements NamedEntryApplicationService<D> {

    private final NamedEntryRepository<T> repository;
    private final NamedEntryValidator<T> validator;
    private final String entryLabel;

    protected AbstractNamedEntryApplicationService(NamedEntryRepository<T> repository,
            NamedEntryValidator<T> validator, String entryLabel) {
        this.repository = repository;
        this.validator = validator;
        this.entryLabel = entryLabel;
    }

    protected abstract T createEntry(String name);

    protected abstract String nameOf(D dto);

    protected abstract D toDto(T entry, Long numberOfUsages);

    @Transactional
    @Override
    public D create(D dto) {
        LOG.info("Creating {}.", this.entryLabel);

        this.validator.validateCreateDto(dto, this::nameOf);
        var entry = this.saveWithUniqueName(this.createEntry(this.nameOf(dto).strip()));

        LOG.info("{} with id {} created.", this.entryLabel, entry.getId());
        return this.toDto(entry, 0L);
    }

    @Transactional
    @Override
    public D update(Long entryId, D dto) {
        LOG.info("Updating {} with id {}.", this.entryLabel, entryId);

        var maybeEntry = this.repository.findById(entryId);
        this.validator.validateExistence(maybeEntry, entryId);
        this.validator.validateUpdateDto(dto, this::nameOf, entryId);
        var entry = maybeEntry.orElseThrow(ResourceNotFoundException::new);
        entry.rename(this.nameOf(dto).strip());
        entry = this.saveWithUniqueName(entry);

        LOG.info("{} with id {} updated.", this.entryLabel, entryId);
        return this.toDto(entry, this.getNumberOfUsages(entryId));
    }

    @Transactional
    @Override
    public void delete(Long entryId) {
        LOG.info("Deleting {} with id {}.", this.entryLabel, entryId);

        this.validator.validateExistence(entryId);
        this.validator.validateHasNoUsages(entryId);
        try {
            this.repository.deleteById(entryId);
            // flushed here, so an item given this entry after the check above fails the delete with the same message
            this.repository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException(this.validator.getUsedEntryMessageCode(), exception);
        }

        LOG.info("{} with id {} deleted.", this.entryLabel, entryId);
    }

    @Override
    public D get(Long entryId) {
        LOG.info("Loading {} with id {}.", this.entryLabel, entryId);

        var maybeEntry = this.repository.findById(entryId);
        this.validator.validateExistence(maybeEntry, entryId);
        var entry = maybeEntry.orElseThrow(ResourceNotFoundException::new);
        return this.toDto(entry, this.getNumberOfUsages(entryId));
    }

    @Override
    public List<D> getAll() {
        LOG.info("Loading {} list.", this.entryLabel);

        var entries = this.repository.findAll(Sort.by("name"));
        var usagesByEntryId = this.validator.countUsages(entries.stream().map(AbstractNamedEntry::getId).toList());
        return entries.stream().map(entry -> this.toDto(entry, usagesByEntryId.get(entry.getId()))).toList();
    }

    /**
     * Saves and flushes, so an entry given the same name in the meantime (after the validator checked it) fails with
     * the same message as the validator.
     */
    private T saveWithUniqueName(T entry) {
        try {
            return this.repository.saveAndFlush(entry);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException(this.validator.getNameUniqueMessageCode(), exception, entry.getName());
        }
    }

    private Long getNumberOfUsages(Long entryId) {
        return this.validator.countUsages(List.of(entryId)).get(entryId);
    }
}
