package com.arturjarosz.task.sharedkernel.namedentry;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@NoRepositoryBean
public interface NamedEntryRepository<T extends AbstractNamedEntry> extends JpaRepository<T, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    /**
     * Finds the entry with a shared lock held until the transaction ends, so the entry cannot be deleted while an item
     * referencing it is being saved: the delete waits and then fails on the foreign key.
     */
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Transactional
    Optional<T> findLockedById(Long id);
}
