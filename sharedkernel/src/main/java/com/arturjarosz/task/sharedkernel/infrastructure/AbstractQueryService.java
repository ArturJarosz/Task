package com.arturjarosz.task.sharedkernel.infrastructure;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class AbstractQueryService<T extends EntityPathBase<? extends AbstractAggregateRoot>> {

    private final T aggregatePath;
    @PersistenceContext
    private EntityManager entityManager;

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
