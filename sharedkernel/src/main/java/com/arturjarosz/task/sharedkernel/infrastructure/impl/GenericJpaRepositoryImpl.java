package com.arturjarosz.task.sharedkernel.infrastructure.impl;

import com.arturjarosz.task.sharedkernel.infrastructure.AbstractBaseRepository;
import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.arturjarosz.task.sharedkernel.model.QAbstractAggregateRoot;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.List;

/**
 * An implementation of generic Jpa repository.
 *
 * @param <T>
 * @param <S>
 */

@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
@Repository
public abstract class GenericJpaRepositoryImpl<T extends AbstractAggregateRoot, S extends EntityPathBase<T>>
        implements AbstractBaseRepository<T> {

    private AutowireCapableBeanFactory spring;
    @PersistenceContext
    private EntityManager entityManager;
    private S qAggregateRoot;
    private QAbstractAggregateRoot qAbstractAggregateRoot;

    public GenericJpaRepositoryImpl(S qAggregateRoot) {
        this.qAggregateRoot = qAggregateRoot;
        this.qAbstractAggregateRoot = new QAbstractAggregateRoot(qAggregateRoot.getMetadata());
    }

    @Autowired
    public void setSpring(AutowireCapableBeanFactory spring) {
        this.spring = spring;
    }

    /**
     * Returns query for aggregate root T, that can be extended.
     *
     * @return
     */
    @Override
    public JPAQuery<T> queryFromAggregateRoot() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(this.entityManager);
        return queryFactory.selectFrom(this.qAggregateRoot);
    }

    /**
     * Loads aggregate root T of given id.
     *
     * @param id
     * @return aggregate root T.
     */
    @Override
    public T load(Long id) {
        T aggregate = this.queryFromAggregateRoot().where(this.qAbstractAggregateRoot.id.eq(id)).fetchOne();
        return aggregate;
    }

    /**
     * Loads all objects that are aggregate root of type T.
     *
     * @return List of aggregate roots T.
     */
    @Override
    public List<T> loadAll() {
        List<T> aggregates = this.queryFromAggregateRoot().fetch();
        this.autowire(aggregates);
        return aggregates;
    }

    /**
     * Saves aggregate root T.
     *
     * @param aggregate
     */
    @Override
    public T save(T aggregate) {
        if (!this.entityManager.contains(aggregate)) {
            this.entityManager.persist(aggregate);
        } else {
            this.entityManager.merge(aggregate);
        }
        this.entityManager.flush();
        return aggregate;
    }

    /**
     * Saves all given aggregate root T.
     *
     * @param aggregates
     */
    @Override
    public Collection<T> saveAll(Collection<T> aggregates) {
        for (T aggregate : aggregates) {
            this.save(aggregate);
        }
        this.entityManager.flush();
        return aggregates;
    }

    /**
     * Removes aggregate root T if it exists.
     *
     * @param id
     */
    @Override
    public void remove(Long id) {
        T aggregate = this.load(id);

        this.entityManager.remove(aggregate);
        this.entityManager.flush();
    }

    /**
     * Autowire Entity.
     */
    private void autowire(T aggregate) {
        this.spring.autowireBean(aggregate);
    }

    /**
     * Autowire all elements of aggregates collection.
     */
    private void autowire(Collection<T> aggregates) {
        for (T aggregate : aggregates) {
            this.autowire(aggregate);
        }
    }
}
