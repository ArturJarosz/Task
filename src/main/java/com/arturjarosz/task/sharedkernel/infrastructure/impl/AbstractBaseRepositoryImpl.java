package com.arturjarosz.task.sharedkernel.infrastructure.impl;

import com.arturjarosz.task.sharedkernel.infrastructure.AbstractBaseRepository;
import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class AbstractBaseRepositoryImpl<T extends AbstractAggregateRoot> implements AbstractBaseRepository<T> {
    @PersistenceContext
    private EntityManager em;

    @Override
    public void save(T aggregate) {
        if (!this.em.contains(aggregate)) {
            this.em.persist(aggregate);
        } else {
            this.em.merge(aggregate);
        }
    }
}
