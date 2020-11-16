package com.arturjarosz.task.sharedkernel.infrastructure;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;

/**
 * Repostiroy for AggregateRoot objects. Provides base base methods for all aggregate roots.
 *
 * @param <T> the aggregate root
 */

public interface AbstractBaseRepository<T extends AbstractAggregateRoot> {

    //TODO: remove comments when able to implement those generic methods

    //T load(Long id);

    //List<T> loadAll();

    void save(T aggregate);

    //void saveAll(Collection<T> aggregates);

    //void remove(Long id);
}
