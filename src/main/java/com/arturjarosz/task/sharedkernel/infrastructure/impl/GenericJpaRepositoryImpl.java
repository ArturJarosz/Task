package com.arturjarosz.task.sharedkernel.infrastructure.impl;

import com.arturjarosz.task.sharedkernel.infrastructure.AbstractBaseRepository;
import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.arturjarosz.task.sharedkernel.model.QAbstractAggregateRoot;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.List;

@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
@Repository
public abstract class GenericJpaRepositoryImpl<T extends AbstractAggregateRoot, S extends EntityPathBase<T>>
        implements AbstractBaseRepository<T> {

    @PersistenceContext
    private EntityManager entityManager;
    private Class<T> tClass;
    private S qAggregateRoot;
    private QAbstractAggregateRoot qAbstractAggregateRoot;

    public GenericJpaRepositoryImpl(S qAggregateRoot) {
        this.tClass = (Class<T>) qAggregateRoot.getType();
        this.qAggregateRoot = qAggregateRoot;
        this.qAbstractAggregateRoot = new QAbstractAggregateRoot(qAggregateRoot.getMetadata());
    }

    @Override
    public JPAQuery<T> queryFromAggregateRoot() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(this.entityManager);
        return queryFactory.selectFrom(this.qAggregateRoot);
    }

    @Override
    public T load(Long id) {
        return this.queryFromAggregateRoot().where(this.qAbstractAggregateRoot.id.eq(id)).fetchOne();
    }

    @Override
    public List<T> loadAll() {
        return this.queryFromAggregateRoot().fetch();
    }

    @Override
    public void save(T aggregate) {
        if (!this.entityManager.contains(aggregate)) {
            this.entityManager.persist(aggregate);
        } else {
            this.entityManager.merge(aggregate);
        }
        this.entityManager.flush();
    }

    @Override
    public void saveAll(Collection<T> aggregates) {
        for (T aggregate : aggregates) {
            this.save(aggregate);
        }
        this.entityManager.flush();
    }

    @Override
    public void remove(Long id) {
        T aggregate = this.load(id);
        this.entityManager.remove(aggregate);
        this.entityManager.flush();
    }
}
