package com.arturjarosz.task.sharedkernel.utils;

import com.arturjarosz.task.sharedkernel.model.AbstractEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Builder for Entities. It can create object from protected constructor.
 *
 * @param <T>
 * @param <B>
 */
public class AbstractBuilder<T extends AbstractEntity, B extends AbstractBuilder<T, B>> {

    protected B builder;
    protected T object;

    public AbstractBuilder(T object) {
        this.object = object;
        this.builder = (B) this;
    }

    public AbstractBuilder(Class<? extends T> theClass) {
        try {
            final Constructor<? extends T> declaredConstructor = theClass.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            this.object = declaredConstructor.newInstance();
            this.builder = (B) this;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * Build entity T type object.
     *
     * @return
     */
    public T build() {
        return this.object;
    }
}
