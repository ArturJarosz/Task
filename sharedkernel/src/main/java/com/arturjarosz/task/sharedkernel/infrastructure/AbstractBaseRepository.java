package com.arturjarosz.task.sharedkernel.infrastructure;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.querydsl.jpa.impl.JPAQuery;

import java.util.Collection;
import java.util.List;

/**
 * Repostiroy for AggregateRoot objects. Provides base base methods for all aggregate roots.
 *
 * @param <T> the aggregate root
 */

public interface AbstractBaseRepository<T extends AbstractAggregateRoot> {

    T load(Long id);

    List<T> loadAll();

    void save(T aggregate);

    void saveAll(Collection<T> aggregates);

    void remove(Long id);

    JPAQuery<T> queryFromAggregateRoot();

}
