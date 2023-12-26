package com.arturjarosz.task.sharedkernel.testhelpers;

import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Builder for Entities. It can create object from protected constructor.
 *
 * @param <T>
 * @param <B>
 */
@Slf4j
public class AbstractBuilder<T extends AbstractEntity, B extends AbstractBuilder<T, B>> {

    protected B builder;
    protected T object;

    public AbstractBuilder(Class<? extends T> theClass) {
        try {
            final Constructor<? extends T> declaredConstructor = theClass.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            this.object = declaredConstructor.newInstance();
            this.builder = (B) this;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            LOG.error("Cannot create builder. ", e);
        }
    }

    /**
     * Build entity T type object.
     */
    public T build() {
        return this.object;
    }
}
