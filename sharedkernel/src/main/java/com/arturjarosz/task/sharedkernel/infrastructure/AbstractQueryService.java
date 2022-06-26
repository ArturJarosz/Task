package com.arturjarosz.task.sharedkernel.infrastructure;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class AbstractQueryService<T extends EntityPathBase<? extends AbstractAggregateRoot>> {

    @PersistenceContext
    private EntityManager entityManager;

    private final T aggregatePath;

    public AbstractQueryService(T aggregatePath) {
        this.aggregatePath = aggregatePath;
    }

    protected JPQLQuery<?> queryFromAggregate() {
        return new JPAQueryFactory(this.entityManager).from(this.aggregatePath);
    }

    protected JPAQueryFactory query() {
        return new JPAQueryFactory(this.entityManager);
    }
}
